/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.parser.css;

import com.github.weisj.jsvg.parser.css.StyleSheet;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface CssParser {
    @NotNull
    public StyleSheet parse(@NotNull List<char[]> var1);
}

