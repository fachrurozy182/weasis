/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.Utils;

public class FlatCarbonIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Carbon";

    public static boolean setup() {
        try {
            return FlatCarbonIJTheme.setup(new FlatCarbonIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatCarbonIJTheme.installLafInfo(NAME, FlatCarbonIJTheme.class);
    }

    public FlatCarbonIJTheme() {
        super(Utils.loadTheme("Carbon.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

