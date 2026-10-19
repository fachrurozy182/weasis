/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes.materialthemeuilite;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.Utils;

public class FlatMTArcDarkIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Arc Dark (Material)";

    public static boolean setup() {
        try {
            return FlatMTArcDarkIJTheme.setup(new FlatMTArcDarkIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatMTArcDarkIJTheme.installLafInfo(NAME, FlatMTArcDarkIJTheme.class);
    }

    public FlatMTArcDarkIJTheme() {
        super(Utils.loadTheme("Arc Dark.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

