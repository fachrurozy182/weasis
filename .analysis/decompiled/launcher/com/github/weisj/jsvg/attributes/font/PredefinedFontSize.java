/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.attributes.font;

import com.github.weisj.jsvg.attributes.HasMatchName;
import com.github.weisj.jsvg.attributes.font.FontSize;
import com.github.weisj.jsvg.attributes.font.SVGFont;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.geometry.size.Unit;
import org.jetbrains.annotations.NotNull;

public enum PredefinedFontSize implements HasMatchName,
FontSize
{
    xxSmall("xx-small", 0.6f),
    xSmall("x-small", 0.75f),
    small(0.8888889f),
    medium(1.0f),
    large(1.2f),
    xLarge("x-large", 1.5f),
    xxLarge("xx-large", 2.0f),
    xxxLarge("xxx-large", 3.0f),
    larger(1.3f),
    smaller(0.7f),
    Number(0.0f);

    @NotNull
    private final String matchName;
    private final float scalingFactor;

    private PredefinedFontSize(String matchName, float scalingFactor) {
        this.matchName = matchName;
        this.scalingFactor = scalingFactor;
    }

    private PredefinedFontSize(float scalingFactor) {
        this.scalingFactor = scalingFactor;
        this.matchName = this.name();
    }

    @Override
    @NotNull
    public String matchName() {
        return this.matchName;
    }

    @Override
    @NotNull
    public Length size(@NotNull Length parentSize) {
        if (this == Number) {
            throw new UnsupportedOperationException("Number font-size needs to parsed explicitly");
        }
        if (this == smaller || this == larger) {
            return parentSize.multiply(this.scalingFactor);
        }
        return Unit.RAW.valueOf(SVGFont.defaultFontSize() * this.scalingFactor);
    }
}

