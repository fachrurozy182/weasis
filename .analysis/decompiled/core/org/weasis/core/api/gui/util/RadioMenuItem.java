/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui.util;

import javax.swing.Icon;
import javax.swing.JRadioButtonMenuItem;

public class RadioMenuItem
extends JRadioButtonMenuItem {
    private final Object userObject;

    public RadioMenuItem(String text, Object userObject) {
        this(text, null, userObject);
    }

    public RadioMenuItem(String text, Icon icon, Object userObject) {
        this(text, icon, userObject, false);
    }

    public RadioMenuItem(String text, Icon icon, Object userObject, boolean selected) {
        super(text, icon, selected);
        this.userObject = userObject;
    }

    public Object getUserObject() {
        return this.userObject;
    }
}

