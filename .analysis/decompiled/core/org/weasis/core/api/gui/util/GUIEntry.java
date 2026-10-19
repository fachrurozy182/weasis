/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui.util;

import javax.swing.Icon;

public interface GUIEntry {
    default public Icon getIcon() {
        return null;
    }

    public String getUIName();

    public String getDescription();
}

