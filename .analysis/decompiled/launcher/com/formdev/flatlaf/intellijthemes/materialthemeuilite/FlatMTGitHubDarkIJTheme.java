/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes.materialthemeuilite;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.Utils;

public class FlatMTGitHubDarkIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "GitHub Dark (Material)";

    public static boolean setup() {
        try {
            return FlatMTGitHubDarkIJTheme.setup(new FlatMTGitHubDarkIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatMTGitHubDarkIJTheme.installLafInfo(NAME, FlatMTGitHubDarkIJTheme.class);
    }

    public FlatMTGitHubDarkIJTheme() {
        super(Utils.loadTheme("GitHub Dark.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

