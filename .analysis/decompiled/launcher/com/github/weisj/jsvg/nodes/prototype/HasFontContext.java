/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.prototype;

import com.github.weisj.jsvg.attributes.font.MeasurableFontSpec;
import com.github.weisj.jsvg.nodes.prototype.Mutator;
import org.jetbrains.annotations.NotNull;

public interface HasFontContext {
    @NotNull
    public Mutator<MeasurableFontSpec> fontSpec();
}

