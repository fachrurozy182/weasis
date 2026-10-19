/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.codec.display;

import java.util.Arrays;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.Messages;

public enum Modality {
    DEFAULT(Messages.getString("Modality.default")),
    ANN("Annotation"),
    AR("Autorefraction"),
    ASMT("Content Assessment Results"),
    AU("Audio"),
    BDUS("Bone Densitometry (ultrasound)"),
    BI("Biomagnetic imaging"),
    BMD("Bone Densitometry (X-Ray)"),
    CFM("Confocal Microscopy"),
    CR("Computed Radiography"),
    CT("Computed Tomography"),
    CTPROTOCOL("CT Protocol (Performed)"),
    DMS("Dermoscopy"),
    DG("Diaphanography"),
    DOC("Document"),
    DX("Digital Radiography"),
    ECG("Electrocardiography"),
    EEG("Electroencephalography"),
    EMG("Electromyography"),
    EOG("Electrooculography"),
    EPS("Cardiac Electrophysiology"),
    ES("Endoscopy"),
    FID("Fiducials"),
    GM("General Microscopy"),
    HC("Hard Copy"),
    HD("Hemodynamic Waveform"),
    IO("Intra-Oral Radiography"),
    IOL("Intraocular Lens Data"),
    IVOCT("Intravascular Optical Coherence Tomography"),
    IVUS("Intravascular Ultrasound"),
    KER("Keratometry"),
    KO("Key Object Selection"),
    LEN("Lensometry"),
    LS("Laser surface scan"),
    MG("Mammography"),
    MR("Magnetic Resonance"),
    M3D("Model for 3D Manufacturing"),
    NM("Nuclear Medicine"),
    OAM("Ophthalmic Axial Measurements"),
    OCT("Optical Coherence Tomography (non-Ophthalmic)"),
    OP("Ophthalmic Photography"),
    OPM("Ophthalmic Mapping"),
    OPT("Ophthalmic Tomography"),
    OPTBSV("Ophthalmic Tomography B-scan Volume Analysis"),
    OPTENF("Ophthalmic Tomography En Face"),
    OPV("Ophthalmic Visual Field"),
    OSS("Optical Surface Scan"),
    OT("Other"),
    PA("Photoacoustic"),
    PLAN("Plan"),
    POS("Position Sensor"),
    PR("Presentation State"),
    PT("Positron emission tomography (PET)"),
    PX("Panoramic X-Ray"),
    REG("Registration"),
    RESP("Respiratory Waveform"),
    RF("Radio Fluoroscopy"),
    RG("Radiographic imaging (conventional film/screen)"),
    RTDOSE("Radiotherapy Dose"),
    RTIMAGE("Radiotherapy Image"),
    RTINTENT("Radiotherapy Intent"),
    RTPLAN("Radiotherapy Plan"),
    RTRAD("RT Radiation"),
    RTRECORD("RT Treatment Record"),
    RTSEGANN("Radiotherapy Segment Annotation"),
    RTSTRUCT("Radiotherapy Structure Set"),
    RWV("Real World Value Map"),
    SEG("Segmentation"),
    SM("Slide Microscopy"),
    SMR("Stereometric Relationship"),
    SR("SR Document"),
    SRF("Subjective Refraction"),
    STAIN("Automated Slide Stainer"),
    TEXTUREMAP("Texture Map"),
    TG("Thermography"),
    US("Ultrasound"),
    VA("Visual Acuity"),
    XA("X-Ray Angiography"),
    XAPROTOCOL("XA Protocol (Performed)"),
    XC("External-camera Photography");

    private final String description;

    private Modality(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public String toString() {
        return this.name() + " - " + this.description;
    }

    public static Modality getModality(String modality) {
        Modality v = DEFAULT;
        if (StringUtil.hasText((CharSequence)modality)) {
            try {
                v = Modality.valueOf(modality);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return v;
    }

    public static Modality[] getAllModalitiesExceptDefault() {
        Modality[] vals = Modality.values();
        return Arrays.copyOfRange(vals, 1, vals.length);
    }
}

