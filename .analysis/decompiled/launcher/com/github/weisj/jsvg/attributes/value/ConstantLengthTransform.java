/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.attributes.value;

import com.github.weisj.jsvg.attributes.transform.TransformPart;
import com.github.weisj.jsvg.attributes.value.TransformValue;
import com.github.weisj.jsvg.renderer.MeasureContext;
import java.awt.geom.AffineTransform;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class ConstantLengthTransform
implements TransformValue {
    public static final ConstantLengthTransform IDENTITY = new ConstantLengthTransform(Collections.emptyList());
    public static final ConstantLengthTransform INHERITED = new ConstantLengthTransform(Collections.emptyList());
    @NotNull
    private final @NotNull List<@NotNull TransformPart> parts;

    public ConstantLengthTransform(@NotNull @NotNull List<@NotNull TransformPart> parts) {
        this.parts = parts;
    }

    @Override
    @NotNull
    public AffineTransform get(@NotNull MeasureContext context) {
        AffineTransform transform = new AffineTransform();
        for (TransformPart part : this.parts) {
            part.applyToTransform(transform, context);
        }
        return transform;
    }
}

