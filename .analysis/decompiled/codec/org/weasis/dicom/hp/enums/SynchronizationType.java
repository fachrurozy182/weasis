/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.hp.enums;

public enum SynchronizationType {
    PAGE("PAGE"),
    ROW_COLUMN("ROW_COLUMN"),
    IMAGE("IMAGE");

    private final String codeString;

    private SynchronizationType(String codeString) {
        this.codeString = codeString;
    }

    public String getCodeString() {
        return this.codeString;
    }

    public String toString() {
        return this.codeString;
    }

    public static SynchronizationType fromString(String value) {
        for (SynchronizationType st : SynchronizationType.values()) {
            if (!st.codeString.equals(value)) continue;
            return st;
        }
        throw new IllegalArgumentException("Invalid Synchronization Type: " + value);
    }
}

