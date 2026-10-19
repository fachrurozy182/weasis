/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.editor.image;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.RectangularShape;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.weasis.core.api.gui.util.GeomUtil;
import org.weasis.core.ui.editor.image.Canvas;
import org.weasis.core.ui.editor.image.GraphicsPane;
import org.weasis.core.ui.model.GraphicModel;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.utils.imp.DefaultGraphicLabel;

public class PropertyChangeHandler
implements PropertyChangeListener {
    private final Canvas canvas;

    public PropertyChangeHandler(Canvas canvas) {
        this.canvas = Objects.requireNonNull(canvas);
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertychangeevent) {
        Object obj = propertychangeevent.getSource();
        String s = propertychangeevent.getPropertyName();
        if (obj instanceof Graphic) {
            Graphic graph = (Graphic)obj;
            if ("bounds".equals(s)) {
                this.graphicBoundsChanged(graph, (Shape)propertychangeevent.getOldValue(), (Shape)propertychangeevent.getNewValue(), this.canvas.getAffineTransform());
            } else if ("graphicLabel".equals(s)) {
                this.labelBoundsChanged(graph, (DefaultGraphicLabel)propertychangeevent.getOldValue(), (DefaultGraphicLabel)propertychangeevent.getNewValue(), this.canvas.getAffineTransform());
            } else if ("remove".equals(s)) {
                this.removeGraphic(graph);
            } else if ("remove.repaint".equals(s)) {
                this.removeGraphicAndRepaint(graph);
            } else if ("toFront".equals(s)) {
                this.toFront(graph);
            } else if ("toBack".equals(s)) {
                this.toBack(graph);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void toFront(Graphic graphic) {
        List<Graphic> list;
        GraphicModel graphicManager = this.canvas.getGraphicManager();
        List<Graphic> list2 = list = graphicManager.getModels();
        synchronized (list2) {
            for (int i = 0; i < list.size(); ++i) {
                if (!list.get(i).equals(graphic)) continue;
                Collections.rotate(list.subList(i, list.size()), -1);
                break;
            }
        }
        this.canvas.getJComponent().repaint();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void toBack(Graphic graphic) {
        List<Graphic> list;
        GraphicModel graphicManager = this.canvas.getGraphicManager();
        List<Graphic> list2 = list = graphicManager.getModels();
        synchronized (list2) {
            for (int i = 0; i < list.size(); ++i) {
                if (!list.get(i).equals(graphic)) continue;
                Collections.rotate(list.subList(0, i + 1), 1);
                break;
            }
        }
        this.canvas.getJComponent().repaint();
    }

    public void removeGraphicAndRepaint(Graphic graphic) {
        this.removeGraphic(graphic);
        GraphicsPane.repaint(this.canvas, graphic.getTransformedBounds(graphic.getShape(), this.canvas.getAffineTransform()));
    }

    public void removeGraphic(Graphic graphic) {
        GraphicModel graphicManager = this.canvas.getGraphicManager();
        if (graphicManager != null) {
            graphicManager.removeGraphic(graphic);
        }
        graphic.removePropertyChangeListener(this);
    }

    protected Rectangle rectangleUnion(Rectangle rectangle, Rectangle rectangle1) {
        if (rectangle == null) {
            return rectangle1;
        }
        return rectangle1 == null ? rectangle : rectangle.union(rectangle1);
    }

    protected void graphicBoundsChanged(Graphic graphic, Shape oldShape, Shape shape, AffineTransform transform) {
        if (graphic != null) {
            if (oldShape == null) {
                if (shape != null) {
                    Rectangle rect = graphic.getTransformedBounds(shape, transform);
                    GraphicsPane.repaint(this.canvas, rect);
                }
            } else if (shape == null) {
                Rectangle rect = graphic.getTransformedBounds(oldShape, transform);
                GraphicsPane.repaint(this.canvas, rect);
            } else {
                Rectangle rect = this.rectangleUnion(graphic.getTransformedBounds(oldShape, transform), graphic.getTransformedBounds(shape, transform));
                GraphicsPane.repaint(this.canvas, rect);
            }
        }
    }

    protected void labelBoundsChanged(Graphic graphic, DefaultGraphicLabel oldLabel, DefaultGraphicLabel newLabel, AffineTransform transform) {
        if (graphic != null) {
            boolean newNull;
            boolean oldNull = oldLabel == null || oldLabel.getLabels() == null;
            boolean bl = newNull = newLabel == null || newLabel.getLabels() == null;
            if (oldNull) {
                if (!newNull) {
                    Rectangle rect = graphic.getTransformedBounds(newLabel, transform);
                    GeomUtil.growRectangle(rect, 2.0);
                    GraphicsPane.repaint(this.canvas, ((RectangularShape)rect).getBounds());
                }
            } else if (newNull) {
                Rectangle rect = graphic.getTransformedBounds(oldLabel, transform);
                GeomUtil.growRectangle(rect, 2.0);
                GraphicsPane.repaint(this.canvas, ((RectangularShape)rect).getBounds());
            } else {
                Rectangle newRect = graphic.getTransformedBounds(newLabel, transform);
                GeomUtil.growRectangle(newRect, 2.0);
                Rectangle oldRect = graphic.getTransformedBounds(oldLabel, transform);
                GeomUtil.growRectangle(oldRect, 2.0);
                Rectangle rect = this.rectangleUnion(((RectangularShape)oldRect).getBounds(), ((RectangularShape)newRect).getBounds());
                GraphicsPane.repaint(this.canvas, rect);
            }
        }
    }
}

