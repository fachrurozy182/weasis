/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.Utils;

public class FlatGruvboxDarkHardIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Gruvbox Dark Hard";

    public static boolean setup() {
        try {
            return FlatGruvboxDarkHardIJTheme.setup(new FlatGruvboxDarkHardIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatGruvboxDarkHardIJTheme.installLafInfo(NAME, FlatGruvboxDarkHardIJTheme.class);
    }

    public FlatGruvboxDarkHardIJTheme() {
        super(Utils.loadTheme("gruvbox_dark_hard.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

