/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 *  org.weasis.core.api.media.data.MediaSeries
 *  org.weasis.core.api.media.data.MediaSeries$MEDIA_POSITION
 *  org.weasis.core.api.media.data.TagReadable
 */
package org.weasis.dicom.codec.geometry;

import java.util.Objects;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.geometry.Orientation;
import org.weasis.dicom.codec.geometry.PatientOrientation;
import org.weasis.dicom.codec.geometry.VectorUtils;

public abstract class ImageOrientation {
    private static final double OBLIQUITY_THRESHOLD = 0.8;

    public static Vector3d getRowImagePosition(TagReadable taggable) {
        double[] imagePosition = TagD.getTagValue(Objects.requireNonNull(taggable), 2097207, double[].class);
        if (imagePosition != null && imagePosition.length == 6) {
            return new Vector3d(imagePosition);
        }
        return null;
    }

    public static Vector3d getColumnImagePosition(TagReadable taggable) {
        double[] imagePosition = TagD.getTagValue(Objects.requireNonNull(taggable), 2097207, double[].class);
        if (imagePosition != null && imagePosition.length == 6) {
            return new Vector3d(imagePosition[3], imagePosition[4], imagePosition[5]);
        }
        return null;
    }

    private static Orientation getPatientOrientation(Vector3d v, boolean quadruped) {
        double absX = Math.abs(v.x);
        double absY = Math.abs(v.y);
        double absZ = Math.abs(v.z);
        if (absX > 0.8 && absX > absY && absX > absZ) {
            return quadruped ? PatientOrientation.getQuadrupedXOrientation(v) : PatientOrientation.getBipedXOrientation(v);
        }
        if (absY > 0.8 && absY > absX && absY > absZ) {
            return quadruped ? PatientOrientation.getQuadrupedYOrientation(v) : PatientOrientation.getBipedYOrientation(v);
        }
        if (absZ > 0.8 && absZ > absX && absZ > absY) {
            return quadruped ? PatientOrientation.getQuadrupedZOrientation(v) : PatientOrientation.getBipedZOrientation(v);
        }
        return null;
    }

    public static Plan getPlan(Vector3d vr, Vector3d vc) {
        Orientation rowAxis = ImageOrientation.getPatientOrientation(vr, false);
        Orientation colAxis = ImageOrientation.getPatientOrientation(vc, false);
        if (rowAxis != null && colAxis != null) {
            if (rowAxis.getColor().equals(PatientOrientation.blue) && colAxis.getColor().equals(PatientOrientation.red)) {
                return Plan.AXIAL;
            }
            if (colAxis.getColor().equals(PatientOrientation.blue) && rowAxis.getColor().equals(PatientOrientation.red)) {
                return Plan.AXIAL;
            }
            if (rowAxis.getColor().equals(PatientOrientation.blue) && colAxis.getColor().equals(PatientOrientation.green)) {
                return Plan.CORONAL;
            }
            if (colAxis.getColor().equals(PatientOrientation.blue) && rowAxis.getColor().equals(PatientOrientation.green)) {
                return Plan.CORONAL;
            }
            if (rowAxis.getColor().equals(PatientOrientation.red) && colAxis.getColor().equals(PatientOrientation.green)) {
                return Plan.SAGITTAL;
            }
            if (colAxis.getColor().equals(PatientOrientation.red) && rowAxis.getColor().equals(PatientOrientation.green)) {
                return Plan.SAGITTAL;
            }
        }
        return Plan.OBLIQUE;
    }

    public static Plan getPlan(TagReadable taggable) {
        Vector3d vr = ImageOrientation.getRowImagePosition(taggable);
        Vector3d vc = ImageOrientation.getColumnImagePosition(taggable);
        if (vr != null && vc != null) {
            return ImageOrientation.getPlan(vr, vc);
        }
        return null;
    }

    public static String getOrientation(Vector3d v, boolean quadruped) {
        Enum orientationX = quadruped ? PatientOrientation.getQuadrupedXOrientation(v) : PatientOrientation.getBipedXOrientation(v);
        Enum orientationY = quadruped ? PatientOrientation.getQuadrupedYOrientation(v) : PatientOrientation.getBipedYOrientation(v);
        Enum orientationZ = quadruped ? PatientOrientation.getQuadrupedZOrientation(v) : PatientOrientation.getBipedZOrientation(v);
        double absX = Math.abs(v.x);
        double absY = Math.abs(v.y);
        double absZ = Math.abs(v.z);
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < 3; ++i) {
            if (absX > 1.0E-4 && absX >= absY && absX >= absZ) {
                buffer.append(orientationX.name());
                buffer.append(" ");
                absX = 0.0;
                continue;
            }
            if (absY > 1.0E-4 && absY >= absX && absY >= absZ) {
                buffer.append(orientationY.name());
                buffer.append(" ");
                absY = 0.0;
                continue;
            }
            if (!(absZ > 1.0E-4) || !(absZ >= absX) || !(absZ >= absY)) break;
            buffer.append(orientationZ.name());
            buffer.append(" ");
            absZ = 0.0;
        }
        return buffer.toString();
    }

    public static String getImageOrientationOpposite(String val, boolean quadruped) {
        if (quadruped) {
            PatientOrientation.Quadruped quad = PatientOrientation.Quadruped.getQuadruped(val);
            return quad == null ? "" : PatientOrientation.getOppositeOrientation(quad).name();
        }
        PatientOrientation.Biped biped = PatientOrientation.Biped.getBiped(val);
        return biped == null ? "" : PatientOrientation.getOppositeOrientation(biped).name();
    }

    public static boolean hasSameOrientation(MediaSeries<DicomImageElement> series1, MediaSeries<DicomImageElement> series2) {
        if (series1 != null && series2 != null) {
            DicomImageElement image1 = (DicomImageElement)series1.getMedia(MediaSeries.MEDIA_POSITION.MIDDLE, null, null);
            DicomImageElement image2 = (DicomImageElement)series2.getMedia(MediaSeries.MEDIA_POSITION.MIDDLE, null, null);
            return ImageOrientation.hasSameOrientation(image1, image2);
        }
        return false;
    }

    public static boolean hasSameOrientation(DicomImageElement image1, DicomImageElement image2) {
        if (image1 != null && image2 != null) {
            Vector3d vr1 = ImageOrientation.getRowImagePosition((TagReadable)image1);
            Vector3d vc1 = ImageOrientation.getColumnImagePosition((TagReadable)image1);
            Vector3d vr2 = ImageOrientation.getRowImagePosition((TagReadable)image2);
            Vector3d vc2 = ImageOrientation.getColumnImagePosition((TagReadable)image2);
            if (vr1 != null && vc1 != null && vr2 != null && vc2 != null) {
                return ImageOrientation.hasSameOrientation(vr1, vc1, vr2, vc2);
            }
        }
        return false;
    }

    public static boolean hasSameOrientation(Vector3d vr1, Vector3d vc1, Vector3d vr2, Vector3d vc2) {
        if (vr1 != null && vc1 != null && vr2 != null && vc2 != null) {
            Plan plan1 = ImageOrientation.getPlan(vr1, vc1);
            Plan plan2 = ImageOrientation.getPlan(vr2, vc2);
            if (plan1 != null && !plan1.equals((Object)Plan.OBLIQUE)) {
                return plan1.equals((Object)plan2);
            }
            Vector3d normal1 = VectorUtils.computeNormalOfSurface(vr1, vc1);
            Vector3d normal2 = VectorUtils.computeNormalOfSurface(vr2, vc2);
            if (normal1 != null && normal2 != null) {
                normal1.mul((Vector3dc)normal2);
                return normal1.x + normal1.y + normal1.z > 0.95;
            }
        }
        return false;
    }

    public static enum Plan {
        UNKNOWN,
        AXIAL,
        SAGITTAL,
        CORONAL,
        OBLIQUE;

    }
}

