/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.Utils;

public class FlatDarkPurpleIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Dark purple";

    public static boolean setup() {
        try {
            return FlatDarkPurpleIJTheme.setup(new FlatDarkPurpleIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatDarkPurpleIJTheme.installLafInfo(NAME, FlatDarkPurpleIJTheme.class);
    }

    public FlatDarkPurpleIJTheme() {
        super(Utils.loadTheme("DarkPurple.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

