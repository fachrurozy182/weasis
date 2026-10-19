/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.icons.FlatAbstractIcon;
import com.formdev.flatlaf.ui.FlatButtonUI;
import com.formdev.flatlaf.ui.FlatTitlePane;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.DerivedColor;
import com.formdev.flatlaf.util.HiDPIUtils;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Window;
import javax.swing.SwingUtilities;

public abstract class FlatWindowAbstractIcon
extends FlatAbstractIcon {
    protected final Insets insets;
    protected final int arc;
    protected final int symbolHeight;
    protected final Color background;
    protected final Color foreground;
    protected final Color inactiveBackground;
    protected final Color inactiveForeground;
    protected final Color hoverBackground;
    protected final Color hoverForeground;
    protected final Color pressedBackground;
    protected final Color pressedForeground;

    protected FlatWindowAbstractIcon(String windowStyle) {
        this(windowStyle, null, null, null, null, null, null, null, null);
    }

    protected FlatWindowAbstractIcon(String windowStyle, Color background, Color foreground, Color inactiveBackground, Color inactiveForeground, Color hoverBackground, Color hoverForeground, Color pressedBackground, Color pressedForeground) {
        this(FlatUIUtils.getSubUIDimension("TitlePane.buttonSize", windowStyle), FlatUIUtils.getSubUIInsets("TitlePane.buttonInsets", windowStyle), FlatUIUtils.getSubUIInt("TitlePane.buttonArc", windowStyle, 0), FlatUIUtils.getSubUIInt("TitlePane.buttonSymbolHeight", windowStyle, 10), background != null ? background : FlatUIUtils.getSubUIColor("TitlePane.buttonBackground", windowStyle), foreground != null ? foreground : FlatUIUtils.getSubUIColor("TitlePane.buttonForeground", windowStyle), inactiveBackground != null ? inactiveBackground : FlatUIUtils.getSubUIColor("TitlePane.buttonInactiveBackground", windowStyle), inactiveForeground != null ? inactiveForeground : FlatUIUtils.getSubUIColor("TitlePane.buttonInactiveForeground", windowStyle), hoverBackground != null ? hoverBackground : FlatUIUtils.getSubUIColor("TitlePane.buttonHoverBackground", windowStyle), hoverForeground != null ? hoverForeground : FlatUIUtils.getSubUIColor("TitlePane.buttonHoverForeground", windowStyle), pressedBackground != null ? pressedBackground : FlatUIUtils.getSubUIColor("TitlePane.buttonPressedBackground", windowStyle), pressedForeground != null ? pressedForeground : FlatUIUtils.getSubUIColor("TitlePane.buttonPressedForeground", windowStyle));
    }

    protected FlatWindowAbstractIcon(Dimension size, Insets insets, int arc, int symbolHeight, Color background, Color foreground, Color inactiveBackground, Color inactiveForeground, Color hoverBackground, Color hoverForeground, Color pressedBackground, Color pressedForeground) {
        super(size.width, size.height, null);
        this.insets = insets != null ? insets : new Insets(0, 0, 0, 0);
        this.arc = arc;
        this.symbolHeight = symbolHeight;
        this.background = background;
        this.foreground = foreground;
        this.inactiveBackground = inactiveBackground;
        this.inactiveForeground = inactiveForeground;
        this.hoverBackground = hoverBackground;
        this.hoverForeground = hoverForeground;
        this.pressedBackground = pressedBackground;
        this.pressedForeground = pressedForeground;
    }

    @Override
    protected void paintIcon(Component c, Graphics2D g) {
        g.setColor(this.getForeground(c));
        HiDPIUtils.paintAtScale1x(g, 0, 0, this.width, this.height, this::paintIconAt1x);
    }

    protected abstract void paintIconAt1x(Graphics2D var1, int var2, int var3, int var4, int var5, double var6);

    @Override
    protected void paintBackground(Component c, Graphics2D g, int x, int y) {
        Color background;
        Color bg = null;
        if (this.background != null || this.inactiveBackground != null) {
            Window window = SwingUtilities.windowForComponent(c);
            Color color = bg = window == null || window.isActive() ? this.background : this.inactiveBackground;
        }
        if ((background = FlatButtonUI.buttonStateColor(c, bg, null, null, this.hoverBackground, this.pressedBackground)) != null) {
            Insets insets = UIScale.scale(this.insets);
            float arc = UIScale.scale((float)this.arc);
            if (background instanceof DerivedColor) {
                Container titlePane = SwingUtilities.getAncestorOfClass(FlatTitlePane.class, c);
                Component baseComp = titlePane != null ? titlePane : c;
                background = FlatUIUtils.deriveColor(background, baseComp.getBackground());
            }
            g.setColor(background);
            FlatUIUtils.paintComponentBackground(g, insets.left, insets.top, c.getWidth() - insets.left - insets.right, c.getHeight() - insets.top - insets.bottom, 0.0f, arc);
        }
    }

    protected Color getForeground(Component c) {
        Color fg = null;
        if (this.foreground != null || this.inactiveForeground != null) {
            Window window = SwingUtilities.windowForComponent(c);
            fg = window == null || window.isActive() ? this.foreground : this.inactiveForeground;
        }
        return FlatButtonUI.buttonStateColor(c, fg != null ? fg : c.getForeground(), null, null, this.hoverForeground, this.pressedForeground);
    }
}

