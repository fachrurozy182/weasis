/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.ui.tp.raven.spinner.SpinnerProgress
 *  org.weasis.dicom.param.CancelListener
 */
package org.weasis.dicom.explorer.exp;

import java.awt.Dimension;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JComponent;
import javax.swing.SwingWorker;
import org.weasis.core.ui.tp.raven.spinner.SpinnerProgress;
import org.weasis.dicom.param.CancelListener;

public abstract class ExplorerTask<T, V>
extends SwingWorker<T, V> {
    private final String message;
    private final boolean globalLoadingManager;
    private final SpinnerProgress bar;
    private final boolean subTask;
    private final List<CancelListener> cancelListeners;

    protected ExplorerTask(String message, boolean interruptible) {
        this(message, interruptible, false);
    }

    protected ExplorerTask(String message, boolean globalLoadingManager, boolean subTask) {
        this.message = message;
        this.globalLoadingManager = globalLoadingManager;
        this.bar = new SpinnerProgress();
        this.bar.setStringPainted(true);
        Dimension dim = this.bar.getUI().getPreferredSize((JComponent)this.bar);
        this.bar.setSize(dim.width, dim.height);
        this.bar.setPreferredSize(dim);
        this.bar.setMaximumSize(dim);
        this.subTask = subTask;
        this.cancelListeners = new CopyOnWriteArrayList<CancelListener>();
    }

    public boolean cancel() {
        this.stopProgress();
        this.fireCancelListeners();
        if (this.isDone()) {
            this.done();
        }
        return this.cancel(true);
    }

    public boolean isGlobalLoadingManager() {
        return this.globalLoadingManager;
    }

    public String getMessage() {
        return this.message;
    }

    public SpinnerProgress getBar() {
        return this.bar;
    }

    public boolean isSubTask() {
        return this.subTask;
    }

    public void stopProgress() {
        this.bar.setIndeterminate(false);
    }

    public void addCancelListener(CancelListener listener) {
        if (listener != null && !this.cancelListeners.contains(listener)) {
            this.cancelListeners.add(listener);
        }
    }

    public void removeCancelListener(CancelListener listener) {
        if (listener != null) {
            this.cancelListeners.remove(listener);
        }
    }

    public void removeAllCancelListeners() {
        this.cancelListeners.clear();
    }

    protected void fireCancelListeners() {
        for (CancelListener cancelListener : this.cancelListeners) {
            cancelListener.cancel();
        }
    }
}

