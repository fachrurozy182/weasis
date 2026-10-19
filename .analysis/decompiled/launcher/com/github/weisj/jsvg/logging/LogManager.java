/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.logging;

import com.github.weisj.jsvg.logging.Logger;
import org.jetbrains.annotations.NotNull;

public interface LogManager {
    @NotNull
    public Logger createLogger(@NotNull String var1);
}

