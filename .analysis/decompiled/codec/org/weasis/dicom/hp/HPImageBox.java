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
import org.weasis.dicom.hp.enums.ImageBoxLayoutType;
import org.weasis.dicom.macro.Module;

public class HPImageBox
extends Module {
    public HPImageBox() {
        super(new Attributes());
    }

    public HPImageBox(Attributes item, int tot) {
        super(item);
        if (tot > 1 && !ImageBoxLayoutType.TILED.getCodeString().equals(item.getString(7471876))) {
            throw new IllegalArgumentException(item.getString(7471876));
        }
    }

    @Generated
    public Integer getImageBoxNumber() {
        return DicomUtils.getIntegerFromDicomElement(this.dcmItems, 7471874, null);
    }

    @Generated
    public void setImageBoxNumber(int value) {
        this.dcmItems.setInt(7471874, VR.US, value);
    }

    @Generated
    public double[] getDisplayEnvironmentSpatialPosition() {
        return this.dcmItems.getDoubles(7471368);
    }

    @Generated
    public void setDisplayEnvironmentSpatialPosition(double[] values) {
        this.dcmItems.setDouble(7471368, VR.FD, values);
    }

    @Generated
    public ImageBoxLayoutType getImageBoxLayoutType() {
        return ImageBoxLayoutType.fromString(this.dcmItems.getString(7471876));
    }

    @Generated
    public void setImageBoxLayoutType(ImageBoxLayoutType type) {
        this.dcmItems.setString(7471876, VR.CS, type.getCodeString());
    }

    @Generated
    public Integer getImageBoxTileHorizontalDimension() {
        return DicomUtils.getIntegerFromDicomElement(this.dcmItems, 7471878, null);
    }

    @Generated
    public void setImageBoxTileHorizontalDimension(int value) {
        this.dcmItems.setInt(7471878, VR.US, value);
    }

    @Generated
    public Integer getImageBoxTileVerticalDimension() {
        return DicomUtils.getIntegerFromDicomElement(this.dcmItems, 7471880, null);
    }

    @Generated
    public void setImageBoxTileVerticalDimension(int value) {
        this.dcmItems.setInt(7471880, VR.US, value);
    }

    @Generated
    public String getImageBoxScrollDirection() {
        return this.dcmItems.getString(7471888);
    }

    @Generated
    public void setImageBoxScrollDirection(String value) {
        this.dcmItems.setString(7471888, VR.CS, value);
    }

    @Generated
    public String getImageBoxSmallScrollType() {
        return this.dcmItems.getString(7471890);
    }

    @Generated
    public void setImageBoxSmallScrollType(String value) {
        this.dcmItems.setString(7471890, VR.CS, value);
    }

    @Generated
    public Integer getImageBoxSmallScrollAmount() {
        return DicomUtils.getIntegerFromDicomElement(this.dcmItems, 7471892, null);
    }

    @Generated
    public void setImageBoxSmallScrollAmount(int value) {
        this.dcmItems.setInt(7471892, VR.US, value);
    }

    @Generated
    public String getImageBoxLargeScrollType() {
        return this.dcmItems.getString(7471894);
    }

    @Generated
    public void setImageBoxLargeScrollType(String value) {
        this.dcmItems.setString(7471894, VR.CS, value);
    }

    @Generated
    public Integer getImageBoxOverlapPriority() {
        return DicomUtils.getIntegerFromDicomElement(this.dcmItems, 7471904, null);
    }

    @Generated
    public void setImageBoxOverlapPriority(int value) {
        this.dcmItems.setInt(7471904, VR.US, value);
    }

    @Generated
    public Integer getPreferredPlaybackSequencing() {
        return DicomUtils.getIntegerFromDicomElement(this.dcmItems, 1577540, null);
    }

    @Generated
    public void setPreferredPlaybackSequencing(int value) {
        this.dcmItems.setInt(1577540, VR.US, value);
    }

    @Generated
    public Integer getRecommendedDisplayFrameRate() {
        return DicomUtils.getIntegerFromDicomElement(this.dcmItems, 532804, null);
    }

    @Generated
    public void setRecommendedDisplayFrameRate(int value) {
        this.dcmItems.setInt(532804, VR.IS, value);
    }

    @Generated
    public double getCineRelativeToRealTime() {
        return DicomUtils.getIntegerFromDicomElement(this.dcmItems, 7471920, null).intValue();
    }

    @Generated
    public void setCineRelativeToRealTime(double value) {
        this.dcmItems.setDouble(7471920, VR.FD, value);
    }
}

