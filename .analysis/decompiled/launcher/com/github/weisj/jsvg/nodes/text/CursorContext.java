/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.text;

import com.github.weisj.jsvg.nodes.text.GlyphCursor;
import org.jetbrains.annotations.NotNull;

public interface CursorContext {
    @NotNull
    public GlyphCursor createLocalCursor(boolean var1, @NotNull GlyphCursor var2);

    public void cleanUpLocalCursor(@NotNull GlyphCursor var1, @NotNull GlyphCursor var2);
}

