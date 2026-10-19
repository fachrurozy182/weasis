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
import org.jetbrains.annotations.NotNull;

final class Vertical
extends PathCommand {
    private final float y;

    public Vertical(boolean isRelative, float y) {
        super(isRelative, 2);
        this.y = y;
    }

    @Override
    public void appendPath(@NotNull Path2D path, @NotNull BuildHistory hist) {
        float xOff = hist.lastPoint.x;
        float yOff = this.isRelative() ? hist.lastPoint.y : 0.0f;
        path.lineTo(xOff, this.y + yOff);
        hist.setLast(path.getCurrentPoint());
    }

    public String toString() {
        return "V " + this.y;
    }
}

