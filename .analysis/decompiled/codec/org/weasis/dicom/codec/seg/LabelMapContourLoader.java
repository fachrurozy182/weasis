/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.Core
 *  org.opencv.core.Mat
 *  org.opencv.core.Scalar
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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.ui.model.graphic.imp.seg.SegContour;
import org.weasis.core.ui.model.graphic.imp.seg.SegRegion;
import org.weasis.core.util.SoftHashMap;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.seg.CachedContourLoader;
import org.weasis.dicom.codec.seg.LabelMapScanner;
import org.weasis.dicom.codec.seg.MaskFrames;
import org.weasis.opencv.data.ImageCV;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.seg.Region;

public final class LabelMapContourLoader
extends CachedContourLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(LabelMapContourLoader.class);
    private static final SoftHashMap<String, Set<SegContour>> CACHE = new SoftHashMap();
    private final DicomImageElement frame;
    private final int id;
    private final Map<Integer, ? extends SegRegion<?>> segments;

    public LabelMapContourLoader(DicomImageElement frame, int id, Map<Integer, ? extends SegRegion<?>> segments) {
        super(CACHE, frame.toString() + "_label_" + id);
        this.frame = frame;
        this.id = id;
        this.segments = segments;
    }

    @Override
    protected Set<SegContour> build() {
        return MaskFrames.withImage(this.frame, Set.of(), image -> {
            try {
                Mat src = image.toMat();
                int[] labels = LabelMapScanner.sortedDistinctLabels(src, this.segments::containsKey);
                if (labels.length == 0) {
                    return Set.of();
                }
                LinkedHashSet<SegContour> result = new LinkedHashSet<SegContour>(labels.length);
                for (int label : labels) {
                    SegContour contour = this.buildLabelContour(src, label);
                    if (contour == null) continue;
                    result.add(contour);
                }
                return result;
            }
            catch (RuntimeException e) {
                LOGGER.error("Error building label-map contours for frame {}", (Object)this.id, (Object)e);
                return Set.of();
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private SegContour buildLabelContour(Mat src, int label) {
        SegRegion<?> region = this.segments.get(label);
        if (region == null) {
            return null;
        }
        ImageCV binary = new ImageCV();
        try {
            Core.compare((Mat)src, (Scalar)new Scalar((double)label), (Mat)binary, (int)0);
            int pixelCount = Core.countNonZero((Mat)binary);
            if (pixelCount == 0) {
                SegContour segContour = null;
                return segContour;
            }
            List segmentList = Region.buildSegmentList((PlanarImage)binary);
            SegContour contour = new SegContour(label + "_" + this.id, segmentList, pixelCount);
            region.addPixels((Region)contour);
            contour.setAttributes(region);
            SegContour segContour = contour;
            return segContour;
        }
        finally {
            binary.release();
        }
    }
}

