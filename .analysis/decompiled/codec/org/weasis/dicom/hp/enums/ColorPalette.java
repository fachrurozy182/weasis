/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.hp.enums;

public enum ColorPalette {
    BLACK_BODY("BLACK_BODY"),
    HOT_IRON("HOT_IRON"),
    DEFAULT("DEFAULT");

    private final String codeString;

    private ColorPalette(String codeString) {
        this.codeString = codeString;
    }

    public String getCodeString() {
        return this.codeString;
    }

    public String toString() {
        return this.codeString;
    }

    public static ColorPalette fromString(String value) {
        for (ColorPalette cp : ColorPalette.values()) {
            if (!cp.codeString.equals(value)) continue;
            return cp;
        }
        throw new IllegalArgumentException("Invalid Color Palette: " + value);
    }
}

