/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.hp.enums;

public enum SortingOperation {
    ALONG_AXIS("ALONG_AXIS"),
    BY_ACQ_TIME("BY_ACQ_TIME");

    private final String codeString;

    private SortingOperation(String codeString) {
        this.codeString = codeString;
    }

    public String getCodeString() {
        return this.codeString;
    }

    public String toString() {
        return this.codeString;
    }

    public static SortingOperation fromString(String value) {
        for (SortingOperation so : SortingOperation.values()) {
            if (!so.codeString.equals(value)) continue;
            return so;
        }
        throw new IllegalArgumentException("Invalid Sorting Operation: " + value);
    }
}

