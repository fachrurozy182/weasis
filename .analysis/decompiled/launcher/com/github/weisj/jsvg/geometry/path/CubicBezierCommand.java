/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.geometry.path;

import com.github.weisj.jsvg.geometry.mesh.Bezier;
import com.github.weisj.jsvg.geometry.path.BezierPathCommand;
import java.awt.geom.Point2D;
import org.jetbrains.annotations.NotNull;

final class CubicBezierCommand
implements BezierPathCommand {
    private final boolean relative;
    private final float bx;
    private final float by;
    private final float cx;
    private final float cy;
    private final float dx;
    private final float dy;

    public CubicBezierCommand(boolean relative, float bx, float by, float cx, float cy, float dx, float dy) {
        this.relative = relative;
        this.bx = bx;
        this.by = by;
        this.cx = cx;
        this.cy = cy;
        this.dx = dx;
        this.dy = dy;
    }

    @Override
    @NotNull
    public Bezier createBezier(@NotNull Point2D.Float start) {
        if (this.relative) {
            return new Bezier(start, new Point2D.Float(start.x + this.bx, start.y + this.by), new Point2D.Float(start.x + this.cx, start.y + this.cy), new Point2D.Float(start.x + this.dx, start.y + this.dy));
        }
        return new Bezier(start, new Point2D.Float(this.bx, this.by), new Point2D.Float(this.cx, this.cy), new Point2D.Float(this.dx, this.dy));
    }

    public String toString() {
        return "CubicBezierCommand{relative=" + this.relative + ", bx=" + this.bx + ", by=" + this.by + ", cx=" + this.cx + ", cy=" + this.cy + ", dx=" + this.dx + ", dy=" + this.dy + '}';
    }
}

