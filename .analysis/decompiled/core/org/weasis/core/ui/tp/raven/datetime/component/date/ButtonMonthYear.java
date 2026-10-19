/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.ui.FlatUIUtils
 *  com.formdev.flatlaf.util.UIScale
 */
package org.weasis.core.ui.tp.raven.datetime.component.date;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.weasis.core.ui.tp.raven.datetime.DatePicker;
import org.weasis.core.ui.tp.raven.datetime.util.Utils;

public class ButtonMonthYear
extends JButton {
    private final DatePicker datePicker;
    private final int value;
    private boolean press;
    private boolean hover;

    public ButtonMonthYear(DatePicker datePicker, int value) {
        this.datePicker = datePicker;
        this.value = value;
        this.init();
    }

    private void init() {
        this.setContentAreaFilled(false);
        this.addMouseListener(new MouseAdapter(this){
            final /* synthetic */ ButtonMonthYear this$0;
            {
                ButtonMonthYear buttonMonthYear = this$0;
                Objects.requireNonNull(buttonMonthYear);
                this.this$0 = buttonMonthYear;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    this.this$0.press = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    this.this$0.press = false;
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                this.this$0.hover = true;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                this.this$0.hover = false;
            }
        });
        this.putClientProperty("FlatLaf.style", "margin:6,6,6,6;selectedForeground:contrast($Component.accentColor,$Button.background,#fff);");
    }

    public int getValue() {
        return this.value;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g.create();
        FlatUIUtils.setRenderingHints((Graphics)g2);
        int border = UIScale.scale((int)6);
        float arc = UIScale.scale((float)this.datePicker.getSelectionArc());
        int width = this.getWidth() - border;
        int height = this.getHeight() - border;
        int x = (this.getWidth() - width) / 2;
        int y = (this.getHeight() - height) / 2;
        g2.setColor(this.getColor());
        FlatUIUtils.paintComponentBackground((Graphics2D)g2, (int)x, (int)y, (int)width, (int)height, (float)0.0f, (float)arc);
        g2.dispose();
        super.paintComponent(g);
    }

    protected Color getColor() {
        Color color = this.isSelected() ? this.getAccentColor() : FlatUIUtils.getParentBackground((JComponent)this);
        return Utils.getColor(color, this.press, this.hover);
    }

    protected Color getAccentColor() {
        if (this.datePicker.getColor() != null) {
            return this.datePicker.getColor();
        }
        return UIManager.getColor("Component.accentColor");
    }
}

