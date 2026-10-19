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
import org.weasis.core.ui.model.graphic.imp.angle.OpenAngleToolGraphic;
import org.weasis.core.ui.model.utils.bean.AdvancedShape;
import org.weasis.core.ui.model.utils.bean.MeasureItem;
import org.weasis.core.ui.model.utils.bean.Measurement;
import org.weasis.core.ui.model.utils.exceptions.InvalidShapeException;
import org.weasis.core.ui.util.MouseEventDouble;
import org.weasis.core.util.MathUtil;

@XmlType(name="fourPointsAngle")
@XmlRootElement(name="fourPointsAngle")
public class FourPointsAngleToolGraphic
extends AbstractDragGraphic {
    public static final Integer POINTS_NUMBER = 8;
    public static final Icon ICON = ResourceUtil.getIcon(ResourceUtil.ActionIcon.DRAW_4POINTS_ANGLE);
    public static final Measurement ANGLE = new Measurement(Messages.getString("measure.angle"), 1, true);
    public static final Measurement COMPLEMENTARY_ANGLE = new Measurement(Messages.getString("measure.complement_angle"), 2, true, true, false);
    protected static final List<Measurement> MEASUREMENT_LIST = new ArrayList<Measurement>();
    Point2D ptA;
    Point2D ptB;
    Point2D ptC;
    Point2D ptD;
    Point2D ptI;
    Point2D ptJ;
    Point2D ptE;
    Point2D ptF;
    Point2D ptG;
    Point2D ptH;
    Point2D ptK;
    Point2D ptL;
    Point2D ptP;
    Point2D[] lineIJP;
    Point2D[] lineKLP;
    boolean lineParallel;
    boolean intersectIJsegment;
    boolean intersectKLsegment;
    boolean lineABvalid;
    boolean lineCDvalid;
    boolean lineEFvalid;
    boolean lineGHvalid;
    boolean lineIJvalid;
    boolean lineKLvalid;
    double angleDeg;

    public FourPointsAngleToolGraphic() {
        super(POINTS_NUMBER);
    }

    public FourPointsAngleToolGraphic(FourPointsAngleToolGraphic graphic) {
        super(graphic);
    }

    @Override
    public FourPointsAngleToolGraphic copy() {
        return new FourPointsAngleToolGraphic(this);
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getUIName() {
        return Messages.getString("measure.four_pt_angle");
    }

    @Override
    protected void prepareShape() throws InvalidShapeException {
        if (!this.isShapeValid()) {
            throw new InvalidShapeException("This shape cannot be drawn");
        }
        this.buildShape(null);
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
        if (this.lineIJvalid) {
            path.append(new Line2D.Double(this.ptI, this.ptJ), false);
        }
        if (this.lineEFvalid) {
            path.append(new Line2D.Double(this.ptE, this.ptF), false);
        }
        if (this.lineGHvalid) {
            path.append(new Line2D.Double(this.ptG, this.ptH), false);
        }
        if (this.lineKLvalid) {
            path.append(new Line2D.Double(this.ptK, this.ptL), false);
        }
        if (this.lineIJvalid && this.lineKLvalid && !this.lineParallel && Math.abs(this.angleDeg) > 0.1) {
            newShape = new AdvancedShape(this, 4);
            AdvancedShape aShape = (AdvancedShape)newShape;
            aShape.addShape(path);
            double startingAngle = GeomUtil.getAngleDeg(this.ptP, this.lineIJP[0]);
            double radius = 32.0;
            Rectangle2D.Double arcAngleBounds = new Rectangle2D.Double(this.ptP.getX() - radius, this.ptP.getY() - radius, 2.0 * radius, 2.0 * radius);
            Arc2D.Double arcAngle = new Arc2D.Double(arcAngleBounds, startingAngle, this.angleDeg, 0);
            double rMax = Math.min(this.ptP.distance(this.lineIJP[0]), this.ptP.distance(this.lineKLP[0])) * 2.0 / 3.0;
            double scalingMin = radius / rMax;
            aShape.addScaleInvShape((Shape)arcAngle, this.ptP, scalingMin, true);
            if (!this.intersectIJsegment) {
                aShape.addShape(new Line2D.Double(this.ptP, this.lineIJP[1]), this.getDashStroke(Float.valueOf(1.0f)), true);
            }
            if (!this.intersectKLsegment) {
                aShape.addShape(new Line2D.Double(this.ptP, this.lineKLP[1]), this.getDashStroke(Float.valueOf(1.0f)), true);
            }
        } else if (path.getCurrentPoint() != null) {
            newShape = path;
        }
        this.setShape(newShape, mouseEvent);
        this.updateLabel(mouseEvent, this.getDefaultView2d(mouseEvent));
    }

    @Override
    public List<MeasureItem> computeMeasurements(MeasurableLayer layer, boolean releaseEvent, Unit displayUnit) {
        MeasurementsAdapter adapter;
        if (layer != null && layer.hasContent() && this.isShapeValid() && (adapter = layer.getMeasurementAdapter(displayUnit)) != null) {
            ArrayList<MeasureItem> measVal = new ArrayList<MeasureItem>();
            if (ANGLE.getComputed().booleanValue() || COMPLEMENTARY_ANGLE.getComputed().booleanValue()) {
                double positiveAngle = Math.abs(this.angleDeg);
                if (ANGLE.getComputed().booleanValue()) {
                    measVal.add(new MeasureItem(ANGLE, positiveAngle, Messages.getString("measure.deg")));
                }
                if (COMPLEMENTARY_ANGLE.getComputed().booleanValue()) {
                    measVal.add(new MeasureItem(COMPLEMENTARY_ANGLE, 180.0 - positiveAngle, Messages.getString("measure.deg")));
                }
            }
            return measVal;
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isShapeValid() {
        this.updateTool();
        return this.lineABvalid && this.lineCDvalid && this.lineEFvalid && this.lineGHvalid && this.lineIJvalid && this.lineKLvalid;
    }

    protected void init() {
        this.ptA = this.getHandlePoint(0);
        this.ptB = this.getHandlePoint(1);
        this.ptC = this.getHandlePoint(2);
        this.ptD = this.getHandlePoint(3);
        this.ptI = this.ptA;
        this.ptJ = this.ptC;
        this.ptE = this.getHandlePoint(4);
        this.ptF = this.getHandlePoint(5);
        this.ptG = this.getHandlePoint(6);
        this.ptH = this.getHandlePoint(7);
        this.ptK = this.ptE;
        this.ptL = this.ptG;
        this.lineKLP = null;
        this.lineIJP = null;
        this.intersectKLsegment = false;
        this.intersectIJsegment = false;
        this.lineParallel = false;
        this.lineKLvalid = false;
        this.lineIJvalid = false;
        this.lineGHvalid = false;
        this.lineEFvalid = false;
        this.lineCDvalid = false;
        this.lineABvalid = false;
        this.angleDeg = 0.0;
    }

    protected void updateTool() {
        this.init();
        boolean bl = this.lineABvalid = this.ptA != null && this.ptB != null && !this.ptB.equals(this.ptA);
        if (this.lineABvalid) {
            this.ptI = GeomUtil.getMidPoint(this.ptA, this.ptB);
        }
        boolean bl2 = this.lineCDvalid = this.ptC != null && this.ptD != null && !this.ptC.equals(this.ptD);
        if (this.lineCDvalid) {
            this.ptJ = GeomUtil.getMidPoint(this.ptC, this.ptD);
        }
        this.lineIJvalid = this.ptI != null && this.ptJ != null && !this.ptI.equals(this.ptJ);
        boolean bl3 = this.lineEFvalid = this.ptE != null && this.ptF != null && !this.ptE.equals(this.ptF);
        if (this.lineEFvalid) {
            this.ptK = GeomUtil.getMidPoint(this.ptE, this.ptF);
        }
        boolean bl4 = this.lineGHvalid = this.ptG != null && this.ptH != null && !this.ptG.equals(this.ptH);
        if (this.lineGHvalid) {
            this.ptL = GeomUtil.getMidPoint(this.ptG, this.ptH);
        } else if (this.ptG == null && this.lineEFvalid) {
            this.ptL = GeomUtil.getPerpendicularPointFromLine(this.ptE, this.ptF, this.ptK, 1.0);
        }
        boolean bl5 = this.lineKLvalid = this.ptK != null && this.ptL != null && !this.ptK.equals(this.ptL);
        if (this.lineIJvalid && this.lineKLvalid) {
            double denominator = (this.ptJ.getX() - this.ptI.getX()) * (this.ptL.getY() - this.ptK.getY()) - (this.ptJ.getY() - this.ptI.getY()) * (this.ptL.getX() - this.ptK.getX());
            this.lineParallel = MathUtil.isEqualToZero((double)denominator);
            if (!this.lineParallel) {
                Point2D switchPt;
                double numerator1 = (this.ptI.getY() - this.ptK.getY()) * (this.ptL.getX() - this.ptK.getX()) - (this.ptI.getX() - this.ptK.getX()) * (this.ptL.getY() - this.ptK.getY());
                double numerator2 = (this.ptI.getY() - this.ptK.getY()) * (this.ptJ.getX() - this.ptI.getX()) - (this.ptI.getX() - this.ptK.getX()) * (this.ptJ.getY() - this.ptI.getY());
                double r = numerator1 / denominator;
                double s = numerator2 / denominator;
                this.ptP = new Point2D.Double(this.ptI.getX() + r * (this.ptJ.getX() - this.ptI.getX()), this.ptI.getY() + r * (this.ptJ.getY() - this.ptI.getY()));
                this.lineIJP = new Point2D[3];
                this.lineKLP = new Point2D[3];
                this.intersectIJsegment = r >= 0.0 && r <= 1.0;
                this.intersectKLsegment = s >= 0.0 && s <= 1.0;
                OpenAngleToolGraphic.addToLine(r, this.lineIJP, this.ptI, this.ptJ, this.ptP);
                if (this.intersectIJsegment && this.ptP.distance(this.lineIJP[0]) < this.ptP.distance(this.lineIJP[2])) {
                    switchPt = (Point2D)this.lineIJP[2].clone();
                    this.lineIJP[2] = (Point2D)this.lineIJP[0].clone();
                    this.lineIJP[0] = switchPt;
                } else if (this.ptP.distance(this.lineIJP[0]) < this.ptP.distance(this.lineIJP[1])) {
                    switchPt = (Point2D)this.lineIJP[1].clone();
                    this.lineIJP[1] = (Point2D)this.lineIJP[0].clone();
                    this.lineIJP[0] = switchPt;
                }
                OpenAngleToolGraphic.addToLine(s, this.lineKLP, this.ptK, this.ptL, this.ptP);
                if (this.intersectKLsegment && this.ptP.distance(this.lineKLP[0]) < this.ptP.distance(this.lineKLP[2])) {
                    switchPt = (Point2D)this.lineKLP[2].clone();
                    this.lineKLP[2] = (Point2D)this.lineKLP[0].clone();
                    this.lineKLP[0] = switchPt;
                } else if (this.ptP.distance(this.lineKLP[0]) < this.ptP.distance(this.lineKLP[1])) {
                    switchPt = (Point2D)this.lineKLP[1].clone();
                    this.lineKLP[1] = (Point2D)this.lineKLP[0].clone();
                    this.lineKLP[0] = switchPt;
                }
                this.angleDeg = GeomUtil.getSmallestRotationAngleDeg(GeomUtil.getAngleDeg(this.lineIJP[0], this.ptP, this.lineKLP[0]));
            }
        }
    }

    @Override
    public List<Measurement> getMeasurementList() {
        return MEASUREMENT_LIST;
    }

    static {
        MEASUREMENT_LIST.add(ANGLE);
        MEASUREMENT_LIST.add(COMPLEMENTARY_ANGLE);
    }
}

