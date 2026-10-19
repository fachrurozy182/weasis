/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.Utils;

public class FlatHighContrastIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "High Contrast";

    public static boolean setup() {
        try {
            return FlatHighContrastIJTheme.setup(new FlatHighContrastIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatHighContrastIJTheme.installLafInfo(NAME, FlatHighContrastIJTheme.class);
    }

    public FlatHighContrastIJTheme() {
        super(Utils.loadTheme("HighContrast.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

