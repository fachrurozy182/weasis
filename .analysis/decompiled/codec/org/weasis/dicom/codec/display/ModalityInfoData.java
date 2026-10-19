/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.codec.display;

import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.display.CornerDisplay;
import org.weasis.dicom.codec.display.CornerInfoData;
import org.weasis.dicom.codec.display.Modality;

public class ModalityInfoData {
    private final Modality modality;
    private final Modality extendModality;
    private final CornerInfoData[] cornerInfo;

    public ModalityInfoData(Modality modality, Modality extendModality) {
        this.modality = modality;
        this.extendModality = extendModality;
        CornerDisplay[] corners = CornerDisplay.values();
        this.cornerInfo = new CornerInfoData[corners.length];
        for (int i = 0; i < corners.length; ++i) {
            this.cornerInfo[i] = new CornerInfoData(corners[i], extendModality);
        }
    }

    public Modality getModality() {
        return this.modality;
    }

    public Modality getExtendModality() {
        return this.extendModality;
    }

    public CornerInfoData[] getCornerInfo() {
        return this.cornerInfo;
    }

    public CornerInfoData getCornerInfo(CornerDisplay corner) {
        for (CornerInfoData cornerInfoData : this.cornerInfo) {
            if (!cornerInfoData.getCorner().equals((Object)corner)) continue;
            return cornerInfoData;
        }
        return null;
    }

    public String toString() {
        String desc = StringUtil.hasText((CharSequence)this.modality.getDescription()) ? " (" + this.modality.getDescription() + ")" : "";
        return String.valueOf((Object)this.modality) + desc;
    }
}

