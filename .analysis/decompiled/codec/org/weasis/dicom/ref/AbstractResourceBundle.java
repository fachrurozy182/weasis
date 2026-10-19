/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.ref;

import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

abstract class AbstractResourceBundle {
    private static final String MISSING_KEY_FORMAT = "!%s!";
    private static final Map<String, ResourceBundle> bundleCache = new ConcurrentHashMap<String, ResourceBundle>();
    private final String bundleName;
    private final ResourceBundle defaultBundle;

    protected AbstractResourceBundle(String bundleName) {
        this.bundleName = bundleName;
        this.defaultBundle = ResourceBundle.getBundle(bundleName);
    }

    public String getString(String key) {
        try {
            return this.defaultBundle.getString(key);
        }
        catch (MissingResourceException e) {
            return MISSING_KEY_FORMAT.formatted(new Object[]{key});
        }
    }

    public String getString(String key, Locale locale) {
        if (locale == null) {
            return this.getString(key);
        }
        try {
            ResourceBundle bundle = this.getBundle(locale);
            return bundle.getString(key);
        }
        catch (MissingResourceException e) {
            return MISSING_KEY_FORMAT.formatted(new Object[]{key});
        }
    }

    private ResourceBundle getBundle(Locale locale) {
        String cacheKey = this.bundleName + "_" + locale.toString();
        return bundleCache.computeIfAbsent(cacheKey, k -> ResourceBundle.getBundle(this.bundleName, locale));
    }
}

