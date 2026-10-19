/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.attributes.value;

import com.github.weisj.jsvg.renderer.MeasureContext;
import java.awt.Color;
import org.jetbrains.annotations.NotNull;

public interface ColorValue {
    @NotNull
    public Color get(@NotNull MeasureContext var1);
}

