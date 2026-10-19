/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes.materialthemeuilite;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.Utils;

public class FlatMTAtomOneDarkIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Atom One Dark (Material)";

    public static boolean setup() {
        try {
            return FlatMTAtomOneDarkIJTheme.setup(new FlatMTAtomOneDarkIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatMTAtomOneDarkIJTheme.installLafInfo(NAME, FlatMTAtomOneDarkIJTheme.class);
    }

    public FlatMTAtomOneDarkIJTheme() {
        super(Utils.loadTheme("Atom One Dark.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

