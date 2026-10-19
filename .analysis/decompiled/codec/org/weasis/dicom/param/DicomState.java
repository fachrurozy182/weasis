/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.param;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.Status;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.param.DicomParam;
import org.weasis.dicom.param.DicomProgress;

public class DicomState {
    private static final int INVALID_STATUS_FLAG = -1;
    private final List<Attributes> dicomRSP;
    private final DicomProgress progress;
    private final List<DicomParam> dicomMatchingKeys;
    private volatile int status;
    private volatile String message;
    private volatile String errorMessage;
    private LocalDateTime startConnectionDateTime;
    private LocalDateTime startTransferDateTime;
    private LocalDateTime endTransferDateTime;
    private long bytesSize = -1L;

    public DicomState() {
        this(65280, null, null);
    }

    public DicomState(DicomProgress progress) {
        this(65280, null, progress);
    }

    public DicomState(int status, String message, DicomProgress progress) {
        this.status = status;
        this.message = message;
        this.progress = progress;
        this.dicomRSP = new ArrayList<Attributes>();
        this.dicomMatchingKeys = new ArrayList<DicomParam>();
    }

    public int getStatus() {
        if (this.progress != null && this.progress.getAttributes() != null) {
            return this.progress.getStatus();
        }
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public synchronized String getMessage() {
        return this.message;
    }

    public synchronized void setMessage(String message) {
        this.message = message;
    }

    public DicomProgress getProgress() {
        return this.progress;
    }

    public List<Attributes> getDicomRSP() {
        return List.copyOf(this.dicomRSP);
    }

    public List<DicomParam> getDicomMatchingKeys() {
        return List.copyOf(this.dicomMatchingKeys);
    }

    public LocalDateTime getStartTransferDateTime() {
        return this.startTransferDateTime;
    }

    public void setStartTransferDateTime(LocalDateTime startTransferDateTime) {
        this.startTransferDateTime = startTransferDateTime;
    }

    public LocalDateTime getEndTransferDateTime() {
        return this.endTransferDateTime;
    }

    public void setEndTransferDateTime(LocalDateTime endTransferDateTime) {
        this.endTransferDateTime = endTransferDateTime;
    }

    public LocalDateTime getStartConnectionDateTime() {
        return this.startConnectionDateTime;
    }

    public void setStartConnectionDateTime(LocalDateTime startConnectionDateTime) {
        this.startConnectionDateTime = startConnectionDateTime;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getBytesSize() {
        return this.bytesSize;
    }

    public void setBytesSize(long bytesSize) {
        this.bytesSize = bytesSize;
    }

    public Duration getProcessTime() {
        if (this.startTransferDateTime != null && this.endTransferDateTime != null) {
            ZoneId zone = ZoneId.systemDefault();
            return Duration.between(this.startTransferDateTime.atZone(zone), this.endTransferDateTime.atZone(zone));
        }
        return null;
    }

    public void addDicomRSP(Attributes dicomRSP) {
        if (dicomRSP != null) {
            this.dicomRSP.add(dicomRSP);
        }
    }

    public void addDicomMatchingKeys(DicomParam param) {
        if (param != null) {
            this.dicomMatchingKeys.add(param);
        }
    }

    public void addProcessTime(long startTimeStamp, long endTimeStamp) {
        this.addProcessTime(0L, startTimeStamp, endTimeStamp);
    }

    public void addProcessTime(long connectionTimeStamp, long startTimeStamp, long endTimeStamp) {
        if (connectionTimeStamp > 0L) {
            this.setStartConnectionDateTime(this.timestampToDateTime(connectionTimeStamp));
        }
        if (startTimeStamp > 0L) {
            this.setStartTransferDateTime(this.timestampToDateTime(startTimeStamp));
        }
        if (endTimeStamp > 0L) {
            this.setEndTransferDateTime(this.timestampToDateTime(endTimeStamp));
        }
    }

    private LocalDateTime timestampToDateTime(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static DicomState buildMessage(DicomState dcmState, String timeMessage, Exception exception) {
        DicomState state = Objects.requireNonNullElse(dcmState, new DicomState(49152, null, null));
        StringBuilder messageBuilder = new StringBuilder();
        boolean hasFailed = DicomState.appendProgressInfo(state.getProgress(), messageBuilder);
        hasFailed |= DicomState.appendExceptionInfo(exception, messageBuilder, state);
        DicomState.appendFinalMessage(hasFailed |= DicomState.appendDicomErrorInfo(state.getProgress(), state.getStatus(), messageBuilder), timeMessage, messageBuilder, state);
        state.setMessage(messageBuilder.toString());
        return state;
    }

    private static boolean appendProgressInfo(DicomProgress progress, StringBuilder messageBuilder) {
        if (progress == null) {
            return false;
        }
        int failed = progress.getNumberOfFailedSuboperations();
        int warning = progress.getNumberOfWarningSuboperations();
        int remaining = progress.getNumberOfRemainingSuboperations();
        int completed = progress.getNumberOfCompletedSuboperations();
        if (failed > 0) {
            messageBuilder.append(String.format("%d/%d operations has failed.", failed, failed + completed));
            return true;
        }
        if (remaining > 0) {
            messageBuilder.append(String.format("%d operations remains. ", remaining));
        } else if (warning > 0) {
            messageBuilder.append(String.format("%d operations has a warning status. ", warning));
        }
        return false;
    }

    private static boolean appendExceptionInfo(Exception exception, StringBuilder messageBuilder, DicomState state) {
        if (exception == null) {
            return false;
        }
        if (!messageBuilder.isEmpty()) {
            messageBuilder.append(" ");
        }
        messageBuilder.append(exception.getMessage());
        state.setErrorMessage(exception.getMessage());
        return true;
    }

    private static boolean appendDicomErrorInfo(DicomProgress progress, int status, StringBuilder messageBuilder) {
        if (progress == null || progress.getAttributes() == null) {
            return false;
        }
        boolean hasFailed = false;
        String error = progress.getErrorComment();
        if (StringUtil.hasText((CharSequence)error)) {
            hasFailed = true;
            if (!messageBuilder.isEmpty()) {
                messageBuilder.append("\n");
            }
            messageBuilder.append("DICOM error").append(": ").append(error);
        }
        if (DicomState.isErrorStatus(status)) {
            if (!messageBuilder.isEmpty()) {
                messageBuilder.append("\n");
            }
            messageBuilder.append("DICOM status").append(": ").append(status);
        }
        return hasFailed;
    }

    private static boolean isErrorStatus(int status) {
        return !Status.isPending(status) && status != -1 && status != 0 && status != 65024;
    }

    private static void appendFinalMessage(boolean hasFailed, String timeMessage, StringBuilder messageBuilder, DicomState state) {
        int status;
        if (!hasFailed && timeMessage != null) {
            messageBuilder.append(timeMessage);
        } else if (hasFailed && (Status.isPending(status = state.getStatus()) || status == -1)) {
            state.setStatus(49152);
        }
    }
}

