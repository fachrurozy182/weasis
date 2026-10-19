/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes.materialthemeuilite;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.Utils;

public class FlatMTAtomOneLightIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Atom One Light (Material)";

    public static boolean setup() {
        try {
            return FlatMTAtomOneLightIJTheme.setup(new FlatMTAtomOneLightIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatMTAtomOneLightIJTheme.installLafInfo(NAME, FlatMTAtomOneLightIJTheme.class);
    }

    public FlatMTAtomOneLightIJTheme() {
        super(Utils.loadTheme("Atom One Light.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

