/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.Utils;

public class FlatDarkFlatIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Dark Flat";

    public static boolean setup() {
        try {
            return FlatDarkFlatIJTheme.setup(new FlatDarkFlatIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatDarkFlatIJTheme.installLafInfo(NAME, FlatDarkFlatIJTheme.class);
    }

    public FlatDarkFlatIJTheme() {
        super(Utils.loadTheme("DarkFlatTheme.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

