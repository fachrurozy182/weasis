/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.macro;

import java.util.Collection;
import java.util.List;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.weasis.dicom.macro.DigitalSignatures;
import org.weasis.dicom.macro.MACParameters;
import org.weasis.dicom.macro.SOPInstanceReferenceAndPurpose;

public class SOPInstanceReferenceAndMAC
extends SOPInstanceReferenceAndPurpose {
    public SOPInstanceReferenceAndMAC(Attributes dcmItems) {
        super(dcmItems);
    }

    public SOPInstanceReferenceAndMAC() {
        super(new Attributes());
    }

    public static Collection<SOPInstanceReferenceAndMAC> toSOPInstanceReferenceAndMacMacros(Sequence seq) {
        if (seq == null || seq.isEmpty()) {
            return List.of();
        }
        return seq.stream().map(SOPInstanceReferenceAndMAC::new).toList();
    }

    public Collection<MACParameters> getMACParameters() {
        return MACParameters.toMACParametersMacros(this.dcmItems.getSequence(1342046209));
    }

    public void setMACParameters(Collection<MACParameters> macParameters) {
        this.updateSequence(1342046209, macParameters);
    }

    public Collection<DigitalSignatures> getDigitalSignatures() {
        return DigitalSignatures.toDigitalSignaturesMacros(this.dcmItems.getSequence(-327686));
    }

    public void setDigitalSignatures(Collection<DigitalSignatures> digitalSignatures) {
        this.updateSequence(-327686, digitalSignatures);
    }
}

