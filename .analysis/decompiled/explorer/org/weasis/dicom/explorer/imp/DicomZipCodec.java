/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.osgi.service.component.annotations.Component
 *  org.weasis.core.api.media.data.Codec
 *  org.weasis.core.api.media.data.MediaElement
 *  org.weasis.core.api.media.data.MediaReader
 */
package org.weasis.dicom.explorer.imp;

import java.net.URI;
import java.util.Hashtable;
import org.osgi.service.component.annotations.Component;
import org.weasis.core.api.media.data.Codec;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaReader;
import org.weasis.dicom.explorer.imp.DicomZipMediaIO;

@Component(service={Codec.class})
public class DicomZipCodec
implements Codec<MediaElement> {
    public static final String NAME = "DICOM-ZIP";
    public static final String MIME_TYPE = "application/x-zip";
    public static final String[] FILE_EXTENSIONS = new String[]{"zip"};

    public String getCodecName() {
        return NAME;
    }

    public String[] getReaderMIMETypes() {
        return new String[]{MIME_TYPE};
    }

    public String[] getReaderExtensions() {
        return FILE_EXTENSIONS;
    }

    public String[] getWriterMIMETypes() {
        return new String[0];
    }

    public String[] getWriterExtensions() {
        return new String[0];
    }

    public boolean isMimeTypeSupported(String mimeType) {
        return MIME_TYPE.equals(mimeType);
    }

    public MediaReader<MediaElement> getMediaIO(URI media, String mimeType, Hashtable<String, Object> properties) {
        if (this.isMimeTypeSupported(mimeType)) {
            return new DicomZipMediaIO(media, this);
        }
        return null;
    }
}

