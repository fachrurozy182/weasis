/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.logging;

import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Logger {
    public void log(Level var1, @Nullable String var2);

    public void log(Level var1, @Nullable String var2, @NotNull Throwable var3);

    public void log(Level var1, @NotNull Supplier<@Nullable String> var2);

    public static enum Level {
        DEBUG,
        INFO,
        WARNING,
        ERROR;

    }
}

