/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.nodes.prototype;

import com.github.weisj.jsvg.attributes.Coordinate;
import com.github.weisj.jsvg.attributes.transform.TransformBox;
import com.github.weisj.jsvg.attributes.value.LengthValue;
import com.github.weisj.jsvg.attributes.value.TransformValue;
import com.github.weisj.jsvg.nodes.ClipPath;
import com.github.weisj.jsvg.nodes.Mask;
import com.github.weisj.jsvg.nodes.filter.Filter;
import com.github.weisj.jsvg.nodes.prototype.HasClip;
import com.github.weisj.jsvg.nodes.prototype.HasFilter;
import com.github.weisj.jsvg.nodes.prototype.Transformable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface HasGeometryContext
extends Transformable,
HasClip,
HasFilter {

    public static interface ByDelegate
    extends HasGeometryContext {
        @NotNull
        public HasGeometryContext geometryContextDelegate();

        @Override
        @Nullable
        default public ClipPath clipPath() {
            return this.geometryContextDelegate().clipPath();
        }

        @Override
        @Nullable
        default public Mask mask() {
            return this.geometryContextDelegate().mask();
        }

        @Override
        @Nullable
        default public Filter filter() {
            return this.geometryContextDelegate().filter();
        }

        @Override
        @Nullable
        default public TransformValue transform() {
            return this.geometryContextDelegate().transform();
        }

        @Override
        default public TransformBox transformBox() {
            return this.geometryContextDelegate().transformBox();
        }

        @Override
        @NotNull
        default public Coordinate<LengthValue> transformOrigin() {
            return this.geometryContextDelegate().transformOrigin();
        }
    }
}

