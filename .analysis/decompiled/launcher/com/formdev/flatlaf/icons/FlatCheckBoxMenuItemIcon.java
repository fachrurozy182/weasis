/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.icons.FlatAbstractIcon;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import javax.swing.AbstractButton;
import javax.swing.JMenuItem;
import javax.swing.UIManager;

@FlatStylingSupport.StyleableField(cls=FlatAbstractIcon.class, key="scale")
public class FlatCheckBoxMenuItemIcon
extends FlatAbstractIcon
implements FlatStylingSupport.StyleableObject {
    @FlatStylingSupport.Styleable
    protected Color checkmarkColor = UIManager.getColor("CheckBoxMenuItem.icon.checkmarkColor");
    @FlatStylingSupport.Styleable
    protected Color disabledCheckmarkColor = UIManager.getColor("CheckBoxMenuItem.icon.disabledCheckmarkColor");
    @FlatStylingSupport.Styleable
    protected Color selectionForeground = UIManager.getColor("MenuItem.selectionForeground");

    public FlatCheckBoxMenuItemIcon() {
        super(15, 15, null);
    }

    @Override
    protected void paintIcon(Component c, Graphics2D g2) {
        boolean selected;
        boolean bl = selected = c instanceof AbstractButton && ((AbstractButton)c).isSelected();
        if (selected) {
            g2.setColor(this.getCheckmarkColor(c));
            this.paintCheckmark(g2);
        }
    }

    protected void paintCheckmark(Graphics2D g2) {
        Path2D.Float path = new Path2D.Float(1, 3);
        path.moveTo(4.5f, 7.5f);
        path.lineTo(6.6f, 10.0f);
        path.lineTo(11.25f, 3.5f);
        g2.setStroke(new BasicStroke(1.9f, 1, 1));
        g2.draw(path);
    }

    protected Color getCheckmarkColor(Component c) {
        if (c instanceof JMenuItem && ((JMenuItem)c).isArmed() && !this.isUnderlineSelection()) {
            return this.selectionForeground;
        }
        return c.isEnabled() ? this.checkmarkColor : this.disabledCheckmarkColor;
    }

    protected boolean isUnderlineSelection() {
        return "underline".equals(UIManager.getString("MenuItem.selectionType"));
    }
}

