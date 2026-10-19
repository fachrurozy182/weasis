/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes.materialthemeuilite;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.Utils;

public class FlatMTLightOwlIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Light Owl (Material)";

    public static boolean setup() {
        try {
            return FlatMTLightOwlIJTheme.setup(new FlatMTLightOwlIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatMTLightOwlIJTheme.installLafInfo(NAME, FlatMTLightOwlIJTheme.class);
    }

    public FlatMTLightOwlIJTheme() {
        super(Utils.loadTheme("Light Owl.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

