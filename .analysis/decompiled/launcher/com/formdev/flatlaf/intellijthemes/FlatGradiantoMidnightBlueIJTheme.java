/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.Utils;

public class FlatGradiantoMidnightBlueIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Gradianto Midnight Blue";

    public static boolean setup() {
        try {
            return FlatGradiantoMidnightBlueIJTheme.setup(new FlatGradiantoMidnightBlueIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatGradiantoMidnightBlueIJTheme.installLafInfo(NAME, FlatGradiantoMidnightBlueIJTheme.class);
    }

    public FlatGradiantoMidnightBlueIJTheme() {
        super(Utils.loadTheme("Gradianto_midnight_blue.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

