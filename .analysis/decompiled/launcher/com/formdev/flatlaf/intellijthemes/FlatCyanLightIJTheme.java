/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.Utils;

public class FlatCyanLightIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Cyan light";

    public static boolean setup() {
        try {
            return FlatCyanLightIJTheme.setup(new FlatCyanLightIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatCyanLightIJTheme.installLafInfo(NAME, FlatCyanLightIJTheme.class);
    }

    public FlatCyanLightIJTheme() {
        super(Utils.loadTheme("Cyan.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

