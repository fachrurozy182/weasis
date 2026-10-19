/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.attributes.text;

import com.github.weisj.jsvg.attributes.HasMatchName;
import org.jetbrains.annotations.NotNull;

public enum DominantBaseline implements HasMatchName
{
    Auto,
    Ideographic,
    Alphabetic,
    Hanging,
    Mathematical,
    Central,
    Middle,
    TextAfterEdge("text-after-edge"),
    TextBottom("text-bottom"),
    TextBeforeEdge("text-before-edge"),
    TextTop("text-top");

    @NotNull
    private final String matchName;

    private DominantBaseline(String matchName) {
        this.matchName = matchName;
    }

    private DominantBaseline() {
        this.matchName = this.name();
    }

    @Override
    @NotNull
    public String matchName() {
        return this.matchName;
    }
}

