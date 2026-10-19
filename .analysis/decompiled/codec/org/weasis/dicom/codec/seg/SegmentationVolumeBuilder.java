/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 *  org.opencv.core.Core
 *  org.opencv.core.Mat
 *  org.opencv.core.Scalar
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.ui.model.graphic.imp.seg.SegRegion
 *  org.weasis.opencv.data.ImageCV
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.ImageConversion
 *  org.weasis.opencv.seg.RegionAttributes
 */
package org.weasis.dicom.codec.seg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.img.util.DicomUtils;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.ui.model.graphic.imp.seg.SegRegion;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.DicomMediaIO;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.geometry.VectorUtils;
import org.weasis.dicom.codec.seg.LabelMapScanner;
import org.weasis.dicom.codec.seg.MaskFrames;
import org.weasis.dicom.codec.seg.SegBinaryMaskWorkaround;
import org.weasis.dicom.codec.seg.SegMaskOrientation;
import org.weasis.dicom.codec.seg.SegSpecialElement;
import org.weasis.dicom.codec.seg.SegmentationVolume;
import org.weasis.opencv.data.ImageCV;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.ImageConversion;
import org.weasis.opencv.seg.RegionAttributes;

public final class SegmentationVolumeBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SegmentationVolumeBuilder.class);
    private static final double TOLERANCE_MM = 0.05;

    private SegmentationVolumeBuilder() {
    }

    private static void checkInterrupted() {
        if (Thread.interrupted()) {
            throw new CancellationException("Segmentation volume build was cancelled");
        }
    }

    public static SegmentationVolume buildCanonical(SegSpecialElement segElement, DicomSeries segSeries) {
        StampStats stats;
        DicomImageElement first;
        if (segElement == null || segSeries == null) {
            return null;
        }
        DicomMediaIO mediaIO = segElement.getMediaReader();
        if (mediaIO == null) {
            return null;
        }
        Attributes dicom = mediaIO.getDicomObject();
        if (dicom == null) {
            return null;
        }
        Attributes sharedFG = dicom.getNestedDataset(1375769129);
        Sequence perFrameSeq = dicom.getSequence(1375769136);
        if (perFrameSeq == null || perFrameSeq.isEmpty()) {
            LOGGER.debug("SEG has no PerFrameFunctionalGroupsSequence; cannot build canonical volume");
            return null;
        }
        ArrayList<FrameSpatialInfo> frames = new ArrayList<FrameSpatialInfo>(perFrameSeq.size());
        for (Attributes frame : perFrameSeq) {
            SegmentationVolumeBuilder.checkInterrupted();
            frames.add(SegmentationVolumeBuilder.extractSpatialInfo(frame, sharedFG, dicom));
        }
        FrameSpatialInfo reference = frames.stream().filter(Objects::nonNull).findFirst().orElse(null);
        if (reference == null) {
            LOGGER.debug("No SEG frame exposes complete spatial metadata");
            return null;
        }
        Vector3d axisX = new Vector3d((Vector3dc)reference.rowDir);
        Vector3d axisY = new Vector3d((Vector3dc)reference.colDir);
        Vector3d axisZ = VectorUtils.computeNormalOfSurface(axisX, axisY).normalize();
        double minProj = Double.POSITIVE_INFINITY;
        double maxProj = Double.NEGATIVE_INFINITY;
        double[] projections = new double[frames.size()];
        Arrays.fill(projections, Double.NaN);
        int validCount = 0;
        for (int i = 0; i < frames.size(); ++i) {
            double p;
            FrameSpatialInfo f = (FrameSpatialInfo)((Object)frames.get(i));
            if (f == null) continue;
            projections[i] = p = axisZ.dot((Vector3dc)f.position);
            if (p < minProj) {
                minProj = p;
            }
            if (p > maxProj) {
                maxProj = p;
            }
            ++validCount;
        }
        if (validCount == 0) {
            return null;
        }
        double sliceSpacing = SegmentationVolumeBuilder.estimateSliceSpacing(projections, reference);
        double colSpacing = reference.pixelSpacing[1];
        double rowSpacing = reference.pixelSpacing[0];
        int sizeX = dicom.getInt(2621457, 0);
        int sizeY = dicom.getInt(2621456, 0);
        String segUid = dicom.getString(524312);
        if ((sizeX <= 0 || sizeY <= 0) && (first = (DicomImageElement)segSeries.getMedia(0, null, null)) != null) {
            if (sizeX <= 0) {
                sizeX = SegmentationVolumeBuilder.safeWidth(first);
            }
            if (sizeY <= 0) {
                sizeY = SegmentationVolumeBuilder.safeHeight(first);
            }
        }
        if (sizeX <= 0 || sizeY <= 0) {
            LOGGER.debug("Cannot determine SEG mask dimensions");
            return null;
        }
        int sizeZ = Math.max(1, (int)Math.round((maxProj - minProj) / sliceSpacing) + 1);
        Vector3d origin = new Vector3d((Vector3dc)reference.position);
        double refProj = axisZ.dot((Vector3dc)reference.position);
        origin.fma(refProj - minProj, (Vector3dc)new Vector3d((Vector3dc)axisZ).negate());
        Map<Integer, SegRegion<DicomImageElement>> segAttrs = segElement.getSegAttributes();
        boolean isLabelMap = "LABELMAP".equalsIgnoreCase(dicom.getString(6422529));
        boolean noOverlap = isLabelMap || "NO".equalsIgnoreCase(dicom.getString(6422547));
        SegmentationVolume volume = new SegmentationVolume(sizeX, sizeY, sizeZ, new Vector3d(colSpacing, rowSpacing, sliceSpacing), origin, axisX, axisY, axisZ, segAttrs, noOverlap);
        boolean binaryDecoderWorkaround = SegBinaryMaskWorkaround.isAffected(dicom);
        try {
            stats = SegmentationVolumeBuilder.stampAllFrames(volume, segSeries, perFrameSeq, frames, sizeX, sizeY, sizeZ, origin, axisX, axisY, axisZ, colSpacing, rowSpacing, sliceSpacing, minProj, isLabelMap, segAttrs, segUid, binaryDecoderWorkaround ? dicom : null);
        }
        catch (CancellationException ce) {
            LOGGER.debug("Canonical SEG volume build cancelled");
            volume.removeData();
            throw ce;
        }
        if (stats.total == 0) {
            LOGGER.debug("No SEG frame could be stamped into the canonical volume");
            volume.removeData();
            return null;
        }
        LOGGER.info("Built canonical SegmentationVolume {}x{}x{} (storage: {}, stamped: {} fast + {} transform)", new Object[]{sizeX, sizeY, sizeZ, volume.isShortMode() ? "short" : "byte", stats.fast, stats.transform});
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("  {} ms decoding frames, {} ms stamping", (Object)(stats.decodeNanos / 1000000L), (Object)(stats.stampNanos / 1000000L));
            LOGGER.debug("  first decoded mask: {}x{}, axisX={}, axisY={}, axisZ={}, ps=[{}, {}, {}], origin={}", new Object[]{stats.firstW, stats.firstH, axisX, axisY, axisZ, colSpacing, rowSpacing, sliceSpacing, origin});
        }
        volume.applySegmentVoxelCounts();
        return volume;
    }

    private static StampStats stampAllFrames(SegmentationVolume volume, DicomSeries segSeries, Sequence perFrameSeq, List<FrameSpatialInfo> frames, int sizeX, int sizeY, int sizeZ, Vector3d origin, Vector3d axisX, Vector3d axisY, Vector3d axisZ, double colSpacing, double rowSpacing, double sliceSpacing, double minProj, boolean isLabelMap, Map<Integer, ? extends RegionAttributes> segAttrs, String segUid, Attributes binaryDecoderWorkaroundDicom) {
        StampStats stats = new StampStats();
        for (int i = 0; i < frames.size(); ++i) {
            DicomImageElement maskElement;
            List<Integer> declaredSegs;
            SegmentationVolumeBuilder.checkInterrupted();
            FrameSpatialInfo info = frames.get(i);
            if (info == null) continue;
            Attributes frame = (Attributes)perFrameSeq.get(i);
            List<Integer> list = declaredSegs = isLabelMap ? null : SegmentationVolumeBuilder.resolveSegmentNumber(frame, segAttrs);
            if (!isLabelMap && declaredSegs == null || (maskElement = (DicomImageElement)segSeries.getMedia(i, null, null)) == null) continue;
            SegmentationVolumeBuilder.stampFrame(volume, maskElement, info, declaredSegs, isLabelMap, segAttrs, sizeX, sizeY, sizeZ, origin, axisX, axisY, axisZ, colSpacing, rowSpacing, sliceSpacing, minProj, segUid, stats, binaryDecoderWorkaroundDicom, i);
        }
        return stats;
    }

    private static List<Integer> resolveSegmentNumber(Attributes frame, Map<Integer, ? extends RegionAttributes> segAttrs) {
        Attributes segIdSeq = frame.getNestedDataset(6422538);
        if (segIdSeq == null) {
            return null;
        }
        Integer segNum = DicomUtils.getIntegerFromDicomElement(segIdSeq, 6422539, null);
        return segNum != null && segAttrs.containsKey(segNum) ? List.of(segNum) : null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void stampFrame(SegmentationVolume volume, DicomImageElement maskElement, FrameSpatialInfo info, List<Integer> declaredSegs, boolean isLabelMap, Map<Integer, ? extends RegionAttributes> segAttrs, int sizeX, int sizeY, int sizeZ, Vector3d origin, Vector3d axisX, Vector3d axisY, Vector3d axisZ, double colSpacing, double rowSpacing, double sliceSpacing, double minProj, String segUid, StampStats stats, Attributes binaryDecoderWorkaroundDicom, int frameIndex) {
        boolean cached;
        long decodeStart = System.nanoTime();
        PlanarImage rawMask = null;
        if (binaryDecoderWorkaroundDicom != null) {
            rawMask = SegBinaryMaskWorkaround.reDecodeFrame(binaryDecoderWorkaroundDicom, frameIndex, sizeX, sizeY);
        }
        boolean bl = cached = rawMask == null;
        if (cached) {
            rawMask = MaskFrames.acquire(maskElement);
        }
        stats.decodeNanos += System.nanoTime() - decodeStart;
        if (rawMask == null || rawMask.width() <= 0 || rawMask.height() <= 0) {
            SegmentationVolumeBuilder.releaseRawMask(maskElement, rawMask, cached);
            return;
        }
        PlanarImage maskImage = SegMaskOrientation.normalize(rawMask, sizeX, sizeY, segUid);
        if (maskImage == null) {
            SegmentationVolumeBuilder.releaseRawMask(maskElement, rawMask, cached);
            return;
        }
        boolean transposed = maskImage != rawMask;
        try {
            List<Integer> resolvedSegs;
            if (stats.firstW < 0) {
                stats.firstW = maskImage.width();
                stats.firstH = maskImage.height();
            }
            boolean dimsMatch = maskImage.width() == sizeX && maskImage.height() == sizeY;
            boolean axisAligned = dimsMatch && SegmentationVolumeBuilder.isAxisAligned(info, origin, axisX, axisY, colSpacing, rowSpacing, sizeX, sizeY, maskImage);
            List<Integer> list = resolvedSegs = isLabelMap ? SegmentationVolumeBuilder.collectLabelMapSegments(maskImage, segAttrs) : declaredSegs;
            if (resolvedSegs.isEmpty()) {
                return;
            }
            long stampStart = System.nanoTime();
            for (Integer segNum : resolvedSegs) {
                PlanarImage stampMask = isLabelMap ? SegmentationVolumeBuilder.extractLabelMask(maskImage, segNum) : maskImage;
                try {
                    if (axisAligned) {
                        int sliceZ = (int)Math.round((axisZ.dot((Vector3dc)info.position) - minProj) / sliceSpacing);
                        if (sliceZ < 0 || sliceZ >= sizeZ) continue;
                        volume.stampAxialMask(stampMask, sliceZ, segNum);
                        ++stats.total;
                        ++stats.fast;
                        continue;
                    }
                    volume.stampMaskWithTransform(stampMask, info.position, info.rowDir, info.colDir, info.pixelSpacing, segNum);
                    ++stats.total;
                    ++stats.transform;
                }
                finally {
                    if (!isLabelMap) continue;
                    stampMask.release();
                }
            }
            stats.stampNanos += System.nanoTime() - stampStart;
        }
        finally {
            if (transposed) {
                maskImage.release();
            }
            SegmentationVolumeBuilder.releaseRawMask(maskElement, rawMask, cached);
        }
    }

    private static void releaseRawMask(DicomImageElement maskElement, PlanarImage rawMask, boolean cached) {
        if (cached) {
            MaskFrames.release(maskElement, rawMask);
        } else if (rawMask != null) {
            ImageConversion.releasePlanarImage((PlanarImage)rawMask);
        }
    }

    private static int safeWidth(DicomImageElement el) {
        return SegmentationVolumeBuilder.safeDim(el, true);
    }

    private static int safeHeight(DicomImageElement el) {
        return SegmentationVolumeBuilder.safeDim(el, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static int safeDim(DicomImageElement el, boolean width) {
        PlanarImage img = MaskFrames.acquire(el);
        try {
            int n = img == null ? 0 : (width ? img.width() : img.height());
            return n;
        }
        finally {
            MaskFrames.release(el, img);
        }
    }

    private static double estimateSliceSpacing(double[] projections, FrameSpatialInfo reference) {
        double[] sorted = Arrays.stream(projections).filter(d -> !Double.isNaN(d)).sorted().toArray();
        if (sorted.length >= 2) {
            double[] deltas = new double[sorted.length - 1];
            int n = 0;
            for (int i = 1; i < sorted.length; ++i) {
                double d2 = Math.abs(sorted[i] - sorted[i - 1]);
                if (!(d2 > 1.0E-6)) continue;
                deltas[n++] = d2;
            }
            if (n > 0) {
                double[] trimmed = Arrays.copyOf(deltas, n);
                Arrays.sort(trimmed);
                return trimmed[trimmed.length / 2];
            }
        }
        return reference.sliceThickness > 0.0 ? reference.sliceThickness : 1.0;
    }

    private static boolean isAxisAligned(FrameSpatialInfo info, Vector3d origin, Vector3d axisX, Vector3d axisY, double colSpacing, double rowSpacing, int sizeX, int sizeY, PlanarImage mask) {
        if (mask.width() != sizeX || mask.height() != sizeY) {
            return false;
        }
        if (info.rowDir.dot((Vector3dc)axisX) < 0.999) {
            return false;
        }
        if (info.colDir.dot((Vector3dc)axisY) < 0.999) {
            return false;
        }
        if (Math.abs(info.pixelSpacing[1] - colSpacing) > 0.05) {
            return false;
        }
        if (Math.abs(info.pixelSpacing[0] - rowSpacing) > 0.05) {
            return false;
        }
        Vector3d offset = new Vector3d((Vector3dc)info.position).sub((Vector3dc)origin);
        if (Math.abs(offset.dot((Vector3dc)axisX)) > 0.05) {
            return false;
        }
        return Math.abs(offset.dot((Vector3dc)axisY)) <= 0.05;
    }

    private static FrameSpatialInfo extractSpatialInfo(Attributes frame, Attributes sharedFG, Attributes dicom) {
        Attributes pms;
        double[] ipp = SegmentationVolumeBuilder.nested(frame, 2134291, 0x200032);
        if (ipp == null) {
            ipp = SegmentationVolumeBuilder.nested(sharedFG, 2134291, 0x200032);
        }
        if (ipp == null) {
            ipp = DicomUtils.getDoubleArrayFromDicomElement(dicom, 0x200032, null);
        }
        if (ipp == null || ipp.length != 3) {
            return null;
        }
        double[] iop = SegmentationVolumeBuilder.nested(frame, 2134294, 2097207);
        if (iop == null) {
            iop = SegmentationVolumeBuilder.nested(sharedFG, 2134294, 2097207);
        }
        if (iop == null) {
            iop = DicomUtils.getDoubleArrayFromDicomElement(dicom, 2097207, null);
        }
        if (iop == null || iop.length != 6) {
            return null;
        }
        double[] ps = SegmentationVolumeBuilder.nested(frame, 2658576, 2621488);
        if (ps == null) {
            ps = SegmentationVolumeBuilder.nested(sharedFG, 2658576, 2621488);
        }
        if (ps == null) {
            ps = DicomUtils.getDoubleArrayFromDicomElement(dicom, 2621488, null);
        }
        if (ps == null || ps.length < 2) {
            ps = new double[]{1.0, 1.0};
        }
        Double thickness = null;
        Attributes attributes = pms = frame == null ? null : frame.getNestedDataset(2658576);
        if (pms == null && sharedFG != null) {
            pms = sharedFG.getNestedDataset(2658576);
        }
        if (pms != null) {
            thickness = DicomUtils.getDoubleFromDicomElement(pms, 1572944, null);
        }
        return new FrameSpatialInfo(new Vector3d(ipp), new Vector3d(iop[0], iop[1], iop[2]), new Vector3d(iop[3], iop[4], iop[5]), ps, thickness == null ? 0.0 : thickness);
    }

    private static double[] nested(Attributes attrs, int seqTag, int valueTag) {
        if (attrs == null) {
            return null;
        }
        Attributes nested = attrs.getNestedDataset(seqTag);
        if (nested == null) {
            return null;
        }
        return DicomUtils.getDoubleArrayFromDicomElement(nested, valueTag, null);
    }

    private static List<Integer> collectLabelMapSegments(PlanarImage maskImage, Map<Integer, ? extends RegionAttributes> segAttrs) {
        int[] labels = LabelMapScanner.sortedDistinctLabels(maskImage.toMat(), segAttrs::containsKey);
        return labels.length == 0 ? List.of() : Arrays.stream(labels).boxed().toList();
    }

    private static PlanarImage extractLabelMask(PlanarImage labelMap, int segNum) {
        Mat src = labelMap.toMat();
        ImageCV out = new ImageCV();
        Core.compare((Mat)src, (Scalar)new Scalar((double)segNum), (Mat)out, (int)0);
        return out;
    }

    private record FrameSpatialInfo(Vector3d position, Vector3d rowDir, Vector3d colDir, double[] pixelSpacing, double sliceThickness) {
    }

    private static final class StampStats {
        int total;
        int fast;
        int transform;
        int firstW = -1;
        int firstH = -1;
        long decodeNanos;
        long stampNanos;

        private StampStats() {
        }
    }
}

