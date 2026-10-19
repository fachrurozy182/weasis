/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.util;

import java.util.Objects;
import javax.swing.table.AbstractTableModel;

public class SimpleTableModel
extends AbstractTableModel {
    private final String[] columnNames;
    private final Object[][] data;
    private final boolean editable;

    public SimpleTableModel(String[] columnNames, Object[][] data) {
        this(columnNames, data, false);
    }

    public SimpleTableModel(String[] columnNames, Object[][] data, boolean editable) {
        this.columnNames = Objects.requireNonNull(columnNames);
        this.data = Objects.requireNonNull(data);
        this.editable = editable;
    }

    @Override
    public int getColumnCount() {
        return this.columnNames.length;
    }

    @Override
    public int getRowCount() {
        return this.data.length;
    }

    @Override
    public String getColumnName(int col) {
        return this.columnNames[col];
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return this.editable;
    }

    @Override
    public Object getValueAt(int row, int col) {
        return this.data[row][col];
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        this.data[row][col] = value;
        this.fireTableCellUpdated(row, col);
    }

    @Override
    public Class<?> getColumnClass(int c) {
        return this.getValueAt(0, c).getClass();
    }
}

