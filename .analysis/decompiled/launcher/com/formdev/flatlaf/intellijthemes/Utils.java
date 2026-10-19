/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.intellijthemes;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.util.LoggingFacade;
import java.io.IOException;

class Utils {
    Utils() {
    }

    static IntelliJTheme loadTheme(String name) {
        try {
            return new IntelliJTheme(Utils.class.getResourceAsStream("/com/formdev/flatlaf/intellijthemes/themes/" + name));
        }
        catch (IOException ex) {
            String msg = "FlatLaf: Failed to load IntelliJ theme '" + name + "'";
            LoggingFacade.INSTANCE.logSevere(msg, ex);
            throw new RuntimeException(msg, ex);
        }
    }
}

