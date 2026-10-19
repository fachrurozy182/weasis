/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.CvType
 *  org.opencv.core.Mat
 *  org.opencv.imgproc.Imgproc
 *  org.weasis.core.util.MathUtil
 *  org.weasis.core.util.Pair
 *  org.weasis.opencv.data.ImageCV
 *  org.weasis.opencv.data.PlanarImage
 */
package org.dcm4che3.img.util;

import org.dcm4che3.data.VR;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.weasis.core.util.MathUtil;
import org.weasis.core.util.Pair;
import org.weasis.opencv.data.ImageCV;
import org.weasis.opencv.data.PlanarImage;

public final class PixelDataUtils {
    private static final int MAX_BITS_STORED = 16;
    private static final int MIN_BITS_STORED = 1;

    private PixelDataUtils() {
    }

    public static Pair<Double, Double> getMinMax(int bitsStored, boolean signed) {
        int clampedBits = MathUtil.clamp((int)bitsStored, (int)1, (int)16);
        return signed ? PixelDataUtils.calculateSignedRange(clampedBits) : PixelDataUtils.calculateUnsignedRange(clampedBits);
    }

    public static PlanarImage bgr2rgb(PlanarImage img) {
        return PixelDataUtils.requiresColorConversion(img) ? PixelDataUtils.performColorConversion(img, 4) : img;
    }

    public static PlanarImage rgb2bgr(PlanarImage img) {
        return PixelDataUtils.requiresColorConversion(img) ? PixelDataUtils.performColorConversion(img, 4) : img;
    }

    public static VR getAppropriateVR(int cvType) {
        int depth = CvType.depth((int)cvType);
        return switch (depth) {
            case 0, 1 -> VR.OB;
            case 2, 3 -> VR.OW;
            case 4 -> VR.OL;
            case 5 -> VR.OF;
            case 6 -> VR.OD;
            default -> VR.UN;
        };
    }

    public static int getBitsAllocatedFromVR(VR vr) {
        return switch (vr) {
            case VR.OB -> 8;
            case VR.OW -> 16;
            case VR.OL, VR.OF -> 32;
            case VR.OD -> 64;
            default -> 0;
        };
    }

    public static int getBitsAllocatedFromCvType(int cvType) {
        int depth = CvType.depth((int)cvType);
        return switch (depth) {
            case 0, 1 -> 8;
            case 2, 3 -> 16;
            case 4, 5 -> 32;
            case 6 -> 64;
            default -> 0;
        };
    }

    public static boolean isSignedCvType(int cvType) {
        int depth = CvType.depth((int)cvType);
        return depth == 1 || depth >= 3;
    }

    private static Pair<Double, Double> calculateSignedRange(int bitsStored) {
        double minValue = -(1L << bitsStored - 1);
        double maxValue = (double)(1L << bitsStored - 1) - 1.0;
        return new Pair((Object)minValue, (Object)maxValue);
    }

    private static Pair<Double, Double> calculateUnsignedRange(int bitsStored) {
        return new Pair((Object)0.0, (Object)((double)(1L << bitsStored) - 1.0));
    }

    private static boolean requiresColorConversion(PlanarImage img) {
        return img != null && img.channels() > 1;
    }

    private static PlanarImage performColorConversion(PlanarImage img, int colorConversionCode) {
        ImageCV dstImg = new ImageCV();
        Imgproc.cvtColor((Mat)img.toMat(), (Mat)dstImg, (int)colorConversionCode);
        return dstImg;
    }
}

