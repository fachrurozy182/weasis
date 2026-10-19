/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.editor.image;

import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JPopupMenu;
import org.weasis.core.api.gui.util.MouseActionAdapter;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.model.graphic.Graphic;

public class ContextMenuHandler<E extends ImageElement>
extends MouseActionAdapter {
    private final ViewCanvas<E> viewCanvas;

    public ContextMenuHandler(ViewCanvas<E> viewCanvas) {
        this.viewCanvas = viewCanvas;
    }

    @Override
    public void mousePressed(MouseEvent evt) {
        this.showPopup(evt);
    }

    @Override
    public void mouseReleased(MouseEvent evt) {
        this.showPopup(evt);
    }

    private void showPopup(MouseEvent evt) {
        if (evt.isConsumed()) {
            return;
        }
        if ((evt.getModifiersEx() & this.getButtonMaskEx()) != 0) {
            JPopupMenu popupMenu = null;
            List<Graphic> selected = this.viewCanvas.getGraphicManager().getSelectedGraphics();
            if (!selected.isEmpty() && this.viewCanvas.isDrawActionActive()) {
                popupMenu = this.viewCanvas.buildGraphicContextMenu(evt, selected);
            } else if (this.viewCanvas.hasValidContent()) {
                popupMenu = this.viewCanvas.buildContextMenu(evt);
            }
            if (popupMenu != null) {
                popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
            }
        }
    }
}

