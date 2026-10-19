/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.xml.bind.annotation.XmlRootElement
 *  jakarta.xml.bind.annotation.XmlType
 */
package org.weasis.core.ui.model.graphic.imp.angle;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.GeomUtil;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.model.graphic.imp.angle.OpenAngleToolGraphic;
import org.weasis.core.ui.model.utils.bean.AdvancedShape;
import org.weasis.core.ui.model.utils.bean.Measurement;
import org.weasis.core.ui.util.MouseEventDouble;

@XmlType(name="cobbAngle")
@XmlRootElement(name="cobbAngle")
public class CobbAngleToolGraphic
extends OpenAngleToolGraphic {
    public static final Integer POINTS_NUMBER = 5;
    public static final Icon ICON = ResourceUtil.getIcon(ResourceUtil.ActionIcon.DRAW_COBB);
    public static final Measurement ANGLE = new Measurement(Messages.getString("measure.angle"), 1, true);
    public static final Measurement COMPLEMENTARY_ANGLE = new Measurement(Messages.getString("measure.complement_angle"), 2, true, true, false);
    protected static final List<Measurement> MEASUREMENT_LIST = new ArrayList<Measurement>();
    protected Point2D ptO;

    public CobbAngleToolGraphic() {
        super(POINTS_NUMBER);
    }

    public CobbAngleToolGraphic(CobbAngleToolGraphic graphic) {
        super(graphic);
    }

    @Override
    public CobbAngleToolGraphic copy() {
        return new CobbAngleToolGraphic(this);
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getUIName() {
        return Messages.getString("measure.coobs");
    }

    @Override
    public List<Measurement> getMeasurementList() {
        return MEASUREMENT_LIST;
    }

    @Override
    public Integer moveAndResizeOnDrawing(Integer handlePointIndex, Double deltaX, Double deltaY, MouseEventDouble mouseEvent) {
        if ((handlePointIndex = super.moveAndResizeOnDrawing(handlePointIndex, deltaX, deltaY, mouseEvent)) != -1 && this.pts.size() >= 4) {
            this.updateTool();
            if (this.lineABvalid && this.lineCDvalid) {
                Line2D lineMN;
                if (this.linesParallel) {
                    lineMN = GeomUtil.getMedianLine(this.ptA, this.ptB, this.ptC, this.ptD);
                } else {
                    AffineTransform rotate = AffineTransform.getRotateInstance(-Math.toRadians(this.angleDeg) / 2.0, this.ptP.getX(), this.ptP.getY());
                    Point2D ptM = (Point2D)this.lineABP[0].clone();
                    rotate.transform(ptM, ptM);
                    lineMN = new Line2D.Double(ptM, this.ptP);
                }
                if (handlePointIndex == 4 && this.ptO != null) {
                    this.ptO = GeomUtil.getPerpendicularPointToLine(lineMN, this.ptO);
                } else if (this.linesParallel) {
                    this.ptO = GeomUtil.getMidPoint(lineMN.getP1(), lineMN.getP1());
                } else {
                    Point2D h1 = GeomUtil.getMidPoint(this.lineABP[1], GeomUtil.getMidPoint(this.lineABP[0], this.lineABP[1]));
                    Point2D h2 = GeomUtil.getMidPoint(this.lineCDP[1], GeomUtil.getMidPoint(this.lineCDP[0], this.lineCDP[1]));
                    Point2D o1 = GeomUtil.getPerpendicularPointToLine(lineMN, h1);
                    Point2D o2 = GeomUtil.getPerpendicularPointToLine(lineMN, h2);
                    this.ptO = GeomUtil.getMidPoint(o1, o2);
                }
            } else {
                this.ptO = null;
            }
            if (this.pts.size() < 5) {
                this.pts.add(this.ptO);
            } else {
                this.pts.set(4, this.ptO);
            }
        }
        return handlePointIndex;
    }

    @Override
    public void buildShape(MouseEventDouble mouseEvent) {
        this.updateTool();
        Shape newShape = null;
        Path2D.Double path = new Path2D.Double(1, 6);
        if (this.lineABvalid) {
            path.append(new Line2D.Double(this.ptA, this.ptB), false);
        }
        if (this.lineCDvalid) {
            path.append(new Line2D.Double(this.ptC, this.ptD), false);
        }
        if (this.lineABvalid && this.lineCDvalid && this.ptO != null) {
            newShape = new AdvancedShape(this, 10);
            AdvancedShape aShape = newShape;
            aShape.addShape(path);
            double ax = this.ptA.getX();
            double ay = this.ptA.getY();
            double bx = this.ptB.getX();
            double by = this.ptB.getY();
            double cx = this.ptC.getX();
            double cy = this.ptC.getY();
            double dx = this.ptD.getX();
            double dy = this.ptD.getY();
            double distAB2 = Point2D.distanceSq(ax, ay, bx, by);
            double distCD2 = Point2D.distanceSq(cx, cy, dx, dy);
            double r1 = ((ay - this.ptO.getY()) * (ay - by) + (ax - this.ptO.getX()) * (ax - bx)) / distAB2;
            double r2 = ((cy - this.ptO.getY()) * (cy - dy) + (cx - this.ptO.getX()) * (cx - dx)) / distCD2;
            Point2D.Double ptI = new Point2D.Double(ax + r1 * (bx - ax), ay + r1 * (by - ay));
            Point2D.Double ptJ = new Point2D.Double(cx + r2 * (dx - cx), cy + r2 * (dy - cy));
            if (r1 < 0.0 || r1 > 1.0) {
                aShape.addShape(new Line2D.Double(r1 > 1.0 ? this.ptB : this.ptA, ptI), this.getDashStroke(Float.valueOf(1.0f)), true);
            }
            if (r2 < 0.0 || r2 > 1.0) {
                aShape.addShape(new Line2D.Double(r1 > 1.0 ? this.ptD : this.ptC, ptJ), this.getDashStroke(Float.valueOf(1.0f)), true);
            }
            aShape.addShape(new Line2D.Double(this.ptO, ptI));
            aShape.addShape(new Line2D.Double(this.ptO, ptJ));
            double cLength = 10.0;
            double cImax = 0.6666666666666666 * Math.min(this.ptO.distance(ptI), Math.max(ptI.distance(this.ptA), ptI.distance(this.ptB)));
            aShape.addScaleInvShape(GeomUtil.getCornerShape(GeomUtil.getMidPoint(this.ptA, this.ptB), ptI, this.ptO, cLength), ptI, cLength / cImax, this.getStroke(Float.valueOf(1.0f)), true);
            double cJmax = 0.6666666666666666 * Math.min(this.ptO.distance(ptJ), Math.max(ptJ.distance(this.ptC), ptJ.distance(this.ptD)));
            aShape.addScaleInvShape(GeomUtil.getCornerShape(GeomUtil.getMidPoint(this.ptC, this.ptD), ptJ, this.ptO, cLength), ptJ, cLength / cJmax, this.getStroke(Float.valueOf(1.0f)), true);
            if (!this.linesParallel) {
                double extSegLength = 32.0;
                Point2D ptK = GeomUtil.getColinearPointWithLength(ptI, this.ptO, this.ptO.distance(ptI) + extSegLength);
                Point2D ptL = GeomUtil.getColinearPointWithLength(ptJ, this.ptO, this.ptO.distance(ptJ) + extSegLength);
                double distOKmax = 0.5 * this.ptO.distance(ptI);
                aShape.addScaleInvShape((Shape)new Line2D.Double(this.ptO, ptK), this.ptO, extSegLength / distOKmax, false);
                double distOLmax = 0.5 * this.ptO.distance(ptJ);
                aShape.addScaleInvShape((Shape)new Line2D.Double(this.ptO, ptL), this.ptO, extSegLength / distOLmax, false);
                double startingAngle = ptK.getY() > ptL.getY() ? GeomUtil.getAngleDeg(this.ptO, ptJ) : GeomUtil.getAngleDeg(this.ptO, ptI);
                double angularExtent = ptK.getY() > ptL.getY() ? GeomUtil.getAngleDeg(ptJ, this.ptO, ptK) : GeomUtil.getAngleDeg(ptI, this.ptO, ptL);
                angularExtent = GeomUtil.getSmallestRotationAngleDeg(angularExtent);
                double radius = 0.6666666666666666 * extSegLength;
                Rectangle2D.Double arcAngleBounds = new Rectangle2D.Double(this.ptO.getX() - radius, this.ptO.getY() - radius, 2.0 * radius, 2.0 * radius);
                Arc2D.Double arcAngle = new Arc2D.Double(arcAngleBounds, startingAngle, angularExtent, 0);
                double rMax = (ptK.getY() > ptL.getY() ? distOKmax : distOLmax) * 2.0 / 3.0;
                aShape.addScaleInvShape((Shape)arcAngle, this.ptO, radius / rMax, false);
            }
        } else if (path.getCurrentPoint() != null) {
            newShape = path;
        }
        this.setShape(newShape, mouseEvent);
        this.updateLabel(mouseEvent, this.getDefaultView2d(mouseEvent));
    }

    @Override
    protected void updateTool() {
        super.updateTool();
        this.ptO = this.getHandlePoint(4);
    }

    static {
        MEASUREMENT_LIST.add(ANGLE);
        MEASUREMENT_LIST.add(COMPLEMENTARY_ANGLE);
    }
}

