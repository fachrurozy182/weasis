/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.xml.bind.annotation.XmlTransient
 */
package org.weasis.core.ui.model.graphic;

import jakarta.xml.bind.annotation.XmlTransient;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Robot;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.swing.SwingUtilities;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.model.graphic.AbstractGraphic;
import org.weasis.core.ui.model.graphic.DragGraphic;
import org.weasis.core.ui.model.utils.Draggable;
import org.weasis.core.ui.model.utils.imp.DefaultDragSequence;
import org.weasis.core.ui.model.utils.imp.DragLabelSequence;
import org.weasis.core.ui.util.MouseEventDouble;

@XmlTransient
public abstract class AbstractDragGraphic
extends AbstractGraphic
implements DragGraphic {
    private Boolean resizingOrMoving = DEFAULT_RESIZE_OR_MOVING;

    protected AbstractDragGraphic(Integer pointNumber) {
        super(pointNumber);
    }

    protected AbstractDragGraphic(AbstractGraphic graphic) {
        super(graphic);
    }

    @Override
    public void setResizeOrMoving(Boolean value) {
        this.resizingOrMoving = Optional.ofNullable(value).orElse(DEFAULT_RESIZE_OR_MOVING);
    }

    @Override
    public void buildShape() {
        if (this.isShapeValid()) {
            this.buildShape(null);
        }
    }

    @Override
    protected void paintHandles(Graphics2D g2d, AffineTransform transform) {
        if (!this.getResizingOrMoving().booleanValue()) {
            super.paintHandles(g2d, transform);
        }
    }

    @Override
    public Boolean getResizingOrMoving() {
        return Optional.ofNullable(this.resizingOrMoving).orElse(Boolean.FALSE);
    }

    @Override
    public void forceToAddPoints(Integer fromPtIndex) {
        if (this.variablePointsNumber.booleanValue() && fromPtIndex >= 0 && fromPtIndex < this.pts.size()) {
            if (fromPtIndex < this.pts.size() - 1) {
                List<Point2D> list = this.pts.subList(fromPtIndex + 1, this.pts.size());
                for (int i = 0; i <= fromPtIndex; ++i) {
                    list.add((Point2D)this.pts.get(i));
                }
                this.pts = list;
            }
            this.pointNumber = UNDEFINED;
        }
    }

    @Override
    public Point2D removeHandlePoint(Integer index, MouseEventDouble mouseEvent) {
        if (this.variablePointsNumber.booleanValue() && this.pts.size() > 2 && index >= 0 && index < this.pts.size()) {
            Point2D pt = (Point2D)this.pts.remove(index);
            this.pointNumber = this.pts.size();
            this.buildShape(mouseEvent);
            return pt;
        }
        return null;
    }

    @Override
    public void moveMouseOverHandlePoint(Integer handlePtIndex, MouseEventDouble event) {
        ViewCanvas graphPane = this.getDefaultView2d(event);
        if (graphPane != null) {
            Point mousePt;
            Point2D handlePt = null;
            if (handlePtIndex >= 0 && handlePtIndex < this.pts.size()) {
                handlePt = (Point2D)this.pts.get(handlePtIndex);
            }
            if (handlePt != null && (mousePt = graphPane.getMouseCoordinatesFromImage(handlePt.getX(), handlePt.getY())) != null && (event.getX() != mousePt.x || event.getY() != mousePt.y)) {
                try {
                    event.translatePoint(mousePt.x - event.getX(), mousePt.y - event.getY());
                    event.setImageCoordinates(handlePt);
                    SwingUtilities.convertPointToScreen(mousePt, graphPane.getJComponent());
                    new Robot().mouseMove(mousePt.x, mousePt.y);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
    }

    @Override
    public Draggable createMoveDrag() {
        return new DefaultDragSequence(this);
    }

    @Override
    public Draggable createResizeDrag() {
        return this.createResizeDrag(UNDEFINED);
    }

    @Override
    public Draggable createResizeDrag(Integer i) {
        return new DefaultDragSequence(this, i);
    }

    @Override
    public Draggable createDragLabelSequence() {
        return new DragLabelSequence(this);
    }

    @Override
    public Integer moveAndResizeOnDrawing(Integer handlePointIndex, Double deltaX, Double deltaY, MouseEventDouble mouseEvent) {
        int index = handlePointIndex == null ? UNDEFINED : handlePointIndex;
        if (index == UNDEFINED) {
            this.pts.stream().filter(Objects::nonNull).forEach(p -> p.setLocation(p.getX() + deltaX, p.getY() + deltaY));
        } else if (index >= 0 && index < this.pts.size()) {
            Point2D point = (Point2D)this.pts.get(index);
            Optional.ofNullable(point).ifPresent(p -> p.setLocation(mouseEvent.getImageCoordinates()));
        }
        return index;
    }
}

