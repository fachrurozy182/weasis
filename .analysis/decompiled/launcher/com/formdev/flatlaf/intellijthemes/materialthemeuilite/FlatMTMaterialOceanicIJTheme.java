/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes.materialthemeuilite;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.Utils;

public class FlatMTMaterialOceanicIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Material Oceanic (Material)";

    public static boolean setup() {
        try {
            return FlatMTMaterialOceanicIJTheme.setup(new FlatMTMaterialOceanicIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatMTMaterialOceanicIJTheme.installLafInfo(NAME, FlatMTMaterialOceanicIJTheme.class);
    }

    public FlatMTMaterialOceanicIJTheme() {
        super(Utils.loadTheme("Material Oceanic.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

