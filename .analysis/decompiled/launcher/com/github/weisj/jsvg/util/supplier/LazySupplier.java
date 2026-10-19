/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.util.supplier;

import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public final class LazySupplier<T>
implements Supplier<T> {
    @NotNull
    private final @NotNull Supplier<@NotNull T> supplier;
    private T t;

    public LazySupplier(@NotNull @NotNull Supplier<@NotNull T> supplier) {
        this.supplier = supplier;
    }

    @Override
    @NotNull
    public T get() {
        if (this.t == null) {
            this.t = this.supplier.get();
        }
        return this.t;
    }
}

