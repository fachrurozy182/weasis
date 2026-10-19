/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.img;

import java.io.IOException;
import java.util.Locale;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import org.dcm4che3.data.Implementation;
import org.dcm4che3.img.DicomImageReader;
import org.dcm4che3.img.stream.BytesWithImageDescriptor;
import org.dcm4che3.img.stream.DicomFileInputStream;

public final class DicomImageReaderSpi
extends ImageReaderSpi {
    private static final String[] DICOM_FORMAT_NAMES = new String[]{"dicom", "DICOM"};
    private static final String[] DICOM_EXTENSIONS = new String[]{"dcm", "dic", "dicm", "dicom"};
    private static final String[] DICOM_MIME_TYPES = new String[]{"application/dicom"};
    private static final Class<?>[] DICOM_INPUT_TYPES = new Class[]{DicomFileInputStream.class, BytesWithImageDescriptor.class};
    private static final int DICOM_TAG_MIN = 524288;
    private static final int DICOM_TAG_MAX = 524310;
    private static final int DICOM_PREFIX_OFFSET = 124;
    private static final String DICOM_PREFIX = "DICM";

    public DicomImageReaderSpi() {
        super("dcm4che", Implementation.getVersionName(), DICOM_FORMAT_NAMES, DICOM_EXTENSIONS, DICOM_MIME_TYPES, DicomImageReader.class.getName(), DICOM_INPUT_TYPES, null, false, null, null, null, null, false, null, null, null, null);
    }

    @Override
    public String getDescription(Locale locale) {
        return "DICOM Image Reader (dcm4che)";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean canDecodeInput(Object source) throws IOException {
        if (!(source instanceof ImageInputStream)) {
            return false;
        }
        ImageInputStream iis = (ImageInputStream)source;
        iis.mark();
        try {
            boolean bl = this.isDicomByTag(iis) || this.isDicomByPreamble(iis);
            return bl;
        }
        finally {
            iis.reset();
        }
    }

    @Override
    public ImageReader createReaderInstance(Object extension) {
        return new DicomImageReader(this);
    }

    private boolean isDicomByTag(ImageInputStream iis) throws IOException {
        int tag = this.readLittleEndianInt(iis);
        return tag >= 524288 && tag <= 524310;
    }

    private boolean isDicomByPreamble(ImageInputStream iis) throws IOException {
        return iis.skipBytes(124) == 124 && this.hasValidDicomPrefix(iis);
    }

    private int readLittleEndianInt(ImageInputStream iis) throws IOException {
        return iis.read() | iis.read() << 8 | iis.read() << 16 | iis.read() << 24;
    }

    private boolean hasValidDicomPrefix(ImageInputStream iis) throws IOException {
        byte[] bytes = new byte[DICOM_PREFIX.length()];
        iis.readFully(bytes, 0, bytes.length);
        return DICOM_PREFIX.equals(new String(bytes));
    }
}

