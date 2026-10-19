/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.launcher;

import com.formdev.flatlaf.FlatIconColors;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.intellijthemes.FlatAllIJThemes;
import com.formdev.flatlaf.util.ColorFunctions;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.UIManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.launcher.Utils;

public final class LookAndFeels {
    private static final Logger LOGGER = LoggerFactory.getLogger(LookAndFeels.class);
    private final List<ReadableLookAndFeelInfo> lookAndFeels;
    private final ReadableLookAndFeelInfo defaultLightTheme;
    private final ReadableLookAndFeelInfo defaultDarkTheme;
    private static final String[] FLAT_NAMES = new String[]{"Core Dark - FlatDarcula", "Core Dark - FlatDark", "Core Light - FlatLight", "Core Dark - macOS", "Core Light - macOS"};
    private static final String[] FLAT_CLASSES = new String[]{"com.formdev.flatlaf.FlatDarculaLaf", "com.formdev.flatlaf.FlatDarkLaf", "com.formdev.flatlaf.FlatLightLaf", "com.formdev.flatlaf.themes.FlatMacDarkLaf", "com.formdev.flatlaf.themes.FlatMacLightLaf"};

    LookAndFeels() {
        FlatLaf.registerCustomDefaultsSource("org.weasis.theme", LookAndFeels.class.getClassLoader());
        this.defaultLightTheme = new ReadableLookAndFeelInfo("Core Light - FlatIntelliJ", "com.formdev.flatlaf.FlatIntelliJLaf", false);
        this.defaultDarkTheme = new ReadableLookAndFeelInfo("Core Dark - Flat Weasis", "org.weasis.launcher.FlatWeasisTheme", true);
        this.lookAndFeels = LookAndFeels.buildFlatLookAndFeels();
        this.lookAndFeels.add(this.defaultLightTheme);
        this.lookAndFeels.add(this.defaultDarkTheme);
    }

    private static List<ReadableLookAndFeelInfo> buildFlatLookAndFeels() {
        ArrayList<ReadableLookAndFeelInfo> infos = new ArrayList<ReadableLookAndFeelInfo>();
        Arrays.asList(FlatAllIJThemes.INFOS).forEach(i -> infos.add(new ReadableLookAndFeelInfo(i.isDark() ? "Dark - " + i.getName() : "Light - " + i.getName(), i.getClassName(), i.isDark())));
        for (int i2 = 0; i2 < FLAT_CLASSES.length; ++i2) {
            if (!LookAndFeels.isClassExist(FLAT_CLASSES[i2])) continue;
            infos.add(new ReadableLookAndFeelInfo(FLAT_NAMES[i2], FLAT_CLASSES[i2], FLAT_CLASSES[i2].contains("Dark")));
        }
        return infos;
    }

    private static boolean isClassExist(String clazz) {
        try {
            Class.forName(clazz);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public List<ReadableLookAndFeelInfo> getLookAndFeels() {
        return this.lookAndFeels;
    }

    public String setLookAndFeel(ReadableLookAndFeelInfo look) {
        ReadableLookAndFeelInfo info = look == null ? this.defaultDarkTheme : look;
        boolean dark = info.isDark();
        try {
            UIManager.setLookAndFeel(info.getClassName());
            LookAndFeels.increaseToolbarSeparatorContrast(dark);
            LookAndFeels.applyDefaultColor(FlatIconColors.ACTIONS_RED, dark);
            LookAndFeels.applyDefaultColor(FlatIconColors.ACTIONS_YELLOW, dark);
            LookAndFeels.applyDefaultColor(FlatIconColors.ACTIONS_GREEN, dark);
            LookAndFeels.applyDefaultColor(FlatIconColors.ACTIONS_BLUE, dark);
            LookAndFeels.applyDefaultColor(FlatIconColors.ACTIONS_GREY, dark);
            LookAndFeels.applyDefaultColor(FlatIconColors.ACTIONS_GREYINLINE, dark);
        }
        catch (Exception e) {
            LOGGER.error("Unable to set the Look&Feel", e);
        }
        return info.getClassName();
    }

    private static void increaseToolbarSeparatorContrast(boolean dark) {
        Color c = UIManager.getColor("ToolBar.separatorColor");
        c = dark ? ColorFunctions.lighten(c, 0.2f) : ColorFunctions.darken(c, 0.2f);
        UIManager.put("ToolBar.separatorColor", c);
    }

    private static void applyDefaultColor(FlatIconColors flatIconColor, boolean dark) {
        if (dark) {
            FlatIconColors darkColor = switch (flatIconColor) {
                case FlatIconColors.ACTIONS_RED -> FlatIconColors.ACTIONS_RED_DARK;
                case FlatIconColors.ACTIONS_YELLOW -> FlatIconColors.ACTIONS_YELLOW_DARK;
                case FlatIconColors.ACTIONS_GREEN -> FlatIconColors.ACTIONS_GREEN_DARK;
                case FlatIconColors.ACTIONS_BLUE -> FlatIconColors.ACTIONS_BLUE_DARK;
                case FlatIconColors.ACTIONS_GREY -> FlatIconColors.ACTIONS_GREY_DARK;
                case FlatIconColors.ACTIONS_GREYINLINE -> FlatIconColors.ACTIONS_GREYINLINE_DARK;
                default -> flatIconColor;
            };
            UIManager.put(darkColor.key, new Color(darkColor.rgb));
        } else {
            UIManager.put(flatIconColor.key, new Color(flatIconColor.rgb));
        }
    }

    public ReadableLookAndFeelInfo getAvailableLookAndFeel(String look, String profileName) {
        if (Utils.hasText(look)) {
            for (ReadableLookAndFeelInfo lookAndFeelInfo : this.lookAndFeels) {
                if (!lookAndFeelInfo.getClassName().equals(look)) continue;
                return lookAndFeelInfo;
            }
        }
        if ("dicomizer".equalsIgnoreCase(profileName)) {
            return this.defaultLightTheme;
        }
        return this.defaultDarkTheme;
    }

    public static class ReadableLookAndFeelInfo
    extends UIManager.LookAndFeelInfo {
        private final boolean dark;

        public ReadableLookAndFeelInfo(String name, String className, boolean dark) {
            super(name, className);
            this.dark = dark;
        }

        public boolean isDark() {
            return this.dark;
        }

        @Override
        public String toString() {
            return this.getName();
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof UIManager.LookAndFeelInfo)) {
                return false;
            }
            UIManager.LookAndFeelInfo other = (UIManager.LookAndFeelInfo)obj;
            return this.getClassName().equals(other.getClassName());
        }

        public int hashCode() {
            return this.getClassName().hashCode();
        }
    }
}

