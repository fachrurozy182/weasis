/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.hp.enums;

public enum HangingProtocolLevel {
    MANUFACTURER("MANUFACTURER"),
    SITE("SITE"),
    SINGLE_USER("SINGLE_USER"),
    USER_GROUP("USER_GROUP");

    private final String codeString;

    private HangingProtocolLevel(String codeString) {
        this.codeString = codeString;
    }

    public String getCodeString() {
        return this.codeString;
    }

    public String toString() {
        return this.codeString;
    }

    public static HangingProtocolLevel fromString(String value) {
        for (HangingProtocolLevel hpl : HangingProtocolLevel.values()) {
            if (!hpl.codeString.equals(value)) continue;
            return hpl;
        }
        throw new IllegalArgumentException("Invalid Hanging Protocol Level: " + value);
    }
}

