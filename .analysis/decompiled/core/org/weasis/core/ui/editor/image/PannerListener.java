/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.editor.image;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import org.weasis.core.api.gui.util.ActionState;
import org.weasis.core.api.gui.util.BasicActionState;
import org.weasis.core.api.gui.util.Feature;
import org.weasis.core.api.gui.util.MouseActionAdapter;
import org.weasis.core.api.gui.util.ShortcutManager;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.service.AuditLog;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.model.utils.bean.PanPoint;

public abstract class PannerListener
extends MouseActionAdapter
implements ActionState,
KeyListener {
    private final BasicActionState basicState;
    private final boolean triggerAction = true;
    protected Point pickPoint;
    private Point2D point;

    protected PannerListener(Feature<? extends ActionState> action, Point2D point) {
        this.basicState = new BasicActionState(action);
        this.point = point == null ? new Point2D.Double() : point;
    }

    @Override
    public void enableAction(boolean enabled) {
        this.basicState.enableAction(enabled);
    }

    @Override
    public boolean isActionEnabled() {
        return this.basicState.isActionEnabled();
    }

    @Override
    public boolean registerActionState(Object c) {
        return this.basicState.registerActionState(c);
    }

    @Override
    public void unregisterActionState(Object c) {
        this.basicState.unregisterActionState(c);
    }

    public Point2D getPoint() {
        return (Point2D)this.point.clone();
    }

    public void setPoint(Point2D point) {
        if (point != null) {
            this.point = point;
            this.pointChanged(point);
            AuditLog.LOGGER.info("action:{} val:{},{}", new Object[]{this.getActionW().cmd(), point.getX(), point.getY()});
        }
    }

    public boolean isTriggerAction() {
        return true;
    }

    @Override
    public Feature<? extends ActionState> getActionW() {
        return this.basicState.getActionW();
    }

    public String getValueToDisplay() {
        return "x:" + this.point.getX() + ", y:" + this.point.getY();
    }

    public abstract void pointChanged(Point2D var1);

    public String toString() {
        return this.basicState.getActionW().getTitle();
    }

    protected ViewCanvas<? extends ImageElement> getViewCanvas(InputEvent e) {
        Object source = e.getSource();
        if (source instanceof ViewCanvas) {
            return (ViewCanvas)source;
        }
        return null;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        ViewCanvas<? extends ImageElement> panner;
        if (e.isConsumed()) {
            return;
        }
        int buttonMask = this.getButtonMaskEx();
        if ((e.getModifiersEx() & buttonMask) != 0 && (panner = this.getViewCanvas(e)) != null) {
            this.pickPoint = e.getPoint();
            if (panner.getViewModel() != null) {
                double scale = panner.getViewModel().getViewScale();
                this.setPoint(new PanPoint(PanPoint.State.DRAGSTART, -(this.pickPoint.getX() / scale), -(this.pickPoint.getY() / scale)));
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        ViewCanvas<? extends ImageElement> panner;
        int buttonMask = this.getButtonMaskEx();
        if (!e.isConsumed() && (e.getModifiersEx() & buttonMask) != 0 && (panner = this.getViewCanvas(e)) != null && this.pickPoint != null && panner.getViewModel() != null) {
            double scale = panner.getViewModel().getViewScale();
            this.setPoint(new PanPoint(PanPoint.State.DRAGGING, -(((double)e.getX() - this.pickPoint.getX()) / scale), -(((double)e.getY() - this.pickPoint.getY()) / scale)));
            panner.addPointerType(2);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        ViewCanvas<? extends ImageElement> panner;
        int buttonMask = this.getButtonMask();
        if (!e.isConsumed() && (e.getModifiers() & buttonMask) != 0 && (panner = this.getViewCanvas(e)) != null) {
            panner.resetPointerType(2);
            panner.getJComponent().repaint();
        }
    }

    public void reset() {
        this.pickPoint = null;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int modifiers;
        int keyCode;
        ShortcutManager sm = ShortcutManager.getInstance();
        if (sm.matches("pan.leftFast", keyCode = e.getKeyCode(), modifiers = e.getModifiers())) {
            this.setPoint(new PanPoint(PanPoint.State.MOVE, 10.0, 0.0));
        } else if (sm.matches("pan.rightFast", keyCode, modifiers)) {
            this.setPoint(new PanPoint(PanPoint.State.MOVE, -10.0, 0.0));
        } else if (sm.matches("pan.upFast", keyCode, modifiers)) {
            this.setPoint(new PanPoint(PanPoint.State.MOVE, 0.0, 10.0));
        } else if (sm.matches("pan.downFast", keyCode, modifiers)) {
            this.setPoint(new PanPoint(PanPoint.State.MOVE, 0.0, -10.0));
        } else if (sm.matches("pan.left", keyCode, modifiers)) {
            this.setPoint(new PanPoint(PanPoint.State.MOVE, 5.0, 0.0));
        } else if (sm.matches("pan.right", keyCode, modifiers)) {
            this.setPoint(new PanPoint(PanPoint.State.MOVE, -5.0, 0.0));
        } else if (sm.matches("pan.up", keyCode, modifiers)) {
            this.setPoint(new PanPoint(PanPoint.State.MOVE, 0.0, 5.0));
        } else if (sm.matches("pan.down", keyCode, modifiers)) {
            this.setPoint(new PanPoint(PanPoint.State.MOVE, 0.0, -5.0));
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}

