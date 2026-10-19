/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.dcm4che3.data.Attributes
 *  org.dcm4che3.img.data.CIELab
 *  org.dcm4che3.img.util.DicomObjectUtil
 *  org.dcm4che3.img.util.DicomUtils
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.media.data.ImageElement
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.ui.model.GraphicModel
 *  org.weasis.core.ui.model.graphic.Graphic
 *  org.weasis.core.ui.model.graphic.imp.NonEditableGraphic
 *  org.weasis.core.ui.model.graphic.imp.PointGraphic
 *  org.weasis.core.ui.model.graphic.imp.area.EllipseGraphic
 *  org.weasis.core.ui.model.graphic.imp.area.PolygonGraphic
 *  org.weasis.core.ui.model.graphic.imp.area.RectangleGraphic
 *  org.weasis.core.ui.model.graphic.imp.line.PolylineGraphic
 *  org.weasis.core.ui.model.utils.exceptions.InvalidShapeException
 *  org.weasis.core.ui.serialize.XmlSerializer
 *  org.weasis.core.util.MathUtil
 *  org.weasis.dicom.codec.PresentationStateReader
 */
package org.weasis.dicom.explorer.pr;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.img.data.CIELab;
import org.dcm4che3.img.util.DicomObjectUtil;
import org.dcm4che3.img.util.DicomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.ui.model.GraphicModel;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.graphic.imp.NonEditableGraphic;
import org.weasis.core.ui.model.graphic.imp.PointGraphic;
import org.weasis.core.ui.model.graphic.imp.area.EllipseGraphic;
import org.weasis.core.ui.model.graphic.imp.area.PolygonGraphic;
import org.weasis.core.ui.model.graphic.imp.area.RectangleGraphic;
import org.weasis.core.ui.model.graphic.imp.line.PolylineGraphic;
import org.weasis.core.ui.model.utils.exceptions.InvalidShapeException;
import org.weasis.core.ui.serialize.XmlSerializer;
import org.weasis.core.util.MathUtil;
import org.weasis.dicom.codec.PresentationStateReader;
import org.weasis.dicom.explorer.pr.InterpolatedPath2D;

public class PrGraphicUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrGraphicUtil.class);
    public static final String POINT = "POINT";
    public static final String POLYLINE = "POLYLINE";
    public static final String INTERPOLATED = "INTERPOLATED";
    public static final String CIRCLE = "CIRCLE";
    public static final String ELLIPSE = "ELLIPSE";
    public static final String MULTILINE = "MULTILINE";
    public static final String INFINITELINE = "INFINITELINE";
    public static final String CUTLINE = "CUTLINE";
    public static final String RANGELINE = "RANGELINE";
    public static final String RULER = "RULER";
    public static final String AXIS = "AXIS";
    public static final String CROSSHAIR = "CROSSHAIR";
    public static final String ARROW = "ARROW";
    public static final String RECTANGLE = "RECTANGLE";
    public static final String MULTIPOINT = "MULTIPOINT";
    private static final int DEFAULT_POINT_SIZE = 3;
    private static final float DEFAULT_LINE_THICKNESS = 1.0f;

    private PrGraphicUtil() {
    }

    public static Graphic buildGraphic(Attributes attributes, Color defaultColor, boolean labelVisible, double width, double height, boolean canBeEdited, AffineTransform inverse, boolean dcmSR) throws InvalidShapeException {
        if (attributes == null) {
            LOGGER.warn("Cannot build graphic from null attributes");
            return null;
        }
        try {
            GraphicContext context = new GraphicContext(attributes, defaultColor, labelVisible, width, height, canBeEdited, inverse, dcmSR);
            return PrGraphicUtil.buildGraphicByType(context);
        }
        catch (Exception e) {
            LOGGER.error("Error building graphic: {}", (Object)e.getMessage(), (Object)e);
            throw new InvalidShapeException("Failed to build graphic", (Throwable)e);
        }
    }

    public static Graphic buildCompoundGraphic(Attributes attributes, Color defaultColor, boolean labelVisible, double width, double height, AffineTransform inverse) throws InvalidShapeException {
        if (attributes == null) {
            LOGGER.warn("Cannot build compound graphic from null attributes");
            return null;
        }
        try {
            CompoundGraphicContext context = new CompoundGraphicContext(attributes, defaultColor, labelVisible, width, height, inverse);
            return PrGraphicUtil.buildGraphicByType(context);
        }
        catch (Exception e) {
            LOGGER.error("Error building compound graphic: {}", (Object)e.getMessage(), (Object)e);
            throw new InvalidShapeException("Failed to build compound graphic", (Throwable)e);
        }
    }

    private static Graphic buildGraphicByType(BaseGraphicContext context) throws InvalidShapeException {
        String type = context.getType();
        if (type == null) {
            LOGGER.warn("Cannot build graphic with null type");
            return null;
        }
        GraphicBuilder builder = PrGraphicUtil.getGraphicBuilder(type.toUpperCase());
        if (builder == null) {
            LOGGER.warn("Unsupported graphic type: {}", (Object)type);
            return null;
        }
        return builder.build(context);
    }

    private static GraphicBuilder getGraphicBuilder(String type) {
        return switch (type) {
            case POINT -> PrGraphicUtil::buildPoint;
            case POLYLINE, MULTILINE -> PrGraphicUtil::buildPolyline;
            case INTERPOLATED -> PrGraphicUtil::buildInterpolated;
            case CIRCLE -> PrGraphicUtil::buildCircle;
            case ELLIPSE -> PrGraphicUtil::buildEllipse;
            case MULTIPOINT -> PrGraphicUtil::buildMultiPoint;
            case RECTANGLE -> PrGraphicUtil::buildRectangle;
            case RULER, ARROW -> PrGraphicUtil::buildLine;
            default -> null;
        };
    }

    private static double getRotationAngle(Attributes attributes) {
        Float angle = DicomUtils.getFloatFromDicomElement((Attributes)attributes, (int)7340592, null);
        return angle != null ? Math.toRadians(angle.floatValue()) : 0.0;
    }

    private static Point2D getRotationPoint(Attributes attributes, BaseGraphicContext context) {
        float[] rotPoint = DicomUtils.getFloatArrayFromDicomElement((Attributes)attributes, (int)7340659, null);
        if (rotPoint != null && rotPoint.length >= 2) {
            return context.transformPoint(rotPoint[0], rotPoint[1]);
        }
        return null;
    }

    private static Graphic buildSrEllipse(BaseGraphicContext context, float[] pts) throws InvalidShapeException {
        Point2D center = context.transformPoint(pts[0], pts[1]);
        Point2D boundary = context.transformPoint(pts[2], pts[3]);
        double radius = center.distance(boundary);
        Ellipse2D.Double ellipse = new Ellipse2D.Double();
        ellipse.setFrameFromCenter(center.getX(), center.getY(), center.getX() + radius, center.getY() + radius);
        double rotation = PrGraphicUtil.getRotationAngle(context.attributes);
        Point2D rotPoint = PrGraphicUtil.getRotationPoint(context.attributes, context);
        if (MathUtil.isDifferentFromZero((double)rotation)) {
            double rx = rotPoint != null ? rotPoint.getX() : center.getX();
            double ry = rotPoint != null ? rotPoint.getY() : center.getY();
            AffineTransform rotate = AffineTransform.getRotateInstance(rotation, rx, ry);
            Shape rotatedEllipse = rotate.createTransformedShape(ellipse);
            return PrGraphicUtil.createNonEditableGraphic(context, rotatedEllipse, context.isFilled());
        }
        return PrGraphicUtil.createEllipseGraphic(context, ellipse);
    }

    private static Graphic buildPoint(BaseGraphicContext context) throws InvalidShapeException {
        PointData data = PrGraphicUtil.validateAndExtractPointData(context);
        if (data == null) {
            return null;
        }
        if (context.canBeEdited()) {
            PointGraphic graphic = new PointGraphic();
            graphic.buildGraphic(Collections.singletonList(data.point()));
            graphic.setPointSize(Integer.valueOf(3));
            context.applyProperties((Graphic)graphic, true);
            return graphic;
        }
        return PrGraphicUtil.createPointShape(context, data.point());
    }

    private static Graphic buildPolyline(BaseGraphicContext context) throws InvalidShapeException {
        PolylineData data = PrGraphicUtil.validateAndExtractPolylineData(context);
        if (data == null) {
            return null;
        }
        List<Point2D> points = data.points();
        boolean isClosed = data.isClosed(context.isDcmSR());
        if (context.canBeEdited()) {
            return PrGraphicUtil.createEditablePolyline(context, points, isClosed);
        }
        return PrGraphicUtil.createNonEditablePolyline(context, points, isClosed);
    }

    private static Graphic buildInterpolated(BaseGraphicContext context) {
        PolylineData data = PrGraphicUtil.validateAndExtractPolylineData(context);
        if (data == null) {
            return null;
        }
        Path2D path = PrGraphicUtil.createInterpolatedPath(context, data.rawPoints());
        return PrGraphicUtil.createNonEditableGraphic(context, path, context.isFilled());
    }

    private static Graphic buildCircle(BaseGraphicContext context) throws InvalidShapeException {
        LineData data = PrGraphicUtil.validateAndExtractLineData(context);
        if (data == null) {
            return null;
        }
        double radius = data.start().distance(data.end());
        Ellipse2D.Double ellipse = new Ellipse2D.Double();
        ellipse.setFrameFromCenter(data.start().getX(), data.start().getY(), data.start().getX() + radius, data.start().getY() + radius);
        return PrGraphicUtil.createEllipseGraphic(context, ellipse);
    }

    private static Graphic buildEllipse(BaseGraphicContext context) throws InvalidShapeException {
        float[] pts = context.getPoints();
        if (pts == null) {
            return null;
        }
        if (context.isDcmSR() && pts.length == 4) {
            return PrGraphicUtil.buildSrEllipse(context, pts);
        }
        if (pts.length >= 8) {
            EllipseData data = PrGraphicUtil.validateAndExtractEllipseData(context);
            if (data == null) {
                return null;
            }
            EllipseGeometry geometry = PrGraphicUtil.calculateEllipseGeometry(data);
            return PrGraphicUtil.createEllipseWithRotation(context, geometry);
        }
        LOGGER.warn("Invalid ellipse data: expected 4 (SR) or 8 (PR) coordinates, got {}", (Object)pts.length);
        return null;
    }

    private static Graphic buildMultiPoint(BaseGraphicContext context) {
        PolylineData data = PrGraphicUtil.validateAndExtractPolylineData(context);
        if (data == null) {
            return null;
        }
        Path2D path = PrGraphicUtil.createMultiPointPath(context, data.rawPoints());
        return PrGraphicUtil.createNonEditableGraphic(context, path, true);
    }

    private static Graphic buildRectangle(BaseGraphicContext context) throws InvalidShapeException {
        double rotation;
        RectangleData data = PrGraphicUtil.validateAndExtractRectangleData(context);
        if (data == null) {
            return null;
        }
        Rectangle2D rectangle = PrGraphicUtil.createRectangle(data);
        if (context.isDcmSR() && MathUtil.isDifferentFromZero((double)(rotation = PrGraphicUtil.getRotationAngle(context.attributes)))) {
            Point2D rotPoint = PrGraphicUtil.getRotationPoint(context.attributes, context);
            double rx = rotPoint != null ? rotPoint.getX() : rectangle.getCenterX();
            double ry = rotPoint != null ? rotPoint.getY() : rectangle.getCenterY();
            AffineTransform rotate = AffineTransform.getRotateInstance(rotation, rx, ry);
            Shape rotatedRect = rotate.createTransformedShape(rectangle);
            return PrGraphicUtil.createNonEditableGraphic(context, rotatedRect, context.isFilled());
        }
        return PrGraphicUtil.createRectangleGraphic(context, rectangle);
    }

    private static Graphic buildLine(BaseGraphicContext context) {
        LineData data = PrGraphicUtil.validateAndExtractLineData(context);
        if (data == null) {
            return null;
        }
        Path2D.Double path = new Path2D.Double();
        ((Path2D)path).moveTo(data.start().getX(), data.start().getY());
        ((Path2D)path).lineTo(data.end().getX(), data.end().getY());
        return PrGraphicUtil.createNonEditableGraphic(context, path, false);
    }

    private static PointData validateAndExtractPointData(BaseGraphicContext context) {
        float[] points = context.getPoints();
        if (points == null || points.length < 2) {
            return null;
        }
        Point2D point = context.transformPoint(points[0], points[1]);
        return new PointData(point, points);
    }

    private static LineData validateAndExtractLineData(BaseGraphicContext context) {
        float[] points = PrGraphicUtil.getFloats(context);
        if (points == null) {
            return null;
        }
        Point2D start = context.transformPoint(points[0], points[1]);
        Point2D end = context.transformPoint(points[2], points[3]);
        return new LineData(start, end, points);
    }

    private static float[] getFloats(BaseGraphicContext context) {
        float[] points = context.getPoints();
        if (points == null || points.length < 4) {
            return null;
        }
        return points;
    }

    private static PolylineData validateAndExtractPolylineData(BaseGraphicContext context) {
        float[] points = PrGraphicUtil.getFloats(context);
        if (points == null) {
            return null;
        }
        List<Point2D> transformedPoints = PrGraphicUtil.transformPoints(context, points);
        return new PolylineData(transformedPoints, points);
    }

    private static RectangleData validateAndExtractRectangleData(BaseGraphicContext context) {
        float[] points = PrGraphicUtil.getFloats(context);
        if (points == null) {
            return null;
        }
        Point2D topLeft = context.transformPoint(points[0], points[1]);
        Point2D bottomRight = context.transformPoint(points[2], points[3]);
        return new RectangleData(topLeft, bottomRight, points);
    }

    private static EllipseData validateAndExtractEllipseData(BaseGraphicContext context) {
        float[] pts = context.getPoints();
        if (pts == null || pts.length < 8) {
            return null;
        }
        Point2D major1 = context.transformPoint(pts[0], pts[1]);
        Point2D major2 = context.transformPoint(pts[2], pts[3]);
        Point2D minor1 = context.transformPoint(pts[4], pts[5]);
        Point2D minor2 = context.transformPoint(pts[6], pts[7]);
        return new EllipseData(major1, major2, minor1, minor2, pts);
    }

    private static List<Point2D> transformPoints(BaseGraphicContext context, float[] points) {
        int size = points.length / 2;
        ArrayList<Point2D> handlePoints = new ArrayList<Point2D>(size);
        for (int i = 0; i < size; ++i) {
            Point2D point = context.transformPoint(points[i * 2], points[i * 2 + 1]);
            handlePoints.add(point);
        }
        return handlePoints;
    }

    private static Graphic createPointShape(BaseGraphicContext context, Point2D point) {
        Ellipse2D.Double ellipse = new Ellipse2D.Double(point.getX() - 1.5, point.getY() - 1.5, 3.0, 3.0);
        return PrGraphicUtil.createNonEditableGraphic(context, ellipse, true);
    }

    private static Graphic createEditablePolyline(BaseGraphicContext context, List<Point2D> points, boolean isClosed) throws InvalidShapeException {
        if (!(context instanceof GraphicContext)) {
            throw new InvalidShapeException("Cannot create editable polyline from non-graphic context");
        }
        GraphicContext graphicContext = (GraphicContext)context;
        if (isClosed) {
            PolygonGraphic graphic = new PolygonGraphic();
            graphic.buildGraphic(points);
            graphicContext.applyProperties((Graphic)graphic, graphicContext.isFilled());
            return graphic;
        }
        PolylineGraphic graphic = new PolylineGraphic();
        graphic.buildGraphic(points);
        graphicContext.applyProperties((Graphic)graphic, false);
        return graphic;
    }

    private static Graphic createNonEditablePolyline(BaseGraphicContext context, List<Point2D> points, boolean isClosed) {
        Path2D path = PrGraphicUtil.createPath2D(points);
        if (context.isDcmSR() || isClosed) {
            path.closePath();
        }
        return PrGraphicUtil.createNonEditableGraphic(context, path, context.isFilled());
    }

    private static Graphic createRectangleGraphic(BaseGraphicContext context, Rectangle2D rect) throws InvalidShapeException {
        if (context instanceof GraphicContext) {
            GraphicContext graphicContext = (GraphicContext)context;
            if (context.canBeEdited()) {
                RectangleGraphic graphic = new RectangleGraphic();
                graphic.buildGraphic(rect);
                graphicContext.applyProperties((Graphic)graphic, graphicContext.isFilled());
                return graphic;
            }
        }
        return PrGraphicUtil.createNonEditableGraphic(context, rect, context.isFilled());
    }

    private static Graphic createEllipseGraphic(BaseGraphicContext context, Ellipse2D ellipse) throws InvalidShapeException {
        if (context instanceof GraphicContext) {
            GraphicContext graphicContext = (GraphicContext)context;
            if (context.canBeEdited()) {
                EllipseGraphic graphic = new EllipseGraphic();
                graphic.buildGraphic(ellipse.getFrame());
                graphicContext.applyProperties((Graphic)graphic, graphicContext.isFilled());
                return graphic;
            }
        }
        return PrGraphicUtil.createNonEditableGraphic(context, ellipse, context.isFilled());
    }

    private static Graphic createEllipseWithRotation(BaseGraphicContext context, EllipseGeometry geometry) throws InvalidShapeException {
        Ellipse2D.Double ellipse = new Ellipse2D.Double();
        ellipse.setFrameFromCenter(geometry.cx(), geometry.cy(), geometry.cx() + geometry.rx(), geometry.cy() + geometry.ry());
        if (MathUtil.isDifferentFromZero((double)geometry.rotation())) {
            AffineTransform rotate = AffineTransform.getRotateInstance(geometry.rotation(), geometry.cx(), geometry.cy());
            Shape rotatedEllipse = rotate.createTransformedShape(ellipse);
            return PrGraphicUtil.createNonEditableGraphic(context, rotatedEllipse, context.isFilled());
        }
        return PrGraphicUtil.createEllipseGraphic(context, ellipse);
    }

    private static Graphic createNonEditableGraphic(BaseGraphicContext context, Shape shape, boolean filled) {
        NonEditableGraphic graphic = new NonEditableGraphic(shape);
        context.applyProperties((Graphic)graphic, filled);
        return graphic;
    }

    private static Path2D createPath2D(List<Point2D> points) {
        Path2D.Double path = new Path2D.Double(1, points.size());
        if (!points.isEmpty()) {
            Point2D first = (Point2D)points.getFirst();
            ((Path2D)path).moveTo(first.getX(), first.getY());
            for (int i = 1; i < points.size(); ++i) {
                Point2D point = points.get(i);
                ((Path2D)path).lineTo(point.getX(), point.getY());
            }
        }
        return path;
    }

    private static Path2D createInterpolatedPath(BaseGraphicContext context, float[] points) {
        List<Point2D> pointList = PrGraphicUtil.transformPoints(context, points);
        boolean closed = ((Point2D)pointList.getFirst()).equals(pointList.getLast());
        return InterpolatedPath2D.buildCentripetal(pointList, closed, 1.0);
    }

    private static Path2D createMultiPointPath(BaseGraphicContext context, float[] points) {
        int size = points.length / 2;
        Path2D.Double path = new Path2D.Double(1, size);
        for (int i = 0; i < size; ++i) {
            Point2D point = context.transformPoint(points[i * 2], points[i * 2 + 1]);
            Ellipse2D.Double ellipse = new Ellipse2D.Double(point.getX() - 1.5, point.getY() - 1.5, 3.0, 3.0);
            path.append(ellipse, false);
        }
        return path;
    }

    private static Rectangle2D createRectangle(RectangleData data) {
        return new Rectangle2D.Double(Math.min(data.topLeft().getX(), data.bottomRight().getX()), Math.min(data.topLeft().getY(), data.bottomRight().getY()), Math.abs(data.bottomRight().getX() - data.topLeft().getX()), Math.abs(data.bottomRight().getY() - data.topLeft().getY()));
    }

    private static EllipseGeometry calculateEllipseGeometry(EllipseData data) {
        double cx = (data.major1().getX() + data.major2().getX()) / 2.0;
        double cy = (data.major1().getY() + data.major2().getY()) / 2.0;
        double rx = data.major1().distance(data.major2()) / 2.0;
        double ry = data.minor1().distance(data.minor2()) / 2.0;
        double rotation = PrGraphicUtil.calculateRotation(data.major1(), data.major2(), cx, cy);
        return new EllipseGeometry(cx, cy, rx, ry, rotation);
    }

    private static double calculateRotation(Point2D major1, Point2D major2, double cx, double cy) {
        if (MathUtil.isEqual((double)major1.getX(), (double)major2.getX())) {
            return 1.5707963267948966;
        }
        if (MathUtil.isEqual((double)major1.getY(), (double)major2.getY())) {
            return 0.0;
        }
        return Math.atan2(major2.getY() - cy, major2.getX() - cx);
    }

    public static boolean isDashedLine(Attributes style) {
        return style != null && "DASHED".equalsIgnoreCase(style.getString(7340628));
    }

    public static Color getPatternColor(Attributes style, Color defaultColor) {
        if (style == null) {
            return defaultColor;
        }
        try {
            int[] labValues = style.getInts(7340625);
            if (labValues != null && labValues.length >= 3) {
                int[] rgb = CIELab.dicomLab2rgb((int[])labValues);
                Color color = DicomObjectUtil.getRGBColor((int)65535, (int[])rgb);
                Float opacity = DicomUtils.getFloatFromDicomElement((Attributes)style, (int)7340676, null);
                if (opacity != null && opacity.floatValue() < 1.0f) {
                    int alpha = Math.round(opacity.floatValue() * 255.0f);
                    color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
                }
                return color;
            }
        }
        catch (Exception e) {
            LOGGER.warn("Failed to extract pattern color from style: {}", (Object)e.getMessage());
        }
        return defaultColor;
    }

    public static Color getFillPatternColor(Attributes fillStyle, Color defaultColor) {
        return fillStyle != null ? PrGraphicUtil.getPatternColor(fillStyle, defaultColor) : defaultColor;
    }

    public static GraphicModel getPresentationModel(Attributes attributes) {
        if (attributes == null) {
            return null;
        }
        String id = attributes.getString(0x71070070);
        if ("weasis/model/xml/2.5".equals(id)) {
            try {
                byte[] modelData = attributes.getBytes(0x71077001);
                if (modelData != null) {
                    return XmlSerializer.buildPresentationModel((byte[])modelData);
                }
            }
            catch (Exception e) {
                LOGGER.error("Cannot extract binary presentation model", (Throwable)e);
            }
        }
        return null;
    }

    public static void applyPresentationModel(ImageElement img) {
        if (img == null) {
            return;
        }
        try {
            GraphicModel model;
            byte[] prBinary = (byte[])TagW.getTagValue((TagReadable)img, (TagW)TagW.PresentationModelBirary, byte[].class);
            if (prBinary != null && (model = XmlSerializer.buildPresentationModel((byte[])prBinary)) != null) {
                img.setTag(TagW.PresentationModel, (Object)model);
                img.setTag(TagW.PresentationModelBirary, null);
            }
        }
        catch (Exception e) {
            LOGGER.error("Failed to apply presentation model", (Throwable)e);
        }
    }

    private static class GraphicContext
    extends BaseGraphicContext {
        private final boolean canBeEdited;
        private final boolean dcmSR;

        public GraphicContext(Attributes attributes, Color defaultColor, boolean labelVisible, double width, double height, boolean canBeEdited, AffineTransform inverse, boolean dcmSR) {
            super(attributes, defaultColor, labelVisible, width, height, inverse, 0x700005, 7340067);
            this.canBeEdited = canBeEdited;
            this.dcmSR = dcmSR;
        }

        @Override
        public boolean canBeEdited() {
            return this.canBeEdited;
        }

        @Override
        public boolean isDcmSR() {
            return this.dcmSR;
        }
    }

    private static abstract class BaseGraphicContext {
        protected final Attributes attributes;
        protected final Color defaultColor;
        protected final boolean labelVisible;
        protected final double width;
        protected final double height;
        protected final AffineTransform inverse;
        protected final boolean isDisplay;
        protected final String type;
        protected final Integer groupID;
        protected final boolean filled;
        protected final float thickness;
        protected final boolean dashed;
        protected final Color color;

        protected BaseGraphicContext(Attributes attributes, Color defaultColor, boolean labelVisible, double width, double height, AffineTransform inverse, int unitsTag, int typeTag) {
            this.attributes = attributes;
            this.defaultColor = defaultColor;
            this.labelVisible = labelVisible;
            this.width = width;
            this.height = height;
            this.inverse = inverse;
            this.isDisplay = "DISPLAY".equalsIgnoreCase(attributes.getString(unitsTag));
            this.type = attributes.getString(typeTag);
            this.groupID = DicomUtils.getIntegerFromDicomElement((Attributes)attributes, (int)7340693, null);
            this.filled = PresentationStateReader.getBooleanValue((Attributes)attributes, (int)7340068);
            Attributes style = attributes.getNestedDataset(7340594);
            this.thickness = DicomUtils.getFloatFromDicomElement((Attributes)style, (int)7340627, (Float)Float.valueOf(1.0f)).floatValue();
            this.dashed = PrGraphicUtil.isDashedLine(style);
            Color styleColor = PrGraphicUtil.getPatternColor(style, defaultColor);
            Attributes fillStyle = attributes.getNestedDataset(7340595);
            this.color = PrGraphicUtil.getFillPatternColor(fillStyle, styleColor);
        }

        public String getType() {
            return this.type;
        }

        public boolean isFilled() {
            return this.filled;
        }

        public boolean isDcmSR() {
            return false;
        }

        public boolean canBeEdited() {
            return false;
        }

        public float[] getPoints() {
            return DicomUtils.getFloatArrayFromDicomElement((Attributes)this.attributes, (int)0x700022, null);
        }

        public Point2D transformPoint(float x, float y) {
            if (this.isDisplay) {
                x *= (float)this.width;
                y *= (float)this.height;
            }
            if (this.isDisplay && this.inverse != null) {
                float[] src = new float[]{x, y};
                float[] dst = new float[2];
                this.inverse.transform(src, 0, dst, 0, 1);
                return new Point2D.Double(dst[0], dst[1]);
            }
            return new Point2D.Double(x, y);
        }

        public void applyProperties(Graphic graphic, boolean filled) {
            if (graphic != null) {
                graphic.setLineThickness(Float.valueOf(this.thickness));
                graphic.setPaint(this.color);
                graphic.setLabelVisible(Boolean.valueOf(this.labelVisible));
                graphic.setClassID(this.groupID);
                graphic.setFilled(Boolean.valueOf(filled));
            }
        }
    }

    private static class CompoundGraphicContext
    extends BaseGraphicContext {
        public CompoundGraphicContext(Attributes attributes, Color defaultColor, boolean labelVisible, double width, double height, AffineTransform inverse) {
            super(attributes, defaultColor, labelVisible, width, height, inverse, 7340674, 7340692);
        }
    }

    @FunctionalInterface
    private static interface GraphicBuilder {
        public Graphic build(BaseGraphicContext var1) throws InvalidShapeException;
    }

    private record PointData(Point2D point, float[] rawPoints) {
        public boolean equals(Object o) {
            if (o == null || ((Object)((Object)this)).getClass() != o.getClass()) {
                return false;
            }
            PointData pointData = (PointData)((Object)o);
            return Objects.equals(this.point(), pointData.point()) && Objects.deepEquals(this.rawPoints(), pointData.rawPoints());
        }

        public int hashCode() {
            return Objects.hash(this.point(), Arrays.hashCode(this.rawPoints()));
        }

        public String toString() {
            return "PointData{point=" + String.valueOf(this.point()) + ", rawPoints=" + Arrays.toString(this.rawPoints()) + "}";
        }
    }

    private record PolylineData(List<Point2D> points, float[] rawPoints) {
        public boolean isClosed(boolean isDcmSR) {
            if (isDcmSR && !((Point2D)this.points.getFirst()).equals(this.points.getLast())) {
                this.points.add(new Point2D.Double(((Point2D)this.points.getFirst()).getX(), ((Point2D)this.points.getFirst()).getY()));
            }
            return ((Point2D)this.points.getFirst()).equals(this.points.getLast());
        }

        public boolean equals(Object o) {
            if (o == null || ((Object)((Object)this)).getClass() != o.getClass()) {
                return false;
            }
            PolylineData that = (PolylineData)((Object)o);
            return Objects.deepEquals(this.rawPoints(), that.rawPoints()) && Objects.equals(this.points(), that.points());
        }

        public int hashCode() {
            return Objects.hash(this.points(), Arrays.hashCode(this.rawPoints()));
        }

        public String toString() {
            return "PolylineData{points=" + String.valueOf(this.points()) + ", rawPoints=" + Arrays.toString(this.rawPoints()) + "}";
        }
    }

    private record LineData(Point2D start, Point2D end, float[] rawPoints) {
        public boolean equals(Object o) {
            if (o == null || ((Object)((Object)this)).getClass() != o.getClass()) {
                return false;
            }
            LineData lineData = (LineData)((Object)o);
            return Objects.equals(this.end(), lineData.end()) && Objects.equals(this.start(), lineData.start()) && Objects.deepEquals(this.rawPoints(), lineData.rawPoints());
        }

        public int hashCode() {
            return Objects.hash(this.start(), this.end(), Arrays.hashCode(this.rawPoints()));
        }

        public String toString() {
            return "LineData{start=" + String.valueOf(this.start()) + ", end=" + String.valueOf(this.end()) + ", rawPoints=" + Arrays.toString(this.rawPoints()) + "}";
        }
    }

    private record EllipseData(Point2D major1, Point2D major2, Point2D minor1, Point2D minor2, float[] rawPoints) {
        public boolean equals(Object o) {
            if (o == null || ((Object)((Object)this)).getClass() != o.getClass()) {
                return false;
            }
            EllipseData that = (EllipseData)((Object)o);
            return Objects.equals(this.major1(), that.major1()) && Objects.equals(this.major2(), that.major2()) && Objects.equals(this.minor1(), that.minor1()) && Objects.equals(this.minor2(), that.minor2()) && Objects.deepEquals(this.rawPoints(), that.rawPoints());
        }

        public int hashCode() {
            return Objects.hash(this.major1(), this.major2(), this.minor1(), this.minor2(), Arrays.hashCode(this.rawPoints()));
        }

        public String toString() {
            return "EllipseData{major1=" + String.valueOf(this.major1()) + ", major2=" + String.valueOf(this.major2()) + ", minor1=" + String.valueOf(this.minor1()) + ", minor2=" + String.valueOf(this.minor2()) + ", rawPoints=" + Arrays.toString(this.rawPoints()) + "}";
        }
    }

    private record EllipseGeometry(double cx, double cy, double rx, double ry, double rotation) {
    }

    private record RectangleData(Point2D topLeft, Point2D bottomRight, float[] rawPoints) {
        public boolean equals(Object o) {
            if (o == null || ((Object)((Object)this)).getClass() != o.getClass()) {
                return false;
            }
            RectangleData that = (RectangleData)((Object)o);
            return Objects.equals(this.topLeft(), that.topLeft()) && Objects.deepEquals(this.rawPoints(), that.rawPoints()) && Objects.equals(this.bottomRight(), that.bottomRight());
        }

        public int hashCode() {
            return Objects.hash(this.topLeft(), this.bottomRight(), Arrays.hashCode(this.rawPoints()));
        }

        public String toString() {
            return "RectangleData{topLeft=" + String.valueOf(this.topLeft()) + ", bottomRight=" + String.valueOf(this.bottomRight()) + ", rawPoints=" + Arrays.toString(this.rawPoints()) + "}";
        }
    }
}

