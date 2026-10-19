/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.Utils;

public class FlatCobalt2IJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Cobalt 2";

    public static boolean setup() {
        try {
            return FlatCobalt2IJTheme.setup(new FlatCobalt2IJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatCobalt2IJTheme.installLafInfo(NAME, FlatCobalt2IJTheme.class);
    }

    public FlatCobalt2IJTheme() {
        super(Utils.loadTheme("Cobalt_2.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

