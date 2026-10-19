/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf;

import com.formdev.flatlaf.UIDefaultsLoader;
import java.io.InputStream;
import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;

public abstract class FlatDefaultsAddon {
    public InputStream getDefaults(Class<?> lafClass) {
        Class<?> addonClass = this.getClass();
        String propertiesName = '/' + addonClass.getPackage().getName().replace('.', '/') + '/' + UIDefaultsLoader.simpleClassName(lafClass) + ".properties";
        return addonClass.getResourceAsStream(propertiesName);
    }

    public void afterDefaultsLoading(LookAndFeel laf, UIDefaults defaults) {
    }

    public int getPriority() {
        return 10000;
    }
}

