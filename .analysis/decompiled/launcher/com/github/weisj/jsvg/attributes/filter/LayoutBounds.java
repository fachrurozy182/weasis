/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.attributes.filter;

import com.github.weisj.jsvg.geometry.size.FloatInsets;
import com.github.weisj.jsvg.geometry.util.GeometryUtil;
import com.github.weisj.jsvg.nodes.filter.FilterLayoutContext;
import java.awt.geom.Rectangle2D;
import java.util.function.BiFunction;
import org.jetbrains.annotations.NotNull;

public final class LayoutBounds {
    @NotNull
    private final Data data;
    @NotNull
    private final @NotNull BiFunction<@NotNull Data, ComputeFlags, @NotNull Data> transformer;
    private final ComputeFlags additionalFlags;

    public LayoutBounds(@NotNull Rectangle2D bounds, @NotNull FloatInsets clipBoundsEscapeInsets) {
        this.data = new Data(bounds, clipBoundsEscapeInsets);
        this.transformer = (d, f) -> d;
        this.additionalFlags = new ComputeFlags(false);
    }

    private LayoutBounds(@NotNull Data data, @NotNull @NotNull BiFunction<@NotNull Data, ComputeFlags, @NotNull Data> transformer, @NotNull ComputeFlags flags) {
        this.data = data;
        this.transformer = transformer;
        this.additionalFlags = flags;
    }

    @NotNull
    public LayoutBounds transform(@NotNull @NotNull BiFunction<@NotNull Data, ComputeFlags, @NotNull Data> newTransformer) {
        return new LayoutBounds(this.data, (data, flags) -> {
            Data newData = this.transformer.apply((Data)data, (ComputeFlags)flags);
            return (Data)newTransformer.apply(newData, (ComputeFlags)flags);
        }, this.additionalFlags);
    }

    @NotNull
    public LayoutBounds withFlags(@NotNull ComputeFlags flags) {
        return new LayoutBounds(this.data, this.transformer, this.additionalFlags.or(flags));
    }

    @NotNull
    public Data resolve(@NotNull ComputeFlags flags) {
        return this.transformer.apply(this.data, flags.or(this.additionalFlags));
    }

    @NotNull
    public LayoutBounds union(@NotNull LayoutBounds other) {
        return this.transform((data, flags) -> {
            Data otherData = other.resolve((ComputeFlags)flags);
            return new Data(((Data)data).bounds.createUnion(otherData.bounds), GeometryUtil.max(((Data)data).clipBoundsEscapeInsets, otherData.clipBoundsEscapeInsets));
        });
    }

    @NotNull
    public LayoutBounds grow(float horizontal, float vertical, @NotNull FilterLayoutContext context) {
        return this.transform((data, flags) -> {
            FloatInsets insets = ((Data)data).clipBoundsEscapeInsets;
            Rectangle2D clipBounds = context.clipBounds();
            FloatInsets growInsets = new FloatInsets(vertical, horizontal, vertical, horizontal);
            Rectangle2D newBounds = GeometryUtil.grow(((Data)data).bounds, growInsets);
            FloatInsets ins = GeometryUtil.min(GeometryUtil.overhangInsets(clipBounds, newBounds), growInsets);
            return new Data(newBounds, GeometryUtil.max(insets, ins));
        });
    }

    @NotNull
    public LayoutBounds translate(float dx, float dy, @NotNull FilterLayoutContext context) {
        return this.transform((data, flags) -> {
            FloatInsets insets = ((Data)data).clipBoundsEscapeInsets;
            FloatInsets offsetInsets = new FloatInsets(Math.max(dy, 0.0f), Math.max(dx, 0.0f), Math.max(-dy, 0.0f), Math.max(-dx, 0.0f));
            Rectangle2D newBounds = GeometryUtil.grow(((Data)data).bounds, offsetInsets);
            Rectangle2D clipBounds = context.clipBounds();
            FloatInsets ins = GeometryUtil.max(GeometryUtil.overhangInsets(clipBounds, ((Data)data).bounds), offsetInsets);
            return new Data(newBounds, GeometryUtil.max(insets, ins));
        });
    }

    public String toString() {
        return "LayoutBounds{data=" + this.data + ", transformer=" + this.transformer + '}';
    }

    public static final class Data {
        @NotNull
        private final Rectangle2D bounds;
        @NotNull
        private final FloatInsets clipBoundsEscapeInsets;

        private Data(@NotNull Rectangle2D bounds, @NotNull FloatInsets clipBoundsEscapeInsets) {
            this.bounds = bounds;
            this.clipBoundsEscapeInsets = clipBoundsEscapeInsets;
        }

        @NotNull
        public FloatInsets clipBoundsEscapeInsets() {
            return this.clipBoundsEscapeInsets;
        }

        @NotNull
        public Rectangle2D bounds() {
            return this.bounds;
        }

        public String toString() {
            return "Data{bounds=" + GeometryUtil.compactRepresentation(this.bounds) + ", clipBoundsEscapeInsets=" + this.clipBoundsEscapeInsets + '}';
        }
    }

    public static class ComputeFlags {
        @NotNull
        public static final ComputeFlags INITIAL = new ComputeFlags(false);
        public final boolean operatesOnWholeFilterRegion;

        public ComputeFlags(boolean operatesOnWholeFilterRegion) {
            this.operatesOnWholeFilterRegion = operatesOnWholeFilterRegion;
        }

        @NotNull
        public ComputeFlags or(@NotNull ComputeFlags other) {
            return new ComputeFlags(this.operatesOnWholeFilterRegion || other.operatesOnWholeFilterRegion);
        }
    }
}

