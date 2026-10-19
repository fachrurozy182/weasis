/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes.materialthemeuilite;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.Utils;

public class FlatMTGitHubIJTheme
extends IntelliJTheme.ThemeLaf {
    public static final String NAME = "GitHub (Material)";

    public static boolean setup() {
        try {
            return FlatMTGitHubIJTheme.setup(new FlatMTGitHubIJTheme());
        }
        catch (RuntimeException ex) {
            return false;
        }
    }

    public static void installLafInfo() {
        FlatMTGitHubIJTheme.installLafInfo(NAME, FlatMTGitHubIJTheme.class);
    }

    public FlatMTGitHubIJTheme() {
        super(Utils.loadTheme("GitHub.theme.json"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}

