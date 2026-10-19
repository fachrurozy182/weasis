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
import java.util.Objects;
import java.util.Optional;
import org.weasis.core.api.gui.util.ActionState;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.BasicActionState;
import org.weasis.core.api.gui.util.Feature;
import org.weasis.core.api.gui.util.MouseActionAdapter;
import org.weasis.core.api.service.AuditLog;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.model.utils.bean.PanPoint;

public abstract class CrosshairListener
extends MouseActionAdapter
implements ActionState,
KeyListener {
    private MouseActionAdapter win = null;
    private MouseActionAdapter lev = null;
    private final BasicActionState basicState;
    private boolean triggerAction = true;
    private Point pickPoint;
    private PanPoint point;

    protected CrosshairListener(Feature<? extends ActionState> action, PanPoint point) {
        this.basicState = new BasicActionState(action);
        this.point = point == null ? new PanPoint(PanPoint.State.CENTER) : point;
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

    public void setPoint(PanPoint point) {
        if (point != null) {
            this.point = point;
            this.pointChanged(point);
            AuditLog.LOGGER.info("action:{} val:{},{}", new Object[]{this.basicState.getActionW().cmd(), point.getX(), point.getY()});
        }
    }

    public boolean isTriggerAction() {
        return this.triggerAction;
    }

    @Override
    public Feature<? extends ActionState> getActionW() {
        return this.basicState.getActionW();
    }

    public String getValueToDisplay() {
        return "x:" + this.point.getX() + ", y:" + this.point.getY();
    }

    public abstract void pointChanged(PanPoint var1);

    @Override
    public void setButtonMaskEx(int buttonMask) {
        if (buttonMask == 0 && this.buttonMaskEx != 0) {
            this.setPoint(new PanPoint(PanPoint.State.CENTER, Double.NaN, Double.NaN));
        }
        super.setButtonMaskEx(buttonMask);
    }

    public String toString() {
        return this.basicState.getActionW().getTitle();
    }

    private static ViewCanvas<?> getViewCanvas(InputEvent e) {
        Object source = e.getSource();
        if (source instanceof ViewCanvas) {
            return (ViewCanvas)source;
        }
        return null;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (this.basicState.isActionEnabled()) {
            int buttonMask = this.getButtonMaskEx();
            int modifier = e.getModifiersEx();
            if (!e.isConsumed() && (modifier & buttonMask) != 0) {
                ViewCanvas<?> panner = CrosshairListener.getViewCanvas(e);
                int mask = 128;
                if ((modifier & mask) == mask && Objects.nonNull(panner)) {
                    panner.getJComponent().setCursor(ActionW.WINLEVEL.getCursor());
                    this.win = panner.getEventManager().getAction(ActionW.WINDOW).orElse(null);
                    if (this.win != null) {
                        this.win.setButtonMaskEx(this.win.getButtonMaskEx() | buttonMask);
                        this.win.setMoveOnX(true);
                        this.win.mousePressed(e);
                    }
                    this.lev = panner.getEventManager().getAction(ActionW.LEVEL).orElse(null);
                    if (this.lev != null) {
                        this.lev.setButtonMaskEx(this.lev.getButtonMaskEx() | buttonMask);
                        this.lev.setInverse(true);
                        this.lev.mousePressed(e);
                    }
                } else {
                    this.releaseWinLevelAdapter();
                    if (Objects.nonNull(panner)) {
                        this.pickPoint = e.getPoint();
                        Point2D pt = panner.getImageCoordinatesFromMouse(e.getX(), e.getY());
                        PanPoint panPoint = new PanPoint(PanPoint.State.DRAGSTART, pt.getX(), pt.getY());
                        this.setPoint(panPoint);
                    }
                }
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (this.basicState.isActionEnabled()) {
            int buttonMask = this.getButtonMaskEx();
            if (!e.isConsumed() && (e.getModifiersEx() & buttonMask) != 0) {
                ViewCanvas<?> panner = CrosshairListener.getViewCanvas(e);
                if (this.win != null && Objects.nonNull(panner)) {
                    panner.getJComponent().setCursor(ActionW.WINLEVEL.getCursor());
                    this.win.mouseDragged(e);
                    if (this.lev != null) {
                        this.lev.mouseDragged(e);
                    }
                } else if (Objects.nonNull(panner) && Objects.nonNull(this.pickPoint)) {
                    Point2D pt = panner.getImageCoordinatesFromMouse(e.getX(), e.getY());
                    PanPoint panPoint = new PanPoint(PanPoint.State.DRAGGING, pt.getX(), pt.getY());
                    this.setPoint(panPoint);
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        this.releaseWinLevelAdapter();
        if (this.basicState.isActionEnabled() && !e.isConsumed() && (e.getModifiers() & this.getButtonMask()) != 0) {
            ViewCanvas<?> panner = CrosshairListener.getViewCanvas(e);
            if (Objects.nonNull(panner) && Objects.nonNull(this.pickPoint)) {
                Point2D pt = panner.getImageCoordinatesFromMouse(e.getX(), e.getY());
                PanPoint panPoint = new PanPoint(PanPoint.State.DRAGEND, pt.getX(), pt.getY());
                this.setPoint(panPoint);
            }
            Optional.ofNullable(panner).ifPresent(p -> p.getJComponent().repaint());
        }
    }

    private void releaseWinLevelAdapter() {
        if (this.win != null) {
            this.win.setButtonMaskEx(0);
            this.win = null;
        }
        if (this.lev != null) {
            this.lev.setButtonMaskEx(0);
            this.lev = null;
        }
    }

    public void reset() {
        this.pickPoint = null;
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}

