/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.MathUtil
 *  org.weasis.core.util.StringUtil
 */
package org.dcm4che3.img.util;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.img.data.CIELab;
import org.dcm4che3.img.util.DateTimeUtils;
import org.dcm4che3.img.util.DicomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.MathUtil;
import org.weasis.core.util.StringUtil;

public final class DicomObjectUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomObjectUtil.class);
    private static final int RGB_MIN = 0;
    private static final int RGB_MAX = 255;
    private static final int POLYGON_MIN_POINTS = 6;

    private DicomObjectUtil() {
    }

    public static List<Attributes> getSequence(Attributes dicom, int tagSeq) {
        if (dicom == null) {
            return Collections.emptyList();
        }
        Sequence item = dicom.getSequence(tagSeq);
        return item != null ? item : Collections.emptyList();
    }

    public static boolean isImageFrameApplicableToReferencedImageSequence(List<Attributes> refImgSeq, int childTag, String sopInstanceUID, int frame, boolean required) {
        if (!required && (refImgSeq == null || refImgSeq.isEmpty())) {
            return true;
        }
        if (!StringUtil.hasText((CharSequence)sopInstanceUID)) {
            return false;
        }
        return refImgSeq.stream().filter(sopUID -> sopInstanceUID.equals(sopUID.getString(528725))).findFirst().map(sopUID -> DicomObjectUtil.isFrameInReferencedFrames(sopUID, childTag, frame)).orElse(false);
    }

    private static boolean isFrameInReferencedFrames(Attributes sopUID, int childTag, int frame) {
        int[] frames = sopUID.getInts(childTag);
        return frames == null || frames.length == 0 || Arrays.stream(frames).anyMatch(f -> f == frame);
    }

    public static LocalDate getDicomDate(String date) {
        if (!StringUtil.hasText((CharSequence)date)) {
            return null;
        }
        try {
            return DateTimeUtils.parseDA(date);
        }
        catch (Exception e) {
            LOGGER.error("Failed to parse DICOM date: {}", (Object)date, (Object)e);
            return null;
        }
    }

    public static LocalTime getDicomTime(String time) {
        if (!StringUtil.hasText((CharSequence)time)) {
            return null;
        }
        try {
            return DateTimeUtils.parseTM(time);
        }
        catch (Exception e) {
            LOGGER.error("Failed to parse DICOM time: {}", (Object)time, (Object)e);
            return null;
        }
    }

    public static LocalDateTime dateTime(Attributes dcm, int dateID, int timeID) {
        if (dcm == null) {
            return null;
        }
        LocalDate date = DicomObjectUtil.getDicomDate(dcm.getString(dateID));
        if (date == null) {
            return null;
        }
        LocalTime time = DicomObjectUtil.getDicomTime(dcm.getString(timeID));
        return time == null ? date.atStartOfDay() : date.atTime(time);
    }

    public static Area getShutterShape(Attributes dcm) {
        String shutterShape = DicomUtils.getStringFromDicomElement(dcm, 1578496);
        if (shutterShape == null) {
            return null;
        }
        Area combinedShape = null;
        if (DicomObjectUtil.containsShutterType(shutterShape, "RECTANGULAR", "RECTANGLE")) {
            combinedShape = DicomObjectUtil.createRectangularShutter(dcm);
        }
        if (shutterShape.contains("CIRCULAR")) {
            Area circularShutter = DicomObjectUtil.createCircularShutter(dcm);
            combinedShape = DicomObjectUtil.combineShutterAreas(combinedShape, circularShutter);
        }
        if (shutterShape.contains("POLYGONAL")) {
            Area polygonalShutter = DicomObjectUtil.createPolygonalShutter(dcm);
            combinedShape = DicomObjectUtil.combineShutterAreas(combinedShape, polygonalShutter);
        }
        return combinedShape;
    }

    private static boolean containsShutterType(String shutterShape, String ... types) {
        return Arrays.stream(types).anyMatch(shutterShape::contains);
    }

    private static Area createRectangularShutter(Attributes dcm) {
        int left = dcm.getInt(1578498, 0);
        int top = dcm.getInt(1578502, 0);
        int right = dcm.getInt(1578500, 0);
        int bottom = dcm.getInt(1578504, 0);
        Rectangle2D.Double rect = new Rectangle2D.Double();
        rect.setFrameFromDiagonal(left, top, right, bottom);
        if (((RectangularShape)rect).isEmpty()) {
            LOGGER.warn("Shutter rectangle has an empty area: left={}, top={}, right={}, bottom={}", new Object[]{left, top, right, bottom});
            return null;
        }
        return new Area(rect);
    }

    private static Area createCircularShutter(Attributes dcm) {
        int[] center = dcm.getInts(1578512);
        if (center == null || center.length < 2) {
            LOGGER.warn("Invalid or missing center coordinates for circular shutter");
            return null;
        }
        double radius = dcm.getInt(1578514, 0);
        if (radius <= 0.0) {
            LOGGER.warn("Invalid radius for circular shutter: {}", (Object)radius);
            return null;
        }
        double centerX = center[1];
        double centerY = center[0];
        Ellipse2D.Double ellipse = new Ellipse2D.Double(centerX - radius, centerY - radius, 2.0 * radius, 2.0 * radius);
        return new Area(ellipse);
    }

    private static Area createPolygonalShutter(Attributes dcm) {
        int[] points = dcm.getInts(1578528);
        if (points == null || points.length < 6) {
            LOGGER.warn("Insufficient points for polygonal shutter: {} points needed, {} provided", (Object)3, (Object)(points != null ? points.length / 2 : 0));
            return null;
        }
        Polygon polygon = new Polygon();
        for (int i = 0; i < points.length / 2; ++i) {
            polygon.addPoint(points[i * 2 + 1], points[i * 2]);
        }
        if (!DicomObjectUtil.isPolygonValid(polygon)) {
            LOGGER.warn("Shutter polygon has zero area or is invalid");
            return null;
        }
        return new Area(polygon);
    }

    private static Area combineShutterAreas(Area existing, Area newArea) {
        if (newArea == null) {
            return existing;
        }
        if (existing == null) {
            return newArea;
        }
        existing.intersect(newArea);
        return existing;
    }

    private static boolean isPolygonValid(Polygon polygon) {
        if (polygon.npoints <= 2) {
            return false;
        }
        double area = 0.0;
        for (int i = 0; i < polygon.npoints; ++i) {
            int nextIndex = (i + 1) % polygon.npoints;
            area += (double)polygon.xpoints[i] * (double)polygon.ypoints[nextIndex] - (double)polygon.xpoints[nextIndex] * (double)polygon.ypoints[i];
        }
        return MathUtil.isDifferentFromZero((double)area);
    }

    public static Color getShutterColor(Attributes dcm) {
        int[] rgb = CIELab.dicomLab2rgb(dcm.getInts(1578532));
        int pGray = dcm.getInt(1578530, 0);
        return DicomObjectUtil.createColorFromValues(pGray, rgb);
    }

    public static Color getRGBColor(int pGray, int[] rgbaColour) {
        return DicomObjectUtil.createColorFromValues(pGray, rgbaColour);
    }

    private static Color createColorFromValues(int pGray, int[] rgbaColour) {
        if (rgbaColour != null && rgbaColour.length >= 3) {
            int r = MathUtil.clamp((int)rgbaColour[0], (int)0, (int)255);
            int g = MathUtil.clamp((int)rgbaColour[1], (int)0, (int)255);
            int b = MathUtil.clamp((int)rgbaColour[2], (int)0, (int)255);
            int a = rgbaColour.length > 3 ? MathUtil.clamp((int)rgbaColour[3], (int)0, (int)255) : 255;
            return new Color(r, g, b, a);
        }
        int gray = MathUtil.clamp((int)(pGray >>> 8), (int)0, (int)255);
        return new Color(gray, gray, gray);
    }
}

