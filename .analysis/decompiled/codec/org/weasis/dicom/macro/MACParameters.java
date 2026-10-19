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

public class MACParameters
extends Module {
    public MACParameters(Attributes dcmItems) {
        super(dcmItems);
    }

    public MACParameters() {
        super(new Attributes());
    }

    public static Collection<MACParameters> toMACParametersMacros(Sequence seq) {
        if (seq == null || seq.isEmpty()) {
            return List.of();
        }
        return seq.stream().map(MACParameters::new).toList();
    }

    public int getMACIDNumber() {
        return this.dcmItems.getInt(0x4000005, -1);
    }

    public void setMACIDNumber(int macId) {
        this.dcmItems.setInt(0x4000005, VR.US, macId);
    }

    public String getMACCalculationTransferSyntaxUID() {
        return this.dcmItems.getString(0x4000010);
    }

    public void setMACCalculationTransferSyntaxUID(String transferSyntaxUID) {
        this.dcmItems.setString(0x4000010, VR.UI, transferSyntaxUID);
    }

    public String getMACAlgorithm() {
        return this.dcmItems.getString(67108885);
    }

    public void setMACAlgorithm(String algorithm) {
        this.dcmItems.setString(67108885, VR.CS, algorithm);
    }

    public int[] getDataElementsSigned() {
        return this.dcmItems.getInts(0x4000020);
    }

    public void setDataElementsSigned(int[] signedTags) {
        this.dcmItems.setInt(0x4000020, VR.AT, signedTags);
    }
}

