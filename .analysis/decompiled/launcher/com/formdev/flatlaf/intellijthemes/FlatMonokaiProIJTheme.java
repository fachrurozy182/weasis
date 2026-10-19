/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.Utils;

public class FlatMonokaiProIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Monokai Pro";

    public static boolean setup() {
        try {
            return FlatMonokaiProIJTheme.setup(new FlatMonokaiProIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatMonokaiProIJTheme.installLafInfo(NAME, FlatMonokaiProIJTheme.class);
    }

    public FlatMonokaiProIJTheme() {
        super(Utils.loadTheme("Monokai_Pro.default.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

