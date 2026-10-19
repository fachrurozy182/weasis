/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui.util;

import javax.swing.BoundedRangeModel;
import javax.swing.JSlider;

public class JSliderW
extends JSlider {
    private int labelDivision = 4;
    private boolean displayValueInTitle = true;
    private boolean showLabels = true;

    public JSliderW(BoundedRangeModel brm) {
        super(brm);
    }

    public JSliderW(int orientation, int min, int max, int value) {
        super(orientation, min, max, value);
    }

    public JSliderW(int min, int max, int value) {
        super(min, max, value);
    }

    public JSliderW(int min, int max) {
        super(min, max);
    }

    public JSliderW(int orientation) {
        super(orientation);
    }

    public int getLabelDivision() {
        return this.labelDivision;
    }

    public void setLabelDivision(int labelDivision) {
        this.labelDivision = labelDivision;
    }

    public boolean isDisplayValueInTitle() {
        return this.displayValueInTitle;
    }

    public void setDisplayValueInTitle(boolean displayOnlyValue) {
        this.displayValueInTitle = displayOnlyValue;
    }

    public boolean isShowLabels() {
        return this.showLabels;
    }

    public void setShowLabels(boolean showLabels) {
        this.showLabels = showLabels;
    }
}

