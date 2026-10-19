/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.hp.enums;

public enum YesNo {
    YES("YES"),
    NO("NO");

    private final String codeString;

    private YesNo(String codeString) {
        this.codeString = codeString;
    }

    public String getCodeString() {
        return this.codeString;
    }

    public String toString() {
        return this.codeString;
    }

    public static YesNo fromString(String value) {
        for (YesNo yn : YesNo.values()) {
            if (!yn.codeString.equals(value)) continue;
            return yn;
        }
        throw new IllegalArgumentException("Invalid YesNo value: " + value);
    }
}

