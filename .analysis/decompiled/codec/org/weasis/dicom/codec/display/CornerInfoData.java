/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.media.data.TagView
 */
package org.weasis.dicom.codec.display;

import java.util.Arrays;
import org.weasis.core.api.media.data.TagView;
import org.weasis.dicom.codec.display.CornerDisplay;
import org.weasis.dicom.codec.display.Modality;
import org.weasis.dicom.codec.display.ModalityInfoData;
import org.weasis.dicom.codec.display.ModalityView;

public class CornerInfoData {
    public static final int ELEMENT_NUMBER = 7;
    private final CornerDisplay corner;
    private final TagView[] infos;

    public CornerInfoData(CornerDisplay corner, Modality extendModality) {
        ModalityInfoData modalityInfoData;
        this.corner = corner;
        TagView[] extInfos = null;
        if (extendModality != null && (modalityInfoData = ModalityView.MODALITY_VIEW_MAP.get((Object)extendModality)) != null) {
            extInfos = modalityInfoData.getCornerInfo(corner).getInfos();
        }
        this.infos = extInfos == null ? new TagView[7] : (TagView[])Arrays.copyOf(extInfos, extInfos.length);
    }

    public TagView[] getInfos() {
        return this.infos;
    }

    public CornerDisplay getCorner() {
        return this.corner;
    }

    public String toString() {
        return this.corner.toString();
    }
}

