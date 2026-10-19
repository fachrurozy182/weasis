/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.weasis.core.api.gui.util.GuiUtils;

public class TableColumnAdjuster {
    private TableColumnAdjuster() {
    }

    private static int preferredWidth(JTable table, int col) {
        TableColumn tableColumn = table.getColumnModel().getColumn(col);
        int width = (int)table.getTableHeader().getDefaultRenderer().getTableCellRendererComponent(table, tableColumn.getIdentifier(), false, false, -1, col).getPreferredSize().getWidth();
        if (table.getRowCount() != 0) {
            Rectangle rect = table.getVisibleRect();
            int from = table.rowAtPoint(rect.getLocation());
            int to = table.rowAtPoint(new Point((int)rect.getMaxX(), (int)rect.getMaxY())) + 1;
            if (to == 0) {
                to = Math.max(to, table.getRowCount());
            }
            for (int row = from; row < to; ++row) {
                try {
                    int preferredWidth = (int)table.getCellRenderer(row, col).getTableCellRendererComponent(table, table.getValueAt(row, col), false, false, row, col).getPreferredSize().getWidth();
                    width = Math.max(width, preferredWidth);
                    continue;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        return width + table.getIntercellSpacing().width + GuiUtils.getScaleLength(7);
    }

    public static void pack(JTable table) {
        if (!table.isShowing() || table.getColumnCount() == 0) {
            return;
        }
        int[] width = new int[table.getColumnCount()];
        int total = 0;
        for (int col = 0; col < width.length; ++col) {
            width[col] = TableColumnAdjuster.preferredWidth(table, col);
            total += width[col];
        }
        int extra = table.getVisibleRect().width - total;
        if (extra > 0 && width.length > 0 && table.getColumnCount() > 0) {
            int bonus = extra / table.getColumnCount();
            int i = 0;
            while (i < width.length) {
                int n = i++;
                width[n] = width[n] + bonus;
            }
            int n = width.length - 1;
            width[n] = width[n] + (extra -= bonus * table.getColumnCount());
        }
        TableColumnModel columnModel = table.getColumnModel();
        for (int col = 0; col < width.length; ++col) {
            TableColumn tableColumn = columnModel.getColumn(col);
            table.getTableHeader().setResizingColumn(tableColumn);
            tableColumn.setWidth(width[col]);
        }
    }

    public static void adjustPreferredSizeForViewPort(JTable jtable, JPanel tableContainer) {
        TableColumnAdjuster.pack(jtable);
        int height = (jtable.getRowHeight() + jtable.getRowMargin()) * jtable.getRowCount() + jtable.getTableHeader().getHeight() + GuiUtils.insetHeight(tableContainer);
        int width = jtable.getColumnModel().getTotalColumnWidth();
        tableContainer.setPreferredSize(new Dimension(width, height));
        tableContainer.add((Component)jtable.getTableHeader(), "First");
        tableContainer.add((Component)jtable, "Center");
        TableColumnAdjuster.pack(jtable);
    }
}

