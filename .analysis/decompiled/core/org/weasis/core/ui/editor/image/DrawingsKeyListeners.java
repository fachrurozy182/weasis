/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.editor.image;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Objects;
import org.weasis.core.api.gui.util.ShortcutManager;
import org.weasis.core.ui.editor.image.Canvas;
import org.weasis.core.ui.model.GraphicModel;

public class DrawingsKeyListeners
implements KeyListener {
    private final Canvas canvas;

    public DrawingsKeyListeners(Canvas canvas) {
        this.canvas = Objects.requireNonNull(canvas);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        ShortcutManager sm = ShortcutManager.getInstance();
        GraphicModel graphicManager = this.canvas.getGraphicManager();
        if (sm.matches("draw.delete", e)) {
            graphicManager.deleteSelectedGraphics(this.canvas, true);
        } else if (sm.matches("draw.deselectAll", e)) {
            graphicManager.setSelectedGraphic(null);
        } else if (sm.matches("draw.selectAll", e)) {
            graphicManager.setSelectedAllGraphics();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}

