/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.editor.image;

import javax.swing.JProgressBar;

public interface ViewProgress {
    public void setProgressBar(JProgressBar var1);

    public JProgressBar getProgressBar();

    public int getWidth();

    public void repaint();
}

