/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.hp.enums;

public enum ReconstructionType {
    COLOR("COLOR"),
    MPR("MPR"),
    RENDERING_3D("3D_RENDERING"),
    SLAB("SLAB");

    private final String codeString;

    private ReconstructionType(String codeString) {
        this.codeString = codeString;
    }

    public String getCodeString() {
        return this.codeString;
    }

    public String toString() {
        return this.codeString;
    }

    public static ReconstructionType fromString(String value) {
        for (ReconstructionType rt : ReconstructionType.values()) {
            if (!rt.codeString.equals(value)) continue;
            return rt;
        }
        throw new IllegalArgumentException("Invalid Reconstruction Type: " + value);
    }
}

