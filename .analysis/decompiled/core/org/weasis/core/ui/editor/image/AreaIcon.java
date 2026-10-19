/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.editor.image;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.Icon;

record AreaIcon(double x, double y, double width, double height) implements Icon
{
    private static final int SIZE = 16;
    private static final int FILL_ALPHA = 150;
    private static final int MIN_FILL = 2;

    @Override
    public void paintIcon(Component c, Graphics g, int px, int py) {
        Graphics2D g2d = (Graphics2D)g.create();
        Color color = c.getForeground();
        int inner = 15;
        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
        g2d.fillRect(px + (int)Math.round(this.x * (double)inner), py + (int)Math.round(this.y * (double)inner), Math.max(2, (int)Math.round(this.width * (double)inner)), Math.max(2, (int)Math.round(this.height * (double)inner)));
        g2d.setColor(color);
        g2d.drawRect(px, py, inner, inner);
        g2d.dispose();
    }

    @Override
    public int getIconWidth() {
        return 16;
    }

    @Override
    public int getIconHeight() {
        return 16;
    }
}

