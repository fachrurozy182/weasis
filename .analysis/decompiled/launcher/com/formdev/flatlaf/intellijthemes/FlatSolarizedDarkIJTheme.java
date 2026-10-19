/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.Utils;

public class FlatSolarizedDarkIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Solarized Dark";

    public static boolean setup() {
        try {
            return FlatSolarizedDarkIJTheme.setup(new FlatSolarizedDarkIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatSolarizedDarkIJTheme.installLafInfo(NAME, FlatSolarizedDarkIJTheme.class);
    }

    public FlatSolarizedDarkIJTheme() {
        super(Utils.loadTheme("SolarizedDark.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

