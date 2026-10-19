/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.annotations.Generated
 */
package org.weasis.dicom.hp;

import java.util.List;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.img.util.DicomUtils;
import org.weasis.core.util.annotations.Generated;
import org.weasis.dicom.hp.AbstractPriorValue;
import org.weasis.dicom.hp.RelativeTime;
import org.weasis.dicom.hp.enums.RelativeTimeUnits;
import org.weasis.dicom.hp.enums.SelectorValueNumber;
import org.weasis.dicom.macro.Code;
import org.weasis.dicom.macro.Module;

public class HPTimeBasedImageSet
extends Module {
    public HPTimeBasedImageSet() {
        super(new Attributes());
    }

    public HPTimeBasedImageSet(Attributes attributes) {
        super(attributes);
    }

    @Generated
    public Integer getImageSetNumber() {
        return DicomUtils.getIntegerFromDicomElement(this.dcmItems, 7471154, null);
    }

    @Generated
    public void setImageSetNumber(int imageSetNumber) {
        this.dcmItems.setInt(7471154, VR.US, imageSetNumber);
    }

    @Generated
    public String getImageSetLabel() {
        return this.dcmItems.getString(7471168);
    }

    @Generated
    public void setImageSetLabel(String imageSetLabel) {
        this.dcmItems.setString(7471168, VR.LO, imageSetLabel);
    }

    @Generated
    public String getImageSetSelectorCategory() {
        return this.dcmItems.getString(7471156);
    }

    @Generated
    public boolean hasRelativeTime() {
        return this.dcmItems.containsValue(7471160);
    }

    public RelativeTime getRelativeTime() {
        RelativeTimeUnits units = RelativeTimeUnits.valueOf(this.dcmItems.getString(7471162));
        return new RelativeTime(this.dcmItems.getInts(7471160), units);
    }

    public void setRelativeTime(RelativeTime relativeTime) {
        this.dcmItems.setString(7471156, VR.CS, SelectorValueNumber.RELATIVE_TIME.getCodeString());
        this.dcmItems.setInt(7471160, VR.US, relativeTime.getValues());
        this.dcmItems.setString(7471162, VR.CS, relativeTime.getUnits().getCodeString());
    }

    public boolean hasAbstractPriorValue() {
        return this.dcmItems.containsValue(7471164);
    }

    public AbstractPriorValue getAbstractPriorValue() {
        return new AbstractPriorValue(this.dcmItems.getInts(7471164));
    }

    public void setAbstractPriorValue(AbstractPriorValue abstractPriorValue) {
        this.dcmItems.setString(7471156, VR.CS, SelectorValueNumber.ABSTRACT_PRIOR.getCodeString());
        this.dcmItems.setInt(7471164, VR.SS, abstractPriorValue.getValues());
    }

    public boolean hasAbstractPriorCode() {
        return this.dcmItems.containsValue(7471166);
    }

    public Code getAbstractPriorCode() {
        List<Code> items = Code.toCodeMacros(this.dcmItems.getSequence(7471166));
        return items.isEmpty() ? null : (Code)items.iterator().next();
    }

    public void setAbstractPriorCode(Code code) {
        this.dcmItems.setString(7471156, VR.CS, SelectorValueNumber.ABSTRACT_PRIOR.getCodeString());
        this.dcmItems.ensureSequence(7471166, 1).add(code.getAttributes());
    }
}

