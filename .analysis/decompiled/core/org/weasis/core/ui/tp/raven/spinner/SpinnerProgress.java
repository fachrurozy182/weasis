/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.tp.raven.spinner;

import javax.swing.Icon;
import javax.swing.JProgressBar;
import javax.swing.plaf.ProgressBarUI;
import org.weasis.core.api.util.FontItem;
import org.weasis.core.ui.tp.raven.spinner.SpinnerProgressUI;

public class SpinnerProgress
extends JProgressBar {
    private Icon icon;
    private int verticalAlignment = 0;
    private int horizontalAlignment = 0;
    private int verticalTextPosition = 0;
    private int horizontalTextPosition = 11;
    private int iconTextGap = 4;
    private int space = 10;

    public SpinnerProgress() {
        this.init();
    }

    public SpinnerProgress(Icon icon) {
        this();
        this.icon = icon;
    }

    public SpinnerProgress(int min, int max) {
        super(min, max);
        this.init();
    }

    @Override
    public void updateUI() {
        this.setUI((ProgressBarUI)((Object)new SpinnerProgressUI()));
        this.setFont(FontItem.MICRO_SEMIBOLD.getFont());
    }

    private void init() {
        this.updateUI();
    }

    public Icon getIcon() {
        return this.icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
        this.repaint();
        this.revalidate();
    }

    public int getVerticalAlignment() {
        return this.verticalAlignment;
    }

    public void setVerticalAlignment(int alignment) {
        if (this.verticalAlignment != alignment) {
            this.verticalAlignment = alignment;
            this.revalidate();
        }
    }

    public int getHorizontalAlignment() {
        return this.horizontalAlignment;
    }

    public void setHorizontalAlignment(int alignment) {
        if (this.horizontalAlignment != alignment) {
            this.horizontalAlignment = alignment;
            this.revalidate();
        }
    }

    public int getVerticalTextPosition() {
        return this.verticalTextPosition;
    }

    public void setVerticalTextPosition(int textPosition) {
        if (this.verticalTextPosition != textPosition) {
            this.verticalTextPosition = textPosition;
            this.revalidate();
        }
    }

    public int getHorizontalTextPosition() {
        return this.horizontalTextPosition;
    }

    public void setHorizontalTextPosition(int textPosition) {
        if (this.horizontalTextPosition != textPosition) {
            this.horizontalTextPosition = textPosition;
            this.revalidate();
        }
    }

    public int getIconTextGap() {
        return this.iconTextGap;
    }

    public void setIconTextGap(int iconTextGap) {
        if (this.iconTextGap != iconTextGap) {
            this.iconTextGap = iconTextGap;
            this.revalidate();
        }
    }

    public int getSpace() {
        return this.space;
    }

    public void setSpace(int space) {
        if (this.space != space) {
            this.space = space;
            this.revalidate();
        }
    }
}

