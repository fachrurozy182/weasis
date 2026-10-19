/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.editor.image;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Objects;
import javax.swing.JPanel;
import org.weasis.core.api.gui.layout.ConstraintSpec;
import org.weasis.core.api.gui.layout.LayoutCellManager;
import org.weasis.core.api.gui.layout.MigCell;
import org.weasis.core.api.gui.layout.MigLayoutModel;

public class GridMouseHandler
extends MouseAdapter {
    private static final double MIN_CELL_SIZE = 50.0;
    private static final double PRECISION_SCALE = 1000.0;
    private final LayoutCellManager<?> cellManager;
    private final JPanel grid;
    private DragState dragState;

    public GridMouseHandler(LayoutCellManager<?> cellManager, JPanel grid) {
        this.cellManager = cellManager;
        this.grid = grid;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.dragState = this.createDragStateFromMouseEvent(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        this.dragState = null;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (this.dragState != null && this.isDraggingWithButton1(e)) {
            this.processDragResize(e.getPoint());
            this.updateCursorForDragDirection();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int cursorType = this.determineCursorType(e.getPoint());
        this.grid.setCursor(Cursor.getPredefinedCursor(cursorType));
    }

    @Override
    public void mouseExited(MouseEvent e) {
        this.grid.setCursor(Cursor.getDefaultCursor());
    }

    private DragState createDragStateFromMouseEvent(MouseEvent e) {
        Point clickPoint = e.getPoint();
        MigLayoutModel layout = this.cellManager.getLayoutModel();
        Dimension gridSize = layout.getGridSize();
        if (gridSize.width == 0 || gridSize.height == 0) {
            return null;
        }
        ResizeBorder border = this.findResizeBorderAt(clickPoint);
        if (border == null) {
            return null;
        }
        Rectangle gridBounds = this.grid.getBounds();
        ConstraintSpec[] colSpecs = this.parseAndUpdateConstraintSpecs(layout.getColumnConstraints(), gridSize.width, true);
        ConstraintSpec[] rowSpecs = this.parseAndUpdateConstraintSpecs(layout.getRowConstraints(), gridSize.height, false);
        return new DragState(clickPoint, border, gridBounds, colSpecs, rowSpecs);
    }

    private ResizeBorder findResizeBorderAt(Point point) {
        for (LayoutCellManager.CellEntry<?> entry : this.cellManager.getAllEntries()) {
            Component c = entry.getComponent();
            if (c == null) continue;
            Rectangle bounds = c.getBounds();
            MigCell cell = entry.getCell();
            if (cell.x() > 0 && this.isNearVerticalBorder(point, bounds)) {
                return new ResizeBorder(true, cell);
            }
            if (cell.y() <= 0 || !this.isNearHorizontalBorder(point, bounds)) continue;
            return new ResizeBorder(false, cell);
        }
        return null;
    }

    private ConstraintSpec[] parseAndUpdateConstraintSpecs(String constraintString, int count, boolean isColumn) {
        double totalSize;
        ConstraintSpec[] specs = ConstraintSpec.parseAll(constraintString, count);
        Rectangle gridBounds = this.grid.getBounds();
        double d = totalSize = isColumn ? gridBounds.getWidth() : gridBounds.getHeight();
        if (totalSize <= 0.0) {
            return specs;
        }
        MigLayoutModel layout = this.cellManager.getLayoutModel();
        for (MigCell cell : layout.getCells()) {
            double size;
            Component c;
            if (isColumn && cell.spanX() != 1 || !isColumn && cell.spanY() != 1 || (c = this.cellManager.getComponent(cell.position())) == null) continue;
            Rectangle bounds = c.getBounds();
            int index = isColumn ? cell.x() : cell.y();
            double d2 = size = isColumn ? bounds.getWidth() : bounds.getHeight();
            if (index >= specs.length) continue;
            double growWeight = this.calculateGrowWeight(size / totalSize);
            specs[index] = specs[index].withGrowWeight(growWeight);
        }
        return specs;
    }

    private void processDragResize(Point currentPoint) {
        if (this.dragState.border.isVertical) {
            this.resizeColumns(currentPoint);
        } else {
            this.resizeRows(currentPoint);
        }
    }

    private void resizeColumns(Point currentPoint) {
        double dx = currentPoint.getX() - (double)this.dragState.startPoint.x;
        int leftIdx = this.dragState.border.cell.x() - 1;
        int rightIdx = this.dragState.border.cell.x();
        ConstraintSpec leftSpec = this.dragState.colSpecs[leftIdx];
        ConstraintSpec rightSpec = this.dragState.colSpecs[rightIdx];
        double totalWidth = this.dragState.gridBounds.getWidth();
        double leftWidth = leftSpec.growWeight() / 100.0 * totalWidth;
        double rightWidth = rightSpec.growWeight() / 100.0 * totalWidth;
        ResizePair resized = this.calculateResizedWidths(leftWidth, rightWidth, dx);
        ConstraintSpec[] newSpecs = (ConstraintSpec[])this.dragState.colSpecs.clone();
        newSpecs[leftIdx] = leftSpec.withGrowWeight(this.calculateGrowWeight(resized.first / totalWidth));
        newSpecs[rightIdx] = rightSpec.withGrowWeight(this.calculateGrowWeight(resized.second / totalWidth));
        this.applyColumnSpecs(newSpecs);
    }

    private void resizeRows(Point currentPoint) {
        double dy = currentPoint.getY() - (double)this.dragState.startPoint.y;
        int topIdx = this.dragState.border.cell().y() - 1;
        int bottomIdx = this.dragState.border.cell().y();
        ConstraintSpec topSpec = this.dragState.rowSpecs[topIdx];
        ConstraintSpec bottomSpec = this.dragState.rowSpecs[bottomIdx];
        double totalHeight = this.dragState.gridBounds.getHeight();
        double topHeight = topSpec.growWeight() / 100.0 * totalHeight;
        double bottomHeight = bottomSpec.growWeight() / 100.0 * totalHeight;
        ResizePair resized = this.calculateResizedWidths(topHeight, bottomHeight, dy);
        ConstraintSpec[] newSpecs = (ConstraintSpec[])this.dragState.rowSpecs.clone();
        newSpecs[topIdx] = topSpec.withGrowWeight(this.calculateGrowWeight(resized.first / totalHeight));
        newSpecs[bottomIdx] = bottomSpec.withGrowWeight(this.calculateGrowWeight(resized.second / totalHeight));
        this.applyRowSpecs(newSpecs);
    }

    private ResizePair calculateResizedWidths(double firstSize, double secondSize, double delta) {
        double newFirst = firstSize + delta;
        double newSecond = secondSize - delta;
        if (newFirst < 50.0) {
            newFirst = 50.0;
            newSecond = firstSize + secondSize - 50.0;
        } else if (newSecond < 50.0) {
            newSecond = 50.0;
            newFirst = firstSize + secondSize - 50.0;
        }
        return new ResizePair(newFirst, newSecond);
    }

    private void applyColumnSpecs(ConstraintSpec[] specs) {
        MigLayoutModel layout = this.cellManager.getLayoutModel();
        layout.setColumnConstraintSpecs(specs);
        layout.applyConstraintsToLayout(this.grid);
    }

    private void applyRowSpecs(ConstraintSpec[] specs) {
        MigLayoutModel layout = this.cellManager.getLayoutModel();
        layout.setRowConstraintSpecs(specs);
        layout.applyConstraintsToLayout(this.grid);
    }

    private int determineCursorType(Point point) {
        for (MigCell cell : this.cellManager.getLayoutModel().getCells()) {
            Component c = this.cellManager.getComponent(cell.position());
            if (c == null) continue;
            Rectangle bounds = c.getBounds();
            if (this.isNearVerticalBorder(point, bounds)) {
                return 11;
            }
            if (!this.isNearHorizontalBorder(point, bounds)) continue;
            return 9;
        }
        return 0;
    }

    private boolean isNearVerticalBorder(Point point, Rectangle bounds) {
        return Math.abs(bounds.x - point.x) <= 5;
    }

    private boolean isNearHorizontalBorder(Point point, Rectangle bounds) {
        return Math.abs(bounds.y - point.y) <= 5;
    }

    private boolean isDraggingWithButton1(MouseEvent e) {
        return (e.getModifiersEx() & 0x400) != 0;
    }

    private void updateCursorForDragDirection() {
        if (this.dragState != null) {
            int cursorType = this.dragState.border.isVertical ? 11 : 9;
            this.grid.setCursor(Cursor.getPredefinedCursor(cursorType));
        }
    }

    private double calculateGrowWeight(double proportion) {
        return (double)Math.round(proportion * 1000.0) / 10.0;
    }

    private record DragState(Point startPoint, ResizeBorder border, Rectangle gridBounds, ConstraintSpec[] colSpecs, ConstraintSpec[] rowSpecs) {
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof DragState)) {
                return false;
            }
            DragState other = (DragState)((Object)obj);
            return Objects.equals(this.startPoint, other.startPoint) && Objects.equals((Object)this.border, (Object)other.border) && Objects.equals(this.gridBounds, other.gridBounds) && Arrays.equals((Object[])this.colSpecs, (Object[])other.colSpecs) && Arrays.equals((Object[])this.rowSpecs, (Object[])other.rowSpecs);
        }

        public int hashCode() {
            int result = Objects.hash(new Object[]{this.startPoint, this.border, this.gridBounds});
            result = 31 * result + Arrays.hashCode((Object[])this.colSpecs);
            result = 31 * result + Arrays.hashCode((Object[])this.rowSpecs);
            return result;
        }

        public String toString() {
            return "DragState[startPoint=" + String.valueOf(this.startPoint) + ", border=" + String.valueOf((Object)this.border) + ", gridBounds=" + String.valueOf(this.gridBounds) + ", colSpecs=" + Arrays.toString((Object[])this.colSpecs) + ", rowSpecs=" + Arrays.toString((Object[])this.rowSpecs) + "]";
        }
    }

    private record ResizeBorder(boolean isVertical, MigCell cell) {
    }

    private record ResizePair(double first, double second) {
    }
}

