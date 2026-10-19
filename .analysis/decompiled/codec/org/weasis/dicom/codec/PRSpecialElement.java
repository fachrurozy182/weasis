/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.util.ResourceUtil$OtherIcon
 *  org.weasis.core.api.util.ResourceUtil$ResourceIconPath
 */
package org.weasis.dicom.codec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.img.data.PrDicomObject;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.DicomMediaIO;
import org.weasis.dicom.codec.HiddenSpecialElement;
import org.weasis.dicom.codec.PresentationStateReader;

public class PRSpecialElement
extends HiddenSpecialElement {
    public PRSpecialElement(DicomMediaIO mediaIO) {
        super(mediaIO);
    }

    @Override
    protected void initLabel() {
        Attributes dicom = ((DicomMediaIO)this.mediaIO).getDicomObject();
        String clabel = dicom.getString(0x700080);
        if (clabel == null) {
            clabel = dicom.getString(7340161);
        }
        if (clabel == null) {
            super.initLabel();
        } else {
            this.label = this.getLabelPrefix() + clabel;
        }
    }

    public PrDicomObject getPrDicomObject() {
        return (PrDicomObject)this.getTagValue(TagW.PrDicomObject);
    }

    public static List<PRSpecialElement> getPRSpecialElements(Collection<PRSpecialElement> specialElements, DicomImageElement img) {
        if (specialElements == null) {
            return Collections.emptyList();
        }
        ArrayList<PRSpecialElement> prList = null;
        for (PRSpecialElement prElement : specialElements) {
            if (!PresentationStateReader.isImageApplicable(prElement, img)) continue;
            if (prList == null) {
                prList = new ArrayList<PRSpecialElement>();
            }
            prList.add(prElement);
        }
        if (prList != null) {
            prList.sort((Comparator<PRSpecialElement>)ORDER_BY_DATE);
        }
        return prList == null ? Collections.emptyList() : prList;
    }

    @Override
    public ResourceUtil.ResourceIconPath getIconPath() {
        return ResourceUtil.OtherIcon.PRESENTATION;
    }
}

