/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.Point3
 */
package org.weasis.core.ui.editor.image;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.util.Optional;
import org.opencv.core.Point3;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.ActionState;
import org.weasis.core.api.gui.util.Feature;
import org.weasis.core.api.gui.util.MouseActionAdapter;
import org.weasis.core.api.gui.util.WinUtil;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.ui.editor.image.DefaultView2d;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.PixelInfo;
import org.weasis.core.ui.editor.image.SynchViewButton;
import org.weasis.core.ui.editor.image.ViewButton;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.model.layer.LayerAnnotation;
import org.weasis.core.ui.model.layer.LayerItem;

public final class FocusHandler<E extends ImageElement>
extends MouseActionAdapter {
    private static final long UPDATE_INTERVAL_MS = 50L;
    private final ViewCanvas<E> viewCanvas;
    private long lastUpdateTime;
    private boolean buttonPressActive;

    public FocusHandler(ViewCanvas<E> viewCanvas) {
        this.viewCanvas = viewCanvas;
    }

    @Override
    public void mousePressed(MouseEvent evt) {
        ImageViewerPlugin<E> pane = this.viewCanvas.getEventManager().getSelectedView2dContainer();
        if (pane == null) {
            return;
        }
        Optional<ViewButton> selectedButton = this.findViewButtonAt(evt.getPoint());
        if (selectedButton.isPresent()) {
            this.buttonPressActive = true;
            evt.consume();
            this.selectViewIfNeeded(pane);
            this.viewCanvas.getJComponent().requestFocusInWindow();
            this.handleViewButtonClick(evt, selectedButton.get());
            return;
        }
        this.buttonPressActive = false;
        if (evt.getClickCount() == 2) {
            pane.maximizedSelectedImagePane(this.viewCanvas, evt);
            return;
        }
        this.selectViewIfNeeded(pane);
        this.viewCanvas.getJComponent().requestFocusInWindow();
        this.updateCursorForMouseAction(evt);
    }

    @Override
    public void mouseDragged(MouseEvent evt) {
        if (this.buttonPressActive) {
            evt.consume();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        ImageViewerEventManager<E> eventManager = this.viewCanvas.getEventManager();
        if (eventManager == null) {
            return;
        }
        ImageViewerPlugin container = WinUtil.getParentOfClass(this.viewCanvas.getJComponent(), ImageViewerPlugin.class);
        if (container == null) {
            return;
        }
        if (!container.equals(eventManager.getSelectedView2dContainer())) {
            eventManager.setSelectedView2dContainer(container);
        }
        if (!container.getSelectedViewCanvas().equals(this.viewCanvas)) {
            container.setSelectedImagePane(this.viewCanvas);
        }
    }

    @Override
    public void mouseMoved(MouseEvent evt) {
        this.showPixelInfos(evt);
        this.updateViewButtonsHoverState(evt.getPoint());
    }

    @Override
    public void mouseReleased(MouseEvent evt) {
        this.viewCanvas.getJComponent().setCursor(DefaultView2d.DEFAULT_CURSOR);
        if (this.buttonPressActive) {
            evt.consume();
            this.buttonPressActive = false;
        } else {
            this.findViewButtonAt(evt.getPoint()).ifPresent(viewButton -> evt.consume());
        }
    }

    private Optional<ViewButton> findViewButtonAt(Point point) {
        return this.viewCanvas.getViewButtons().stream().filter(ViewButton::isVisible).filter(button -> button.contains(point)).findFirst();
    }

    private void selectViewIfNeeded(ImageViewerPlugin<E> pane) {
        if (pane.isContainingView(this.viewCanvas).booleanValue() && pane.getSelectedViewCanvas() != this.viewCanvas) {
            pane.setSelectedImagePane(this.viewCanvas);
        }
    }

    private void handleViewButtonClick(MouseEvent evt, ViewButton button) {
        this.viewCanvas.getJComponent().setCursor(DefaultView2d.DEFAULT_CURSOR);
        evt.consume();
        button.showPopup(evt.getComponent(), evt.getX(), evt.getY());
        if (button instanceof SynchViewButton) {
            this.viewCanvas.getJComponent().repaint();
        }
    }

    private void updateCursorForMouseAction(MouseEvent evt) {
        Optional<Feature<ActionState>> action = this.viewCanvas.getEventManager().getMouseAction(evt.getModifiersEx());
        Cursor cursor = action.map(Feature::getCursor).orElse(DefaultView2d.DEFAULT_CURSOR);
        this.viewCanvas.getJComponent().setCursor(cursor);
    }

    private void updateViewButtonsHoverState(Point mousePoint) {
        this.viewCanvas.getViewButtons().stream().filter(ViewButton::isVisible).forEach(button -> {
            boolean hover = button.contains(mousePoint);
            if (hover != button.isHover()) {
                button.setHover(hover);
                this.viewCanvas.getJComponent().repaint();
            }
        });
    }

    private void showPixelInfos(MouseEvent mouseevent) {
        LayerAnnotation infoLayer = this.viewCanvas.getInfoLayer();
        if (!this.isPixelInfoVisible(infoLayer)) {
            return;
        }
        if (!this.shouldUpdatePixelInfo()) {
            return;
        }
        PixelInfo pixelInfo = this.computePixelInfo(mouseevent);
        if (pixelInfo == null) {
            return;
        }
        this.updatePixelInfoDisplay(pixelInfo);
    }

    private boolean isPixelInfoVisible(LayerAnnotation infoLayer) {
        return infoLayer != null && infoLayer.getVisible() != false && infoLayer.getDisplayPreferences(LayerItem.PIXEL) && !infoLayer.getDisplayPreferences(LayerItem.MIN_ANNOTATIONS);
    }

    private boolean shouldUpdatePixelInfo() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastUpdateTime < 50L) {
            return false;
        }
        this.lastUpdateTime = currentTime;
        return true;
    }

    private PixelInfo computePixelInfo(MouseEvent mouseevent) {
        Point2D pModel = this.viewCanvas.getImageCoordinatesFromMouse(mouseevent.getX(), mouseevent.getY());
        PixelInfo pixelInfo = this.viewCanvas.getPixelInfo(new Point((int)Math.floor(pModel.getX()), (int)Math.floor(pModel.getY())));
        if (pixelInfo != null) {
            Point3 point3d = this.viewCanvas.getVolumePoint3FromMouse(mouseevent.getX(), mouseevent.getY());
            pixelInfo.setPosition3d(point3d);
        }
        return pixelInfo;
    }

    private void updatePixelInfoDisplay(PixelInfo pixelInfo) {
        LayerAnnotation infoLayer = this.viewCanvas.getInfoLayer();
        Rectangle oldBound = infoLayer.getPixelInfoBound();
        int textWidth = this.calculatePixelInfoTextWidth(pixelInfo);
        oldBound.width = Math.max(oldBound.width, textWidth + 4);
        infoLayer.setPixelInfo(pixelInfo);
        this.viewCanvas.getJComponent().repaint(oldBound);
    }

    private int calculatePixelInfoTextWidth(PixelInfo pixelInfo) {
        String text = Messages.getString("DefaultView2d.pix") + ": " + pixelInfo.getPixelValueText() + " - " + pixelInfo.getPixelPositionText();
        return this.viewCanvas.getJComponent().getFontMetrics(this.viewCanvas.getJComponent().getFont()).stringWidth(text);
    }
}

