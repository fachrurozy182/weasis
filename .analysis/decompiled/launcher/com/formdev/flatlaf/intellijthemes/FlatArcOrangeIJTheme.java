/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.Utils;

public class FlatArcOrangeIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Arc - Orange";

    public static boolean setup() {
        try {
            return FlatArcOrangeIJTheme.setup(new FlatArcOrangeIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatArcOrangeIJTheme.installLafInfo(NAME, FlatArcOrangeIJTheme.class);
    }

    public FlatArcOrangeIJTheme() {
        super(Utils.loadTheme("arc-theme-orange.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

