/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.util.supplier;

import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public final class ConstantSupplier<T>
implements Supplier<T> {
    @NotNull
    private final T t;

    public ConstantSupplier(@NotNull T t) {
        this.t = t;
    }

    @Override
    @NotNull
    public T get() {
        return this.t;
    }
}

