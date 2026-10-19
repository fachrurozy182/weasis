/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.Utils;

public class FlatSolarizedLightIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Solarized Light";

    public static boolean setup() {
        try {
            return FlatSolarizedLightIJTheme.setup(new FlatSolarizedLightIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatSolarizedLightIJTheme.installLafInfo(NAME, FlatSolarizedLightIJTheme.class);
    }

    public FlatSolarizedLightIJTheme() {
        super(Utils.loadTheme("SolarizedLight.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

