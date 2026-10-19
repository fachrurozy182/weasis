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
import java.awt.Point;
import java.awt.Robot;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.GeomUtil;
import org.weasis.core.api.image.measure.MeasurementsAdapter;
import org.weasis.core.api.image.util.MeasurableLayer;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.model.graphic.AbstractDragGraphicArea;
import org.weasis.core.ui.model.graphic.imp.area.EllipseGraphic;
import org.weasis.core.ui.model.graphic.imp.area.RectangleGraphic;
import org.weasis.core.ui.model.utils.bean.MeasureItem;
import org.weasis.core.ui.model.utils.bean.Measurement;
import org.weasis.core.ui.model.utils.exceptions.InvalidShapeException;
import org.weasis.core.ui.util.MouseEventDouble;
import org.weasis.core.util.MathUtil;

@XmlType(name="rectangle")
@XmlRootElement(name="rectangle")
public class ObliqueRectangleGraphic
extends AbstractDragGraphicArea {
    public static final Integer POINTS_NUMBER = 4;
    public static final Icon ICON = ResourceUtil.getIcon(ResourceUtil.ActionIcon.DRAW_RECTANGLE);
    public static final Measurement AREA = new Measurement(Messages.getString("measure.area"), 1, true, true, true);
    public static final Measurement PERIMETER = new Measurement(Messages.getString("measure.perimeter"), 2, true, true, false);
    public static final Measurement CENTER_X = new Measurement(Messages.getString("measure.centerx"), 5, true, true, false);
    public static final Measurement CENTER_Y = new Measurement(Messages.getString("measure.centery"), 6, true, true, false);
    public static final Measurement WIDTH = new Measurement(Messages.getString("measure.width"), 7, true, true, false);
    public static final Measurement HEIGHT = new Measurement(Messages.getString("measure.height"), 8, true, true, false);
    public static final Measurement ORIENTATION = new Measurement(Messages.getString("measure.orientation"), 9, true, true, false);
    protected static final List<Measurement> MEASUREMENT_LIST = new ArrayList<Measurement>();
    protected Point2D ptA;
    protected Point2D ptB;
    protected Point2D ptC;
    protected Point2D ptD;
    protected boolean lineABvalid;
    protected boolean lineCDvalid;

    public ObliqueRectangleGraphic() {
        super(POINTS_NUMBER);
    }

    public ObliqueRectangleGraphic(ObliqueRectangleGraphic graphic) {
        super(graphic);
    }

    @Override
    public ObliqueRectangleGraphic copy() {
        return new ObliqueRectangleGraphic(this);
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getUIName() {
        return Messages.getString("MeasureToolBar.rect");
    }

    public ObliqueRectangleGraphic buildGraphic(Rectangle2D rectangle) throws InvalidShapeException {
        Rectangle2D r = Optional.ofNullable(rectangle).orElseThrow(() -> new InvalidShapeException("Rectangle2D is null!"));
        this.setHandlePointList(r);
        this.prepareShape();
        return this;
    }

    @Override
    protected void prepareShape() throws InvalidShapeException {
        if (!this.isShapeValid()) {
            throw new InvalidShapeException("This shape cannot be drawn");
        }
        this.buildShape(null);
    }

    @Override
    public Integer moveAndResizeOnDrawing(Integer handlePointIndex, Double deltaX, Double deltaY, MouseEventDouble mouseEvent) {
        List<Point2D> prevHandlePointList = this.getHandlePointList();
        if ((handlePointIndex = super.moveAndResizeOnDrawing(handlePointIndex, deltaX, deltaY, mouseEvent)) >= 0 && handlePointIndex < this.getHandlePointListSize()) {
            this.updateTool();
            if (handlePointIndex == 0 || handlePointIndex == 1) {
                Point2D prevPtB;
                Point2D prevPtA = !prevHandlePointList.isEmpty() ? prevHandlePointList.get(0) : null;
                Point2D point2D = prevPtB = prevHandlePointList.size() > 1 ? prevHandlePointList.get(1) : null;
                if (this.lineABvalid && GeomUtil.isLineValid(prevPtA, prevPtB) && this.ptC != null && this.ptD != null) {
                    double dist = this.ptC.distance(this.ptD);
                    this.ptC = GeomUtil.getMidPoint(this.ptA, this.ptB);
                    this.ptD = GeomUtil.getPerpendicularPointFromLine(this.ptA, this.ptB, this.ptC, dist);
                    this.setHandlePoint(2, this.ptC);
                    this.setHandlePoint(3, this.ptD);
                }
            } else if (handlePointIndex == 2) {
                if (this.lineABvalid && this.ptC != null) {
                    double abDist = this.ptA.distance(this.ptB);
                    if (this.ptD == null) {
                        if (this instanceof EllipseGraphic) {
                            Point2D newPtA = GeomUtil.getPerpendicularPointFromLine(this.ptA, this.ptB, this.ptA, -abDist / 6.0);
                            Point2D newPtB = GeomUtil.getPerpendicularPointFromLine(this.ptA, this.ptB, this.ptB, -abDist / 6.0);
                            this.ptA = newPtA;
                            this.ptB = newPtB;
                            this.setHandlePoint(0, this.ptA);
                            this.setHandlePoint(1, this.ptB);
                        }
                        this.ptC = GeomUtil.getMidPoint(this.ptA, this.ptB);
                        this.ptD = GeomUtil.getPerpendicularPointFromLine(this.ptA, this.ptB, this.ptC, abDist / 3.0);
                        this.setHandlePoint(2, this.ptC);
                        this.setHandlePoint(3, this.ptD);
                        ViewCanvas graphPane = this.getDefaultView2d(mouseEvent);
                        if (graphPane != null) {
                            Point mousePt = graphPane.getMouseCoordinatesFromImage(this.ptD.getX(), this.ptD.getY());
                            try {
                                mouseEvent.translatePoint(mousePt.x - mouseEvent.getX(), mousePt.y - mouseEvent.getY());
                                mouseEvent.setImageCoordinates(this.ptD);
                                SwingUtilities.convertPointToScreen(mousePt, graphPane.getJComponent());
                                new Robot().mouseMove(mousePt.x, mousePt.y);
                                return 3;
                            }
                            catch (Exception exception) {}
                        }
                    } else {
                        Point2D ptNext = GeomUtil.getPerpendicularPointToLine(this.ptA, this.ptB, this.ptC);
                        double dist = this.ptC.distance(ptNext);
                        if (this.ptA.getX() > this.ptB.getX()) {
                            dist *= -1.0;
                        }
                        if (ptNext.getY() > this.ptC.getY()) {
                            dist *= -1.0;
                        }
                        Point2D prevPtC = prevHandlePointList.size() > 2 ? prevHandlePointList.get(2) : null;
                        double dist2 = prevPtC == null ? abDist / 3.0 : prevPtC.distance(this.ptD);
                        Line2D ab = GeomUtil.getParallelLine(this.ptA, this.ptB, dist);
                        this.ptA = ab.getP1();
                        this.ptB = ab.getP2();
                        this.setHandlePoint(0, this.ptA);
                        this.setHandlePoint(1, this.ptB);
                        this.ptC = GeomUtil.getMidPoint(this.ptA, this.ptB);
                        this.setHandlePoint(2, this.ptC);
                        this.ptD = GeomUtil.getPerpendicularPointFromLine(this.ptA, this.ptB, this.ptC, dist2);
                        this.setHandlePoint(3, this.ptD);
                    }
                }
            } else if (handlePointIndex == 3 && this.lineABvalid && this.ptC != null && this.ptD != null) {
                Point2D ptNext = GeomUtil.getPerpendicularPointToLine(this.ptA, this.ptB, this.ptD);
                double dist = this.ptD.distance(ptNext);
                this.ptD = GeomUtil.getPerpendicularPointFromLine(this.ptA, this.ptB, this.ptC, dist);
                this.setHandlePoint(3, this.ptD);
            }
        }
        return handlePointIndex;
    }

    @Override
    public void buildShape(MouseEventDouble mouseEvent) {
        this.updateTool();
        Path2D.Double polygonPath = new Path2D.Double(1, this.pts.size());
        if (this.lineABvalid) {
            ((Path2D)polygonPath).moveTo(this.ptA.getX(), this.ptA.getY());
            ((Path2D)polygonPath).lineTo(this.ptB.getX(), this.ptB.getY());
            if (this.lineCDvalid) {
                double dist = this.ptC.distance(this.ptD);
                Line2D cd = GeomUtil.getParallelLine(this.ptA, this.ptB, dist);
                ((Path2D)polygonPath).lineTo(cd.getP2().getX(), cd.getP2().getY());
                ((Path2D)polygonPath).lineTo(cd.getP1().getX(), cd.getP1().getY());
            }
            polygonPath.closePath();
        }
        this.setShape(polygonPath, mouseEvent);
        this.updateLabel(mouseEvent, this.getDefaultView2d(mouseEvent));
    }

    @Override
    public List<Measurement> getMeasurementList() {
        return MEASUREMENT_LIST;
    }

    @Override
    public List<MeasureItem> computeMeasurements(MeasurableLayer layer, boolean releaseEvent, Unit displayUnit) {
        MeasurementsAdapter adapter;
        if (layer != null && layer.hasContent() && this.isShapeValid() && (adapter = layer.getMeasurementAdapter(displayUnit)) != null) {
            List<MeasureItem> stats;
            Double val;
            double ab = this.ptA.distance(this.ptB);
            double cd = this.ptC.distance(this.ptD);
            Point2D center = GeomUtil.getMidPoint(this.ptC, this.ptD);
            ArrayList<MeasureItem> measVal = new ArrayList<MeasureItem>();
            double ratio = adapter.calibrationRatio();
            String unitStr = adapter.unit();
            if (CENTER_X.getComputed().booleanValue()) {
                measVal.add(new MeasureItem(CENTER_X, adapter.getXCalibratedValue(center.getX()), unitStr));
            }
            if (CENTER_Y.getComputed().booleanValue()) {
                measVal.add(new MeasureItem(CENTER_Y, adapter.getYCalibratedValue(center.getY()), unitStr));
            }
            if (WIDTH.getComputed().booleanValue()) {
                measVal.add(new MeasureItem(WIDTH, ab * ratio, unitStr));
            }
            if (HEIGHT.getComputed().booleanValue()) {
                measVal.add(new MeasureItem(HEIGHT, cd * ratio, unitStr));
            }
            if (ORIENTATION.getComputed().booleanValue()) {
                measVal.add(new MeasureItem(ORIENTATION, MathUtil.getOrientation((Point2D)this.ptA, (Point2D)this.ptB), Messages.getString("measure.deg")));
            }
            if (AREA.getComputed().booleanValue()) {
                val = ab * cd * ratio * ratio;
                Object unit = "pix".equals(unitStr) ? unitStr : unitStr + "2";
                measVal.add(new MeasureItem(AREA, val, (String)unit));
            }
            if (PERIMETER.getComputed().booleanValue()) {
                val = (ab + cd) * 2.0 * ratio;
                measVal.add(new MeasureItem(PERIMETER, val, unitStr));
            }
            if ((stats = this.getImageStatistics(layer, releaseEvent)) != null) {
                measVal.addAll(stats);
            }
            return measVal;
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isShapeValid() {
        this.updateTool();
        return this.lineABvalid && this.lineCDvalid;
    }

    protected void updateTool() {
        if (this.pts.size() >= 8 && !this.getHandlePoint(RectangleGraphic.eHandlePoint.NW.index).equals(this.getHandlePoint(RectangleGraphic.eHandlePoint.SE.index))) {
            Rectangle2D.Double rectangle = new Rectangle2D.Double();
            rectangle.setFrameFromDiagonal(this.getHandlePoint(RectangleGraphic.eHandlePoint.NW.index), this.getHandlePoint(RectangleGraphic.eHandlePoint.SE.index));
            this.setHandlePointList(rectangle);
        }
        this.ptA = this.getHandlePoint(0);
        this.ptB = this.getHandlePoint(1);
        this.ptC = this.getHandlePoint(2);
        this.ptD = this.getHandlePoint(3);
        this.lineABvalid = this.ptA != null && this.ptB != null && !this.ptB.equals(this.ptA);
        this.lineCDvalid = this.ptC != null && this.ptD != null && !this.ptC.equals(this.ptD);
    }

    protected void setHandlePointList(Rectangle2D rectangle) {
        double x = rectangle.getX();
        double y = rectangle.getY();
        double w = rectangle.getWidth();
        double h = rectangle.getHeight();
        while (this.pts.size() < this.pointNumber) {
            this.pts.add(new Point2D.Double());
        }
        while (this.pts.size() > this.pointNumber) {
            this.pts.remove(this.pointNumber);
        }
        this.setHandlePoint(0, new Point2D.Double(x, y));
        this.setHandlePoint(1, new Point2D.Double(x + w, y));
        this.setHandlePoint(2, new Point2D.Double(x + w / 2.0, y));
        this.setHandlePoint(3, new Point2D.Double(x + w / 2.0, y + h));
    }

    public List<Point2D> getRectanglePointList() {
        this.updateTool();
        ArrayList<Point2D> pts = new ArrayList<Point2D>();
        if (this.lineABvalid && this.lineCDvalid) {
            Point2D.Double a = new Point2D.Double(this.ptA.getX(), this.ptA.getY());
            Point2D.Double b = new Point2D.Double(this.ptB.getX(), this.ptB.getY());
            Line2D cd = GeomUtil.getParallelLine(a, b, this.ptC.distance(this.ptD));
            if (((Point2D)a).getX() > ((Point2D)b).getX()) {
                pts.add(cd.getP2());
                pts.add(cd.getP1());
                pts.add(a);
                pts.add(b);
            } else {
                pts.add(a);
                pts.add(b);
                pts.add(cd.getP2());
                pts.add(cd.getP1());
            }
        }
        return pts;
    }

    static {
        MEASUREMENT_LIST.add(CENTER_X);
        MEASUREMENT_LIST.add(CENTER_Y);
        MEASUREMENT_LIST.add(WIDTH);
        MEASUREMENT_LIST.add(HEIGHT);
        MEASUREMENT_LIST.add(ORIENTATION);
        MEASUREMENT_LIST.add(AREA);
        MEASUREMENT_LIST.add(PERIMETER);
    }
}

