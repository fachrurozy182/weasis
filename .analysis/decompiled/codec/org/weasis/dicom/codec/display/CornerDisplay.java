/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.codec.display;

import org.weasis.dicom.codec.Messages;

public enum CornerDisplay {
    TOP_LEFT(Messages.getString("CornerDisplay.t_left")),
    TOP_RIGHT(Messages.getString("CornerDisplay.t_right")),
    BOTTOM_LEFT(Messages.getString("CornerDisplay.b_left")),
    BOTTOM_RIGHT(Messages.getString("CornerDisplay.b_right"));

    private final String name;

    private CornerDisplay(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }
}

