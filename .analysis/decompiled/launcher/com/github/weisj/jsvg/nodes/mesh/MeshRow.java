/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.mesh;

import com.github.weisj.jsvg.nodes.Style;
import com.github.weisj.jsvg.nodes.container.ContainerNode;
import com.github.weisj.jsvg.nodes.mesh.MeshPatch;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent;
import org.jetbrains.annotations.NotNull;

@ElementCategories(value={})
@PermittedContent(categories={Category.Descriptive}, anyOf={MeshPatch.class, Style.class})
public final class MeshRow
extends ContainerNode {
    public static final String TAG = "meshrow";

    @Override
    @NotNull
    public String tagName() {
        return TAG;
    }
}

