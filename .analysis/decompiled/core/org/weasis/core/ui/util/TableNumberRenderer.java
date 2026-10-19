/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.util;

import java.text.NumberFormat;
import javax.swing.table.DefaultTableCellRenderer;

public class TableNumberRenderer
extends DefaultTableCellRenderer {
    private final NumberFormat formatter = NumberFormat.getNumberInstance();

    public TableNumberRenderer() {
        this(2);
    }

    public TableNumberRenderer(int maxDecimal) {
        this.formatter.setMaximumFractionDigits(maxDecimal);
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Number) {
            this.setHorizontalAlignment(4);
            this.setText(this.formatter.format(value));
        } else {
            this.setHorizontalAlignment(2);
            this.setText(value == null ? "" : value.toString());
        }
    }
}

