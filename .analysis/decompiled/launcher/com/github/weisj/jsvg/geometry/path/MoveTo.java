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

final class MoveTo
extends PathCommand {
    private final float x;
    private final float y;

    public MoveTo(boolean isRelative, float x, float y) {
        super(isRelative, 2);
        this.x = x;
        this.y = y;
    }

    @Override
    public void appendPath(@NotNull Path2D path, @NotNull BuildHistory hist) {
        Point2D.Float offset = this.offset(hist);
        path.moveTo(this.x + offset.x, this.y + offset.y);
        hist.setStartPoint(path.getCurrentPoint());
        hist.setLast(path.getCurrentPoint());
    }

    public String toString() {
        return "M " + this.x + " " + this.y;
    }
}

