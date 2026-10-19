/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.ui.FlatUIUtils
 *  net.miginfocom.swing.MigLayout
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.api.gui.layout;

import com.formdev.flatlaf.ui.FlatUIUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import javax.swing.Icon;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.layout.ConstraintSpec;
import org.weasis.core.api.gui.layout.MigCell;
import org.weasis.core.api.gui.util.GUIEntry;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.RadioMenuItem;
import org.weasis.core.api.util.Copyable;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.util.StringUtil;

public final class MigLayoutModel
implements GUIEntry,
Copyable<MigLayoutModel> {
    public static final int DEFAULT_INTERCELL_GAP = 5;
    private static final Logger LOGGER = LoggerFactory.getLogger(MigLayoutModel.class);
    private static final int ICON_SIZE = 22;
    private final String id;
    private final String layoutConstraints;
    private final List<MigCell> cells;
    private String title;
    private Icon icon;
    private String columnConstraints;
    private String rowConstraints;

    public MigLayoutModel(String id, String title, int rows, int cols, String defaultClass) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.title = Objects.requireNonNull(title, "title cannot be null");
        this.layoutConstraints = "wrap " + cols + ", ins 0, gap 5";
        this.columnConstraints = this.buildDefaultColumnConstraints(cols);
        this.rowConstraints = this.buildDefaultRowConstraints(rows);
        this.cells = this.createUniformGrid(rows, cols, defaultClass);
        this.icon = this.buildIcon();
    }

    public MigLayoutModel(InputStream stream, String id, String title) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.cells = new ArrayList<MigCell>();
        Properties props = new Properties();
        try {
            props.load(stream);
        }
        catch (IOException e) {
            LOGGER.error("Failed to load layout properties", (Throwable)e);
            throw new IllegalArgumentException("Invalid layout properties", e);
        }
        this.title = props.getProperty("layout.name", title);
        this.layoutConstraints = props.getProperty("layout.constraints", "wrap, ins 0, gap 5");
        this.columnConstraints = props.getProperty("layout.columns", "[]");
        this.rowConstraints = props.getProperty("layout.rows", "[]");
        this.parseLayoutProperties(props);
        String iconPath = props.getProperty("layout.icon");
        this.icon = StringUtil.hasText((CharSequence)iconPath) ? ResourceUtil.getIcon(iconPath).derive(22, 22) : this.buildIcon();
    }

    public MigLayoutModel(String id, String title, String layoutConstraints, String columnConstraints, String rowConstraints, List<MigCell> cells) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.title = Objects.requireNonNull(title, "title cannot be null");
        this.layoutConstraints = Objects.requireNonNull(layoutConstraints);
        this.columnConstraints = columnConstraints;
        this.rowConstraints = rowConstraints;
        this.cells = new ArrayList<MigCell>(cells);
        this.icon = this.buildIcon();
    }

    public MigLayoutModel(MigLayoutModel layoutModel) {
        this.id = layoutModel.id;
        this.title = layoutModel.title;
        this.layoutConstraints = layoutModel.layoutConstraints;
        this.columnConstraints = layoutModel.columnConstraints;
        this.rowConstraints = layoutModel.rowConstraints;
        this.cells = new ArrayList<MigCell>(layoutModel.cells.stream().map(MigCell::copy).toList());
        this.icon = layoutModel.icon;
    }

    public MigLayoutModel(String id, String title, int rows, int cols, String defaultClass, Map<Integer, int[]> mergedCells) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.title = Objects.requireNonNull(title, "title cannot be null");
        this.layoutConstraints = "wrap " + cols + ", ins 0, gap 5";
        this.columnConstraints = this.buildDefaultColumnConstraints(cols);
        this.rowConstraints = this.buildDefaultRowConstraints(rows);
        this.validateMergedCells(mergedCells, rows, cols);
        this.cells = this.createGridWithMergedCells(rows, cols, defaultClass, mergedCells);
        this.icon = this.buildIcon();
    }

    private void validateMergedCells(Map<Integer, int[]> mergedCells, int rows, int cols) {
        if (mergedCells == null || mergedCells.isEmpty()) {
            return;
        }
        boolean[][] occupied = new boolean[rows][cols];
        for (Map.Entry<Integer, int[]> entry : mergedCells.entrySet()) {
            this.validateAndMarkMergedCell(entry.getKey(), entry.getValue(), rows, cols, occupied);
        }
    }

    private void validateAndMarkMergedCell(int position, int[] span, int rows, int cols, boolean[][] occupied) {
        if (span.length != 2 || span[0] < 1 || span[1] < 1) {
            throw new IllegalArgumentException("Invalid span dimensions: must be [spanX, spanY] with positive values");
        }
        int x = position % cols;
        int y = position / cols;
        int spanX = span[0];
        int spanY = span[1];
        if (x + spanX > cols || y + spanY > rows) {
            throw new IllegalArgumentException("Merged cell at position " + position + " exceeds grid boundaries");
        }
        this.markCellsAsOccupied(x, y, spanX, spanY, occupied, position);
    }

    private void markCellsAsOccupied(int x, int y, int spanX, int spanY, boolean[][] occupied, int position) {
        for (int dy = 0; dy < spanY; ++dy) {
            for (int dx = 0; dx < spanX; ++dx) {
                if (occupied[y + dy][x + dx]) {
                    throw new IllegalArgumentException("Overlapping merged cells detected at position " + position);
                }
                occupied[y + dy][x + dx] = true;
            }
        }
    }

    private List<MigCell> createGridWithMergedCells(int rows, int cols, String defaultClass, Map<Integer, int[]> mergedCells) {
        ArrayList<MigCell> cells = new ArrayList<MigCell>();
        boolean[][] occupied = this.markOccupiedCells(rows, cols, mergedCells);
        int cellIndex = 0;
        for (int y = 0; y < rows; ++y) {
            for (int x = 0; x < cols; ++x) {
                int position = y * cols + x;
                if (occupied[y][x] && mergedCells != null && mergedCells.containsKey(position)) {
                    cells.add(this.createMergedCell(cellIndex++, defaultClass, x, y, mergedCells.get(position)));
                    continue;
                }
                if (occupied[y][x]) continue;
                cells.add(this.createRegularCell(cellIndex++, defaultClass, x, y));
            }
        }
        return cells;
    }

    private boolean[][] markOccupiedCells(int rows, int cols, Map<Integer, int[]> mergedCells) {
        boolean[][] occupied = new boolean[rows][cols];
        if (mergedCells != null) {
            for (Map.Entry<Integer, int[]> entry : mergedCells.entrySet()) {
                int position = entry.getKey();
                int[] span = entry.getValue();
                int x = position % cols;
                int y = position / cols;
                for (int dy = 0; dy < span[1]; ++dy) {
                    for (int dx = 0; dx < span[0]; ++dx) {
                        occupied[y + dy][x + dx] = true;
                    }
                }
            }
        }
        return occupied;
    }

    private MigCell createMergedCell(int index, String defaultClass, int x, int y, int[] span) {
        return new MigCell(index, defaultClass, this.buildCellConstraint(x, y), x, y, span[0], span[1]);
    }

    private MigCell createRegularCell(int index, String defaultClass, int x, int y) {
        return new MigCell(index, defaultClass, this.buildCellConstraint(x, y), x, y, 1, 1);
    }

    private String buildCellConstraint(int x, int y) {
        return (x == 0 && y > 0 ? "newline, " : "") + "grow";
    }

    private String buildDefaultColumnConstraints(int cols) {
        return "[grow,fill]".repeat(cols);
    }

    private String buildDefaultRowConstraints(int rows) {
        return "[grow,fill]".repeat(rows);
    }

    private List<MigCell> createUniformGrid(int rows, int cols, String defaultClass) {
        ArrayList<MigCell> cells = new ArrayList<MigCell>(rows * cols);
        for (int y = 0; y < rows; ++y) {
            for (int x = 0; x < cols; ++x) {
                String constraint = this.buildCellConstraint(x, y);
                cells.add(new MigCell(y * cols + x, defaultClass, constraint, x, y, 1, 1));
            }
        }
        return cells;
    }

    private void parseLayoutProperties(Properties props) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (String key : props.stringPropertyNames()) {
            if (!key.startsWith("cell.") || !key.endsWith(".type")) continue;
            int cellId = this.parseInt(key.substring(5, key.length() - 5), -1);
            if (cellId < 0) {
                LOGGER.warn("Invalid cell ID in property key: {}", (Object)key);
                continue;
            }
            list.add(cellId);
        }
        Collections.sort(list);
        for (Integer cellId : list) {
            MigCell cell = this.parseCellFromProperties(props, cellId);
            if (cell == null) continue;
            this.cells.add(cell);
        }
    }

    private MigCell parseCellFromProperties(Properties props, int cellId) {
        String prefix = "cell." + cellId + ".";
        String type = props.getProperty(prefix + "type");
        if (!StringUtil.hasText((CharSequence)type)) {
            return null;
        }
        int x = this.parseInt(props.getProperty(prefix + "x"), 0);
        int y = this.parseInt(props.getProperty(prefix + "y"), 0);
        int spanX = this.parseInt(props.getProperty(prefix + "spanX"), 1);
        int spanY = this.parseInt(props.getProperty(prefix + "spanY"), 1);
        String constraints = props.getProperty(prefix + "constraints", "grow");
        return new MigCell(cellId, type, constraints, x, y, spanX, spanY);
    }

    private int parseInt(String value, int defaultValue) {
        if (!StringUtil.hasText((CharSequence)value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        }
        catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public String getId() {
        return this.id;
    }

    public String toString() {
        return this.title;
    }

    public String getLayoutConstraints() {
        return this.layoutConstraints;
    }

    public String getColumnConstraints() {
        return this.columnConstraints;
    }

    public void setColumnConstraintSpecs(ConstraintSpec[] specs) {
        if (this.isValidConstraintSpecs(specs, this.getGridSize().width)) {
            this.columnConstraints = this.buildConstraintsFromSpecs(specs);
        }
    }

    public void setRowConstraintSpecs(ConstraintSpec[] specs) {
        if (this.isValidConstraintSpecs(specs, this.getGridSize().height)) {
            this.rowConstraints = this.buildConstraintsFromSpecs(specs);
        }
    }

    private boolean isValidConstraintSpecs(ConstraintSpec[] specs, int expectedCount) {
        return specs != null && specs.length == expectedCount;
    }

    private String buildConstraintsFromSpecs(ConstraintSpec[] specs) {
        StringBuilder sb = new StringBuilder();
        for (ConstraintSpec spec : specs) {
            sb.append(spec.toConstraintString());
        }
        return sb.toString();
    }

    public void applyConstraintsToLayout(JPanel panel) {
        LayoutManager layoutManager = panel.getLayout();
        if (!(layoutManager instanceof MigLayout)) {
            LOGGER.warn("Panel does not use MigLayout, cannot apply constraints");
            return;
        }
        MigLayout migLayout = (MigLayout)layoutManager;
        Dimension gridSize = this.getGridSize();
        ConstraintSpec[] colSpecs = ConstraintSpec.parseAll(this.columnConstraints, gridSize.width);
        ConstraintSpec[] rowSpecs = ConstraintSpec.parseAll(this.rowConstraints, gridSize.height);
        String newColConstraints = this.buildConstraintsFromSpecs(colSpecs);
        String newRowConstraints = this.buildConstraintsFromSpecs(rowSpecs);
        migLayout.setColumnConstraints((Object)newColConstraints);
        migLayout.setRowConstraints((Object)newRowConstraints);
        Component[] components = panel.getComponents();
        if (components.length != this.cells.size()) {
            LOGGER.warn("Component count mismatch: {} components vs {} cells", (Object)components.length, (Object)this.cells.size());
            return;
        }
        for (int i = 0; i < components.length; ++i) {
            MigCell cell = this.cells.get(i);
            Dimension totalSize = panel.getSize();
            Dimension preferredSize = this.calculateCellPreferredSize(totalSize, cell, colSpecs, rowSpecs);
            if (preferredSize == null) continue;
            components[i].setPreferredSize(preferredSize);
        }
        panel.revalidate();
        panel.repaint();
    }

    private Dimension calculateCellPreferredSize(Dimension totalSize, MigCell cell, ConstraintSpec[] colSpecs, ConstraintSpec[] rowSpecs) {
        Integer width = this.calculateSpannedPreferredSize(totalSize.width, colSpecs, cell.x(), cell.spanX());
        Integer height = this.calculateSpannedPreferredSize(totalSize.height, rowSpecs, cell.y(), cell.spanY());
        if (width == null && height == null) {
            return null;
        }
        return new Dimension(width != null ? width : -1, height != null ? height : -1);
    }

    private Integer calculateSpannedPreferredSize(int totalSize, ConstraintSpec[] specs, int start, int span) {
        boolean hasBounds = false;
        int totalMin = 0;
        int totalMax = 0;
        boolean hasMin = false;
        boolean hasMax = false;
        double spannedWeight = 0.0;
        for (int i = start; i < start + span && i < specs.length; ++i) {
            ConstraintSpec s = specs[i];
            if (s.minSize() != null || s.maxSize() != null) {
                hasBounds = true;
                if (s.minSize() != null) {
                    totalMin += s.minSize().intValue();
                    hasMin = true;
                }
                if (s.maxSize() == null) continue;
                totalMax += s.maxSize().intValue();
                hasMax = true;
                continue;
            }
            spannedWeight += s.growWeight();
        }
        if (!hasBounds) {
            if (spannedWeight <= 0.0) {
                return null;
            }
            return (int)Math.round((double)totalSize * spannedWeight / 100.0);
        }
        if (hasMin && hasMax) {
            return (totalMin + totalMax) / 2;
        }
        if (hasMin) {
            return totalMin;
        }
        return totalMax;
    }

    public String getRowConstraints() {
        return this.rowConstraints;
    }

    public List<MigCell> getCells() {
        return new ArrayList<MigCell>(this.cells);
    }

    public int getCellCount() {
        return this.cells.size();
    }

    public void applyLayout(JPanel panel, Function<String, Component> componentProvider) {
        panel.removeAll();
        panel.setLayout((LayoutManager)new MigLayout(this.layoutConstraints, this.columnConstraints, this.rowConstraints));
        for (MigCell cell : this.cells) {
            Component component = componentProvider.apply(cell.type());
            if (component == null) continue;
            panel.add(component, cell.getFullConstraints());
        }
    }

    public Dimension getGridSize() {
        int maxX = 0;
        int maxY = 0;
        for (MigCell cell : this.cells) {
            maxX = Math.max(maxX, cell.x() + cell.spanX());
            maxY = Math.max(maxY, cell.y() + cell.spanY());
        }
        return new Dimension(maxX, maxY);
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Icon getIcon() {
        return this.icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    @Override
    public String getUIName() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public MigLayoutModel copy() {
        return new MigLayoutModel(this);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof MigLayoutModel)) return false;
        MigLayoutModel that = (MigLayoutModel)obj;
        if (!Objects.equals(this.id, that.id)) return false;
        if (!Objects.equals(this.title, that.title)) return false;
        if (!Objects.equals(this.cells, that.cells)) return false;
        return true;
    }

    public int hashCode() {
        return Objects.hash(this.id, this.title, this.cells);
    }

    private Icon buildIcon() {
        return new LayoutIcon(this);
    }

    private final class LayoutIcon
    implements Icon {
        final /* synthetic */ MigLayoutModel this$0;

        private LayoutIcon(MigLayoutModel migLayoutModel) {
            MigLayoutModel migLayoutModel2 = migLayoutModel;
            Objects.requireNonNull(migLayoutModel2);
            this.this$0 = migLayoutModel2;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D)g;
            Color originalColor = g2d.getColor();
            Object[] renderingHints = GuiUtils.setRenderingHints(g2d, true, true, false);
            try {
                ColorScheme colorScheme = this.determineColorScheme(c);
                this.drawIconBackground(g2d, x, y, colorScheme.background);
                this.drawGridCells(g2d, x, y, colorScheme.foreground);
            }
            finally {
                g2d.setColor(originalColor);
                GuiUtils.resetRenderingHints(g2d, renderingHints);
            }
        }

        private ColorScheme determineColorScheme(Component c) {
            Color background = GuiUtils.IconColor.ACTIONS_GREY.color;
            Color foreground = FlatUIUtils.getUIColor((String)"MenuItem.background", (Color)Color.WHITE);
            if (c instanceof RadioMenuItem) {
                RadioMenuItem menuItem = (RadioMenuItem)c;
                if (menuItem.isArmed()) {
                    background = FlatUIUtils.getUIColor((String)"MenuItem.selectionForeground", (Color)Color.DARK_GRAY);
                    foreground = FlatUIUtils.getUIColor((String)"MenuItem.selectionBackground", (Color)Color.LIGHT_GRAY);
                } else if (menuItem.isSelected()) {
                    foreground = FlatUIUtils.getUIColor((String)"MenuItem.checkBackground", (Color)Color.BLUE);
                }
            }
            return new ColorScheme(background, foreground);
        }

        private void drawIconBackground(Graphics2D g2d, int x, int y, Color background) {
            g2d.setColor(background);
            g2d.fillRect(x, y, this.getIconWidth(), this.getIconHeight());
        }

        private void drawGridCells(Graphics2D g2d, int x, int y, Color foreground) {
            Dimension gridSize = this.this$0.getGridSize();
            if (gridSize.width == 0 || gridSize.height == 0) {
                return;
            }
            double stepX = (double)this.getIconWidth() / (double)gridSize.width;
            double stepY = (double)this.getIconHeight() / (double)gridSize.height;
            for (MigCell cell : this.this$0.cells) {
                Rectangle2D cellRect = this.createCellRectangle(cell, x, y, stepX, stepY);
                this.drawCell(g2d, cellRect, foreground);
            }
        }

        private Rectangle2D createCellRectangle(MigCell cell, int x, int y, double stepX, double stepY) {
            return new Rectangle2D.Double((double)x + (double)cell.x() * stepX, (double)y + (double)cell.y() * stepY, (double)cell.spanX() * stepX, (double)cell.spanY() * stepY);
        }

        private void drawCell(Graphics2D g2d, Rectangle2D rect, Color borderColor) {
            g2d.setColor(borderColor);
            g2d.draw(rect);
        }

        @Override
        public int getIconWidth() {
            return GuiUtils.getScaleLength(22);
        }

        @Override
        public int getIconHeight() {
            return GuiUtils.getScaleLength(22);
        }
    }

    private record ColorScheme(Color background, Color foreground) {
    }
}

