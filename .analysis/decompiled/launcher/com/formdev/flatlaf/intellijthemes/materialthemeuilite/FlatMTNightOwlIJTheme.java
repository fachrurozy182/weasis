/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes.materialthemeuilite;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.Utils;

public class FlatMTNightOwlIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Night Owl (Material)";

    public static boolean setup() {
        try {
            return FlatMTNightOwlIJTheme.setup(new FlatMTNightOwlIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatMTNightOwlIJTheme.installLafInfo(NAME, FlatMTNightOwlIJTheme.class);
    }

    public FlatMTNightOwlIJTheme() {
        super(Utils.loadTheme("Night Owl.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

