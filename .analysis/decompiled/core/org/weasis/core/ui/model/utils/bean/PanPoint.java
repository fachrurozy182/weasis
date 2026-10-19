/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.model.utils.bean;

import java.awt.geom.Point2D;
import java.util.Objects;

public class PanPoint
extends Point2D {
    private double x;
    private double y;
    private final State state;
    private boolean highlightedPosition;

    public PanPoint(State state) {
        this(state, 0.0, 0.0);
    }

    public PanPoint(State state, double x, double y) {
        this.x = x;
        this.y = y;
        this.state = Objects.requireNonNull(state);
        this.highlightedPosition = false;
    }

    public State getState() {
        return this.state;
    }

    @Override
    public double getX() {
        return this.x;
    }

    @Override
    public double getY() {
        return this.y;
    }

    @Override
    public void setLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = super.hashCode();
        result = 31 * result + (this.highlightedPosition ? 1231 : 1237);
        result = 31 * result + this.state.hashCode();
        long temp = Double.doubleToLongBits(this.x);
        result = 31 * result + (int)(temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(this.y);
        result = 31 * result + (int)(temp ^ temp >>> 32);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        PanPoint other = (PanPoint)obj;
        if (this.highlightedPosition != other.highlightedPosition) {
            return false;
        }
        if (this.state != other.state) {
            return false;
        }
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x)) {
            return false;
        }
        return Double.doubleToLongBits(this.y) == Double.doubleToLongBits(other.y);
    }

    public boolean isHighlightedPosition() {
        return this.highlightedPosition;
    }

    public void setHighlightedPosition(boolean highlightedPosition) {
        this.highlightedPosition = highlightedPosition;
    }

    public static enum State {
        MOVE,
        CENTER,
        DRAGSTART,
        DRAGGING,
        DRAGEND;

    }
}

