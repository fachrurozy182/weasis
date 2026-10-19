/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.xml.bind.annotation.XmlRootElement
 *  jakarta.xml.bind.annotation.XmlType
 *  org.weasis.core.util.MathUtil
 */
package org.weasis.core.ui.model.graphic.imp.angle;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
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
import org.weasis.core.ui.model.graphic.AbstractDragGraphic;
import org.weasis.core.ui.model.utils.bean.AdvancedShape;
import org.weasis.core.ui.model.utils.bean.MeasureItem;
import org.weasis.core.ui.model.utils.bean.Measurement;
import org.weasis.core.ui.model.utils.exceptions.InvalidShapeException;
import org.weasis.core.ui.util.MouseEventDouble;
import org.weasis.core.util.MathUtil;

@XmlType(name="openAngle")
@XmlRootElement(name="openAngle")
public class OpenAngleToolGraphic
extends AbstractDragGraphic {
    public static final Integer POINTS_NUMBER = 4;
    public static final Icon ICON = ResourceUtil.getIcon(ResourceUtil.ActionIcon.DRAW_OPEN_ANGLE);
    public static final Measurement ANGLE = new Measurement(Messages.getString("measure.angle"), 1, true);
    public static final Measurement COMPLEMENTARY_ANGLE = new Measurement(Messages.getString("measure.complement_angle"), 2, true, true, false);
    public static final Measurement REFLEX_ANGLE = new Measurement(Messages.getString("AngleToolGraphic.reflex_angle"), 3, true, true, false);
    protected static final List<Measurement> MEASUREMENT_LIST = new ArrayList<Measurement>();
    protected Point2D ptA;
    protected Point2D ptB;
    protected Point2D ptC;
    protected Point2D ptD;
    protected Point2D ptP;
    protected Point2D[] lineABP;
    protected Point2D[] lineCDP;
    protected boolean linesParallel;
    protected boolean intersectABsegment;
    protected boolean intersectCDsegment;
    protected boolean lineABvalid;
    protected boolean lineCDvalid;
    protected double angleDeg;

    public OpenAngleToolGraphic() {
        super(POINTS_NUMBER);
    }

    public OpenAngleToolGraphic(Integer pointsNumber) {
        super(pointsNumber);
    }

    public OpenAngleToolGraphic(OpenAngleToolGraphic graphic) {
        super(graphic);
    }

    @Override
    public OpenAngleToolGraphic copy() {
        return new OpenAngleToolGraphic(this);
    }

    @Override
    public List<Measurement> getMeasurementList() {
        return MEASUREMENT_LIST;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getUIName() {
        return Messages.getString("measure.open_angle");
    }

    @Override
    protected void prepareShape() throws InvalidShapeException {
        if (!this.isShapeValid()) {
            throw new InvalidShapeException("This shape cannot be drawn");
        }
        this.buildShape(null);
    }

    @Override
    public boolean isShapeValid() {
        this.updateTool();
        return this.lineABvalid && this.lineCDvalid;
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
        if (this.lineABvalid && this.lineCDvalid && !this.linesParallel && Math.abs(this.angleDeg) > 0.1) {
            newShape = new AdvancedShape(this, 5);
            AdvancedShape aShape = (AdvancedShape)newShape;
            aShape.addShape(path);
            Point2D.Double ptI1 = GeomUtil.getCollinearPointWithRatio(this.lineABP[1], this.lineABP[0], 0.25);
            Point2D.Double ptJ1 = GeomUtil.getCollinearPointWithRatio(this.lineCDP[1], this.lineCDP[0], 0.25);
            Point2D.Double ptI2 = GeomUtil.getCollinearPointWithRatio(this.lineABP[0], this.lineABP[1], 0.25);
            Point2D.Double ptJ2 = GeomUtil.getCollinearPointWithRatio(this.lineCDP[0], this.lineCDP[1], 0.25);
            double maxRadius = Math.min(this.ptP.distance(ptI2), this.ptP.distance(ptJ2));
            double radius = Math.min(maxRadius, (this.ptP.distance(ptI1) + this.ptP.distance(ptJ1)) / 2.0);
            double startingAngle = GeomUtil.getAngleDeg(this.ptP, this.lineABP[0]);
            Rectangle2D.Double arcAngleBounds = new Rectangle2D.Double(this.ptP.getX() - radius, this.ptP.getY() - radius, 2.0 * radius, 2.0 * radius);
            Arc2D.Double arcAngle = new Arc2D.Double(arcAngleBounds, startingAngle, this.angleDeg, 0);
            aShape.addShape(arcAngle, this.getStroke(Float.valueOf(1.0f)), true);
            if (!this.intersectABsegment) {
                aShape.addShape(new Line2D.Double(this.ptP, this.lineABP[1]), this.getDashStroke(Float.valueOf(1.0f)), true);
            }
            if (!this.intersectCDsegment) {
                aShape.addShape(new Line2D.Double(this.ptP, this.lineCDP[1]), this.getDashStroke(Float.valueOf(1.0f)), true);
            }
            int iPtSize = 8;
            Path2D.Double intersectPtShape = new Path2D.Double(1, 5);
            Rectangle2D.Double intersecPtBounds = new Rectangle2D.Double(this.ptP.getX() - (double)iPtSize / 2.0, this.ptP.getY() - (double)iPtSize / 2.0, iPtSize, iPtSize);
            intersectPtShape.append(new Line2D.Double(this.ptP.getX() - (double)iPtSize, this.ptP.getY(), this.ptP.getX() - 2.0, this.ptP.getY()), false);
            intersectPtShape.append(new Line2D.Double(this.ptP.getX() + 2.0, this.ptP.getY(), this.ptP.getX() + (double)iPtSize, this.ptP.getY()), false);
            intersectPtShape.append(new Line2D.Double(this.ptP.getX(), this.ptP.getY() - (double)iPtSize, this.ptP.getX(), this.ptP.getY() - 2.0), false);
            intersectPtShape.append(new Line2D.Double(this.ptP.getX(), this.ptP.getY() + 2.0, this.ptP.getX(), this.ptP.getY() + (double)iPtSize), false);
            intersectPtShape.append(new Arc2D.Double(intersecPtBounds, 0.0, 360.0, 0), false);
            aShape.addScaleInvShape((Shape)intersectPtShape, this.ptP, this.getStroke(Float.valueOf(0.5f)), true);
        } else if (path.getCurrentPoint() != null) {
            newShape = path;
        }
        this.setShape(newShape, mouseEvent);
        this.updateLabel(mouseEvent, this.getDefaultView2d(mouseEvent));
    }

    protected void updateTool() {
        this.ptA = this.getHandlePoint(0);
        this.ptB = this.getHandlePoint(1);
        this.ptC = this.getHandlePoint(2);
        this.ptD = this.getHandlePoint(3);
        this.lineCDP = null;
        this.lineABP = null;
        this.intersectCDsegment = false;
        this.intersectABsegment = false;
        this.linesParallel = false;
        this.angleDeg = 0.0;
        this.lineABvalid = this.ptA != null && this.ptB != null && !this.ptB.equals(this.ptA);
        boolean bl = this.lineCDvalid = this.ptC != null && this.ptD != null && !this.ptC.equals(this.ptD);
        if (this.lineABvalid && this.lineCDvalid) {
            double denominator = (this.ptB.getX() - this.ptA.getX()) * (this.ptD.getY() - this.ptC.getY()) - (this.ptB.getY() - this.ptA.getY()) * (this.ptD.getX() - this.ptC.getX());
            this.linesParallel = MathUtil.isEqualToZero((double)denominator);
            if (!this.linesParallel) {
                double numerator1 = (this.ptA.getY() - this.ptC.getY()) * (this.ptD.getX() - this.ptC.getX()) - (this.ptA.getX() - this.ptC.getX()) * (this.ptD.getY() - this.ptC.getY());
                double numerator2 = (this.ptA.getY() - this.ptC.getY()) * (this.ptB.getX() - this.ptA.getX()) - (this.ptA.getX() - this.ptC.getX()) * (this.ptB.getY() - this.ptA.getY());
                double r = numerator1 / denominator;
                double s = numerator2 / denominator;
                this.ptP = new Point2D.Double(this.ptA.getX() + r * (this.ptB.getX() - this.ptA.getX()), this.ptA.getY() + r * (this.ptB.getY() - this.ptA.getY()));
                this.lineABP = new Point2D[3];
                this.lineCDP = new Point2D[3];
                this.intersectABsegment = r >= 0.0 && r <= 1.0;
                this.intersectCDsegment = s >= 0.0 && s <= 1.0;
                this.validateOrder(r, this.lineABP, this.ptA, this.ptB, this.intersectABsegment);
                this.validateOrder(s, this.lineCDP, this.ptC, this.ptD, this.intersectCDsegment);
                this.angleDeg = GeomUtil.getSmallestRotationAngleDeg(GeomUtil.getAngleDeg(this.lineABP[0], this.ptP, this.lineCDP[0]));
            }
        }
    }

    private void validateOrder(double s, Point2D[] lineCDP, Point2D ptC, Point2D ptD, boolean intersectCDsegment) {
        OpenAngleToolGraphic.addToLine(s, lineCDP, ptC, ptD, this.ptP);
        this.validatePosition(intersectCDsegment, lineCDP);
    }

    static void addToLine(double s, Point2D[] lineCDP, Point2D ptC, Point2D ptD, Point2D ptP) {
        Point2D point2D = lineCDP[0] = s >= 0.0 ? ptC : ptD;
        Point2D point2D2 = s < 0.0 ? ptC : (lineCDP[1] = s > 1.0 ? ptD : ptP);
        lineCDP[2] = s < 0.0 ? ptP : (s > 1.0 ? ptP : ptD);
    }

    private void validatePosition(boolean intersectABSegment, Point2D[] lineABP) {
        if (intersectABSegment) {
            if (this.ptP.distance(lineABP[0]) < this.ptP.distance(lineABP[2])) {
                Point2D switchPt = (Point2D)lineABP[2].clone();
                lineABP[2] = (Point2D)lineABP[0].clone();
                lineABP[0] = switchPt;
            }
        } else if (this.ptP.distance(lineABP[0]) < this.ptP.distance(lineABP[1])) {
            Point2D switchPt = (Point2D)lineABP[1].clone();
            lineABP[1] = (Point2D)lineABP[0].clone();
            lineABP[0] = switchPt;
        }
    }

    @Override
    public List<MeasureItem> computeMeasurements(MeasurableLayer layer, boolean releaseEvent, Unit displayUnit) {
        MeasurementsAdapter adapter;
        if (layer != null && layer.hasContent() && this.isShapeValid() && (adapter = layer.getMeasurementAdapter(displayUnit)) != null) {
            ArrayList<MeasureItem> measVal = new ArrayList<MeasureItem>();
            double positiveAngle = Math.abs(this.angleDeg);
            if (ANGLE.getComputed().booleanValue()) {
                measVal.add(new MeasureItem(ANGLE, positiveAngle, Messages.getString("measure.deg")));
            }
            if (COMPLEMENTARY_ANGLE.getComputed().booleanValue()) {
                measVal.add(new MeasureItem(COMPLEMENTARY_ANGLE, 180.0 - positiveAngle, Messages.getString("measure.deg")));
            }
            if (REFLEX_ANGLE.getComputed().booleanValue()) {
                measVal.add(new MeasureItem(REFLEX_ANGLE, 360.0 - positiveAngle, Messages.getString("measure.deg")));
            }
            return measVal;
        }
        return Collections.emptyList();
    }

    static {
        MEASUREMENT_LIST.add(ANGLE);
        MEASUREMENT_LIST.add(COMPLEMENTARY_ANGLE);
        MEASUREMENT_LIST.add(REFLEX_ANGLE);
    }
}

