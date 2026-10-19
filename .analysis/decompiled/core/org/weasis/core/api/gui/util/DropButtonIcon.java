/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.ui.FlatUIUtils
 */
package org.weasis.core.api.gui.util;

import com.formdev.flatlaf.ui.FlatUIUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.UIManager;
import org.weasis.core.api.gui.util.DropDownButton;
import org.weasis.core.api.gui.util.GuiUtils;

public class DropButtonIcon
implements Icon {
    private final Icon leftIcon;

    public DropButtonIcon(Icon leftIcon) {
        this.leftIcon = leftIcon;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D)g;
        this.leftIcon.paintIcon(c, g2d, x, y);
        if (c instanceof DropDownButton) {
            DropDownButton button = (DropDownButton)c;
            ButtonModel model = button.getModel();
            Color color = model.isRollover() ? FlatUIUtils.getUIColor((String)"ComboBox.buttonHoverArrowColor", (Color)Color.GRAY) : (model.isPressed() ? FlatUIUtils.getUIColor((String)"ComboBox.buttonPressedArrowColor", (Color)Color.LIGHT_GRAY) : (!model.isEnabled() ? FlatUIUtils.getUIColor((String)"ComboBox.buttonDisabledArrowColor", (Color)Color.DARK_GRAY) : FlatUIUtils.getUIColor((String)"ComboBox.buttonArrowColor", (Color)Color.DARK_GRAY)));
            g2d.setPaint(color);
        }
        int midSize = GuiUtils.getScaleLength(3);
        int shiftX = x + this.leftIcon.getIconWidth() + GuiUtils.getScaleLength(1);
        int shiftY = y + this.leftIcon.getIconHeight() - GuiUtils.getScaleLength(5);
        int[] xPoints = new int[]{shiftX, shiftX + 2 * midSize, shiftX + midSize};
        int[] yPoints = new int[]{shiftY, shiftY, shiftY + midSize};
        g2d.fillPolygon(xPoints, yPoints, xPoints.length);
    }

    @Override
    public int getIconWidth() {
        return this.leftIcon.getIconWidth() + GuiUtils.getScaleLength(7);
    }

    @Override
    public int getIconHeight() {
        return this.leftIcon.getIconHeight();
    }

    public static Icon createDropButtonIcon(final Icon mainIcon) {
        return new DropButtonIcon(new Icon(){

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                if (c instanceof AbstractButton) {
                    AbstractButton model = (AbstractButton)c;
                    Icon icon = null;
                    if (!model.isEnabled()) {
                        icon = UIManager.getLookAndFeel().getDisabledIcon(model, mainIcon);
                    }
                    if (icon == null) {
                        icon = mainIcon;
                    }
                    icon.paintIcon(c, g, x, y);
                }
            }

            @Override
            public int getIconWidth() {
                return mainIcon.getIconWidth();
            }

            @Override
            public int getIconHeight() {
                return mainIcon.getIconHeight();
            }
        });
    }
}

