/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.Core
 *  org.opencv.core.Mat
 *  org.weasis.core.ui.model.graphic.imp.seg.SegContour
 *  org.weasis.core.ui.model.graphic.imp.seg.SegRegion
 *  org.weasis.core.util.SoftHashMap
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.ImageConversion
 *  org.weasis.opencv.seg.Region
 */
package org.weasis.dicom.codec.seg;

import java.util.List;
import java.util.Set;
import org.dcm4che3.data.Attributes;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.weasis.core.ui.model.graphic.imp.seg.SegContour;
import org.weasis.core.ui.model.graphic.imp.seg.SegRegion;
import org.weasis.core.util.SoftHashMap;
import org.weasis.dicom.codec.DcmMediaReader;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.seg.CachedContourLoader;
import org.weasis.dicom.codec.seg.MaskFrames;
import org.weasis.dicom.codec.seg.SegBinaryMaskWorkaround;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.ImageConversion;
import org.weasis.opencv.seg.Region;

public final class BasicContourLoader
extends CachedContourLoader {
    private static final SoftHashMap<String, Set<SegContour>> CACHE = new SoftHashMap();
    private final DicomImageElement binaryMask;
    private final int id;
    private final SegRegion<?> region;

    public BasicContourLoader(DicomImageElement binaryMask, int id, SegRegion<?> region) {
        super(CACHE, binaryMask.toString() + "_" + id);
        this.binaryMask = binaryMask;
        this.id = id;
        this.region = region;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected Set<SegContour> build() {
        PlanarImage reDecoded;
        Attributes dicom = this.segDicomObject();
        if (SegBinaryMaskWorkaround.isAffected(dicom) && (reDecoded = SegBinaryMaskWorkaround.reDecodeFrame(dicom, this.id - 1, dicom.getInt(2621457, 0), dicom.getInt(2621456, 0))) != null) {
            try {
                Set<SegContour> set = this.buildContours(reDecoded);
                return set;
            }
            finally {
                ImageConversion.releasePlanarImage((PlanarImage)reDecoded);
            }
        }
        return MaskFrames.withImage(this.binaryMask, Set.of(), this::buildContours);
    }

    private Attributes segDicomObject() {
        DcmMediaReader reader = this.binaryMask.getMediaReader();
        return reader == null ? null : reader.getDicomObject();
    }

    private Set<SegContour> buildContours(PlanarImage binary) {
        Mat mat = binary.toMat();
        List segmentList = Region.buildSegmentList((PlanarImage)binary);
        int pixelCount = Core.countNonZero((Mat)mat);
        SegContour contour = new SegContour(String.valueOf(this.id), segmentList, pixelCount);
        this.region.addPixels((Region)contour);
        contour.setAttributes(this.region);
        return Set.of(contour);
    }
}

