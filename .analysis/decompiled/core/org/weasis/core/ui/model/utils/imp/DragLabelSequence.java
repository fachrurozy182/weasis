/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.MathUtil
 */
package org.weasis.core.ui.model.utils.imp;

import java.awt.geom.Point2D;
import java.util.Objects;
import org.weasis.core.ui.model.graphic.DragGraphic;
import org.weasis.core.ui.model.utils.Draggable;
import org.weasis.core.ui.util.MouseEventDouble;
import org.weasis.core.util.MathUtil;

public class DragLabelSequence
implements Draggable {
    private final Point2D lastPoint;
    private final DragGraphic graphic;

    public DragLabelSequence(DragGraphic graphic) {
        this.graphic = Objects.requireNonNull(graphic);
        this.lastPoint = new Point2D.Double();
    }

    @Override
    public void startDrag(MouseEventDouble evt) {
        this.lastPoint.setLocation(evt.getImageX(), evt.getImageY());
    }

    @Override
    public void drag(MouseEventDouble evt) {
        double deltaX = evt.getImageX() - this.lastPoint.getX();
        double deltaY = evt.getImageY() - this.lastPoint.getY();
        if (MathUtil.isDifferentFromZero((double)deltaX) || MathUtil.isDifferentFromZero((double)deltaY)) {
            this.lastPoint.setLocation(evt.getImageX(), evt.getImageY());
            this.graphic.moveLabel(deltaX, deltaY);
        }
    }

    @Override
    public Boolean completeDrag(MouseEventDouble mouseevent) {
        return Boolean.TRUE;
    }
}

