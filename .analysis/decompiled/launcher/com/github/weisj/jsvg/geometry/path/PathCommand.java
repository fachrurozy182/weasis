/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.geometry.path;

import com.github.weisj.jsvg.geometry.path.BuildHistory;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import org.jetbrains.annotations.NotNull;

public abstract class PathCommand {
    private final boolean isRelative;
    private final int nodeCount;

    protected PathCommand(int nodeCount) {
        this(false, nodeCount);
    }

    protected PathCommand(boolean isRelative, int nodeCount) {
        this.isRelative = isRelative;
        this.nodeCount = nodeCount;
    }

    protected Point2D.Float offset(@NotNull BuildHistory hist) {
        if (this.isRelative()) {
            return new Point2D.Float(hist.lastPoint.x, hist.lastPoint.y);
        }
        return new Point2D.Float(0.0f, 0.0f);
    }

    @NotNull
    protected Point2D.Float lastKnotReflectionCubic(@NotNull BuildHistory hist) {
        return this.lastKnotReflection(hist.lastPoint, hist.lastCubicKnot);
    }

    @NotNull
    protected Point2D.Float lastKnotReflectionQuadratic(@NotNull BuildHistory hist) {
        return this.lastKnotReflection(hist.lastPoint, hist.lastQuadraticKnot);
    }

    @NotNull
    private Point2D.Float lastKnotReflection(@NotNull Point2D.Float point, @NotNull Point2D.Float knot) {
        float kx = point.x * 2.0f - knot.x;
        float ky = point.y * 2.0f - knot.y;
        return new Point2D.Float(kx, ky);
    }

    public boolean isRelative() {
        return this.isRelative;
    }

    public abstract void appendPath(@NotNull Path2D var1, @NotNull BuildHistory var2);

    public int nodeCount() {
        return this.nodeCount;
    }
}

