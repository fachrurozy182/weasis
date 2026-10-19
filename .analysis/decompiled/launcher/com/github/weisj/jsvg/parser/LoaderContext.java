/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.parser;

import com.github.weisj.jsvg.parser.DocumentLimits;
import com.github.weisj.jsvg.parser.DomProcessor;
import com.github.weisj.jsvg.parser.ElementLoader;
import com.github.weisj.jsvg.parser.PaintParser;
import com.github.weisj.jsvg.parser.css.CssParser;
import com.github.weisj.jsvg.parser.impl.MutableLoaderContext;
import com.github.weisj.jsvg.parser.resources.ResourceLoader;
import com.github.weisj.jsvg.parser.resources.ResourcePolicy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface LoaderContext {
    @Nullable
    public DomProcessor preProcessor();

    @NotNull
    public CssParser cssParser();

    @NotNull
    public PaintParser paintParser();

    @NotNull
    public ResourceLoader resourceLoader();

    @NotNull
    public ElementLoader elementLoader();

    @NotNull
    public ResourcePolicy externalResourcePolicy();

    @NotNull
    public DocumentLimits documentLimits();

    @NotNull
    public static Builder builder() {
        return MutableLoaderContext.createDefault();
    }

    @NotNull
    public static LoaderContext createDefault() {
        return LoaderContext.builder().build();
    }

    public static interface Builder {
        @NotNull
        public Builder preProcessor(@Nullable DomProcessor var1);

        @NotNull
        public Builder cssParser(@NotNull CssParser var1);

        @NotNull
        public Builder paintParser(@NotNull PaintParser var1);

        @NotNull
        public Builder resourceLoader(@NotNull ResourceLoader var1);

        @NotNull
        public Builder elementLoader(@NotNull ElementLoader var1);

        @NotNull
        public Builder externalResourcePolicy(@NotNull ResourcePolicy var1);

        @NotNull
        public Builder documentLimits(@NotNull DocumentLimits var1);

        @NotNull
        public LoaderContext build();
    }
}

