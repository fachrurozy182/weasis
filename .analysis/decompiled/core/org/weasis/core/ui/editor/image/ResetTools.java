/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.editor.image;

import org.weasis.core.Messages;

public enum ResetTools {
    ALL(Messages.getString("ResetTools.all")),
    WL(Messages.getString("ResetTools.wl")),
    ZOOM(Messages.getString("ResetTools.zoom")),
    ROTATION(Messages.getString("ResetTools.rotation")),
    PAN(Messages.getString("ResetTools.pan"));

    private final String name;

    private ResetTools(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }
}

