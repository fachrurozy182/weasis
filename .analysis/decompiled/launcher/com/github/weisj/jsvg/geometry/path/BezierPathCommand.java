/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.geometry.path;

import com.github.weisj.jsvg.geometry.mesh.Bezier;
import java.awt.geom.Point2D;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface BezierPathCommand {
    @NotNull
    public Bezier createBezier(@NotNull Point2D.Float var1);
}

