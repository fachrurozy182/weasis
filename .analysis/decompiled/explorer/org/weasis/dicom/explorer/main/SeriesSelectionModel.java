/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.ui.FlatUIUtils
 *  org.weasis.core.api.explorer.model.DataExplorerModel
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.MediaSeriesGroupNode
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.media.data.Thumbnail
 *  org.weasis.core.ui.editor.ViewerOpenOptions
 *  org.weasis.core.ui.editor.ViewerPluginBuilder
 *  org.weasis.dicom.codec.DicomSeries
 */
package org.weasis.dicom.explorer.main;

import com.formdev.flatlaf.ui.FlatUIUtils;
import java.awt.Color;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.MediaSeriesGroupNode;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.media.data.Thumbnail;
import org.weasis.core.ui.editor.ViewerOpenOptions;
import org.weasis.core.ui.editor.ViewerPluginBuilder;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.main.DicomExplorer;
import org.weasis.dicom.explorer.main.SeriesPane;
import org.weasis.dicom.explorer.main.StudyPane;

public class SeriesSelectionModel
extends ArrayList<DicomSeries> {
    public static final String FOREGROUND = "List.foreground";
    public static final String BACKGROUND = "List.background";
    public static final String SELECTION_FOREGROUND = "List.selectionForeground";
    public static final String SELECTION_BACKGROUND = "List.selectionBackground";
    private static final Color DEFAULT_FOREGROUND = Color.DARK_GRAY;
    private static final Color DEFAULT_BACKGROUND = Color.LIGHT_GRAY;
    private static final Color DEFAULT_SELECTION_FOREGROUND = Color.LIGHT_GRAY;
    private static final Color DEFAULT_SELECTION_BACKGROUND = Color.DARK_GRAY;
    private final DicomExplorer explorer;
    private final List<SelectionChangeListener> listeners = new CopyOnWriteArrayList<SelectionChangeListener>();
    private DicomSeries anchorSelection;
    private DicomSeries leadSelection;
    private volatile boolean openingSeries = false;
    private final Set<DicomSeries> pendingUpdates = new LinkedHashSet<DicomSeries>();

    public SeriesSelectionModel(DicomExplorer explorer) {
        this.explorer = Objects.requireNonNull(explorer, "Explorer cannot be null");
    }

    @Override
    public void add(int index, DicomSeries element) {
        if (element != null && !this.contains(element)) {
            super.add(index, element);
            this.updateSeriesVisualState(element, true);
            this.notifySelectionChanged();
        }
    }

    @Override
    public boolean add(DicomSeries series) {
        if (series != null && !this.contains(series)) {
            boolean added = super.add(series);
            if (added) {
                this.updateSeriesVisualState(series, true);
                this.notifySelectionChanged();
            }
            return added;
        }
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends DicomSeries> collection) {
        if (collection == null || collection.isEmpty()) {
            return false;
        }
        boolean modified = false;
        for (DicomSeries dicomSeries : collection) {
            if (dicomSeries == null || this.contains(dicomSeries)) continue;
            super.add(dicomSeries);
            this.updateSeriesVisualState(dicomSeries, true);
            modified = true;
        }
        if (modified) {
            this.notifySelectionChanged();
        }
        return modified;
    }

    @Override
    public boolean addAll(int index, Collection<? extends DicomSeries> collection) {
        if (collection == null || collection.isEmpty()) {
            return false;
        }
        List<DicomSeries> toAdd = collection.stream().filter(Objects::nonNull).filter(series -> !this.contains(series)).collect(Collectors.toList());
        if (toAdd.isEmpty()) {
            return false;
        }
        boolean modified = super.addAll(index, toAdd);
        if (modified) {
            toAdd.forEach((? super T series) -> this.updateSeriesVisualState((DicomSeries)series, true));
            this.notifySelectionChanged();
        }
        return modified;
    }

    @Override
    public void clear() {
        if (this.isEmpty()) {
            return;
        }
        this.forEach((? super E series) -> this.updateSeriesVisualState((DicomSeries)series, false));
        this.anchorSelection = null;
        this.leadSelection = null;
        super.clear();
        this.notifySelectionChanged();
    }

    @Override
    public DicomSeries remove(int index) {
        if (index < 0 || index >= this.size()) {
            return null;
        }
        DicomSeries removed = (DicomSeries)super.remove(index);
        if (removed != null) {
            this.updateSeriesVisualState(removed, false);
            this.updateSelectionAnchors(removed);
            this.notifySelectionChanged();
        }
        return removed;
    }

    @Override
    public boolean remove(Object obj) {
        if (!(obj instanceof DicomSeries)) {
            return false;
        }
        DicomSeries series = (DicomSeries)obj;
        boolean removed = super.remove(series);
        if (removed) {
            this.updateSeriesVisualState(series, false);
            this.updateSelectionAnchors(series);
            this.notifySelectionChanged();
        }
        return removed;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        if (fromIndex >= toIndex || fromIndex < 0 || toIndex > this.size()) {
            return;
        }
        for (int i = fromIndex; i < toIndex; ++i) {
            DicomSeries series = (DicomSeries)this.get(i);
            this.updateSeriesVisualState(series, false);
            this.updateSelectionAnchors(series);
        }
        super.removeRange(fromIndex, toIndex);
        this.notifySelectionChanged();
    }

    @Override
    public DicomSeries set(int index, DicomSeries element) {
        if (index < 0 || index >= this.size() || element == null) {
            return null;
        }
        DicomSeries old = super.set(index, element);
        if (old != null) {
            this.updateSeriesVisualState(old, false);
            this.updateSelectionAnchors(old);
        }
        this.updateSeriesVisualState(element, true);
        this.notifySelectionChanged();
        return old;
    }

    public DicomSeries getAnchorSelection() {
        return this.anchorSelection;
    }

    public DicomSeries getLeadSelection() {
        return this.leadSelection;
    }

    public boolean isOpeningSeries() {
        return this.openingSeries;
    }

    public void setOpeningSeries(boolean openingSeries) {
        this.openingSeries = openingSeries;
    }

    public void selectAll() {
        DicomSeries first = this.getFirstElement();
        DicomSeries last = this.getLastElement();
        if (first != null && last != null) {
            this.setSelectionInterval(first, last);
        }
    }

    public void selectFirst() {
        DicomSeries first = this.getFirstElement();
        if (first != null) {
            this.setSelectionInterval(first, first);
        }
    }

    public void selectLast() {
        DicomSeries last = this.getLastElement();
        if (last != null) {
            this.setSelectionInterval(last, last);
        }
    }

    public void selectSeries(DicomSeries series) {
        if (series != null) {
            this.setSelectionInterval(series, series);
        }
    }

    public void selectNext() {
        DicomSeries current = this.getLeadSelection();
        DicomSeries next = this.getNextElement(current);
        if (next != null) {
            this.setSelectionInterval(next, next);
        }
    }

    public void selectPrevious() {
        DicomSeries current = this.getLeadSelection();
        DicomSeries previous = this.getPreviousElement(current);
        if (previous != null) {
            this.setSelectionInterval(previous, previous);
        }
    }

    void setSelectionInterval(DicomSeries anchor, DicomSeries lead) {
        if (anchor == null || lead == null) {
            return;
        }
        this.clear();
        this.addSelectionInterval(anchor, lead);
    }

    void removeSelectionInterval(DicomSeries anchor, DicomSeries lead) {
        if (anchor == null || lead == null) {
            return;
        }
        List<DicomSeries> toRemove = this.findSeriesInInterval(anchor, lead);
        toRemove.forEach(this::remove);
        this.requestFocusForSeries(lead);
        this.updateSelectionAnchors(anchor, lead);
    }

    void addSelectionInterval(DicomSeries anchor, DicomSeries lead) {
        if (anchor == null || lead == null) {
            return;
        }
        if (anchor.equals((Object)lead)) {
            this.add(anchor);
        } else {
            List<DicomSeries> intervalSeries = this.findSeriesInInterval(anchor, lead);
            this.addAll((Collection<? extends DicomSeries>)intervalSeries);
        }
        this.requestFocusForSeries(lead);
        this.updateSelectionAnchors(anchor, lead);
    }

    public DicomSeries getFirstElement() {
        return this.getPatientSeries().findFirst().orElse(null);
    }

    public DicomSeries getLastElement() {
        return this.getPatientSeries().reduce((dicomSeries, second) -> second).orElse(null);
    }

    public DicomSeries getPreviousElement(DicomSeries element) {
        if (element == null) {
            return null;
        }
        List allSeries = this.getPatientSeries().toList();
        if (allSeries.isEmpty()) {
            return null;
        }
        int index = allSeries.indexOf(element);
        if (index <= 0) {
            return (DicomSeries)allSeries.getLast();
        }
        return (DicomSeries)allSeries.get(index - 1);
    }

    public DicomSeries getNextElement(DicomSeries element) {
        if (element == null) {
            return null;
        }
        List allSeries = this.getPatientSeries().toList();
        if (allSeries.isEmpty()) {
            return null;
        }
        int index = allSeries.indexOf(element);
        if (index < 0 || index >= allSeries.size() - 1) {
            return (DicomSeries)allSeries.getFirst();
        }
        return (DicomSeries)allSeries.get(index + 1);
    }

    public void adjustSelection(InputEvent event, DicomSeries series) {
        boolean isAnchorSelected;
        if (event == null || series == null) {
            return;
        }
        boolean isControlDown = this.isControlModifierDown(event);
        boolean isShiftDown = event.isShiftDown();
        boolean bl = isAnchorSelected = this.anchorSelection != null && this.contains(this.anchorSelection);
        if (isControlDown) {
            this.handleControlModifiedSelection(event, series, isShiftDown, isAnchorSelected);
        } else if (isShiftDown) {
            this.handleShiftModifiedSelection(series);
        } else {
            this.handleSimpleSelection(series);
        }
    }

    public void addSelectionChangeListener(SelectionChangeListener listener) {
        if (listener != null) {
            this.listeners.add(listener);
        }
    }

    public void removeSelectionChangeListener(SelectionChangeListener listener) {
        this.listeners.remove(listener);
    }

    private void updateSeriesVisualState(DicomSeries series, boolean selected) {
        if (series == null) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            Optional<Thumbnail> thumbnail = this.getThumbnailForSeries(series);
            thumbnail.ifPresent(thumb -> this.updateThumbnailVisualState((Thumbnail)thumb, selected));
        });
    }

    private void updateThumbnailVisualState(Thumbnail thumbnail, boolean selected) {
        Container parent = thumbnail.getParent();
        if (!(parent instanceof JPanel)) {
            return;
        }
        Color background = selected ? FlatUIUtils.getUIColor((String)SELECTION_BACKGROUND, (Color)DEFAULT_SELECTION_BACKGROUND) : FlatUIUtils.getUIColor((String)BACKGROUND, (Color)DEFAULT_BACKGROUND);
        Color foreground = selected ? FlatUIUtils.getUIColor((String)SELECTION_FOREGROUND, (Color)DEFAULT_SELECTION_FOREGROUND) : FlatUIUtils.getUIColor((String)FOREGROUND, (Color)DEFAULT_FOREGROUND);
        parent.setBackground(background);
        if (parent instanceof SeriesPane) {
            SeriesPane pane = (SeriesPane)parent;
            pane.getLabel().setForeground(foreground);
        }
    }

    private Optional<Thumbnail> getThumbnailForSeries(DicomSeries series) {
        Optional<Thumbnail> optional;
        if (series == null) {
            return Optional.empty();
        }
        Object thumbnailObj = series.getTagValue(TagW.Thumbnail);
        if (thumbnailObj instanceof Thumbnail) {
            Thumbnail thumb = (Thumbnail)thumbnailObj;
            optional = Optional.of(thumb);
        } else {
            optional = Optional.empty();
        }
        return optional;
    }

    private void requestFocusForSeries(DicomSeries series) {
        if (series == null) {
            return;
        }
        this.getThumbnailForSeries(series).ifPresent(thumbnail -> {
            if (!thumbnail.hasFocus() && thumbnail.isRequestFocusEnabled()) {
                SwingUtilities.invokeLater(() -> thumbnail.requestFocus());
            }
        });
    }

    private void updateSelectionAnchors(DicomSeries removed) {
        if (this.anchorSelection == removed) {
            this.anchorSelection = null;
        }
        if (this.leadSelection == removed) {
            this.leadSelection = null;
        }
    }

    private void updateSelectionAnchors(DicomSeries anchor, DicomSeries lead) {
        this.anchorSelection = anchor;
        this.leadSelection = lead;
    }

    private List<DicomSeries> findSeriesInInterval(DicomSeries anchor, DicomSeries lead) {
        ArrayList<DicomSeries> result = new ArrayList<DicomSeries>();
        Optional<MediaSeriesGroupNode> patient = Optional.ofNullable(this.explorer.getSelectedPatient());
        if (patient.isEmpty()) {
            return result;
        }
        boolean inInterval = false;
        boolean foundFirst = false;
        block0: for (StudyPane studyPane : this.explorer.getPaneManager().getStudyList((MediaSeriesGroup)patient.get())) {
            for (SeriesPane seriesPane : studyPane.getSeriesPaneList()) {
                DicomSeries series = seriesPane.getDicomSeries();
                if (series == anchor || series == lead) {
                    result.add(series);
                    if (foundFirst) break block0;
                    foundFirst = true;
                    inInterval = true;
                    continue;
                }
                if (!inInterval) continue;
                result.add(series);
            }
        }
        return result;
    }

    private Stream<DicomSeries> getPatientSeries() {
        Optional<MediaSeriesGroupNode> patient = Optional.ofNullable(this.explorer.getSelectedPatient());
        if (patient.isEmpty()) {
            return Stream.empty();
        }
        return this.explorer.getPaneManager().getStudyList((MediaSeriesGroup)patient.get()).stream().flatMap(studyPane -> studyPane.getSeriesPaneList().stream()).map(SeriesPane::getDicomSeries).filter(Objects::nonNull);
    }

    private boolean isControlModifierDown(InputEvent event) {
        return (event.getModifiersEx() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()) != 0;
    }

    private void handleControlModifiedSelection(InputEvent event, DicomSeries series, boolean isShiftDown, boolean isAnchorSelected) {
        if (isShiftDown) {
            if (isAnchorSelected) {
                this.addSelectionInterval(this.anchorSelection, series);
            } else {
                this.removeSelectionInterval(this.anchorSelection, series);
            }
        } else {
            this.toggleSelection(series);
            this.updateSelectionAnchors(series, series);
        }
    }

    private void handleShiftModifiedSelection(DicomSeries series) {
        this.setSelectionInterval(this.anchorSelection, series);
    }

    private void handleSimpleSelection(DicomSeries series) {
        this.setSelectionInterval(series, series);
    }

    private void notifySelectionChanged() {
        if (!this.listeners.isEmpty()) {
            SwingUtilities.invokeLater(() -> this.listeners.forEach((? super T listener) -> {
                try {
                    listener.selectionChanged(this);
                }
                catch (Exception e) {
                    System.err.println("Error in selection change listener: " + e.getMessage());
                }
            }));
        }
    }

    public void ensureSelection(DicomSeries series) {
        if (this.isEmpty() && series != null) {
            this.selectSeries(series);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean openSelectedSeries(DicomModel dicomModel) {
        if (this.isEmpty()) {
            return false;
        }
        this.setOpeningSeries(true);
        try {
            ViewerPluginBuilder.openInDefaultViewer(new ArrayList<DicomSeries>(this), (DataExplorerModel)dicomModel, (ViewerOpenOptions)ViewerOpenOptions.defaults());
            boolean bl = true;
            return bl;
        }
        catch (Exception ex) {
            System.err.println("Failed to open selected series: " + ex.getMessage());
            boolean bl = false;
            return bl;
        }
        finally {
            this.setOpeningSeries(false);
        }
    }

    public void requestFocusOnSelection() {
        DicomSeries lead = this.getLeadSelection();
        if (lead != null) {
            this.requestFocusForSeries(lead);
        }
    }

    public boolean isSelected(DicomSeries series) {
        return this.contains(series);
    }

    public void toggleSelection(DicomSeries series) {
        if (series == null) {
            return;
        }
        if (this.contains(series)) {
            this.remove(series);
        } else {
            this.add(series);
        }
    }

    public int getSelectionCount() {
        return this.size();
    }

    public boolean hasSelection() {
        return !this.isEmpty();
    }

    @FunctionalInterface
    public static interface SelectionChangeListener {
        public void selectionChanged(SeriesSelectionModel var1);
    }
}

