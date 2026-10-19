/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StreamUtil
 */
package org.weasis.dicom.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.StreamUtil;
import org.weasis.dicom.param.DicomProgress;
import org.weasis.dicom.param.DicomState;

public final class ServiceUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceUtil.class);
    private static final int MIN_REMAINING_OPERATIONS = 1;

    private ServiceUtil() {
    }

    public static ThreadFactory getThreadFactory(String name) {
        Objects.requireNonNull(name, "Thread factory name cannot be null");
        return runnable -> {
            Thread thread = Executors.defaultThreadFactory().newThread(runnable);
            thread.setName(name + "-" + thread.getName());
            return thread;
        };
    }

    public static void shutdownService(ExecutorService executorService) {
        if (executorService != null) {
            try {
                executorService.shutdown();
            }
            catch (Exception e) {
                LOGGER.error("ExecutorService shutdown failed", (Throwable)e);
            }
        }
    }

    public static void forceGettingAttributes(DicomState dcmState, AutoCloseable closeable) {
        DicomProgress progress = dcmState.getProgress();
        if (progress != null) {
            StreamUtil.safeClose((AutoCloseable)closeable);
        }
    }

    public static void safeClose(DicomInputStream inputStream) {
        if (inputStream != null) {
            inputStream.getBulkDataFiles().forEach(file -> ServiceUtil.deleteBulkDataFile(file.toPath()));
        }
    }

    public static void notifyProgression(DicomState state, String instanceUID, String classUID, int status, ProgressStatus progressStatus, int numberOfSuboperations) {
        state.setStatus(status);
        DicomProgress progress = state.getProgress();
        if (progress == null) {
            return;
        }
        Attributes attributes = Optional.ofNullable(progress.getAttributes()).orElseGet(Attributes::new);
        ServiceUtil.setStatusAttributes(attributes, instanceUID, classUID, status);
        ServiceUtil.notifyProgression(progress, attributes, progressStatus, numberOfSuboperations);
        progress.setAttributes(attributes);
    }

    public static void notifyProgression(DicomProgress progress, Attributes attributes, ProgressStatus progressStatus, int numberOfSuboperations) {
        if (progress == null || attributes == null) {
            return;
        }
        OperationCounts operationCounts = ServiceUtil.calculateOperationCounts(progress, numberOfSuboperations);
        OperationCounts updatedCounts = ServiceUtil.updateCountsForStatus(operationCounts, progressStatus);
        ServiceUtil.setOperationCountAttributes(attributes, updatedCounts);
    }

    public static int getTotalOfSuboperations(Attributes attributes) {
        if (attributes == null) {
            return 0;
        }
        return attributes.getInt(4129, 0) + attributes.getInt(4130, 0) + attributes.getInt(4131, 0) + attributes.getInt(4128, 0);
    }

    private static void deleteBulkDataFile(Path filePath) {
        try {
            Files.deleteIfExists(filePath);
        }
        catch (Exception e) {
            LOGGER.warn("Failed to delete bulk data file: {}", (Object)filePath, (Object)e);
        }
    }

    private static void setStatusAttributes(Attributes attributes, String instanceUID, String classUID, int status) {
        attributes.setInt(2304, VR.US, status);
        attributes.setString(4096, VR.UI, instanceUID);
        attributes.setString(2, VR.UI, classUID);
    }

    private static OperationCounts calculateOperationCounts(DicomProgress progress, int numberOfSuboperations) {
        if (progress.getAttributes() == null) {
            return new OperationCounts(0, 0, 0, numberOfSuboperations);
        }
        int completed = progress.getNumberOfCompletedSuboperations();
        int failed = progress.getNumberOfFailedSuboperations();
        int warning = progress.getNumberOfWarningSuboperations();
        int remaining = numberOfSuboperations - (completed + failed + warning);
        return new OperationCounts(completed, failed, warning, Math.max(remaining, 1));
    }

    private static OperationCounts updateCountsForStatus(OperationCounts counts, ProgressStatus status) {
        return switch (status) {
            default -> throw new IncompatibleClassChangeError();
            case ProgressStatus.COMPLETED -> new OperationCounts(counts.completed + 1, counts.failed, counts.warning, counts.remaining);
            case ProgressStatus.FAILED -> new OperationCounts(counts.completed, counts.failed + 1, counts.warning, counts.remaining);
            case ProgressStatus.WARNING -> new OperationCounts(counts.completed, counts.failed, counts.warning + 1, counts.remaining);
        };
    }

    private static void setOperationCountAttributes(Attributes attributes, OperationCounts counts) {
        attributes.setInt(4129, VR.US, counts.completed);
        attributes.setInt(4130, VR.US, counts.failed);
        attributes.setInt(4131, VR.US, counts.warning);
        attributes.setInt(4128, VR.US, counts.remaining - 1);
    }

    public static enum ProgressStatus {
        FAILED,
        WARNING,
        COMPLETED;

    }

    private record OperationCounts(int completed, int failed, int warning, int remaining) {
    }
}

