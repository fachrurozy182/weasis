/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.prototype;

import com.github.weisj.jsvg.parser.impl.AttributeNode;
import com.github.weisj.jsvg.renderer.RenderContext;
import com.github.weisj.jsvg.renderer.output.Output;
import org.jetbrains.annotations.NotNull;

public interface Renderable {
    default public boolean requiresInstantiation() {
        return false;
    }

    public boolean isVisible(@NotNull RenderContext var1);

    public void render(@NotNull RenderContext var1, @NotNull Output var2);

    default public boolean parseIsVisible(@NotNull AttributeNode node) {
        return Renderable.parseVisibility(node);
    }

    public static boolean parseVisibility(@NotNull AttributeNode node) {
        return !"none".equals(node.getValue("display")) && !"hidden".equals(node.getValue("visibility")) && !"collapse".equals(node.getValue("visibility"));
    }
}

