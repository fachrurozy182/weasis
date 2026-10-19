/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.miginfocom.swing.MigLayout
 *  org.osgi.service.prefs.Preferences
 */
package org.weasis.core.ui.pref;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.text.html.HTMLEditorKit;
import net.miginfocom.swing.MigLayout;
import org.osgi.service.prefs.Preferences;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.ShortcutManager;
import org.weasis.core.api.service.BundlePreferences;
import org.weasis.core.api.util.FontItem;

public class ShortcutPrefView
extends AbstractItemDialogPage {
    private static final String TITLE = Messages.getString("shortcuts");
    private static final int COL_CATEGORY = 0;
    private static final int COL_ACTION = 1;
    private static final int COL_SHORTCUT = 2;
    private static final int COL_DEFAULT = 3;
    private final ShortcutTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<ShortcutTableModel> sorter;
    private final JTextField searchField;

    public ShortcutPrefView() {
        super(TITLE, 105);
        this.getProperties().put("show.restore", Boolean.TRUE.toString());
        this.getProperties().put("show.apply", Boolean.TRUE.toString());
        this.getProperties().setProperty("help.item", "../basics/shortcuts");
        this.setLayout(new BorderLayout(5, 5));
        this.searchField = new JTextField(20);
        this.searchField.setToolTipText(Messages.getString("search.shortcut.filter"));
        JPanel searchPanel = GuiUtils.getFlowLayoutPanel(3, 5, 2, new JLabel(Messages.getString("filter") + ":"), this.searchField);
        this.add((Component)searchPanel, "North");
        this.tableModel = new ShortcutTableModel();
        this.table = new JTable(this.tableModel);
        this.table.setSelectionMode(0);
        this.table.setRowHeight(24);
        this.table.getColumnModel().getColumn(0).setPreferredWidth(100);
        this.table.getColumnModel().getColumn(1).setPreferredWidth(160);
        this.table.getColumnModel().getColumn(2).setPreferredWidth(120);
        this.table.getColumnModel().getColumn(3).setPreferredWidth(120);
        this.table.setDefaultRenderer(Object.class, new ShortcutCellRenderer());
        this.sorter = new TableRowSorter<ShortcutTableModel>(this.tableModel);
        this.table.setRowSorter(this.sorter);
        this.table.addMouseListener(new MouseAdapter(this){
            final /* synthetic */ ShortcutPrefView this$0;
            {
                ShortcutPrefView shortcutPrefView = this$0;
                Objects.requireNonNull(shortcutPrefView);
                this.this$0 = shortcutPrefView;
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == 1) {
                    this.this$0.editSelectedShortcut();
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(this.table);
        scrollPane.setPreferredSize(new Dimension(550, 300));
        this.add((Component)scrollPane, "Center");
        JButton editButton = new JButton(Messages.getString("edit.shortcut"));
        editButton.addActionListener(actionEvent -> this.editSelectedShortcut());
        JButton clearButton = new JButton(Messages.getString("clear.shortcut"));
        clearButton.addActionListener(actionEvent -> this.clearSelectedShortcut());
        JPanel buttonPanel = GuiUtils.getFlowLayoutPanel(4, 5, 2, editButton, clearButton);
        this.add((Component)buttonPanel, "South");
        this.searchField.getDocument().addDocumentListener(new DocumentListener(this){
            final /* synthetic */ ShortcutPrefView this$0;
            {
                ShortcutPrefView shortcutPrefView = this$0;
                Objects.requireNonNull(shortcutPrefView);
                this.this$0 = shortcutPrefView;
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                this.this$0.applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                this.this$0.applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                this.this$0.applyFilter();
            }
        });
    }

    private void applyFilter() {
        String text = this.searchField.getText().trim();
        if (text.isEmpty()) {
            this.sorter.setRowFilter(null);
        } else {
            this.sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, new int[0]));
        }
    }

    private void editSelectedShortcut() {
        int viewRow = this.table.getSelectedRow();
        if (viewRow < 0) {
            return;
        }
        int modelRow = this.table.convertRowIndexToModel(viewRow);
        ShortcutManager.ShortcutEntry entry = this.tableModel.getEntry(modelRow);
        KeyCaptureDialog dialog = new KeyCaptureDialog(this, entry);
        if (dialog.isConfirmed()) {
            int newKeyCode = dialog.getCapturedKeyCode();
            int newModifier = dialog.getCapturedModifier();
            List<ShortcutManager.ShortcutEntry> conflicts = ShortcutManager.getInstance().findConflicts(entry.getId(), newKeyCode, newModifier);
            if (!conflicts.isEmpty()) {
                StringBuilder msg = new StringBuilder();
                msg.append(Messages.getString("shortcut.conflict.message")).append("\n\n");
                for (ShortcutManager.ShortcutEntry conflict : conflicts) {
                    msg.append("\u2022 ").append(conflict.getDescription());
                    msg.append(" (").append(conflict.getCategory()).append(" \u2014 ").append(conflict.getContext().getDisplayName()).append(")\n");
                }
                msg.append("\n").append(Messages.getString("shortcut.conflict.continue"));
                int result = JOptionPane.showConfirmDialog(this, msg.toString(), Messages.getString("shortcut.conflict.title"), 0, 2);
                if (result != 0) {
                    return;
                }
            }
            ShortcutManager.getInstance().setShortcut(entry.getId(), newKeyCode, newModifier);
            this.tableModel.fireTableRowsUpdated(modelRow, modelRow);
        }
    }

    private void clearSelectedShortcut() {
        int viewRow = this.table.getSelectedRow();
        if (viewRow < 0) {
            return;
        }
        int modelRow = this.table.convertRowIndexToModel(viewRow);
        ShortcutManager.ShortcutEntry entry = this.tableModel.getEntry(modelRow);
        ShortcutManager.getInstance().setShortcut(entry.getId(), 0, 0);
        this.tableModel.fireTableRowsUpdated(modelRow, modelRow);
    }

    @Override
    public void closeAdditionalWindow() {
        ShortcutManager manager = ShortcutManager.getInstance();
        manager.applyToFeatures();
        Preferences prefs = BundlePreferences.getDefaultPreferences(AppProperties.getBundleContext(this.getClass()));
        manager.savePreferences(prefs);
    }

    @Override
    public void resetToDefaultValues() {
        ShortcutManager.getInstance().resetAllToDefaults();
        this.tableModel.refresh();
    }

    private static class ShortcutTableModel
    extends AbstractTableModel {
        private static final int COLUMN_COUNT = 4;
        private List<ShortcutManager.ShortcutEntry> data;

        ShortcutTableModel() {
            this.refresh();
        }

        void refresh() {
            this.data = ShortcutManager.getInstance().getShortcutList();
            this.fireTableDataChanged();
        }

        ShortcutManager.ShortcutEntry getEntry(int row) {
            return this.data.get(row);
        }

        @Override
        public int getRowCount() {
            return this.data.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public String getColumnName(int column) {
            return switch (column) {
                case 0 -> Messages.getString("shortcut.column.category");
                case 1 -> Messages.getString("shortcut.column.action");
                case 2 -> Messages.getString("shortcut.column.shortcut");
                case 3 -> Messages.getString("shortcut.column.default");
                default -> "";
            };
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ShortcutManager.ShortcutEntry entry = this.data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> entry.getCategory();
                case 1 -> entry.getDescription();
                case 2 -> entry.getShortcutText();
                case 3 -> entry.getDefaultShortcutText();
                default -> "";
            };
        }
    }

    private static class ShortcutCellRenderer
    extends DefaultTableCellRenderer {
        private ShortcutCellRenderer() {
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                List<ShortcutManager.ShortcutEntry> conflicts;
                int modelRow = table.convertRowIndexToModel(row);
                ShortcutTableModel model = (ShortcutTableModel)table.getModel();
                ShortcutManager.ShortcutEntry entry = model.getEntry(modelRow);
                if (entry.isModified()) {
                    c.setForeground(GuiUtils.IconColor.ACTIONS_BLUE.getColor());
                } else {
                    c.setForeground(table.getForeground());
                }
                if (column == 2 && entry.getKeyCode() != 0 && !(conflicts = ShortcutManager.getInstance().findConflicts(entry.getId(), entry.getKeyCode(), entry.getModifier())).isEmpty()) {
                    c.setForeground(GuiUtils.IconColor.ACTIONS_RED.getColor());
                }
            }
            return c;
        }
    }

    private class KeyCaptureDialog {
        private final boolean confirmed;
        private int capturedKeyCode;
        private int capturedModifier;
        protected JTextField captureField;

        KeyCaptureDialog(ShortcutPrefView shortcutPrefView, ShortcutManager.ShortcutEntry entry) {
            Objects.requireNonNull(shortcutPrefView);
            this.captureField = new JTextField(20);
            this.captureField.setEditable(false);
            this.captureField.setText(Messages.getString("press.shortcut.key"));
            this.captureField.setHorizontalAlignment(0);
            this.captureField.setFont(FontItem.H3.getFont());
            this.captureField.addHierarchyListener(e -> {
                if ((e.getChangeFlags() & 4L) != 0L && this.captureField.isShowing()) {
                    SwingUtilities.invokeLater(this.captureField::requestFocusInWindow);
                }
            });
            this.captureField.addKeyListener(new KeyListener(this){
                final /* synthetic */ KeyCaptureDialog this$1;
                {
                    KeyCaptureDialog keyCaptureDialog = this$1;
                    Objects.requireNonNull(keyCaptureDialog);
                    this.this$1 = keyCaptureDialog;
                }

                @Override
                public void keyTyped(KeyEvent e) {
                    e.consume();
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    int kc = e.getKeyCode();
                    if (kc == 16 || kc == 17 || kc == 18 || kc == 157) {
                        return;
                    }
                    this.this$1.capturedKeyCode = kc;
                    this.this$1.capturedModifier = e.getModifiers();
                    KeyStroke ks = KeyStroke.getKeyStroke(this.this$1.capturedKeyCode, this.this$1.capturedModifier);
                    this.this$1.captureField.setText(ShortcutManager.formatKeyStroke(ks));
                    e.consume();
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    e.consume();
                }
            });
            JEditorPane descPane = new JEditorPane();
            descPane.setEditorKit(new HTMLEditorKit());
            descPane.putClientProperty("JEditorPane.honorDisplayProperties", Boolean.TRUE);
            descPane.setFont(UIManager.getFont("Label.font"));
            descPane.setText("<html>" + Messages.getString("edit.shortcut.for") + ":<br><b>" + entry.getDescription() + "</b></html>");
            descPane.setEditable(false);
            descPane.setOpaque(false);
            descPane.setBorder(null);
            JScrollPane descScroll = new JScrollPane(descPane);
            descScroll.setBorder(null);
            descScroll.getViewport().setOpaque(false);
            descScroll.setHorizontalScrollBarPolicy(31);
            descScroll.setVerticalScrollBarPolicy(20);
            JPanel panel = new JPanel((LayoutManager)new MigLayout("insets 12 10 6 10, wrap 1, fillx", "[fill, 380]"));
            panel.add((Component)descScroll, "gapbottom 8, h ::100");
            panel.add((Component)this.captureField, "h 46!");
            ActionListener al = actionEvent -> this.captureField.requestFocusInWindow();
            Timer timer = new Timer(250, al);
            timer.setRepeats(false);
            timer.start();
            int result = JOptionPane.showConfirmDialog(shortcutPrefView, panel, Messages.getString("edit.shortcut"), 2, -1);
            this.confirmed = result == 0 && this.capturedKeyCode != 0;
        }

        boolean isConfirmed() {
            return this.confirmed;
        }

        int getCapturedKeyCode() {
            return this.capturedKeyCode;
        }

        int getCapturedModifier() {
            return this.capturedModifier;
        }
    }
}

