/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.attributes;

import com.github.weisj.jsvg.attributes.HasMatchName;
import org.jetbrains.annotations.NotNull;

public enum ColorInterpolation implements HasMatchName
{
    S_RGB("sRGB"),
    LinearRGB("linearRGB"),
    Auto("auto"),
    Inherit("inherit");

    @NotNull
    private final String matchName;

    private ColorInterpolation(String matchName) {
        this.matchName = matchName;
    }

    @Override
    @NotNull
    public String matchName() {
        return this.matchName;
    }
}

