/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.model.graphic;

import java.awt.geom.Point2D;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.utils.Draggable;
import org.weasis.core.ui.util.MouseEventDouble;

public interface DragGraphic
extends Graphic {
    public static final Boolean DEFAULT_RESIZE_OR_MOVING = Boolean.FALSE;

    public Boolean getResizingOrMoving();

    public void setResizeOrMoving(Boolean var1);

    public void buildShape(MouseEventDouble var1);

    public void forceToAddPoints(Integer var1);

    public Point2D removeHandlePoint(Integer var1, MouseEventDouble var2);

    public void moveMouseOverHandlePoint(Integer var1, MouseEventDouble var2);

    public Integer moveAndResizeOnDrawing(Integer var1, Double var2, Double var3, MouseEventDouble var4);

    public Draggable createMoveDrag();

    public Draggable createDragLabelSequence();

    public Draggable createResizeDrag();

    public Draggable createResizeDrag(Integer var1);

    @Override
    public DragGraphic copy();
}

