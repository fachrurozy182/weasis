/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.xml.bind.annotation.XmlRootElement
 *  jakarta.xml.bind.annotation.XmlType
 *  org.weasis.core.util.MathUtil
 */
package org.weasis.core.ui.model.graphic.imp.line;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
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
import org.weasis.core.ui.model.graphic.AbstractDragGraphic;
import org.weasis.core.ui.model.utils.bean.AdvancedShape;
import org.weasis.core.ui.model.utils.bean.MeasureItem;
import org.weasis.core.ui.model.utils.bean.Measurement;
import org.weasis.core.ui.model.utils.exceptions.InvalidShapeException;
import org.weasis.core.ui.util.MouseEventDouble;
import org.weasis.core.util.MathUtil;

@XmlType(name="perpendicularLine")
@XmlRootElement(name="perpendicularLine")
public class PerpendicularLineGraphic
extends AbstractDragGraphic {
    public static final Integer POINTS_NUMBER = 4;
    public static final Icon ICON = ResourceUtil.getIcon(ResourceUtil.ActionIcon.DRAW_PERPENDICULAR);
    public static final Measurement LINE_LENGTH = new Measurement(Messages.getString("measure.length"), 1, true, true, true);
    public static final Measurement ORIENTATION = new Measurement(Messages.getString("measure.orientation"), 2, true, true, false);
    public static final Measurement AZIMUTH = new Measurement(Messages.getString("measure.azimuth"), 3, true, true, false);
    protected static final List<Measurement> MEASUREMENT_LIST = new ArrayList<Measurement>();
    protected Point2D ptA;
    protected Point2D ptB;
    protected Point2D ptC;
    protected Point2D ptD;
    protected boolean lineABvalid;
    protected boolean lineCDvalid;

    public PerpendicularLineGraphic() {
        super(POINTS_NUMBER);
    }

    public PerpendicularLineGraphic(PerpendicularLineGraphic graphic) {
        super(graphic);
    }

    @Override
    public PerpendicularLineGraphic copy() {
        return new PerpendicularLineGraphic(this);
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getUIName() {
        return Messages.getString("measure.perpendicular");
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
                    double theta = GeomUtil.getAngleRad(prevPtA, prevPtB) - GeomUtil.getAngleRad(this.ptA, this.ptB);
                    Point2D anchor = handlePointIndex == 0 ? this.ptB : this.ptA;
                    AffineTransform rotate = AffineTransform.getRotateInstance(theta, anchor.getX(), anchor.getY());
                    rotate.transform(this.ptC, this.ptC);
                    rotate.transform(this.ptD, this.ptD);
                    this.setHandlePoint(2, this.ptC);
                    this.setHandlePoint(3, this.ptD);
                }
            } else if (handlePointIndex == 2) {
                if (this.lineABvalid && this.ptC != null) {
                    this.ptD = GeomUtil.getPerpendicularPointToLine(this.ptA, this.ptB, this.ptC);
                    this.setHandlePoint(3, this.ptD);
                }
            } else if (handlePointIndex == 3) {
                Point2D prevPtD;
                Point2D point2D = prevPtD = prevHandlePointList.size() > 3 ? prevHandlePointList.get(3) : null;
                if (this.lineABvalid && this.ptD != null && prevPtD != null && this.ptC != null) {
                    this.ptD = GeomUtil.getPerpendicularPointToLine(this.ptA, this.ptB, this.ptD);
                    AffineTransform translate = AffineTransform.getTranslateInstance(this.ptD.getX() - prevPtD.getX(), this.ptD.getY() - prevPtD.getY());
                    translate.transform(this.ptC, this.ptC);
                    this.setHandlePoint(2, this.ptC);
                    this.setHandlePoint(3, this.ptD);
                }
            }
        }
        return handlePointIndex;
    }

    @Override
    public void buildShape(MouseEventDouble mouseEvent) {
        this.updateTool();
        Shape newShape = null;
        Path2D.Double path = new Path2D.Double(1, 2);
        if (this.lineABvalid) {
            path.append(new Line2D.Double(this.ptA, this.ptB), false);
        }
        if (this.lineCDvalid) {
            path.append(new Line2D.Double(this.ptC, this.ptD), false);
        }
        if (this.lineABvalid && this.lineCDvalid) {
            newShape = new AdvancedShape(this, 3);
            AdvancedShape aShape = (AdvancedShape)newShape;
            aShape.addShape(path);
            if (!this.ptD.equals(this.ptA) && !this.ptD.equals(this.ptB) && MathUtil.isEqual((double)Math.signum(GeomUtil.getAngleDeg(this.ptD, this.ptA)), (double)Math.signum(GeomUtil.getAngleDeg(this.ptD, this.ptB)))) {
                Point2D ptE = this.ptD.distance(this.ptA) < this.ptD.distance(this.ptB) ? this.ptA : this.ptB;
                aShape.addShape(new Line2D.Double(this.ptD, ptE), this.getDashStroke(Float.valueOf(1.0f)), true);
            }
            double cornerLength = 10.0;
            double dMin = Math.min(this.ptD.distance(this.ptC), Math.max(this.ptD.distance(this.ptA), this.ptD.distance(this.ptB))) * 2.0 / 3.0;
            double scalingMin = cornerLength / dMin;
            Point2D f = GeomUtil.getMidPoint(this.ptA, this.ptB);
            Shape cornerShape = GeomUtil.getCornerShape(f, this.ptD, this.ptC, cornerLength);
            if (cornerShape != null) {
                aShape.addScaleInvShape(cornerShape, this.ptD, scalingMin, this.getStroke(Float.valueOf(1.0f)), true);
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
            ArrayList<MeasureItem> measVal = new ArrayList<MeasureItem>(3);
            if (LINE_LENGTH.getComputed().booleanValue()) {
                measVal.add(new MeasureItem(LINE_LENGTH, this.ptC.distance(this.ptD) * adapter.calibrationRatio(), adapter.unit()));
            }
            if (ORIENTATION.getComputed().booleanValue()) {
                measVal.add(new MeasureItem(ORIENTATION, MathUtil.getOrientation((Point2D)this.ptC, (Point2D)this.ptD), Messages.getString("measure.deg")));
            }
            if (AZIMUTH.getComputed().booleanValue()) {
                measVal.add(new MeasureItem(AZIMUTH, MathUtil.getAzimuth((Point2D)this.ptC, (Point2D)this.ptD), Messages.getString("measure.deg")));
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
        this.ptA = this.getHandlePoint(0);
        this.ptB = this.getHandlePoint(1);
        this.ptC = this.getHandlePoint(2);
        this.ptD = this.getHandlePoint(3);
        this.lineABvalid = this.ptA != null && this.ptB != null && !this.ptB.equals(this.ptA);
        this.lineCDvalid = this.ptC != null && this.ptD != null && !this.ptC.equals(this.ptD);
    }

    @Override
    public List<Measurement> getMeasurementList() {
        return MEASUREMENT_LIST;
    }

    static {
        MEASUREMENT_LIST.add(LINE_LENGTH);
        MEASUREMENT_LIST.add(ORIENTATION);
        MEASUREMENT_LIST.add(AZIMUTH);
    }
}

