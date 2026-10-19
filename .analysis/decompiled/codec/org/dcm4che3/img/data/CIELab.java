/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.MathUtil
 */
package org.dcm4che3.img.data;

import java.awt.Color;
import java.util.Objects;
import org.weasis.core.util.MathUtil;

public final class CIELab {
    private static final double D65_WHITE_POINT_X = 0.950456;
    private static final double D65_WHITE_POINT_Y = 1.0;
    private static final double D65_WHITE_POINT_Z = 1.088754;
    private static final double LAB_THRESHOLD = 0.008856451679035631;
    private static final double LAB_INVERSE_THRESHOLD = 0.20689655172413793;
    private static final double LAB_ALPHA = 7.787037037037037;
    private static final double LAB_BETA = 0.13793103448275862;
    private static final double LAB_INVERSE_ALPHA = 0.12841854934601665;
    private static final double GAMMA_THRESHOLD = 0.0031306684425005883;
    private static final double GAMMA_INVERSE_THRESHOLD = 0.0404482362771076;
    private static final double GAMMA_LINEAR_COEFFICIENT = 12.92;
    private static final double GAMMA_POWER_COEFFICIENT = 1.055;
    private static final double GAMMA_POWER_OFFSET = 0.055;
    private static final double GAMMA_EXPONENT = 0.4166666666666667;
    private static final double GAMMA_INVERSE_EXPONENT = 2.4;
    private static final double[][] RGB_TO_XYZ_MATRIX = new double[][]{{0.41239558896741424, 0.3575834307637148, 0.18049264738170157}, {0.21258623078559555, 0.7151703037034108, 0.07220049864333623}, {0.019297215491746945, 0.11918386458084854, 0.9504971251315798}};
    private static final double[][] XYZ_TO_RGB_MATRIX = new double[][]{{3.2406, -1.5372, -0.4986}, {-0.9689, 1.8758, 0.0415}, {0.0557, -0.204, 1.057}};
    private static final double DICOM_L_SCALE = 655.35;
    private static final double DICOM_AB_SCALE = 257.0;
    private static final double DICOM_AB_OFFSET = 128.0;
    private static final double RGB_MAX_VALUE = 255.0;
    private static final int RGB_MIN = 0;
    private static final int DICOM_MAX = 65535;

    private CIELab() {
    }

    public static int[] dicomLab2rgb(int[] lab) {
        if (lab == null || lab.length != 3) {
            return new int[0];
        }
        double l = (double)lab[0] * 100.0 / 65535.0;
        double a = (double)lab[1] * 255.0 / 65535.0 - 128.0;
        double b = (double)lab[2] * 255.0 / 65535.0 - 128.0;
        double[] rgb = CIELab.labToRgb(l, a, b);
        return new int[]{(int)Math.round(MathUtil.clamp((double)rgb[0], (double)0.0, (double)1.0) * 255.0), (int)Math.round(MathUtil.clamp((double)rgb[1], (double)0.0, (double)1.0) * 255.0), (int)Math.round(MathUtil.clamp((double)rgb[2], (double)0.0, (double)1.0) * 255.0)};
    }

    public static int[] rgbToDicomLab(Color color) {
        Objects.requireNonNull(color, "Color cannot be null");
        return CIELab.rgbToDicomLab(color.getRed(), color.getGreen(), color.getBlue());
    }

    public static int[] rgbToDicomLab(int r, int g, int b) {
        double[] lab = CIELab.rgbToLab((double)r / 255.0, (double)g / 255.0, (double)b / 255.0);
        return new int[]{MathUtil.clamp((int)((int)Math.round(lab[0] * 655.35)), (int)0, (int)65535), MathUtil.clamp((int)((int)Math.round((lab[1] + 128.0) * 257.0)), (int)0, (int)65535), MathUtil.clamp((int)((int)Math.round((lab[2] + 128.0) * 257.0)), (int)0, (int)65535)};
    }

    private static double[] labToRgb(double l, double a, double b) {
        double[] xyz = CIELab.labToXyz(l, a, b);
        return CIELab.xyzToRgb(xyz);
    }

    private static double[] rgbToLab(double r, double g, double b) {
        double[] xyz = CIELab.rgbToXyz(new double[]{r, g, b});
        return CIELab.xyzToLab(xyz);
    }

    private static double[] labToXyz(double l, double a, double b) {
        double fy = (l + 16.0) / 116.0;
        double fx = fy + a / 500.0;
        double fz = fy - b / 200.0;
        return new double[]{0.950456 * CIELab.labfInverse(fx), 1.0 * CIELab.labfInverse(fy), 1.088754 * CIELab.labfInverse(fz)};
    }

    private static double[] xyzToLab(double[] xyz) {
        double x = CIELab.labf(xyz[0] / 0.950456);
        double y = CIELab.labf(xyz[1] / 1.0);
        double z = CIELab.labf(xyz[2] / 1.088754);
        return new double[]{116.0 * y - 16.0, 500.0 * (x - y), 200.0 * (y - z)};
    }

    private static double[] xyzToRgb(double[] xyz) {
        double[] rgb = CIELab.matrixMultiply(XYZ_TO_RGB_MATRIX, xyz);
        double min = Math.min(Math.min(rgb[0], rgb[1]), rgb[2]);
        if (min < 0.0) {
            rgb[0] = rgb[0] - min;
            rgb[1] = rgb[1] - min;
            rgb[2] = rgb[2] - min;
        }
        return new double[]{CIELab.gammaCorrection(rgb[0]), CIELab.gammaCorrection(rgb[1]), CIELab.gammaCorrection(rgb[2])};
    }

    private static double[] rgbToXyz(double[] rgb) {
        double[] linearRgb = new double[]{CIELab.inverseGammaCorrection(rgb[0]), CIELab.inverseGammaCorrection(rgb[1]), CIELab.inverseGammaCorrection(rgb[2])};
        return CIELab.matrixMultiply(RGB_TO_XYZ_MATRIX, linearRgb);
    }

    private static double[] matrixMultiply(double[][] matrix, double[] vector) {
        return new double[]{matrix[0][0] * vector[0] + matrix[0][1] * vector[1] + matrix[0][2] * vector[2], matrix[1][0] * vector[0] + matrix[1][1] * vector[1] + matrix[1][2] * vector[2], matrix[2][0] * vector[0] + matrix[2][1] * vector[1] + matrix[2][2] * vector[2]};
    }

    private static double labf(double t) {
        return t > 0.008856451679035631 ? Math.pow(t, 0.3333333333333333) : 7.787037037037037 * t + 0.13793103448275862;
    }

    private static double labfInverse(double t) {
        return t > 0.20689655172413793 ? t * t * t : 0.12841854934601665 * (t - 0.13793103448275862);
    }

    private static double gammaCorrection(double value) {
        return value <= 0.0031306684425005883 ? 12.92 * value : 1.055 * Math.pow(value, 0.4166666666666667) - 0.055;
    }

    private static double inverseGammaCorrection(double value) {
        return value <= 0.0404482362771076 ? value / 12.92 : Math.pow((value + 0.055) / 1.055, 2.4);
    }
}

