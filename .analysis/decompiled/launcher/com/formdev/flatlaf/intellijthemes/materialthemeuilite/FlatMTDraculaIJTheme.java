/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes.materialthemeuilite;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.Utils;

public class FlatMTDraculaIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Dracula (Material)";

    public static boolean setup() {
        try {
            return FlatMTDraculaIJTheme.setup(new FlatMTDraculaIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatMTDraculaIJTheme.installLafInfo(NAME, FlatMTDraculaIJTheme.class);
    }

    public FlatMTDraculaIJTheme() {
        super(Utils.loadTheme("Dracula.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

