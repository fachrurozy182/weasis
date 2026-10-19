/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.macro;

import java.util.Collection;
import java.util.List;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.weasis.dicom.macro.Code;
import org.weasis.dicom.macro.SOPInstanceReference;

public class SOPInstanceReferenceAndPurpose
extends SOPInstanceReference {
    public SOPInstanceReferenceAndPurpose(Attributes dcmItems) {
        super(dcmItems);
    }

    public SOPInstanceReferenceAndPurpose() {
        this(new Attributes());
    }

    public static Collection<SOPInstanceReferenceAndPurpose> toSOPInstanceReferenceAndPurposesMacros(Sequence seq) {
        if (seq == null || seq.isEmpty()) {
            return List.of();
        }
        return seq.stream().map(SOPInstanceReferenceAndPurpose::new).toList();
    }

    public Code getPurposeOfReferenceCode() {
        return Code.getNestedCode(this.dcmItems, 4235632);
    }

    public void setPurposeOfReferenceCode(Code purposeCode) {
        this.updateSequence(4235632, purposeCode);
    }
}

