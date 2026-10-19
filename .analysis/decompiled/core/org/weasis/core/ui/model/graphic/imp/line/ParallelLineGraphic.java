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
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.swing.Icon;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.GeomUtil;
import org.weasis.core.api.image.measure.MeasurementsAdapter;
import org.weasis.core.api.image.util.MeasurableLayer;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.model.graphic.AbstractDragGraphic;
import org.weasis.core.ui.model.utils.bean.MeasureItem;
import org.weasis.core.ui.model.utils.bean.Measurement;
import org.weasis.core.ui.model.utils.exceptions.InvalidShapeException;
import org.weasis.core.ui.util.MouseEventDouble;
import org.weasis.core.util.MathUtil;

@XmlType(name="parallelLine")
@XmlRootElement(name="parallelLine")
public class ParallelLineGraphic
extends AbstractDragGraphic {
    public static final Integer POINTS_NUMBER = 6;
    public static final Icon ICON = ResourceUtil.getIcon(ResourceUtil.ActionIcon.DRAW_PARALLEL);
    public static final Measurement DISTANCE = new Measurement(Messages.getString("measure.distance"), 1, true, true, true);
    public static final Measurement ORIENTATION = new Measurement(Messages.getString("measure.orientation"), 2, true, true, false);
    public static final Measurement AZIMUTH = new Measurement(Messages.getString("measure.azimuth"), 3, true, true, false);
    protected static final List<Measurement> MEASUREMENT_LIST = new ArrayList<Measurement>();
    protected Point2D ptA;
    protected Point2D ptB;
    protected Point2D ptC;
    protected Point2D ptD;
    protected Point2D ptE;
    protected Point2D ptF;
    protected Boolean lineABvalid;
    protected Boolean lineCDvalid;

    public ParallelLineGraphic() {
        super(POINTS_NUMBER);
    }

    public ParallelLineGraphic(ParallelLineGraphic graphic) {
        super(graphic);
    }

    @Override
    public ParallelLineGraphic copy() {
        return new ParallelLineGraphic(this);
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getUIName() {
        return Messages.getString("measure.parallel");
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
        if ((handlePointIndex = super.moveAndResizeOnDrawing(handlePointIndex, deltaX, deltaY, mouseEvent)) >= 0 && handlePointIndex < this.getHandlePointListSize()) {
            this.updateTool();
            if (this.lineABvalid.booleanValue() && this.lineCDvalid.booleanValue()) {
                if (handlePointIndex == 0 || handlePointIndex == 1) {
                    Point2D anchor = handlePointIndex == 0 ? this.ptB : this.ptA;
                    double theta = GeomUtil.getSmallestAngleRad(GeomUtil.getAngleRad(this.ptC, this.ptD) - GeomUtil.getAngleRad(this.ptA, this.ptB));
                    AffineTransform rotate = AffineTransform.getRotateInstance(theta, anchor.getX(), anchor.getY());
                    rotate.transform(this.ptC, this.ptC);
                    rotate.transform(this.ptD, this.ptD);
                    this.setHandlePoint(2, this.ptC);
                    this.setHandlePoint(3, this.ptD);
                } else if (handlePointIndex == 2 || handlePointIndex == 3) {
                    Point2D pt1 = handlePointIndex == 2 ? this.ptC : this.ptD;
                    Point2D pt2 = handlePointIndex == 2 ? this.ptD : this.ptC;
                    int hIndex = handlePointIndex == 2 ? 3 : 2;
                    Point2D ptI = GeomUtil.getPerpendicularPointToLine(this.ptA, this.ptB, pt1);
                    Point2D ptJ = GeomUtil.getPerpendicularPointToLine(this.ptA, this.ptB, pt2);
                    double transX = pt1.getX() - ptI.getX() - (pt2.getX() - ptJ.getX());
                    double transY = pt1.getY() - ptI.getY() - (pt2.getY() - ptJ.getY());
                    AffineTransform translate = AffineTransform.getTranslateInstance(transX, transY);
                    translate.transform(pt2, pt2);
                    this.setHandlePoint(hIndex, pt2);
                } else if (handlePointIndex == 4 || handlePointIndex == 5) {
                    int hIndex2;
                    Point2D pt0 = handlePointIndex == 4 ? this.ptE : this.ptF;
                    Point2D pt1 = handlePointIndex == 4 ? this.ptA : this.ptC;
                    Point2D pt2 = handlePointIndex == 4 ? this.ptB : this.ptD;
                    int hIndex1 = handlePointIndex == 4 ? 0 : 2;
                    int n = hIndex2 = handlePointIndex == 4 ? 1 : 3;
                    if (Objects.nonNull(pt0)) {
                        Point2D ptI = GeomUtil.getPerpendicularPointToLine(pt1, pt2, pt0);
                        AffineTransform translate = AffineTransform.getTranslateInstance(pt0.getX() - ptI.getX(), pt0.getY() - ptI.getY());
                        translate.transform(pt1, pt1);
                        translate.transform(pt2, pt2);
                        this.setHandlePoint(hIndex1, pt1);
                        this.setHandlePoint(hIndex2, pt2);
                    }
                }
                this.setHandlePoint(4, GeomUtil.getMidPoint(this.ptA, this.ptB));
                this.setHandlePoint(5, GeomUtil.getMidPoint(this.ptC, this.ptD));
            }
        }
        return handlePointIndex;
    }

    @Override
    public void buildShape(MouseEventDouble mouseEvent) {
        this.updateTool();
        Path2D.Double newShape = null;
        Path2D.Double path = new Path2D.Double(1, 2);
        if (this.lineABvalid.booleanValue()) {
            path.append(new Line2D.Double(this.ptA, this.ptB), false);
        }
        if (this.lineCDvalid.booleanValue()) {
            path.append(new Line2D.Double(this.ptC, this.ptD), false);
        }
        if (path.getCurrentPoint() != null) {
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
            if (DISTANCE.getComputed().booleanValue()) {
                Double val = this.ptC.distance(GeomUtil.getPerpendicularPointToLine(this.ptA, this.ptB, this.ptC)) * adapter.calibrationRatio();
                measVal.add(new MeasureItem(DISTANCE, val, adapter.unit()));
            }
            if (ORIENTATION.getComputed().booleanValue()) {
                measVal.add(new MeasureItem(ORIENTATION, MathUtil.getOrientation((Point2D)this.ptA, (Point2D)this.ptB), Messages.getString("measure.deg")));
            }
            if (AZIMUTH.getComputed().booleanValue()) {
                measVal.add(new MeasureItem(AZIMUTH, MathUtil.getAzimuth((Point2D)this.ptA, (Point2D)this.ptB), Messages.getString("measure.deg")));
            }
            return measVal;
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isShapeValid() {
        this.updateTool();
        return this.lineABvalid != false && this.lineCDvalid != false;
    }

    protected void updateTool() {
        this.ptA = this.getHandlePoint(0);
        this.ptB = this.getHandlePoint(1);
        this.ptC = this.getHandlePoint(2);
        this.ptD = this.getHandlePoint(3);
        this.ptE = this.getHandlePoint(4);
        this.ptF = this.getHandlePoint(5);
        this.lineABvalid = this.ptA != null && this.ptB != null && !this.ptB.equals(this.ptA);
        this.lineCDvalid = this.ptC != null && this.ptD != null && !this.ptC.equals(this.ptD);
    }

    @Override
    public List<Measurement> getMeasurementList() {
        return MEASUREMENT_LIST;
    }

    static {
        MEASUREMENT_LIST.add(DISTANCE);
        MEASUREMENT_LIST.add(ORIENTATION);
        MEASUREMENT_LIST.add(AZIMUTH);
    }
}

