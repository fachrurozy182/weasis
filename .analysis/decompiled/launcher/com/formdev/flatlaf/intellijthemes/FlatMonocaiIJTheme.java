/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.Utils;

public class FlatMonocaiIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Monocai";

    public static boolean setup() {
        try {
            return FlatMonocaiIJTheme.setup(new FlatMonocaiIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatMonocaiIJTheme.installLafInfo(NAME, FlatMonocaiIJTheme.class);
    }

    public FlatMonocaiIJTheme() {
        super(Utils.loadTheme("Monocai.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

