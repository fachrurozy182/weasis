/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.ref;

import java.util.Locale;
import org.weasis.dicom.ref.AbstractResourceBundle;

public final class MesCategory {
    private static final AbstractResourceBundle BUNDLE = new AbstractResourceBundle("org.weasis.dicom.ref.category"){};

    private MesCategory() {
    }

    public static String getString(String key) {
        return BUNDLE.getString(key);
    }

    public static String getString(String contextUID, Locale locale) {
        return BUNDLE.getString(contextUID, locale);
    }
}

