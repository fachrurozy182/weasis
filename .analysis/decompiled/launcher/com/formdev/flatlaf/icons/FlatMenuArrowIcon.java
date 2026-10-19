/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.icons.FlatAbstractIcon;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatUIUtils;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import javax.swing.JMenu;
import javax.swing.UIManager;

@FlatStylingSupport.StyleableField(cls=FlatAbstractIcon.class, key="scale")
public class FlatMenuArrowIcon
extends FlatAbstractIcon
implements FlatStylingSupport.StyleableObject {
    @FlatStylingSupport.Styleable
    protected String arrowType = UIManager.getString("Component.arrowType");
    @FlatStylingSupport.Styleable
    protected Color arrowColor = UIManager.getColor("Menu.icon.arrowColor");
    @FlatStylingSupport.Styleable
    protected Color disabledArrowColor = UIManager.getColor("Menu.icon.disabledArrowColor");
    @FlatStylingSupport.Styleable
    protected Color selectionForeground = UIManager.getColor("Menu.selectionForeground");

    public FlatMenuArrowIcon() {
        super(6, 10, null);
    }

    @Override
    protected void paintIcon(Component c, Graphics2D g) {
        if (c != null && !c.getComponentOrientation().isLeftToRight()) {
            g.rotate(Math.toRadians(180.0), (double)this.width / 2.0, (double)this.height / 2.0);
        }
        g.setColor(this.getArrowColor(c));
        if (FlatUIUtils.isChevron(this.arrowType)) {
            Path2D path = FlatUIUtils.createPath(false, 1.0, 1.0, 5.0, 5.0, 1.0, 9.0);
            g.setStroke(new BasicStroke(1.0f));
            g.draw(path);
        } else {
            g.fill(FlatUIUtils.createPath(0.0, 0.5, 5.0, 5.0, 0.0, 9.5));
        }
    }

    protected Color getArrowColor(Component c) {
        if (c instanceof JMenu && ((JMenu)c).isSelected() && !this.isUnderlineSelection()) {
            return this.selectionForeground;
        }
        return c == null || c.isEnabled() ? this.arrowColor : this.disabledArrowColor;
    }

    protected boolean isUnderlineSelection() {
        return "underline".equals(UIManager.getString("MenuItem.selectionType"));
    }
}

