/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.filter;

import com.github.weisj.jsvg.attributes.filter.DefaultFilterChannel;
import com.github.weisj.jsvg.attributes.filter.FilterChannelKey;
import com.github.weisj.jsvg.nodes.AbstractSVGNode;
import com.github.weisj.jsvg.nodes.animation.Animate;
import com.github.weisj.jsvg.nodes.animation.Set;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent;
import com.github.weisj.jsvg.parser.impl.AttributeNode;
import org.jetbrains.annotations.NotNull;

@ElementCategories(value={Category.None})
@PermittedContent(anyOf={Animate.class, Set.class})
public final class FeMergeNode
extends AbstractSVGNode {
    public static final String TAG = "feMergeNode";
    private FilterChannelKey inputChannel;

    @Override
    @NotNull
    public String tagName() {
        return TAG;
    }

    @NotNull
    public FilterChannelKey inputChannel() {
        return this.inputChannel;
    }

    @Override
    public void build(@NotNull AttributeNode attributeNode) {
        super.build(attributeNode);
        this.inputChannel = attributeNode.getFilterChannelKey("in", DefaultFilterChannel.LastResult);
    }
}

