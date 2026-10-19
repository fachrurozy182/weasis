/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes.materialthemeuilite;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.Utils;

public class FlatMTMaterialDarkerIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Material Darker (Material)";

    public static boolean setup() {
        try {
            return FlatMTMaterialDarkerIJTheme.setup(new FlatMTMaterialDarkerIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatMTMaterialDarkerIJTheme.installLafInfo(NAME, FlatMTMaterialDarkerIJTheme.class);
    }

    public FlatMTMaterialDarkerIJTheme() {
        super(Utils.loadTheme("Material Darker.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

