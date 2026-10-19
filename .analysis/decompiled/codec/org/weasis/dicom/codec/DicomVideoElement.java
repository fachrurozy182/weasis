/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.gui.util.AppProperties
 *  org.weasis.core.util.StreamUtil
 */
package org.weasis.dicom.codec;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.util.StreamUtil;
import org.weasis.dicom.codec.DicomElement;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.DicomMediaIO;
import org.weasis.dicom.codec.DicomSpecialElement;
import org.weasis.dicom.codec.FileExtractor;

public class DicomVideoElement
extends DicomImageElement
implements FileExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomVideoElement.class);
    private File videoFile = null;

    public DicomVideoElement(DicomMediaIO mediaIO, Object key) {
        super(mediaIO, key);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public File getExtractFile() {
        DicomVideoElement dicomVideoElement = this;
        synchronized (dicomVideoElement) {
            VR.Holder holder;
            Object pixelData;
            Attributes dcm;
            if ((this.videoFile == null || !this.videoFile.exists()) && this.getMediaReader() != null && (dcm = this.getMediaReader().getDicomObject()) != null && (pixelData = dcm.getValue(2145386512, holder = new VR.Holder())) instanceof Fragments) {
                Fragments fragments = (Fragments)pixelData;
                this.readFragments(fragments);
            }
        }
        return this.videoFile;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void readFragments(Fragments fragments) {
        for (Object data : fragments) {
            if (!(data instanceof BulkData)) continue;
            BulkData bulkData = (BulkData)data;
            BufferedInputStream in = null;
            FileOutputStream out = null;
            try {
                File file = File.createTempFile("video_", ".mpg", AppProperties.FILE_CACHE_DIR.toFile());
                in = new BufferedInputStream(bulkData.openStream());
                out = new FileOutputStream(file);
                StreamUtils.copy((InputStream)in, (OutputStream)out, bulkData.length());
                this.videoFile = file;
                StreamUtil.safeClose((AutoCloseable)out);
            }
            catch (Exception e) {
                LOGGER.error("Cannot extract video stream", (Throwable)e);
                continue;
            }
            finally {
                StreamUtil.safeClose(out);
                StreamUtil.safeClose(in);
                continue;
            }
            StreamUtil.safeClose((AutoCloseable)in);
        }
    }

    @Override
    public Attributes saveToFile(File output, DicomElement.DicomExportParameters params) {
        return DicomSpecialElement.saveToFile(this, output, params.dicomEditors());
    }
}

