/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.gui.util.GuiUtils$IconColor
 *  org.weasis.core.api.gui.util.WinUtil
 *  org.weasis.core.api.util.ResourceUtil
 *  org.weasis.core.api.util.ResourceUtil$ActionIcon
 *  org.weasis.core.api.util.ResourceUtil$ResourceIconPath
 *  org.weasis.core.ui.util.LabelHighlighted
 *  org.weasis.core.ui.util.TableColumnAdjuster
 */
package org.weasis.dicom.explorer.tag;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.WinUtil;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.util.LabelHighlighted;
import org.weasis.core.ui.util.TableColumnAdjuster;
import org.weasis.dicom.explorer.tag.AbstractTagSearchPanel;

public class TagSearchTablePanel
extends AbstractTagSearchPanel {
    private final TreeSet<Integer> searchPositions = new TreeSet();
    private int currentSearchIndex = -1;
    private final TableRowSorter<? extends TableModel> rowSorter;
    private final JToggleButton filterButton = new JToggleButton((Icon)ResourceUtil.getIcon((ResourceUtil.ResourceIconPath)ResourceUtil.ActionIcon.FILTER));
    private final JTable table;
    private final JPanel tableContainer;

    public TagSearchTablePanel(JTable jtable, JPanel tableContainer) {
        TableRowSorter<Object> tableRowSorter;
        this.table = Objects.requireNonNull(jtable);
        this.tableContainer = Objects.requireNonNull(tableContainer);
        RowSorter<? extends TableModel> sorter = this.table.getRowSorter();
        if (sorter == null) {
            this.table.setAutoCreateRowSorter(true);
            sorter = this.table.getRowSorter();
        }
        this.rowSorter = sorter instanceof TableRowSorter ? (tableRowSorter = (TableRowSorter<Object>)sorter) : new TableRowSorter<TableModel>(this.table.getModel());
        this.table.setRowSorter(this.rowSorter);
        RendererHighlighted renderer = new RendererHighlighted(this.textFieldSearch, this.searchPositions);
        this.table.setDefaultRenderer(Object.class, renderer);
        this.filterButton.setSelected(true);
        this.filterButton.addActionListener(evt -> this.filter());
        this.navigateToolbar.add(this.filterButton);
    }

    @Override
    protected void previous() {
        if (!this.searchPositions.isEmpty()) {
            int index = Math.max(this.currentSearchIndex - 1, 0);
            Integer val = this.searchPositions.floor(index);
            if (val == null) {
                val = this.searchPositions.first();
            }
            if (val != null) {
                this.currentSearchIndex = val;
                this.scrollRow(this.currentSearchIndex);
            }
        }
    }

    @Override
    protected void next() {
        if (!this.searchPositions.isEmpty()) {
            int index = Math.min(this.currentSearchIndex + 1, this.table.getModel().getRowCount() - 1);
            Integer val = this.searchPositions.ceiling(index);
            if (val == null) {
                val = this.searchPositions.last();
            }
            if (val != null) {
                this.currentSearchIndex = val;
                this.scrollRow(this.currentSearchIndex);
            }
        }
    }

    public void scrollRow(int row) {
        JViewport viewport = (JViewport)WinUtil.getParentOfClass((Component)this.table, JViewport.class);
        if (viewport != null) {
            int viewRow = this.table.convertRowIndexToView(row);
            Rectangle r = this.table.getCellRect(viewRow, 0, true);
            int extentHeight = viewport.getExtentSize().height;
            int viewHeight = viewport.getViewSize().height;
            int y = Math.max(0, r.y - (extentHeight - r.height) / 2);
            y = Math.min(y, viewHeight - extentHeight);
            viewport.setViewPosition(new Point(0, y));
            this.table.setRowSelectionInterval(viewRow, viewRow);
        }
    }

    @Override
    protected void filter() {
        Integer position;
        this.searchPositions.clear();
        this.currentSearchIndex = -1;
        String text = this.textFieldSearch.getText();
        if (text.trim().length() == 0) {
            this.rowSorter.setRowFilter(null);
        } else {
            this.rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, new int[0]));
            for (int row = 0; row < this.table.getRowCount(); ++row) {
                this.searchPositions.add(this.table.convertRowIndexToModel(row));
            }
            if (!this.filterButton.isSelected()) {
                this.rowSorter.setRowFilter(null);
            }
        }
        TableColumnAdjuster.adjustPreferredSizeForViewPort((JTable)this.table, (JPanel)this.tableContainer);
        this.table.revalidate();
        this.table.repaint();
        if (!this.searchPositions.isEmpty() && (position = this.searchPositions.first()) != null) {
            this.scrollRow(position);
        }
    }

    public static class RendererHighlighted
    extends DefaultTableCellRenderer {
        private final JTextField searchField;
        private final Set<Integer> searchPositions;
        private final LabelHighlighted label;

        public RendererHighlighted(JTextField searchField, Set<Integer> searchPositions) {
            this.label = new LabelHighlighted(GuiUtils.IconColor.ACTIONS_BLUE.color);
            this.searchField = Objects.requireNonNull(searchField);
            this.searchPositions = Objects.requireNonNull(searchPositions);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, selected, hasFocus, row, column);
            JLabel original = (JLabel)c;
            this.label.setFont(original.getFont());
            this.label.setText(original.getText());
            this.label.setBackground(original.getBackground());
            this.label.setForeground(original.getForeground());
            this.label.setHorizontalTextPosition(original.getHorizontalTextPosition());
            this.label.highlightText(this.searchField.getText());
            return this.label;
        }
    }
}

