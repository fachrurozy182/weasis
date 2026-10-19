/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.attributes.filter;

import com.github.weisj.jsvg.attributes.HasMatchName;
import org.jetbrains.annotations.NotNull;

public enum BlendMode implements HasMatchName
{
    Normal,
    Multiply,
    Screen,
    Overlay,
    Darken,
    Lighten,
    ColorDodge("color-dodge"),
    ColorBurn("color-burn"),
    HardLight("hard-light"),
    SoftLight("soft-light"),
    Difference,
    Exclusion,
    Hue,
    Saturation,
    Color,
    Luminosity;

    @NotNull
    private final String matchName;

    private BlendMode(String matchName) {
        this.matchName = matchName;
    }

    private BlendMode() {
        this.matchName = this.name();
    }

    @Override
    @NotNull
    public String matchName() {
        return this.matchName;
    }
}

