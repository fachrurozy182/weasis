/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.geometry.path;

import com.github.weisj.jsvg.geometry.path.BuildHistory;
import com.github.weisj.jsvg.geometry.path.PathCommand;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import org.jetbrains.annotations.NotNull;

final class CubicSmooth
extends PathCommand {
    private final float x;
    private final float y;
    private final float k2x;
    private final float k2y;

    public CubicSmooth(boolean isRelative, float k2x, float k2y, float x, float y) {
        super(isRelative, 6);
        this.k2x = k2x;
        this.k2y = k2y;
        this.x = x;
        this.y = y;
    }

    @Override
    public void appendPath(@NotNull Path2D path, @NotNull BuildHistory hist) {
        Point2D.Float offset = this.offset(hist);
        Point2D.Float knot = this.lastKnotReflectionCubic(hist);
        path.curveTo(knot.x, knot.y, this.k2x + offset.x, this.k2y + offset.y, this.x + offset.x, this.y + offset.y);
        hist.setLastCubic(path.getCurrentPoint(), this.k2x + offset.x, this.k2y + offset.y);
    }

    public String toString() {
        return "S " + this.k2x + " " + this.k2y + " " + this.x + " " + this.y;
    }
}

