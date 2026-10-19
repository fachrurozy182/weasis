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

final class QuadraticSmooth
extends PathCommand {
    private final float x;
    private final float y;

    public QuadraticSmooth(boolean isRelative, float x, float y) {
        super(isRelative, 4);
        this.x = x;
        this.y = y;
    }

    @Override
    public void appendPath(@NotNull Path2D path, @NotNull BuildHistory hist) {
        Point2D.Float offset = this.offset(hist);
        Point2D.Float knot = this.lastKnotReflectionQuadratic(hist);
        path.quadTo(knot.x, knot.y, this.x + offset.x, this.y + offset.y);
        hist.setLastQuadratic(path.getCurrentPoint(), knot);
    }

    public String toString() {
        return "T " + this.x + " " + this.y;
    }
}

