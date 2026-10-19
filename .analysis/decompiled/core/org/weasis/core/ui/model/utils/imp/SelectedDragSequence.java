/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.model.utils.imp;

import org.weasis.core.ui.model.graphic.DragGraphic;
import org.weasis.core.ui.model.utils.imp.DefaultDragSequence;
import org.weasis.core.ui.util.MouseEventDouble;

public class SelectedDragSequence
extends DefaultDragSequence {
    public SelectedDragSequence(DragGraphic graphic) {
        super(graphic);
    }

    @Override
    public Boolean completeDrag(MouseEventDouble mouseEvent) {
        if (mouseEvent != null && mouseEvent.getID() == 502) {
            this.graphic.fireRemoveAndRepaintAction();
        }
        return Boolean.TRUE;
    }
}

