/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.errorprone.annotations.Immutable
 */
package com.github.weisj.jsvg.util.supplier;

import com.google.errorprone.annotations.Immutable;

@Immutable
@FunctionalInterface
public interface ImmutableSupplier<T> {
    public T get();
}

