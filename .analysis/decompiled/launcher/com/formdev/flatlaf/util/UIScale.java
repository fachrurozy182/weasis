/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.util;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatSystemProperties;
import com.formdev.flatlaf.util.SystemInfo;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Method;
import java.util.Arrays;
import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.UIResource;

public class UIScale {
    public static final String PROP_USER_SCALE_FACTOR = "userScaleFactor";
    public static final String PROP_ZOOM_FACTOR = "zoomFactor";
    private static final boolean DEBUG = false;
    private static PropertyChangeSupport changeSupport;
    private static Boolean jreHiDPI;
    private static float unzoomedScaleFactor;
    private static float scaleFactor;
    private static boolean initialized;
    private static boolean listenerInitialized;
    private static boolean ignoreFontChange;
    private static float zoomFactor;
    private static float[] supportedZoomFactors;
    static boolean inUnitTests;

    public static void addPropertyChangeListener(PropertyChangeListener listener) {
        if (changeSupport == null) {
            changeSupport = new PropertyChangeSupport(UIScale.class);
        }
        changeSupport.addPropertyChangeListener(listener);
    }

    public static void removePropertyChangeListener(PropertyChangeListener listener) {
        if (changeSupport == null) {
            return;
        }
        changeSupport.removePropertyChangeListener(listener);
    }

    public static boolean isSystemScalingEnabled() {
        if (jreHiDPI != null) {
            return jreHiDPI;
        }
        jreHiDPI = false;
        if (SystemInfo.isJava_9_orLater) {
            jreHiDPI = true;
        } else if (SystemInfo.isJetBrainsJVM) {
            try {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                Class<?> sunGeClass = Class.forName("sun.java2d.SunGraphicsEnvironment");
                if (sunGeClass.isInstance(ge)) {
                    Method m = sunGeClass.getDeclaredMethod("isUIScaleOn", new Class[0]);
                    jreHiDPI = (Boolean)m.invoke(ge, new Object[0]);
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return jreHiDPI;
    }

    public static double getSystemScaleFactor(Graphics2D g) {
        return UIScale.isSystemScalingEnabled() ? UIScale.getSystemScaleFactor(g.getDeviceConfiguration()) : 1.0;
    }

    public static double getSystemScaleFactor(GraphicsConfiguration gc) {
        return UIScale.isSystemScalingEnabled() && gc != null ? gc.getDefaultTransform().getScaleX() : 1.0;
    }

    private static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        if (!UIScale.isUserScalingEnabled()) {
            return;
        }
        UIScale.initializeListener();
        UIScale.updateScaleFactor(true);
    }

    private static void initializeListener() {
        if (listenerInitialized) {
            return;
        }
        listenerInitialized = true;
        PropertyChangeListener listener = new PropertyChangeListener(){

            @Override
            public void propertyChange(PropertyChangeEvent e) {
                switch (e.getPropertyName()) {
                    case "lookAndFeel": {
                        if (e.getNewValue() instanceof LookAndFeel) {
                            UIManager.getLookAndFeelDefaults().addPropertyChangeListener(this);
                        }
                        UIScale.updateScaleFactor(true);
                        break;
                    }
                    case "defaultFont": 
                    case "Label.font": {
                        if (ignoreFontChange) break;
                        UIScale.updateScaleFactor(false);
                    }
                }
            }
        };
        UIManager.getDefaults().addPropertyChangeListener(listener);
        UIManager.getLookAndFeelDefaults().addPropertyChangeListener(listener);
        UIManager.addPropertyChangeListener(listener);
    }

    private static void updateScaleFactor(boolean lafChanged) {
        if (!UIScale.isUserScalingEnabled()) {
            return;
        }
        float customScaleFactor = UIScale.getCustomScaleFactor();
        if (customScaleFactor > 0.0f) {
            UIScale.setUserScaleFactor(customScaleFactor, false);
            return;
        }
        Font font = null;
        if (UIManager.getLookAndFeel() instanceof FlatLaf) {
            font = UIManager.getFont("defaultFont");
        }
        if (font == null) {
            font = UIManager.getFont("Label.font");
        }
        float fontScaleFactor = UIScale.computeFontScaleFactor(font);
        if (lafChanged && UIManager.getLookAndFeel() instanceof FlatLaf) {
            fontScaleFactor /= zoomFactor;
        }
        UIScale.setUserScaleFactor(fontScaleFactor, true);
    }

    public static float computeFontScaleFactor(Font font) {
        Font uiFont;
        if (SystemInfo.isWindows && !inUnitTests && font instanceof UIResource && ((uiFont = (Font)Toolkit.getDefaultToolkit().getDesktopProperty("win.messagebox.font")) == null || uiFont.getSize() == font.getSize())) {
            if (UIScale.isSystemScalingEnabled()) {
                return 1.0f;
            }
            Font winFont = (Font)Toolkit.getDefaultToolkit().getDesktopProperty("win.defaultGUI.font");
            return UIScale.computeScaleFactor(winFont != null ? winFont : font);
        }
        return UIScale.computeScaleFactor(font);
    }

    private static float computeScaleFactor(Font font) {
        String customFontSizeDivider = System.getProperty("flatlaf.uiScale.fontSizeDivider");
        if (customFontSizeDivider != null) {
            try {
                float fontSizeDivider = Math.max(Integer.parseInt(customFontSizeDivider), 10);
                return (float)font.getSize() / fontSizeDivider;
            }
            catch (NumberFormatException fontSizeDivider) {
                // empty catch block
            }
        }
        float fontSizeDivider = 12.0f;
        if (SystemInfo.isWindows) {
            if ("Tahoma".equals(font.getFamily())) {
                fontSizeDivider = 11.0f;
            }
        } else if (SystemInfo.isMacOS) {
            fontSizeDivider = 13.0f;
        } else if (SystemInfo.isLinux) {
            fontSizeDivider = SystemInfo.isKDE ? 13.0f : 15.0f;
        }
        return (float)font.getSize() / fontSizeDivider;
    }

    private static boolean isUserScalingEnabled() {
        return FlatSystemProperties.getBoolean("flatlaf.uiScale.enabled", true);
    }

    public static FontUIResource applyCustomScaleFactor(FontUIResource font) {
        if (!UIScale.isUserScalingEnabled()) {
            return font;
        }
        float scaleFactor = UIScale.getCustomScaleFactor();
        if (scaleFactor <= 0.0f) {
            return font;
        }
        float fontScaleFactor = UIScale.computeScaleFactor(font);
        if (scaleFactor == fontScaleFactor) {
            return font;
        }
        int newFontSize = Math.max(Math.round((float)font.getSize() / fontScaleFactor * scaleFactor), 1);
        return new FontUIResource(font.deriveFont((float)newFontSize));
    }

    private static float getCustomScaleFactor() {
        return UIScale.parseScaleFactor(System.getProperty("flatlaf.uiScale"));
    }

    private static float parseScaleFactor(String s) {
        if (s == null) {
            return -1.0f;
        }
        float units = 1.0f;
        if (s.endsWith("x")) {
            s = s.substring(0, s.length() - 1);
        } else if (s.endsWith("dpi")) {
            units = 96.0f;
            s = s.substring(0, s.length() - 3);
        } else if (s.endsWith("%")) {
            units = 100.0f;
            s = s.substring(0, s.length() - 1);
        }
        try {
            float scale = Float.parseFloat(s);
            return scale > 0.0f ? scale / units : -1.0f;
        }
        catch (NumberFormatException ex) {
            return -1.0f;
        }
    }

    public static float getUserScaleFactor() {
        UIScale.initialize();
        return scaleFactor;
    }

    private static void setUserScaleFactor(float unzoomedScaleFactor, boolean normalize) {
        if (normalize) {
            unzoomedScaleFactor = UIScale.normalizeScaleFactor(unzoomedScaleFactor);
        }
        if ((unzoomedScaleFactor = Math.max(unzoomedScaleFactor, 0.1f)) == UIScale.unzoomedScaleFactor) {
            return;
        }
        UIScale.unzoomedScaleFactor = unzoomedScaleFactor;
        UIScale.setScaleFactor(unzoomedScaleFactor * zoomFactor);
    }

    private static void setScaleFactor(float scaleFactor) {
        if ((scaleFactor = (float)Math.round(scaleFactor * 100.0f) / 100.0f) == UIScale.scaleFactor) {
            return;
        }
        float oldScaleFactor = UIScale.scaleFactor;
        UIScale.scaleFactor = scaleFactor;
        if (changeSupport != null) {
            changeSupport.firePropertyChange(PROP_USER_SCALE_FACTOR, Float.valueOf(oldScaleFactor), Float.valueOf(scaleFactor));
        }
    }

    private static float normalizeScaleFactor(float scaleFactor) {
        if (scaleFactor < 1.0f) {
            return FlatSystemProperties.getBoolean("flatlaf.uiScale.allowScaleDown", false) ? (float)Math.round(scaleFactor * 10.0f) / 10.0f : 1.0f;
        }
        if (scaleFactor > 1.0f) {
            return (float)Math.round(scaleFactor * 4.0f) / 4.0f;
        }
        return scaleFactor;
    }

    public static float scale(float value) {
        UIScale.initialize();
        return scaleFactor == 1.0f ? value : value * scaleFactor;
    }

    public static int scale(int value) {
        UIScale.initialize();
        return scaleFactor == 1.0f ? value : Math.round((float)value * scaleFactor);
    }

    public static int scale2(int value) {
        UIScale.initialize();
        return scaleFactor == 1.0f ? value : (int)((float)value * scaleFactor);
    }

    public static float unscale(float value) {
        UIScale.initialize();
        return scaleFactor == 1.0f ? value : value / scaleFactor;
    }

    public static int unscale(int value) {
        UIScale.initialize();
        return scaleFactor == 1.0f ? value : Math.round((float)value / scaleFactor);
    }

    public static void scaleGraphics(Graphics2D g) {
        UIScale.initialize();
        if (scaleFactor != 1.0f) {
            g.scale(scaleFactor, scaleFactor);
        }
    }

    public static Dimension scale(Dimension dimension) {
        UIScale.initialize();
        return dimension == null || scaleFactor == 1.0f ? dimension : (dimension instanceof UIResource ? new DimensionUIResource(UIScale.scale(dimension.width), UIScale.scale(dimension.height)) : new Dimension(UIScale.scale(dimension.width), UIScale.scale(dimension.height)));
    }

    public static Insets scale(Insets insets) {
        UIScale.initialize();
        return insets == null || scaleFactor == 1.0f ? insets : (insets instanceof UIResource ? new InsetsUIResource(UIScale.scale(insets.top), UIScale.scale(insets.left), UIScale.scale(insets.bottom), UIScale.scale(insets.right)) : new Insets(UIScale.scale(insets.top), UIScale.scale(insets.left), UIScale.scale(insets.bottom), UIScale.scale(insets.right)));
    }

    public static float getZoomFactor() {
        return zoomFactor;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean setZoomFactor(float zoomFactor) {
        if (UIScale.zoomFactor == (zoomFactor = Math.max(zoomFactor, 0.1f))) {
            return false;
        }
        float oldZoomFactor = UIScale.zoomFactor;
        UIScale.zoomFactor = zoomFactor;
        UIScale.setScaleFactor(unzoomedScaleFactor * zoomFactor);
        if (initialized && UIManager.getLookAndFeel() instanceof FlatLaf) {
            UIDefaults defaults = UIManager.getLookAndFeelDefaults();
            Font font = defaults.getFont("defaultFont");
            int unzoomedSize = defaults.getInt("defaultFont.unzoomedSize");
            if (unzoomedSize == 0) {
                unzoomedSize = font.getSize();
                defaults.put("defaultFont.unzoomedSize", (Object)unzoomedSize);
            }
            ignoreFontChange = true;
            try {
                Font appFont = UIManager.getFont("defaultFont");
                int newFontSize = Math.max(Math.round((float)unzoomedSize * zoomFactor), 1);
                defaults.put("defaultFont", new FontUIResource(font.deriveFont((float)newFontSize)));
                if (appFont != font) {
                    int newAppFontSize = Math.max(Math.round((float)appFont.getSize() / oldZoomFactor * zoomFactor), 1);
                    UIManager.put("defaultFont", appFont.deriveFont((float)newAppFontSize));
                }
            }
            finally {
                ignoreFontChange = false;
            }
        }
        if (changeSupport != null) {
            changeSupport.firePropertyChange(PROP_ZOOM_FACTOR, Float.valueOf(oldZoomFactor), Float.valueOf(zoomFactor));
        }
        return true;
    }

    public static boolean zoomIn() {
        int next;
        int i = Arrays.binarySearch(supportedZoomFactors, zoomFactor);
        int n = next = i >= 0 ? i + 1 : -i - 1;
        if (next >= supportedZoomFactors.length) {
            return false;
        }
        return UIScale.setZoomFactor(supportedZoomFactors[next]);
    }

    public static boolean zoomOut() {
        int prev;
        int i = Arrays.binarySearch(supportedZoomFactors, zoomFactor);
        int n = prev = i >= 0 ? i - 1 : -i - 2;
        if (prev < 0) {
            return false;
        }
        return UIScale.setZoomFactor(supportedZoomFactors[prev]);
    }

    public static boolean zoomReset() {
        return UIScale.setZoomFactor(1.0f);
    }

    public static float[] getSupportedZoomFactors() {
        return (float[])supportedZoomFactors.clone();
    }

    public static void setSupportedZoomFactors(float[] supportedZoomFactors) {
        UIScale.supportedZoomFactors = (float[])supportedZoomFactors.clone();
        Arrays.sort(UIScale.supportedZoomFactors);
        if (Arrays.binarySearch(UIScale.supportedZoomFactors, 1.0f) < 0) {
            throw new IllegalArgumentException("supportedZoomFactors array must contain value 1f");
        }
    }

    static void tests_uninitialize() {
        initialized = false;
        unzoomedScaleFactor = 1.0f;
        scaleFactor = 1.0f;
        zoomFactor = 1.0f;
    }

    static {
        unzoomedScaleFactor = 1.0f;
        scaleFactor = 1.0f;
        zoomFactor = 1.0f;
        supportedZoomFactors = new float[]{1.0f, 1.1f, 1.25f, 1.5f, 1.75f, 2.0f};
    }
}

