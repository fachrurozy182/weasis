/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.MathUtil
 */
package org.weasis.core.ui.model.utils.bean;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.GeomUtil;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.util.MathUtil;

public class AdvancedShape
implements Shape {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdvancedShape.class);
    public final List<BasicShape> shapeList;
    protected AffineTransform transform;
    private final Graphic graphic;

    public AdvancedShape(Graphic graphic, int initialShapeNumber) {
        this.graphic = Objects.requireNonNull(graphic, "Graphic cannot be null!");
        this.shapeList = new ArrayList<BasicShape>(initialShapeNumber);
    }

    public List<BasicShape> getShapeList() {
        return this.shapeList;
    }

    public BasicShape addShape(Shape shape) {
        BasicShape s = new BasicShape(this, shape);
        this.shapeList.add(s);
        return s;
    }

    public BasicShape addShape(Shape shape, Stroke stroke, boolean fixedLineWidth) {
        BasicShape s = new BasicShape(this, shape, stroke, fixedLineWidth);
        this.shapeList.add(s);
        return s;
    }

    ScaleInvariantShape addScaleInvShape(Shape shape, Point2D anchorPoint) {
        return this.addScaleInvShape(shape, anchorPoint, this.graphic.getStroke(this.graphic.getLineThickness()), false);
    }

    public ScaleInvariantShape addScaleInvShape(Shape shape, Point2D anchorPoint, double scalingMin, boolean fixedLineWidth) {
        return this.addScaleInvShape(shape, anchorPoint, scalingMin, this.graphic.getStroke(this.graphic.getLineThickness()), fixedLineWidth);
    }

    public InvariantShape addAllInvShape(Shape shape, Point2D anchorPoint, Stroke stroke, boolean fixedLineWidth) {
        InvariantShape s = new InvariantShape(this, shape, stroke, anchorPoint, fixedLineWidth);
        this.shapeList.add(s);
        return s;
    }

    public LinkSegmentToInvariantShape addLinkSegmentToInvariantShape(Line2D line, Point2D anchorPoint, Shape invShape, Stroke stroke, boolean fixedLineWidth) {
        LinkSegmentToInvariantShape s = new LinkSegmentToInvariantShape(this, line, stroke, anchorPoint, invShape, fixedLineWidth);
        this.shapeList.add(s);
        return s;
    }

    public ScaleInvariantShape addScaleInvShape(Shape shape, Point2D anchorPoint, double scalingMin, Stroke stroke, boolean fixedLineWidth) {
        ScaleInvariantShape s = new ScaleInvariantShape(this, shape, stroke, anchorPoint, scalingMin, fixedLineWidth);
        this.shapeList.add(s);
        return s;
    }

    public ScaleInvariantShape addScaleInvShape(Shape shape, Point2D anchorPoint, Stroke stroke, boolean fixedLineWidth) {
        ScaleInvariantShape s = new ScaleInvariantShape(this, shape, stroke, anchorPoint, fixedLineWidth);
        this.shapeList.add(s);
        return s;
    }

    public void setAffineTransform(AffineTransform transform) {
        this.transform = transform;
    }

    public void paint(Graphics2D g2d, AffineTransform transform) {
        this.setAffineTransform(transform);
        Paint oldPaint = g2d.getPaint();
        Stroke oldStroke = g2d.getStroke();
        Paint paint = this.graphic.getColorPaint();
        boolean filled = this.graphic.getFilled();
        for (BasicShape item : this.shapeList) {
            Paint paint2;
            Paint itemPaint;
            Shape drawingShape;
            if (!item.isVisible() || (drawingShape = item.getRealShape()) == null) continue;
            if (transform != null) {
                drawingShape = transform.createTransformedShape(drawingShape);
            }
            g2d.setPaint((itemPaint = item.getColorPaint()) == null ? paint : itemPaint);
            g2d.setStroke(item.stroke);
            g2d.draw(drawingShape);
            Boolean itemFilled = item.getFilled();
            if (!(itemFilled == null ? filled : itemFilled != false)) continue;
            float fillOpacity = this.graphic.getFillOpacity().floatValue();
            if (fillOpacity < 1.0f && (paint2 = g2d.getPaint()) instanceof Color) {
                Color color = (Color)paint2;
                int alpha = (int)(fillOpacity * (float)color.getAlpha());
                g2d.setPaint(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            }
            g2d.fill(drawingShape);
        }
        g2d.setPaint(oldPaint);
        g2d.setStroke(oldStroke);
    }

    public Shape getGeneralShape() {
        BasicShape s;
        if (!this.shapeList.isEmpty() && (s = this.shapeList.get(0)) != null) {
            return s.getRealShape();
        }
        return null;
    }

    @Override
    public Rectangle getBounds() {
        Rectangle rectangle = null;
        for (BasicShape item : this.shapeList) {
            Shape realShape = item.getRealShape();
            Rectangle bounds = realShape != null ? realShape.getBounds() : null;
            if (bounds == null) continue;
            if (rectangle == null) {
                rectangle = bounds;
                continue;
            }
            rectangle.add(bounds);
        }
        return rectangle;
    }

    @Override
    public Rectangle2D getBounds2D() {
        Rectangle2D rectangle = null;
        for (BasicShape item : this.shapeList) {
            Shape realShape = item.getRealShape();
            Rectangle2D bounds = realShape != null ? realShape.getBounds2D() : null;
            if (bounds == null) continue;
            if (rectangle == null) {
                rectangle = bounds;
                continue;
            }
            rectangle.add(bounds);
        }
        return rectangle;
    }

    @Override
    public boolean contains(double x, double y) {
        for (BasicShape item : this.shapeList) {
            Shape realShape = item.getRealShape();
            if (realShape == null || !realShape.contains(x, y)) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean contains(Point2D p) {
        for (BasicShape item : this.shapeList) {
            Shape realShape = item.getRealShape();
            if (realShape == null || !realShape.contains(p)) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        for (BasicShape item : this.shapeList) {
            Shape realShape = item.getRealShape();
            if (realShape == null || !realShape.contains(x, y, w, h)) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean contains(Rectangle2D r) {
        for (BasicShape item : this.shapeList) {
            Shape realShape = item.getRealShape();
            if (realShape == null || !realShape.contains(r)) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        for (BasicShape item : this.shapeList) {
            Shape realShape = item.getRealShape();
            if (realShape == null || !realShape.intersects(x, y, w, h)) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        for (BasicShape item : this.shapeList) {
            Shape realShape = item.getRealShape();
            if (realShape == null || !realShape.intersects(r)) continue;
            return true;
        }
        return false;
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        if (at != null) {
            this.setAffineTransform(at);
        }
        return this.getFullPathShape().getPathIterator(at);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        if (at != null) {
            this.setAffineTransform(at);
        }
        return this.getFullPathShape().getPathIterator(at, flatness);
    }

    private Path2D getFullPathShape() {
        Path2D.Double pathShape = new Path2D.Double(1);
        for (BasicShape item : this.shapeList) {
            Shape realShape = item.getRealShape();
            if (realShape == null) continue;
            pathShape.append(realShape, false);
        }
        return pathShape;
    }

    public Area getArea(AffineTransform transform) {
        this.setAffineTransform(transform);
        double scalingFactor = GeomUtil.extractScalingFactor(transform);
        double growingSize = (double)this.graphic.getHandleSize().intValue() * 2.0 / scalingFactor;
        BasicStroke boundingStroke = new BasicStroke((float)growingSize, 1, 1);
        Area pathBoundingArea = new Area();
        for (BasicShape item : this.shapeList) {
            Area strokedArea = null;
            try {
                Shape realShape = item.getRealShape();
                if (realShape != null) {
                    Shape strokedShape = boundingStroke.createStrokedShape(realShape);
                    strokedArea = new Area(strokedShape);
                }
            }
            catch (Exception e) {
                LOGGER.error("This shape cannot be drawn, the graphic is deleted.", (Throwable)e);
                this.graphic.fireRemoveAction();
            }
            if (strokedArea == null) continue;
            pathBoundingArea.add(new Area(strokedArea));
        }
        return pathBoundingArea;
    }

    public class BasicShape {
        final Shape shape;
        final boolean fixedLineWidth;
        Stroke stroke;
        boolean visible;
        Boolean filled;
        Paint colorPaint;

        public BasicShape(AdvancedShape this$0, Shape shape) {
            this(this$0, shape, this$0.graphic.getStroke(this$0.graphic.getLineThickness()), false);
        }

        public BasicShape(AdvancedShape this$0, Shape shape, Stroke stroke, boolean fixedLineWidth) {
            Objects.requireNonNull(this$0);
            if (shape == null || stroke == null) {
                throw new IllegalArgumentException();
            }
            this.shape = shape;
            this.stroke = stroke;
            this.fixedLineWidth = fixedLineWidth;
            this.visible = true;
            this.filled = null;
            this.colorPaint = null;
        }

        public Paint getColorPaint() {
            return this.colorPaint;
        }

        public void setColorPaint(Paint colorPaint) {
            this.colorPaint = colorPaint;
        }

        public Boolean getFilled() {
            return this.filled;
        }

        public Shape getRealShape() {
            return this.shape;
        }

        public void changeLineThickness(float width) {
            BasicStroke s;
            Stroke stroke;
            if (!this.fixedLineWidth && (stroke = this.stroke) instanceof BasicStroke && MathUtil.isDifferent((float)(s = (BasicStroke)stroke).getLineWidth(), (float)width)) {
                this.stroke = new BasicStroke(width, s.getEndCap(), s.getLineJoin(), s.getMiterLimit(), s.getDashArray(), s.getDashPhase());
            }
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public boolean isVisible() {
            return this.visible;
        }

        public Shape getShape() {
            return this.shape;
        }

        public boolean isFixedLineWidth() {
            return this.fixedLineWidth;
        }

        public Stroke getStroke() {
            return this.stroke;
        }

        public void setStroke(Stroke stroke) {
            this.stroke = stroke;
        }

        public void setFilled(boolean filled) {
            this.filled = filled;
        }
    }

    public class ScaleInvariantShape
    extends BasicShape {
        final Point2D anchorPoint;
        final double scalingMin;
        final /* synthetic */ AdvancedShape this$0;

        public ScaleInvariantShape(AdvancedShape this$0, Shape shape, Stroke stroke, Point2D anchorPoint, boolean fixedLineWidth) {
            this(this$0, shape, stroke, anchorPoint, 0.0, fixedLineWidth);
        }

        public ScaleInvariantShape(AdvancedShape this$0, Shape shape, Stroke stroke, Point2D anchorPoint, double scalingMin, boolean fixedLineWidth) {
            AdvancedShape advancedShape = this$0;
            Objects.requireNonNull(advancedShape);
            this.this$0 = advancedShape;
            super(this$0, shape, stroke, fixedLineWidth);
            if (anchorPoint == null) {
                throw new IllegalArgumentException();
            }
            if (scalingMin < 0.0) {
                throw new IllegalArgumentException();
            }
            this.anchorPoint = (Point2D)anchorPoint.clone();
            this.scalingMin = scalingMin;
        }

        @Override
        public Shape getRealShape() {
            double scalingFactor = GeomUtil.extractScalingFactor(this.this$0.transform);
            double scale = Math.max(scalingFactor, this.scalingMin);
            return MathUtil.isDifferentFromZero((double)scale) ? GeomUtil.getScaledShape(this.shape, 1.0 / scale, this.anchorPoint) : null;
        }
    }

    public class InvariantShape
    extends BasicShape {
        final Point2D anchorPoint;
        final /* synthetic */ AdvancedShape this$0;

        public InvariantShape(AdvancedShape this$0, Shape shape, Stroke stroke, Point2D anchorPoint, boolean fixedLineWidth) {
            AdvancedShape advancedShape = this$0;
            Objects.requireNonNull(advancedShape);
            this.this$0 = advancedShape;
            super(this$0, shape, stroke, fixedLineWidth);
            if (anchorPoint == null) {
                throw new IllegalArgumentException();
            }
            this.anchorPoint = (Point2D)anchorPoint.clone();
        }

        @Override
        public Shape getRealShape() {
            if (this.this$0.transform == null) {
                return this.shape;
            }
            AffineTransform invTransform = new AffineTransform();
            double scale = GeomUtil.extractScalingFactor(this.this$0.transform);
            double angleRad = GeomUtil.extractAngleRad(this.this$0.transform);
            boolean scaled = MathUtil.isDifferent((double)scale, (double)1.0);
            boolean rotated = MathUtil.isDifferentFromZero((double)angleRad);
            invTransform.translate(this.anchorPoint.getX(), this.anchorPoint.getY());
            if (scaled) {
                invTransform.scale(1.0 / scale, 1.0 / scale);
            }
            if (rotated) {
                invTransform.rotate(-angleRad);
            }
            if ((this.this$0.transform.getType() & 0x40) != 0) {
                invTransform.scale(-1.0, -1.0);
            }
            invTransform.translate(-this.anchorPoint.getX(), -this.anchorPoint.getY());
            return invTransform.createTransformedShape(this.shape);
        }
    }

    public class LinkSegmentToInvariantShape
    extends BasicShape {
        final Point2D anchorPoint;
        final Shape invShape;
        final /* synthetic */ AdvancedShape this$0;

        public LinkSegmentToInvariantShape(AdvancedShape this$0, Line2D line, Stroke stroke, Point2D anchorPoint, Shape invShape, boolean fixedLineWidth) {
            AdvancedShape advancedShape = this$0;
            Objects.requireNonNull(advancedShape);
            this.this$0 = advancedShape;
            super(this$0, line, stroke, fixedLineWidth);
            if (anchorPoint == null) {
                throw new IllegalArgumentException();
            }
            this.anchorPoint = (Point2D)anchorPoint.clone();
            this.invShape = invShape;
        }

        @Override
        public Shape getRealShape() {
            if (this.this$0.transform == null) {
                return this.shape;
            }
            Line2D line = (Line2D)this.shape;
            AffineTransform invTransform = new AffineTransform();
            double scale = GeomUtil.extractScalingFactor(this.this$0.transform);
            double angleRad = GeomUtil.extractAngleRad(this.this$0.transform);
            boolean scaled = MathUtil.isDifferent((double)scale, (double)1.0);
            boolean rotated = MathUtil.isDifferentFromZero((double)angleRad);
            invTransform.translate(this.anchorPoint.getX(), this.anchorPoint.getY());
            if (scaled) {
                invTransform.scale(1.0 / scale, 1.0 / scale);
            }
            if (rotated) {
                invTransform.rotate(-angleRad);
            }
            invTransform.translate(-this.anchorPoint.getX(), -this.anchorPoint.getY());
            Point2D p = null;
            Shape shape = this.invShape;
            if (shape instanceof Rectangle2D) {
                Rectangle2D rectangle2D = (Rectangle2D)shape;
                AffineTransform tr = new AffineTransform();
                tr.translate(this.anchorPoint.getX(), this.anchorPoint.getY());
                if (scaled) {
                    tr.scale(scale, scale);
                }
                if (rotated) {
                    tr.rotate(angleRad);
                }
                tr.translate(-this.anchorPoint.getX(), -this.anchorPoint.getY());
                Point2D p2 = line.getP2();
                tr.transform(p2, p2);
                p = GeomUtil.getIntersectPoint((Line2D)new Line2D.Double(this.anchorPoint, p2), rectangle2D);
            }
            Point2D invpt = invTransform.transform(p == null ? line.getP1() : p, null);
            return new Line2D.Double(invpt, line.getP2());
        }
    }
}

