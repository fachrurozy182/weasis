/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui.task;

import java.awt.Component;
import javax.swing.ProgressMonitor;

public class TaskMonitor
extends ProgressMonitor {
    private volatile boolean showProgression = true;
    private volatile boolean aborting = false;

    public TaskMonitor(Component parentComponent, Object message, String note, int min, int max) {
        super(parentComponent, message, note, min, max);
    }

    public boolean isShowProgression() {
        return this.showProgression;
    }

    public void setShowProgression(boolean showProgression) {
        this.showProgression = showProgression;
    }

    public boolean isAborting() {
        return this.aborting || this.isCanceled();
    }

    public void setAborting(boolean aborting) {
        this.aborting = aborting;
    }
}

