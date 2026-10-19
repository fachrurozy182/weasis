/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.attributes.font;

import com.github.weisj.jsvg.nodes.text.Glyph;
import org.jetbrains.annotations.NotNull;

public interface SVGFont {
    public static float defaultFontSize() {
        return 10.0f;
    }

    @NotNull
    public static String defaultFontFamily() {
        return "Serif";
    }

    public static float exFromEm(float em) {
        return em / 2.0f;
    }

    public static float emFromEx(float ex) {
        return 2.0f * ex;
    }

    @NotNull
    public Glyph codepointGlyph(@NotNull String var1);

    @NotNull
    public String family();

    public int size();

    public float effectiveExHeight();

    public float effectiveEmHeight();

    public float mathematicalBaseline();

    public float hangingBaseline();

    public float romanBaseline();

    public float centerBaseline();

    public float middleBaseline();

    public float textUnderBaseline();

    public float textOverBaseline();
}

