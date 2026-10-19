/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.param;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.dcm4che3.data.Attributes;
import org.weasis.dicom.param.CancelListener;
import org.weasis.dicom.param.ProgressListener;

public class DicomProgress
implements CancelListener {
    private final List<ProgressListener> listeners = new CopyOnWriteArrayList<ProgressListener>();
    private final List<CancelListener> cancelListeners = new CopyOnWriteArrayList<CancelListener>();
    private final AtomicReference<Attributes> attributes = new AtomicReference();
    private final AtomicReference<Path> processedFile = new AtomicReference();
    private final AtomicBoolean cancelled = new AtomicBoolean();
    private final AtomicBoolean aborted = new AtomicBoolean();
    private volatile boolean lastFailed;

    public Attributes getAttributes() {
        return this.attributes.get();
    }

    public void setAttributes(Attributes attributes) {
        int previousFailed = this.getFailedCount();
        this.attributes.set(attributes);
        this.lastFailed = previousFailed >= 0 && previousFailed < this.getFailedCount();
        this.notifyListeners();
    }

    public boolean isLastFailed() {
        return this.lastFailed;
    }

    public Path getProcessedFile() {
        return this.processedFile.get();
    }

    public void setProcessedFile(Path processedFile) {
        this.processedFile.set(processedFile);
    }

    public void addProgressListener(ProgressListener listener) {
        if (listener != null && !this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    public void removeProgressListener(ProgressListener listener) {
        this.listeners.remove(listener);
    }

    public void addCancelListener(CancelListener listener) {
        if (listener != null && !this.cancelListeners.contains(listener)) {
            this.cancelListeners.add(listener);
            if (this.cancelled.get()) {
                listener.cancel();
            }
        }
    }

    public void removeCancelListener(CancelListener listener) {
        this.cancelListeners.remove(listener);
    }

    @Override
    public void cancel() {
        if (this.cancelled.compareAndSet(false, true)) {
            this.cancelListeners.forEach(CancelListener::cancel);
        }
    }

    public void abort() {
        this.cancelled.set(true);
        if (this.aborted.compareAndSet(false, true)) {
            this.cancelListeners.forEach(CancelListener::cancel);
        }
    }

    public boolean isCancelled() {
        return this.cancelled.get();
    }

    public boolean isAborted() {
        return this.aborted.get();
    }

    public int getStatus() {
        if (this.cancelled.get()) {
            return 65024;
        }
        Attributes dcm = this.attributes.get();
        return dcm != null ? dcm.getInt(2304, 65280) : 65280;
    }

    public String getErrorComment() {
        Attributes dcm = this.attributes.get();
        return dcm != null ? dcm.getString(2306) : null;
    }

    public int getNumberOfRemainingSuboperations() {
        return this.getTagValue(4128);
    }

    public int getNumberOfCompletedSuboperations() {
        return this.getTagValue(4129);
    }

    public int getNumberOfFailedSuboperations() {
        return this.getTagValue(4130);
    }

    public int getNumberOfWarningSuboperations() {
        return this.getTagValue(4131);
    }

    private void notifyListeners() {
        this.listeners.forEach(listener -> listener.handleProgression(this));
    }

    private int getTagValue(int tag) {
        Attributes dcm = this.attributes.get();
        return dcm != null ? dcm.getInt(tag, -1) : -1;
    }

    private int getFailedCount() {
        return this.getTagValue(4130);
    }
}

