/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.osgi.OpenCVNativeLoader
 *  org.osgi.service.component.ComponentContext
 *  org.osgi.service.component.annotations.Activate
 *  org.osgi.service.component.annotations.Component
 *  org.osgi.service.component.annotations.Deactivate
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.core.internal.cv;

import java.net.URI;
import java.util.Hashtable;
import org.opencv.osgi.OpenCVNativeLoader;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.image.cv.ImageCVIO;
import org.weasis.core.api.media.data.Codec;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaReader;

@Component(service={Codec.class})
public class NativeOpenCVCodec
implements Codec<ImageElement> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NativeOpenCVCodec.class);
    private static final String[] readerMIMETypes = new String[]{"image/bmp", "image/x-bmp", "image/x-windows-bmp", "image/jpeg", "image/pjpeg", "image/png", "image/x-portable-bitmap", "image/x-portable-graymap", "image/x-portable-greymap", "image/x-portable-pixmap", "image/x-portable-anymap", "application/x-portable-anymap", "image/cmu-raster", "application/x-cmu-raster", "image/x-cmu-raster", "image/tiff", "image/x-tiff", "image/hdr", "image/jp2", "image/jp2k", "image/j2k", "image/j2c", "image/jxl"};
    private static final String[] readerFileSuffixes = new String[]{"bm", "bmp", "dib", "jpeg", "jpg", "jpe", "png", "x-png", "pbm", "pgm", "ppm", "pxm", "pnm", "ras", "rast", "tiff", "tif", "hdr", "jp2", "jp2k", "j2k", "j2c", "jxl"};
    private static final String[] writerMIMETypes = new String[0];
    private static final String[] writerFileSuffixes = new String[0];

    @Override
    public String[] getReaderMIMETypes() {
        return readerMIMETypes;
    }

    @Override
    public String[] getReaderExtensions() {
        return readerFileSuffixes;
    }

    @Override
    public boolean isMimeTypeSupported(String mimeType) {
        if (mimeType != null) {
            for (String mime : this.getReaderMIMETypes()) {
                if (!mimeType.equals(mime)) continue;
                return true;
            }
        }
        return false;
    }

    @Override
    public MediaReader<ImageElement> getMediaIO(URI media, String mimeType, Hashtable<String, Object> properties) {
        if (this.isMimeTypeSupported(mimeType)) {
            return new ImageCVIO(media, mimeType, this);
        }
        return null;
    }

    @Override
    public String getCodecName() {
        return "OpenCV imgcodecs";
    }

    @Override
    public String[] getWriterExtensions() {
        return writerFileSuffixes;
    }

    @Override
    public String[] getWriterMIMETypes() {
        return writerMIMETypes;
    }

    @Activate
    protected void activate(ComponentContext context) {
        OpenCVNativeLoader loader = new OpenCVNativeLoader();
        loader.init();
        LOGGER.info("Native OpenCV is activated");
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
    }
}

