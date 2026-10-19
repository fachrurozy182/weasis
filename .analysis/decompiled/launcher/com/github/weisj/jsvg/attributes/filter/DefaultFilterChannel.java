/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.attributes.filter;

import com.github.weisj.jsvg.attributes.filter.FilterChannelKey;
import org.jetbrains.annotations.NotNull;

public enum DefaultFilterChannel implements FilterChannelKey
{
    SourceGraphic,
    SourceAlpha,
    BackgroundImage,
    BackgroundAlpha,
    FillPaint,
    StrokePaint,
    LastResult;


    @Override
    @NotNull
    public Object key() {
        if (this == LastResult) {
            return this;
        }
        return this.toString();
    }
}

