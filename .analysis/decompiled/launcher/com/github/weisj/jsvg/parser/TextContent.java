/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.ApiStatus$Experimental
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.parser;

import java.util.List;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public interface TextContent {
    @ApiStatus.Experimental
    @NotNull
    public @NotNull List<@NotNull Segment> contentAfterChildIndex(int var1);

    public static interface Segment {
        @NotNull
        public String text();

        public boolean isConstant();
    }
}

