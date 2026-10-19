/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatEmptyBorder;
import com.formdev.flatlaf.ui.FlatLabelUI;
import com.formdev.flatlaf.ui.FlatPanelUI;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.LabelUI;

public class FlatLineBorder
extends FlatEmptyBorder {
    private final Color lineColor;
    private final float lineThickness;
    private final int arc;

    public FlatLineBorder(Insets insets, Color lineColor) {
        this(insets, lineColor, 1.0f, -1);
    }

    public FlatLineBorder(Insets insets, Color lineColor, float lineThickness, int arc) {
        super(insets);
        this.lineColor = lineColor;
        this.lineThickness = lineThickness;
        this.arc = arc;
    }

    public FlatLineBorder(Insets insets, int arc) {
        this(insets, null, 0.0f, arc);
    }

    public Color getLineColor() {
        return this.lineColor;
    }

    public float getLineThickness() {
        return this.lineThickness;
    }

    public int getArc() {
        return this.arc;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        if (c instanceof JComponent && ((JComponent)c).getClientProperty("FlatLaf.internal.FlatPopupFactory.popupUsesNativeBorder") != null) {
            return;
        }
        Color lineColor = this.getLineColor();
        float lineThickness = this.getLineThickness();
        if (lineColor == null || lineThickness <= 0.0f) {
            return;
        }
        int arc = this.getArc();
        if (arc < 0) {
            LabelUI ui;
            ComponentUI componentUI = c instanceof JLabel ? ((JLabel)c).getUI() : (ui = c instanceof JPanel ? ((JPanel)c).getUI() : null);
            if (ui instanceof FlatLabelUI) {
                arc = ((FlatLabelUI)ui).arc;
            } else if (ui instanceof FlatPanelUI) {
                arc = ((FlatPanelUI)((Object)ui)).arc;
            }
            if (arc < 0) {
                arc = 0;
            }
        }
        Graphics2D g2 = (Graphics2D)g.create();
        try {
            FlatUIUtils.setRenderingHints(g2);
            FlatUIUtils.paintOutlinedComponent(g2, x, y, width, height, 0.0f, 0.0f, 0.0f, UIScale.scale(lineThickness), UIScale.scale(arc), null, lineColor, null);
        }
        finally {
            g2.dispose();
        }
    }
}

