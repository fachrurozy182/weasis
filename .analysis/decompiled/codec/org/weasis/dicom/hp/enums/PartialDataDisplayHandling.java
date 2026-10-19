/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.hp.enums;

public enum PartialDataDisplayHandling {
    MAINTAIN_LAYOUT("MAINTAIN_LAYOUT"),
    ADAPT_LAYOUT("ADAPT_LAYOUT");

    private final String codeString;

    private PartialDataDisplayHandling(String codeString) {
        this.codeString = codeString;
    }

    public String getCodeString() {
        return this.codeString;
    }

    public String toString() {
        return this.codeString;
    }

    public static PartialDataDisplayHandling fromString(String value) {
        for (PartialDataDisplayHandling pddh : PartialDataDisplayHandling.values()) {
            if (!pddh.codeString.equals(value)) continue;
            return pddh;
        }
        throw new IllegalArgumentException("Invalid Partial Data Display Handling: " + value);
    }
}

