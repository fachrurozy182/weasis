/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.geometry.path;

import java.awt.geom.Point2D;
import org.jetbrains.annotations.NotNull;

public final class BuildHistory {
    @NotNull
    final Point2D.Float startPoint = new Point2D.Float();
    @NotNull
    final Point2D.Float lastPoint = new Point2D.Float();
    @NotNull
    final Point2D.Float lastCubicKnot = new Point2D.Float();
    @NotNull
    final Point2D.Float lastQuadraticKnot = new Point2D.Float();

    public void setStartPoint(@NotNull Point2D point) {
        this.startPoint.setLocation(point);
    }

    public void setLast(@NotNull Point2D point) {
        this.lastPoint.setLocation(point);
        this.lastQuadraticKnot.setLocation(point);
        this.lastCubicKnot.setLocation(point);
    }

    public void setLastQuadratic(@NotNull Point2D point, float knotX, float knotY) {
        this.setLastQuadratic(point, new Point2D.Float(knotX, knotY));
    }

    public void setLastQuadratic(@NotNull Point2D point, @NotNull Point2D knot) {
        this.lastPoint.setLocation(point);
        this.lastQuadraticKnot.setLocation(knot);
        this.lastCubicKnot.setLocation(point);
    }

    public void setLastCubic(@NotNull Point2D current, float x, float y) {
        this.lastPoint.setLocation(current);
        this.lastQuadraticKnot.setLocation(current);
        this.lastCubicKnot.setLocation(x, y);
    }
}

