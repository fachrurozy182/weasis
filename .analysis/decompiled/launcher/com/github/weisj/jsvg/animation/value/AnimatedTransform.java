/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.animation.value;

import com.github.weisj.jsvg.animation.Track;
import com.github.weisj.jsvg.attributes.transform.TransformPart;
import com.github.weisj.jsvg.attributes.value.ConstantLengthTransform;
import com.github.weisj.jsvg.attributes.value.TransformValue;
import com.github.weisj.jsvg.renderer.MeasureContext;
import java.awt.geom.AffineTransform;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class AnimatedTransform
implements TransformValue {
    @NotNull
    private final Track track;
    @NotNull
    private final TransformValue initial;
    @NotNull
    private final @NotNull TransformPart @NotNull [] values;
    private AffineTransform current;
    private long currentTimestamp = -1L;

    public AnimatedTransform(@NotNull Track track, @NotNull TransformValue initial, @NotNull @NotNull TransformPart @NotNull [] values) {
        this.track = track;
        this.initial = initial;
        this.values = values;
    }

    @NotNull
    public TransformValue initial() {
        return this.initial;
    }

    @NotNull
    public AnimatedTransform derive(@NotNull TransformValue initialValue) {
        if (this.initial != ConstantLengthTransform.INHERITED) {
            return this;
        }
        return new AnimatedTransform(this.track, initialValue, this.values);
    }

    @NotNull
    private AffineTransform current(@NotNull MeasureContext context) {
        long timestamp = context.timestamp();
        if (this.currentTimestamp == timestamp) {
            return this.current;
        }
        this.currentTimestamp = timestamp;
        this.current = this.computeCurrent(context, timestamp);
        return this.current;
    }

    @NotNull
    private AffineTransform computeCurrent(@NotNull MeasureContext context, long timestamp) {
        Track.InterpolationProgress progress = this.track.interpolationProgress(timestamp, this.values.length);
        if (progress.isInitial()) {
            return Objects.requireNonNull(this.initial).get(context);
        }
        int i = progress.iterationIndex();
        assert (i >= 0);
        assert (this.values.length > 0);
        if (i >= this.values.length - 1) {
            AffineTransform transform = new AffineTransform();
            this.values[this.values.length - 1].applyToTransform(transform, context);
            return transform;
        }
        TransformPart start = this.values[i];
        TransformPart end = this.values[i + 1];
        float t = progress.indexProgress();
        return this.track.transformInterpolator().interpolate(context, this.initial, start, end, t);
    }

    @Override
    @NotNull
    public AffineTransform get(@NotNull MeasureContext context) {
        return this.current(context);
    }
}

