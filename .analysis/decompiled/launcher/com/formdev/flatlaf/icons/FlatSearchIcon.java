/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.icons.FlatAbstractIcon;
import com.formdev.flatlaf.ui.FlatButtonUI;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatUIUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import javax.swing.UIManager;

@FlatStylingSupport.StyleableField(cls=FlatAbstractIcon.class, key="searchIconScale", fieldName="scale")
public class FlatSearchIcon
extends FlatAbstractIcon
implements FlatStylingSupport.StyleableObject {
    @FlatStylingSupport.Styleable
    protected Color searchIconColor = UIManager.getColor("SearchField.searchIconColor");
    @FlatStylingSupport.Styleable
    protected Color searchIconHoverColor = UIManager.getColor("SearchField.searchIconHoverColor");
    @FlatStylingSupport.Styleable
    protected Color searchIconPressedColor = UIManager.getColor("SearchField.searchIconPressedColor");
    private final boolean ignoreButtonState;
    private Area area;

    public FlatSearchIcon() {
        this(false);
    }

    public FlatSearchIcon(boolean ignoreButtonState) {
        super(16, 16, null);
        this.ignoreButtonState = ignoreButtonState;
    }

    @Override
    protected void paintIcon(Component c, Graphics2D g) {
        g.setColor(this.ignoreButtonState ? this.searchIconColor : FlatButtonUI.buttonStateColor(c, this.searchIconColor, this.searchIconColor, null, this.searchIconHoverColor, this.searchIconPressedColor));
        if (this.area == null) {
            this.area = new Area(new Ellipse2D.Float(2.0f, 2.0f, 10.0f, 10.0f));
            this.area.subtract(new Area(new Ellipse2D.Float(3.0f, 3.0f, 8.0f, 8.0f)));
            this.area.add(new Area(FlatUIUtils.createPath(10.813, 9.75, 14.0, 12.938, 12.938, 14.0, 9.75, 10.813)));
        }
        g.fill(this.area);
    }
}

