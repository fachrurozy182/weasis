/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.nodes;

import com.github.weisj.jsvg.parser.TextContent;
import com.github.weisj.jsvg.parser.impl.AttributeNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SVGNode {
    @NotNull
    public String tagName();

    @Nullable
    public String id();

    public void build(@NotNull AttributeNode var1);

    default public void addContent(@NotNull TextContent.Segment content) {
    }
}

