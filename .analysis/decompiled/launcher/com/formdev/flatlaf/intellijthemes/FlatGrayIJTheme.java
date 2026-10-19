/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.Utils;

public class FlatGrayIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Gray";

    public static boolean setup() {
        try {
            return FlatGrayIJTheme.setup(new FlatGrayIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatGrayIJTheme.installLafInfo(NAME, FlatGrayIJTheme.class);
    }

    public FlatGrayIJTheme() {
        super(Utils.loadTheme("Gray.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

