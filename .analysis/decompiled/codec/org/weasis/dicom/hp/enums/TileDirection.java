/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.hp.enums;

public enum TileDirection {
    VERTICAL("VERTICAL"),
    HORIZONTAL("HORIZONTAL");

    private final String codeString;

    private TileDirection(String codeString) {
        this.codeString = codeString;
    }

    public String getCodeString() {
        return this.codeString;
    }

    public String toString() {
        return this.codeString;
    }

    public static TileDirection fromString(String value) {
        for (TileDirection td : TileDirection.values()) {
            if (!td.codeString.equals(value)) continue;
            return td;
        }
        throw new IllegalArgumentException("Invalid Tile Direction: " + value);
    }
}

