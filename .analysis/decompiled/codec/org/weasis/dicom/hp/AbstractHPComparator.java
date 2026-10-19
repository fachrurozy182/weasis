/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.annotations.Generated
 */
package org.weasis.dicom.hp;

import org.dcm4che3.img.util.DicomUtils;
import org.weasis.core.util.annotations.Generated;
import org.weasis.dicom.hp.HPComparator;

public abstract class AbstractHPComparator
implements HPComparator {
    @Override
    @Generated
    public String getImageSetSelectorUsageFlag() {
        return this.getAttributes().getString(7471140);
    }

    @Override
    @Generated
    public Integer getSelectorAttribute() {
        return DicomUtils.getIntegerFromDicomElement(this.getAttributes(), 7471142, null);
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
    public Integer getSelectorValueNumber() {
        return DicomUtils.getIntegerFromDicomElement(this.getAttributes(), 7471144, null);
    }

    @Override
    @Generated
    public String getSortByCategory() {
        return this.getAttributes().getString(7472642);
    }

    @Override
    @Generated
    public String getSortingDirection() {
        return this.getAttributes().getString(7472644);
    }
}

