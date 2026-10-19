/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.media;

public enum RecordType {
    PATIENT,
    STUDY,
    SERIES,
    IMAGE,
    OVERLAY,
    VOI_LUT,
    CURVE,
    STORED_PRINT,
    RT_DOSE,
    RT_STRUCTURE_SET,
    RT_PLAN,
    RT_TREAT_RECORD,
    PRESENTATION,
    WAVEFORM,
    SR_DOCUMENT,
    KEY_OBJECT_DOC,
    SPECTROSCOPY,
    RAW_DATA,
    REGISTRATION,
    FIDUCIAL,
    HANGING_PROTOCOL,
    ENCAP_DOC,
    HL7_STRUC_DOC,
    VALUE_MAP,
    STEREOMETRIC,
    PALETTE,
    IMPLANT,
    IMPLANT_ASSY,
    IMPLANT_GROUP,
    PLAN,
    MEASUREMENT,
    SURFACE,
    SURFACE_SCAN,
    TRACT,
    ASSESSMENT,
    PRIVATE;


    public String code() {
        return this.name().replace('_', ' ');
    }

    public static RecordType forCode(String code) {
        try {
            return RecordType.valueOf(code.replace(' ', '_'));
        }
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(code);
        }
    }
}

