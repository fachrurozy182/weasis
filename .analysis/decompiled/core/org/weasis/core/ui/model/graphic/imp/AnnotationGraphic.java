/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.xml.bind.annotation.XmlAccessType
 *  jakarta.xml.bind.annotation.XmlAccessorType
 *  jakarta.xml.bind.annotation.XmlAttribute
 *  jakarta.xml.bind.annotation.XmlElement
 *  jakarta.xml.bind.annotation.XmlElementWrapper
 *  jakarta.xml.bind.annotation.XmlRootElement
 *  jakarta.xml.bind.annotation.XmlType
 *  jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.ui.model.graphic.imp;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.swing.Icon;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.GeomUtil;
import org.weasis.core.api.gui.util.ShortcutManager;
import org.weasis.core.api.util.FontTools;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.editor.image.dockable.MeasureTool;
import org.weasis.core.ui.model.graphic.AbstractDragGraphic;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.utils.bean.AdvancedShape;
import org.weasis.core.ui.model.utils.exceptions.InvalidShapeException;
import org.weasis.core.ui.serialize.RectangleAdapter;
import org.weasis.core.ui.util.MouseEventDouble;
import org.weasis.core.util.StringUtil;

@XmlType(name="annotation")
@XmlRootElement(name="annotation")
@XmlAccessorType(value=XmlAccessType.NONE)
public class AnnotationGraphic
extends AbstractDragGraphic {
    public static final Integer POINTS_NUMBER = 2;
    public static final Icon ICON = ResourceUtil.getIcon(ResourceUtil.ActionIcon.DRAW_TEXT);
    protected Point2D ptBox;
    protected Point2D ptAnchor;
    protected String[] labels;
    protected Boolean lineABvalid;
    protected Rectangle2D labelBounds;
    protected Double labelWidth;
    protected Double labelHeight;

    public AnnotationGraphic() {
        super(POINTS_NUMBER);
    }

    public AnnotationGraphic(AnnotationGraphic annotationGraphic) {
        super(annotationGraphic);
    }

    @Override
    protected void initCopy(Graphic graphic) {
        super.initCopy(graphic);
        if (graphic instanceof AnnotationGraphic) {
            AnnotationGraphic annotationGraphic = (AnnotationGraphic)graphic;
            this.labels = Optional.ofNullable(annotationGraphic.labels).map(rec$ -> (String[])rec$.clone()).orElse(null);
            this.labelBounds = Optional.ofNullable(annotationGraphic.labelBounds).map(Rectangle2D::getBounds2D).orElse(null);
            this.labelWidth = annotationGraphic.labelWidth;
            this.labelHeight = annotationGraphic.labelHeight;
        }
    }

    @Override
    public AnnotationGraphic copy() {
        return new AnnotationGraphic(this);
    }

    @Override
    protected void prepareShape() throws InvalidShapeException {
        if (!this.isShapeValid()) {
            throw new InvalidShapeException("This shape cannot be drawn");
        }
    }

    protected void setHandlePointList(Point2D ptAnchor, Point2D ptBox) {
        Point2D pt2 = ptBox == null && ptAnchor != null ? ptAnchor : ptBox;
        Point2D pt1 = pt2 != null && pt2.equals(ptAnchor) ? null : ptAnchor;
        this.setHandlePoint(0, pt1 == null ? null : (Point2D)pt1.clone());
        this.setHandlePoint(1, pt2 == null ? null : (Point2D)pt2.clone());
        this.buildShape(null);
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getUIName() {
        return Messages.getString("Tools.Anno");
    }

    @XmlElementWrapper(name="labels")
    @XmlElement(name="label")
    public String[] getLabels() {
        return this.labels;
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    @XmlElement(name="labelBounds")
    @XmlJavaTypeAdapter(value=RectangleAdapter.Rectangle2DAdapter.class)
    public Rectangle2D getLabelBounds() {
        return this.labelBounds;
    }

    public void setLabelBounds(Rectangle2D labelBounds) {
        this.labelBounds = labelBounds;
    }

    @XmlAttribute(name="labelWidth")
    public Double getLabelWidth() {
        return this.labelWidth;
    }

    public void setLabelWidth(Double labelWidth) {
        this.labelWidth = labelWidth;
    }

    @XmlAttribute(name="labelHeight")
    public Double getLabelHeight() {
        return this.labelHeight;
    }

    public void setLabelHeight(Double labelHeight) {
        this.labelHeight = labelHeight;
    }

    @Override
    public void updateLabel(ViewCanvas<?> view2d, Point2D pos, boolean releasedEvent) {
        this.setLabel(this.labels, view2d, pos);
    }

    @Override
    public void updateLabel(Object source, ViewCanvas<?> view2d) {
        this.setLabel(this.labels, view2d);
    }

    @Override
    public void buildShape(MouseEventDouble mouseEvent) {
        this.updateTool();
        AdvancedShape newShape = null;
        if (this.ptBox != null) {
            ViewCanvas view = this.getDefaultView2d(mouseEvent);
            if (this.labels == null) {
                if (view != null) {
                    this.setLabel(new String[]{this.getInitialText(view)}, view, this.ptBox);
                    return;
                }
                if (this.labelHeight == 0.0 || this.labelWidth == 0.0) {
                    this.fireRemoveAction();
                    return;
                }
            }
            newShape = new AdvancedShape(this, 2);
            Line2D.Double line = null;
            if (this.lineABvalid.booleanValue()) {
                line = new Line2D.Double(this.ptBox, this.ptAnchor);
            }
            this.labelBounds = new Rectangle2D.Double();
            this.labelBounds.setFrameFromCenter(this.ptBox.getX(), this.ptBox.getY(), this.ptBox.getX() + this.labelWidth / 2.0 + 3.0, this.ptBox.getY() + this.labelHeight * (double)(this.labels == null ? 1 : this.labels.length) / 2.0 + 3.0);
            GeomUtil.growRectangle(this.labelBounds, 3.0);
            if (line != null) {
                newShape.addLinkSegmentToInvariantShape(line, this.ptBox, this.labelBounds, this.getDashStroke(this.lineThickness), false);
                AdvancedShape.ScaleInvariantShape arrow = newShape.addScaleInvShape(GeomUtil.getArrowShape(this.ptAnchor, this.ptBox, 15.0, 8.0), this.ptAnchor, this.getStroke(this.lineThickness), false);
                arrow.setFilled(true);
            }
            newShape.addAllInvShape(this.labelBounds, this.ptBox, this.getStroke(this.lineThickness), false);
        }
        this.setShape(newShape, mouseEvent);
    }

    @Override
    public int getKeyCode() {
        return ShortcutManager.getInstance().getKeyCode("graphic.annotation");
    }

    @Override
    public int getModifier() {
        return ShortcutManager.getInstance().getModifier("graphic.annotation");
    }

    protected void updateTool() {
        this.ptAnchor = this.getHandlePoint(0);
        this.ptBox = this.getHandlePoint(1);
        this.lineABvalid = this.ptAnchor != null && !this.ptAnchor.equals(this.ptBox);
    }

    protected String getInitialText(ViewCanvas<?> view) {
        return Messages.getString("AnnotationGraphic.text_box");
    }

    @Override
    public void paintLabel(Graphics2D g2d, AffineTransform transform) {
        if (this.labelVisible.booleanValue() && this.labels != null && this.labelBounds != null) {
            Paint oldPaint = g2d.getPaint();
            Rectangle2D rect = this.labelBounds;
            Point2D.Double pt = new Point2D.Double(rect.getCenterX(), rect.getCenterY());
            if (transform != null) {
                transform.transform(pt, pt);
            }
            float px = (float)(((Point2D)pt).getX() - rect.getWidth() / 2.0 + 3.0);
            float py = (float)(((Point2D)pt).getY() - rect.getHeight() / 2.0 + 3.0);
            float height = this.labelHeight.floatValue();
            g2d.setFont(MeasureTool.viewSetting.getFont());
            for (String label : this.labels) {
                if (!StringUtil.hasText((CharSequence)label)) continue;
                FontTools.paintColorFontOutline(g2d, label, px, py += height, Color.WHITE);
            }
            g2d.setPaint(oldPaint);
        }
    }

    @Override
    public Area getArea(AffineTransform transform) {
        if (this.shape == null) {
            return new Area();
        }
        Shape shape = this.shape;
        if (shape instanceof AdvancedShape) {
            AdvancedShape s = (AdvancedShape)shape;
            Area area = s.getArea(transform);
            List<AdvancedShape.BasicShape> list = s.getShapeList();
            if (!list.isEmpty()) {
                AdvancedShape.BasicShape b = list.get(list.size() - 1);
                area.add(new Area(b.getRealShape()));
            }
            return area;
        }
        return super.getArea(transform);
    }

    public Point2D getAnchorPoint() {
        this.updateTool();
        return this.ptAnchor == null ? null : (Point2D)this.ptAnchor.clone();
    }

    public Point2D getBoxPoint() {
        this.updateTool();
        return this.ptBox == null ? null : (Point2D)this.ptBox.clone();
    }

    protected void reset() {
        this.labels = null;
        this.labelBounds = null;
        this.labelHeight = this.labelWidth = Double.valueOf(0.0);
    }

    @Override
    public void setLabel(String[] labels, ViewCanvas<?> view2d) {
        Point2D pt = this.getBoxPoint();
        if (pt == null) {
            pt = this.getAnchorPoint();
        }
        if (pt != null) {
            this.setLabel(labels, view2d, pt);
        }
    }

    @Override
    public void setLabel(String[] labels, ViewCanvas<?> view2d, Point2D pos) {
        if (view2d == null || labels == null || labels.length == 0 || pos == null) {
            this.reset();
        } else {
            this.labels = labels;
            Font defaultFont = MeasureTool.viewSetting.getFont();
            Graphics2D g2d = (Graphics2D)view2d.getJComponent().getGraphics();
            FontRenderContext fontRenderContext = g2d == null ? new FontRenderContext(null, false, false) : g2d.getFontRenderContext();
            this.updateBoundsSize(defaultFont, fontRenderContext);
            this.labelBounds = new Rectangle2D.Double();
            this.labelBounds.setFrameFromCenter(pos.getX(), pos.getY(), this.ptBox.getX() + this.labelWidth / 2.0 + 3.0, this.ptBox.getY() + this.labelHeight * (double)this.labels.length / 2.0 + 3.0);
            GeomUtil.growRectangle(this.labelBounds, 3.0);
        }
        this.buildShape(null);
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
            this.labelHeight = new TextLayout("Tg", defaultFont, fontRenderContext).getBounds().getHeight() + 2.0;
            this.labelWidth = maxWidth;
        }
    }
}

