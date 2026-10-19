/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes.materialthemeuilite;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.Utils;

public class FlatMTMoonlightIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Moonlight (Material)";

    public static boolean setup() {
        try {
            return FlatMTMoonlightIJTheme.setup(new FlatMTMoonlightIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatMTMoonlightIJTheme.installLafInfo(NAME, FlatMTMoonlightIJTheme.class);
    }

    public FlatMTMoonlightIJTheme() {
        super(Utils.loadTheme("Moonlight.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

