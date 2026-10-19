/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 *  org.weasis.core.api.media.data.MediaElement
 *  org.weasis.core.api.media.data.MediaSeries
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.util.StringUtil
 *  org.weasis.opencv.seg.RegionAttributes
 */
package org.weasis.dicom.codec;

import java.awt.Color;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.DicomElement;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.geometry.ImageOrientation;
import org.weasis.dicom.codec.seg.LazyContourLoader;
import org.weasis.dicom.codec.seg.SegmentationVolume;
import org.weasis.dicom.codec.seg.VolumeSliceContourLoader;
import org.weasis.dicom.codec.utils.DicomMediaUtils;
import org.weasis.opencv.seg.RegionAttributes;

public interface SpecialElementRegion {
    public static final double POSITION_TOLERANCE = 0.07;
    public static final double POSITION_TOLERANCE_OBLIQUE = 2.5;
    public static final double ORIENTATION_COSINE_STRICT = 0.9999;
    public static final double ORIENTATION_COSINE_OBLIQUE = 0.95;

    public boolean isVisible();

    public void setVisible(boolean var1);

    default public String getRegionUID() {
        MediaElement media;
        String uid;
        SpecialElementRegion specialElementRegion = this;
        if (specialElementRegion instanceof MediaElement && StringUtil.hasText((CharSequence)(uid = TagD.getTagValue((TagReadable)(media = (MediaElement)specialElementRegion), 524312, String.class)))) {
            return uid;
        }
        return Integer.toHexString(System.identityHashCode(this));
    }

    default public boolean isReady() {
        return true;
    }

    public float getOpacity();

    public void setOpacity(float var1);

    public Map<String, Map<String, Set<LazyContourLoader>>> getRefMap();

    public NavigableMap<Double, Set<LazyContourLoader>> getPositionMap();

    public Map<Integer, ? extends RegionAttributes> getSegAttributes();

    default public Vector3d getReferenceNormal() {
        return null;
    }

    default public double getSliceSpacing() {
        return 0.0;
    }

    default public Vector3d getMaskRowDirection() {
        return null;
    }

    default public Vector3d getMaskColumnDirection() {
        return null;
    }

    default public double[] getMaskPixelSpacing() {
        return null;
    }

    default public int getMaskWidth() {
        return 0;
    }

    default public int getMaskHeight() {
        return 0;
    }

    default public SegmentationVolume getOrBuildSegmentationVolume() {
        return null;
    }

    default public SegmentationVolume peekCanonicalSegmentationVolume() {
        return null;
    }

    default public boolean isSegmentationVolumeBuilding() {
        return false;
    }

    default public boolean isFractionalSeg() {
        return false;
    }

    default public boolean matchesSeries(MediaSeries<?> series) {
        SpecialElementRegion specialElementRegion;
        if (series == null) {
            return false;
        }
        String seriesUID = TagD.getTagValue(series, 0x20000E, String.class);
        if (seriesUID != null && this.getRefMap().containsKey(seriesUID)) {
            return true;
        }
        if (this.isReady() && !this.getPositionMap().isEmpty() && (specialElementRegion = this) instanceof DicomElement) {
            DicomElement group = (DicomElement)((Object)specialElementRegion);
            String frameOfRef = TagD.getTagValue(series, 0x200052, String.class);
            String segFrameOfRef = TagD.getTagValue((TagReadable)group.getMediaReader().getMediaSeries(), 0x200052, String.class);
            return frameOfRef != null && frameOfRef.equals(segFrameOfRef);
        }
        return false;
    }

    default public boolean containsSopInstanceUIDReference(DicomImageElement img) {
        String seriesUID;
        if (!this.isReady()) {
            return false;
        }
        if (img != null && (seriesUID = TagD.getTagValue((TagReadable)img, 0x20000E, String.class)) != null) {
            DicomElement group;
            String frameOfRef2;
            SpecialElementRegion specialElementRegion;
            DicomSeries series;
            String frameOfRef;
            NavigableMap<Double, Set<LazyContourLoader>> positionMap = this.getPositionMap();
            if (!positionMap.isEmpty() && (frameOfRef = TagD.getTagValue((TagReadable)(series = img.getMediaReader().getMediaSeries()), 0x200052, String.class)) != null && (specialElementRegion = this) instanceof DicomElement && Objects.equals(frameOfRef, frameOfRef2 = TagD.getTagValue((TagReadable)(group = (DicomElement)((Object)specialElementRegion)).getMediaReader().getMediaSeries(), 0x200052, String.class))) {
                Vector3d segNormal = this.getReferenceNormal();
                if (segNormal == null) {
                    return true;
                }
                Vector3d imgNormal = DicomMediaUtils.computeImageNormal((TagReadable)img);
                if (imgNormal == null) {
                    return true;
                }
                return Math.abs(imgNormal.dot((Vector3dc)segNormal)) >= 0.95 || this.getOrBuildSegmentationVolume() != null;
            }
            String sopInstanceUID = TagD.getTagValue((TagReadable)img, 524312, String.class);
            Map<String, Set<LazyContourLoader>> map = this.getRefMap().get(seriesUID);
            if (map != null && sopInstanceUID != null) {
                return map.containsKey(sopInstanceUID);
            }
        }
        return false;
    }

    default public Set<LazyContourLoader> getContours(DicomImageElement img) {
        if (!this.isReady()) {
            return null;
        }
        String seriesUID = TagD.getTagValue((TagReadable)img, 0x20000E, String.class);
        if (seriesUID != null) {
            PositionMatch match;
            Set<LazyContourLoader> volumeContours;
            boolean fractional = this.isFractionalSeg();
            if (!fractional && this.peekCanonicalSegmentationVolume() != null && (volumeContours = this.lookupByVolume(img)) != null) {
                return volumeContours;
            }
            NavigableMap<Double, Set<LazyContourLoader>> positionMap = this.getPositionMap();
            PositionMatch positionMatch = match = positionMap.isEmpty() ? PositionMatch.NOT_APPLICABLE : this.lookupByPosition(img, positionMap);
            if (match.loaders() != null) {
                return match.loaders();
            }
            if (!match.applicable() || fractional) {
                Set<LazyContourLoader> volumeContours2 = this.lookupByVolume(img);
                if (volumeContours2 != null) {
                    return volumeContours2;
                }
                if (!fractional && this.isSegmentationVolumeBuilding()) {
                    return null;
                }
            }
            String sopInstanceUID = TagD.getTagValue((TagReadable)img, 524312, String.class);
            Map<String, Set<LazyContourLoader>> map = this.getRefMap().get(seriesUID);
            if (map != null && sopInstanceUID != null) {
                Set<LazyContourLoader> loader;
                Object object;
                int frames = img.getMediaReader().getMediaElementNumber();
                if (frames > 1 && (object = img.getKey()) instanceof Integer) {
                    Integer intVal = (Integer)object;
                    loader = map.get(sopInstanceUID + "_" + intVal);
                } else {
                    loader = map.get(sopInstanceUID);
                }
                return loader;
            }
        }
        return null;
    }

    private Set<LazyContourLoader> lookupByVolume(DicomImageElement img) {
        SegmentationVolume volume = this.getOrBuildSegmentationVolume();
        if (volume == null) {
            return null;
        }
        LinkedHashSet<LazyContourLoader> set = new LinkedHashSet<LazyContourLoader>();
        set.add(new VolumeSliceContourLoader(volume, img));
        return set;
    }

    private PositionMatch lookupByPosition(DicomImageElement img, NavigableMap<Double, Set<LazyContourLoader>> positionMap) {
        Double loc;
        Vector3d imgNormal;
        Vector3d segNormal = this.getReferenceNormal();
        if (segNormal != null && (imgNormal = DicomMediaUtils.computeImageNormal((TagReadable)img)) != null) {
            double cos = Math.abs(imgNormal.dot((Vector3dc)segNormal));
            if (cos < 0.95) {
                return PositionMatch.NOT_APPLICABLE;
            }
            if (!this.isInPlaneGridCompatible(img)) {
                return PositionMatch.NOT_APPLICABLE;
            }
            Double key = SpecialElementRegion.projectOnNormal(img, segNormal);
            if (key != null) {
                double baseTol = cos >= 0.9999 ? 0.07 : 2.5;
                double segSpacing = this.getSliceSpacing();
                double imgThickness = SpecialElementRegion.imageSliceThickness(img);
                double tol = Math.max(baseTol, Math.max(segSpacing * 0.5, imgThickness * 0.5)) + 1.0E-4;
                if (this.isFractionalSeg()) {
                    return PositionMatch.of(SpecialElementRegion.findByTolerance(positionMap, key, tol));
                }
                return PositionMatch.of(SpecialElementRegion.findNearest(positionMap, key, tol));
            }
        }
        if ((loc = (Double)img.getTagValue(TagW.SlicePosition)) == null) {
            loc = TagD.getTagValue((TagReadable)img, 2101313, Double.class);
        }
        if (loc != null) {
            Set<LazyContourLoader> loaders = SpecialElementRegion.findByTolerance(positionMap, loc, 0.07);
            return loaders == null ? PositionMatch.NOT_APPLICABLE : PositionMatch.of(loaders);
        }
        return PositionMatch.NOT_APPLICABLE;
    }

    private static Double projectOnNormal(DicomImageElement img, Vector3d normal) {
        double[] ipp = TagD.getTagValue((TagReadable)img, 0x200032, double[].class);
        if (ipp == null || ipp.length != 3) {
            return null;
        }
        return normal.dot((Vector3dc)new Vector3d(ipp));
    }

    private boolean isInPlaneGridCompatible(DicomImageElement img) {
        Vector3d segRow = this.getMaskRowDirection();
        Vector3d segCol = this.getMaskColumnDirection();
        if (segRow == null || segCol == null) {
            return true;
        }
        Vector3d imgRow = ImageOrientation.getRowImagePosition((TagReadable)img);
        Vector3d imgCol = ImageOrientation.getColumnImagePosition((TagReadable)img);
        if (imgRow == null || imgCol == null) {
            return true;
        }
        if (segRow.dot((Vector3dc)imgRow) < 0.9999) {
            return false;
        }
        if (segCol.dot((Vector3dc)imgCol) < 0.9999) {
            return false;
        }
        double[] segPs = this.getMaskPixelSpacing();
        double[] imgPs = TagD.getTagValue((TagReadable)img, 2621488, double[].class);
        if (segPs != null && imgPs != null && segPs.length >= 2 && imgPs.length >= 2) {
            if (Math.abs(segPs[0] - imgPs[0]) > 0.001) {
                return false;
            }
            if (Math.abs(segPs[1] - imgPs[1]) > 0.001) {
                return false;
            }
        }
        Integer imgW = TagD.getTagValue((TagReadable)img, 2621457, Integer.class);
        Integer imgH = TagD.getTagValue((TagReadable)img, 2621456, Integer.class);
        int segW = this.getMaskWidth();
        int segH = this.getMaskHeight();
        if (segW > 0 && imgW != null && imgW != segW) {
            return false;
        }
        return segH <= 0 || imgH == null || imgH == segH;
    }

    private static double imageSliceThickness(DicomImageElement img) {
        Double v = TagD.getTagValue((TagReadable)img, 0x180088, Double.class);
        if (v == null || v <= 0.0) {
            v = TagD.getTagValue((TagReadable)img, 1572944, Double.class);
        }
        return v == null || v <= 0.0 ? 0.0 : v;
    }

    default public void updateOpacityInSegAttributes(float opacity) {
        int opacityValue = (int)(opacity * 255.0f);
        this.getSegAttributes().values().forEach(c -> {
            Color color = c.getColor();
            color = new Color(color.getRed(), color.getGreen(), color.getBlue(), opacityValue);
            c.setColor(color);
        });
    }

    public static Set<LazyContourLoader> findByTolerance(NavigableMap<Double, Set<LazyContourLoader>> map, double target, double tolerance) {
        NavigableMap<Double, Set<LazyContourLoader>> sub = map.subMap(target - tolerance, true, target + tolerance, true);
        if (sub.isEmpty()) {
            return null;
        }
        if (sub.size() == 1) {
            return sub.firstEntry().getValue();
        }
        LinkedHashSet<LazyContourLoader> merged = new LinkedHashSet<LazyContourLoader>();
        for (Set s : sub.values()) {
            merged.addAll(s);
        }
        return merged;
    }

    public static Set<LazyContourLoader> findNearest(NavigableMap<Double, Set<LazyContourLoader>> map, double target, double tolerance) {
        Map.Entry<Double, Set<LazyContourLoader>> below = map.floorEntry(target);
        Map.Entry<Double, Set<LazyContourLoader>> above = map.ceilingEntry(target);
        double distBelow = below == null ? Double.MAX_VALUE : target - below.getKey();
        double distAbove = above == null ? Double.MAX_VALUE : above.getKey() - target;
        Map.Entry<Double, Set<LazyContourLoader>> nearest = distAbove <= distBelow ? above : below;
        return nearest == null || Math.min(distBelow, distAbove) > tolerance ? null : nearest.getValue();
    }

    public static NavigableMap<Double, Set<LazyContourLoader>> emptyPositionMap() {
        return Collections.emptyNavigableMap();
    }

    public record PositionMatch(boolean applicable, Set<LazyContourLoader> loaders) {
        static final PositionMatch NOT_APPLICABLE = new PositionMatch(false, null);

        static PositionMatch of(Set<LazyContourLoader> loaders) {
            return new PositionMatch(true, loaders);
        }
    }
}

