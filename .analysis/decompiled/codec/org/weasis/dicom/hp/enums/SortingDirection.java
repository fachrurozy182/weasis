/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.hp.enums;

public enum SortingDirection {
    INCREASING("INCREASING", 1),
    DECREASING("DECREASING", -1);

    private final String codeString;
    private final int sign;

    private SortingDirection(String codeString, int sign) {
        this.codeString = codeString;
        this.sign = sign;
    }

    public String getCodeString() {
        return this.codeString;
    }

    public int getSign() {
        return this.sign;
    }

    public String toString() {
        return this.codeString;
    }

    public static SortingDirection fromString(String value) {
        for (SortingDirection sd : SortingDirection.values()) {
            if (!sd.codeString.equals(value)) continue;
            return sd;
        }
        throw new IllegalArgumentException("Invalid (0072,0604) Sorting Direction: " + value);
    }
}

