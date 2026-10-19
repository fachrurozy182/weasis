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

final class LineToBezier
implements BezierPathCommand {
    private final boolean relative;
    private final float x;
    private final float y;

    public LineToBezier(boolean relative, float x, float y) {
        this.relative = relative;
        this.x = x;
        this.y = y;
    }

    @Override
    @NotNull
    public Bezier createBezier(@NotNull Point2D.Float start) {
        if (this.relative) {
            return Bezier.straightLine(start, new Point2D.Float(start.x + this.x, start.y + this.y));
        }
        return Bezier.straightLine(start, new Point2D.Float(this.x, this.y));
    }
}

