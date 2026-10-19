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

final class LineTo
extends PathCommand {
    private final float x;
    private final float y;

    public LineTo(boolean isRelative, float x, float y) {
        super(isRelative, 2);
        this.x = x;
        this.y = y;
    }

    public float x() {
        return this.x;
    }

    public float y() {
        return this.y;
    }

    @Override
    public void appendPath(@NotNull Path2D path, @NotNull BuildHistory hist) {
        Point2D.Float offset = this.offset(hist);
        path.lineTo(this.x + offset.x, this.y + offset.y);
        hist.setLast(path.getCurrentPoint());
    }

    @Override
    public int nodeCount() {
        return 2;
    }

    public String toString() {
        return "L " + this.x + " " + this.y;
    }
}

