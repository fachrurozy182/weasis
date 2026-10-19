/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.editor.image;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;
import javax.swing.Icon;
import javax.swing.UIManager;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.editor.image.ShowPopup;

public class ViewButton
extends Rectangle2D.Double
implements ShowPopup {
    private final ShowPopup popup;
    private final Icon icon;
    private final String name;
    private boolean visible;
    private boolean enable;
    private boolean hover;
    private int position;

    public ViewButton(ShowPopup popup, Icon icon, String name) {
        this.popup = Objects.requireNonNull(popup);
        this.icon = Objects.requireNonNull(icon);
        this.name = Objects.requireNonNull(name);
        this.position = 13;
        this.setFrame(0.0, 0.0, GuiUtils.getScaleLength(icon.getIconWidth()), GuiUtils.getScaleLength(icon.getIconHeight()));
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isEnable() {
        return this.enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isHover() {
        return this.hover;
    }

    public void setHover(boolean hover) {
        this.hover = hover;
    }

    public Icon getIcon() {
        return this.icon;
    }

    public String getName() {
        return this.name;
    }

    public int getPosition() {
        return this.position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ViewButton that = (ViewButton)o;
        return this.popup.equals(that.popup) && this.name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.popup, this.name);
    }

    @Override
    public void showPopup(Component invoker, int x, int y) {
        this.popup.showPopup(invoker, x, y);
    }

    public static Icon getResizedIcon(ResourceUtil.ResourceIconPath path, int size) {
        return ResourceUtil.getIcon(path, size, size);
    }

    public static void drawButtonBackground(Graphics2D g2d, Component c, ViewButton b, Icon icon) {
        int x = (int)b.x - 3;
        int y = (int)b.y - 3;
        int width = icon.getIconWidth() + 7;
        int height = icon.getIconHeight() + 7;
        Color oldColor = g2d.getColor();
        Color bck = UIManager.getColor("Button.background");
        g2d.setColor(bck);
        g2d.fillRoundRect(x, y, width, height, 7, 7);
        if (b.isHover()) {
            Color borderColor = UIManager.getColor("Button.hoverBorderColor");
            Color highlight = borderColor.brighter().brighter();
            Color shadow = borderColor.darker().darker();
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.setColor(highlight);
            g2d.drawRoundRect(x, y, width, height, 7, 7);
            g2d.setColor(shadow);
            g2d.drawRoundRect(x, y, width, height, 7, 7);
        }
        icon.paintIcon(c, g2d, (int)b.x, (int)b.y);
        b.paintOverlay(g2d, (int)b.x, (int)b.y, icon.getIconWidth(), icon.getIconHeight());
        g2d.setColor(oldColor);
    }

    protected void paintOverlay(Graphics2D g2d, int x, int y, int width, int height) {
    }
}

