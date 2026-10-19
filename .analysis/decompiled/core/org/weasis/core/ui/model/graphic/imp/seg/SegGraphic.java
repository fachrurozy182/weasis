/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.opencv.seg.RegionAttributes
 */
package org.weasis.core.ui.model.graphic.imp.seg;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Objects;
import org.weasis.core.api.image.util.MeasurableLayer;
import org.weasis.core.ui.model.graphic.AbstractGraphic;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.graphic.GraphicArea;
import org.weasis.core.ui.model.graphic.imp.seg.SegContour;
import org.weasis.core.ui.model.graphic.imp.seg.SegRegion;
import org.weasis.core.ui.model.utils.bean.MeasureItem;
import org.weasis.core.ui.model.utils.exceptions.InvalidShapeException;
import org.weasis.core.ui.util.MouseEventDouble;
import org.weasis.opencv.seg.RegionAttributes;

public class SegGraphic
extends AbstractGraphic
implements GraphicArea {
    public static final float HIGHLIGHT_EXTRA_THICKNESS = 2.0f;
    private Stroke stroke;
    private SegContour contour;
    private Rectangle2D outlineBounds;
    private boolean highlighted;

    public SegGraphic(Shape path) {
        this(path, null);
    }

    public SegGraphic(Shape path, Stroke stroke) {
        super(0);
        this.stroke = stroke;
        this.setShape(path, null);
        this.outlineBounds = path == null ? null : path.getBounds2D();
        this.updateLabel(null, null);
    }

    public SegGraphic(SegGraphic graphic) {
        super(graphic);
    }

    @Override
    protected void initCopy(Graphic graphic) {
        super.initCopy(graphic);
        if (graphic instanceof SegGraphic) {
            SegGraphic other = (SegGraphic)graphic;
            this.stroke = other.stroke;
            this.contour = other.contour;
            this.shape = other.shape;
            this.outlineBounds = other.outlineBounds;
        }
    }

    @Override
    public SegGraphic copy() {
        return new SegGraphic(this);
    }

    @Override
    public void setFilled(Boolean filled) {
        if (!Objects.equals(this.filled, filled)) {
            this.filled = filled == null ? DEFAULT_FILLED : filled;
            this.fireDrawingChanged();
        }
    }

    @Override
    protected void prepareShape() throws InvalidShapeException {
        if (!this.isShapeValid()) {
            throw new InvalidShapeException("This shape cannot be drawn");
        }
        this.buildShape();
    }

    @Override
    public void buildShape() {
        this.updateLabel(null, null);
    }

    @Override
    public String getUIName() {
        return "";
    }

    @Override
    public boolean isOnGraphicLabel(MouseEventDouble mouseevent) {
        return false;
    }

    @Override
    public String getDescription() {
        return this.getUIName();
    }

    @Override
    public Area getArea(AffineTransform transform) {
        return new Area();
    }

    public Stroke getStroke() {
        return this.stroke;
    }

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }

    public SegContour getContour() {
        return this.contour;
    }

    public void setContour(SegContour contour) {
        this.contour = contour;
    }

    public SegRegion<?> getRegion() {
        SegRegion region;
        RegionAttributes regionAttributes;
        return this.contour != null && (regionAttributes = this.contour.getAttributes()) instanceof SegRegion ? (region = (SegRegion)regionAttributes) : null;
    }

    public boolean isHighlighted() {
        return this.highlighted;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    public boolean isOnOutline(Point2D p, double tolerance) {
        if (this.shape == null || this.outlineBounds == null || !this.isInBounds(p, tolerance)) {
            return false;
        }
        Rectangle2D.Double box = new Rectangle2D.Double(p.getX() - tolerance, p.getY() - tolerance, tolerance * 2.0, tolerance * 2.0);
        if (!this.shape.intersects(box)) {
            return false;
        }
        return this.getFilled() != false || !this.shape.contains(box);
    }

    private boolean isInBounds(Point2D p, double tolerance) {
        return p.getX() >= this.outlineBounds.getMinX() - tolerance && p.getX() <= this.outlineBounds.getMaxX() + tolerance && p.getY() >= this.outlineBounds.getMinY() - tolerance && p.getY() <= this.outlineBounds.getMaxY() + tolerance;
    }

    @Override
    public Stroke getStroke(Float lineThickness) {
        if (this.highlighted) {
            float width = (lineThickness == null ? DEFAULT_LINE_THICKNESS : lineThickness).floatValue() + 2.0f;
            return new BasicStroke(width, 1, 1);
        }
        return this.stroke != null ? this.stroke : super.getStroke(lineThickness);
    }

    @Override
    public List<MeasureItem> getImageStatistics(MeasurableLayer layer, Boolean releaseEvent) {
        return null;
    }
}

