/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.xml.bind.annotation.XmlRootElement
 */
package org.weasis.core.ui.model.imp;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.ui.model.AbstractGraphicModel;
import org.weasis.core.ui.model.ReferencedImage;
import org.weasis.core.ui.model.ReferencedSeries;

@XmlRootElement(name="presentation")
public class XmlGraphicModel
extends AbstractGraphicModel {
    public XmlGraphicModel() {
    }

    public XmlGraphicModel(ImageElement img) {
        super(XmlGraphicModel.buildReferences(img));
    }

    private static List<ReferencedSeries> buildReferences(ImageElement img) {
        Object object;
        String uid;
        String seriesUUID = (String)img.getTagValue(TagW.get("SeriesInstanceUID"));
        if (seriesUUID == null) {
            seriesUUID = UUID.randomUUID().toString();
            img.setTag(TagW.get("SeriesInstanceUID"), seriesUUID);
        }
        if ((uid = (String)img.getTagValue(TagW.get("SOPInstanceUID"))) == null) {
            uid = UUID.randomUUID().toString();
            img.setTag(TagW.get("SOPInstanceUID"), uid);
        }
        ArrayList<Integer> frameList = new ArrayList<Integer>(1);
        int frames = img.getMediaReader().getMediaElementNumber();
        if (frames > 1 && (object = img.getKey()) instanceof Integer) {
            Integer intVal = (Integer)object;
            frameList.add(intVal);
        }
        return Collections.singletonList(new ReferencedSeries(seriesUUID, Collections.singletonList(new ReferencedImage(uid, frameList))));
    }
}

