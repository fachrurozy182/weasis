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
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Icon;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.GeomUtil;
import org.weasis.core.api.image.measure.MeasurementsAdapter;
import org.weasis.core.api.image.util.MeasurableLayer;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.model.graphic.imp.area.ObliqueRectangleGraphic;
import org.weasis.core.ui.model.utils.bean.AdvancedShape;
import org.weasis.core.ui.model.utils.bean.MeasureItem;
import org.weasis.core.ui.model.utils.bean.Measurement;
import org.weasis.core.ui.util.MouseEventDouble;
import org.weasis.core.util.MathUtil;

@XmlType(name="ellipse")
@XmlRootElement(name="ellipse")
public class EllipseGraphic
extends ObliqueRectangleGraphic {
    public static final Icon ICON = ResourceUtil.getIcon(ResourceUtil.ActionIcon.DRAW_ELLIPSE);
    public static final Measurement AREA = new Measurement(Messages.getString("measure.area"), 1, true, true, true);
    public static final Measurement PERIMETER = new Measurement(Messages.getString("measure.perimeter"), 2, true, true, false);
    public static final Measurement CENTER_X = new Measurement(Messages.getString("measure.centerx"), 3, true, true, false);
    public static final Measurement CENTER_Y = new Measurement(Messages.getString("measure.centery"), 4, true, true, false);
    public static final Measurement WIDTH = new Measurement(Messages.getString("measure.width"), 5, true, true, false);
    public static final Measurement HEIGHT = new Measurement(Messages.getString("measure.height"), 6, true, true, false);
    public static final Measurement ORIENTATION = new Measurement(Messages.getString("measure.orientation"), 7, true, true, false);
    protected static final List<Measurement> MEASUREMENT_LIST = new ArrayList<Measurement>();

    public EllipseGraphic() {
    }

    public EllipseGraphic(EllipseGraphic graphic) {
        super(graphic);
    }

    @Override
    public EllipseGraphic copy() {
        return new EllipseGraphic(this);
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getUIName() {
        return Messages.getString("MeasureToolBar.ellipse");
    }

    @Override
    public void buildShape(MouseEventDouble mouseEvent) {
        this.updateTool();
        Shape newShape = null;
        Path2D.Double polygonPath = new Path2D.Double(1, this.pts.size());
        if (this.lineABvalid) {
            newShape = new AdvancedShape(this, 2);
            AdvancedShape aShape = (AdvancedShape)newShape;
            aShape.addShape(polygonPath);
            aShape.addShape(new Line2D.Double(this.ptA, this.ptB), this.getDashStroke(Float.valueOf(1.0f)), true);
            ((Path2D)polygonPath).moveTo(this.ptA.getX(), this.ptA.getY());
            if (this.lineCDvalid) {
                double dist = this.ptC.distance(this.ptD);
                double a = this.ptA.distance(this.ptB) / 2.0;
                double b = dist / 2.0;
                Point2D ptx = GeomUtil.getPerpendicularPointFromLine(this.ptA, this.ptB, this.ptC, b);
                double widthTwoThirds = a * 4.0 / 3.0;
                double rotationAngle = Math.atan2(this.ptA.getY() - this.ptB.getY(), this.ptA.getX() - this.ptB.getX());
                double dx1 = Math.sin(rotationAngle) * b;
                double dy1 = Math.cos(rotationAngle) * b;
                double dx2 = Math.cos(rotationAngle) * widthTwoThirds;
                double dy2 = Math.sin(rotationAngle) * widthTwoThirds;
                double topCenterX = ptx.getX() - dx1;
                double topCenterY = ptx.getY() + dy1;
                double topRightX = topCenterX + dx2;
                double topRightY = topCenterY + dy2;
                double topLeftX = topCenterX - dx2;
                double topLeftY = topCenterY - dy2;
                double bottomCenterX = ptx.getX() + dx1;
                double bottomCenterY = ptx.getY() - dy1;
                double bottomRightX = bottomCenterX + dx2;
                double bottomRightY = bottomCenterY + dy2;
                double bottomLeftX = bottomCenterX - dx2;
                double bottomLeftY = bottomCenterY - dy2;
                ((Path2D)polygonPath).moveTo(bottomCenterX, bottomCenterY);
                ((Path2D)polygonPath).curveTo(bottomRightX, bottomRightY, topRightX, topRightY, topCenterX, topCenterY);
                ((Path2D)polygonPath).curveTo(topLeftX, topLeftY, bottomLeftX, bottomLeftY, bottomCenterX, bottomCenterY);
            } else {
                ((Path2D)polygonPath).lineTo(this.ptB.getX(), this.ptB.getY());
            }
            polygonPath.closePath();
        } else if (polygonPath.getCurrentPoint() != null) {
            newShape = polygonPath;
        }
        this.setShape(newShape, mouseEvent);
        this.updateLabel(mouseEvent, this.getDefaultView2d(mouseEvent));
    }

    @Override
    public List<MeasureItem> computeMeasurements(MeasurableLayer layer, boolean releaseEvent, Unit displayUnit) {
        MeasurementsAdapter adapter;
        if (layer != null && layer.hasContent() && this.isShapeValid() && (adapter = layer.getMeasurementAdapter(displayUnit)) != null) {
            List<MeasureItem> stats;
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
                Double val = Math.PI * ab * ratio * cd * ratio / 4.0;
                Object unit = "pix".equals(unitStr) ? unitStr : unitStr + "2";
                measVal.add(new MeasureItem(AREA, val, (String)unit));
            }
            if (PERIMETER.getComputed().booleanValue()) {
                double a = ratio * ab / 2.0;
                double b = ratio * cd / 2.0;
                Double val = Math.PI * 2 * Math.sqrt((a * a + b * b) / 2.0);
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
    public List<Measurement> getMeasurementList() {
        return MEASUREMENT_LIST;
    }

    @Override
    public List<Point2D> getRectanglePointList() {
        this.updateTool();
        ArrayList<Point2D> pts = new ArrayList<Point2D>();
        if (this.lineABvalid && this.lineCDvalid) {
            double abDistance = this.ptA.distance(this.ptB);
            double cdDistance = this.ptC.distance(this.ptD);
            Point2D.Double n = new Point2D.Double(this.ptC.getX(), this.ptC.getY());
            Point2D.Double s = new Point2D.Double(this.ptD.getX(), this.ptD.getY());
            Line2D we = GeomUtil.getParallelLine(this.ptA, this.ptB, cdDistance / 2.0);
            if (abDistance < cdDistance) {
                pts.add(n);
                pts.add(s);
                pts.add(we.getP1());
                pts.add(we.getP2());
            } else {
                pts.add(we.getP1());
                pts.add(we.getP2());
                pts.add(n);
                pts.add(s);
            }
        }
        return pts;
    }

    static {
        MEASUREMENT_LIST.add(CENTER_X);
        MEASUREMENT_LIST.add(CENTER_Y);
        MEASUREMENT_LIST.add(AREA);
        MEASUREMENT_LIST.add(PERIMETER);
        MEASUREMENT_LIST.add(ORIENTATION);
        MEASUREMENT_LIST.add(WIDTH);
        MEASUREMENT_LIST.add(HEIGHT);
    }
}

