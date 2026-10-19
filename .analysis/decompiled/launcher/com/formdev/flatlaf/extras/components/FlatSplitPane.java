/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.extras.components;

import com.formdev.flatlaf.extras.components.FlatComponentExtension;
import com.formdev.flatlaf.extras.components.FlatStyleableComponent;
import javax.swing.JSplitPane;

public class FlatSplitPane
extends JSplitPane
implements FlatComponentExtension,
FlatStyleableComponent {
    public ExpandableSide getExpandableSide() {
        return this.getClientPropertyEnumString("JSplitPane.expandableSide", ExpandableSide.class, null, ExpandableSide.both);
    }

    public void setExpandableSide(ExpandableSide expandableSide) {
        this.putClientPropertyEnumString("JSplitPane.expandableSide", expandableSide);
    }

    public static enum ExpandableSide {
        both,
        left,
        right;

    }
}

