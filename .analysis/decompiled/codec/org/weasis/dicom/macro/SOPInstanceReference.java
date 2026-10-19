/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.macro;

import java.util.Collection;
import java.util.List;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.dcm4che3.img.util.DicomUtils;
import org.weasis.dicom.macro.Module;

public class SOPInstanceReference
extends Module {
    public SOPInstanceReference(Attributes dcmItems) {
        super(dcmItems);
    }

    public SOPInstanceReference() {
        super(new Attributes());
    }

    public static Collection<SOPInstanceReference> toSOPInstanceReferenceMacros(Sequence seq) {
        if (seq == null || seq.isEmpty()) {
            return List.of();
        }
        return seq.stream().map(SOPInstanceReference::new).toList();
    }

    public int[] getReferencedFrameNumber() {
        return DicomUtils.getIntArrayFromDicomElement(this.dcmItems, 528736, null);
    }

    public void setReferencedFrameNumber(int ... frameNumbers) {
        this.dcmItems.setInt(528736, VR.IS, frameNumbers);
    }

    public String getReferencedSOPInstanceUID() {
        return this.dcmItems.getString(528725);
    }

    public void setReferencedSOPInstanceUID(String instanceUID) {
        this.dcmItems.setString(528725, VR.UI, instanceUID);
    }

    public String getReferencedSOPClassUID() {
        return this.dcmItems.getString(528720);
    }

    public void setReferencedSOPClassUID(String classUID) {
        this.dcmItems.setString(528720, VR.UI, classUID);
    }

    public Integer getInstanceNumber() {
        return DicomUtils.getIntegerFromDicomElement(this.dcmItems, 2097171, null);
    }

    public void setInstanceNumber(Integer instanceNumber) {
        if (instanceNumber != null) {
            this.dcmItems.setInt(2097171, VR.IS, instanceNumber);
        }
    }
}

