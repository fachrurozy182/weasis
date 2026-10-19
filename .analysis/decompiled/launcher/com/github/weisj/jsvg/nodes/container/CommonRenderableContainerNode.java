/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.MustBeInvokedByOverriders
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.container;

import com.github.weisj.jsvg.nodes.SVGNode;
import com.github.weisj.jsvg.nodes.container.BaseContainerNode;
import com.github.weisj.jsvg.nodes.prototype.HasContext;
import com.github.weisj.jsvg.nodes.prototype.HasGeometryContext;
import com.github.weisj.jsvg.nodes.prototype.Renderable;
import com.github.weisj.jsvg.nodes.prototype.impl.HasContextImpl;
import com.github.weisj.jsvg.nodes.prototype.impl.HasGeometryContextImpl;
import com.github.weisj.jsvg.parser.impl.AttributeNode;
import com.github.weisj.jsvg.renderer.RenderContext;
import com.github.weisj.jsvg.renderer.impl.NodeRenderer;
import com.github.weisj.jsvg.renderer.output.Output;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;

public abstract class CommonRenderableContainerNode
extends BaseContainerNode<SVGNode>
implements Renderable,
HasGeometryContext.ByDelegate,
HasContext.ByDelegate {
    private final List<@NotNull SVGNode> children = new ArrayList<SVGNode>();
    private boolean isVisible;
    private HasGeometryContext geometryContext;
    private HasContext context;

    @Override
    @MustBeInvokedByOverriders
    public void build(@NotNull AttributeNode attributeNode) {
        super.build(attributeNode);
        this.isVisible = this.parseIsVisible(attributeNode);
        this.geometryContext = HasGeometryContextImpl.parse(attributeNode);
        this.context = HasContextImpl.parse(attributeNode);
    }

    @Override
    @NotNull
    public HasGeometryContext geometryContextDelegate() {
        return this.geometryContext;
    }

    @Override
    @NotNull
    public HasContext contextDelegate() {
        return this.context;
    }

    @Override
    protected void doAdd(@NotNull SVGNode node) {
        this.children.add(node);
    }

    @Override
    public List<? extends @NotNull SVGNode> children() {
        return this.children;
    }

    @Override
    public void render(@NotNull RenderContext context, @NotNull Output output) {
        for (SVGNode sVGNode : this.children()) {
            NodeRenderer.renderNode(sVGNode, context, output);
        }
    }

    @Override
    public boolean isVisible(@NotNull RenderContext context) {
        return this.isVisible;
    }
}

