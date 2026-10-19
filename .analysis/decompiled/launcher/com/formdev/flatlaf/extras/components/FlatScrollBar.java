/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.extras.components;

import com.formdev.flatlaf.extras.components.FlatComponentExtension;
import com.formdev.flatlaf.extras.components.FlatStyleableComponent;
import javax.swing.JScrollBar;

public class FlatScrollBar
extends JScrollBar
implements FlatComponentExtension,
FlatStyleableComponent {
    public boolean isShowButtons() {
        return this.getClientPropertyBoolean((Object)"JScrollBar.showButtons", "ScrollBar.showButtons");
    }

    public void setShowButtons(boolean showButtons) {
        this.putClientProperty("JScrollBar.showButtons", showButtons);
    }
}

