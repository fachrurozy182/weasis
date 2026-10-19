/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.model.graphic.imp.area;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.swing.Icon;
import org.weasis.core.Messages;
import org.weasis.core.api.image.measure.MeasurementsAdapter;
import org.weasis.core.api.image.util.MeasurableLayer;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.model.graphic.AbstractDragGraphicArea;
import org.weasis.core.ui.model.utils.bean.MeasureItem;
import org.weasis.core.ui.model.utils.bean.Measurement;
import org.weasis.core.ui.model.utils.exceptions.InvalidShapeException;
import org.weasis.core.ui.util.MouseEventDouble;

public class RectangleGraphic
extends AbstractDragGraphicArea {
    public static final Integer POINTS_NUMBER = 8;
    public static final Icon ICON = ResourceUtil.getIcon(ResourceUtil.ActionIcon.DRAW_RECTANGLE);
    public static final Measurement AREA = new Measurement(Messages.getString("measure.area"), 1, true, true, true);
    public static final Measurement PERIMETER = new Measurement(Messages.getString("measure.perimeter"), 2, true, true, false);
    public static final Measurement TOP_LEFT_POINT_X = new Measurement(Messages.getString("measure.topx"), 3, true, true, false);
    public static final Measurement TOP_LEFT_POINT_Y = new Measurement(Messages.getString("measure.topy"), 4, true, true, false);
    public static final Measurement CENTER_X = new Measurement(Messages.getString("measure.centerx"), 5, true, true, false);
    public static final Measurement CENTER_Y = new Measurement(Messages.getString("measure.centery"), 6, true, true, false);
    public static final Measurement WIDTH = new Measurement(Messages.getString("measure.width"), 7, true, true, false);
    public static final Measurement HEIGHT = new Measurement(Messages.getString("measure.height"), 8, true, true, false);
    protected static final List<Measurement> MEASUREMENT_LIST = new ArrayList<Measurement>();

    public RectangleGraphic() {
        super(POINTS_NUMBER);
    }

    public RectangleGraphic(RectangleGraphic graphic) {
        super(graphic);
    }

    @Override
    public RectangleGraphic copy() {
        return new RectangleGraphic(this);
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getUIName() {
        return Messages.getString("MeasureToolBar.rect");
    }

    public RectangleGraphic buildGraphic(Rectangle2D rectangle) throws InvalidShapeException {
        Rectangle2D r = Optional.ofNullable(rectangle).orElseThrow(() -> new InvalidShapeException("Rectangle2D is null!"));
        this.setHandlePointList(r);
        this.prepareShape();
        return this;
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
        if (Objects.equals(handlePointIndex, UNDEFINED)) {
            for (Point2D point : this.pts) {
                if (point == null) continue;
                point.setLocation(point.getX() + deltaX, point.getY() + deltaY);
            }
        } else {
            Rectangle2D.Double rectangle = new Rectangle2D.Double();
            rectangle.setFrameFromDiagonal(this.getHandlePoint(eHandlePoint.NW.index), this.getHandlePoint(eHandlePoint.SE.index));
            double x = ((RectangularShape)rectangle).getX();
            double y = ((RectangularShape)rectangle).getY();
            double w = ((RectangularShape)rectangle).getWidth();
            double h = ((RectangularShape)rectangle).getHeight();
            eHandlePoint pt = eHandlePoint.valueFromIndex(handlePointIndex);
            if (pt.equals((Object)eHandlePoint.W) || pt.equals((Object)eHandlePoint.NW) || pt.equals((Object)eHandlePoint.SW)) {
                x += deltaX.doubleValue();
                w -= deltaX.doubleValue();
            }
            if (pt.equals((Object)eHandlePoint.N) || pt.equals((Object)eHandlePoint.NW) || pt.equals((Object)eHandlePoint.NE)) {
                y += deltaY.doubleValue();
                h -= deltaY.doubleValue();
            }
            if (pt.equals((Object)eHandlePoint.E) || pt.equals((Object)eHandlePoint.NE) || pt.equals((Object)eHandlePoint.SE)) {
                w += deltaX.doubleValue();
            }
            if (pt.equals((Object)eHandlePoint.S) || pt.equals((Object)eHandlePoint.SW) || pt.equals((Object)eHandlePoint.SE)) {
                h += deltaY.doubleValue();
            }
            if (w < 0.0) {
                w = -w;
                x -= w;
                pt = pt.getVerticalMirror();
            }
            if (h < 0.0) {
                h = -h;
                y -= h;
                pt = pt.getHorizontalMirror();
            }
            handlePointIndex = pt.index;
            rectangle.setFrame(x, y, w, h);
            this.setHandlePointList(rectangle);
        }
        return handlePointIndex;
    }

    @Override
    public void buildShape(MouseEventDouble mouseevent) {
        Rectangle2D.Double rectangle = null;
        if (this.pts.size() > 1) {
            Point2D nw = this.getHandlePoint(eHandlePoint.NW.index);
            Point2D se = this.getHandlePoint(eHandlePoint.SE.index);
            if (nw != null && !nw.equals(se)) {
                rectangle = new Rectangle2D.Double();
                rectangle.setFrameFromDiagonal(nw, se);
            }
        }
        this.setShape(rectangle, mouseevent);
        this.updateLabel(mouseevent, this.getDefaultView2d(mouseevent));
    }

    @Override
    public List<Measurement> getMeasurementList() {
        return MEASUREMENT_LIST;
    }

    @Override
    public List<MeasureItem> computeMeasurements(MeasurableLayer layer, boolean releaseEvent, Unit displayUnit) {
        MeasurementsAdapter adapter;
        if (layer != null && layer.hasContent() && this.isShapeValid() && (adapter = layer.getMeasurementAdapter(displayUnit)) != null) {
            List<MeasureItem> stats;
            Double val;
            ArrayList<MeasureItem> measVal = new ArrayList<MeasureItem>();
            Rectangle2D.Double rect = new Rectangle2D.Double();
            rect.setFrameFromDiagonal(this.getHandlePoint(eHandlePoint.NW.index), this.getHandlePoint(eHandlePoint.SE.index));
            double ratio = adapter.calibrationRatio();
            if (TOP_LEFT_POINT_X.getComputed().booleanValue()) {
                measVal.add(new MeasureItem(TOP_LEFT_POINT_X, adapter.getXCalibratedValue(((RectangularShape)rect).getX()), adapter.unit()));
            }
            if (TOP_LEFT_POINT_Y.getComputed().booleanValue()) {
                measVal.add(new MeasureItem(TOP_LEFT_POINT_Y, adapter.getYCalibratedValue(((RectangularShape)rect).getY()), adapter.unit()));
            }
            if (CENTER_X.getComputed().booleanValue()) {
                measVal.add(new MeasureItem(CENTER_X, adapter.getXCalibratedValue(rect.getCenterX()), adapter.unit()));
            }
            if (CENTER_Y.getComputed().booleanValue()) {
                measVal.add(new MeasureItem(CENTER_Y, adapter.getYCalibratedValue(rect.getCenterY()), adapter.unit()));
            }
            if (WIDTH.getComputed().booleanValue()) {
                measVal.add(new MeasureItem(WIDTH, ratio * ((RectangularShape)rect).getWidth(), adapter.unit()));
            }
            if (HEIGHT.getComputed().booleanValue()) {
                measVal.add(new MeasureItem(HEIGHT, ratio * ((RectangularShape)rect).getHeight(), adapter.unit()));
            }
            if (AREA.getComputed().booleanValue()) {
                val = ((RectangularShape)rect).getWidth() * ((RectangularShape)rect).getHeight() * ratio * ratio;
                Object unit = "pix".equals(adapter.unit()) ? adapter.unit() : adapter.unit() + "2";
                measVal.add(new MeasureItem(AREA, val, (String)unit));
            }
            if (PERIMETER.getComputed().booleanValue()) {
                val = (((RectangularShape)rect).getWidth() + ((RectangularShape)rect).getHeight()) * 2.0 * ratio;
                measVal.add(new MeasureItem(PERIMETER, val, adapter.unit()));
            }
            if ((stats = this.getImageStatistics(layer, releaseEvent)) != null) {
                measVal.addAll(stats);
            }
            return measVal;
        }
        return Collections.emptyList();
    }

    protected void setHandlePointList(Rectangle2D rectangle) {
        double x = rectangle.getX();
        double y = rectangle.getY();
        double w = rectangle.getWidth();
        double h = rectangle.getHeight();
        while (this.pts.size() < this.pointNumber) {
            this.pts.add(new Point2D.Double());
        }
        this.setHandlePoint(eHandlePoint.NW.index, new Point2D.Double(x, y));
        this.setHandlePoint(eHandlePoint.N.index, new Point2D.Double(x + w / 2.0, y));
        this.setHandlePoint(eHandlePoint.NE.index, new Point2D.Double(x + w, y));
        this.setHandlePoint(eHandlePoint.E.index, new Point2D.Double(x + w, y + h / 2.0));
        this.setHandlePoint(eHandlePoint.SE.index, new Point2D.Double(x + w, y + h));
        this.setHandlePoint(eHandlePoint.S.index, new Point2D.Double(x + w / 2.0, y + h));
        this.setHandlePoint(eHandlePoint.SW.index, new Point2D.Double(x, y + h));
        this.setHandlePoint(eHandlePoint.W.index, new Point2D.Double(x, y + h / 2.0));
    }

    static {
        MEASUREMENT_LIST.add(TOP_LEFT_POINT_X);
        MEASUREMENT_LIST.add(TOP_LEFT_POINT_Y);
        MEASUREMENT_LIST.add(CENTER_X);
        MEASUREMENT_LIST.add(CENTER_Y);
        MEASUREMENT_LIST.add(WIDTH);
        MEASUREMENT_LIST.add(HEIGHT);
        MEASUREMENT_LIST.add(AREA);
        MEASUREMENT_LIST.add(PERIMETER);
    }

    public static enum eHandlePoint {
        NONE(-1),
        NW(0),
        SE(1),
        NE(2),
        SW(3),
        N(4),
        S(5),
        E(6),
        W(7);

        static final Map<Integer, eHandlePoint> map;
        public final int index;

        private eHandlePoint(int index) {
            this.index = index;
        }

        public int getIndex() {
            return this.index;
        }

        static eHandlePoint valueFromIndex(int index) {
            return Optional.ofNullable(map.get(index)).orElseThrow(() -> new RuntimeException("Not a valid index for a rectangular DragGraphic : " + index));
        }

        eHandlePoint getVerticalMirror() {
            return switch (this.ordinal()) {
                case 1 -> NE;
                case 3 -> NW;
                case 8 -> E;
                case 7 -> W;
                case 4 -> SE;
                case 2 -> SW;
                default -> this;
            };
        }

        eHandlePoint getHorizontalMirror() {
            return switch (this.ordinal()) {
                case 1 -> SW;
                case 4 -> NW;
                case 5 -> S;
                case 6 -> N;
                case 3 -> SE;
                case 2 -> NE;
                default -> this;
            };
        }

        static {
            map = new HashMap<Integer, eHandlePoint>(eHandlePoint.values().length);
            for (eHandlePoint corner : eHandlePoint.values()) {
                map.put(corner.index, corner);
            }
        }
    }
}

