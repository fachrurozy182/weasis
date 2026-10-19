/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.xml.bind.annotation.XmlElement
 *  jakarta.xml.bind.annotation.XmlElementWrapper
 *  jakarta.xml.bind.annotation.adapters.XmlAdapter
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.ui.model.graphic;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;
import java.util.Optional;
import org.weasis.core.api.gui.util.GeomUtil;
import org.weasis.core.api.util.FontTools;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.editor.image.dockable.MeasureTool;
import org.weasis.core.ui.model.graphic.GraphicLabel;
import org.weasis.core.ui.model.utils.imp.DefaultGraphicLabel;
import org.weasis.core.util.StringUtil;

public abstract class AbstractGraphicLabel
implements GraphicLabel {
    protected String[] labels;
    protected Rectangle2D labelBounds;
    protected Double labelWidth;
    protected Double labelHeight;
    protected Double offsetX;
    protected Double offsetY;

    protected AbstractGraphicLabel() {
        this(DEFAULT_OFFSET_X, DEFAULT_OFFSET_Y);
    }

    protected AbstractGraphicLabel(Double offsetX, Double offsetY) {
        this.offsetX = Optional.ofNullable(offsetX).orElse(DEFAULT_OFFSET_X);
        this.offsetY = Optional.ofNullable(offsetY).orElse(DEFAULT_OFFSET_Y);
        this.reset();
    }

    protected AbstractGraphicLabel(AbstractGraphicLabel object) {
        this.offsetX = object.offsetX;
        this.offsetY = object.offsetY;
        this.labels = Optional.ofNullable(object.labels).map(rec$ -> (String[])rec$.clone()).orElse(null);
        this.labelBounds = Optional.ofNullable(object.labelBounds).map(Rectangle2D::getBounds2D).orElse(null);
        this.labelWidth = object.labelWidth;
        this.labelHeight = object.labelHeight;
    }

    @Override
    public void reset() {
        this.labels = null;
        this.labelBounds = null;
        this.labelHeight = 0.0;
        this.labelWidth = 0.0;
    }

    @Override
    @XmlElementWrapper(name="labels")
    @XmlElement(name="label")
    public String[] getLabels() {
        return this.labels;
    }

    @Override
    @XmlElement(name="offsetX")
    public Double getOffsetX() {
        return this.offsetX;
    }

    @Override
    @XmlElement(name="offsetY")
    public Double getOffsetY() {
        return this.offsetY;
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    public void setOffsetX(Double offsetX) {
        this.offsetX = offsetX;
    }

    public void setOffsetY(Double offsetY) {
        this.offsetY = offsetY;
    }

    @Override
    public Rectangle2D getBounds(AffineTransform transform) {
        return this.getArea(transform).getBounds2D();
    }

    @Override
    public Area getArea(AffineTransform transform) {
        if (Objects.isNull(this.labelBounds)) {
            return new Area();
        }
        if (Objects.isNull(transform)) {
            return new Area(this.labelBounds);
        }
        AffineTransform invTransform = new AffineTransform();
        Point2D.Double anchorPt = new Point2D.Double(this.labelBounds.getX(), this.labelBounds.getY());
        double scale = GeomUtil.extractScalingFactor(transform);
        double angleRad = GeomUtil.extractAngleRad(transform);
        invTransform.translate(((Point2D)anchorPt).getX(), ((Point2D)anchorPt).getY());
        if (!Objects.equals(scale, 1.0)) {
            invTransform.scale(1.0 / scale, 1.0 / scale);
        }
        if (!Objects.equals(angleRad, 0.0)) {
            invTransform.rotate(-angleRad);
        }
        invTransform.translate(-((Point2D)anchorPt).getX(), -((Point2D)anchorPt).getY());
        if ((transform.getType() & 0x40) != 0) {
            invTransform.translate(0.0, -this.labelBounds.getHeight());
        }
        Area areaBounds = new Area(invTransform.createTransformedShape(this.labelBounds));
        areaBounds.transform(AffineTransform.getTranslateInstance(this.offsetX, this.offsetY));
        return areaBounds;
    }

    @Override
    public Rectangle2D getTransformedBounds(AffineTransform transform) {
        Point2D.Double anchorPoint = new Point2D.Double(this.labelBounds.getX() + this.offsetX, this.labelBounds.getY() + this.offsetY);
        Optional.ofNullable(transform).ifPresent(t -> transform.transform(anchorPoint, anchorPoint));
        return new Rectangle2D.Double(((Point2D)anchorPoint).getX(), ((Point2D)anchorPoint).getY(), this.labelBounds.getWidth(), this.labelBounds.getHeight());
    }

    @Override
    public void setLabel(ViewCanvas<?> view2d, Double xPos, Double yPos, String ... labels) {
        if (labels == null || labels.length == 0) {
            this.reset();
        } else {
            this.labels = labels;
            Font font = MeasureTool.viewSetting.getFont();
            Graphics2D g2d = view2d == null ? null : (Graphics2D)view2d.getJComponent().getGraphics();
            FontRenderContext fontRenderContext = g2d == null ? new FontRenderContext(null, false, false) : g2d.getFontRenderContext();
            this.updateBoundsSize(font, fontRenderContext);
            this.labelBounds = new Rectangle2D.Double(xPos, yPos, this.labelWidth, this.labelHeight * (double)labels.length);
            GeomUtil.growRectangle(this.labelBounds, 3.0);
        }
    }

    protected void updateBoundsSize(Font defaultFont, FontRenderContext fontRenderContext) {
        Objects.requireNonNull(defaultFont);
        Objects.requireNonNull(fontRenderContext);
        if (this.labels == null || this.labels.length == 0) {
            this.reset();
        } else {
            double maxWidth = 0.0;
            for (String label : this.labels) {
                if (!StringUtil.hasText((CharSequence)label)) continue;
                TextLayout layout = new TextLayout(label, defaultFont, fontRenderContext);
                maxWidth = Math.max(layout.getBounds().getWidth(), maxWidth);
            }
            this.labelHeight = new TextLayout("Tg", defaultFont, fontRenderContext).getBounds().getHeight();
            this.labelWidth = maxWidth;
        }
    }

    @Override
    public void move(Double deltaX, Double deltaY) {
        Optional.ofNullable(deltaX).ifPresent(delta -> {
            this.offsetX = this.offsetX + delta;
        });
        Optional.ofNullable(deltaY).ifPresent(delta -> {
            this.offsetY = this.offsetY + delta;
        });
    }

    @Override
    public void paint(Graphics2D g2d, AffineTransform transform, boolean selected) {
        if (this.labels != null && this.labelBounds != null) {
            Paint oldPaint = g2d.getPaint();
            Font oldFont = g2d.getFont();
            Font font = MeasureTool.viewSetting.getFont();
            g2d.setFont(font);
            Point2D.Double pt = new Point2D.Double(this.labelBounds.getX() + this.offsetX, this.labelBounds.getY() + this.offsetY);
            if (transform != null) {
                transform.transform(pt, pt);
            }
            float px = (float)((Point2D)pt).getX() + 3.0f;
            float py = (float)((Point2D)pt).getY() + 3.0f - (float)g2d.getFontMetrics().getDescent() + 1.0f;
            float height = this.labelHeight.floatValue();
            for (String label : this.labels) {
                if (!StringUtil.hasText((CharSequence)label)) continue;
                FontTools.paintColorFontOutline(g2d, label, px, py += height, Color.WHITE);
            }
            if (selected) {
                this.paintBoundOutline(g2d, transform);
            }
            g2d.setPaint(oldPaint);
            g2d.setFont(oldFont);
        }
    }

    protected void paintBoundOutline(Graphics2D g2d, AffineTransform transform) {
        Rectangle2D boundingRect = this.getTransformedBounds(transform);
        Paint oldPaint = g2d.getPaint();
        g2d.setPaint(Color.BLACK);
        g2d.draw(boundingRect);
        GeomUtil.growRectangle(boundingRect, -1.0);
        g2d.setPaint(Color.WHITE);
        g2d.draw(boundingRect);
        g2d.setPaint(oldPaint);
    }

    @Override
    public Rectangle2D getLabelBounds() {
        return this.labelBounds;
    }

    public static class Adapter
    extends XmlAdapter<DefaultGraphicLabel, GraphicLabel> {
        public GraphicLabel unmarshal(DefaultGraphicLabel v) throws Exception {
            return v;
        }

        public DefaultGraphicLabel marshal(GraphicLabel v) throws Exception {
            return (DefaultGraphicLabel)v;
        }
    }
}

