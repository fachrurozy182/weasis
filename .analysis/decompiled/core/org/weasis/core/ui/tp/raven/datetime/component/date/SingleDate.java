/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.tp.raven.datetime.component.date;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;

public class SingleDate {
    private int day;
    private int month;
    private int year;

    public SingleDate() {
        this(LocalDate.now(ZoneId.systemDefault()));
    }

    public SingleDate(LocalDate date) {
        this.day = date.getDayOfMonth();
        this.month = date.getMonthValue();
        this.year = date.getYear();
    }

    public SingleDate(Calendar calendar) {
        this(calendar.get(5), calendar.get(2) + 1, calendar.get(1));
    }

    public SingleDate(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    public int getDay() {
        return this.day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return this.month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return this.year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean same(SingleDate date) {
        return date != null && this.year == date.year && this.month == date.month && this.day == date.day;
    }

    public boolean after(SingleDate date) {
        if (date == null) {
            return false;
        }
        if (this.year > date.year) {
            return true;
        }
        if (this.year < date.year) {
            return false;
        }
        if (this.month > date.month) {
            return true;
        }
        if (this.month < date.month) {
            return false;
        }
        return this.day > date.day;
    }

    public boolean before(SingleDate date) {
        if (date == null) {
            return false;
        }
        if (this.year < date.year) {
            return true;
        }
        if (this.year > date.year) {
            return false;
        }
        if (this.month < date.month) {
            return true;
        }
        if (this.month > date.month) {
            return false;
        }
        return this.day < date.day;
    }

    public boolean between(SingleDate from, SingleDate to) {
        if (from.before(to)) {
            return this.after(from) && this.before(to);
        }
        return this.after(to) && this.before(from);
    }

    public LocalDate toLocalDate() {
        return LocalDate.of(this.year, this.month, this.day);
    }

    public String toString() {
        return this.day + "/" + this.month + "/" + this.year;
    }
}

