/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.geom;

import java.awt.Color;
import java.util.Objects;
import org.weasis.dicom.geom.Orientation;
import org.weasis.dicom.geom.PatientOrientation;
import org.weasis.dicom.geom.Vector3;

public final class ImageOrientation {
    public static final double DEFAULT_OBLIQUITY_THRESHOLD = 0.8;

    private ImageOrientation() {
    }

    public static Plan getPlan(Vector3 rowVector, Vector3 columnVector) {
        return ImageOrientation.getPlan(rowVector, columnVector, 0.8);
    }

    public static Plan getPlan(Vector3 rowVector, Vector3 columnVector, double threshold) {
        Objects.requireNonNull(rowVector, "Row vector cannot be null");
        Objects.requireNonNull(columnVector, "Column vector cannot be null");
        ImageOrientation.validateThreshold(threshold);
        Orientation rowAxis = ImageOrientation.getPatientOrientation(rowVector, threshold, false);
        Orientation colAxis = ImageOrientation.getPatientOrientation(columnVector, threshold, false);
        return ImageOrientation.determinePlanFromOrientations(rowAxis, colAxis);
    }

    public static Plan getPlan(Orientation rowOrientation, Orientation columnOrientation) {
        return ImageOrientation.determinePlanFromOrientations(rowOrientation, columnOrientation);
    }

    public static Orientation getPatientOrientation(Vector3 vector, double minCosine, boolean quadruped) {
        double absZ;
        double absY;
        double absX = Math.abs(vector.x());
        if (ImageOrientation.isDominantAxis(absX, absY = Math.abs(vector.y()), absZ = Math.abs(vector.z()), minCosine)) {
            return ImageOrientation.getXAxisOrientation(vector, quadruped);
        }
        if (ImageOrientation.isDominantAxis(absY, absX, absZ, minCosine)) {
            return ImageOrientation.getYAxisOrientation(vector, quadruped);
        }
        if (ImageOrientation.isDominantAxis(absZ, absX, absY, minCosine)) {
            return ImageOrientation.getZAxisOrientation(vector, quadruped);
        }
        return null;
    }

    public static Orientation getXAxisOrientation(Vector3 vector, boolean quadruped) {
        return quadruped ? PatientOrientation.getQuadrupedXOrientation(vector) : PatientOrientation.getBipedXOrientation(vector);
    }

    public static Orientation getYAxisOrientation(Vector3 vector, boolean quadruped) {
        return quadruped ? PatientOrientation.getQuadrupedYOrientation(vector) : PatientOrientation.getBipedYOrientation(vector);
    }

    public static Orientation getZAxisOrientation(Vector3 vector, boolean quadruped) {
        return quadruped ? PatientOrientation.getQuadrupedZOrientation(vector) : PatientOrientation.getBipedZOrientation(vector);
    }

    private static void validateThreshold(double threshold) {
        if (threshold < 0.0 || threshold > 1.0 || Double.isNaN(threshold)) {
            throw new IllegalArgumentException("Threshold must be between 0.0 and 1.0, got: " + threshold);
        }
    }

    private static boolean isDominantAxis(double primary, double secondary1, double secondary2, double threshold) {
        return primary > threshold && primary > secondary1 && primary > secondary2;
    }

    private static Plan determinePlanFromOrientations(Orientation rowAxis, Orientation colAxis) {
        if (rowAxis == null || colAxis == null) {
            return Plan.OBLIQUE;
        }
        return switch (ImageOrientation.getColorPairType(rowAxis.getColor(), colAxis.getColor())) {
            default -> throw new IncompatibleClassChangeError();
            case ColorPairType.BLUE_RED -> Plan.TRANSVERSE;
            case ColorPairType.BLUE_GREEN -> Plan.CORONAL;
            case ColorPairType.RED_GREEN -> Plan.SAGITTAL;
            case ColorPairType.UNKNOWN -> Plan.OBLIQUE;
        };
    }

    private static ColorPairType getColorPairType(Color rowColor, Color colColor) {
        if (ImageOrientation.isColorCombination(rowColor, colColor, PatientOrientation.BLUE, PatientOrientation.RED)) {
            return ColorPairType.BLUE_RED;
        }
        if (ImageOrientation.isColorCombination(rowColor, colColor, PatientOrientation.BLUE, PatientOrientation.GREEN)) {
            return ColorPairType.BLUE_GREEN;
        }
        if (ImageOrientation.isColorCombination(rowColor, colColor, PatientOrientation.RED, PatientOrientation.GREEN)) {
            return ColorPairType.RED_GREEN;
        }
        return ColorPairType.UNKNOWN;
    }

    private static boolean isColorCombination(Color rowColor, Color colColor, Color color1, Color color2) {
        return rowColor.equals(color1) && colColor.equals(color2) || rowColor.equals(color2) && colColor.equals(color1);
    }

    public static enum Plan {
        UNKNOWN("Unknown"),
        TRANSVERSE("Axial"),
        SAGITTAL("Sagittal"),
        CORONAL("Coronal"),
        OBLIQUE("Oblique");

        private final String displayName;

        private Plan(String displayName) {
            this.displayName = displayName;
        }

        public String toString() {
            return this.displayName;
        }
    }

    private static enum ColorPairType {
        BLUE_RED,
        BLUE_GREEN,
        RED_GREEN,
        UNKNOWN;

    }
}

