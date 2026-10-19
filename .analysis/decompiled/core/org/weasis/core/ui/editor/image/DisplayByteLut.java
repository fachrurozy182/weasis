/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.opencv.op.lut.ByteLut
 */
package org.weasis.core.ui.editor.image;

import org.weasis.opencv.op.lut.ByteLut;

public class DisplayByteLut {
    private boolean invert;
    private final ByteLut byteLut;

    public DisplayByteLut(ByteLut lut) {
        this.byteLut = lut;
    }

    public ByteLut getByteLut() {
        return this.byteLut;
    }

    public boolean isInvert() {
        return this.invert;
    }

    public void setInvert(boolean invert) {
        this.invert = invert;
    }
}

