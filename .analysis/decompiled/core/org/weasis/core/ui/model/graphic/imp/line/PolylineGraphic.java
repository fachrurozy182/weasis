/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.xml.bind.annotation.XmlRootElement
 *  jakarta.xml.bind.annotation.XmlType
 */
package org.weasis.core.ui.model.graphic.imp.line;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.swing.Icon;
import org.weasis.core.Messages;
import org.weasis.core.api.image.measure.MeasurementsAdapter;
import org.weasis.core.api.image.util.MeasurableLayer;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.model.graphic.AbstractDragGraphic;
import org.weasis.core.ui.model.utils.bean.MeasureItem;
import org.weasis.core.ui.model.utils.bean.Measurement;
import org.weasis.core.ui.model.utils.exceptions.InvalidShapeException;
import org.weasis.core.ui.util.MouseEventDouble;

@XmlType(name="polyline")
@XmlRootElement(name="polyline")
public class PolylineGraphic
extends AbstractDragGraphic {
    public static final Integer POINTS_NUMBER = UNDEFINED;
    public static final Icon ICON = ResourceUtil.getIcon(ResourceUtil.ActionIcon.DRAW_POLYLINE);
    public static final Measurement LINE_LENGTH = new Measurement(Messages.getString("measure.length"), 5, true, true, true);
    protected static final List<Measurement> MEASUREMENT_LIST = new ArrayList<Measurement>();

    public PolylineGraphic() {
        super(POINTS_NUMBER);
    }

    public PolylineGraphic(PolylineGraphic graphic) {
        super(graphic);
    }

    @Override
    public PolylineGraphic copy() {
        return new PolylineGraphic(this);
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getUIName() {
        return Messages.getString("MeasureToolBar.polyline");
    }

    @Override
    protected void prepareShape() throws InvalidShapeException {
        this.setPointNumber(this.pts.size());
        this.buildShape(null);
        if (!this.isShapeValid()) {
            int lastPointIndex = this.pts.size() - 1;
            if (lastPointIndex > 0) {
                Point2D checkPoint = (Point2D)this.pts.get(lastPointIndex);
                for (int i = lastPointIndex - 1; i >= 0 && checkPoint.equals(this.pts.get(i)); --i) {
                    this.pts.remove(i);
                }
                this.setPointNumber(this.pts.size());
            }
            if (!this.isShapeValid() || this.pts.size() < 2) {
                throw new IllegalStateException("This Polyline cannot be drawn");
            }
            this.buildShape(null);
        }
    }

    @Override
    public void buildShape(MouseEventDouble mouseEvent) {
        Path2D.Double newShape = null;
        Optional firstHandlePoint = this.pts.stream().findFirst();
        if (firstHandlePoint.isPresent()) {
            Point2D pt;
            Point2D p = (Point2D)firstHandlePoint.get();
            Path2D.Double polygonPath = new Path2D.Double(1, this.pts.size());
            ((Path2D)polygonPath).moveTo(p.getX(), p.getY());
            Iterator iterator = this.pts.iterator();
            while (iterator.hasNext() && (pt = (Point2D)iterator.next()) != null) {
                ((Path2D)polygonPath).lineTo(pt.getX(), pt.getY());
            }
            newShape = polygonPath;
        }
        this.setShape(newShape, mouseEvent);
        this.updateLabel(mouseEvent, this.getDefaultView2d(mouseEvent));
    }

    @Override
    public boolean isShapeValid() {
        if (!this.isGraphicComplete().booleanValue()) {
            return false;
        }
        int lastPointIndex = this.pts.size() - 1;
        if (lastPointIndex > 0) {
            Point2D checkPoint = (Point2D)this.pts.get(lastPointIndex);
            return !Objects.equals(checkPoint, this.pts.get(--lastPointIndex));
        }
        return true;
    }

    @Override
    public List<MeasureItem> computeMeasurements(MeasurableLayer layer, boolean releaseEvent, Unit displayUnit) {
        MeasurementsAdapter adapter;
        if (layer != null && layer.hasContent() && this.isShapeValid() && (adapter = layer.getMeasurementAdapter(displayUnit)) != null) {
            ArrayList<MeasureItem> measVal = new ArrayList<MeasureItem>(5);
            double ratio = adapter.calibrationRatio();
            String unitStr = adapter.unit();
            ArrayList<Point2D> handlePointListcopy = new ArrayList<Point2D>(this.pts.size());
            for (Point2D handlePt : this.pts) {
                if (handlePt == null) continue;
                handlePointListcopy.add((Point2D)handlePt.clone());
            }
            if (LINE_LENGTH.getComputed().booleanValue()) {
                Double val = handlePointListcopy.size() > 1 ? Double.valueOf(this.getPerimeter(handlePointListcopy) * ratio) : null;
                measVal.add(new MeasureItem(LINE_LENGTH, val, unitStr));
            }
            return measVal;
        }
        return Collections.emptyList();
    }

    @Override
    public List<Measurement> getMeasurementList() {
        return MEASUREMENT_LIST;
    }

    protected Double getPerimeter(List<Point2D> handlePointList) {
        if (handlePointList.size() > 1) {
            double perimeter = 0.0;
            Point2D pLast = handlePointList.get(0);
            for (Point2D p2 : handlePointList) {
                perimeter += pLast.distance(p2);
                pLast = p2;
            }
            return perimeter;
        }
        return null;
    }

    @Override
    public void forceToAddPoints(Integer fromPtIndex) {
        if (this.getVariablePointsNumber().booleanValue() && fromPtIndex >= 0 && fromPtIndex < this.pts.size()) {
            if (fromPtIndex < this.pts.size() - 1) {
                this.pts.add(fromPtIndex, this.getHandlePoint(fromPtIndex));
                Integer n = this.pointNumber;
                this.pointNumber = this.pointNumber + 1;
            } else {
                this.setPointNumber(UNDEFINED);
            }
        }
    }

    static {
        MEASUREMENT_LIST.add(LINE_LENGTH);
    }
}

