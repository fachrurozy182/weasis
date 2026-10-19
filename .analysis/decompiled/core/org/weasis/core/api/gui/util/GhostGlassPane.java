/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui.util;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.Icon;
import javax.swing.JComponent;

public class GhostGlassPane
extends JComponent {
    private static final AlphaComposite composite = AlphaComposite.getInstance(3, 0.5f);
    private transient Image dragged = null;
    private Point location = null;
    private transient Icon draggedIcon = null;

    public GhostGlassPane() {
        this.setBorder(null);
    }

    public void setImage(Image dragged) {
        this.dragged = dragged;
    }

    public void setIcon(Icon dragged) {
        this.draggedIcon = dragged;
    }

    public void setImagePosition(Point location) {
        int height;
        Point oldLocation = this.location == null ? location : this.location;
        this.location = location;
        int width = this.dragged == null ? this.draggedIcon.getIconWidth() + 1 : this.dragged.getWidth(this) + 1;
        int n = height = this.dragged == null ? this.draggedIcon.getIconHeight() + 1 : this.dragged.getHeight(this) + 1;
        if (location == null) {
            if (oldLocation == null) {
                this.repaint();
            } else {
                this.repaint(new Rectangle(oldLocation.x, oldLocation.y, width, height));
            }
        } else {
            Rectangle newClip = new Rectangle(location.x, location.y, width, height);
            newClip.add(new Rectangle(oldLocation.x, oldLocation.y, width, height));
            this.repaint(newClip);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        if (this.location == null || this.dragged == null && this.draggedIcon == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D)g;
        Composite oldComposite = g2.getComposite();
        g2.setComposite(composite);
        if (this.dragged == null) {
            this.draggedIcon.paintIcon(this, g2, this.location.x, this.location.y);
        } else {
            g2.drawImage(this.dragged, this.location.x, this.location.y, null);
        }
        g2.setComposite(oldComposite);
    }
}

