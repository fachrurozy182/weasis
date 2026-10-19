/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui.util;

import java.util.Objects;

public class CheckBoxModel {
    private final Object object;
    private boolean selected;

    public CheckBoxModel(Object object, boolean selected) {
        this.object = Objects.requireNonNull(object);
        this.selected = selected;
    }

    public Object getObject() {
        return this.object;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}

