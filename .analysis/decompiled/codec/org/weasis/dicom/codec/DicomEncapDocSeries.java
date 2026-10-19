/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.media.data.TagView
 */
package org.weasis.dicom.codec;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.weasis.core.api.media.data.TagView;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.FileExtractor;
import org.weasis.dicom.codec.FilesExtractor;

public class DicomEncapDocSeries
extends DicomSeries
implements FilesExtractor {
    public DicomEncapDocSeries(String subseriesInstanceUID) {
        super(subseriesInstanceUID);
    }

    public DicomEncapDocSeries(String subseriesInstanceUID, List<DicomImageElement> c, TagView displayTag) {
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
        StringBuilder toolTips = DicomSeries.getToolTips(this);
        toolTips.append("</html>");
        return toolTips.toString();
    }

    @Override
    public String getMimeType() {
        return "encap/dicom";
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

