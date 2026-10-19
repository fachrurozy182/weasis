/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes.materialthemeuilite;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.Utils;

public class FlatMTMaterialLighterIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Material Lighter (Material)";

    public static boolean setup() {
        try {
            return FlatMTMaterialLighterIJTheme.setup(new FlatMTMaterialLighterIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatMTMaterialLighterIJTheme.installLafInfo(NAME, FlatMTMaterialLighterIJTheme.class);
    }

    public FlatMTMaterialLighterIJTheme() {
        super(Utils.loadTheme("Material Lighter.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

