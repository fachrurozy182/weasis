/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.macro;

import java.util.Collection;
import java.util.List;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.weasis.dicom.macro.Module;
import org.weasis.dicom.macro.SOPInstanceReferenceAndMAC;

public class SeriesAndInstanceReference
extends Module {
    public SeriesAndInstanceReference(Attributes dcmItems) {
        super(dcmItems);
    }

    public SeriesAndInstanceReference() {
        super(new Attributes());
    }

    public static Collection<SeriesAndInstanceReference> toSeriesAndInstanceReferenceMacros(Sequence seq) {
        if (seq == null || seq.isEmpty()) {
            return List.of();
        }
        return seq.stream().map(SeriesAndInstanceReference::new).toList();
    }

    public String getSeriesInstanceUID() {
        return this.dcmItems.getString(0x20000E);
    }

    public void setSeriesInstanceUID(String seriesUID) {
        this.dcmItems.setString(0x20000E, VR.UI, seriesUID);
    }

    public String getRetrieveAETitle() {
        return this.dcmItems.getString(524372);
    }

    public void setRetrieveAETitle(String aeTitle) {
        this.dcmItems.setString(524372, VR.AE, aeTitle);
    }

    public String getStorageMediaFileSetID() {
        return this.dcmItems.getString(8913200);
    }

    public void setStorageMediaFileSetID(String fileSetId) {
        this.dcmItems.setString(8913200, VR.SH, fileSetId);
    }

    public String getStorageMediaFileSetUID() {
        return this.dcmItems.getString(8913216);
    }

    public void setStorageMediaFileSetUID(String fileSetUID) {
        this.dcmItems.setString(8913216, VR.UI, fileSetUID);
    }

    public Collection<SOPInstanceReferenceAndMAC> getReferencedSOPInstances() {
        return SOPInstanceReferenceAndMAC.toSOPInstanceReferenceAndMacMacros(this.dcmItems.getSequence(528793));
    }

    public void setReferencedSOPInstances(Collection<SOPInstanceReferenceAndMAC> referencedInstances) {
        this.updateSequence(528793, referencedInstances);
    }
}

