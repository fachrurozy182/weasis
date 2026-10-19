/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.xml.bind.annotation.XmlRootElement
 *  jakarta.xml.bind.annotation.XmlType
 *  org.weasis.core.util.MathUtil
 */
package org.weasis.core.ui.model.graphic.imp.area;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.swing.Icon;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.ShortcutManager;
import org.weasis.core.api.image.measure.MeasurementsAdapter;
import org.weasis.core.api.image.util.MeasurableLayer;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.model.graphic.AbstractDragGraphicArea;
import org.weasis.core.ui.model.utils.algo.MinimumEnclosingRectangle;
import org.weasis.core.ui.model.utils.bean.MeasureItem;
import org.weasis.core.ui.model.utils.bean.Measurement;
import org.weasis.core.ui.model.utils.exceptions.InvalidShapeException;
import org.weasis.core.ui.util.MouseEventDouble;
import org.weasis.core.util.MathUtil;

@XmlType(name="polygon")
@XmlRootElement(name="polygon")
public class PolygonGraphic
extends AbstractDragGraphicArea {
    public static final Integer POINTS_NUMBER = UNDEFINED;
    public static final Icon ICON = ResourceUtil.getIcon(ResourceUtil.ActionIcon.DRAW_POLYGON);
    public static final Measurement AREA = new Measurement(Messages.getString("measure.area"), 1, true, true, true);
    public static final Measurement PERIMETER = new Measurement(Messages.getString("measure.perimeter"), 2, true, true, false);
    public static final Measurement WIDTH = new Measurement(Messages.getString("measure.width"), 3, true, true, false);
    public static final Measurement HEIGHT = new Measurement(Messages.getString("measure.height"), 4, true, true, false);
    public static final Measurement TOP_LEFT_POINT_X = new Measurement(Messages.getString("measure.topx"), 5, true, true, false);
    public static final Measurement TOP_LEFT_POINT_Y = new Measurement(Messages.getString("measure.topy"), 6, true, true, false);
    public static final Measurement CENTROID_X = new Measurement(Messages.getString("measure.centerx"), 7, true, true, false);
    public static final Measurement CENTROID_Y = new Measurement(Messages.getString("measure.centery"), 8, true, true, false);
    public static final Measurement WIDTH_OMBB = new Measurement(Messages.getString("measure.width") + " (OMBB)", 9, false, true, false);
    public static final Measurement LENGTH_OMBB = new Measurement(Messages.getString("measure.length") + " (OMBB)", 10, false, true, false);
    public static final Measurement ORIENTATION_OMBB = new Measurement(Messages.getString("measure.orientation") + " (OMBB)", 10, false, true, false);
    protected static final List<Measurement> MEASUREMENT_LIST = new ArrayList<Measurement>();

    public PolygonGraphic() {
        super(POINTS_NUMBER);
    }

    public PolygonGraphic(PolygonGraphic graphic) {
        super(graphic);
    }

    @Override
    public PolygonGraphic copy() {
        return new PolygonGraphic(this);
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getUIName() {
        return Messages.getString("MeasureToolBar.polygon");
    }

    @Override
    public int getKeyCode() {
        return ShortcutManager.getInstance().getKeyCode("graphic.polygon");
    }

    @Override
    public int getModifier() {
        return ShortcutManager.getInstance().getModifier("graphic.polygon");
    }

    @Override
    protected void prepareShape() throws InvalidShapeException {
        this.setPointNumber(this.pts.size());
        this.buildShape(null);
        if (!this.isShapeValid()) {
            int lastPointIndex = this.pts.size() - 1;
            if (lastPointIndex > 1) {
                Point2D checkPoint = (Point2D)this.pts.get(lastPointIndex);
                for (int i = lastPointIndex - 1; i >= 0 && Objects.equals(checkPoint, this.pts.get(i)); --i) {
                    this.pts.remove(i);
                }
                if (Objects.equals(checkPoint, this.pts.get(0))) {
                    this.pts.remove(0);
                }
                this.setPointNumber(this.pts.size());
            }
            if (!this.isShapeValid() || this.pts.size() < 3) {
                throw new InvalidShapeException("This Polygon cannot be drawn");
            }
            this.buildShape(null);
        }
    }

    @Override
    public void buildShape(MouseEventDouble mouseEvent) {
        Path2D.Double newShape = null;
        Optional firstHandlePoint = this.pts.stream().findFirst();
        if (firstHandlePoint.isPresent()) {
            Point2D pt;
            Point2D p = (Point2D)firstHandlePoint.get();
            Path2D.Double polygonPath = new Path2D.Double(1, this.pts.size());
            ((Path2D)polygonPath).moveTo(p.getX(), p.getY());
            Iterator iterator = this.pts.iterator();
            while (iterator.hasNext() && (pt = (Point2D)iterator.next()) != null) {
                ((Path2D)polygonPath).lineTo(pt.getX(), pt.getY());
            }
            polygonPath.closePath();
            newShape = polygonPath;
        }
        this.setShape(newShape, mouseEvent);
        this.updateLabel(mouseEvent, this.getDefaultView2d(mouseEvent));
    }

    @Override
    public boolean isShapeValid() {
        if (!this.isGraphicComplete().booleanValue()) {
            return false;
        }
        int lastPointIndex = this.pts.size() - 1;
        if (lastPointIndex > 0) {
            Point2D checkPoint = (Point2D)this.pts.get(lastPointIndex);
            return !Objects.equals(checkPoint, this.pts.get(--lastPointIndex));
        }
        return true;
    }

    @Override
    public List<MeasureItem> computeMeasurements(MeasurableLayer layer, boolean releaseEvent, Unit displayUnit) {
        MeasurementsAdapter adapter;
        if (layer != null && layer.hasContent() && this.isShapeValid() && (adapter = layer.getMeasurementAdapter(displayUnit)) != null) {
            List<MeasureItem> stats;
            Double val;
            Double val2;
            ArrayList<MeasureItem> measVal = new ArrayList<MeasureItem>(12);
            double ratio = adapter.calibrationRatio();
            String unitStr = adapter.unit();
            Area pathArea = this.getPathArea();
            List<Line2D.Double> lineSegmentList = null;
            if (TOP_LEFT_POINT_X.getComputed().booleanValue()) {
                val2 = Optional.ofNullable(pathArea).map(pa -> adapter.getXCalibratedValue(pa.getBounds2D().getX())).orElse(null);
                measVal.add(new MeasureItem(TOP_LEFT_POINT_X, val2, unitStr));
            }
            if (TOP_LEFT_POINT_Y.getComputed().booleanValue()) {
                val2 = Optional.ofNullable(pathArea).map(pa -> adapter.getYCalibratedValue(pa.getBounds2D().getY())).orElse(null);
                measVal.add(new MeasureItem(TOP_LEFT_POINT_Y, val2, unitStr));
            }
            if (WIDTH.getComputed().booleanValue()) {
                val2 = Optional.ofNullable(pathArea).map(pa -> ratio * pa.getBounds2D().getWidth()).orElse(null);
                measVal.add(new MeasureItem(WIDTH, val2, unitStr));
            }
            if (HEIGHT.getComputed().booleanValue()) {
                val2 = Optional.ofNullable(pathArea).map(pa -> ratio * pa.getBounds2D().getHeight()).orElse(null);
                measVal.add(new MeasureItem(HEIGHT, val2, unitStr));
            }
            Point2D centroid = null;
            if (CENTROID_X.getComputed().booleanValue()) {
                if (lineSegmentList == null) {
                    lineSegmentList = this.getClosedPathSegments(pathArea);
                }
                centroid = centroid == null ? this.getCentroid(lineSegmentList) : centroid;
                val = centroid != null ? Double.valueOf(adapter.getXCalibratedValue(centroid.getX())) : null;
                measVal.add(new MeasureItem(CENTROID_X, val, unitStr));
            }
            if (CENTROID_Y.getComputed().booleanValue()) {
                if (lineSegmentList == null) {
                    lineSegmentList = this.getClosedPathSegments(pathArea);
                }
                centroid = centroid == null ? this.getCentroid(lineSegmentList) : centroid;
                val = centroid != null ? Double.valueOf(adapter.getYCalibratedValue(centroid.getY())) : null;
                measVal.add(new MeasureItem(CENTROID_Y, val, unitStr));
            }
            if (AREA.getComputed().booleanValue()) {
                if (lineSegmentList == null) {
                    lineSegmentList = this.getClosedPathSegments(pathArea);
                }
                val = lineSegmentList != null ? Double.valueOf(this.getAreaValue(lineSegmentList) * ratio * ratio) : null;
                Object unit = "pix".equals(unitStr) ? unitStr : unitStr + "2";
                measVal.add(new MeasureItem(AREA, val, (String)unit));
            }
            if (PERIMETER.getComputed().booleanValue()) {
                if (lineSegmentList == null) {
                    lineSegmentList = this.getClosedPathSegments(pathArea);
                }
                val = lineSegmentList != null ? Double.valueOf(this.getPerimeter(lineSegmentList) * ratio) : null;
                measVal.add(new MeasureItem(PERIMETER, val, unitStr));
            }
            if (releaseEvent && (WIDTH_OMBB.getComputed().booleanValue() || LENGTH_OMBB.getComputed().booleanValue())) {
                Double l = null;
                Double w = null;
                Double o = null;
                MinimumEnclosingRectangle rect = new MinimumEnclosingRectangle(this.pts, false);
                List<Point2D> minRect = rect.getMinimumRectangle();
                if (minRect.size() == 4) {
                    l = ratio * minRect.get(0).distance(minRect.get(1));
                    w = ratio * minRect.get(1).distance(minRect.get(2));
                    o = MathUtil.getOrientation((Point2D)minRect.get(0), (Point2D)minRect.get(1));
                    if (l < w) {
                        double tmp = l;
                        l = w;
                        w = tmp;
                        o = MathUtil.getOrientation((Point2D)minRect.get(1), (Point2D)minRect.get(2));
                    }
                }
                measVal.add(new MeasureItem(LENGTH_OMBB, l, unitStr));
                measVal.add(new MeasureItem(WIDTH_OMBB, w, unitStr));
                measVal.add(new MeasureItem(ORIENTATION_OMBB, o, Messages.getString("measure.deg")));
            }
            if ((stats = this.getImageStatistics(layer, releaseEvent)) != null) {
                measVal.addAll(stats);
            }
            return measVal;
        }
        return Collections.emptyList();
    }

    @Override
    public List<Measurement> getMeasurementList() {
        return MEASUREMENT_LIST;
    }

    protected final Area getPathArea() {
        return PolygonGraphic.getPathArea(this.pts);
    }

    public static Area getPathArea(List<Point2D> pts) {
        Optional firstHandlePoint = pts.stream().findFirst();
        if (firstHandlePoint.isPresent()) {
            Point2D p = (Point2D)firstHandlePoint.get();
            Path2D.Double polygonPath = new Path2D.Double(1, pts.size());
            ((Path2D)polygonPath).moveTo(p.getX(), p.getY());
            for (Point2D pt : pts) {
                if (pt == null) {
                    return null;
                }
                ((Path2D)polygonPath).lineTo(pt.getX(), pt.getY());
            }
            return new Area(polygonPath);
        }
        return null;
    }

    public final List<Line2D.Double> getClosedPathSegments() {
        return this.getClosedPathSegments(this.getPathArea());
    }

    protected final List<Line2D.Double> getClosedPathSegments(Area pathArea) {
        ArrayList<Line2D.Double> lineSegmentList = null;
        if (pathArea != null) {
            lineSegmentList = new ArrayList<Line2D.Double>(this.pts.size());
            PathIterator pathIt = pathArea.getPathIterator(null);
            double[] coords = new double[6];
            Double curX = Double.NaN;
            Double curY = Double.NaN;
            HashSet ptSet = HashSet.newHashSet((int)(this.pts.size() * 2));
            while (!pathIt.isDone()) {
                int segType = pathIt.currentSegment(coords);
                Double lastX = coords[0];
                Double lastY = coords[1];
                switch (segType) {
                    case 4: {
                        break;
                    }
                    case 1: {
                        Point2D.Double ptP1 = new Point2D.Double(curX, curY);
                        Point2D.Double ptP2 = new Point2D.Double(lastX, lastY);
                        BigDecimal dist = BigDecimal.valueOf(ptP1.distance(ptP2)).setScale(10, RoundingMode.DOWN);
                        if (dist.compareTo(BigDecimal.ZERO) != 0) {
                            for (Point2D pt : new Point2D[]{ptP1, ptP2}) {
                                boolean newPt = true;
                                for (Point2D p : ptSet) {
                                    dist = BigDecimal.valueOf(p.distance(pt)).setScale(10, RoundingMode.DOWN);
                                    if (dist.compareTo(BigDecimal.ZERO) != 0) continue;
                                    pt.setLocation(p);
                                    newPt = false;
                                    break;
                                }
                                if (!newPt) continue;
                                ptSet.add(pt);
                            }
                            lineSegmentList.add(new Line2D.Double(ptP1, ptP2));
                        }
                        curX = lastX;
                        curY = lastY;
                        break;
                    }
                    case 0: {
                        curX = lastX;
                        curY = lastY;
                        break;
                    }
                }
                pathIt.next();
            }
        }
        return lineSegmentList;
    }

    public Double getPerimeter() {
        return this.getPerimeter(this.getClosedPathSegments());
    }

    protected Double getPerimeter(List<Line2D.Double> lineSegmentList) {
        if (lineSegmentList != null) {
            double perimeter = 0.0;
            for (Line2D line2D : lineSegmentList) {
                perimeter += line2D.getP1().distance(line2D.getP2());
            }
            return perimeter;
        }
        return null;
    }

    public Point2D getCentroid() {
        return this.getCentroid(this.getClosedPathSegments());
    }

    protected Point2D getCentroid(List<Line2D.Double> lineSegmentList) {
        if (lineSegmentList != null) {
            double area = 0.0;
            double cx = 0.0;
            double cy = 0.0;
            for (Line2D.Double line : lineSegmentList) {
                Point2D p1 = line.getP1();
                Point2D p2 = line.getP2();
                double tmp = p1.getX() * p2.getY() - p2.getX() * p1.getY();
                area += tmp;
                cx += (p1.getX() + p2.getX()) * tmp;
                cy += (p1.getY() + p2.getY()) * tmp;
            }
            if ((area /= 2.0) == 0.0 || MathUtil.isEqualToZero((double)area)) {
                return null;
            }
            return new Point2D.Double(cx /= 6.0 * area, cy /= 6.0 * area);
        }
        return null;
    }

    public Double getAreaValue() {
        return this.getAreaValue(this.getClosedPathSegments());
    }

    protected Double getAreaValue(List<Line2D.Double> lineSegmentList) {
        if (lineSegmentList != null) {
            double area = 0.0;
            for (Line2D.Double line : lineSegmentList) {
                Point2D p1 = line.getP1();
                Point2D p2 = line.getP2();
                area += p1.getX() * p2.getY() - p2.getX() * p1.getY();
            }
            return Math.abs(area) / 2.0;
        }
        return null;
    }

    static {
        MEASUREMENT_LIST.add(TOP_LEFT_POINT_X);
        MEASUREMENT_LIST.add(TOP_LEFT_POINT_Y);
        MEASUREMENT_LIST.add(WIDTH);
        MEASUREMENT_LIST.add(HEIGHT);
        MEASUREMENT_LIST.add(CENTROID_X);
        MEASUREMENT_LIST.add(CENTROID_Y);
        MEASUREMENT_LIST.add(AREA);
        MEASUREMENT_LIST.add(PERIMETER);
    }
}

