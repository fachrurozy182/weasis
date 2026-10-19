/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.icons.FlatCheckBoxIcon;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatTableHeaderUI;
import com.formdev.flatlaf.ui.FlatUIAction;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.ui.FlatViewportUI;
import com.formdev.flatlaf.ui.StackUtils;
import com.formdev.flatlaf.util.Graphics2DProxy;
import com.formdev.flatlaf.util.HiDPIUtils;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.SystemInfo;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.util.Map;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public class FlatTableUI
extends BasicTableUI
implements FlatStylingSupport.StyleableUI,
FlatViewportUI.ViewportPainter {
    protected boolean showHorizontalLines;
    protected boolean showVerticalLines;
    @FlatStylingSupport.Styleable
    protected boolean showTrailingVerticalLine;
    protected Dimension intercellSpacing;
    @FlatStylingSupport.Styleable
    protected Color selectionBackground;
    @FlatStylingSupport.Styleable
    protected Color selectionForeground;
    @FlatStylingSupport.Styleable
    protected Color selectionInactiveBackground;
    @FlatStylingSupport.Styleable
    protected Color selectionInactiveForeground;
    @FlatStylingSupport.Styleable
    protected Insets selectionInsets;
    @FlatStylingSupport.Styleable
    protected int selectionArc;
    @FlatStylingSupport.Styleable
    protected Insets cellMargins;
    @FlatStylingSupport.Styleable
    protected Color cellFocusColor;
    @FlatStylingSupport.Styleable
    protected Boolean showCellFocusIndicator;
    private boolean oldShowHorizontalLines;
    private boolean oldShowVerticalLines;
    private Dimension oldIntercellSpacing;
    private TableCellRenderer oldBooleanRenderer;
    private PropertyChangeListener propertyChangeListener;
    private ComponentListener outsideAlternateRowsListener;
    private ListSelectionListener rowSelectionListener;
    private TableColumnModelListener columnSelectionListener;
    private Map<String, Object> oldStyleValues;

    public static ComponentUI createUI(JComponent c) {
        return new FlatTableUI();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        this.installStyle();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        this.showHorizontalLines = UIManager.getBoolean("Table.showHorizontalLines");
        this.showVerticalLines = UIManager.getBoolean("Table.showVerticalLines");
        this.showTrailingVerticalLine = UIManager.getBoolean("Table.showTrailingVerticalLine");
        this.intercellSpacing = UIManager.getDimension("Table.intercellSpacing");
        this.selectionBackground = UIManager.getColor("Table.selectionBackground");
        this.selectionForeground = UIManager.getColor("Table.selectionForeground");
        this.selectionInactiveBackground = UIManager.getColor("Table.selectionInactiveBackground");
        this.selectionInactiveForeground = UIManager.getColor("Table.selectionInactiveForeground");
        this.selectionInsets = UIManager.getInsets("Table.selectionInsets");
        this.selectionArc = UIManager.getInt("Table.selectionArc");
        this.toggleSelectionColors();
        int rowHeight = FlatUIUtils.getUIInt("Table.rowHeight", 16);
        if (rowHeight > 0) {
            LookAndFeel.installProperty(this.table, "rowHeight", UIScale.scale(rowHeight));
        }
        if (!this.showHorizontalLines) {
            FlatUIUtils.FlatPropertyWatcher.runIfNotChanged(this.table, "showHorizontalLines", () -> {
                this.oldShowHorizontalLines = this.table.getShowHorizontalLines();
                this.table.setShowHorizontalLines(false);
            });
        }
        if (!this.showVerticalLines) {
            FlatUIUtils.FlatPropertyWatcher.runIfNotChanged(this.table, "showVerticalLines", () -> {
                this.oldShowVerticalLines = this.table.getShowVerticalLines();
                this.table.setShowVerticalLines(false);
            });
        }
        if (this.intercellSpacing != null) {
            FlatUIUtils.FlatPropertyWatcher.runIfNotChanged(this.table, "rowMargin", () -> {
                this.oldIntercellSpacing = this.table.getIntercellSpacing();
                this.table.setIntercellSpacing(this.intercellSpacing);
            });
        }
        this.oldBooleanRenderer = this.table.getDefaultRenderer(Boolean.class);
        if (this.oldBooleanRenderer instanceof UIResource) {
            this.table.setDefaultRenderer(Boolean.class, new FlatBooleanRenderer());
        } else {
            this.oldBooleanRenderer = null;
        }
    }

    @Override
    protected void uninstallDefaults() {
        super.uninstallDefaults();
        this.selectionBackground = null;
        this.selectionForeground = null;
        this.selectionInactiveBackground = null;
        this.selectionInactiveForeground = null;
        this.oldStyleValues = null;
        if (!this.showHorizontalLines && this.oldShowHorizontalLines && !this.table.getShowHorizontalLines()) {
            FlatUIUtils.FlatPropertyWatcher.runIfNotChanged(this.table, "showHorizontalLines", () -> this.table.setShowHorizontalLines(true));
        }
        if (!this.showVerticalLines && this.oldShowVerticalLines && !this.table.getShowVerticalLines()) {
            FlatUIUtils.FlatPropertyWatcher.runIfNotChanged(this.table, "showVerticalLines", () -> this.table.setShowVerticalLines(true));
        }
        if (this.intercellSpacing != null && this.table.getIntercellSpacing().equals(this.intercellSpacing)) {
            FlatUIUtils.FlatPropertyWatcher.runIfNotChanged(this.table, "rowMargin", () -> this.table.setIntercellSpacing(this.oldIntercellSpacing));
        }
        if (this.table.getDefaultRenderer(Boolean.class) instanceof FlatBooleanRenderer) {
            if (this.oldBooleanRenderer instanceof Component) {
                SwingUtilities.updateComponentTreeUI((Component)((Object)this.oldBooleanRenderer));
            }
            this.table.setDefaultRenderer(Boolean.class, this.oldBooleanRenderer);
        }
        this.oldBooleanRenderer = null;
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        this.propertyChangeListener = e -> {
            switch (e.getPropertyName()) {
                case "selectionModel": {
                    if (this.rowSelectionListener == null) break;
                    Object oldModel = e.getOldValue();
                    Object newModel = e.getNewValue();
                    if (oldModel != null) {
                        ((ListSelectionModel)oldModel).removeListSelectionListener(this.rowSelectionListener);
                    }
                    if (newModel == null) break;
                    ((ListSelectionModel)newModel).addListSelectionListener(this.rowSelectionListener);
                    break;
                }
                case "columnModel": {
                    if (this.columnSelectionListener == null) break;
                    Object oldModel = e.getOldValue();
                    Object newModel = e.getNewValue();
                    if (oldModel != null) {
                        ((TableColumnModel)oldModel).removeColumnModelListener(this.columnSelectionListener);
                    }
                    if (newModel == null) break;
                    ((TableColumnModel)newModel).addColumnModelListener(this.columnSelectionListener);
                    break;
                }
                case "JComponent.focusOwner": {
                    this.toggleSelectionColors();
                    break;
                }
                case "FlatLaf.style": 
                case "FlatLaf.styleClass": {
                    this.installStyle();
                    this.table.revalidate();
                    HiDPIUtils.repaint(this.table);
                }
            }
        };
        this.table.addPropertyChangeListener(this.propertyChangeListener);
        if (this.selectionArc > 0) {
            this.installRepaintRoundedSelectionListeners();
        }
    }

    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        this.table.removePropertyChangeListener(this.propertyChangeListener);
        this.propertyChangeListener = null;
        if (this.outsideAlternateRowsListener != null) {
            this.table.removeComponentListener(this.outsideAlternateRowsListener);
            this.outsideAlternateRowsListener = null;
        }
        if (this.rowSelectionListener != null) {
            this.table.getSelectionModel().removeListSelectionListener(this.rowSelectionListener);
            this.rowSelectionListener = null;
        }
        if (this.columnSelectionListener != null) {
            this.table.getColumnModel().removeColumnModelListener(this.columnSelectionListener);
            this.columnSelectionListener = null;
        }
    }

    @Override
    protected FocusListener createFocusListener() {
        return new BasicTableUI.FocusHandler(){

            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                FlatTableUI.this.toggleSelectionColors();
            }

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                EventQueue.invokeLater(() -> FlatTableUI.this.toggleSelectionColors());
            }
        };
    }

    @Override
    protected void installKeyboardActions() {
        ActionMap map;
        super.installKeyboardActions();
        if (UIManager.getBoolean("Table.editorSelectAllOnStartEditing") && (map = SwingUtilities.getUIActionMap(this.table)) != null) {
            StartEditingAction.install(map, "startEditing");
        }
    }

    protected void installStyle() {
        try {
            this.applyStyle(FlatStylingSupport.getResolvedStyle(this.table, "Table"));
        }
        catch (RuntimeException ex) {
            LoggingFacade.INSTANCE.logSevere(null, ex);
        }
    }

    protected void applyStyle(Object style) {
        Color oldSelectionBackground = this.selectionBackground;
        Color oldSelectionForeground = this.selectionForeground;
        Color oldSelectionInactiveBackground = this.selectionInactiveBackground;
        Color oldSelectionInactiveForeground = this.selectionInactiveForeground;
        this.oldStyleValues = FlatStylingSupport.parseAndApply(this.oldStyleValues, style, this::applyStyleProperty);
        if (this.selectionBackground != oldSelectionBackground) {
            Color selBg = this.table.getSelectionBackground();
            if (selBg == oldSelectionBackground) {
                this.table.setSelectionBackground(this.selectionBackground);
            } else if (selBg == oldSelectionInactiveBackground) {
                this.table.setSelectionBackground(this.selectionInactiveBackground);
            }
        }
        if (this.selectionForeground != oldSelectionForeground) {
            Color selFg = this.table.getSelectionForeground();
            if (selFg == oldSelectionForeground) {
                this.table.setSelectionForeground(this.selectionForeground);
            } else if (selFg == oldSelectionInactiveForeground) {
                this.table.setSelectionForeground(this.selectionInactiveForeground);
            }
        }
    }

    protected Object applyStyleProperty(String key, Object value) {
        if ("rowHeight".equals(key) && value instanceof Integer) {
            value = UIScale.scale((Integer)value);
        } else if ("selectionArc".equals(key) && value instanceof Integer && (Integer)value > 0) {
            this.installRepaintRoundedSelectionListeners();
        }
        return FlatStylingSupport.applyToAnnotatedObjectOrComponent(this, this.table, key, value);
    }

    @Override
    public Map<String, Class<?>> getStyleableInfos(JComponent c) {
        return FlatStylingSupport.getAnnotatedStyleableInfos(this);
    }

    @Override
    public Object getStyleableValue(JComponent c, String key) {
        return FlatStylingSupport.getAnnotatedStyleableValue(this, key);
    }

    private void toggleSelectionColors() {
        if (this.table == null) {
            return;
        }
        if (FlatUIUtils.isPermanentFocusOwner(this.table)) {
            if (this.table.getSelectionBackground() == this.selectionInactiveBackground) {
                this.table.setSelectionBackground(this.selectionBackground);
            }
            if (this.table.getSelectionForeground() == this.selectionInactiveForeground) {
                this.table.setSelectionForeground(this.selectionForeground);
            }
        } else {
            if (this.table.getSelectionBackground() == this.selectionBackground) {
                this.table.setSelectionBackground(this.selectionInactiveBackground);
            }
            if (this.table.getSelectionForeground() == this.selectionForeground) {
                this.table.setSelectionForeground(this.selectionInactiveForeground);
            }
        }
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        FlatTableHeaderUI.fixDraggedAndResizingColumns(this.table.getTableHeader());
        final boolean horizontalLines = this.table.getShowHorizontalLines();
        final boolean verticalLines = this.table.getShowVerticalLines();
        if (horizontalLines || verticalLines) {
            final boolean hideLastVerticalLine = this.hideLastVerticalLine();
            final int tableWidth = this.table.getWidth();
            JTableHeader header = this.table.getTableHeader();
            final boolean isDragging = header != null && header.getDraggedColumn() != null;
            double systemScaleFactor = UIScale.getSystemScaleFactor((Graphics2D)g);
            final double lineThickness = 1.0 / systemScaleFactor * (double)((int)systemScaleFactor);
            final double lineOffset = 1.0 - lineThickness + 0.05;
            g = new Graphics2DProxy((Graphics2D)g){

                @Override
                public void drawLine(int x1, int y1, int x2, int y2) {
                    if (hideLastVerticalLine && verticalLines && x1 == x2 && y1 == 0 && x1 == tableWidth - 1 && this.wasInvokedFromPaintGrid()) {
                        return;
                    }
                    if (isDragging && SystemInfo.isJava_9_orLater && (horizontalLines && y1 == y2 || verticalLines && x1 == x2) && this.wasInvokedFromMethod("paintDraggedArea")) {
                        if (y1 == y2) {
                            super.fill(new Rectangle2D.Double(x1, y1, x2 - x1 + 1, lineThickness));
                        } else if (x1 == x2) {
                            super.fill(new Rectangle2D.Double(x1, y1, lineThickness, y2 - y1 + 1));
                        }
                        return;
                    }
                    super.drawLine(x1, y1, x2, y2);
                }

                @Override
                public void fillRect(int x, int y, int width, int height) {
                    if (hideLastVerticalLine && verticalLines && width == 1 && y == 0 && x == tableWidth - 1 && this.wasInvokedFromPaintGrid()) {
                        return;
                    }
                    if (lineThickness != 1.0) {
                        if (horizontalLines && height == 1 && this.wasInvokedFromPaintGrid()) {
                            super.fill(new Rectangle2D.Double(x, (double)y + lineOffset, width, lineThickness));
                            return;
                        }
                        if (verticalLines && width == 1 && y == 0 && this.wasInvokedFromPaintGrid()) {
                            super.fill(new Rectangle2D.Double((double)x + lineOffset, y, lineThickness, height));
                            return;
                        }
                    }
                    super.fillRect(x, y, width, height);
                }

                private boolean wasInvokedFromPaintGrid() {
                    return this.wasInvokedFromMethod("paintGrid");
                }

                private boolean wasInvokedFromMethod(String methodName) {
                    return StackUtils.wasInvokedFrom(BasicTableUI.class.getName(), methodName, 8);
                }
            };
        }
        if (this.selectionArc > 0 || this.selectionInsets != null && !FlatUIUtils.isInsetsEmpty(this.selectionInsets)) {
            g = new RoundedSelectionGraphics(g, UIManager.getColor("Table.alternateRowColor"));
        }
        super.paint(g, c);
    }

    protected boolean hideLastVerticalLine() {
        Container viewportParent;
        if (this.showTrailingVerticalLine) {
            return false;
        }
        Container viewport = SwingUtilities.getUnwrappedParent(this.table);
        Container container = viewportParent = viewport != null ? viewport.getParent() : null;
        if (!(viewportParent instanceof JScrollPane)) {
            return false;
        }
        if (this.table.getX() + this.table.getWidth() < viewport.getWidth()) {
            return false;
        }
        JScrollPane scrollPane = (JScrollPane)viewportParent;
        JViewport rowHeader = scrollPane.getRowHeader();
        return scrollPane.getComponentOrientation().isLeftToRight() ? viewport != rowHeader : viewport == rowHeader || rowHeader == null;
    }

    @Override
    public void paintViewport(Graphics g, JComponent c, JViewport viewport) {
        Color alternateColor;
        boolean paintOutside;
        int viewportWidth = viewport.getWidth();
        int viewportHeight = viewport.getHeight();
        if (viewport.isOpaque()) {
            g.setColor(this.table.getBackground());
            g.fillRect(0, 0, viewportWidth, viewportHeight);
        }
        if ((paintOutside = UIManager.getBoolean("Table.paintOutsideAlternateRows")) && (alternateColor = UIManager.getColor("Table.alternateRowColor")) != null) {
            int rowCount = this.table.getRowCount();
            int tableHeight = this.table.getHeight();
            if (tableHeight < viewportHeight) {
                int tableWidth = this.table.getWidth();
                int rowHeight = this.table.getRowHeight();
                g.setColor(alternateColor);
                int x = viewport.getComponentOrientation().isLeftToRight() ? 0 : viewportWidth - tableWidth;
                int y = tableHeight;
                int row = rowCount;
                while (y < viewportHeight) {
                    if (row % 2 != 0) {
                        this.paintAlternateRowBackground(g, -1, -1, x, y, tableWidth, rowHeight);
                    }
                    y += rowHeight;
                    ++row;
                }
                if (this.outsideAlternateRowsListener == null && this.table.getAutoResizeMode() == 0) {
                    this.outsideAlternateRowsListener = new FlatOutsideAlternateRowsListener();
                    this.table.addComponentListener(this.outsideAlternateRowsListener);
                }
            }
        }
    }

    protected void paintAlternateRowBackground(Graphics g, int row, int column, int x, int y, int width, int height) {
        float arcBottomRight;
        Insets insets = this.selectionInsets != null ? (Insets)this.selectionInsets.clone() : null;
        float arcBottomLeft = arcBottomRight = UIScale.scale((float)this.selectionArc / 2.0f);
        float arcTopRight = arcBottomRight;
        float arcTopLeft = arcBottomRight;
        if (column >= 0) {
            if (column > 0) {
                if (insets != null) {
                    insets.left = 0;
                }
                if (this.table.getComponentOrientation().isLeftToRight()) {
                    arcBottomLeft = 0.0f;
                    arcTopLeft = 0.0f;
                } else {
                    arcBottomRight = 0.0f;
                    arcTopRight = 0.0f;
                }
            }
            if (column < this.table.getColumnCount() - 1) {
                if (insets != null) {
                    insets.right = 0;
                }
                if (this.table.getComponentOrientation().isLeftToRight()) {
                    arcBottomRight = 0.0f;
                    arcTopRight = 0.0f;
                } else {
                    arcBottomLeft = 0.0f;
                    arcTopLeft = 0.0f;
                }
            }
        }
        FlatUIUtils.paintSelection((Graphics2D)g, x, y, width, height, UIScale.scale(insets), arcTopLeft, arcTopRight, arcBottomLeft, arcBottomRight, 0);
    }

    protected void paintCellSelection(Graphics g, int row, int column, int x, int y, int width, int height) {
        float arcBottomRight;
        Insets insets;
        boolean bottomSelected;
        boolean rowSelAllowed = this.table.getRowSelectionAllowed();
        boolean colSelAllowed = this.table.getColumnSelectionAllowed();
        boolean rowSelOnly = rowSelAllowed && !colSelAllowed;
        boolean colSelOnly = colSelAllowed && !rowSelAllowed;
        boolean cellOnlySel = rowSelAllowed && colSelAllowed;
        boolean leftSelected = column > 0 && (rowSelOnly || this.table.isCellSelected(row, column - 1));
        boolean topSelected = row > 0 && (colSelOnly || this.table.isCellSelected(row - 1, column));
        boolean rightSelected = column < this.table.getColumnCount() - 1 && (rowSelOnly || this.table.isCellSelected(row, column + 1));
        boolean bl = bottomSelected = row < this.table.getRowCount() - 1 && (colSelOnly || this.table.isCellSelected(row + 1, column));
        if (!this.table.getComponentOrientation().isLeftToRight()) {
            boolean temp = leftSelected;
            leftSelected = rightSelected;
            rightSelected = temp;
        }
        Insets insets2 = insets = this.selectionInsets != null ? (Insets)this.selectionInsets.clone() : null;
        if (insets != null) {
            if (rowSelOnly && leftSelected) {
                insets.left = 0;
            }
            if (rowSelOnly && rightSelected) {
                insets.right = 0;
            }
            if (colSelOnly && topSelected) {
                insets.top = 0;
            }
            if (colSelOnly && bottomSelected) {
                insets.bottom = 0;
            }
        }
        float arcBottomLeft = arcBottomRight = UIScale.scale((float)this.selectionArc / 2.0f);
        float arcTopRight = arcBottomRight;
        float arcTopLeft = arcBottomRight;
        if (this.selectionArc > 0) {
            boolean hasColGap;
            boolean hasRowGap = !(!rowSelOnly && !cellOnlySel || insets == null || insets.top == 0 && insets.bottom == 0);
            boolean bl2 = hasColGap = !(!colSelOnly && !cellOnlySel || insets == null || insets.left == 0 && insets.right == 0);
            if (leftSelected && !hasColGap) {
                arcBottomLeft = 0.0f;
                arcTopLeft = 0.0f;
            }
            if (rightSelected && !hasColGap) {
                arcBottomRight = 0.0f;
                arcTopRight = 0.0f;
            }
            if (topSelected && !hasRowGap) {
                arcTopRight = 0.0f;
                arcTopLeft = 0.0f;
            }
            if (bottomSelected && !hasRowGap) {
                arcBottomRight = 0.0f;
                arcBottomLeft = 0.0f;
            }
        }
        FlatUIUtils.paintSelection((Graphics2D)g, x, y, width, height, UIScale.scale(insets), arcTopLeft, arcTopRight, arcBottomLeft, arcBottomRight, 0);
    }

    public static void paintCellSelection(JTable table, Graphics g, int row, int column, int x, int y, int width, int height) {
        if (!(table.getUI() instanceof FlatTableUI)) {
            return;
        }
        FlatTableUI ui = (FlatTableUI)table.getUI();
        ui.paintCellSelection(g, row, column, x, y, width, height);
    }

    private void installRepaintRoundedSelectionListeners() {
        if (this.rowSelectionListener == null) {
            this.rowSelectionListener = this::repaintRoundedRowSelection;
            this.table.getSelectionModel().addListSelectionListener(this.rowSelectionListener);
        }
        if (this.columnSelectionListener == null) {
            this.columnSelectionListener = new TableColumnModelListener(){

                @Override
                public void columnSelectionChanged(ListSelectionEvent e) {
                    FlatTableUI.this.repaintRoundedColumnSelection(e);
                }

                @Override
                public void columnRemoved(TableColumnModelEvent e) {
                }

                @Override
                public void columnMoved(TableColumnModelEvent e) {
                }

                @Override
                public void columnMarginChanged(ChangeEvent e) {
                }

                @Override
                public void columnAdded(TableColumnModelEvent e) {
                }
            };
            this.table.getColumnModel().addColumnModelListener(this.columnSelectionListener);
        }
    }

    private void repaintRoundedRowSelection(ListSelectionEvent e) {
        if (this.selectionArc <= 0 || !this.table.getRowSelectionAllowed()) {
            return;
        }
        int rowCount = this.table.getRowCount();
        int columnCount = this.table.getColumnCount();
        if (rowCount <= 0 || columnCount <= 0) {
            return;
        }
        int firstRow = Math.max(0, Math.min(e.getFirstIndex() - 1, rowCount - 1));
        int lastRow = Math.max(0, Math.min(e.getLastIndex() + 1, rowCount - 1));
        Rectangle firstRect = this.table.getCellRect(firstRow, 0, false);
        Rectangle lastRect = this.table.getCellRect(lastRow, columnCount - 1, false);
        this.table.repaint(firstRect.union(lastRect));
    }

    private void repaintRoundedColumnSelection(ListSelectionEvent e) {
        if (this.selectionArc <= 0 || !this.table.getColumnSelectionAllowed()) {
            return;
        }
        int rowCount = this.table.getRowCount();
        int columnCount = this.table.getColumnCount();
        if (rowCount <= 0 || columnCount <= 0) {
            return;
        }
        int firstRow = 0;
        int lastRow = rowCount - 1;
        if (this.table.getRowSelectionAllowed()) {
            firstRow = this.table.getSelectionModel().getMinSelectionIndex();
            lastRow = this.table.getSelectionModel().getMaxSelectionIndex();
        }
        int firstColumn = Math.max(0, Math.min(e.getFirstIndex() - 1, columnCount - 1));
        int lastColumn = Math.max(0, Math.min(e.getLastIndex() + 1, columnCount - 1));
        Rectangle firstRect = this.table.getCellRect(firstRow, firstColumn, false);
        Rectangle lastRect = this.table.getCellRect(lastRow, lastColumn, false);
        this.table.repaint(firstRect.union(lastRect));
    }

    private static class StartEditingAction
    extends FlatUIAction {
        static void install(ActionMap map, String key) {
            Action oldAction = map.get(key);
            if (oldAction == null || oldAction instanceof StartEditingAction) {
                return;
            }
            map.put(key, new StartEditingAction(oldAction));
        }

        private StartEditingAction(Action delegate) {
            super(delegate);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTable table = (JTable)e.getSource();
            Component oldEditorComp = table.getEditorComponent();
            this.delegate.actionPerformed(e);
            Component editorComp = table.getEditorComponent();
            if (oldEditorComp == null && editorComp instanceof JTextField) {
                ((JTextField)editorComp).selectAll();
            }
        }
    }

    private static class FlatBooleanRenderer
    extends DefaultTableCellRenderer
    implements UIResource {
        private boolean selected;

        FlatBooleanRenderer() {
            this.setHorizontalAlignment(0);
            FlatCheckBoxIcon icon = new FlatCheckBoxIcon(){

                @Override
                protected boolean isSelected(Component c) {
                    return selected;
                }
            };
            this.setIcon(icon);
            this.setDisabledIcon(icon);
        }

        @Override
        protected void setValue(Object value) {
            this.selected = value != null && (Boolean)value != false;
        }
    }

    private class FlatOutsideAlternateRowsListener
    extends ComponentAdapter {
        private FlatOutsideAlternateRowsListener() {
        }

        @Override
        public void componentHidden(ComponentEvent e) {
            Container viewport = SwingUtilities.getUnwrappedParent(FlatTableUI.this.table);
            if (viewport instanceof JViewport) {
                HiDPIUtils.repaint(viewport);
            }
        }

        @Override
        public void componentMoved(ComponentEvent e) {
            this.repaintAreaBelowTable();
        }

        @Override
        public void componentResized(ComponentEvent e) {
            this.repaintAreaBelowTable();
        }

        private void repaintAreaBelowTable() {
            Container viewport = SwingUtilities.getUnwrappedParent(FlatTableUI.this.table);
            if (viewport instanceof JViewport) {
                int viewportHeight = viewport.getHeight();
                int tableHeight = FlatTableUI.this.table.getHeight();
                if (tableHeight < viewportHeight) {
                    HiDPIUtils.repaint(viewport, 0, tableHeight, viewport.getWidth(), viewportHeight - tableHeight);
                }
            }
        }
    }

    private class RoundedSelectionGraphics
    extends Graphics2DProxy {
        private final Color alternateRowColor;
        private boolean inPaintSelection;

        RoundedSelectionGraphics(Graphics delegate, Color alternateRowColor) {
            super((Graphics2D)delegate);
            this.alternateRowColor = alternateRowColor;
        }

        @Override
        public Graphics create() {
            return new RoundedSelectionGraphics(super.create(), this.alternateRowColor);
        }

        @Override
        public Graphics create(int x, int y, int width, int height) {
            return new RoundedSelectionGraphics(super.create(x, y, width, height), this.alternateRowColor);
        }

        @Override
        public void fillRect(int x, int y, int width, int height) {
            if (this.fillCellSelection(x, y, width, height)) {
                return;
            }
            super.fillRect(x, y, width, height);
        }

        @Override
        public void fill(Shape shape) {
            if (shape instanceof Rectangle2D) {
                Rectangle2D r = (Rectangle2D)shape;
                double x = r.getX();
                double y = r.getY();
                double width = r.getWidth();
                double height = r.getHeight();
                if (x == (double)((int)x) && y == (double)((int)y) && width == (double)((int)width) && height == (double)((int)height) && this.fillCellSelection((int)x, (int)y, (int)width, (int)height)) {
                    return;
                }
            }
            super.fill(shape);
        }

        private boolean fillCellSelection(int x, int y, int width, int height) {
            Component rendererComponent;
            Color color;
            if (this.inPaintSelection) {
                return false;
            }
            if (x == 0 && y == 0 && ((color = this.getColor()) == FlatTableUI.this.table.getSelectionBackground() || this.alternateRowColor != null && color == this.alternateRowColor) && (rendererComponent = this.findActiveRendererComponent()) != null && width == rendererComponent.getWidth() && height == rendererComponent.getHeight()) {
                Point location = rendererComponent.getLocation();
                int row = FlatTableUI.this.table.rowAtPoint(location);
                int column = FlatTableUI.this.table.columnAtPoint(location);
                if (row >= 0 && column >= 0) {
                    this.inPaintSelection = true;
                    if (color == FlatTableUI.this.table.getSelectionBackground()) {
                        FlatTableUI.this.paintCellSelection(this, row, column, x, y, width, height);
                    } else {
                        FlatTableUI.this.paintAlternateRowBackground(this, row, column, x, y, width, height);
                    }
                    this.inPaintSelection = false;
                    return true;
                }
            }
            return false;
        }

        private Component findActiveRendererComponent() {
            int count = FlatTableUI.this.rendererPane.getComponentCount();
            for (int i = 0; i < count; ++i) {
                Component c = FlatTableUI.this.rendererPane.getComponent(i);
                if (c.getWidth() <= 0 || c.getHeight() <= 0) continue;
                return c;
            }
            return null;
        }
    }
}

