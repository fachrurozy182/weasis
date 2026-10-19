/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui.layout;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.weasis.core.api.gui.layout.MigLayoutModel;

public class MergedCellsBuilder {
    private final int rows;
    private final int cols;
    private final String id;
    private final String title;
    private final String defaultClass;
    private final Map<Integer, int[]> mergedCells = new LinkedHashMap<Integer, int[]>();

    public MergedCellsBuilder(int rows, int cols, String id, String title, String defaultClass) {
        if (rows < 1 || cols < 1) {
            throw new IllegalArgumentException("Grid dimensions must be positive");
        }
        this.rows = rows;
        this.cols = cols;
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.title = Objects.requireNonNull(title, "title cannot be null");
        this.defaultClass = Objects.requireNonNull(defaultClass, "defaultClass cannot be null");
    }

    public MergedCellsBuilder addMergedCell(int row, int col, int spanX, int spanY) {
        this.validatePosition(row, col);
        this.validateSpan(spanX, spanY);
        this.validateBoundaries(row, col, spanX, spanY);
        int position = row * this.cols + col;
        this.mergedCells.put(position, new int[]{spanX, spanY});
        return this;
    }

    public MergedCellsBuilder addCell(int row, int col) {
        return this.addMergedCell(row, col, 1, 1);
    }

    public MigLayoutModel build() {
        return new MigLayoutModel(this.id, this.title, this.rows, this.cols, this.defaultClass, this.mergedCells);
    }

    private void validatePosition(int row, int col) {
        if (row < 0 || row >= this.rows) {
            throw new IllegalArgumentException("Row must be between 0 and %d, got: %d".formatted(new Object[]{this.rows - 1, row}));
        }
        if (col < 0 || col >= this.cols) {
            throw new IllegalArgumentException("Column must be between 0 and %d, got: %d".formatted(new Object[]{this.cols - 1, col}));
        }
    }

    private void validateSpan(int spanX, int spanY) {
        if (spanX < 1 || spanY < 1) {
            throw new IllegalArgumentException("Span dimensions must be positive, got: [%d, %d]".formatted(new Object[]{spanX, spanY}));
        }
    }

    private void validateBoundaries(int row, int col, int spanX, int spanY) {
        if (row + spanY > this.rows || col + spanX > this.cols) {
            throw new IllegalArgumentException("Merged cell at (%d,%d) with span [%d,%d] exceeds grid boundaries [%d,%d]".formatted(new Object[]{row, col, spanX, spanY, this.cols, this.rows}));
        }
    }
}

