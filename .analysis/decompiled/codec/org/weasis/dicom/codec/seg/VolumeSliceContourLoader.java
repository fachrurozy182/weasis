/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3d
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.ui.model.graphic.imp.seg.SegContour
 *  org.weasis.opencv.data.PlanarImage
 */
package org.weasis.dicom.codec.seg;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.joml.Vector3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.ui.model.graphic.imp.seg.SegContour;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.geometry.ImageOrientation;
import org.weasis.dicom.codec.seg.LazyContourLoader;
import org.weasis.dicom.codec.seg.MaskFrames;
import org.weasis.dicom.codec.seg.SegmentationVolume;
import org.weasis.opencv.data.PlanarImage;

public final class VolumeSliceContourLoader
implements LazyContourLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(VolumeSliceContourLoader.class);
    private static final double[] DEFAULT_PIXEL_SPACING = new double[]{1.0, 1.0};
    private final SegmentationVolume volume;
    private final DicomImageElement image;
    private volatile Set<SegContour> cached;

    public VolumeSliceContourLoader(SegmentationVolume volume, DicomImageElement image) {
        this.volume = volume;
        this.image = image;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Set<SegContour> getLazyContours() {
        Set<SegContour> result = this.cached;
        if (result != null) {
            return result;
        }
        VolumeSliceContourLoader volumeSliceContourLoader = this;
        synchronized (volumeSliceContourLoader) {
            if (this.cached == null) {
                this.cached = this.compute();
            }
            return this.cached;
        }
    }

    private Set<SegContour> compute() {
        if (this.volume == null || this.image == null) {
            return Set.of();
        }
        PlaneParams p = this.resolvePlaneParams();
        if (p == null) {
            return Set.of();
        }
        Map<Integer, List<SegContour>> map = this.volume.getContoursForImagePlane(p.ipp, p.row, p.col, p.ps[1], p.ps[0], p.w, p.h);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Volume reslice: img IPP={} rowDir={} colDir={} ps=[{}, {}] size={}x{} \u2192 {} segments", new Object[]{p.ipp, p.row, p.col, p.ps[0], p.ps[1], p.w, p.h, map == null ? 0 : map.size()});
        }
        if (map == null || map.isEmpty()) {
            return Set.of();
        }
        return map.values().stream().flatMap(Collection::stream).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private PlaneParams resolvePlaneParams() {
        int[] dims;
        double[] ipp = TagD.getTagValue((TagReadable)this.image, 0x200032, double[].class);
        if (ipp == null || ipp.length != 3) {
            return null;
        }
        Vector3d row = ImageOrientation.getRowImagePosition((TagReadable)this.image);
        Vector3d col = ImageOrientation.getColumnImagePosition((TagReadable)this.image);
        if (row == null || col == null) {
            return null;
        }
        double[] ps = TagD.getTagValue((TagReadable)this.image, 2621488, double[].class);
        if (ps == null || ps.length < 2) {
            ps = DEFAULT_PIXEL_SPACING;
        }
        if ((dims = this.readDimensions()) == null) {
            return null;
        }
        return new PlaneParams(new Vector3d(ipp), row, col, ps, dims[0], dims[1]);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int[] readDimensions() {
        PlanarImage planar = MaskFrames.acquire(this.image);
        try {
            int[] nArray;
            if (planar == null) {
                int[] nArray2 = null;
                return nArray2;
            }
            int w = planar.width();
            int h = planar.height();
            if (w > 0 && h > 0) {
                int[] nArray3 = new int[2];
                nArray3[0] = w;
                nArray = nArray3;
                nArray3[1] = h;
            } else {
                nArray = null;
            }
            int[] nArray4 = nArray;
            return nArray4;
        }
        finally {
            MaskFrames.release(this.image, planar);
        }
    }

    private record PlaneParams(Vector3d ipp, Vector3d row, Vector3d col, double[] ps, int w, int h) {
    }
}

