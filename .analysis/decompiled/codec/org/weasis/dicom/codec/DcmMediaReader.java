/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.media.data.MediaReader
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagUtil
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.media.data.Taggable
 */
package org.weasis.dicom.codec;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.TimeZone;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.img.DicomMetaData;
import org.dcm4che3.img.util.DateTimeUtils;
import org.weasis.core.api.media.data.MediaReader;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagUtil;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.media.data.Taggable;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.utils.DicomMediaUtils;

public interface DcmMediaReader
extends MediaReader<DicomImageElement> {
    public DicomSeries getMediaSeries();

    public Attributes getDicomObject();

    default public void writeMetaData(MediaSeriesGroup group) {
        if (group == null) {
            return;
        }
        Attributes header = this.getDicomObject();
        DicomMediaUtils.writeMetaData(group, header);
        if (TagD.get(0x20000D).equals((Object)group.getTagID())) {
            DcmMediaReader.addTimeZone((Taggable)group, header);
        }
        if (TagW.SubseriesInstanceUID.equals((Object)group.getTagID())) {
            group.setTagNoNull(TagD.get(0x100010), this.getTagValue(TagD.get(0x100010)));
            group.setTagNoNull(TagD.get(528432), (Object)header.getString(528432));
            DcmMediaReader.addTimeZone((Taggable)group, header);
        }
    }

    public static void addTimeZone(Taggable taggable, Attributes header) {
        if (taggable == null || header == null) {
            return;
        }
        TimeZone timeZone = header.getTimeZone();
        if (timeZone != null && !timeZone.equals(header.getDefaultTimeZone())) {
            taggable.setTag(TagW.Timezone, (Object)timeZone);
        }
    }

    public static String buildDateTimeWithTimeZone(TagReadable readable, int dateTag, int timeTag) {
        LocalTime time;
        LocalDate date = TagD.getTagValue(readable, dateTag, LocalDate.class);
        LocalDateTime dateTime = DateTimeUtils.dateTime(date, time = TagD.getTagValue(readable, timeTag, LocalTime.class));
        if (dateTime != null) {
            TimeZone timeZone = (TimeZone)TagW.getTagValue((TagReadable)readable, (TagW)TagW.Timezone, TimeZone.class);
            ZonedDateTime zonedDateTime = TagUtil.getZonedDateTime((LocalDateTime)dateTime, (TimeZone)timeZone);
            return TagUtil.formatDateTime((TemporalAccessor)zonedDateTime);
        }
        return "";
    }

    public DicomMetaData getDicomMetaData();

    public boolean isEditableDicom();
}

