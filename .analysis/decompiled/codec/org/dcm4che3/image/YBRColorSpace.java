/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.image;

import java.awt.color.ColorSpace;
import org.dcm4che3.image.YBR;

public final class YBRColorSpace
extends ColorSpace {
    private static final long serialVersionUID = 94133999660773774L;
    private final ColorSpace csRGB;
    private final YBR ybr;

    public YBRColorSpace(ColorSpace csRGB, YBR ybr) {
        super(3, 3);
        this.csRGB = csRGB;
        this.ybr = ybr;
    }

    @Override
    public float[] toRGB(float[] ybr) {
        return this.ybr.toRGB(ybr);
    }

    @Override
    public float[] fromRGB(float[] rgb) {
        return this.ybr.fromRGB(rgb);
    }

    @Override
    public float[] toCIEXYZ(float[] colorvalue) {
        return this.csRGB.toCIEXYZ(this.toRGB(colorvalue));
    }

    @Override
    public float[] fromCIEXYZ(float[] xyzvalue) {
        return this.fromRGB(this.csRGB.fromCIEXYZ(xyzvalue));
    }
}

