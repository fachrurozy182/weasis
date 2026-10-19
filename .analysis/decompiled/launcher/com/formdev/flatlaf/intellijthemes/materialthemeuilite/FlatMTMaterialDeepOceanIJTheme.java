/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes.materialthemeuilite;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.Utils;

public class FlatMTMaterialDeepOceanIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "Material Deep Ocean (Material)";

    public static boolean setup() {
        try {
            return FlatMTMaterialDeepOceanIJTheme.setup(new FlatMTMaterialDeepOceanIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatMTMaterialDeepOceanIJTheme.installLafInfo(NAME, FlatMTMaterialDeepOceanIJTheme.class);
    }

    public FlatMTMaterialDeepOceanIJTheme() {
        super(Utils.loadTheme("Material Deep Ocean.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

