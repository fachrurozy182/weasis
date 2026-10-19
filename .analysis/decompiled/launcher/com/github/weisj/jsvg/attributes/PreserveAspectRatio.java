/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.attributes;

import com.github.weisj.jsvg.parser.impl.AttributeParser;
import com.github.weisj.jsvg.parser.impl.SeparatorMode;
import com.github.weisj.jsvg.view.FloatSize;
import com.github.weisj.jsvg.view.ViewBox;
import java.awt.geom.AffineTransform;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PreserveAspectRatio {
    @NotNull
    public final Align align;
    @NotNull
    public final MeetOrSlice meetOrSlice;

    private PreserveAspectRatio(@NotNull Align align, @NotNull MeetOrSlice meetOrSlice) {
        this.align = align;
        this.meetOrSlice = meetOrSlice;
    }

    @NotNull
    public static PreserveAspectRatio none() {
        return new PreserveAspectRatio(Align.None, MeetOrSlice.Meet);
    }

    @NotNull
    public static PreserveAspectRatio forDisplay() {
        return new PreserveAspectRatio(Align.xMidYMid, MeetOrSlice.Meet);
    }

    @NotNull
    public static PreserveAspectRatio parse(@Nullable String preserveAspectRation, @NotNull AttributeParser parser) {
        return PreserveAspectRatio.parse(preserveAspectRation, null, parser);
    }

    @NotNull
    public static PreserveAspectRatio parse(@Nullable String preserveAspectRation, @Nullable PreserveAspectRatio fallback, @NotNull AttributeParser parser) {
        Align align = Align.xMidYMid;
        MeetOrSlice meetOrSlice = MeetOrSlice.Meet;
        if (preserveAspectRation == null) {
            return fallback != null ? fallback : new PreserveAspectRatio(align, meetOrSlice);
        }
        String[] components = parser.parseStringList(preserveAspectRation, SeparatorMode.COMMA_AND_WHITESPACE);
        if (components.length < 1 || components.length > 2) {
            throw new IllegalArgumentException("Too many arguments specified: " + preserveAspectRation);
        }
        align = parser.parseEnum(components[0], align);
        if (components.length > 1) {
            meetOrSlice = parser.parseEnum(components[1], meetOrSlice);
        }
        return new PreserveAspectRatio(align, meetOrSlice);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PreserveAspectRatio)) {
            return false;
        }
        PreserveAspectRatio that = (PreserveAspectRatio)o;
        return this.align == that.align && this.meetOrSlice == that.meetOrSlice;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.align, this.meetOrSlice});
    }

    @NotNull
    public AffineTransform computeViewportTransform(@NotNull FloatSize size, @NotNull ViewBox viewBox) {
        AffineTransform viewTransform = new AffineTransform();
        if (this.align == Align.None) {
            viewTransform.scale(size.width / viewBox.width, size.height / viewBox.height);
        } else {
            double xScale = size.width / viewBox.width;
            double yScale = size.height / viewBox.height;
            switch (this.meetOrSlice.ordinal()) {
                case 0: {
                    xScale = yScale = Math.min(xScale, yScale);
                    break;
                }
                case 1: {
                    xScale = yScale = Math.max(xScale, yScale);
                    break;
                }
                default: {
                    throw new IllegalStateException();
                }
            }
            viewTransform.translate(this.align.xAlign.align(size.width, (double)viewBox.width * xScale), this.align.yAlign.align(size.height, (double)viewBox.height * yScale));
            viewTransform.scale(xScale, yScale);
        }
        viewTransform.translate(-viewBox.x, -viewBox.y);
        return viewTransform;
    }

    public String toString() {
        return "PreserveAspectRatio{align=" + (Object)((Object)this.align) + ", meetOrSlice=" + (Object)((Object)this.meetOrSlice) + '}';
    }

    public static enum Align {
        None(AlignType.Min, AlignType.Min),
        xMinYMin(AlignType.Min, AlignType.Min),
        xMidYMin(AlignType.Mid, AlignType.Min),
        xMaxYMin(AlignType.Max, AlignType.Min),
        xMinYMid(AlignType.Min, AlignType.Mid),
        xMidYMid(AlignType.Mid, AlignType.Mid),
        xMaxYMid(AlignType.Max, AlignType.Mid),
        xMinYMax(AlignType.Min, AlignType.Max),
        xMidYMax(AlignType.Mid, AlignType.Max),
        xMaxYMax(AlignType.Max, AlignType.Max);

        @NotNull
        private final AlignType xAlign;
        @NotNull
        private final AlignType yAlign;

        private Align(AlignType xAlign, AlignType yAlign) {
            this.xAlign = xAlign;
            this.yAlign = yAlign;
        }

        public String toString() {
            return this.name() + "{" + (Object)((Object)this.xAlign) + ", " + (Object)((Object)this.yAlign) + "}";
        }
    }

    public static enum MeetOrSlice {
        Meet,
        Slice;

    }

    private static enum AlignType {
        Min{

            @Override
            double align(double size1, double size2) {
                return 0.0;
            }
        }
        ,
        Mid{

            @Override
            double align(double size1, double size2) {
                return (size1 - size2) / 2.0;
            }
        }
        ,
        Max{

            @Override
            double align(double size1, double size2) {
                return size1 - size2;
            }
        };


        abstract double align(double var1, double var3);
    }
}

