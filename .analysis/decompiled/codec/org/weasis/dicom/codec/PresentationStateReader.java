/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.media.data.Taggable
 */
package org.weasis.dicom.codec;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.img.data.PrDicomObject;
import org.dcm4che3.img.util.DicomObjectUtil;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.media.data.Taggable;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.PRSpecialElement;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.TagSeq;
import org.weasis.dicom.codec.utils.DicomMediaUtils;

public class PresentationStateReader
implements Taggable {
    public static final int PRIVATE_CREATOR_TAG = 0x71070070;
    public static final int PR_MODEL_PRIVATE_TAG = 0x71077001;
    public static final String PR_MODEL_ID = "weasis/model/xml/2.5";
    public static final String TAG_PR_ROTATION = "pr.rotation";
    public static final String TAG_PR_FLIP = "pr.flip";
    private final PRSpecialElement prSpecialElement;
    private final Attributes dicomObject;
    private final HashMap<TagW, Object> tags = new HashMap();
    private final PrDicomObject prDicomObject;

    public PresentationStateReader(PRSpecialElement dicom) {
        Objects.requireNonNull(dicom, "Dicom parameter cannot be null");
        this.prSpecialElement = dicom;
        this.prDicomObject = Objects.requireNonNull(dicom.getPrDicomObject(), "PR DICOM object cannot be null");
        this.dicomObject = this.prDicomObject.getDicomObject();
    }

    public PRSpecialElement getPrSpecialElement() {
        return this.prSpecialElement;
    }

    public String toString() {
        return this.prSpecialElement.toString();
    }

    public Attributes getDicomObject() {
        return this.dicomObject;
    }

    public PrDicomObject getPrDicomObject() {
        return this.prDicomObject;
    }

    public Object getTagValue(TagW tag) {
        return tag == null ? null : this.tags.get(tag);
    }

    public void setTag(TagW tag, Object value) {
        DicomMediaUtils.setTag(this.tags, tag, value);
    }

    public void setTagNoNull(TagW tag, Object value) {
        if (value != null) {
            this.setTag(tag, value);
        }
    }

    public boolean containTagKey(TagW tag) {
        return this.tags.containsKey(tag);
    }

    public Iterator<Map.Entry<TagW, Object>> getTagEntrySetIterator() {
        return this.tags.entrySet().iterator();
    }

    private static Predicate<Attributes> isSequenceApplicable(DicomImageElement img) {
        return attributes -> PresentationStateReader.isModuleApplicable(attributes, img, false);
    }

    public static boolean isImageApplicable(PRSpecialElement prSpecialElement, DicomImageElement img) {
        PrDicomObject prDcm;
        if (prSpecialElement != null && img != null && (prDcm = prSpecialElement.getPrDicomObject()) != null) {
            String seriesUID = TagD.getTagValue((TagReadable)img, 0x20000E, String.class);
            String imgSop = TagD.getTagValue((TagReadable)img, 524312, String.class);
            int dicomFrame = 1;
            Object object = img.getKey();
            if (object instanceof Integer) {
                Integer intVal = (Integer)object;
                dicomFrame = intVal + 1;
            }
            return prDcm.isImageFrameApplicable(seriesUID, imgSop, dicomFrame);
        }
        return false;
    }

    public static boolean isModuleApplicable(Attributes refImgSeqParent, DicomImageElement img, boolean sequenceRequired) {
        Objects.requireNonNull(refImgSeqParent);
        Objects.requireNonNull(img);
        String imgSop = TagD.getTagValue((TagReadable)img, 524312, String.class);
        int dicomFrame = 1;
        Object object = img.getKey();
        if (object instanceof Integer) {
            Integer intVal = (Integer)object;
            dicomFrame = intVal + 1;
        }
        return DicomObjectUtil.isImageFrameApplicableToReferencedImageSequence(DicomObjectUtil.getSequence(refImgSeqParent, 528704), 528736, imgSop, dicomFrame, sequenceRequired);
    }

    public static boolean getBooleanValue(Attributes attributes, int tag) {
        return "Y".equalsIgnoreCase(attributes.getString(tag));
    }

    public void applySpatialTransformationModule(Map<String, Object> actionsInView) {
        if (this.dicomObject != null) {
            actionsInView.put(TAG_PR_ROTATION, this.dicomObject.getInt(7340098, 0));
            actionsInView.put(TAG_PR_FLIP, PresentationStateReader.getBooleanValue(this.dicomObject, 7340097));
        }
    }

    public void readDisplayArea(DicomImageElement img) {
        if (this.dicomObject != null) {
            TagW[] tagList = TagD.getTagFromIDs(0x700101, 7340290, 4719361, 0x700100, 7340114, 7340115, 7340291);
            TagSeq.MacroSeqData data = new TagSeq.MacroSeqData(this.dicomObject, tagList, PresentationStateReader.isSequenceApplicable(img));
            TagD.get(7340122).readValue((Object)data, (Taggable)this);
        }
    }
}

