/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.hp.enums;

public enum ImagePlaneOrientation {
    SAGITTAL("SAGITTAL"),
    TRANSVERSE("TRANSVERSE"),
    CORONAL("CORONAL"),
    OBLIQUE("OBLIQUE");

    private final String codeString;

    private ImagePlaneOrientation(String codeString) {
        this.codeString = codeString;
    }

    public String getCodeString() {
        return this.codeString;
    }

    public String toString() {
        return this.codeString;
    }

    public static ImagePlaneOrientation fromString(String value) {
        for (ImagePlaneOrientation ipo : ImagePlaneOrientation.values()) {
            if (!ipo.codeString.equals(value)) continue;
            return ipo;
        }
        throw new IllegalArgumentException("Invalid Image Plane Orientation: " + value);
    }
}

