/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.extras.components;

import com.formdev.flatlaf.extras.components.FlatComponentExtension;
import com.formdev.flatlaf.extras.components.FlatStyleableComponent;
import java.awt.Insets;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTextField;

public class FlatTextField
extends JTextField
implements FlatComponentExtension,
FlatStyleableComponent {
    public String getPlaceholderText() {
        return (String)this.getClientProperty("JTextField.placeholderText");
    }

    public void setPlaceholderText(String placeholderText) {
        this.putClientProperty("JTextField.placeholderText", placeholderText);
    }

    public Icon getLeadingIcon() {
        return (Icon)this.getClientProperty("JTextField.leadingIcon");
    }

    public void setLeadingIcon(Icon leadingIcon) {
        this.putClientProperty("JTextField.leadingIcon", leadingIcon);
    }

    public Icon getTrailingIcon() {
        return (Icon)this.getClientProperty("JTextField.trailingIcon");
    }

    public void setTrailingIcon(Icon trailingIcon) {
        this.putClientProperty("JTextField.trailingIcon", trailingIcon);
    }

    public JComponent getLeadingComponent() {
        return (JComponent)this.getClientProperty("JTextField.leadingComponent");
    }

    public void setLeadingComponent(JComponent leadingComponent) {
        this.putClientProperty("JTextField.leadingComponent", leadingComponent);
    }

    public JComponent getTrailingComponent() {
        return (JComponent)this.getClientProperty("JTextField.trailingComponent");
    }

    public void setTrailingComponent(JComponent trailingComponent) {
        this.putClientProperty("JTextField.trailingComponent", trailingComponent);
    }

    public boolean isShowClearButton() {
        return this.getClientPropertyBoolean((Object)"JTextField.showClearButton", false);
    }

    public void setShowClearButton(boolean showClearButton) {
        this.putClientPropertyBoolean("JTextField.showClearButton", showClearButton, false);
    }

    public SelectAllOnFocusPolicy getSelectAllOnFocusPolicy() {
        return this.getClientPropertyEnumString("JTextField.selectAllOnFocusPolicy", SelectAllOnFocusPolicy.class, "TextComponent.selectAllOnFocusPolicy", SelectAllOnFocusPolicy.once);
    }

    public void setSelectAllOnFocusPolicy(SelectAllOnFocusPolicy selectAllOnFocusPolicy) {
        this.putClientPropertyEnumString("JTextField.selectAllOnFocusPolicy", selectAllOnFocusPolicy);
    }

    public Insets getPadding() {
        return (Insets)this.getClientProperty("JTextField.padding");
    }

    public void setPadding(Insets padding) {
        this.putClientProperty("JTextField.padding", padding);
    }

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

    public static enum SelectAllOnFocusPolicy {
        never,
        once,
        always;

    }
}

