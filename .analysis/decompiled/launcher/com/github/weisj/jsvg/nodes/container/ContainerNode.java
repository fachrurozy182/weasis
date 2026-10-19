/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.container;

import com.github.weisj.jsvg.nodes.SVGNode;
import com.github.weisj.jsvg.nodes.container.BaseContainerNode;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public abstract class ContainerNode
extends BaseContainerNode<SVGNode> {
    private final List<@NotNull SVGNode> children = new ArrayList<SVGNode>();

    @Override
    protected void doAdd(@NotNull SVGNode node) {
        this.children.add(node);
    }

    @Override
    public List<? extends @NotNull SVGNode> children() {
        return this.children;
    }
}

