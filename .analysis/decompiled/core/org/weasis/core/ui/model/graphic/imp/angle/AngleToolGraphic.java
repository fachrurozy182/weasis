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
import org.weasis.core.api.gui.util.ShortcutManager;
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

@XmlType(name="angle")
@XmlRootElement(name="angle")
public class AngleToolGraphic
extends AbstractDragGraphic {
    public static final Integer POINTS_NUMBER = 3;
    public static final Icon ICON = ResourceUtil.getIcon(ResourceUtil.ActionIcon.DRAW_ANGLE);
    public static final Measurement ANGLE = new Measurement(Messages.getString("measure.angle"), 1, true);
    public static final Measurement COMPLEMENTARY_ANGLE = new Measurement(Messages.getString("measure.complement_angle"), 2, true, true, false);
    public static final Measurement REFLEX_ANGLE = new Measurement(Messages.getString("AngleToolGraphic.reflex_angle"), 3, true, true, false);
    protected static final List<Measurement> MEASUREMENT_LIST = new ArrayList<Measurement>();
    Point2D ptA;
    Point2D ptO;
    Point2D ptB;
    boolean lineColinear;
    boolean lineOAvalid;
    Boolean lineOBvalid;
    double angleDeg;

    public AngleToolGraphic() {
        super(POINTS_NUMBER);
    }

    public AngleToolGraphic(AngleToolGraphic graphic) {
        super(graphic);
    }

    @Override
    public AngleToolGraphic copy() {
        return new AngleToolGraphic(this);
    }

    protected void init() {
        this.ptA = this.getHandlePoint(0);
        this.ptO = this.getHandlePoint(1);
        this.ptB = this.getHandlePoint(2);
        this.lineColinear = false;
        this.lineOBvalid = false;
        this.lineOAvalid = this.lineOBvalid;
        this.angleDeg = 0.0;
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

    @Override
    public void buildShape(MouseEventDouble mouseEvent) {
        this.updateTool();
        Shape newShape = null;
        Path2D.Double path = new Path2D.Double(1, 2);
        if (this.lineOAvalid) {
            path.append(new Line2D.Double(this.ptA, this.ptO), false);
        }
        if (this.lineOBvalid.booleanValue()) {
            path.append(new Line2D.Double(this.ptO, this.ptB), false);
        }
        if (this.lineOAvalid && this.lineOBvalid.booleanValue() && !this.lineColinear) {
            newShape = new AdvancedShape(this, 2);
            AdvancedShape aShape = (AdvancedShape)newShape;
            aShape.addShape(path);
            double startingAngle = GeomUtil.getAngleDeg(this.ptO, this.ptA);
            double radius = 32.0;
            Rectangle2D.Double arcAngleBounds = new Rectangle2D.Double(this.ptO.getX() - radius, this.ptO.getY() - radius, 2.0 * radius, 2.0 * radius);
            Arc2D.Double arcAngle = new Arc2D.Double(arcAngleBounds, startingAngle, this.angleDeg, 0);
            double rMax = Math.min(this.ptO.distance(this.ptA), this.ptO.distance(this.ptB)) * 2.0 / 3.0;
            double scalingMin = radius / rMax;
            aShape.addScaleInvShape((Shape)arcAngle, this.ptO, scalingMin, true);
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
        return Messages.getString("measure.angle");
    }

    @Override
    public int getKeyCode() {
        return ShortcutManager.getInstance().getKeyCode("graphic.angle");
    }

    @Override
    public int getModifier() {
        return ShortcutManager.getInstance().getModifier("graphic.angle");
    }

    protected void updateTool() {
        this.init();
        this.lineOAvalid = this.ptA != null && this.ptO != null && !this.ptO.equals(this.ptA);
        this.lineOBvalid = this.ptB != null && this.ptO != null && !this.ptO.equals(this.ptB);
        if (this.lineOAvalid && this.lineOBvalid.booleanValue()) {
            this.angleDeg = GeomUtil.getSmallestRotationAngleDeg(GeomUtil.getAngleDeg(this.ptA, this.ptO, this.ptB));
            this.lineColinear = GeomUtil.lineColinear(this.ptO, this.ptA, this.ptO, this.ptB);
        }
    }

    static {
        MEASUREMENT_LIST.add(ANGLE);
        MEASUREMENT_LIST.add(COMPLEMENTARY_ANGLE);
        MEASUREMENT_LIST.add(REFLEX_ANGLE);
    }
}

