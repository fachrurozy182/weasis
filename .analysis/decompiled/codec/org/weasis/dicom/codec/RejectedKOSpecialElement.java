/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.codec;

import org.dcm4che3.data.Attributes;
import org.weasis.dicom.codec.AbstractKOSpecialElement;
import org.weasis.dicom.codec.DicomMediaIO;

public class RejectedKOSpecialElement
extends AbstractKOSpecialElement {
    public RejectedKOSpecialElement(DicomMediaIO mediaIO) {
        super(mediaIO);
    }

    public static boolean isRejectionKOS(DicomMediaIO mediaIO) {
        String cm;
        Attributes item;
        Attributes dcmItems;
        if (mediaIO != null && (dcmItems = mediaIO.getDicomObject()) != null && (item = dcmItems.getNestedDataset(4235331)) != null && (cm = item.getString(524548, null)) != null) {
            return "Rejected for Quality Reasons".equalsIgnoreCase(cm) || "Rejected for Patient Safety Reasons".equalsIgnoreCase(cm) || "Incorrect Modality Worklist Entry".equalsIgnoreCase(cm) || "Data Retention Policy Expired".equalsIgnoreCase(cm);
        }
        return false;
    }
}

