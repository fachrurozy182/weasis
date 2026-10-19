/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.miginfocom.swing.MigLayout
 */
package org.weasis.core.ui.tp.raven.datetime.component.date;

import java.awt.Component;
import java.awt.LayoutManager;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.miginfocom.swing.MigLayout;
import org.weasis.core.ui.tp.raven.datetime.DatePicker;
import org.weasis.core.ui.tp.raven.datetime.component.date.ButtonMonthYear;
import org.weasis.core.ui.tp.raven.datetime.component.date.DateSelectionModel;

public class PanelMonth
extends JPanel {
    private final DatePicker datePicker;
    private final int year;
    private int selectedMonth = -1;

    public PanelMonth(DatePicker datePicker, int year) {
        this.datePicker = datePicker;
        this.year = year;
        this.init();
    }

    private void init() {
        this.putClientProperty("FlatLaf.style", "background:null");
        this.setLayout((LayoutManager)new MigLayout("novisualpadding,wrap 3,insets 0,fillx,gap 0,al center center", "fill,sg main", "fill"));
        int count = 12;
        for (int i = 0; i < 12; ++i) {
            int month = i;
            ButtonMonthYear button = new ButtonMonthYear(this.datePicker, i);
            button.setText(DatePicker.getDefaultMonths()[i]);
            if (this.checkSelected(month + 1)) {
                button.setSelected(true);
            }
            button.addActionListener(e -> {
                this.selectedMonth = month;
                this.fireMonthChanged(new ChangeEvent(this));
            });
            this.add(button);
        }
    }

    public boolean checkSelected(int month) {
        DateSelectionModel dateSelectionModel = this.datePicker.getDateSelectionModel();
        if (dateSelectionModel.getDateSelectionMode() == DatePicker.DateSelectionMode.SINGLE_DATE_SELECTED) {
            return dateSelectionModel.getDate() != null && this.year == dateSelectionModel.getDate().getYear() && month == dateSelectionModel.getDate().getMonth();
        }
        return dateSelectionModel.getDate() != null && this.year == dateSelectionModel.getDate().getYear() && month == dateSelectionModel.getDate().getMonth() || dateSelectionModel.getToDate() != null && this.year == dateSelectionModel.getToDate().getYear() && month == dateSelectionModel.getToDate().getMonth();
    }

    public void checkSelection() {
        for (int i = 0; i < this.getComponentCount(); ++i) {
            Component com = this.getComponent(i);
            if (!(com instanceof ButtonMonthYear)) continue;
            ButtonMonthYear button = (ButtonMonthYear)com;
            button.setSelected(this.checkSelected(button.getValue() + 1));
        }
    }

    public void addChangeListener(ChangeListener listener) {
        this.listenerList.add(ChangeListener.class, listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        this.listenerList.remove(ChangeListener.class, listener);
    }

    public void fireMonthChanged(ChangeEvent event) {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ChangeListener.class) continue;
            ((ChangeListener)listeners[i + 1]).stateChanged(event);
        }
    }

    public int getSelectedMonth() {
        return this.selectedMonth;
    }
}

