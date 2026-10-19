/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.hp.enums;

public enum Presence {
    PRESENT("PRESENT"),
    NOT_PRESENT("NOT_PRESENT");

    private final String codeString;

    private Presence(String codeString) {
        this.codeString = codeString;
    }

    public String getCodeString() {
        return this.codeString;
    }

    public boolean isPresent() {
        return this == PRESENT;
    }

    public String toString() {
        return this.codeString;
    }

    public static Presence fromString(String value) {
        for (Presence p : Presence.values()) {
            if (!p.codeString.equals(value)) continue;
            return p;
        }
        throw new IllegalArgumentException("Invalid Presence value: " + value);
    }
}

