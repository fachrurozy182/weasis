/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.animation;

import com.github.weisj.jsvg.nodes.MetaSVGNode;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent;
import org.jetbrains.annotations.NotNull;

@ElementCategories(value={Category.Animation})
@PermittedContent(categories={Category.Descriptive})
public final class Set
extends MetaSVGNode {
    public static final String TAG = "set";

    @Override
    @NotNull
    public String tagName() {
        return TAG;
    }
}

