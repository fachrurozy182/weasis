/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.hp.enums;

public enum SelectorValueNumber {
    ABSTRACT_PRIOR("ABSTRACT_PRIOR"),
    RELATIVE_TIME("RELATIVE_TIME");

    private final String codeString;

    private SelectorValueNumber(String codeString) {
        this.codeString = codeString;
    }

    public String getCodeString() {
        return this.codeString;
    }

    public String toString() {
        return this.codeString;
    }

    public static SelectorValueNumber fromString(String value) {
        for (SelectorValueNumber svn : SelectorValueNumber.values()) {
            if (!svn.codeString.equals(value)) continue;
            return svn;
        }
        throw new IllegalArgumentException("Invalid Selector Value Number: " + value);
    }
}

