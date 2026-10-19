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

public final class Terminal
extends PathCommand {
    Terminal() {
        super(1);
    }

    @Override
    public void appendPath(@NotNull Path2D path, @NotNull BuildHistory hist) {
        path.closePath();
        hist.setLast(hist.startPoint);
    }

    public String toString() {
        return "Z";
    }
}

