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

public class PanelYear
extends JPanel {
    public static final int YEAR_CELL = 28;
    private final DatePicker datePicker;
    private final int year;
    private int selectedYear = -1;

    public PanelYear(DatePicker datePicker, int year) {
        this.datePicker = datePicker;
        this.year = year;
        this.init();
    }

    private void init() {
        this.putClientProperty("FlatLaf.style", "background:null");
        this.setLayout((LayoutManager)new MigLayout("novisualpadding,wrap 4,insets 0,fillx,gap 0,al center center", "fill,sg main", "fill"));
        int count = 28;
        for (int i = 0; i < 28; ++i) {
            int y = this.getStartYear(this.year) + i;
            ButtonMonthYear button = new ButtonMonthYear(this.datePicker, y);
            button.setText("" + y);
            if (this.checkSelected(y)) {
                button.setSelected(true);
            }
            button.addActionListener(e -> {
                this.selectedYear = y;
                this.fireYearChanged(new ChangeEvent(this));
            });
            this.add(button);
        }
        this.checkSelection();
    }

    private int getStartYear(int year) {
        int initYear = 1900;
        int yearsPerPage = 28;
        int yearsPassed = year - initYear;
        int pages = yearsPassed / yearsPerPage;
        return initYear + pages * yearsPerPage;
    }

    protected boolean checkSelected(int year) {
        DateSelectionModel dateSelectionModel = this.datePicker.getDateSelectionModel();
        if (dateSelectionModel.getDateSelectionMode() == DatePicker.DateSelectionMode.SINGLE_DATE_SELECTED) {
            return dateSelectionModel.getDate() != null && year == dateSelectionModel.getDate().getYear();
        }
        return dateSelectionModel.getDate() != null && year == dateSelectionModel.getDate().getYear() || dateSelectionModel.getToDate() != null && year == dateSelectionModel.getToDate().getYear();
    }

    public void checkSelection() {
        for (int i = 0; i < this.getComponentCount(); ++i) {
            Component com = this.getComponent(i);
            if (!(com instanceof ButtonMonthYear)) continue;
            ButtonMonthYear button = (ButtonMonthYear)com;
            button.setSelected(this.checkSelected(button.getValue()));
        }
    }

    public int getYear() {
        return this.year;
    }

    public void addChangeListener(ChangeListener listener) {
        this.listenerList.add(ChangeListener.class, listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        this.listenerList.remove(ChangeListener.class, listener);
    }

    public void fireYearChanged(ChangeEvent event) {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ChangeListener.class) continue;
            ((ChangeListener)listeners[i + 1]).stateChanged(event);
        }
    }

    public int getSelectedYear() {
        return this.selectedYear;
    }
}

