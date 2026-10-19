/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.extras.components;

import com.formdev.flatlaf.extras.components.FlatButton;
import com.formdev.flatlaf.extras.components.FlatComponentExtension;
import com.formdev.flatlaf.extras.components.FlatStyleableComponent;
import java.awt.Color;
import javax.swing.JToggleButton;

public class FlatToggleButton
extends JToggleButton
implements FlatComponentExtension,
FlatStyleableComponent {
    public FlatButton.ButtonType getButtonType() {
        return this.getClientPropertyEnumString("JButton.buttonType", FlatButton.ButtonType.class, null, FlatButton.ButtonType.none);
    }

    public void setButtonType(FlatButton.ButtonType buttonType) {
        if (buttonType == FlatButton.ButtonType.none) {
            buttonType = null;
        }
        this.putClientPropertyEnumString("JButton.buttonType", buttonType);
    }

    public boolean isSquareSize() {
        return this.getClientPropertyBoolean((Object)"JButton.squareSize", false);
    }

    public void setSquareSize(boolean squareSize) {
        this.putClientPropertyBoolean("JButton.squareSize", squareSize, false);
    }

    public int getMinimumWidth() {
        return this.getClientPropertyInt((Object)"JComponent.minimumWidth", "ToggleButton.minimumWidth");
    }

    public void setMinimumWidth(int minimumWidth) {
        this.putClientProperty("JComponent.minimumWidth", minimumWidth >= 0 ? Integer.valueOf(minimumWidth) : null);
    }

    public int getMinimumHeight() {
        return this.getClientPropertyInt((Object)"JComponent.minimumHeight", 0);
    }

    public void setMinimumHeight(int minimumHeight) {
        this.putClientProperty("JComponent.minimumHeight", minimumHeight >= 0 ? Integer.valueOf(minimumHeight) : null);
    }

    public Object getOutline() {
        return this.getClientProperty("JComponent.outline");
    }

    public void setOutline(Object outline) {
        this.putClientProperty("JComponent.outline", outline);
    }

    public int getTabUnderlinePlacement() {
        return this.getClientPropertyInt((Object)"JToggleButton.tab.underlinePlacement", 3);
    }

    public void setTabUnderlinePlacement(int placement) {
        this.putClientProperty("JToggleButton.tab.underlinePlacement", placement >= 0 ? Integer.valueOf(placement) : null);
    }

    public int getTabUnderlineHeight() {
        return this.getClientPropertyInt((Object)"JToggleButton.tab.underlineHeight", "ToggleButton.tab.underlineHeight");
    }

    public void setTabUnderlineHeight(int tabUnderlineHeight) {
        this.putClientProperty("JToggleButton.tab.underlineHeight", tabUnderlineHeight >= 0 ? Integer.valueOf(tabUnderlineHeight) : null);
    }

    public Color getTabUnderlineColor() {
        return this.getClientPropertyColor("JToggleButton.tab.underlineColor", "ToggleButton.tab.underlineColor");
    }

    public void setTabUnderlineColor(Color tabUnderlineColor) {
        this.putClientProperty("JToggleButton.tab.underlineColor", tabUnderlineColor);
    }

    public Color getTabSelectedBackground() {
        return this.getClientPropertyColor("JToggleButton.tab.selectedBackground", "ToggleButton.tab.selectedBackground");
    }

    public void setTabSelectedBackground(Color tabSelectedBackground) {
        this.putClientProperty("JToggleButton.tab.selectedBackground", tabSelectedBackground);
    }
}

