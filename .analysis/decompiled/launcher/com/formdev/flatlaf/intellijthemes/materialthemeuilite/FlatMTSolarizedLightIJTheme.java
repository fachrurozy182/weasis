/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes.materialthemeuilite;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.Utils;

public class FlatMTSolarizedLightIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Solarized Light (Material)";

    public static boolean setup() {
        try {
            return FlatMTSolarizedLightIJTheme.setup(new FlatMTSolarizedLightIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatMTSolarizedLightIJTheme.installLafInfo(NAME, FlatMTSolarizedLightIJTheme.class);
    }

    public FlatMTSolarizedLightIJTheme() {
        super(Utils.loadTheme("Solarized Light.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

