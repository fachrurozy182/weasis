/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.media.data.MediaSeries$MEDIA_POSITION
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagView
 */
package org.weasis.dicom.codec;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagView;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.FileExtractor;
import org.weasis.dicom.codec.FilesExtractor;
import org.weasis.dicom.codec.Messages;
import org.weasis.dicom.codec.TagD;

public class DicomVideoSeries
extends DicomSeries
implements FilesExtractor {
    public DicomVideoSeries(String subseriesInstanceUID) {
        super(subseriesInstanceUID);
    }

    public DicomVideoSeries(String subseriesInstanceUID, List<DicomImageElement> c, TagView displayTag) {
        super(subseriesInstanceUID, c, displayTag);
    }

    @Override
    public void addMedia(DicomImageElement media) {
        if (media instanceof FileExtractor) {
            FileExtractor fileExtractor = (FileExtractor)((Object)media);
            fileExtractor.getExtractFile();
        }
        super.addMedia(media);
    }

    @Override
    public String getToolTips() {
        Double frameTime;
        DicomImageElement video;
        StringBuilder toolTips = DicomSeries.getToolTips(this);
        toolTips.append(Messages.getString("DicomVideo.video_l"));
        toolTips.append(": ");
        Integer frames = TagD.getTagValue((TagReadable)this, 0x280008, Integer.class);
        if (frames != null && (video = (DicomImageElement)this.getMedia(MediaSeries.MEDIA_POSITION.FIRST, null, null)) != null && (frameTime = TagD.getTagValue((TagReadable)video, 1577059, Double.class)) != null) {
            toolTips.append(DicomVideoSeries.convertSecondsInTime((int)((double)frames.intValue() * frameTime / 1000.0)));
        }
        toolTips.append("<br>");
        toolTips.append("</html>");
        return toolTips.toString();
    }

    private static String convertSecondsInTime(int totalSecs) {
        int hours = totalSecs / 3600;
        int minutes = totalSecs % 3600 / 60;
        int seconds = totalSecs % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public String getMimeType() {
        return "video/dicom";
    }

    @Override
    public List<File> getExtractFiles() {
        ArrayList<File> files = new ArrayList<File>();
        this.getMedias(null, null).forEach(dcm -> {
            if (dcm instanceof FileExtractor) {
                FileExtractor fileExtractor = (FileExtractor)((Object)dcm);
                files.add(fileExtractor.getExtractFile());
            }
        });
        return files;
    }
}

