/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.media.data.SeriesComparator
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagW
 */
package org.weasis.dicom.codec;

import java.time.LocalTime;
import org.weasis.core.api.media.data.SeriesComparator;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagW;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.Messages;
import org.weasis.dicom.codec.TagD;

public final class SortSeriesStack {
    public static final SeriesComparator<DicomImageElement> instanceNumber = new SeriesComparator<DicomImageElement>(){

        public int compare(DicomImageElement m1, DicomImageElement m2) {
            Integer val1 = TagD.getTagValue((TagReadable)m1, 2097171, Integer.class);
            Integer val2 = TagD.getTagValue((TagReadable)m2, 2097171, Integer.class);
            if (val1 == null || val2 == null) {
                return 0;
            }
            return val1.compareTo(val2);
        }

        public String toString() {
            return Messages.getString("SortSeriesStack.inst");
        }
    };
    public static final SeriesComparator<DicomImageElement> slicePosition = new SeriesComparator<DicomImageElement>(){

        public int compare(DicomImageElement m1, DicomImageElement m2) {
            Double val1 = (Double)m1.getTagValue(TagW.SlicePosition);
            Double val2 = (Double)m2.getTagValue(TagW.SlicePosition);
            if (val1 == null || val2 == null) {
                return 0;
            }
            return Double.compare(val1, val2);
        }

        public String toString() {
            return Messages.getString("SortSeriesStack.pos_orient");
        }
    };
    public static final SeriesComparator<DicomImageElement> sliceLocation = new SeriesComparator<DicomImageElement>(){

        public int compare(DicomImageElement m1, DicomImageElement m2) {
            Double val1 = TagD.getTagValue((TagReadable)m1, 2101313, Double.class);
            Double val2 = TagD.getTagValue((TagReadable)m2, 2101313, Double.class);
            if (val1 == null || val2 == null) {
                return 0;
            }
            return val1.compareTo(val2);
        }

        public String toString() {
            return Messages.getString("SortSeriesStack.location");
        }
    };
    public static final SeriesComparator<DicomImageElement> acquisitionTime = new SeriesComparator<DicomImageElement>(){

        public int compare(DicomImageElement m1, DicomImageElement m2) {
            LocalTime val1 = TagD.getTagValue((TagReadable)m1, 524338, LocalTime.class);
            LocalTime val2 = TagD.getTagValue((TagReadable)m2, 524338, LocalTime.class);
            if (val1 == null || val2 == null) {
                return 0;
            }
            return val1.compareTo(val2);
        }

        public String toString() {
            return Messages.getString("SortSeriesStack.time");
        }
    };
    public static final SeriesComparator<DicomImageElement> contentTime = new SeriesComparator<DicomImageElement>(){

        public int compare(DicomImageElement m1, DicomImageElement m2) {
            LocalTime val1 = TagD.getTagValue((TagReadable)m1, 524339, LocalTime.class);
            LocalTime val2 = TagD.getTagValue((TagReadable)m2, 524339, LocalTime.class);
            if (val1 == null || val2 == null) {
                return 0;
            }
            return val1.compareTo(val2);
        }

        public String toString() {
            return Messages.getString("SortSeriesStack.content_time");
        }
    };
    public static final SeriesComparator<DicomImageElement> diffusionBValue = new SeriesComparator<DicomImageElement>(){

        public int compare(DicomImageElement m1, DicomImageElement m2) {
            Double val1 = TagD.getTagValue((TagReadable)m1, 1609863, Double.class);
            Double val2 = TagD.getTagValue((TagReadable)m2, 1609863, Double.class);
            if (val1 == null || val2 == null) {
                return 0;
            }
            return val1.compareTo(val2);
        }

        public String toString() {
            return Messages.getString("SortSeriesStack.dvalue");
        }
    };

    private SortSeriesStack() {
    }

    public static SeriesComparator<DicomImageElement>[] getValues() {
        return new SeriesComparator[]{instanceNumber, slicePosition, sliceLocation, contentTime, acquisitionTime, diffusionBValue};
    }
}

