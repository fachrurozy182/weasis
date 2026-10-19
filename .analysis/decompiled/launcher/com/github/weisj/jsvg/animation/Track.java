/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.animation;

import com.github.weisj.jsvg.animation.Additive;
import com.github.weisj.jsvg.animation.AnimationValuesType;
import com.github.weisj.jsvg.animation.Fill;
import com.github.weisj.jsvg.animation.interpolation.DefaultInterpolator;
import com.github.weisj.jsvg.animation.interpolation.FloatInterpolator;
import com.github.weisj.jsvg.animation.interpolation.FloatListInterpolator;
import com.github.weisj.jsvg.animation.interpolation.PaintInterpolator;
import com.github.weisj.jsvg.animation.interpolation.TransformInterpolator;
import com.github.weisj.jsvg.animation.time.Duration;
import com.github.weisj.jsvg.animation.time.Interval;
import com.github.weisj.jsvg.parser.impl.AttributeNode;
import com.github.weisj.jsvg.parser.impl.SeparatorMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Track {
    @NotNull
    private final @NotNull List<@NotNull Interval> intervals;
    private final float repeatCount;
    private final Fill fill;
    private final DefaultInterpolator interpolator;

    private Track(@NotNull @NotNull List<@NotNull Interval> intervals, float repeatCount, Fill fill, AnimationValuesType valuesType, Additive additive) {
        this.intervals = intervals;
        this.repeatCount = repeatCount;
        this.fill = fill;
        this.interpolator = new DefaultInterpolator(valuesType, additive);
    }

    @Nullable
    public static Track parse(@NotNull AttributeNode attributeNode, @NotNull AnimationValuesType valuesType, @NotNull Additive additive) {
        List<Duration> begins = Track.parseBegin(attributeNode);
        Duration duration = attributeNode.getDuration("dur", Duration.INDEFINITE);
        List<Interval> intervals = begins.stream().map(b -> new Interval((Duration)b, b.plus(duration))).filter(Interval::isValid).sorted(Comparator.comparing(Interval::begin)).collect(Collectors.toList());
        String repeatCountStr = attributeNode.getValue("repeatCount");
        float repeatCount = "indefinite".equals(repeatCountStr) ? Float.POSITIVE_INFINITY : attributeNode.parser().parseFloat(repeatCountStr, 1.0f);
        if (intervals.isEmpty() || repeatCount <= 0.0f) {
            return null;
        }
        return new Track(intervals, repeatCount, attributeNode.getEnum("fill", Fill.REMOVE), valuesType, additive);
    }

    @NotNull
    private static List<Duration> parseBegin(@NotNull AttributeNode attributeNode) {
        List<Duration> begins;
        String[] beginsRaw = attributeNode.getStringList("begin", SeparatorMode.SEMICOLON_ONLY);
        if (beginsRaw.length > 0) {
            begins = new ArrayList<Duration>(beginsRaw.length);
            for (String s : beginsRaw) {
                Duration b = attributeNode.parser().parseDuration(s, null);
                if (b == null) continue;
                begins.add(b);
            }
        } else {
            begins = Collections.singletonList(Duration.ZERO);
        }
        return begins;
    }

    @NotNull
    public @NotNull List<@NotNull Interval> intervals() {
        return this.intervals;
    }

    public float repeatCount() {
        return this.repeatCount;
    }

    @NotNull
    public Fill fill() {
        return this.fill;
    }

    private int iterationCount(@NotNull Duration duration, long timestampMillis) {
        long durationMillis = duration.milliseconds();
        return (int)(timestampMillis / durationMillis);
    }

    private float iterationProgress(@NotNull Duration duration, long timestampMillis) {
        long durationMillis = duration.milliseconds();
        return (float)(timestampMillis % durationMillis) / (float)durationMillis;
    }

    @Nullable
    private Interval currentInterval(long timestamp) {
        ListIterator<@NotNull Interval> iterator = this.intervals.listIterator(this.intervals.size());
        while (iterator.hasPrevious()) {
            Interval interval = iterator.previous();
            if (interval.end().milliseconds() > timestamp) continue;
            return interval;
        }
        return null;
    }

    @NotNull
    public InterpolationProgress interpolationProgress(long timestamp, int valueCount) {
        float iterationProgress;
        if (valueCount == 0) {
            return InterpolationProgress.INITIAL;
        }
        Interval currentInterval = this.currentInterval(timestamp);
        if (currentInterval == null) {
            return InterpolationProgress.INITIAL;
        }
        long time = timestamp - currentInterval.begin().milliseconds();
        Duration duration = currentInterval.duration();
        int iterationCount = this.iterationCount(duration, time);
        float totalIteration = (float)iterationCount + (iterationProgress = this.iterationProgress(duration, time));
        if (totalIteration > this.repeatCount) {
            if (this.fill == Fill.FREEZE) {
                return new InterpolationProgress(valueCount - 1, 0.0f);
            }
            return InterpolationProgress.INITIAL;
        }
        int i = (int)Math.floor(iterationProgress * (float)(valueCount - 1));
        float t = (float)(valueCount - 1) * iterationProgress - (float)i;
        return new InterpolationProgress(i, t);
    }

    @NotNull
    public FloatInterpolator floatInterpolator() {
        return this.interpolator;
    }

    @NotNull
    public FloatListInterpolator floatListInterpolator() {
        return this.interpolator;
    }

    @NotNull
    public PaintInterpolator paintInterpolator() {
        return this.interpolator;
    }

    @NotNull
    public TransformInterpolator transformInterpolator() {
        return this.interpolator;
    }

    public static final class InterpolationProgress {
        public static final InterpolationProgress INITIAL = new InterpolationProgress(-1, 0.0f);
        private final int iterationIndex;
        private final float indexProgress;

        public InterpolationProgress(int iterationIndex, float indexProgress) {
            this.iterationIndex = iterationIndex;
            this.indexProgress = indexProgress;
        }

        public boolean isInitial() {
            return this.iterationIndex == -1;
        }

        public int iterationIndex() {
            return this.iterationIndex;
        }

        public float indexProgress() {
            return this.indexProgress;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            InterpolationProgress that = (InterpolationProgress)o;
            return this.iterationIndex == that.iterationIndex && Float.compare(this.indexProgress, that.indexProgress) == 0;
        }

        public int hashCode() {
            return Objects.hash(this.iterationIndex, Float.valueOf(this.indexProgress));
        }
    }
}

