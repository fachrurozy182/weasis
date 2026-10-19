/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.Utils;

public class FlatMaterialDesignDarkIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Material Design Dark";

    public static boolean setup() {
        try {
            return FlatMaterialDesignDarkIJTheme.setup(new FlatMaterialDesignDarkIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatMaterialDesignDarkIJTheme.installLafInfo(NAME, FlatMaterialDesignDarkIJTheme.class);
    }

    public FlatMaterialDesignDarkIJTheme() {
        super(Utils.loadTheme("MaterialTheme.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

