/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Contract
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.parser.impl;

import com.github.weisj.jsvg.animation.time.Duration;
import com.github.weisj.jsvg.animation.time.TimeUnit;
import com.github.weisj.jsvg.attributes.HasMatchName;
import com.github.weisj.jsvg.attributes.SuffixUnit;
import com.github.weisj.jsvg.attributes.transform.TransformPart;
import com.github.weisj.jsvg.attributes.value.PercentageDimension;
import com.github.weisj.jsvg.geometry.size.Angle;
import com.github.weisj.jsvg.geometry.size.AngleUnit;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.geometry.size.Percentage;
import com.github.weisj.jsvg.geometry.size.Unit;
import com.github.weisj.jsvg.logging.Logger;
import com.github.weisj.jsvg.logging.impl.LogFactory;
import com.github.weisj.jsvg.paint.SVGPaint;
import com.github.weisj.jsvg.parser.NumberListSplitter;
import com.github.weisj.jsvg.parser.PaintParser;
import com.github.weisj.jsvg.parser.impl.AttributeNode;
import com.github.weisj.jsvg.parser.impl.ListSplitter;
import com.github.weisj.jsvg.parser.impl.ParserUtil;
import com.github.weisj.jsvg.util.AttributeUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AttributeParser {
    private static final Logger LOGGER = LogFactory.createLogger(AttributeParser.class);
    @NotNull
    private final PaintParser paintParser;

    public AttributeParser(@NotNull PaintParser paintParser) {
        this.paintParser = paintParser;
    }

    @Contract(value="_,!null,_ -> !null")
    @Nullable
    public Length parseLength(@Nullable String value, @Nullable Length fallback, @NotNull PercentageDimension dimension) {
        return this.parseSuffixUnit(value, Unit.RAW, fallback, u -> {
            if (u == Unit.PERCENTAGE) {
                switch (dimension) {
                    case WIDTH: {
                        return Unit.PERCENTAGE_WIDTH;
                    }
                    case HEIGHT: {
                        return Unit.PERCENTAGE_HEIGHT;
                    }
                    case LENGTH: {
                        return Unit.PERCENTAGE_LENGTH;
                    }
                    case CUSTOM: {
                        return Unit.PERCENTAGE;
                    }
                    case NONE: {
                        return null;
                    }
                }
            }
            return u;
        });
    }

    @Contract(value="_,!null -> !null")
    @Nullable
    public Duration parseTimeOffsetValue(@Nullable String value, @Nullable Duration fallback) {
        return this.parseSuffixUnit(value, TimeUnit.Raw, fallback, u -> u);
    }

    @Contract(value="_,!null -> !null")
    @Nullable
    public Duration parseDuration(@Nullable String value, @Nullable Duration fallback) {
        if (value == null) {
            return fallback;
        }
        if ("indefinite".equals(value)) {
            return Duration.INDEFINITE;
        }
        Duration timeCount = this.parseSuffixUnit(value, TimeUnit.Raw, null, u -> u);
        if (timeCount != null) {
            return timeCount;
        }
        return fallback;
    }

    @Contract(value="_,!null -> !null")
    @Nullable
    public Percentage parsePercentage(@Nullable String value, @Nullable Percentage fallback) {
        return this.parsePercentage(value, fallback, 0.0f, 1.0f);
    }

    @Contract(value="_,!null,_,_ -> !null")
    @Nullable
    public Percentage parsePercentage(@Nullable String value, @Nullable Percentage fallback, float min, float max) {
        if (value == null) {
            return fallback;
        }
        try {
            float parsed = value.endsWith("%") ? Float.parseFloat(value.substring(0, value.length() - 1)) / 100.0f : Float.parseFloat(value);
            return new Percentage(Math.max(min, Math.min(max, parsed)));
        }
        catch (NumberFormatException e) {
            return fallback;
        }
    }

    @Contract(value="_,_,!null,_ -> !null")
    @Nullable
    private <U, V> V parseSuffixUnit(@Nullable String value, @NotNull SuffixUnit<U, V> defaultUnit, @Nullable V fallback, @NotNull UnaryOperator<@Nullable SuffixUnit<U, V>> unitMapper) {
        int i;
        if (value == null) {
            return fallback;
        }
        SuffixUnit<U, V> unit = defaultUnit;
        String lower = value.toLowerCase(Locale.ENGLISH);
        for (i = lower.length() - 1; i >= 0 && !Character.isDigit(lower.charAt(i)); --i) {
        }
        String suffix = lower.substring(i + 1);
        for (SuffixUnit<U, V> u : defaultUnit.units()) {
            if (!suffix.equals(u.suffix())) continue;
            unit = u;
            break;
        }
        if ((unit = (SuffixUnit<U, V>)unitMapper.apply(unit)) == null) {
            return fallback;
        }
        String str = lower.substring(0, lower.length() - unit.suffix().length());
        try {
            return unit.valueOf(Float.parseFloat(str));
        }
        catch (NumberFormatException e) {
            return fallback;
        }
    }

    public int parseInt(@Nullable String value, int fallback) {
        return ParserUtil.parseInt(value, fallback);
    }

    @Contract(value="_,!null -> !null")
    @Nullable
    public Length parseNumber(@Nullable String value, @Nullable Length fallback) {
        return ParserUtil.parseNumber(value, fallback);
    }

    public float parseFloat(@Nullable String value, float fallback) {
        return ParserUtil.parseFloat(value, fallback);
    }

    @NotNull
    public Angle parseAngle(@Nullable String value, @NotNull Angle fallback) {
        if (value == null) {
            return fallback;
        }
        AngleUnit unit = AngleUnit.Raw;
        String lower = value.toLowerCase(Locale.ENGLISH);
        for (AngleUnit u : AngleUnit.units()) {
            if (!lower.endsWith(u.suffix())) continue;
            unit = u;
            break;
        }
        String str = lower.substring(0, lower.length() - unit.suffix().length());
        try {
            return new Angle(unit, Float.parseFloat(str));
        }
        catch (NumberFormatException e) {
            return fallback;
        }
    }

    @Contract(value="_,!null,_ -> !null")
    public @NotNull Length @Nullable [] parseLengthList(@Nullable String value, @NotNull Length @Nullable [] fallback, @NotNull PercentageDimension dimension) {
        if (value != null && value.equalsIgnoreCase("none")) {
            return new Length[0];
        }
        String[] values = this.parseStringList(value, NumberListSplitter.INSTANCE, null);
        if (values == null) {
            return fallback;
        }
        Length[] ret = new Length[values.length];
        for (int i = 0; i < ret.length; ++i) {
            Length length = this.parseLength(values[i], null, dimension);
            if (length == null) {
                return fallback;
            }
            ret[i] = length;
        }
        return ret;
    }

    public float @NotNull [] parseFloatList(@Nullable String value) {
        return ParserUtil.parseFloatList(value);
    }

    public double @NotNull [] parseDoubleList(@Nullable String value) {
        return ParserUtil.parseDoubleList(value);
    }

    @NotNull
    public @NotNull String @NotNull [] parseStringList(@Nullable String value, @NotNull ListSplitter listSplitter) {
        return ParserUtil.parseStringList(value, listSplitter);
    }

    @Contract(value="_,_,!null -> !null")
    public @NotNull String @Nullable [] parseStringList(@Nullable String value, @NotNull ListSplitter listSplitter, @NotNull String @Nullable [] fallback) {
        return ParserUtil.parseStringList(value, listSplitter, fallback);
    }

    @Nullable
    public SVGPaint parsePaint(@Nullable String value, @NotNull AttributeNode attributeNode) {
        return this.paintParser.parsePaint(value);
    }

    @NotNull
    public <E extends Enum<E>> E parseEnum(@Nullable String value, @NotNull E fallback) {
        E e = this.parseEnum(value, fallback.getDeclaringClass());
        if (e == null) {
            return fallback;
        }
        return e;
    }

    @Nullable
    public <E extends Enum<E>> E parseEnum(@Nullable String value, @NotNull Class<E> enumType) {
        if (value == null) {
            return null;
        }
        for (Enum enumConstant : (Enum[])enumType.getEnumConstants()) {
            String name;
            String string = name = enumConstant instanceof HasMatchName ? ((HasMatchName)((Object)enumConstant)).matchName() : enumConstant.name();
            if (!name.equalsIgnoreCase(value)) continue;
            return (E)enumConstant;
        }
        return null;
    }

    private static void warnIllegalTransform(@NotNull String value, @NotNull String input) {
        LOGGER.log(Logger.Level.WARNING, () -> String.format("Illegal transform definition '%s' encountered error while parsing '%s'", value, input));
    }

    @Nullable
    private static RawTransformFunction parseNextTransformFunction(@NotNull String value, int start) {
        int i;
        int len = value.length();
        for (i = start; i < len && (Character.isWhitespace(value.charAt(i)) || value.charAt(i) == ','); ++i) {
        }
        if (i >= len) {
            return null;
        }
        int nameStart = i;
        while (i < len && (Character.isLetterOrDigit(value.charAt(i)) || value.charAt(i) == '-' || value.charAt(i) == '_')) {
            ++i;
        }
        if (i >= len || value.charAt(i) != '(') {
            return null;
        }
        String name = value.substring(nameStart, i);
        int argStart = ++i;
        while (i < len && value.charAt(i) != ')') {
            ++i;
        }
        if (i >= len) {
            return null;
        }
        String args = value.substring(argStart, i);
        return new RawTransformFunction(++i, name, args);
    }

    public @Nullable List<@NotNull TransformPart> parseTransform(@Nullable String value) {
        if (value == null) {
            return null;
        }
        if ("none".equals(value)) {
            return null;
        }
        ArrayList<TransformPart> parts = new ArrayList<TransformPart>();
        int i = 0;
        while (i < value.length()) {
            int skipped;
            for (skipped = i; skipped < value.length() && (Character.isWhitespace(value.charAt(skipped)) || value.charAt(skipped) == ','); ++skipped) {
            }
            if (skipped >= value.length()) break;
            RawTransformFunction parsed = AttributeParser.parseNextTransformFunction(value, i);
            if (parsed == null) {
                AttributeParser.warnIllegalTransform(value, value.substring(i));
                return null;
            }
            TransformPart part = this.parseSingleTransformPart(parsed);
            if (part == null) {
                AttributeParser.warnIllegalTransform(value, parsed.args);
                return null;
            }
            parts.add(part);
            i = parsed.endIndex;
        }
        return parts;
    }

    @Nullable
    private TransformPart parseSingleTransformPart(@NotNull RawTransformFunction transformFunction) {
        String command = transformFunction.name.toLowerCase(Locale.ENGLISH);
        TransformPart.TransformType type = this.parseEnum(command, TransformPart.TransformType.class);
        if (type == null) {
            return null;
        }
        return this.parseTransformPart(type, transformFunction.args);
    }

    @Nullable
    public TransformPart parseTransformPart(TransformPart.TransformType type, @NotNull String value) {
        String[] values = this.parseStringList(value, NumberListSplitter.INSTANCE);
        Length[] lengths = this.parseTransformLengths(type, values);
        if (lengths == null) {
            return null;
        }
        return new TransformPart(type, lengths);
    }

    private Length @Nullable [] parseTransformLengths(@NotNull TransformPart.TransformType type, @NotNull @NotNull String @NotNull [] values) {
        Length[] lengths;
        switch (type) {
            case MATRIX: {
                if (values.length == 4) {
                    lengths = AttributeUtil.toNonnullArray(this.parseNumber(values[0], null), this.parseNumber(values[1], null), this.parseNumber(values[2], null), this.parseNumber(values[3], null), Length.ZERO, Length.ZERO);
                    break;
                }
                if (values.length == 6) {
                    lengths = AttributeUtil.toNonnullArray(this.parseNumber(values[0], null), this.parseNumber(values[1], null), this.parseNumber(values[2], null), this.parseNumber(values[3], null), this.parseNumber(values[4], null), this.parseNumber(values[5], null));
                    break;
                }
                lengths = null;
                break;
            }
            case TRANSLATE: {
                if (values.length == 1) {
                    lengths = AttributeUtil.toNonnullArray(this.parseLength(values[0], null, PercentageDimension.WIDTH), Length.ZERO);
                    break;
                }
                lengths = AttributeUtil.toNonnullArray(this.parseLength(values[0], null, PercentageDimension.WIDTH), this.parseLength(values[1], null, PercentageDimension.HEIGHT));
                break;
            }
            case TRANSLATE_X: {
                lengths = AttributeUtil.toNonnullArray(this.parseLength(values[0], null, PercentageDimension.WIDTH));
                break;
            }
            case TRANSLATE_Y: {
                lengths = AttributeUtil.toNonnullArray(this.parseLength(values[0], null, PercentageDimension.HEIGHT));
                break;
            }
            case ROTATE: {
                if (values.length > 2) {
                    lengths = AttributeUtil.toNonnullArray(this.parseNumber(values[0], null), this.parseLength(values[1], null, PercentageDimension.WIDTH), this.parseLength(values[2], null, PercentageDimension.HEIGHT));
                    break;
                }
                lengths = AttributeUtil.toNonnullArray(this.parseNumber(values[0], null));
                break;
            }
            case SCALE: 
            case SKEW: {
                if (values.length == 1) {
                    lengths = AttributeUtil.toNonnullArray(this.parseNumber(values[0], null));
                    break;
                }
                lengths = AttributeUtil.toNonnullArray(this.parseNumber(values[0], null), this.parseNumber(values[1], null));
                break;
            }
            case SCALE_X: 
            case SCALE_Y: 
            case SKEW_X: 
            case SKEW_Y: {
                lengths = AttributeUtil.toNonnullArray(this.parseLength(values[0], null, PercentageDimension.NONE));
                break;
            }
            default: {
                lengths = null;
            }
        }
        return lengths;
    }

    @NotNull
    public PaintParser paintParser() {
        return this.paintParser;
    }

    private static final class RawTransformFunction {
        final int endIndex;
        @NotNull
        final String name;
        @NotNull
        final String args;

        RawTransformFunction(int endIndex, @NotNull String name, @NotNull String args) {
            this.endIndex = endIndex;
            this.name = name;
            this.args = args;
        }
    }
}

