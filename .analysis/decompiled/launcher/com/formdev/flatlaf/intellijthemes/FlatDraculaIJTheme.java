/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.Utils;

public class FlatDraculaIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Dracula";

    public static boolean setup() {
        try {
            return FlatDraculaIJTheme.setup(new FlatDraculaIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatDraculaIJTheme.installLafInfo(NAME, FlatDraculaIJTheme.class);
    }

    public FlatDraculaIJTheme() {
        super(Utils.loadTheme("Dracula.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

