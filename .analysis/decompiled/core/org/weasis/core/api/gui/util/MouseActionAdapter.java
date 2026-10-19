/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui.util;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public abstract class MouseActionAdapter
implements MouseListener,
MouseWheelListener,
MouseMotionListener {
    static final int JDK_1_3_MODIFIERS = 63;
    static final int HIGH_MODIFIERS = -16384;
    protected int buttonMaskEx = 0;
    protected int lastPosition = 0;
    private boolean inverse = false;
    private boolean moveOnX = false;
    private double mouseSensitivity = 1.0;
    protected double dragAccumulator = Double.MAX_VALUE;

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    public double getMouseSensitivity() {
        return this.mouseSensitivity;
    }

    public void setMouseSensitivity(double mouseSensitivity) {
        this.mouseSensitivity = mouseSensitivity;
    }

    public int getButtonMaskEx() {
        return this.buttonMaskEx;
    }

    public int getButtonMask() {
        int buttonMask;
        int n = buttonMask = (this.buttonMaskEx & 0x400) != 0 ? 16 : 0;
        if ((this.buttonMaskEx & 0x800) != 0) {
            buttonMask |= 8;
        }
        if ((this.buttonMaskEx & 0x1000) != 0) {
            buttonMask |= 4;
        }
        return buttonMask;
    }

    public void setButtonMaskEx(int buttonMask) {
        this.buttonMaskEx = buttonMask;
        if (buttonMask == 0) {
            this.dragAccumulator = Double.MAX_VALUE;
        }
    }

    public boolean isMoveOnX() {
        return this.moveOnX;
    }

    public void setMoveOnX(boolean moveOnX) {
        this.moveOnX = moveOnX;
    }

    public boolean isInverse() {
        return this.inverse;
    }

    public void setInverse(boolean inverse) {
        this.inverse = inverse;
    }
}

