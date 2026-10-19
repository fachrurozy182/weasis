/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.ui.FlatUIUtils
 */
package org.weasis.core.ui.tp.raven.datetime.component.date;

import com.formdev.flatlaf.ui.FlatUIUtils;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.weasis.core.ui.tp.raven.datetime.DatePicker;
import org.weasis.core.ui.tp.raven.datetime.component.date.DateSelectionModel;
import org.weasis.core.ui.tp.raven.datetime.component.date.DefaultDateCellRenderer;
import org.weasis.core.ui.tp.raven.datetime.component.date.PanelDate;
import org.weasis.core.ui.tp.raven.datetime.component.date.SingleDate;

public class ButtonDate
extends JButton {
    private final DatePicker datePicker;
    private final SingleDate date;
    private final int rowIndex;
    private boolean press;
    private boolean hover;

    public ButtonDate(DatePicker datePicker, SingleDate date, boolean enable, int rowIndex) {
        this.datePicker = datePicker;
        this.date = date;
        this.rowIndex = rowIndex;
        this.setText("" + date.getDay());
        this.init(enable);
    }

    private void init(boolean enable) {
        this.setContentAreaFilled(false);
        this.addActionListener(e -> {
            if (this.datePicker.isEnabled()) {
                this.datePicker.getDateSelectionModel().selectDate(this.date);
                this.hover = false;
                PanelDate panelDate = (PanelDate)this.getParent();
                panelDate.checkSelection();
            }
        });
        this.addMouseListener(new MouseAdapter(this){
            final /* synthetic */ ButtonDate this$0;
            {
                ButtonDate buttonDate = this$0;
                Objects.requireNonNull(buttonDate);
                this.this$0 = buttonDate;
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (this.this$0.datePicker.isEnabled() && this.this$0.isEnabled() && this.this$0.datePicker.getDateSelectionMode() == DatePicker.DateSelectionMode.SINGLE_DATE_SELECTED && e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    this.this$0.datePicker.closePopup();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (this.this$0.datePicker.isEnabled() && SwingUtilities.isLeftMouseButton(e)) {
                    this.this$0.press = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (this.this$0.datePicker.isEnabled() && SwingUtilities.isLeftMouseButton(e)) {
                    this.this$0.press = false;
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (this.this$0.datePicker.isEnabled()) {
                    this.this$0.hover = true;
                    if (this.this$0.datePicker.getDateSelectionModel().getDateSelectionMode() == DatePicker.DateSelectionMode.BETWEEN_DATE_SELECTED && this.this$0.datePicker.getDateSelectionModel().getDate() != null) {
                        this.this$0.datePicker.getDateSelectionModel().setHoverDate(this.this$0.date);
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (this.this$0.datePicker.isEnabled()) {
                    this.this$0.hover = false;
                }
            }
        });
        if (enable) {
            this.putClientProperty("FlatLaf.style", "margin:7,7,7,7;focusWidth:2;selectedForeground:contrast($Component.accentColor,$Button.background,#fff)");
        } else {
            this.putClientProperty("FlatLaf.style", "margin:7,7,7,7;focusWidth:2;selectedForeground:contrast($Component.accentColor,$Button.background,#fff);foreground:$Button.disabledText");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void paintComponent(Graphics g) {
        DefaultDateCellRenderer dateCellRenderer = this.datePicker.getDefaultDateCellRenderer();
        if (dateCellRenderer != null) {
            Graphics2D g2 = (Graphics2D)g.create();
            try {
                FlatUIUtils.setRenderingHints((Graphics)g2);
                dateCellRenderer.paint(g2, this.datePicker, this, this.date, this.getWidth(), this.getHeight());
            }
            finally {
                g2.dispose();
            }
        }
        super.paintComponent(g);
    }

    public boolean isDateSelected() {
        DateSelectionModel dateSelectionModel = this.datePicker.getDateSelectionModel();
        if (dateSelectionModel.getDateSelectionMode() == DatePicker.DateSelectionMode.SINGLE_DATE_SELECTED) {
            return this.date.same(dateSelectionModel.getDate());
        }
        return this.date.same(dateSelectionModel.getDate()) || this.date.same(dateSelectionModel.getToDate());
    }

    public SingleDate getDate() {
        return this.date;
    }

    public boolean isPress() {
        return this.press;
    }

    public boolean isHover() {
        return this.hover;
    }

    public int getRowIndex() {
        return this.rowIndex;
    }
}

