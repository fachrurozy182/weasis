/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.ref;

import java.util.Locale;
import org.weasis.dicom.ref.AbstractResourceBundle;

public final class MesSurface {
    private static final AbstractResourceBundle BUNDLE = new AbstractResourceBundle("org.weasis.dicom.ref.surface"){};

    private MesSurface() {
    }

    public static String getString(String key) {
        return BUNDLE.getString(key);
    }

    public static String getString(String key, Locale locale) {
        return BUNDLE.getString(key, locale);
    }
}

