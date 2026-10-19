/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.media.data.MediaSeries
 */
package org.weasis.dicom.codec;

import java.util.Map;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.dicom.codec.AbstractKOSpecialElement;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.DicomMediaIO;
import org.weasis.dicom.macro.SOPInstanceReferenceAndMAC;

public class KOSpecialElement
extends AbstractKOSpecialElement {
    public KOSpecialElement(DicomMediaIO mediaIO) {
        super(mediaIO);
    }

    public void toggleKeyObjectReference(DicomImageElement dicomImage) {
        AbstractKOSpecialElement.Reference ref = new AbstractKOSpecialElement.Reference(dicomImage);
        Map<String, SOPInstanceReferenceAndMAC> sopInstanceReferenceBySOPInstanceUID = this.getReferencedSOPInstanceUIDObject(ref.getSeriesInstanceUID());
        boolean isSelected = sopInstanceReferenceBySOPInstanceUID != null && sopInstanceReferenceBySOPInstanceUID.containsKey(ref.getSopInstanceUID());
        this.setKeyObjectReference(!isSelected, ref);
    }

    public boolean setKeyObjectReference(boolean selectedState, DicomImageElement dicomImage) {
        return this.setKeyObjectReference(selectedState, new AbstractKOSpecialElement.Reference(dicomImage));
    }

    private boolean setKeyObjectReference(boolean selectedState, AbstractKOSpecialElement.Reference ref) {
        if (selectedState) {
            return this.addKeyObject(ref);
        }
        return this.removeKeyObject(ref);
    }

    public boolean setKeyObjectReference(boolean selectedState, MediaSeries<DicomImageElement> series) {
        boolean hasDataModelChanged = false;
        for (DicomImageElement dicomImage : series.getSortedMedias(null)) {
            hasDataModelChanged |= this.setKeyObjectReference(selectedState, new AbstractKOSpecialElement.Reference(dicomImage));
        }
        return hasDataModelChanged;
    }
}

