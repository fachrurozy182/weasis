/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.macro;

import java.util.Collection;
import java.util.List;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.weasis.dicom.macro.Code;
import org.weasis.dicom.macro.Module;
import org.weasis.dicom.macro.SOPInstanceReference;

public class ReferencedRequest
extends Module {
    public ReferencedRequest(Attributes dcmItems) {
        super(dcmItems);
    }

    public ReferencedRequest() {
        super(new Attributes());
    }

    public static Collection<ReferencedRequest> toReferencedRequestMacros(Sequence seq) {
        if (seq == null || seq.isEmpty()) {
            return List.of();
        }
        return seq.stream().map(ReferencedRequest::new).toList();
    }

    public String getStudyInstanceUID() {
        return this.dcmItems.getString(0x20000D);
    }

    public void setStudyInstanceUID(String studyUID) {
        this.dcmItems.setString(0x20000D, VR.UI, studyUID);
    }

    public SOPInstanceReference getReferencedStudySOPInstance() {
        Attributes item = this.dcmItems.getNestedDataset(528656);
        return item != null ? new SOPInstanceReference(item) : null;
    }

    public void setReferencedStudySOPInstance(SOPInstanceReference referencedStudy) {
        this.updateSequence(528656, referencedStudy);
    }

    public String getAccessionNumber() {
        return this.dcmItems.getString(524368);
    }

    public void setAccessionNumber(String accessionNumber) {
        this.dcmItems.setString(524368, VR.SH, accessionNumber);
    }

    public String getPlacerOrderNumberImagingServiceRequest() {
        return this.dcmItems.getString(4202518);
    }

    public void setPlacerOrderNumberImagingServiceRequest(String placerOrderNumber) {
        this.dcmItems.setString(4202518, VR.LO, placerOrderNumber);
    }

    public String getFillerOrderNumberImagingServiceRequest() {
        return this.dcmItems.getString(4202519);
    }

    public void setFillerOrderNumberImagingServiceRequest(String fillerOrderNumber) {
        this.dcmItems.setString(4202519, VR.LO, fillerOrderNumber);
    }

    public String getRequestedProcedureID() {
        return this.dcmItems.getString(0x401001);
    }

    public void setRequestedProcedureID(String procedureId) {
        this.dcmItems.setString(0x401001, VR.SH, procedureId);
    }

    public String getRequestedProcedureDescription() {
        return this.dcmItems.getString(3280992);
    }

    public void setRequestedProcedureDescription(String description) {
        this.dcmItems.setString(3280992, VR.LO, description);
    }

    public Code getRequestedProcedureCode() {
        return Code.getNestedCode(this.dcmItems, 3280996);
    }

    public void setRequestedProcedureCode(Code procedureCode) {
        this.updateSequence(3280996, procedureCode);
    }

    public String getReasonForTheRequestedProcedure() {
        return this.dcmItems.getString(4198402);
    }

    public void setReasonForTheRequestedProcedure(String reason) {
        this.dcmItems.setString(4198402, VR.LO, reason);
    }

    public Code getReasonForRequestedProcedureCode() {
        return Code.getNestedCode(this.dcmItems, 4198410);
    }

    public void setReasonForRequestedProcedureCode(Code reasonCode) {
        this.updateSequence(4198410, reasonCode);
    }
}

