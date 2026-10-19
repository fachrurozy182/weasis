/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.image;

import java.awt.color.ColorSpace;

public class CIELabColorSpace
extends ColorSpace {
    private static final double D = 0.13793103448275862;
    private static final double DELTA = 0.20689655172413793;
    private static final double DELTA_3 = 0.008856451679035631;
    private static final double THREE_DELTA_2 = 0.12841854934601665;
    private final ColorSpace CIEXYZ;

    CIELabColorSpace(ColorSpace ciexyz) {
        super(1, 3);
        this.CIEXYZ = ciexyz;
    }

    public static CIELabColorSpace getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public float getMaxValue(int component) {
        if (component < 0 || component > 2) {
            throw new IllegalArgumentException("Component index out of range: " + component);
        }
        return component == 0 ? 100.0f : 127.0f;
    }

    @Override
    public float getMinValue(int component) {
        if (component < 0 || component > 2) {
            throw new IllegalArgumentException("Component index out of range: " + component);
        }
        return component == 0 ? 0.0f : -128.0f;
    }

    @Override
    public float[] fromCIEXYZ(float[] colorvalue) {
        double l = CIELabColorSpace.f(colorvalue[1]);
        double L = 116.0 * l - 16.0;
        double a = 500.0 * (CIELabColorSpace.f(colorvalue[0]) - l);
        double b = 200.0 * (l - CIELabColorSpace.f(colorvalue[2]));
        return new float[]{(float)L, (float)a, (float)b};
    }

    @Override
    public float[] fromRGB(float[] rgbvalue) {
        float[] xyz = this.CIEXYZ.fromRGB(rgbvalue);
        return this.fromCIEXYZ(xyz);
    }

    @Override
    public float[] toCIEXYZ(float[] colorvalue) {
        double l = ((double)colorvalue[0] + 16.0) / 116.0;
        double X = CIELabColorSpace.fInv(l + (double)colorvalue[1] / 500.0);
        double Y = CIELabColorSpace.fInv(l);
        double Z = CIELabColorSpace.fInv(l - (double)colorvalue[2] / 200.0);
        return new float[]{(float)X, (float)Y, (float)Z};
    }

    @Override
    public float[] toRGB(float[] colorvalue) {
        float[] xyz = this.toCIEXYZ(colorvalue);
        return this.CIEXYZ.toRGB(xyz);
    }

    private static double f(double t) {
        return t > 0.008856451679035631 ? Math.cbrt(t) : t / 0.12841854934601665 + 0.13793103448275862;
    }

    private static double fInv(double t) {
        return t > 0.20689655172413793 ? t * t * t : 0.12841854934601665 * (t - 0.13793103448275862);
    }

    private Object readResolve() {
        return CIELabColorSpace.getInstance();
    }

    private static class LazyHolder {
        static final CIELabColorSpace INSTANCE = new CIELabColorSpace(ColorSpace.getInstance(1001));

        private LazyHolder() {
        }
    }
}

