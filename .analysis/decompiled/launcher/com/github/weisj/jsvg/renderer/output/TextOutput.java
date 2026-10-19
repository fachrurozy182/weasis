/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.renderer.output;

import com.github.weisj.jsvg.nodes.text.NullTextOutput;
import com.github.weisj.jsvg.renderer.RenderContext;
import java.awt.geom.AffineTransform;
import org.jetbrains.annotations.NotNull;

public interface TextOutput {
    public void codepoint(@NotNull String var1, @NotNull AffineTransform var2, @NotNull RenderContext var3);

    @NotNull
    public static TextOutput createDefault() {
        return NullTextOutput.INSTANCE;
    }

    public void beginText();

    public void glyphRunBreak();

    public void endText();
}

