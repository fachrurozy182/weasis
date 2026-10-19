/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.hp.enums;

public enum ImageBoxLayoutType {
    TILED("TILED"),
    STACK("STACK"),
    CINE("CINE"),
    PROCESSED("PROCESSED"),
    SINGLE("SINGLE");

    private final String codeString;

    private ImageBoxLayoutType(String codeString) {
        this.codeString = codeString;
    }

    public String getCodeString() {
        return this.codeString;
    }

    public String toString() {
        return this.codeString;
    }

    public static ImageBoxLayoutType fromString(String value) {
        for (ImageBoxLayoutType iblt : ImageBoxLayoutType.values()) {
            if (!iblt.codeString.equals(value)) continue;
            return iblt;
        }
        throw new IllegalArgumentException("Invalid Image Box Layout Type: " + value);
    }
}

