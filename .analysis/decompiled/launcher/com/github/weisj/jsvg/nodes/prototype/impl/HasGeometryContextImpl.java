/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.nodes.prototype.impl;

import com.github.weisj.jsvg.attributes.Animatable;
import com.github.weisj.jsvg.attributes.Coordinate;
import com.github.weisj.jsvg.attributes.Inherited;
import com.github.weisj.jsvg.attributes.transform.TransformBox;
import com.github.weisj.jsvg.attributes.value.LengthValue;
import com.github.weisj.jsvg.attributes.value.TransformValue;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.nodes.ClipPath;
import com.github.weisj.jsvg.nodes.Mask;
import com.github.weisj.jsvg.nodes.filter.Filter;
import com.github.weisj.jsvg.nodes.prototype.HasGeometryContext;
import com.github.weisj.jsvg.parser.impl.AttributeNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class HasGeometryContextImpl
implements HasGeometryContext {
    @Nullable
    private final TransformValue transform;
    @NotNull
    private final Coordinate<LengthValue> transformOrigin;
    @NotNull
    private final TransformBox transformBox;
    @Nullable
    private final ClipPath clipPath;
    @Nullable
    private final Mask mask;
    @Nullable
    private final Filter filter;

    private HasGeometryContextImpl(@Nullable TransformValue transform, @NotNull Coordinate<LengthValue> transformOrigin, @NotNull TransformBox transformBox, @Nullable ClipPath clipPath, @Nullable Mask mask, @Nullable Filter filter) {
        this.transform = transform;
        this.transformOrigin = transformOrigin;
        this.transformBox = transformBox;
        this.clipPath = clipPath;
        this.mask = mask;
        this.filter = filter;
    }

    @NotNull
    public static HasGeometryContext parse(@NotNull AttributeNode attributeNode) {
        return new HasGeometryContextImpl(attributeNode.parseTransform("transform", Inherited.NO, Animatable.YES), HasGeometryContextImpl.parseTransformOrigin(attributeNode, attributeNode.getStringList("transform-origin")), attributeNode.getEnum("transform-box", TransformBox.ViewBox), attributeNode.getClipPath(), attributeNode.getMask(), attributeNode.getFilter());
    }

    @NotNull
    private static Coordinate<LengthValue> parseTransformOrigin(@NotNull AttributeNode attributeNode, @NotNull @NotNull String @NotNull [] parts) {
        String originY;
        String originX;
        if (parts.length == 0) {
            return new Coordinate<LengthValue>(Length.ZERO, Length.ZERO);
        }
        if (parts.length == 1) {
            String val = parts[0];
            if (attributeNode.isVerticalKeyword(val)) {
                originX = "center";
                originY = val;
            } else {
                originX = val;
                originY = "center";
            }
        } else {
            String first = parts[0];
            String second = parts[1];
            if (attributeNode.isVerticalKeyword(first) || attributeNode.isHorizontalKeyword(second)) {
                originX = second;
                originY = first;
            } else {
                originX = first;
                originY = second;
            }
        }
        return new Coordinate<LengthValue>(attributeNode.getHorizontalReferenceLength(originX), attributeNode.getVerticalReferenceLength(originY));
    }

    @Override
    @Nullable
    public ClipPath clipPath() {
        return this.clipPath;
    }

    @Override
    @Nullable
    public Mask mask() {
        return this.mask;
    }

    @Override
    @Nullable
    public Filter filter() {
        return this.filter;
    }

    @Override
    @Nullable
    public TransformValue transform() {
        return this.transform;
    }

    @Override
    @NotNull
    public TransformBox transformBox() {
        return this.transformBox;
    }

    @Override
    @NotNull
    public Coordinate<LengthValue> transformOrigin() {
        return this.transformOrigin;
    }
}

