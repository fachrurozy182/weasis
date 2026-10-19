/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.parser;

import com.github.weisj.jsvg.parser.DomDocument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ElementLoader {
    @Nullable
    public <T> T loadElement(@NotNull Class<T> var1, @Nullable String var2, @NotNull DomDocument var3);
}

