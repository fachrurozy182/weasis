/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.tp.raven.datetime;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class PanelDateOptionLabel {
    private final List<Item> listItems = new ArrayList<Item>();

    public void add(String label, LabelCallback callback) {
        this.listItems.add(new Item(label, callback));
    }

    public List<Item> getListItems() {
        return this.listItems;
    }

    private void remove(int index) {
        this.listItems.remove(index);
    }

    private void clear() {
        this.listItems.clear();
    }

    public static class Item {
        private final String label;
        private final LabelCallback callback;

        public String getLabel() {
            return this.label;
        }

        public LabelCallback getCallback() {
            return this.callback;
        }

        public Item(String label, LabelCallback callback) {
            this.label = label;
            this.callback = callback;
        }
    }

    public static interface LabelCallback {
        public static final LabelCallback TODAY = () -> new LocalDate[]{LocalDate.now(ZoneId.systemDefault())};
        public static final LabelCallback YESTERDAY = () -> new LocalDate[]{LocalDate.now(ZoneId.systemDefault()).minusDays(1L)};
        public static final LabelCallback LAST_7_DAYS = () -> new LocalDate[]{LocalDate.now(ZoneId.systemDefault()).minusDays(7L), LocalDate.now(ZoneId.systemDefault())};
        public static final LabelCallback LAST_30_DAYS = () -> new LocalDate[]{LocalDate.now(ZoneId.systemDefault()).minusDays(30L), LocalDate.now(ZoneId.systemDefault())};
        public static final LabelCallback THIS_MONTH = () -> {
            LocalDate now = LocalDate.now(ZoneId.systemDefault());
            return new LocalDate[]{now.withDayOfMonth(1), now.withDayOfMonth(now.lengthOfMonth())};
        };
        public static final LabelCallback LAST_MONTH = () -> {
            LocalDate lastMonth = LocalDate.now(ZoneId.systemDefault()).minusMonths(1L);
            return new LocalDate[]{lastMonth.withDayOfMonth(1), lastMonth.withDayOfMonth(lastMonth.lengthOfMonth())};
        };
        public static final LabelCallback LAST_YEAR = () -> {
            LocalDate lastYear = LocalDate.now(ZoneId.systemDefault()).minusYears(1L).withDayOfYear(1);
            return new LocalDate[]{lastYear, lastYear.withDayOfYear(lastYear.lengthOfYear())};
        };
        public static final LabelCallback CUSTOM = null;

        public LocalDate[] getDate();
    }
}

