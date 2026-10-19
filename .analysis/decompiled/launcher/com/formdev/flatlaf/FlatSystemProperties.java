/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf;

public interface FlatSystemProperties {
    public static final String UI_SCALE = "flatlaf.uiScale";
    public static final String UI_SCALE_ENABLED = "flatlaf.uiScale.enabled";
    public static final String UI_SCALE_ALLOW_SCALE_DOWN = "flatlaf.uiScale.allowScaleDown";
    public static final String USE_UBUNTU_FONT = "flatlaf.useUbuntuFont";
    public static final String USE_WINDOW_DECORATIONS = "flatlaf.useWindowDecorations";
    @Deprecated
    public static final String USE_JETBRAINS_CUSTOM_DECORATIONS = "flatlaf.useJetBrainsCustomDecorations";
    public static final String MENUBAR_EMBEDDED = "flatlaf.menuBarEmbedded";
    public static final String ANIMATION = "flatlaf.animation";
    public static final String USE_ROUNDED_POPUP_BORDER = "flatlaf.useRoundedPopupBorder";
    public static final String REUSE_VISIBLE_POPUP_WINDOW = "flatlaf.reuseVisiblePopupWindow";
    public static final String USE_TEXT_Y_CORRECTION = "flatlaf.useTextYCorrection";
    public static final String UPDATE_UI_ON_SYSTEM_FONT_CHANGE = "flatlaf.updateUIOnSystemFontChange";
    public static final String USE_NATIVE_LIBRARY = "flatlaf.useNativeLibrary";
    public static final String NATIVE_LIBRARY_PATH = "flatlaf.nativeLibraryPath";
    public static final String USE_SUB_MENU_SAFE_TRIANGLE = "flatlaf.useSubMenuSafeTriangle";
    public static final String USE_SYSTEM_FILE_CHOOSER = "flatlaf.useSystemFileChooser";

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = System.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    public static Boolean getBooleanStrict(String key, Boolean defaultValue) {
        String value = System.getProperty(key);
        if ("true".equalsIgnoreCase(value)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(value)) {
            return Boolean.FALSE;
        }
        return defaultValue;
    }
}

