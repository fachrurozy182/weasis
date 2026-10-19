/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.annotations.Generated
 */
package org.weasis.dicom.hp;

import java.util.Collection;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.weasis.core.util.annotations.Generated;
import org.weasis.dicom.macro.Code;
import org.weasis.dicom.macro.Module;

public class HPDefinition
extends Module {
    public HPDefinition() {
        this(new Attributes());
    }

    public HPDefinition(Attributes item) {
        super(item);
    }

    @Generated
    public String getModality() {
        return this.dcmItems.getString(524384);
    }

    @Generated
    public void setModality(String modality) {
        this.dcmItems.setString(524384, VR.CS, modality);
    }

    @Generated
    public String getLaterality() {
        return this.dcmItems.getString(0x200060);
    }

    @Generated
    public void setLaterality(String laterality) {
        this.dcmItems.setString(0x200060, VR.CS, laterality);
    }

    @Generated
    public Collection<Code> getAnatomicRegionCode() {
        return Code.toCodeMacros(this.dcmItems.getSequence(533016));
    }

    @Generated
    public void addAnatomicRegionCodes(Code code) {
        this.addCode(533016, code);
    }

    @Generated
    public Collection<Code> getProcedureCodes() {
        return Code.toCodeMacros(this.dcmItems.getSequence(528434));
    }

    @Generated
    public void addProcedureCode(Code code) {
        this.addCode(528434, code);
    }

    @Generated
    public Collection<Code> getReasonForRequestedProcedureCodes() {
        return Code.toCodeMacros(this.dcmItems.getSequence(4198410));
    }

    @Generated
    public void addReasonForRequestedProcedureCode(Code code) {
        this.addCode(4198410, code);
    }

    @Generated
    protected void addCode(int tag, Code code) {
        Sequence seq = this.dcmItems.ensureSequence(tag, 1);
        seq.add(code.getAttributes());
    }
}

