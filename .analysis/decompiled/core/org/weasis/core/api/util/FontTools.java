/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.api.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import org.weasis.core.util.StringUtil;

public class FontTools {
    private FontTools() {
    }

    public static void paintColorFontOutline(Graphics2D g2, String str, float x, float y, Color color) {
        if (StringUtil.hasText((CharSequence)str)) {
            g2.setPaint(Color.BLACK);
            if (RenderingHints.VALUE_TEXT_ANTIALIAS_ON.equals(g2.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING))) {
                TextLayout layout = new TextLayout(str, g2.getFont(), g2.getFontRenderContext());
                AffineTransform textAt = new AffineTransform();
                textAt.translate(x, y);
                Shape outline = layout.getOutline(textAt);
                g2.setStroke(new BasicStroke(2.0f, 0, 1));
                g2.draw(outline);
                g2.setPaint(color);
                g2.setStroke(new BasicStroke(1.0f, 0, 1));
                g2.fill(outline);
            } else {
                g2.drawString(str, x - 1.0f, y - 1.0f);
                g2.drawString(str, x - 1.0f, y);
                g2.drawString(str, x - 1.0f, y + 1.0f);
                g2.drawString(str, x, y - 1.0f);
                g2.drawString(str, x, y + 1.0f);
                g2.drawString(str, x + 1.0f, y - 1.0f);
                g2.drawString(str, x + 1.0f, y);
                g2.drawString(str, x + 1.0f, y + 1.0f);
                g2.setPaint(color);
                g2.drawString(str, x, y);
            }
        }
    }

    public static void paintFontOutline(Graphics2D g2, String str, float x, float y) {
        FontTools.paintColorFontOutline(g2, str, x, y, Color.WHITE);
    }
}

