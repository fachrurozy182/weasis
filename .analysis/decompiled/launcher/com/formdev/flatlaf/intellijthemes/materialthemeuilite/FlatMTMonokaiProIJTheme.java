/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes.materialthemeuilite;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.Utils;

public class FlatMTMonokaiProIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Monokai Pro (Material)";

    public static boolean setup() {
        try {
            return FlatMTMonokaiProIJTheme.setup(new FlatMTMonokaiProIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatMTMonokaiProIJTheme.installLafInfo(NAME, FlatMTMonokaiProIJTheme.class);
    }

    public FlatMTMonokaiProIJTheme() {
        super(Utils.loadTheme("Monokai Pro.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

