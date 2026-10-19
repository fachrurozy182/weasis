/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.annotations.Generated
 */
package org.weasis.dicom.hp;

import org.dcm4che3.data.VR;
import org.dcm4che3.img.util.DicomUtils;
import org.weasis.core.util.annotations.Generated;
import org.weasis.dicom.hp.HPSelector;
import org.weasis.dicom.hp.HPSelectorFactory;
import org.weasis.dicom.macro.Code;

public abstract class AbstractHPSelector
implements HPSelector {
    @Override
    @Generated
    public String getImageSetSelectorUsageFlag() {
        return this.getAttributes().getString(7471140);
    }

    @Override
    @Generated
    public String getFilterByCategory() {
        return this.getAttributes().getString(7472130);
    }

    @Override
    @Generated
    public String getFilterByAttributePresence() {
        return this.getAttributes().getString(7472132);
    }

    @Override
    @Generated
    public Integer getSelectorAttribute() {
        return DicomUtils.getIntegerFromDicomElement(this.getAttributes(), 7471142, null);
    }

    @Override
    @Generated
    public String getSelectorAttributeVR() {
        return this.getAttributes().getString(7471184);
    }

    @Override
    @Generated
    public Integer getSelectorSequencePointer() {
        return DicomUtils.getIntegerFromDicomElement(this.getAttributes(), 7471186, null);
    }

    @Override
    @Generated
    public Integer getFunctionalGroupPointer() {
        return DicomUtils.getIntegerFromDicomElement(this.getAttributes(), 2134375, null);
    }

    @Override
    @Generated
    public String getSelectorSequencePointerPrivateCreator() {
        return this.getAttributes().getString(7471188);
    }

    @Override
    @Generated
    public String getFunctionalGroupPrivateCreator() {
        return this.getAttributes().getString(2134584);
    }

    @Override
    @Generated
    public String getSelectorAttributePrivateCreator() {
        return this.getAttributes().getString(7471190);
    }

    @Override
    @Generated
    public Object getSelectorValue() {
        String vrStr = this.getSelectorAttributeVR();
        return switch (HPSelectorFactory.getVR(vrStr)) {
            case VR.AT -> this.getAttributes().getInts(7471200);
            case VR.CS -> (Object)this.getAttributes().getStrings(7471202);
            case VR.DS -> (Object)this.getAttributes().getFloats(0x720072);
            case VR.FD -> (Object)this.getAttributes().getDoubles(7471220);
            case VR.FL -> (Object)this.getAttributes().getFloats(7471222);
            case VR.IS -> this.getAttributes().getInts(7471204);
            case VR.LO -> (Object)this.getAttributes().getStrings(7471206);
            case VR.LT -> (Object)this.getAttributes().getStrings(7471208);
            case VR.PN -> (Object)this.getAttributes().getStrings(7471210);
            case VR.SH -> (Object)this.getAttributes().getStrings(7471212);
            case VR.SL -> this.getAttributes().getInts(7471228);
            case VR.SQ -> (Object)Code.toCodeMacros(this.getAttributes().getSequence(7471232));
            case VR.SS -> this.getAttributes().getInts(7471230);
            case VR.ST -> (Object)this.getAttributes().getStrings(7471214);
            case VR.UL -> this.getAttributes().getInts(7471224);
            case VR.US -> this.getAttributes().getInts(7471226);
            case VR.UT -> (Object)this.getAttributes().getStrings(0x720070);
            default -> null;
        };
    }

    @Override
    @Generated
    public Integer getSelectorValueNumber() {
        return DicomUtils.getIntegerFromDicomElement(this.getAttributes(), 7471144, null);
    }

    @Override
    @Generated
    public String getFilterByOperator() {
        return this.getAttributes().getString(7472134);
    }
}

