/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.MathUtil
 */
package org.weasis.core.ui.model.utils.imp;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.weasis.core.ui.dialog.MeasureDialog;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.model.graphic.AbstractDragGraphic;
import org.weasis.core.ui.model.graphic.DragGraphic;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.utils.Draggable;
import org.weasis.core.ui.util.ColorLayerUI;
import org.weasis.core.ui.util.MouseEventDouble;
import org.weasis.core.util.MathUtil;

public class DefaultDragSequence
implements Draggable {
    protected final DragGraphic graphic;
    private final Point2D lastPoint;
    private Integer handlePointIndex;

    public DefaultDragSequence(DragGraphic graphic) {
        this(graphic, Graphic.UNDEFINED);
    }

    public DefaultDragSequence(DragGraphic graphic, Integer handlePointIndex) {
        this.graphic = Objects.requireNonNull(graphic);
        this.handlePointIndex = handlePointIndex;
        this.lastPoint = new Point2D.Double();
    }

    @Override
    public void startDrag(MouseEventDouble evt) {
        this.graphic.setResizeOrMoving(Boolean.TRUE);
        this.lastPoint.setLocation(evt.getImageX(), evt.getImageY());
        if (!this.graphic.isGraphicComplete().booleanValue()) {
            if (this.graphic.getPts().isEmpty()) {
                this.graphic.getPts().add(evt.getImageCoordinates());
            }
            if (!this.graphic.isGraphicComplete().booleanValue()) {
                this.graphic.getPts().add(evt.getImageCoordinates());
            }
            this.handlePointIndex = this.graphic.getPts().size() - 1;
        }
    }

    @Override
    public void drag(MouseEventDouble evt) {
        double deltaX = evt.getImageX() - this.lastPoint.getX();
        double deltaY = evt.getImageY() - this.lastPoint.getY();
        if (MathUtil.isDifferentFromZero((double)deltaX) || MathUtil.isDifferentFromZero((double)deltaY)) {
            this.lastPoint.setLocation(evt.getImageCoordinates());
            this.handlePointIndex = this.graphic.moveAndResizeOnDrawing(this.handlePointIndex, deltaX, deltaY, evt);
            this.graphic.buildShape(evt);
            this.graphic.setResizeOrMoving(Boolean.TRUE);
        }
    }

    @Override
    public Boolean completeDrag(MouseEventDouble mouseEvent) {
        if (mouseEvent != null) {
            if (!this.graphic.isGraphicComplete().booleanValue()) {
                if (Objects.equals(this.graphic.getPtsNumber(), Graphic.UNDEFINED) && mouseEvent.getClickCount() == 2 && !mouseEvent.isConsumed()) {
                    List<Point2D> handlePointList = this.graphic.getPts();
                    if (!this.graphic.isLastPointValid().booleanValue()) {
                        handlePointList.remove(handlePointList.size() - 1);
                    }
                    this.graphic.setPointNumber(handlePointList.size());
                } else if (this.graphic.isLastPointValid().booleanValue()) {
                    this.graphic.getPts().add(mouseEvent.getImageCoordinates());
                    this.handlePointIndex = this.graphic.getPts().size() - 1;
                }
            } else if (this.graphic.getShape() != null && this.graphic.isShapeValid()) {
                ViewCanvas graphPane;
                this.graphic.setResizeOrMoving(Boolean.FALSE);
                this.graphic.setShape(null, mouseEvent);
                this.graphic.buildShape(mouseEvent);
                if (mouseEvent.getClickCount() == 2 && !mouseEvent.isConsumed() && (graphPane = this.graphic.getDefaultView2d(mouseEvent)) != null) {
                    List<DragGraphic> selectedDragGraphList;
                    AbstractDragGraphic dragGraph;
                    Graphic graphic;
                    boolean isEditingGraph = false;
                    Optional<Graphic> first = graphPane.getGraphicManager().getFirstGraphicIntersecting(mouseEvent);
                    if (first.isPresent() && (graphic = first.get()) instanceof AbstractDragGraphic && (dragGraph = (AbstractDragGraphic)graphic).getSelected().booleanValue() && dragGraph.getVariablePointsNumber().booleanValue() && (selectedDragGraphList = graphPane.getGraphicManager().getSelectedDraggableGraphics()).size() == 1 && !dragGraph.isOnGraphicLabel(mouseEvent) && dragGraph.getHandlePointIndex(mouseEvent) >= 0) {
                        isEditingGraph = true;
                    }
                    if (!isEditingGraph && graphPane.getGraphicManager().getSelectedGraphics().size() == 1) {
                        ColorLayerUI layer = ColorLayerUI.createTransparentLayerUI(graphPane.getJComponent());
                        ArrayList<DragGraphic> list = new ArrayList<DragGraphic>();
                        list.add(this.graphic);
                        MeasureDialog dialog = new MeasureDialog(graphPane, list);
                        ColorLayerUI.showCenterScreen(dialog, layer);
                        mouseEvent.consume();
                    }
                }
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }
}

