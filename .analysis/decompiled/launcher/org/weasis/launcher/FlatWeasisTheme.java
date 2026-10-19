/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.launcher;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.util.LoggingFacade;
import java.io.IOException;
import java.util.Objects;

public class FlatWeasisTheme
extends IntelliJTheme.ThemeLaf {
    public static boolean setup() {
        return FlatWeasisTheme.setup(new FlatWeasisTheme());
    }

    public FlatWeasisTheme() {
        super(FlatWeasisTheme.loadTheme("Weasis.theme.json"));
    }

    @Override
    public String getName() {
        return "Weasis";
    }

    static IntelliJTheme loadTheme(String name) {
        try {
            return new IntelliJTheme(Objects.requireNonNull(FlatWeasisTheme.class.getResourceAsStream("/org/weasis/theme/" + name)));
        }
        catch (IOException var3) {
            String msg = "FlatLaf: Failed to load Weasis theme";
            LoggingFacade.INSTANCE.logSevere(msg, var3);
            throw new RuntimeException(msg, var3);
        }
    }
}

