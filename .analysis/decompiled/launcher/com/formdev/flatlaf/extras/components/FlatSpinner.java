/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.extras.components;

import com.formdev.flatlaf.extras.components.FlatComponentExtension;
import com.formdev.flatlaf.extras.components.FlatStyleableComponent;
import javax.swing.JSpinner;

public class FlatSpinner
extends JSpinner
implements FlatComponentExtension,
FlatStyleableComponent {
    public int getMinimumWidth() {
        return this.getClientPropertyInt((Object)"JComponent.minimumWidth", "Component.minimumWidth");
    }

    public void setMinimumWidth(int minimumWidth) {
        this.putClientProperty("JComponent.minimumWidth", minimumWidth >= 0 ? Integer.valueOf(minimumWidth) : null);
    }

    public boolean isRoundRect() {
        return this.getClientPropertyBoolean((Object)"JComponent.roundRect", false);
    }

    public void setRoundRect(boolean roundRect) {
        this.putClientPropertyBoolean("JComponent.roundRect", roundRect, false);
    }

    public Object getOutline() {
        return this.getClientProperty("JComponent.outline");
    }

    public void setOutline(Object outline) {
        this.putClientProperty("JComponent.outline", outline);
    }
}

