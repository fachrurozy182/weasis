/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.xml.bind.annotation.XmlAccessType
 *  jakarta.xml.bind.annotation.XmlAccessorType
 *  jakarta.xml.bind.annotation.XmlRootElement
 *  jakarta.xml.bind.annotation.XmlType
 *  org.weasis.core.util.MathUtil
 */
package org.weasis.core.ui.model.graphic.imp.line;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Icon;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.ShortcutManager;
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

@XmlType(name="line")
@XmlRootElement(name="line")
@XmlAccessorType(value=XmlAccessType.NONE)
public class LineGraphic
extends AbstractDragGraphic {
    public static final Integer POINTS_NUMBER = 2;
    public static final Icon ICON = ResourceUtil.getIcon(ResourceUtil.ActionIcon.DRAW_LINE);
    public static final Measurement FIRST_POINT_X = new Measurement(Messages.getString("measure.firstx"), 1, true, true, false);
    public static final Measurement FIRST_POINT_Y = new Measurement(Messages.getString("measure.firsty"), 2, true, true, false);
    public static final Measurement LAST_POINT_X = new Measurement(Messages.getString("measure.lastx"), 3, true, true, false);
    public static final Measurement LAST_POINT_Y = new Measurement(Messages.getString("measure.lasty"), 4, true, true, false);
    public static final Measurement LINE_LENGTH = new Measurement(Messages.getString("measure.length"), 5, true, true, true);
    public static final Measurement ORIENTATION = new Measurement(Messages.getString("measure.orientation"), 6, true, true, false);
    public static final Measurement AZIMUTH = new Measurement(Messages.getString("measure.azimuth"), 7, true, true, false);
    protected static final List<Measurement> measurementList = new ArrayList<Measurement>();
    protected Point2D ptA;
    protected Point2D ptB;
    protected Boolean lineABvalid;

    public LineGraphic() {
        super(POINTS_NUMBER);
    }

    public LineGraphic(LineGraphic graphic) {
        super(graphic);
    }

    @Override
    public LineGraphic copy() {
        return new LineGraphic(this);
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
        return super.isShapeValid();
    }

    public Point2D getPtA() {
        return this.ptA;
    }

    public Point2D getPtB() {
        return this.ptB;
    }

    protected Boolean getLineABvalid() {
        return this.lineABvalid;
    }

    public void setHandlePointList(Point2D ptStart, Point2D ptEnd) {
        this.setHandlePoint(0, ptStart == null ? null : (Point2D)ptStart.clone());
        this.setHandlePoint(1, ptEnd == null ? null : (Point2D)ptEnd.clone());
        this.buildShape(null);
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getUIName() {
        return Messages.getString("MeasureToolBar.line");
    }

    @Override
    public int getKeyCode() {
        return ShortcutManager.getInstance().getKeyCode("graphic.line");
    }

    @Override
    public int getModifier() {
        return ShortcutManager.getInstance().getModifier("graphic.line");
    }

    @Override
    public void buildShape(MouseEventDouble mouseEvent) {
        this.updateTool();
        Line2D.Double newShape = null;
        if (this.lineABvalid.booleanValue()) {
            newShape = new Line2D.Double(this.ptA, this.ptB);
        }
        this.setShape(newShape, mouseEvent);
        this.updateLabel(mouseEvent, this.getDefaultView2d(mouseEvent));
    }

    @Override
    public List<MeasureItem> computeMeasurements(MeasurableLayer layer, boolean releaseEvent, Unit displayUnit) {
        MeasurementsAdapter adapter;
        if (layer != null && layer.hasContent() && this.isShapeValid() && (adapter = layer.getMeasurementAdapter(displayUnit)) != null) {
            ArrayList<MeasureItem> measVal = new ArrayList<MeasureItem>();
            if (FIRST_POINT_X.getComputed().booleanValue()) {
                measVal.add(new MeasureItem(FIRST_POINT_X, adapter.getXCalibratedValue(this.ptA.getX()), adapter.unit()));
            }
            if (FIRST_POINT_Y.getComputed().booleanValue()) {
                measVal.add(new MeasureItem(FIRST_POINT_Y, adapter.getYCalibratedValue(this.ptA.getY()), adapter.unit()));
            }
            if (LAST_POINT_X.getComputed().booleanValue()) {
                measVal.add(new MeasureItem(LAST_POINT_X, adapter.getXCalibratedValue(this.ptB.getX()), adapter.unit()));
            }
            if (LAST_POINT_Y.getComputed().booleanValue()) {
                measVal.add(new MeasureItem(LAST_POINT_Y, adapter.getYCalibratedValue(this.ptB.getY()), adapter.unit()));
            }
            if (LINE_LENGTH.getComputed().booleanValue()) {
                measVal.add(new MeasureItem(LINE_LENGTH, this.ptA.distance(this.ptB) * adapter.calibrationRatio(), adapter.unit()));
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

    protected void updateTool() {
        this.ptA = this.getHandlePoint(0);
        this.ptB = this.getHandlePoint(1);
        this.lineABvalid = this.ptA != null && this.ptB != null && !this.ptB.equals(this.ptA);
    }

    public Point2D getStartPoint() {
        this.updateTool();
        return this.ptA == null ? null : (Point2D)this.ptA.clone();
    }

    public Point2D getEndPoint() {
        this.updateTool();
        return this.ptB == null ? null : (Point2D)this.ptB.clone();
    }

    @Override
    public List<Measurement> getMeasurementList() {
        return measurementList;
    }

    static {
        measurementList.add(FIRST_POINT_X);
        measurementList.add(FIRST_POINT_Y);
        measurementList.add(LAST_POINT_X);
        measurementList.add(LAST_POINT_Y);
        measurementList.add(LINE_LENGTH);
        measurementList.add(ORIENTATION);
        measurementList.add(AZIMUTH);
    }
}

