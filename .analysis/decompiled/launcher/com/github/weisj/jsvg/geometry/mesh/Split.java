/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.geometry.mesh;

import org.jetbrains.annotations.NotNull;

final class Split<T> {
    @NotNull
    public final T left;
    @NotNull
    public final T right;

    public Split(@NotNull T left, @NotNull T right) {
        this.left = left;
        this.right = right;
    }
}

