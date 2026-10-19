/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatAbstractIcon;
import com.formdev.flatlaf.ui.FlatButtonUI;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatUIUtils;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.UIManager;

@FlatStylingSupport.StyleableField(cls=FlatAbstractIcon.class, key="scale")
public class FlatCheckBoxIcon
extends FlatAbstractIcon
implements FlatStylingSupport.StyleableObject {
    protected final String style = UIManager.getString(this.getPropertyPrefix() + "icon.style");
    @FlatStylingSupport.Styleable
    protected float focusWidth = FlatCheckBoxIcon.getUIFloat("CheckBox.icon.focusWidth", UIManager.getInt("Component.focusWidth"), this.style);
    @FlatStylingSupport.Styleable
    protected Color focusColor = FlatUIUtils.getUIColor("CheckBox.icon.focusColor", UIManager.getColor("Component.focusColor"));
    @FlatStylingSupport.Styleable
    protected float borderWidth = FlatCheckBoxIcon.getUIFloat("CheckBox.icon.borderWidth", FlatUIUtils.getUIFloat("Component.borderWidth", 1.0f), this.style);
    @FlatStylingSupport.Styleable
    protected float selectedBorderWidth = FlatCheckBoxIcon.getUIFloat("CheckBox.icon.selectedBorderWidth", Float.MIN_VALUE, this.style);
    @FlatStylingSupport.Styleable
    protected float disabledSelectedBorderWidth = FlatCheckBoxIcon.getUIFloat("CheckBox.icon.disabledSelectedBorderWidth", Float.MIN_VALUE, this.style);
    @FlatStylingSupport.Styleable
    protected float indeterminateBorderWidth = FlatCheckBoxIcon.getUIFloat("CheckBox.icon.indeterminateBorderWidth", Float.MIN_VALUE, this.style);
    @FlatStylingSupport.Styleable
    protected float disabledIndeterminateBorderWidth = FlatCheckBoxIcon.getUIFloat("CheckBox.icon.disabledIndeterminateBorderWidth", Float.MIN_VALUE, this.style);
    @FlatStylingSupport.Styleable
    protected int arc = FlatUIUtils.getUIInt("CheckBox.arc", 2);
    @FlatStylingSupport.Styleable
    protected Color borderColor = FlatCheckBoxIcon.getUIColor("CheckBox.icon.borderColor", this.style);
    @FlatStylingSupport.Styleable
    protected Color background = FlatCheckBoxIcon.getUIColor("CheckBox.icon.background", this.style);
    @FlatStylingSupport.Styleable
    protected Color selectedBorderColor = FlatCheckBoxIcon.getUIColor("CheckBox.icon.selectedBorderColor", this.style);
    @FlatStylingSupport.Styleable
    protected Color selectedBackground = FlatCheckBoxIcon.getUIColor("CheckBox.icon.selectedBackground", this.style);
    @FlatStylingSupport.Styleable
    protected Color checkmarkColor = FlatCheckBoxIcon.getUIColor("CheckBox.icon.checkmarkColor", this.style);
    @FlatStylingSupport.Styleable
    protected Color indeterminateBorderColor = FlatCheckBoxIcon.getUIColor("CheckBox.icon.indeterminateBorderColor", this.style);
    @FlatStylingSupport.Styleable
    protected Color indeterminateBackground = FlatCheckBoxIcon.getUIColor("CheckBox.icon.indeterminateBackground", this.style);
    @FlatStylingSupport.Styleable
    protected Color indeterminateCheckmarkColor = FlatCheckBoxIcon.getUIColor("CheckBox.icon.indeterminateCheckmarkColor", this.style);
    @FlatStylingSupport.Styleable
    protected Color disabledBorderColor = FlatCheckBoxIcon.getUIColor("CheckBox.icon.disabledBorderColor", this.style);
    @FlatStylingSupport.Styleable
    protected Color disabledBackground = FlatCheckBoxIcon.getUIColor("CheckBox.icon.disabledBackground", this.style);
    @FlatStylingSupport.Styleable
    protected Color disabledSelectedBorderColor = FlatCheckBoxIcon.getUIColor("CheckBox.icon.disabledSelectedBorderColor", this.style);
    @FlatStylingSupport.Styleable
    protected Color disabledSelectedBackground = FlatCheckBoxIcon.getUIColor("CheckBox.icon.disabledSelectedBackground", this.style);
    @FlatStylingSupport.Styleable
    protected Color disabledCheckmarkColor = FlatCheckBoxIcon.getUIColor("CheckBox.icon.disabledCheckmarkColor", this.style);
    @FlatStylingSupport.Styleable
    protected Color disabledIndeterminateBorderColor = FlatCheckBoxIcon.getUIColor("CheckBox.icon.disabledIndeterminateBorderColor", this.style);
    @FlatStylingSupport.Styleable
    protected Color disabledIndeterminateBackground = FlatCheckBoxIcon.getUIColor("CheckBox.icon.disabledIndeterminateBackground", this.style);
    @FlatStylingSupport.Styleable
    protected Color disabledIndeterminateCheckmarkColor = FlatCheckBoxIcon.getUIColor("CheckBox.icon.disabledIndeterminateCheckmarkColor", this.style);
    @FlatStylingSupport.Styleable
    protected Color focusedBorderColor = FlatCheckBoxIcon.getUIColor("CheckBox.icon.focusedBorderColor", this.style);
    @FlatStylingSupport.Styleable
    protected Color focusedBackground = FlatCheckBoxIcon.getUIColor("CheckBox.icon.focusedBackground", this.style);
    @FlatStylingSupport.Styleable
    protected Color focusedSelectedBorderColor = FlatCheckBoxIcon.getUIColor("CheckBox.icon.focusedSelectedBorderColor", this.style);
    @FlatStylingSupport.Styleable
    protected Color focusedSelectedBackground = FlatCheckBoxIcon.getUIColor("CheckBox.icon.focusedSelectedBackground", this.style);
    @FlatStylingSupport.Styleable
    protected Color focusedCheckmarkColor = FlatCheckBoxIcon.getUIColor("CheckBox.icon.focusedCheckmarkColor", this.style);
    @FlatStylingSupport.Styleable
    protected Color focusedIndeterminateBorderColor = FlatCheckBoxIcon.getUIColor("CheckBox.icon.focusedIndeterminateBorderColor", this.style);
    @FlatStylingSupport.Styleable
    protected Color focusedIndeterminateBackground = FlatCheckBoxIcon.getUIColor("CheckBox.icon.focusedIndeterminateBackground", this.style);
    @FlatStylingSupport.Styleable
    protected Color focusedIndeterminateCheckmarkColor = FlatCheckBoxIcon.getUIColor("CheckBox.icon.focusedIndeterminateCheckmarkColor", this.style);
    @FlatStylingSupport.Styleable
    protected Color hoverBorderColor = FlatCheckBoxIcon.getUIColor("CheckBox.icon.hoverBorderColor", this.style);
    @FlatStylingSupport.Styleable
    protected Color hoverBackground = FlatCheckBoxIcon.getUIColor("CheckBox.icon.hoverBackground", this.style);
    @FlatStylingSupport.Styleable
    protected Color hoverSelectedBorderColor = FlatCheckBoxIcon.getUIColor("CheckBox.icon.hoverSelectedBorderColor", this.style);
    @FlatStylingSupport.Styleable
    protected Color hoverSelectedBackground = FlatCheckBoxIcon.getUIColor("CheckBox.icon.hoverSelectedBackground", this.style);
    @FlatStylingSupport.Styleable
    protected Color hoverCheckmarkColor = FlatCheckBoxIcon.getUIColor("CheckBox.icon.hoverCheckmarkColor", this.style);
    @FlatStylingSupport.Styleable
    protected Color hoverIndeterminateBorderColor = FlatCheckBoxIcon.getUIColor("CheckBox.icon.hoverIndeterminateBorderColor", this.style);
    @FlatStylingSupport.Styleable
    protected Color hoverIndeterminateBackground = FlatCheckBoxIcon.getUIColor("CheckBox.icon.hoverIndeterminateBackground", this.style);
    @FlatStylingSupport.Styleable
    protected Color hoverIndeterminateCheckmarkColor = FlatCheckBoxIcon.getUIColor("CheckBox.icon.hoverIndeterminateCheckmarkColor", this.style);
    @FlatStylingSupport.Styleable
    protected Color pressedBorderColor = FlatCheckBoxIcon.getUIColor("CheckBox.icon.pressedBorderColor", this.style);
    @FlatStylingSupport.Styleable
    protected Color pressedBackground = FlatCheckBoxIcon.getUIColor("CheckBox.icon.pressedBackground", this.style);
    @FlatStylingSupport.Styleable
    protected Color pressedSelectedBorderColor = FlatCheckBoxIcon.getUIColor("CheckBox.icon.pressedSelectedBorderColor", this.style);
    @FlatStylingSupport.Styleable
    protected Color pressedSelectedBackground = FlatCheckBoxIcon.getUIColor("CheckBox.icon.pressedSelectedBackground", this.style);
    @FlatStylingSupport.Styleable
    protected Color pressedCheckmarkColor = FlatCheckBoxIcon.getUIColor("CheckBox.icon.pressedCheckmarkColor", this.style);
    @FlatStylingSupport.Styleable
    protected Color pressedIndeterminateBorderColor = FlatCheckBoxIcon.getUIColor("CheckBox.icon.pressedIndeterminateBorderColor", this.style);
    @FlatStylingSupport.Styleable
    protected Color pressedIndeterminateBackground = FlatCheckBoxIcon.getUIColor("CheckBox.icon.pressedIndeterminateBackground", this.style);
    @FlatStylingSupport.Styleable
    protected Color pressedIndeterminateCheckmarkColor = FlatCheckBoxIcon.getUIColor("CheckBox.icon.pressedIndeterminateCheckmarkColor", this.style);
    static final int ICON_SIZE = 15;

    protected String getPropertyPrefix() {
        return "CheckBox.";
    }

    protected static Color getUIColor(String key, String style) {
        Color color;
        if (style != null && (color = UIManager.getColor(FlatCheckBoxIcon.styleKey(key, style))) != null) {
            return color;
        }
        return UIManager.getColor(key);
    }

    protected static float getUIFloat(String key, float defaultValue, String style) {
        float value;
        if (style != null && (value = FlatUIUtils.getUIFloat(FlatCheckBoxIcon.styleKey(key, style), Float.MIN_VALUE)) != Float.MIN_VALUE) {
            return value;
        }
        return FlatUIUtils.getUIFloat(key, defaultValue);
    }

    private static String styleKey(String key, String style) {
        return key.replace(".icon.", ".icon[" + style + "].");
    }

    public FlatCheckBoxIcon() {
        super(15, 15, null);
    }

    @Override
    protected void paintIcon(Component c, Graphics2D g) {
        boolean indeterminate = this.isIndeterminate(c);
        boolean selected = indeterminate || this.isSelected(c);
        boolean isFocused = FlatUIUtils.isPermanentFocusOwner(c);
        float bw = Float.MIN_VALUE;
        if (!c.isEnabled()) {
            float f = indeterminate && this.disabledIndeterminateBorderWidth != Float.MIN_VALUE ? this.disabledIndeterminateBorderWidth : (bw = selected ? this.disabledSelectedBorderWidth : this.selectedBorderWidth);
        }
        if (bw == Float.MIN_VALUE) {
            float f = indeterminate && this.indeterminateBorderWidth != Float.MIN_VALUE ? this.indeterminateBorderWidth : (bw = selected && this.selectedBorderWidth != Float.MIN_VALUE ? this.selectedBorderWidth : this.borderWidth);
        }
        if (isFocused && this.focusWidth > 0.0f && FlatButtonUI.isFocusPainted(c)) {
            g.setColor(this.getFocusColor(c));
            this.paintFocusBorder(c, g);
        }
        g.setColor(this.getBorderColor(c, selected, indeterminate));
        this.paintBorder(c, g, bw);
        Color baseBg = FlatUIUtils.stateColor(indeterminate, this.indeterminateBackground, selected, this.selectedBackground, this.background);
        Color bg = FlatUIUtils.deriveColor(this.getBackground(c, selected, indeterminate), baseBg);
        if (bg.getAlpha() < 255) {
            g.setColor(baseBg);
            this.paintBackground(c, g, bw);
        }
        g.setColor(bg);
        this.paintBackground(c, g, bw);
        if (selected) {
            g.setColor(this.getCheckmarkColor(c, indeterminate));
            if (indeterminate) {
                this.paintIndeterminate(c, g);
            } else {
                this.paintCheckmark(c, g);
            }
        }
    }

    protected void paintFocusBorder(Component c, Graphics2D g) {
        float wh = 14.0f + this.focusWidth * 2.0f;
        float arcwh = (float)this.arc + this.focusWidth * 2.0f;
        g.fill(new RoundRectangle2D.Float(-this.focusWidth + 1.0f, -this.focusWidth, wh, wh, arcwh, arcwh));
    }

    protected void paintBorder(Component c, Graphics2D g, float borderWidth) {
        if (borderWidth == 0.0f) {
            return;
        }
        int arcwh = this.arc;
        g.fillRoundRect(1, 0, 14, 14, arcwh, arcwh);
    }

    protected void paintBackground(Component c, Graphics2D g, float borderWidth) {
        float xy = borderWidth;
        float wh = 14.0f - borderWidth * 2.0f;
        float arcwh = (float)this.arc - borderWidth;
        g.fill(new RoundRectangle2D.Float(1.0f + xy, xy, wh, wh, arcwh, arcwh));
    }

    protected void paintCheckmark(Component c, Graphics2D g) {
        Path2D.Float path = new Path2D.Float(1, 3);
        path.moveTo(4.5f, 7.5f);
        path.lineTo(6.6f, 10.0f);
        path.lineTo(11.25f, 3.5f);
        g.setStroke(new BasicStroke(1.9f, 1, 1));
        g.draw(path);
    }

    protected void paintIndeterminate(Component c, Graphics2D g) {
        g.fill(new RoundRectangle2D.Float(3.75f, 5.75f, 8.5f, 2.5f, 2.0f, 2.0f));
    }

    protected boolean isIndeterminate(Component c) {
        return c instanceof JComponent && FlatClientProperties.clientPropertyEquals((JComponent)c, "JButton.selectedState", "indeterminate");
    }

    protected boolean isSelected(Component c) {
        return c instanceof AbstractButton && ((AbstractButton)c).isSelected();
    }

    public float getFocusWidth() {
        return this.focusWidth * this.getScale();
    }

    protected Color getFocusColor(Component c) {
        return this.focusColor;
    }

    protected Color getBorderColor(Component c, boolean selected, boolean indeterminate) {
        return FlatButtonUI.buttonStateColor(c, FlatUIUtils.stateColor(indeterminate, this.indeterminateBorderColor, selected, this.selectedBorderColor, this.borderColor), FlatUIUtils.stateColor(indeterminate, this.disabledIndeterminateBorderColor, selected, this.disabledSelectedBorderColor, this.disabledBorderColor), FlatUIUtils.stateColor(indeterminate, this.focusedIndeterminateBorderColor, selected, this.focusedSelectedBorderColor, this.focusedBorderColor), FlatUIUtils.stateColor(indeterminate, this.hoverIndeterminateBorderColor, selected, this.hoverSelectedBorderColor, this.hoverBorderColor), FlatUIUtils.stateColor(indeterminate, this.pressedIndeterminateBorderColor, selected, this.pressedSelectedBorderColor, this.pressedBorderColor));
    }

    protected Color getBackground(Component c, boolean selected, boolean indeterminate) {
        return FlatButtonUI.buttonStateColor(c, FlatUIUtils.stateColor(indeterminate, this.indeterminateBackground, selected, this.selectedBackground, this.background), FlatUIUtils.stateColor(indeterminate, this.disabledIndeterminateBackground, selected, this.disabledSelectedBackground, this.disabledBackground), FlatUIUtils.stateColor(indeterminate, this.focusedIndeterminateBackground, selected, this.focusedSelectedBackground, this.focusedBackground), FlatUIUtils.stateColor(indeterminate, this.hoverIndeterminateBackground, selected, this.hoverSelectedBackground, this.hoverBackground), FlatUIUtils.stateColor(indeterminate, this.pressedIndeterminateBackground, selected, this.pressedSelectedBackground, this.pressedBackground));
    }

    protected Color getCheckmarkColor(Component c, boolean indeterminate) {
        return FlatButtonUI.buttonStateColor(c, FlatUIUtils.stateColor(indeterminate, this.indeterminateCheckmarkColor, this.checkmarkColor), FlatUIUtils.stateColor(indeterminate, this.disabledIndeterminateCheckmarkColor, this.disabledCheckmarkColor), FlatUIUtils.stateColor(indeterminate, this.focusedIndeterminateCheckmarkColor, this.focusedCheckmarkColor), FlatUIUtils.stateColor(indeterminate, this.hoverIndeterminateCheckmarkColor, this.hoverCheckmarkColor), FlatUIUtils.stateColor(indeterminate, this.pressedIndeterminateCheckmarkColor, this.pressedCheckmarkColor));
    }
}

