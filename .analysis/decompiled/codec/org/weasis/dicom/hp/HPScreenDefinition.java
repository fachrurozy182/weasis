/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.annotations.Generated
 */
package org.weasis.dicom.hp;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.img.util.DicomUtils;
import org.weasis.core.util.annotations.Generated;
import org.weasis.dicom.macro.Module;

public class HPScreenDefinition
extends Module {
    public HPScreenDefinition(Attributes attributes) {
        super(attributes);
    }

    public HPScreenDefinition() {
        super(new Attributes());
    }

    @Generated
    public Integer getNumberOfVerticalPixels() {
        return DicomUtils.getIntegerFromDicomElement(this.dcmItems, 7471364, null);
    }

    @Generated
    public void setNumberOfVerticalPixels(int value) {
        this.dcmItems.setInt(7471364, VR.US, value);
    }

    @Generated
    public Integer getNumberOfHorizontalPixels() {
        return DicomUtils.getIntegerFromDicomElement(this.dcmItems, 7471366, null);
    }

    @Generated
    public void setNumberOfHorizontalPixels(int value) {
        this.dcmItems.setInt(7471366, VR.US, value);
    }

    @Generated
    public double[] getDisplayEnvironmentSpatialPosition() {
        return DicomUtils.getDoubleArrayFromDicomElement(this.dcmItems, 7471368, null);
    }

    @Generated
    public void setDisplayEnvironmentSpatialPosition(double[] values) {
        this.dcmItems.setDouble(7471368, VR.FD, values);
    }

    @Generated
    public Integer getScreenMinimumColorBitDepth() {
        return DicomUtils.getIntegerFromDicomElement(this.dcmItems, 7471372, null);
    }

    @Generated
    public void setScreenMinimumColorBitDepth(int value) {
        this.dcmItems.setInt(7471372, VR.US, value);
    }

    @Generated
    public Integer getScreenMinimumGrayscaleBitDepth() {
        return DicomUtils.getIntegerFromDicomElement(this.dcmItems, 7471370, null);
    }

    @Generated
    public void setScreenMinimumGrayscaleBitDepth(int value) {
        this.dcmItems.setInt(7471370, VR.US, value);
    }

    @Generated
    public Integer getApplicationMaximumRepaintTime() {
        return DicomUtils.getIntegerFromDicomElement(this.dcmItems, 7471374, null);
    }

    @Generated
    public void setApplicationMaximumRepaintTime(int value) {
        this.dcmItems.setInt(7471374, VR.US, value);
    }
}

