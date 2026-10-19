/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.osgi.framework.BundleContext
 *  org.osgi.service.prefs.Preferences
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.api.service;

import java.awt.Color;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.util.GzipManager;
import org.weasis.core.util.StringUtil;

public class WProperties
extends Properties {
    private static final Logger LOGGER = LoggerFactory.getLogger(WProperties.class);
    private final transient BundleContext context = AppProperties.getBundleContext();

    @Override
    public synchronized Object setProperty(String key, String value) {
        if (WProperties.isValid(key, value) && value.length() < 65536) {
            return super.setProperty(key, value);
        }
        return null;
    }

    @Override
    public String getProperty(String key) {
        String value = super.getProperty(key);
        if (value == null && (value = System.getProperty(key, null)) == null) {
            value = this.context == null ? null : this.context.getProperty(key);
        }
        return value;
    }

    public void resetProperty(String key, String defaultValue) {
        if (WProperties.isKeyValid(key)) {
            String value = System.getProperty(key, null);
            if (value == null) {
                String string = value = this.context == null ? null : this.context.getProperty(key);
                if (value == null) {
                    value = defaultValue;
                }
            }
            if (WProperties.isValid(key, value) && value.length() < 65536) {
                this.put(key, value);
            }
        }
    }

    public void resetServiceProperty(String key, String defaultValue) {
        if (WProperties.isKeyValid(key)) {
            String value = System.getProperty(key, null);
            if (value == null) {
                String string = value = this.context == null ? null : this.context.getProperty("def." + key);
                if (value == null) {
                    value = defaultValue;
                }
            }
            if (WProperties.isValid(key, value) && value.length() < 65536) {
                this.put(key, value);
            }
        }
    }

    public void putIntProperty(String key, int value) {
        if (WProperties.isKeyValid(key)) {
            this.put(key, String.valueOf(value));
        }
    }

    public int getIntProperty(String key, int def) {
        String value;
        int result = def;
        if (WProperties.isKeyValid(key) && (value = this.getProperty(key)) != null) {
            try {
                result = Integer.parseInt(value);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return result;
    }

    public void putLongProperty(String key, long value) {
        if (WProperties.isKeyValid(key)) {
            this.put(key, String.valueOf(value));
        }
    }

    public long getLongProperty(String key, long def) {
        String value;
        long result = def;
        if (WProperties.isKeyValid(key) && (value = this.getProperty(key)) != null) {
            try {
                result = Long.parseLong(value);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return result;
    }

    public void putBooleanProperty(String key, boolean value) {
        if (WProperties.isKeyValid(key)) {
            this.put(key, String.valueOf(value));
        }
    }

    public boolean getBooleanProperty(String key, boolean def) {
        String value;
        boolean result = def;
        if (WProperties.isKeyValid(key) && (value = this.getProperty(key)) != null) {
            if (Boolean.TRUE.toString().equalsIgnoreCase(value)) {
                result = true;
            } else if (Boolean.FALSE.toString().equalsIgnoreCase(value)) {
                result = false;
            }
        }
        return result;
    }

    public void putFloatProperty(String key, float value) {
        if (WProperties.isKeyValid(key)) {
            this.put(key, String.valueOf(value));
        }
    }

    public float getFloatProperty(String key, float def) {
        String value;
        float result = def;
        if (WProperties.isKeyValid(key) && (value = this.getProperty(key)) != null) {
            try {
                result = Float.parseFloat(value);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return result;
    }

    public void putDoubleProperty(String key, double value) {
        if (WProperties.isKeyValid(key)) {
            this.put(key, String.valueOf(value));
        }
    }

    public double getDoubleProperty(String key, double def) {
        String value;
        double result = def;
        if (WProperties.isKeyValid(key) && (value = this.getProperty(key)) != null) {
            try {
                result = Double.parseDouble(value);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return result;
    }

    public Color getColorProperty(String key) {
        return WProperties.hexadecimal2Color(this.getProperty(key));
    }

    public Color getColorProperty(String key, Color defaultColor) {
        String val = this.getProperty(key);
        if (!StringUtil.hasText((CharSequence)val)) {
            return defaultColor;
        }
        return WProperties.hexadecimal2Color(val);
    }

    public void putColorProperty(String key, Color color) {
        if (WProperties.isValid(key, color)) {
            this.put(key, WProperties.color2Hexadecimal(color, true));
        }
    }

    public void putByteArrayProperty(String key, byte[] value) {
        if (WProperties.isKeyValid(key)) {
            try {
                String val = "";
                if (value != null && value.length > 0) {
                    val = new String(Base64.getEncoder().encode(GzipManager.gzipCompressToByte(value)), StandardCharsets.UTF_8);
                }
                this.put(key, val);
            }
            catch (IOException e) {
                LOGGER.error("Set byte property", (Throwable)e);
            }
        }
    }

    public byte[] getByteArrayProperty(String key, byte[] def) {
        String value;
        byte[] result = def;
        if (WProperties.isKeyValid(key) && StringUtil.hasText((CharSequence)(value = this.getProperty(key)))) {
            try {
                byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
                result = GzipManager.gzipUncompressToByte(Base64.getDecoder().decode(bytes));
            }
            catch (IOException e) {
                LOGGER.error("Get byte property", (Throwable)e);
            }
        }
        return result;
    }

    private static boolean isValid(String key, Object value) {
        return key != null && value != null;
    }

    private static boolean isKeyValid(String key) {
        return key != null;
    }

    public static String color2Hexadecimal(Color c, boolean alpha) {
        int val = c == null ? 0 : (alpha ? c.getRGB() : c.getRGB() & 0xFFFFFF);
        return Integer.toHexString(val);
    }

    public static Color hexadecimal2Color(String hexColor) {
        int intValue = -16777216;
        try {
            if (hexColor != null && hexColor.length() > 6) {
                intValue = (int)Long.parseLong(hexColor, 16);
            } else if (hexColor != null) {
                intValue |= Integer.parseInt(hexColor, 16);
            }
        }
        catch (NumberFormatException e) {
            LOGGER.error("Cannot parse color {} into int", (Object)hexColor);
        }
        return new Color(intValue, true);
    }

    public static void setProperty(WProperties properties, String key, Preferences prefNode, String defaultValue) {
        if (properties != null && key != null) {
            String val = prefNode.get(key, null);
            if (val == null) {
                val = properties.getProperty(key, defaultValue);
            }
            properties.setProperty(key, val);
        }
    }
}

