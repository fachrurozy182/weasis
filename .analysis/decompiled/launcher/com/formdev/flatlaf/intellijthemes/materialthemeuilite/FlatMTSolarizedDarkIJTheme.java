/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes.materialthemeuilite;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.Utils;

public class FlatMTSolarizedDarkIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Solarized Dark (Material)";

    public static boolean setup() {
        try {
            return FlatMTSolarizedDarkIJTheme.setup(new FlatMTSolarizedDarkIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatMTSolarizedDarkIJTheme.installLafInfo(NAME, FlatMTSolarizedDarkIJTheme.class);
    }

    public FlatMTSolarizedDarkIJTheme() {
        super(Utils.loadTheme("Solarized Dark.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

