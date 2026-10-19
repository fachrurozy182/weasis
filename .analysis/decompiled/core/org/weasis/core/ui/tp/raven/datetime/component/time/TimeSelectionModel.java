/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.tp.raven.datetime.component.time;

import java.time.LocalTime;
import javax.swing.event.EventListenerList;
import org.weasis.core.ui.tp.raven.datetime.TimeSelectionAble;
import org.weasis.core.ui.tp.raven.datetime.component.time.event.TimeSelectionModelEvent;
import org.weasis.core.ui.tp.raven.datetime.component.time.event.TimeSelectionModelListener;

public class TimeSelectionModel {
    protected EventListenerList listenerList = new EventListenerList();
    private TimeSelectionAble timeSelectionAble;
    private int hour = -1;
    private int minute = -1;

    public TimeSelectionAble getTimeSelectionAble() {
        return this.timeSelectionAble;
    }

    public void setTimeSelectionAble(TimeSelectionAble timeSelectionAble) {
        this.timeSelectionAble = timeSelectionAble;
    }

    public int getHour() {
        return this.hour;
    }

    public void setHour(int hour) {
        if (!this.checkSelection(hour, this.minute)) {
            return;
        }
        if (this.hour != hour) {
            this.hour = hour;
            this.fireTimePickerChanged(new TimeSelectionModelEvent(this, 1));
        }
    }

    public int getMinute() {
        return this.minute;
    }

    public void setMinute(int minute) {
        if (this.hour == -1) {
            this.set(this.getDefaultSelectionHour(minute), minute);
        } else if (this.minute != minute) {
            if (!this.checkSelection(this.hour, minute)) {
                return;
            }
            this.minute = minute;
            this.fireTimePickerChanged(new TimeSelectionModelEvent(this, 2));
        }
    }

    public boolean isSelected() {
        return this.hour != -1 && this.minute != -1;
    }

    public LocalTime getTime() {
        if (this.isSelected()) {
            return LocalTime.of(this.hour, this.minute);
        }
        return null;
    }

    public void set(int hour, int minute) {
        int action = 0;
        if (this.hour != hour && this.minute != minute) {
            action = 3;
        } else if (this.hour != hour) {
            action = 1;
        } else if (this.minute != minute) {
            action = 2;
        }
        if (action != 0) {
            if (!this.checkSelection(hour, minute)) {
                return;
            }
            this.hour = hour;
            this.minute = minute;
            this.fireTimePickerChanged(new TimeSelectionModelEvent(this, action));
        }
    }

    public int getDefaultSelectionHour(int minute) {
        int defaultHour = 0;
        if (this.timeSelectionAble != null) {
            defaultHour = this.getAvailableDefaultHour(defaultHour, minute);
        }
        return defaultHour;
    }

    private int getAvailableDefaultHour(int startHour, int minute) {
        int hour = startHour;
        for (int i = 0; i < 23; ++i) {
            if (this.checkSelection(hour, minute)) {
                return hour;
            }
            if (++hour != 24) continue;
            hour = 0;
        }
        return startHour;
    }

    private int getDefaultMinuteCheck() {
        return 0;
    }

    public boolean checkSelection(int hour, int minute) {
        if (this.timeSelectionAble == null || hour == -1 || minute == -1 && hour == -1) {
            return true;
        }
        boolean hourView = false;
        if (minute == -1) {
            minute = this.getDefaultMinuteCheck();
            hourView = true;
        }
        return this.timeSelectionAble.isTimeSelectedAble(LocalTime.of(hour, minute), hourView);
    }

    public void addTimePickerSelectionListener(TimeSelectionModelListener listener) {
        this.listenerList.add(TimeSelectionModelListener.class, listener);
    }

    public void removeTimePickerSelectionListener(TimeSelectionModelListener listener) {
        this.listenerList.remove(TimeSelectionModelListener.class, listener);
    }

    public void fireTimePickerChanged(TimeSelectionModelEvent event) {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != TimeSelectionModelListener.class) continue;
            ((TimeSelectionModelListener)listeners[i + 1]).timeSelectionModelChanged(event);
        }
    }
}

