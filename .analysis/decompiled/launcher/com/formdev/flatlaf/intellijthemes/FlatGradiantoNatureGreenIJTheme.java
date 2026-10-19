/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.Utils;

public class FlatGradiantoNatureGreenIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Gradianto Nature Green";

    public static boolean setup() {
        try {
            return FlatGradiantoNatureGreenIJTheme.setup(new FlatGradiantoNatureGreenIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatGradiantoNatureGreenIJTheme.installLafInfo(NAME, FlatGradiantoNatureGreenIJTheme.class);
    }

    public FlatGradiantoNatureGreenIJTheme() {
        super(Utils.loadTheme("Gradianto_Nature_Green.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

