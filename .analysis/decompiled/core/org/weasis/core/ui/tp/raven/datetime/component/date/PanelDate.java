/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.miginfocom.swing.MigLayout
 */
package org.weasis.core.ui.tp.raven.datetime.component.date;

import java.awt.Component;
import java.awt.LayoutManager;
import java.util.Calendar;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.weasis.core.ui.tp.raven.datetime.DatePicker;
import org.weasis.core.ui.tp.raven.datetime.component.date.ButtonDate;
import org.weasis.core.ui.tp.raven.datetime.component.date.SingleDate;

public class PanelDate
extends JPanel {
    private final DatePicker datePicker;
    private final int month;
    private final int year;

    public PanelDate(DatePicker datePicker, int month, int year) {
        this.datePicker = datePicker;
        this.month = month;
        this.year = year;
        this.init();
    }

    private void init() {
        this.putClientProperty("FlatLaf.style", "background:null");
        this.setLayout((LayoutManager)new MigLayout("novisualpadding,wrap 7,insets 3,gap 0,al center center", "fill", "[fill]10[fill][fill]"));
        this.load();
    }

    public void load() {
        this.removeAll();
        this.createDateHeader();
        int col = 7;
        int row = 6;
        int t = 42;
        Calendar calendar = Calendar.getInstance();
        calendar.set(1, this.year);
        calendar.set(2, this.month);
        calendar.set(5, 1);
        int dayOfWeek = calendar.get(7);
        if (this.datePicker.isStartWeekOnMonday() && dayOfWeek == 1) {
            dayOfWeek = 8;
        }
        int startDay = dayOfWeek - (this.datePicker.isStartWeekOnMonday() ? 2 : 1);
        calendar.add(5, -startDay);
        int rowIndex = 0;
        for (int i = 1; i <= 42; ++i) {
            SingleDate singleDate = new SingleDate(calendar);
            boolean selectable = this.datePicker.getDateSelectionModel().getDateSelectionAble() == null || this.datePicker.getDateSelectionModel().getDateSelectionAble().isDateSelectedAble(singleDate.toLocalDate());
            boolean enable = calendar.get(2) == this.month && calendar.get(1) == this.year;
            JButton button = this.createButton(new SingleDate(calendar), enable, rowIndex);
            if (!selectable) {
                button.setEnabled(false);
            }
            this.add(button);
            calendar.add(5, 1);
            if (rowIndex == 6) {
                rowIndex = 0;
                continue;
            }
            ++rowIndex;
        }
        this.checkSelection();
    }

    protected void createDateHeader() {
        String[] weekdays = DatePicker.getDefaultWeekdays();
        if (this.datePicker.isStartWeekOnMonday()) {
            String sunday = weekdays[1];
            for (int i = 2; i < weekdays.length; ++i) {
                weekdays[i - 1] = weekdays[i];
            }
            weekdays[weekdays.length - 1] = sunday;
        }
        for (String week : weekdays) {
            if (week.isEmpty()) continue;
            this.add(this.createLabel(week));
        }
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text, 0);
        label.putClientProperty("FlatLaf.style", "[light]foreground:lighten($Label.foreground,30%);[dark]foreground:darken($Label.foreground,30%)");
        return label;
    }

    protected JButton createButton(SingleDate date, boolean enable, int rowIndex) {
        ButtonDate button = new ButtonDate(this.datePicker, date, enable, rowIndex);
        if (button.isDateSelected()) {
            button.setSelected(true);
        }
        return button;
    }

    public void checkSelection() {
        for (int i = 0; i < this.getComponentCount(); ++i) {
            Component com = this.getComponent(i);
            if (!(com instanceof ButtonDate)) continue;
            ButtonDate buttonDate = (ButtonDate)com;
            if (this.datePicker.getDateSelectionModel().getDateSelectionMode() == DatePicker.DateSelectionMode.SINGLE_DATE_SELECTED) {
                buttonDate.setSelected(buttonDate.getDate().same(this.datePicker.getDateSelectionModel().getDate()));
                continue;
            }
            buttonDate.setSelected(buttonDate.getDate().same(this.datePicker.getDateSelectionModel().getDate()) || buttonDate.getDate().same(this.datePicker.getDateSelectionModel().getToDate()));
        }
    }
}

