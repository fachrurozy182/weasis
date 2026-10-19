/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.editor.image;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;

public final class FrameOfReferenceColor {
    private static final Color[] PALETTE = new Color[]{new Color(15975168), new Color(26533), new Color(8869522), new Color(10603249), new Color(16164352), new Color(15110060), new Color(34902), new Color(8924439), new Color(9287168), new Color(0x848482), new Color(16356217), new Color(11748460), new Color(6636834), new Color(0x222222), new Color(2833702), new Color(15959040), new Color(6311575), new Color(14471936), new Color(14833698), new Color(12759680)};

    private FrameOfReferenceColor() {
    }

    public static Color colorFor(String frameOfReferenceUID, Iterable<String> distinctUidsInOrder) {
        if (frameOfReferenceUID == null || frameOfReferenceUID.isBlank()) {
            return null;
        }
        int slot = 0;
        String last = null;
        for (String uid : distinctUidsInOrder) {
            if (uid == null || uid.isBlank() || uid.equals(last)) continue;
            if (frameOfReferenceUID.equals(uid)) {
                return PALETTE[slot % PALETTE.length];
            }
            last = uid;
            ++slot;
        }
        return null;
    }

    public static Icon swatch(Color color, int size) {
        if (color == null) {
            return null;
        }
        return new SwatchIcon(color, size);
    }

    private record SwatchIcon(Color color, int size) implements Icon
    {
        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D)g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(this.color);
                g2.fillRect(x, y, this.size, this.size);
                g2.setColor(this.color.darker());
                g2.drawRect(x, y, this.size - 1, this.size - 1);
            }
            finally {
                g2.dispose();
            }
        }

        @Override
        public int getIconWidth() {
            return this.size;
        }

        @Override
        public int getIconHeight() {
            return this.size;
        }
    }
}

