/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.osgi.framework.BundleContext
 *  org.osgi.framework.ServiceReference
 *  org.osgi.service.prefs.Preferences
 *  org.osgi.service.prefs.PreferencesService
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.MathUtil
 */
package org.weasis.core.api.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Arrays;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.util.MathUtil;

public class BundlePreferences {
    private static final Logger LOGGER = LoggerFactory.getLogger(BundlePreferences.class);

    private BundlePreferences() {
    }

    public static Path getDataFolder(BundleContext context) {
        if (context != null) {
            Path dataFolder = AppProperties.WEASIS_PATH.resolve("data").resolve(context.getBundle().getSymbolicName());
            try {
                Files.createDirectories(dataFolder, new FileAttribute[0]);
            }
            catch (IOException e) {
                LOGGER.warn("Failed to create data folder: {}", (Object)dataFolder, (Object)e);
            }
            return dataFolder;
        }
        return AppProperties.WEASIS_PATH.resolve("data");
    }

    public static Path getFileInDataFolder(BundleContext context, String filename) {
        Path dataFolder = BundlePreferences.getDataFolder(context);
        return dataFolder.resolve(filename);
    }

    public static Preferences getDefaultPreferences(BundleContext context) {
        return BundlePreferences.getUserPreferences(context, AppProperties.WEASIS_USER);
    }

    public static Preferences getUserPreferences(BundleContext context, String name) {
        if (context != null) {
            String user = name == null ? AppProperties.WEASIS_USER : name;
            PreferencesService prefService = BundlePreferences.getService(context, PreferencesService.class);
            if (prefService != null) {
                return prefService.getUserPreferences(user);
            }
        }
        return null;
    }

    public static <S> S getService(BundleContext context, Class<S> clazz) {
        if (clazz != null) {
            try {
                ServiceReference serviceRef = context.getServiceReference(clazz);
                if (serviceRef != null) {
                    return (S)context.getService(serviceRef);
                }
            }
            catch (Exception e) {
                LOGGER.error("Cannot get OSGI service from {}", clazz, (Object)e);
            }
        }
        return null;
    }

    public static void putStringPreferences(Preferences pref, String key, String value) {
        String val2;
        if (pref != null && key != null && value != null && !value.equals(val2 = pref.get(key, null))) {
            pref.put(key, value);
        }
    }

    public static void putBooleanPreferences(Preferences pref, String key, boolean value) {
        if (pref != null && key != null) {
            Boolean result = null;
            String s = pref.get(key, null);
            if (s != null) {
                result = Boolean.valueOf(s);
            }
            if (result == null || result != value) {
                pref.putBoolean(key, value);
            }
        }
    }

    public static void putByteArrayPreferences(Preferences pref, String key, byte[] value) {
        byte[] val2;
        if (!(pref == null || key == null || value == null || (val2 = pref.getByteArray(key, null)) != null && Arrays.equals(value, val2))) {
            pref.putByteArray(key, value);
        }
    }

    public static void putDoublePreferences(Preferences pref, String key, double value) {
        if (pref != null && key != null) {
            Double result = null;
            String s = pref.get(key, null);
            if (s != null) {
                try {
                    result = Double.parseDouble(s);
                }
                catch (NumberFormatException ignore) {
                    LOGGER.error("Cannot parse {} into double", (Object)s);
                }
            }
            if (result == null || !MathUtil.isEqual((double)result, (double)value)) {
                pref.putDouble(key, value);
            }
        }
    }

    public static void putFloatPreferences(Preferences pref, String key, float value) {
        if (pref != null && key != null) {
            Float result = null;
            String s = pref.get(key, null);
            if (s != null) {
                try {
                    result = Float.valueOf(Float.parseFloat(s));
                }
                catch (NumberFormatException ignore) {
                    LOGGER.error("Cannot parse {} into float", (Object)s);
                }
            }
            if (result == null || !MathUtil.isEqual((float)result.floatValue(), (float)value)) {
                pref.putFloat(key, value);
            }
        }
    }

    public static void putIntPreferences(Preferences pref, String key, int value) {
        if (pref != null && key != null) {
            Integer result = null;
            String s = pref.get(key, null);
            if (s != null) {
                try {
                    result = Integer.parseInt(s);
                }
                catch (NumberFormatException ignore) {
                    LOGGER.error("Cannot parse {} into int", (Object)s);
                }
            }
            if (result == null || result != value) {
                pref.putInt(key, value);
            }
        }
    }

    public static void putLongPreferences(Preferences pref, String key, long value) {
        if (pref != null && key != null) {
            Long result = null;
            String s = pref.get(key, null);
            if (s != null) {
                try {
                    result = Long.parseLong(s);
                }
                catch (NumberFormatException ignore) {
                    LOGGER.error("Cannot parse {} into long", (Object)s);
                }
            }
            if (result == null || result != value) {
                pref.putLong(key, value);
            }
        }
    }
}

