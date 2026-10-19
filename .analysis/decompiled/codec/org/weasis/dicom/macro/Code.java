/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.macro;

import java.util.Date;
import java.util.List;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.macro.ItemCode;
import org.weasis.dicom.macro.Module;
import org.weasis.dicom.ref.CodingScheme;

public class Code
extends Module
implements ItemCode {
    public Code(Attributes dcmItems) {
        super(dcmItems);
    }

    public Code() {
        this(new Attributes());
    }

    public static List<Code> toCodeMacros(Sequence seq) {
        if (seq == null || seq.isEmpty()) {
            return List.of();
        }
        return seq.stream().map(Code::new).toList();
    }

    public static Code getNestedCode(Attributes dcmItems, int tag) {
        Attributes item = dcmItems.getNestedDataset(tag);
        return item != null ? new Code(item) : null;
    }

    public String getExistingCodeValue() {
        String val = this.getCodeValue();
        if (!StringUtil.hasText((CharSequence)val)) {
            val = this.getLongCodeValue();
        }
        if (!StringUtil.hasText((CharSequence)val)) {
            val = this.getURNCodeValue();
        }
        return val;
    }

    @Override
    public String getCodeValue() {
        return this.dcmItems.getString(524544);
    }

    public void setCodeValue(String value) {
        this.dcmItems.setString(524544, VR.SH, value);
    }

    public String getLongCodeValue() {
        return this.dcmItems.getString(524569);
    }

    public void setLongCodeValue(String value) {
        this.dcmItems.setString(524569, VR.UC, value);
    }

    public String getURNCodeValue() {
        return this.dcmItems.getString(524576);
    }

    public void setURNCodeValue(String value) {
        this.dcmItems.setString(524576, VR.UR, value);
    }

    public String getCodingSchemeDesignator() {
        return this.dcmItems.getString(524546);
    }

    public void setCodingSchemeDesignator(String value) {
        this.dcmItems.setString(524546, VR.SH, value);
    }

    public String getCodingSchemeVersion() {
        return this.dcmItems.getString(524547);
    }

    public void setCodingSchemeVersion(String value) {
        this.dcmItems.setString(524547, VR.SH, value);
    }

    @Override
    public String getCodeMeaning() {
        return this.dcmItems.getString(524548);
    }

    public void setCodeMeaning(String value) {
        this.dcmItems.setString(524548, VR.LO, value);
    }

    @Override
    public CodingScheme getCodingScheme() {
        return CodingScheme.fromDesignator(this.getCodingSchemeDesignator()).orElse(null);
    }

    public void setCodingScheme(CodingScheme codingScheme) {
        if (codingScheme != null) {
            this.setCodingSchemeDesignator(codingScheme.getDesignator());
        }
    }

    public String getContextIdentifier() {
        return this.dcmItems.getString(524559);
    }

    public void setContextIdentifier(String value) {
        this.dcmItems.setString(524559, VR.CS, value);
    }

    public String getContextUID() {
        return this.dcmItems.getString(524567);
    }

    public void setContextUID(String value) {
        this.dcmItems.setString(524567, VR.UI, value);
    }

    public String getMappingResource() {
        return this.dcmItems.getString(524549);
    }

    public void setMappingResource(String value) {
        this.dcmItems.setString(524549, VR.CS, value);
    }

    public Date getContextGroupVersion() {
        return this.dcmItems.getDate(524550);
    }

    public void setContextGroupVersion(Date date) {
        this.dcmItems.setDate(524550, VR.DT, date);
    }

    public String getContextGroupExtensionFlag() {
        return this.dcmItems.getString(524555);
    }

    public void setContextGroupExtensionFlag(String value) {
        this.dcmItems.setString(524555, VR.CS, value);
    }

    public Date getContextGroupLocalVersion() {
        return this.dcmItems.getDate(524551);
    }

    public void setContextGroupLocalVersion(Date date) {
        this.dcmItems.setDate(524551, VR.DT, date);
    }

    public String getContextGroupExtensionCreatorUID() {
        return this.dcmItems.getString(524557);
    }

    public void setContextGroupExtensionCreatorUID(String value) {
        this.dcmItems.setString(524557, VR.UI, value);
    }
}

