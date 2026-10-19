/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.gui.util.AppProperties
 *  org.weasis.core.api.media.MimeInspector
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.util.StreamUtil
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.codec;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.util.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.media.MimeInspector;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.util.StreamUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.DicomElement;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.DicomMediaIO;
import org.weasis.dicom.codec.DicomSpecialElement;
import org.weasis.dicom.codec.FileExtractor;
import org.weasis.dicom.codec.TagD;

public class DicomEncapDocElement
extends DicomImageElement
implements FileExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomEncapDocElement.class);
    private File document = null;

    public DicomEncapDocElement(DicomMediaIO mediaIO, Object key) {
        super(mediaIO, key);
    }

    public String getMimeType() {
        String val = TagD.getTagValue((TagReadable)this, 4325394, String.class);
        return val == null ? super.getMimeType() : val;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public File getExtractFile() {
        DicomEncapDocElement dicomEncapDocElement = this;
        synchronized (dicomEncapDocElement) {
            if (!(this.document != null && this.document.exists() || this.getMediaReader() == null)) {
                Object extension = ".tmp";
                Attributes ds = this.getMediaReader().getDicomObject();
                String mime = ds.getString(4325394);
                String ext = MimeInspector.getExtensions((String)mime);
                if (StringUtil.hasText((CharSequence)extension)) {
                    extension = "." + ext;
                }
                Object data = ds.getValue(4325393);
                this.readEncapsulatedDocument(data, (String)extension);
            }
        }
        return this.document;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void readEncapsulatedDocument(Object data, String extension) {
        block6: {
            if (data instanceof BulkData) {
                BulkData bulkData = (BulkData)data;
                BufferedInputStream in = null;
                FileOutputStream out = null;
                try {
                    File file = File.createTempFile("encap_", extension, AppProperties.FILE_CACHE_DIR.toFile());
                    in = new BufferedInputStream(bulkData.openStream());
                    out = new FileOutputStream(file);
                    StreamUtils.copy((InputStream)in, (OutputStream)out, bulkData.length());
                    this.document = file;
                    StreamUtil.safeClose((AutoCloseable)out);
                }
                catch (Exception e) {
                    LOGGER.error("Cannot extract encapsulated document", (Throwable)e);
                    break block6;
                }
                finally {
                    StreamUtil.safeClose(out);
                    StreamUtil.safeClose(in);
                }
                StreamUtil.safeClose((AutoCloseable)in);
            }
        }
    }

    @Override
    public Attributes saveToFile(File output, DicomElement.DicomExportParameters params) {
        return DicomSpecialElement.saveToFile(this, output, params.dicomEditors());
    }
}

