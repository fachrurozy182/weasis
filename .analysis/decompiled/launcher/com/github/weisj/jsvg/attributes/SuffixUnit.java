/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.attributes;

import org.jetbrains.annotations.NotNull;

public interface SuffixUnit<T, V> {
    @NotNull
    public String suffix();

    @NotNull
    public @NotNull SuffixUnit<T, V> @NotNull [] units();

    @NotNull
    public V valueOf(float var1);
}

