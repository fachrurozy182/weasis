/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.tp.raven.datetime.component.date;

import javax.swing.event.EventListenerList;
import org.weasis.core.ui.tp.raven.datetime.DatePicker;
import org.weasis.core.ui.tp.raven.datetime.DateSelectionAble;
import org.weasis.core.ui.tp.raven.datetime.component.date.SingleDate;
import org.weasis.core.ui.tp.raven.datetime.component.date.event.DateSelectionModelEvent;
import org.weasis.core.ui.tp.raven.datetime.component.date.event.DateSelectionModelListener;

public class DateSelectionModel {
    protected EventListenerList listenerList = new EventListenerList();
    private DatePicker.DateSelectionMode dateSelectionMode;
    private DateSelectionAble dateSelectionAble;
    private SingleDate date;
    private SingleDate toDate;
    private SingleDate hoverDate;

    public DateSelectionModel() {
        this(DatePicker.DateSelectionMode.SINGLE_DATE_SELECTED);
    }

    public DateSelectionModel(DatePicker.DateSelectionMode dateSelectionMode) {
        this(dateSelectionMode, null);
    }

    public DateSelectionModel(DateSelectionAble dateSelectionAble) {
        this(DatePicker.DateSelectionMode.SINGLE_DATE_SELECTED, dateSelectionAble);
    }

    public DateSelectionModel(DatePicker.DateSelectionMode dateSelectionMode, DateSelectionAble dateSelectionAble) {
        this.dateSelectionMode = dateSelectionMode;
        this.dateSelectionAble = dateSelectionAble;
    }

    public SingleDate getDate() {
        return this.date;
    }

    public void setDate(SingleDate date) {
        if (this.equalsDate(this.date, date)) {
            return;
        }
        if (!this.checkSelection(date)) {
            return;
        }
        this.date = date;
        this.fireDatePickerChanged(new DateSelectionModelEvent(this, 2));
    }

    public SingleDate getToDate() {
        return this.toDate;
    }

    public void setToDate(SingleDate toDate) {
        if (this.equalsDate(this.toDate, toDate)) {
            return;
        }
        if (!this.checkSelection(toDate)) {
            return;
        }
        this.toDate = toDate;
    }

    public SingleDate getHoverDate() {
        return this.hoverDate;
    }

    public void setHoverDate(SingleDate hoverDate) {
        if (this.equalsDate(this.hoverDate, hoverDate)) {
            return;
        }
        this.hoverDate = hoverDate;
        this.fireDatePickerChanged(new DateSelectionModelEvent(this, 1));
    }

    public void setSelectDate(SingleDate from, SingleDate to) {
        if (this.equalsDate(this.date, from) && this.equalsDate(this.toDate, to)) {
            return;
        }
        if (!this.checkSelection(from) || !this.checkSelection(to)) {
            return;
        }
        this.date = from;
        this.toDate = to;
        this.fireDatePickerChanged(new DateSelectionModelEvent(this, 2));
    }

    protected void selectDate(SingleDate date) {
        if (!this.checkSelection(date)) {
            return;
        }
        if (this.dateSelectionMode == DatePicker.DateSelectionMode.SINGLE_DATE_SELECTED) {
            this.setDate(date);
        } else if (this.getDate() == null || this.getToDate() != null) {
            this.date = date;
            this.hoverDate = date;
            if (this.getToDate() != null) {
                this.toDate = null;
            }
            this.fireDatePickerChanged(new DateSelectionModelEvent(this, 2));
        } else {
            this.toDate = date;
            this.invertIfNecessaryToDate(date);
            this.fireDatePickerChanged(new DateSelectionModelEvent(this, 2));
        }
    }

    public void setDateSelectionAble(DateSelectionAble dateSelectionAble) {
        this.dateSelectionAble = dateSelectionAble;
    }

    public DateSelectionAble getDateSelectionAble() {
        return this.dateSelectionAble;
    }

    public void addDatePickerSelectionListener(DateSelectionModelListener listener) {
        this.listenerList.add(DateSelectionModelListener.class, listener);
    }

    public void removeDatePickerSelectionListener(DateSelectionModelListener listener) {
        this.listenerList.remove(DateSelectionModelListener.class, listener);
    }

    public void fireDatePickerChanged(DateSelectionModelEvent event) {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != DateSelectionModelListener.class) continue;
            ((DateSelectionModelListener)listeners[i + 1]).dateSelectionModelChanged(event);
        }
    }

    public boolean equalsDate(SingleDate date1, SingleDate date2) {
        if (date1 == null && date2 == null) {
            return true;
        }
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.same(date2);
    }

    public DatePicker.DateSelectionMode getDateSelectionMode() {
        return this.dateSelectionMode;
    }

    public void setDateSelectionMode(DatePicker.DateSelectionMode dateSelectionMode) {
        this.dateSelectionMode = dateSelectionMode;
    }

    private boolean checkSelection(SingleDate date) {
        if (this.dateSelectionAble != null) {
            return date == null || this.dateSelectionAble.isDateSelectedAble(date.toLocalDate());
        }
        return true;
    }

    private void invertIfNecessaryToDate(SingleDate date) {
        if (this.date.toLocalDate().isAfter(date.toLocalDate())) {
            this.toDate = this.date;
            this.date = date;
        }
    }
}

