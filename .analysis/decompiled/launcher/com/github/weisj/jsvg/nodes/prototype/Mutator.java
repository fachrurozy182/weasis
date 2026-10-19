/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.prototype;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface Mutator<T> {
    @NotNull
    public T mutate(@NotNull T var1);
}

