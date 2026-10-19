/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.util;

import java.awt.Font;
import javax.swing.UIManager;

public enum FontItem {
    H1_SEMIBOLD("h1.font", "H1 semibold"),
    H1("h1.regular.font", "H1"),
    H2_SEMIBOLD("h2.font", "H2 semibold"),
    H2("h2.regular.font", "H2"),
    H3_SEMIBOLD("h3.font", "H3 semibold"),
    H3("h3.regular.font", "H3"),
    LARGE("large.font", "Large"),
    DEFAULT_BOLD("h4.font", "H4 (default bold)"),
    DEFAULT_SEMIBOLD("semibold.font", "Default semibold"),
    DEFAULT("defaultFont", "Default font"),
    DEFAULT_LIGHT("light.font", "Default light font"),
    MONOSPACED("monospaced.font", "Default monospaced font"),
    MEDIUM_SEMIBOLD("medium.semibold.font", "Medium semibold"),
    MEDIUM("medium.font", "Medium"),
    SMALL_SEMIBOLD("small.semibold.font", "Small semibold"),
    SMALL("small.font", "Small"),
    MINI_SEMIBOLD("mini.semibold.font", "Mini semibold"),
    MINI("mini.font", "Mini"),
    MICRO_SEMIBOLD("micro.semibold.font", "Micro semibold"),
    MICRO("micro.font", "Micro");

    private static final Font FALLBACK_FONT;
    private final String key;
    private final String name;

    private FontItem(String key, String name) {
        this.key = key;
        this.name = name;
    }

    public String getKey() {
        return this.key;
    }

    public String getName() {
        return this.name;
    }

    public Font getFont() {
        Font font = UIManager.getFont(this.key);
        if (font == null) {
            font = UIManager.getFont("defaultFont");
        }
        return font == null ? FALLBACK_FONT : font;
    }

    public String toString() {
        return this.name;
    }

    public static FontItem getFontItem(String key) {
        return FontItem.getFontItem(key, DEFAULT);
    }

    public static FontItem getFontItem(String key, FontItem defaultItem) {
        for (FontItem item : FontItem.values()) {
            if (!item.key.equals(key)) continue;
            return item;
        }
        return defaultItem;
    }

    static {
        FALLBACK_FONT = new Font("SansSerif", 0, 12);
    }
}

