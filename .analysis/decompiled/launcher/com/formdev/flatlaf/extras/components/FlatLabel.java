/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.extras.components;

import com.formdev.flatlaf.extras.components.FlatComponentExtension;
import com.formdev.flatlaf.extras.components.FlatStyleableComponent;
import javax.swing.JLabel;

public class FlatLabel
extends JLabel
implements FlatComponentExtension,
FlatStyleableComponent {
    public LabelType getLabelType() {
        return this.getClientPropertyEnumString("FlatLaf.styleClass", LabelType.class, null, LabelType.regular);
    }

    public void setLabelType(LabelType labelType) {
        if (labelType == LabelType.regular) {
            labelType = null;
        }
        this.putClientPropertyEnumString("FlatLaf.styleClass", labelType);
    }

    public static enum LabelType {
        h00,
        h0,
        h1,
        h2,
        h3,
        h4,
        large,
        regular,
        medium,
        small,
        mini,
        monospaced;

    }
}

