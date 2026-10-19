/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.Utils;

public class FlatNordIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Nord";

    public static boolean setup() {
        try {
            return FlatNordIJTheme.setup(new FlatNordIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatNordIJTheme.installLafInfo(NAME, FlatNordIJTheme.class);
    }

    public FlatNordIJTheme() {
        super(Utils.loadTheme("nord.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

