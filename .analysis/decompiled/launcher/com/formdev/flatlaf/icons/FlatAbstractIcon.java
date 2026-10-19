/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.Icon;
import javax.swing.plaf.UIResource;

public abstract class FlatAbstractIcon
implements Icon,
UIResource {
    protected final int width;
    protected final int height;
    protected Color color;
    private float scale = 1.0f;

    public FlatAbstractIcon(int width, int height, Color color) {
        this.width = width;
        this.height = height;
        this.color = color;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D)g.create();
        try {
            FlatUIUtils.setRenderingHints(g2);
            this.paintBackground(c, g2, x, y);
            g2.translate(x, y);
            UIScale.scaleGraphics(g2);
            float scale = this.getScale();
            if (scale != 1.0f) {
                g2.scale(scale, scale);
            }
            if (this.color != null) {
                g2.setColor(this.color);
            }
            this.paintIcon(c, g2);
        }
        finally {
            g2.dispose();
        }
    }

    protected void paintBackground(Component c, Graphics2D g, int x, int y) {
    }

    protected abstract void paintIcon(Component var1, Graphics2D var2);

    @Override
    public int getIconWidth() {
        return this.scale(UIScale.scale(this.width));
    }

    @Override
    public int getIconHeight() {
        return this.scale(UIScale.scale(this.height));
    }

    public float getScale() {
        return this.scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    protected int scale(int size) {
        float scale = this.getScale();
        return scale == 1.0f ? size : Math.round((float)size * scale);
    }
}

