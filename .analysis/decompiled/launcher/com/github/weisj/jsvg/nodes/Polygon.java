/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes;

import com.github.weisj.jsvg.nodes.AbstractPolyShape;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent;
import org.jetbrains.annotations.NotNull;

@ElementCategories(value={Category.BasicShape, Category.Graphic, Category.Shape})
@PermittedContent(categories={Category.Animation, Category.Descriptive})
public final class Polygon
extends AbstractPolyShape {
    public static final String TAG = "polygon";

    @Override
    @NotNull
    public String tagName() {
        return TAG;
    }

    @Override
    protected boolean doClose() {
        return true;
    }
}

