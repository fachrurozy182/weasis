/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.attributes.font;

import com.github.weisj.jsvg.attributes.font.AttributeFontSpec;
import com.github.weisj.jsvg.attributes.font.FontSize;
import com.github.weisj.jsvg.attributes.font.FontStretch;
import com.github.weisj.jsvg.attributes.font.FontStyle;
import com.github.weisj.jsvg.attributes.font.FontWeight;
import com.github.weisj.jsvg.attributes.font.LengthFontSize;
import com.github.weisj.jsvg.attributes.font.NumberFontWeight;
import com.github.weisj.jsvg.attributes.font.PredefinedFontSize;
import com.github.weisj.jsvg.attributes.font.PredefinedFontWeight;
import com.github.weisj.jsvg.attributes.value.PercentageDimension;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.geometry.size.Percentage;
import com.github.weisj.jsvg.parser.impl.AttributeNode;
import com.github.weisj.jsvg.parser.impl.SeparatorMode;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FontParser {
    private FontParser() {
    }

    @NotNull
    public static AttributeFontSpec parseFontSpec(@NotNull AttributeNode node) {
        String[] fontFamilies = node.getStringList("font-family", SeparatorMode.COMMA_ONLY);
        FontParser.canonicalizeFontFamily(fontFamilies);
        @Nullable FontWeight weight = FontParser.parseWeight(node);
        @Nullable FontSize size = FontParser.parseFontSize(node);
        @Nullable Length sizeAdjust = FontParser.parseSizeAdjust(node);
        @Nullable FontStyle style = FontParser.parseFontStyle(node);
        @NotNull Percentage stretch = FontParser.parseStretch(node);
        return new AttributeFontSpec(fontFamilies, style, sizeAdjust, stretch, size, weight);
    }

    @NotNull
    private static String stripQuotes(@NotNull String str, char quoteChar) {
        String quoteStr = String.valueOf(quoteChar);
        if (str.length() > 2 && str.startsWith(quoteStr) && str.endsWith(quoteStr)) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }

    private static void canonicalizeFontFamily(@NotNull String[] fontFamilies) {
        for (int i = 0; i < fontFamilies.length; ++i) {
            String family;
            switch (family = fontFamilies[i].toLowerCase(Locale.US)) {
                case "sans-serif": {
                    family = "SansSerif";
                    break;
                }
                case "serif": {
                    family = "Serif";
                    break;
                }
                case "monospace": {
                    family = "Monospaced";
                    break;
                }
                default: {
                    family = FontParser.stripQuotes(family, '\'');
                    family = FontParser.stripQuotes(family, '\"');
                }
            }
            fontFamilies[i] = family;
        }
    }

    @Nullable
    public static FontWeight parseWeight(@NotNull AttributeNode node) {
        String fontWeightKey = "font-weight";
        FontWeight weight = node.getEnum("font-weight", PredefinedFontWeight.Number);
        if (weight == PredefinedFontWeight.Number) {
            weight = node.hasAttribute("font-weight") ? new NumberFontWeight(Math.max(1.0f, Math.min(1000.0f, node.getFloat("font-weight", 400.0f)))) : null;
        }
        return weight;
    }

    @NotNull
    public static Percentage parseStretch(@NotNull AttributeNode node) {
        FontStretch stretch = node.getEnum("font-stretch", FontStretch.Percentage);
        return stretch == FontStretch.Percentage ? node.parser().parsePercentage(node.getValue("font-stretch"), Percentage.UNSPECIFIED, 0.5f, 2.0f) : stretch.percentage();
    }

    @Nullable
    public static FontSize parseFontSize(@NotNull AttributeNode node) {
        FontSize fontSize = node.getEnum("font-size", PredefinedFontSize.Number);
        if (fontSize == PredefinedFontSize.Number) {
            Length size = node.getLength("font-size", PercentageDimension.CUSTOM, Length.UNSPECIFIED);
            fontSize = size.isSpecified() ? new LengthFontSize(size) : null;
        }
        return fontSize;
    }

    @Nullable
    public static Length parseSizeAdjust(@NotNull AttributeNode node) {
        return node.getLength("font-size-adjust", PercentageDimension.NONE);
    }

    @Nullable
    static FontStyle parseFontStyle(@NotNull AttributeNode node) {
        FontStyle style = null;
        String styleStr = node.getValue("font-style");
        if ("normal".equalsIgnoreCase(styleStr)) {
            style = FontStyle.normal();
        } else if ("italic".equalsIgnoreCase(styleStr)) {
            style = FontStyle.italic();
        } else if (styleStr != null && styleStr.startsWith("oblique")) {
            String[] comps = styleStr.split(" ", 2);
            style = comps.length == 2 ? new FontStyle.Oblique(node.parser().parseAngle(comps[1], FontStyle.Oblique.DEFAULT_ANGLE)) : FontStyle.oblique();
        }
        return style;
    }
}

