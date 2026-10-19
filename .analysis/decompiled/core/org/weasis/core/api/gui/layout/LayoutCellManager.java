/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui.layout;

import java.awt.Component;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.weasis.core.api.gui.layout.MigCell;
import org.weasis.core.api.gui.layout.MigLayoutModel;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.ui.editor.image.ViewCanvas;

public class LayoutCellManager<E extends ImageElement>
implements Iterable<ViewCanvas<E>> {
    private final Map<Integer, CellEntry<E>> entriesByPosition = new HashMap<Integer, CellEntry<E>>();
    private MigLayoutModel layoutModel;

    public LayoutCellManager(MigLayoutModel layoutModel) {
        this.setLayoutModel(layoutModel);
    }

    public void setLayoutModel(MigLayoutModel layoutModel) {
        this.layoutModel = Objects.requireNonNull(layoutModel.copy(), "layoutModel cannot be null");
        this.entriesByPosition.clear();
    }

    public MigLayoutModel getLayoutModel() {
        return this.layoutModel;
    }

    public CellEntry<E> addViewCanvas(int position, ViewCanvas<E> viewCanvas) {
        Objects.requireNonNull(viewCanvas, "viewCanvas cannot be null");
        MigCell cell = this.getCellAtPosition(position);
        if (cell == null) {
            return null;
        }
        CellEntry<E> entry = CellEntry.forViewCanvas(cell, viewCanvas);
        this.entriesByPosition.put(position, entry);
        return entry;
    }

    public CellEntry<E> addComponent(int position, Component component) {
        Objects.requireNonNull(component, "component cannot be null");
        MigCell cell = this.getCellAtPosition(position);
        if (cell == null) {
            return null;
        }
        CellEntry entry = CellEntry.forComponent(cell, component);
        this.entriesByPosition.put(position, entry);
        return entry;
    }

    public CellEntry<E> replaceViewCanvas(int position, ViewCanvas<E> viewCanvas) {
        Objects.requireNonNull(viewCanvas, "viewCanvas cannot be null");
        MigCell cell = this.getCellAtPosition(position);
        if (cell == null) {
            return null;
        }
        CellEntry<E> newEntry = CellEntry.forViewCanvas(cell, viewCanvas);
        return this.entriesByPosition.put(position, newEntry);
    }

    public CellEntry<E> replaceComponent(int position, Component component) {
        Objects.requireNonNull(component, "component cannot be null");
        MigCell cell = this.getCellAtPosition(position);
        if (cell == null) {
            return null;
        }
        CellEntry newEntry = CellEntry.forComponent(cell, component);
        return this.entriesByPosition.put(position, newEntry);
    }

    public CellEntry<E> getEntry(int position) {
        return this.entriesByPosition.get(position);
    }

    public Component getComponent(int position) {
        CellEntry<E> entry = this.entriesByPosition.get(position);
        return entry != null ? entry.getComponent() : null;
    }

    public Optional<ViewCanvas<E>> getViewCanvas(int position) {
        CellEntry<E> entry = this.entriesByPosition.get(position);
        return entry != null ? entry.getViewCanvas() : Optional.empty();
    }

    public CellEntry<E> remove(int position) {
        return this.entriesByPosition.remove(position);
    }

    public List<CellEntry<E>> getAllEntries() {
        return this.entriesByPosition.values().stream().sorted(Comparator.comparingInt(CellEntry::getPosition)).toList();
    }

    public List<ViewCanvas<E>> getAllViewCanvases() {
        return this.entriesByPosition.values().stream().filter(CellEntry::isViewCanvas).sorted(Comparator.comparingInt(CellEntry::getPosition)).map(e -> e.getViewCanvas().orElseThrow()).toList();
    }

    public List<Component> getAllComponents() {
        return this.entriesByPosition.values().stream().sorted(Comparator.comparingInt(CellEntry::getPosition)).map(CellEntry::getComponent).toList();
    }

    public List<Component> getNonViewCanvasComponents() {
        return this.entriesByPosition.values().stream().filter(e -> !e.isViewCanvas()).sorted(Comparator.comparingInt(CellEntry::getPosition)).map(CellEntry::getComponent).toList();
    }

    public int getViewCanvasCount() {
        return (int)this.entriesByPosition.values().stream().filter(CellEntry::isViewCanvas).count();
    }

    public void clear() {
        this.entriesByPosition.clear();
    }

    public int size() {
        return this.entriesByPosition.size();
    }

    public boolean isEmpty() {
        return this.entriesByPosition.isEmpty();
    }

    public int findPositionOfViewCanvas(ViewCanvas<E> viewCanvas) {
        return this.entriesByPosition.entrySet().stream().filter(e -> ((CellEntry)e.getValue()).getViewCanvas().orElse(null) == viewCanvas).map(Map.Entry::getKey).findFirst().orElse(-1);
    }

    public int findPositionOfComponent(Component component) {
        return this.entriesByPosition.entrySet().stream().filter(e -> ((CellEntry)e.getValue()).getComponent() == component).map(Map.Entry::getKey).findFirst().orElse(-1);
    }

    public ViewCanvas<E> getViewCanvasByIndex(int index) {
        List<ViewCanvas<E>> viewCanvases = this.getAllViewCanvases();
        if (index >= 0 && index < viewCanvases.size()) {
            return viewCanvases.get(index);
        }
        return null;
    }

    public int indexOfViewCanvas(ViewCanvas<E> viewCanvas) {
        List<ViewCanvas<E>> viewCanvases = this.getAllViewCanvases();
        return viewCanvases.indexOf(viewCanvas);
    }

    private MigCell getCellAtPosition(int position) {
        return this.layoutModel.getCells().stream().filter(c -> c.position() == position).findFirst().orElse(null);
    }

    public MigCell getCell(CellEntry<E> entry) {
        return entry.getCell();
    }

    @Override
    public Iterator<ViewCanvas<E>> iterator() {
        return this.getAllViewCanvases().iterator();
    }

    public static class CellEntry<E extends ImageElement> {
        private final MigCell cell;
        private final ViewCanvas<E> viewCanvas;
        private final Component component;

        private CellEntry(MigCell cell, ViewCanvas<E> viewCanvas, Component component) {
            this.cell = Objects.requireNonNull(cell, "cell cannot be null");
            if (viewCanvas == null && component == null) {
                throw new IllegalArgumentException("Either viewCanvas or component must be non-null");
            }
            this.viewCanvas = viewCanvas;
            this.component = component;
        }

        public static <E extends ImageElement> CellEntry<E> forViewCanvas(MigCell cell, ViewCanvas<E> viewCanvas) {
            Objects.requireNonNull(viewCanvas, "viewCanvas cannot be null");
            return new CellEntry<E>(cell, viewCanvas, viewCanvas.getJComponent());
        }

        public static <E extends ImageElement> CellEntry<E> forComponent(MigCell cell, Component component) {
            Objects.requireNonNull(component, "component cannot be null");
            return new CellEntry<E>(cell, null, component);
        }

        public MigCell getCell() {
            return this.cell;
        }

        public Optional<ViewCanvas<E>> getViewCanvas() {
            return Optional.ofNullable(this.viewCanvas);
        }

        public Component getComponent() {
            return this.component;
        }

        public boolean isViewCanvas() {
            return this.viewCanvas != null;
        }

        public int getPosition() {
            return this.cell.position();
        }
    }
}

