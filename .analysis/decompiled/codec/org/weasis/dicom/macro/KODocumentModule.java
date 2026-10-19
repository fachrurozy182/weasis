/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.macro;

import java.util.Collection;
import java.util.Date;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.weasis.dicom.macro.HierarchicalSOPInstanceReference;
import org.weasis.dicom.macro.Module;
import org.weasis.dicom.macro.ReferencedRequest;

public class KODocumentModule
extends Module {
    public KODocumentModule() {
        super(new Attributes());
    }

    public KODocumentModule(Attributes dcmItems) {
        super(dcmItems);
    }

    public String getInstanceNumber() {
        return this.dcmItems.getString(2097171);
    }

    public void setInstanceNumber(String instanceNumber) {
        this.dcmItems.setString(2097171, VR.IS, instanceNumber);
    }

    public Date getContentDateTime() {
        return this.dcmItems.getDate(2251950138064947L);
    }

    public void setContentDateTime(Date dateTime) {
        this.dcmItems.setDate(2251950138064947L, dateTime);
    }

    public Collection<ReferencedRequest> getReferencedRequests() {
        return ReferencedRequest.toReferencedRequestMacros(this.dcmItems.getSequence(4236144));
    }

    public void setReferencedRequest(Collection<ReferencedRequest> referencedRequests) {
        this.updateSequence(4236144, referencedRequests);
    }

    public Collection<HierarchicalSOPInstanceReference> getCurrentRequestedProcedureEvidences() {
        return HierarchicalSOPInstanceReference.toHierarchicalSOPInstanceReferenceMacros(this.dcmItems.getSequence(4236149));
    }

    public void setCurrentRequestedProcedureEvidences(Collection<HierarchicalSOPInstanceReference> evidenceReferences) {
        this.updateSequence(4236149, evidenceReferences);
    }

    public Collection<HierarchicalSOPInstanceReference> getIdenticalDocuments() {
        return HierarchicalSOPInstanceReference.toHierarchicalSOPInstanceReferenceMacros(this.dcmItems.getSequence(4236581));
    }

    public void setIdenticalDocuments(Collection<HierarchicalSOPInstanceReference> identicalRefs) {
        this.updateSequence(4236581, identicalRefs);
    }
}

