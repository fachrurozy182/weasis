/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.xml.bind.annotation.XmlRootElement
 *  jakarta.xml.bind.annotation.XmlType
 */
package org.weasis.core.ui.model.graphic.imp.area;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.awt.geom.Ellipse2D;
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
import org.weasis.core.ui.model.graphic.AbstractDragGraphicArea;
import org.weasis.core.ui.model.utils.bean.MeasureItem;
import org.weasis.core.ui.model.utils.bean.Measurement;
import org.weasis.core.ui.model.utils.exceptions.InvalidShapeException;
import org.weasis.core.ui.util.MouseEventDouble;

@XmlType(name="threePointsCircle")
@XmlRootElement(name="threePointsCircle")
public class ThreePointsCircleGraphic
extends AbstractDragGraphicArea {
    public static final Integer POINTS_NUMBER = 3;
    public static final Icon ICON = ResourceUtil.getIcon(ResourceUtil.ActionIcon.DRAW_CIRCLE);
    public static final Measurement AREA = new Measurement(Messages.getString("measure.area"), 1, true, true, true);
    public static final Measurement DIAMETER = new Measurement(Messages.getString("measure.diameter"), 2, true, true, false);
    public static final Measurement PERIMETER = new Measurement(Messages.getString("measure.perimeter"), 3, true, true, false);
    public static final Measurement CENTER_X = new Measurement(Messages.getString("measure.centerx"), 4, true, true, false);
    public static final Measurement CENTER_Y = new Measurement(Messages.getString("measure.centery"), 5, true, true, false);
    public static final Measurement RADIUS = new Measurement(Messages.getString("measure.radius"), 6, true, true, false);
    protected static final List<Measurement> MEASUREMENT_LIST = new ArrayList<Measurement>();
    protected Point2D centerPt;
    protected Double radiusPt;

    public ThreePointsCircleGraphic() {
        super(POINTS_NUMBER);
    }

    public ThreePointsCircleGraphic(ThreePointsCircleGraphic graphic) {
        super(graphic);
    }

    @Override
    public ThreePointsCircleGraphic copy() {
        return new ThreePointsCircleGraphic(this);
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getUIName() {
        return Messages.getString("measure.three_pt_angle");
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
        Ellipse2D.Double newShape = null;
        if (Objects.nonNull(this.centerPt) && !Objects.equals(this.radiusPt, 0.0)) {
            newShape = new Ellipse2D.Double(this.centerPt.getX() - this.radiusPt, this.centerPt.getY() - this.radiusPt, 2.0 * this.radiusPt, 2.0 * this.radiusPt);
        }
        this.setShape(newShape, mouseEvent);
        this.updateLabel(mouseEvent, this.getDefaultView2d(mouseEvent));
    }

    @Override
    public Boolean getResizingOrMoving() {
        return Boolean.FALSE;
    }

    @Override
    public List<MeasureItem> computeMeasurements(MeasurableLayer layer, boolean releaseEvent, Unit displayUnit) {
        MeasurementsAdapter adapter;
        if (layer != null && layer.hasContent() && this.isShapeValid() && (adapter = layer.getMeasurementAdapter(displayUnit)) != null) {
            List<MeasureItem> stats;
            ArrayList<MeasureItem> measVal = new ArrayList<MeasureItem>();
            double ratio = adapter.calibrationRatio();
            if (CENTER_X.getComputed().booleanValue()) {
                measVal.add(new MeasureItem(CENTER_X, adapter.getXCalibratedValue(this.centerPt.getX()), adapter.unit()));
            }
            if (CENTER_Y.getComputed().booleanValue()) {
                measVal.add(new MeasureItem(CENTER_Y, adapter.getYCalibratedValue(this.centerPt.getY()), adapter.unit()));
            }
            if (RADIUS.getComputed().booleanValue()) {
                measVal.add(new MeasureItem(RADIUS, ratio * this.radiusPt, adapter.unit()));
            }
            if (DIAMETER.getComputed().booleanValue()) {
                measVal.add(new MeasureItem(DIAMETER, ratio * this.radiusPt * 2.0, adapter.unit()));
            }
            if (AREA.getComputed().booleanValue()) {
                Object unit = "pix".equals(adapter.unit()) ? adapter.unit() : adapter.unit() + "2";
                measVal.add(new MeasureItem(AREA, Math.PI * this.radiusPt * this.radiusPt * ratio * ratio, (String)unit));
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
    public boolean isShapeValid() {
        this.updateTool();
        return super.isShapeValid() && this.centerPt != null && this.radiusPt < 50000.0;
    }

    protected void updateTool() {
        Point2D ptA = this.getHandlePoint(0);
        this.centerPt = GeomUtil.getCircleCenter(this.pts);
        this.radiusPt = this.centerPt != null && ptA != null ? this.centerPt.distance(ptA) : 0.0;
    }

    static {
        MEASUREMENT_LIST.add(CENTER_X);
        MEASUREMENT_LIST.add(CENTER_Y);
        MEASUREMENT_LIST.add(RADIUS);
        MEASUREMENT_LIST.add(DIAMETER);
        MEASUREMENT_LIST.add(AREA);
        MEASUREMENT_LIST.add(PERIMETER);
    }
}

