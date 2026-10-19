/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes.materialthemeuilite;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.Utils;

public class FlatMTMaterialPalenightIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Material Palenight (Material)";

    public static boolean setup() {
        try {
            return FlatMTMaterialPalenightIJTheme.setup(new FlatMTMaterialPalenightIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatMTMaterialPalenightIJTheme.installLafInfo(NAME, FlatMTMaterialPalenightIJTheme.class);
    }

    public FlatMTMaterialPalenightIJTheme() {
        super(Utils.loadTheme("Material Palenight.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

