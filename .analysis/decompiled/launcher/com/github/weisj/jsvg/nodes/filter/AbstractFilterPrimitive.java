/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.MustBeInvokedByOverriders
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.filter;

import com.github.weisj.jsvg.attributes.ColorInterpolation;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.nodes.AbstractSVGNode;
import com.github.weisj.jsvg.nodes.filter.FilterContext;
import com.github.weisj.jsvg.nodes.filter.FilterPrimitive;
import com.github.weisj.jsvg.nodes.filter.FilterPrimitiveBase;
import com.github.weisj.jsvg.parser.impl.AttributeNode;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractFilterPrimitive
extends AbstractSVGNode
implements FilterPrimitive {
    private FilterPrimitiveBase filterPrimitiveBase;

    @Override
    @MustBeInvokedByOverriders
    public void build(@NotNull AttributeNode attributeNode) {
        super.build(attributeNode);
        this.filterPrimitiveBase = new FilterPrimitiveBase(attributeNode);
    }

    @NotNull
    protected final FilterPrimitiveBase impl() {
        return this.filterPrimitiveBase;
    }

    @Override
    @NotNull
    public Length x() {
        return this.impl().x;
    }

    @Override
    @NotNull
    public Length y() {
        return this.impl().y;
    }

    @Override
    @NotNull
    public Length width() {
        return this.impl().width;
    }

    @Override
    @NotNull
    public Length height() {
        return this.impl().height;
    }

    @Override
    public ColorInterpolation colorInterpolation(@NotNull FilterContext filterContext) {
        return this.impl().colorInterpolation(filterContext);
    }
}

