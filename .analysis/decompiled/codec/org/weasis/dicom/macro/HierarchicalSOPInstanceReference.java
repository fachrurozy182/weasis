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
import org.weasis.dicom.macro.SeriesAndInstanceReference;

public class HierarchicalSOPInstanceReference
extends Module {
    public HierarchicalSOPInstanceReference(Attributes dcmItems) {
        super(dcmItems);
    }

    public HierarchicalSOPInstanceReference() {
        super(new Attributes());
    }

    public static Collection<HierarchicalSOPInstanceReference> toHierarchicalSOPInstanceReferenceMacros(Sequence seq) {
        if (seq == null || seq.isEmpty()) {
            return List.of();
        }
        return seq.stream().map(HierarchicalSOPInstanceReference::new).toList();
    }

    public String getStudyInstanceUID() {
        return this.dcmItems.getString(0x20000D);
    }

    public void setStudyInstanceUID(String uid) {
        this.dcmItems.setString(0x20000D, VR.UI, uid);
    }

    public Collection<SeriesAndInstanceReference> getReferencedSeries() {
        return SeriesAndInstanceReference.toSeriesAndInstanceReferenceMacros(this.dcmItems.getSequence(528661));
    }

    public void setReferencedSeries(Collection<SeriesAndInstanceReference> referencedSeries) {
        this.updateSequence(528661, referencedSeries);
    }
}

