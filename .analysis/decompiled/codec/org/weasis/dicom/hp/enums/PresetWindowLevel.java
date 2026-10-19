/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.hp.enums;

public enum PresetWindowLevel {
    LUNG("LUNG"),
    MEDIASTINUM("MEDIASTINUM"),
    ABDO_PELVIS("ABDO_PELVIS"),
    LIVER("LIVER"),
    SOFT_TISSUE("SOFT_TISSUE"),
    BONE("BONE"),
    BRAIN("BRAIN"),
    POST_FOSSA("POST_FOSSA");

    private final String codeString;

    private PresetWindowLevel(String codeString) {
        this.codeString = codeString;
    }

    public String getCodeString() {
        return this.codeString;
    }

    public String toString() {
        return this.codeString;
    }

    public static PresetWindowLevel fromString(String value) {
        for (PresetWindowLevel pwl : PresetWindowLevel.values()) {
            if (!pwl.codeString.equals(value)) continue;
            return pwl;
        }
        throw new IllegalArgumentException("Invalid Preset Window Level: " + value);
    }
}

