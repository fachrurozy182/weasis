/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.attributes.font;

import com.github.weisj.jsvg.attributes.font.AWTSVGFont;
import com.github.weisj.jsvg.attributes.font.FontStyle;
import com.github.weisj.jsvg.attributes.font.MeasurableFontSpec;
import com.github.weisj.jsvg.attributes.font.SVGFont;
import com.github.weisj.jsvg.renderer.MeasureContext;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class FontResolver {
    private FontResolver() {
    }

    public static void clearFontCache() {
        FontCache.INSTANCE.cache.clear();
    }

    @NotNull
    public static SVGFont resolve(@NotNull MeasurableFontSpec fontSpec, @NotNull MeasureContext measureContext, @NotNull String defaultFontFamily) {
        FontCache.CacheKey key = new FontCache.CacheKey(fontSpec, measureContext);
        SVGFont cachedFont = (SVGFont)FontCache.INSTANCE.cache.get(key);
        if (cachedFont != null) {
            return cachedFont;
        }
        SVGFont resolvedFont = FontResolver.resolveWithoutCache(fontSpec, measureContext, defaultFontFamily);
        FontCache.INSTANCE.cache.put(key, resolvedFont);
        return resolvedFont;
    }

    @NotNull
    public static SVGFont resolveWithoutCache(@NotNull MeasurableFontSpec fontSpec, @NotNull MeasureContext measureContext, @NotNull String defaultFontFamily) {
        String family = FontResolver.findSupportedFontFamily(fontSpec, defaultFontFamily);
        FontStyle style = fontSpec.style();
        float weight = FontResolver.cssWeightToAwtWeight(fontSpec.currentWeight());
        float size = fontSpec.effectiveSize(measureContext);
        float stretch = fontSpec.stretch().orElseIfUnspecified(1.0f).value();
        HashMap<TextAttribute, Object> attributes = new HashMap<TextAttribute, Object>(5, 1.0f);
        attributes.put(TextAttribute.FAMILY, family);
        attributes.put(TextAttribute.SIZE, Float.valueOf(size));
        attributes.put(TextAttribute.WEIGHT, Float.valueOf(weight));
        attributes.put(TextAttribute.WIDTH, Float.valueOf(stretch));
        if (style instanceof FontStyle.Normal) {
            attributes.put(TextAttribute.POSTURE, TextAttribute.POSTURE_REGULAR);
        } else if (style instanceof FontStyle.Italic) {
            attributes.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
        } else {
            AffineTransform transform = style.transform();
            if (transform != null) {
                attributes.put(TextAttribute.TRANSFORM, transform);
            }
        }
        Font font = new Font(attributes);
        return new AWTSVGFont(font);
    }

    private static float cssWeightToAwtWeight(float weight) {
        float currentWeight = weight;
        int normalWeight = 400;
        if (currentWeight > (float)normalWeight) {
            float awtWeightCompensationFactor = TextAttribute.WEIGHT_BOLD.floatValue() * (float)normalWeight / 700.0f;
            currentWeight *= awtWeightCompensationFactor;
        }
        return currentWeight / (float)normalWeight;
    }

    @NotNull
    private static String findSupportedFontFamily(@NotNull MeasurableFontSpec fontSpec, @NotNull String defaultFontFamily) {
        String[] families;
        for (String family : families = fontSpec.families()) {
            if (!FontFamiliesCache.INSTANCE.isSupportedFontFamily(family)) continue;
            return family;
        }
        return defaultFontFamily;
    }

    @NotNull
    public static @NotNull List<@NotNull String> supportedFonts() {
        return Collections.unmodifiableList(Arrays.asList(FontFamiliesCache.INSTANCE.supportedFonts));
    }

    private static enum FontCache {
        INSTANCE;

        private final HashMap<CacheKey, SVGFont> cache = new HashMap();

        private static final class CacheKey {
            @NotNull
            private final MeasurableFontSpec spec;
            @NotNull
            private final MeasureContext context;

            private CacheKey(@NotNull MeasurableFontSpec spec, @NotNull MeasureContext context) {
                this.spec = spec;
                this.context = context;
            }

            public String toString() {
                return "CacheKey{spec=" + this.spec + ", context=" + this.context + '}';
            }

            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (!(o instanceof CacheKey)) {
                    return false;
                }
                CacheKey cacheKey = (CacheKey)o;
                return this.spec.equals(cacheKey.spec) && this.context.equals(cacheKey.context);
            }

            public int hashCode() {
                return Objects.hash(this.spec, this.context);
            }
        }
    }

    private static enum FontFamiliesCache {
        INSTANCE;

        @NotNull
        private final String[] supportedFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

        boolean isSupportedFontFamily(@NotNull String fontName) {
            for (String supportedFont : this.supportedFonts) {
                if (!supportedFont.equalsIgnoreCase(fontName)) continue;
                return true;
            }
            return false;
        }
    }
}

