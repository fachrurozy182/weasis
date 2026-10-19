/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.media.data;

import javax.swing.JProgressBar;

public interface SeriesImporter {
    public boolean isStopped();

    public JProgressBar getProgressBar();

    public boolean stop();

    public void resume();

    public void setPriority();
}

