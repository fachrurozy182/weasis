/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.macro;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.weasis.dicom.macro.Code;
import org.weasis.dicom.macro.Module;

public class DigitalSignatures
extends Module {
    public DigitalSignatures(Attributes dcmItems) {
        super(dcmItems);
    }

    public DigitalSignatures() {
        super(new Attributes());
    }

    public static Collection<DigitalSignatures> toDigitalSignaturesMacros(Sequence seq) {
        if (seq == null || seq.isEmpty()) {
            return List.of();
        }
        return seq.stream().map(DigitalSignatures::new).toList();
    }

    public int getMACIDNumber() {
        return this.dcmItems.getInt(0x4000005, -1);
    }

    public void setMACIDNumber(int value) {
        this.dcmItems.setInt(0x4000005, VR.US, value);
    }

    public String getDigitalSignatureUID() {
        return this.dcmItems.getString(0x4000100);
    }

    public void setDigitalSignatureUID(String uid) {
        this.dcmItems.setString(0x4000100, VR.UI, uid);
    }

    public Date getDigitalSignatureDateTime() {
        return this.dcmItems.getDate(67109125);
    }

    public void setDigitalSignatureDateTime(Date dateTime) {
        this.dcmItems.setDate(67109125, VR.DT, dateTime);
    }

    public String getCertificateType() {
        return this.dcmItems.getString(0x4000110);
    }

    public void setCertificateType(String type) {
        this.dcmItems.setString(0x4000110, VR.CS, type);
    }

    public byte[] getCertificateOfSigner() throws IOException {
        return this.dcmItems.getBytes(67109141);
    }

    public void setCertificateOfSigner(byte[] certificate) {
        this.dcmItems.setBytes(67109141, VR.OB, certificate);
    }

    public byte[] getSignature() throws IOException {
        return this.dcmItems.getBytes(67109152);
    }

    public void setSignature(byte[] signature) {
        this.dcmItems.setBytes(67109152, VR.OB, signature);
    }

    public String getCertifiedTimestampType() {
        return this.dcmItems.getString(67109637);
    }

    public void setCertifiedTimestampType(String type) {
        this.dcmItems.setString(67109637, VR.CS, type);
    }

    public byte[] getCertifiedTimestamp() throws IOException {
        return this.dcmItems.getBytes(67109648);
    }

    public void setCertifiedTimestamp(byte[] timestamp) {
        this.dcmItems.setBytes(67109648, VR.OB, timestamp);
    }

    public Code getDigitalSignaturePurposeCode() {
        return Code.getNestedCode(this.dcmItems, 0x4000401);
    }

    public void setDigitalSignaturePurposeCode(Code code) {
        this.updateSequence(0x4000401, code);
    }
}

