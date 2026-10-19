/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.util;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.JLabel;

public class LabelHighlighted
extends JLabel {
    private final List<Rectangle2D> rectangles = new ArrayList<Rectangle2D>();
    private final Color highlightColor;

    public LabelHighlighted(Color highlightColor) {
        this.highlightColor = Objects.requireNonNull(highlightColor);
    }

    public void reset() {
        this.rectangles.clear();
        this.repaint();
    }

    public void highlightText(String textToHighlight) {
        if (textToHighlight == null) {
            return;
        }
        this.reset();
        String textToMatch = textToHighlight.toLowerCase().trim();
        if (textToMatch.isEmpty()) {
            return;
        }
        String labelText = this.getText().toLowerCase();
        if (labelText.contains(textToMatch)) {
            String highlight = textToHighlight.trim();
            FontMetrics fm = this.getFontMetrics(this.getFont());
            float w = -1.0f;
            float h = (float)fm.getHeight() - 1.0f;
            int i = 0;
            while ((i = labelText.indexOf(textToMatch, i)) != -1) {
                if (w == -1.0f) {
                    String matchingText = this.getText().substring(i, i + highlight.length());
                    w = fm.stringWidth(matchingText);
                }
                String preText = this.getText().substring(0, i);
                float x = fm.stringWidth(preText);
                this.rectangles.add(new Rectangle2D.Float(x, 1.0f, w, h));
                i += textToMatch.length();
            }
            this.repaint();
        }
    }

    public List<Rectangle2D> getRectangles() {
        return this.rectangles;
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(this.getBackground());
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        if (!this.rectangles.isEmpty()) {
            Graphics2D g2d = (Graphics2D)g;
            Color c = g2d.getColor();
            for (Rectangle2D rectangle : this.rectangles) {
                g2d.setColor(this.highlightColor);
                g2d.fill(rectangle);
            }
            g2d.setColor(c);
        }
        super.paintComponent(g);
    }
}

