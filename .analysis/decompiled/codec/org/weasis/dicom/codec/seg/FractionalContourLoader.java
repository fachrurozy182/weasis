/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.Core
 *  org.opencv.core.CvType
 *  org.opencv.core.Mat
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.ui.model.graphic.imp.seg.SegContour
 *  org.weasis.core.ui.model.graphic.imp.seg.SegRegion
 *  org.weasis.core.util.SoftHashMap
 *  org.weasis.opencv.data.ImageCV
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.seg.Region
 */
package org.weasis.dicom.codec.seg;

import java.util.Set;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.ui.model.graphic.imp.seg.SegContour;
import org.weasis.core.ui.model.graphic.imp.seg.SegRegion;
import org.weasis.core.util.SoftHashMap;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.seg.CachedContourLoader;
import org.weasis.dicom.codec.seg.MaskFrames;
import org.weasis.opencv.data.ImageCV;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.seg.Region;

public final class FractionalContourLoader
extends CachedContourLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(FractionalContourLoader.class);
    private static final SoftHashMap<String, Set<SegContour>> CACHE = new SoftHashMap();
    private final DicomImageElement frame;
    private final int id;
    private final SegRegion<?> region;
    private final int maxFractionalValue;

    public FractionalContourLoader(DicomImageElement frame, int id, SegRegion<?> region, int maxFractionalValue) {
        super(CACHE, frame.toString() + "_frac_" + id);
        this.frame = frame;
        this.id = id;
        this.region = region;
        this.maxFractionalValue = Math.max(1, maxFractionalValue);
    }

    @Override
    protected Set<SegContour> build() {
        return MaskFrames.withImage(this.frame, Set.of(), image -> {
            try {
                Mat src = image.toMat();
                ImageCV normalized = this.normalizeToByteRange(src);
                double weightedPixelCount = Core.sumElems((Mat)src).val[0] / (double)this.maxFractionalValue;
                SegContour contour = new SegContour(String.valueOf(this.id), (PlanarImage)normalized, weightedPixelCount);
                this.region.addPixels((Region)contour);
                contour.setAttributes(this.region);
                return Set.of(contour);
            }
            catch (RuntimeException e) {
                LOGGER.error("Error building fractional contour for frame {}", (Object)this.id, (Object)e);
                return Set.of();
            }
        });
    }

    private ImageCV normalizeToByteRange(Mat src) {
        ImageCV result = new ImageCV();
        if (this.maxFractionalValue == 255 && src.type() == CvType.CV_8UC1) {
            src.copyTo((Mat)result);
        } else {
            src.convertTo((Mat)result, CvType.CV_8UC1, 255.0 / (double)this.maxFractionalValue);
        }
        return result;
    }
}

