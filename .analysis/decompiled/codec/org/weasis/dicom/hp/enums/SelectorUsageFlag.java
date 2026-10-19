/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.hp.enums;

public enum SelectorUsageFlag {
    MATCH("MATCH"),
    NO_MATCH("NO_MATCH");

    private final String codeString;

    private SelectorUsageFlag(String codeString) {
        this.codeString = codeString;
    }

    public String getCodeString() {
        return this.codeString;
    }

    public boolean isMatch() {
        return this == MATCH;
    }

    public String toString() {
        return this.codeString;
    }

    public static SelectorUsageFlag fromString(String value) {
        for (SelectorUsageFlag flag : SelectorUsageFlag.values()) {
            if (!flag.codeString.equals(value)) continue;
            return flag;
        }
        throw new IllegalArgumentException("Invalid (0072,0024) Image Set Selector Usage Flag: " + value);
    }
}

