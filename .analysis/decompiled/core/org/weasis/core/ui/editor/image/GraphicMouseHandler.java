/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.editor.image;

import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.weasis.core.api.gui.util.ActionState;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.ComboItemListener;
import org.weasis.core.api.gui.util.Feature;
import org.weasis.core.api.gui.util.MouseActionAdapter;
import org.weasis.core.api.gui.util.ToggleButtonListener;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.ui.editor.image.DefaultView2d;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.ui.editor.image.MeasureToolBar;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.model.AbstractGraphicModel;
import org.weasis.core.ui.model.GraphicModel;
import org.weasis.core.ui.model.graphic.DragGraphic;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.graphic.imp.area.SelectGraphic;
import org.weasis.core.ui.model.utils.Draggable;
import org.weasis.core.ui.model.utils.imp.DefaultDragSequence;
import org.weasis.core.ui.util.MouseEventDouble;

public class GraphicMouseHandler<E extends ImageElement>
extends MouseActionAdapter {
    private final ViewCanvas<E> vImg;
    private Draggable ds;
    private final CursorSet cursorSet;

    public GraphicMouseHandler(ViewCanvas<E> vImg) {
        this(vImg, new CursorSet(DefaultView2d.DEFAULT_CURSOR, DefaultView2d.MOVE_CURSOR, DefaultView2d.HAND_CURSOR, DefaultView2d.EDIT_CURSOR));
    }

    public GraphicMouseHandler(ViewCanvas<E> vImg, CursorSet cursors) {
        if (vImg == null) {
            throw new IllegalArgumentException();
        }
        this.vImg = vImg;
        this.cursorSet = cursors;
    }

    public Draggable getDragSequence() {
        return this.ds;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Object item;
        Graphic graph;
        Optional<ComboItemListener> items;
        ImageViewerEventManager<E> eventManager;
        Optional<Feature<ActionState>> action;
        Graphic graphic;
        Optional<Graphic> firstGraphicIntersecting;
        int buttonMask = this.getButtonMaskEx();
        if (e.isConsumed() || (e.getModifiersEx() & buttonMask) == 0) {
            return;
        }
        MouseEventDouble mouseEvt = new MouseEventDouble(e);
        mouseEvt.setImageCoordinates(this.vImg.getImageCoordinatesFromMouse(e.getX(), e.getY()));
        if (this.ds != null) {
            Boolean c = this.ds.completeDrag(mouseEvt);
            if (mouseEvt.isConsumed()) {
                c = Boolean.FALSE;
                this.ds = null;
            }
            if (!c.booleanValue()) {
                return;
            }
        }
        Cursor newCursor = this.cursorSet.getDrawingCursor();
        GraphicModel graphicList = this.vImg.getGraphicManager();
        if (!mouseEvt.isShiftDown() && (firstGraphicIntersecting = graphicList.getFirstGraphicIntersecting(mouseEvt)).isPresent() && (graphic = firstGraphicIntersecting.get()) instanceof DragGraphic) {
            DragGraphic dragGraph = (DragGraphic)graphic;
            List<DragGraphic> selectedDragGraphList = graphicList.getSelectedDraggableGraphics();
            boolean locked = dragGraph.getLayer().getLocked();
            if (!locked && selectedDragGraphList.contains(dragGraph)) {
                if (selectedDragGraphList.size() > 1 && selectedDragGraphList.stream().noneMatch(g -> g.getLayer().getLocked())) {
                    this.ds = new DefaultView2d.BulkDragSequence(selectedDragGraphList, mouseEvt);
                    newCursor = this.cursorSet.getMoveCursor();
                } else if (selectedDragGraphList.size() == 1) {
                    if (dragGraph.isOnGraphicLabel(mouseEvt)) {
                        this.ds = dragGraph.createDragLabelSequence();
                        newCursor = this.cursorSet.getHandCursor();
                    } else {
                        int handlePtIndex = dragGraph.getHandlePointIndex(mouseEvt);
                        if (handlePtIndex >= 0) {
                            dragGraph.moveMouseOverHandlePoint(handlePtIndex, mouseEvt);
                            this.ds = dragGraph.createResizeDrag(handlePtIndex);
                            newCursor = this.cursorSet.getEditCursor();
                        } else {
                            this.ds = dragGraph.createMoveDrag();
                            newCursor = this.cursorSet.getMoveCursor();
                        }
                    }
                }
            } else {
                if (!locked && dragGraph.isOnGraphicLabel(mouseEvt)) {
                    this.ds = dragGraph.createDragLabelSequence();
                    newCursor = this.cursorSet.getHandCursor();
                } else if (!locked) {
                    this.ds = dragGraph.createMoveDrag();
                    newCursor = this.cursorSet.getMoveCursor();
                }
                this.vImg.getGraphicManager().setSelectedGraphic(Collections.singletonList(dragGraph));
            }
        }
        if (this.ds == null && (action = (eventManager = this.vImg.getEventManager()).getMouseAction(e.getModifiersEx())).isPresent() && action.get().isDrawingAction() && (items = eventManager.getActionFromActionKey("draw.sub." + action.get().cmd(), ComboItemListener.class)).isPresent() && (graph = AbstractGraphicModel.drawFromCurrentGraphic(this.vImg, (Graphic)((item = items.get().getSelectedItem()) instanceof Graphic ? item : null))) instanceof DragGraphic) {
            DragGraphic dragGraphic = (DragGraphic)graph;
            this.ds = dragGraphic.createResizeDrag();
            if (!(graph instanceof SelectGraphic)) {
                this.vImg.getGraphicManager().setSelectedGraphic(Collections.singletonList(graph));
            }
        }
        this.vImg.getJComponent().setCursor(Optional.ofNullable(newCursor).orElse(this.cursorSet.getDrawingCursor()));
        if (this.ds != null) {
            this.ds.startDrag(mouseEvt);
        } else {
            this.vImg.getGraphicManager().setSelectedGraphic(null);
        }
        this.vImg.getGraphicManager().fireGraphicsSelectionChanged(this.vImg.getMeasurableLayer());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        Graphic graphic;
        int buttonMask = this.getButtonMask();
        if ((e.getModifiers() & buttonMask) == 0) {
            return;
        }
        if (this.ds == null) {
            return;
        }
        MouseEventDouble mouseEvt = new MouseEventDouble(e);
        mouseEvt.setImageCoordinates(this.vImg.getImageCoordinatesFromMouse(e.getX(), e.getY()));
        Optional<SelectGraphic> selection = this.vImg.getGraphicManager().getSelectGraphic();
        if (selection.isPresent()) {
            List<Graphic> selectedGraphList;
            AffineTransform transform;
            Graphic selectGraphic = selection.get();
            Rectangle selectionRect = selectGraphic.getBounds(transform = DefaultView2d.getAffineTransform(mouseEvt));
            boolean isSelectionSingleClick = selectionRect == null || selectionRect.width < 5 && selectionRect.height < 5;
            ArrayList<Graphic> newSelectedGraphList = new ArrayList<Graphic>();
            if (isSelectionSingleClick) {
                this.vImg.getGraphicManager().getFirstGraphicIntersecting(mouseEvt).ifPresent(newSelectedGraphList::add);
            } else {
                newSelectedGraphList.addAll(this.vImg.getGraphicManager().getSelectedAllGraphicsIntersecting(selectionRect, transform));
            }
            if (mouseEvt.isShiftDown() && !(selectedGraphList = this.vImg.getGraphicManager().getSelectedGraphics()).isEmpty()) {
                if (newSelectedGraphList.isEmpty()) {
                    newSelectedGraphList.addAll(selectedGraphList);
                } else {
                    selectedGraphList.forEach(g -> {
                        if (!newSelectedGraphList.contains(g)) {
                            newSelectedGraphList.add((Graphic)g);
                        } else if (isSelectionSingleClick) {
                            newSelectedGraphList.remove(g);
                        }
                    });
                }
            }
            this.vImg.getGraphicManager().setSelectedGraphic(newSelectedGraphList);
        }
        if (this.ds.completeDrag(mouseEvt).booleanValue()) {
            this.vImg.getEventManager().getAction(ActionW.DRAW_ONLY_ONCE).filter(ToggleButtonListener::isSelected).ifPresent(a -> {
                this.vImg.getEventManager().getAction(ActionW.DRAW_MEASURE).ifPresent(c -> c.setSelectedItem(MeasureToolBar.selectionGraphic));
                this.vImg.getEventManager().getAction(ActionW.DRAW_GRAPHICS).ifPresent(c -> c.setSelectedItem(MeasureToolBar.selectionGraphic));
            });
            this.ds = null;
        }
        this.vImg.getGraphicManager().fireGraphicsSelectionChanged(this.vImg.getMeasurableLayer());
        Cursor newCursor = this.cursorSet.getDrawingCursor();
        List<DragGraphic> selectedDragGraphList = this.vImg.getGraphicManager().getSelectedDraggableGraphics();
        Optional<Graphic> firstGraphicIntersecting = this.vImg.getGraphicManager().getFirstGraphicIntersecting(mouseEvt);
        if (firstGraphicIntersecting.isPresent() && (graphic = firstGraphicIntersecting.get()) instanceof DragGraphic) {
            DragGraphic dragGraph = (DragGraphic)graphic;
            if (!firstGraphicIntersecting.get().getLayer().getLocked().booleanValue()) {
                newCursor = GraphicMouseHandler.getCursor(mouseEvt, selectedDragGraphList, dragGraph, this.cursorSet);
            }
        }
        this.vImg.getJComponent().setCursor(Optional.ofNullable(newCursor).orElse(this.cursorSet.getDrawingCursor()));
    }

    private static Cursor getCursor(MouseEventDouble mouseEvt, List<DragGraphic> selectedDragGraphList, DragGraphic dragGraph, CursorSet cursorSet) {
        Cursor newCursor = selectedDragGraphList.contains(dragGraph) ? (selectedDragGraphList.size() > 1 ? cursorSet.getMoveCursor() : (dragGraph.isOnGraphicLabel(mouseEvt) ? cursorSet.getHandCursor() : (dragGraph.getHandlePointIndex(mouseEvt) >= 0 ? cursorSet.getEditCursor() : cursorSet.getMoveCursor()))) : (dragGraph.isOnGraphicLabel(mouseEvt) ? cursorSet.getHandCursor() : cursorSet.getMoveCursor());
        return newCursor;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int buttonMask = this.getButtonMaskEx();
        if (e.isConsumed() || (e.getModifiersEx() & buttonMask) == 0) {
            return;
        }
        if (this.ds != null) {
            MouseEventDouble mouseEvt = new MouseEventDouble(e);
            mouseEvt.setImageCoordinates(this.vImg.getImageCoordinatesFromMouse(e.getX(), e.getY()));
            this.ds.drag(mouseEvt);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (e.isConsumed()) {
            return;
        }
        MouseEventDouble mouseEvt = new MouseEventDouble(e);
        mouseEvt.setImageCoordinates(this.vImg.getImageCoordinatesFromMouse(e.getX(), e.getY()));
        if (this.ds instanceof DefaultDragSequence) {
            this.ds.drag(mouseEvt);
        } else {
            Graphic graphic;
            Optional<Graphic> firstGraphicIntersecting;
            Cursor newCursor = null;
            GraphicModel graphicList = this.vImg.getGraphicManager();
            if (!mouseEvt.isShiftDown() && (firstGraphicIntersecting = graphicList.getFirstGraphicIntersecting(mouseEvt)).isPresent() && (graphic = firstGraphicIntersecting.get()) instanceof DragGraphic) {
                DragGraphic dragGraph = (DragGraphic)graphic;
                if (!firstGraphicIntersecting.get().getLayer().getLocked().booleanValue()) {
                    List<DragGraphic> selectedDragGraphList = this.vImg.getGraphicManager().getSelectedDraggableGraphics();
                    newCursor = GraphicMouseHandler.getCursor(mouseEvt, selectedDragGraphList, dragGraph, this.cursorSet);
                }
            }
            if (newCursor != null) {
                this.vImg.getJComponent().setCursor(newCursor);
            }
        }
    }

    public static class CursorSet {
        private final Cursor drawingCursor;
        private final Cursor moveCursor;
        private final Cursor handCursor;
        private final Cursor editCursor;

        public CursorSet(Cursor drawing, Cursor move, Cursor hand, Cursor edit) {
            this.drawingCursor = Optional.ofNullable(drawing).orElse(DefaultView2d.DEFAULT_CURSOR);
            this.moveCursor = Optional.ofNullable(move).orElse(DefaultView2d.MOVE_CURSOR);
            this.handCursor = Optional.ofNullable(hand).orElse(DefaultView2d.HAND_CURSOR);
            this.editCursor = Optional.ofNullable(edit).orElse(DefaultView2d.EDIT_CURSOR);
        }

        public Cursor getDrawingCursor() {
            return this.drawingCursor;
        }

        public Cursor getMoveCursor() {
            return this.moveCursor;
        }

        public Cursor getHandCursor() {
            return this.handCursor;
        }

        public Cursor getEditCursor() {
            return this.editCursor;
        }
    }
}

