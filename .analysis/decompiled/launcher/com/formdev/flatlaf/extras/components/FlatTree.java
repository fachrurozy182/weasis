/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.extras.components;

import com.formdev.flatlaf.extras.components.FlatComponentExtension;
import com.formdev.flatlaf.extras.components.FlatStyleableComponent;
import javax.swing.JTree;

public class FlatTree
extends JTree
implements FlatComponentExtension,
FlatStyleableComponent {
    public boolean isWideSelection() {
        return this.getClientPropertyBoolean((Object)"JTree.wideSelection", "Tree.wideSelection");
    }

    public void setWideSelection(boolean wideSelection) {
        this.putClientProperty("JTree.wideSelection", wideSelection);
    }

    public boolean isWideCellRenderer() {
        return this.getClientPropertyBoolean((Object)"JTree.wideCellRenderer", "Tree.wideCellRenderer");
    }

    public void setWideCellRenderer(boolean wideCellRenderer) {
        this.putClientProperty("JTree.wideCellRenderer", wideCellRenderer);
    }

    public boolean isPaintSelection() {
        return this.getClientPropertyBoolean((Object)"JTree.paintSelection", true);
    }

    public void setPaintSelection(boolean paintSelection) {
        this.putClientProperty("JTree.paintSelection", paintSelection);
    }
}

