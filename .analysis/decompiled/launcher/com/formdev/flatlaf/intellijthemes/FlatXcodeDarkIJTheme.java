/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.Utils;

public class FlatXcodeDarkIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Xcode-Dark";

    public static boolean setup() {
        try {
            return FlatXcodeDarkIJTheme.setup(new FlatXcodeDarkIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatXcodeDarkIJTheme.installLafInfo(NAME, FlatXcodeDarkIJTheme.class);
    }

    public FlatXcodeDarkIJTheme() {
        super(Utils.loadTheme("Xcode-Dark.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

