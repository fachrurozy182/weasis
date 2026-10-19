/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.parser.impl;

import com.github.weisj.jsvg.parser.TextContent;
import org.jetbrains.annotations.NotNull;

public final class StringSegment
implements TextContent.Segment {
    @NotNull
    private final String text;

    public StringSegment(@NotNull String text) {
        this.text = text;
    }

    @Override
    @NotNull
    public String text() {
        return this.text;
    }

    @Override
    public boolean isConstant() {
        return true;
    }
}

