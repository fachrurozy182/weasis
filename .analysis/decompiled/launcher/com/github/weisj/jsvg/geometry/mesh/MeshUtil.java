/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.geometry.mesh;

import java.awt.geom.Point2D;
import org.jetbrains.annotations.NotNull;

final class MeshUtil {
    private MeshUtil() {
    }

    @NotNull
    static Point2D.Float p(float x, float y) {
        return new Point2D.Float(x, y);
    }
}

