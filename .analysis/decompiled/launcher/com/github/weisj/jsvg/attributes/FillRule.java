/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.attributes;

import com.github.weisj.jsvg.parser.impl.AttributeNode;
import org.jetbrains.annotations.NotNull;

public enum FillRule {
    Nonzero(1),
    EvenOdd(0),
    Inherit(FillRule.Nonzero.awtWindingRule);

    public final int awtWindingRule;

    private FillRule(int awtWindingRule) {
        this.awtWindingRule = awtWindingRule;
    }

    @NotNull
    public static FillRule parse(@NotNull AttributeNode attributeNode) {
        return attributeNode.getEnum("fill-rule", Inherit);
    }
}

