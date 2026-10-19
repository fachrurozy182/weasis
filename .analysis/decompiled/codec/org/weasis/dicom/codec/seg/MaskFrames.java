/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.ImageConversion
 */
package org.weasis.dicom.codec.seg;

import java.util.function.Function;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.ImageConversion;

public final class MaskFrames {
    private MaskFrames() {
    }

    public static PlanarImage acquire(DicomImageElement frame) {
        frame.pinInCache();
        PlanarImage image = frame.getImage();
        return image == null || image.width() <= 0 || image.height() <= 0 ? null : image;
    }

    public static void release(DicomImageElement frame, PlanarImage image) {
        frame.unpinFromCache();
        if (frame.isPinnedInCache()) {
            return;
        }
        ImageConversion.releasePlanarImage((PlanarImage)image);
        frame.removeImageFromCache();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static <T> T withImage(DicomImageElement frame, T emptyResult, Function<PlanarImage, T> body) {
        PlanarImage image = MaskFrames.acquire(frame);
        try {
            T t = image == null ? emptyResult : body.apply(image);
            return t;
        }
        finally {
            MaskFrames.release(frame, image);
        }
    }
}

