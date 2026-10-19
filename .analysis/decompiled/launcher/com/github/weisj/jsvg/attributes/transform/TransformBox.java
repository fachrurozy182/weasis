/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.attributes.transform;

import com.github.weisj.jsvg.attributes.HasMatchName;
import org.jetbrains.annotations.NotNull;

public enum TransformBox implements HasMatchName
{
    FillBox("fill-box"),
    StrokeBox("stroke-box"),
    ViewBox("view-box");

    @NotNull
    private final String matchName;

    private TransformBox(String matchName) {
        this.matchName = matchName;
    }

    @Override
    @NotNull
    public String matchName() {
        return this.matchName;
    }
}

