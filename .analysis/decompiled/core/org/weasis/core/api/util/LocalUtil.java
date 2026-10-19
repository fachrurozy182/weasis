/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.api.util;

import java.util.Locale;
import org.weasis.core.util.StringUtil;

public class LocalUtil {
    private LocalUtil() {
    }

    public static String localeToText(Locale value) {
        if (value == null) {
            return "en";
        }
        return value.toLanguageTag();
    }

    public static Locale textToLocale(String value) {
        if (!StringUtil.hasText((CharSequence)value)) {
            return Locale.ENGLISH;
        }
        if (!"system".equals(value)) {
            return Locale.forLanguageTag(value.replace("_", "-"));
        }
        return Locale.getDefault();
    }
}

