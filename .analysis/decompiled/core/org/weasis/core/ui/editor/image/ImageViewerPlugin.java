/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.miginfocom.swing.MigLayout
 */
package org.weasis.core.ui.editor.image;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.LayoutManager;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.weasis.core.Messages;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.gui.Image2DViewer;
import org.weasis.core.api.gui.layout.LayoutCellManager;
import org.weasis.core.api.gui.layout.MergedCellsBuilder;
import org.weasis.core.api.gui.layout.MigCell;
import org.weasis.core.api.gui.layout.MigLayoutModel;
import org.weasis.core.api.gui.util.ActionState;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.ComboItemListener;
import org.weasis.core.api.gui.util.Filter;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.gui.util.WinUtil;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.ui.editor.SeriesViewer;
import org.weasis.core.ui.editor.SeriesViewerEvent;
import org.weasis.core.ui.editor.SeriesViewerListener;
import org.weasis.core.ui.editor.ViewerOpenOptions;
import org.weasis.core.ui.editor.image.Canvas;
import org.weasis.core.ui.editor.image.GridMouseHandler;
import org.weasis.core.ui.editor.image.HistogramView;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.ui.editor.image.MouseActions;
import org.weasis.core.ui.editor.image.SynchData;
import org.weasis.core.ui.editor.image.SynchOptionsCheckBoxGroup;
import org.weasis.core.ui.editor.image.SynchView;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.editor.image.ViewerPlugin;
import org.weasis.core.ui.model.graphic.DragGraphic;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.graphic.GraphicSelectionListener;
import org.weasis.core.ui.pref.Monitor;
import org.weasis.core.ui.util.MouseEventDouble;

public abstract class ImageViewerPlugin<E extends ImageElement>
extends ViewerPlugin<E> {
    public static final String F_VIEWS = Messages.getString("ImageViewerPlugin.2");
    public static final Class<?> view2dClass = ViewCanvas.class;
    public static final MigLayoutModel VIEWS_1x1 = new MigLayoutModel("1x1", String.format(Messages.getString("ImageViewerPlugin.1"), "1x1"), 1, 1, view2dClass.getName());
    public static final MigLayoutModel VIEWS_2x1 = new MigLayoutModel("2x1", String.format(F_VIEWS, "2x1"), 2, 1, view2dClass.getName());
    public static final MigLayoutModel VIEWS_1x2 = new MigLayoutModel("1x2", String.format(F_VIEWS, "1x2"), 1, 2, view2dClass.getName());
    public static final MigLayoutModel VIEWS_1x3 = new MigLayoutModel("1x3", String.format(F_VIEWS, "1x3"), 1, 3, view2dClass.getName());
    public static final MigLayoutModel VIEWS_1x4 = new MigLayoutModel("1x4", String.format(F_VIEWS, "1x4"), 1, 4, view2dClass.getName());
    public static final MigLayoutModel VIEWS_2x2_f2 = new MergedCellsBuilder(2, 2, "layout_c2x1", "3 views (right merged)", view2dClass.getName()).addCell(0, 0).addMergedCell(0, 1, 1, 2).addCell(1, 0).build();
    public static final MigLayoutModel VIEWS_2_f1x2 = new MergedCellsBuilder(2, 2, "layout_r1x2", "3 views (top merged)", view2dClass.getName()).addMergedCell(0, 0, 2, 1).addCell(1, 0).addCell(1, 1).build();
    public static final MigLayoutModel VIEWS_2x2 = new MigLayoutModel("2x2", String.format(F_VIEWS, "2x2"), 2, 2, view2dClass.getName());
    public static final MigLayoutModel VIEWS_2x3 = new MigLayoutModel("2x3", String.format(F_VIEWS, "2x3"), 2, 3, view2dClass.getName());
    public static final MigLayoutModel VIEWS_2x4 = new MigLayoutModel("2x4", String.format(F_VIEWS, "2x4"), 2, 4, view2dClass.getName());
    protected ViewCanvas<E> selectedImagePane = null;
    protected final LayoutCellManager<E> cellManager;
    protected SynchView synchView = SynchView.DEFAULT_STACK;
    protected final ImageViewerEventManager<E> eventManager;
    protected final JPanel grid;
    protected final MigLayout migLayout;

    protected ImageViewerPlugin(ImageViewerEventManager<E> eventManager, String pluginName) {
        this(eventManager, VIEWS_1x1, pluginName, null, null, null);
    }

    protected ImageViewerPlugin(ImageViewerEventManager<E> eventManager, MigLayoutModel layoutModel, String uid, String pluginName, Icon icon, String tooltips) {
        super(uid, pluginName, icon, tooltips);
        if (eventManager == null) {
            throw new IllegalArgumentException("EventManager cannot be null");
        }
        this.eventManager = eventManager;
        this.cellManager = new LayoutCellManager(VIEWS_1x1.copy());
        this.grid = new JPanel();
        this.grid.setFocusCycleRoot(true);
        this.migLayout = new MigLayout();
        this.grid.setLayout((LayoutManager)this.migLayout);
        this.add((Component)this.grid, "Center");
        this.setLayoutModel(layoutModel);
        GridMouseHandler mouseHandler = new GridMouseHandler(this.cellManager, this.grid);
        this.grid.addMouseListener(mouseHandler);
        this.grid.addMouseMotionListener(mouseHandler);
    }

    public abstract boolean isViewType(Class<?> var1, String var2);

    public abstract int getViewTypeNumber(MigLayoutModel var1, Class<?> var2);

    public abstract ViewCanvas<E> createDefaultView(String var1);

    public abstract Component createComponent(String var1);

    public abstract Class<?> getSeriesViewerClass();

    public abstract MigLayoutModel getDefaultLayoutModel();

    public ViewCanvas<E> getSelectedViewCanvas() {
        return this.selectedImagePane;
    }

    public List<ViewCanvas<E>> getView2ds() {
        return this.cellManager.getAllViewCanvases();
    }

    public ImageViewerEventManager<E> getEventManager() {
        return this.eventManager;
    }

    @Override
    public void close() {
        super.close();
        GuiExecutor.execute(() -> {
            this.removeComponents();
            for (ViewCanvas<E> v : this.cellManager) {
                this.resetMaximizedSelectedImagePane(v);
                v.disposeView();
            }
        });
    }

    public synchronized MigLayoutModel getLayoutModel() {
        return this.cellManager.getLayoutModel();
    }

    public LayoutCellManager<E> getCellManager() {
        return this.cellManager;
    }

    public MigLayoutModel getOriginalLayoutModel() {
        Optional<MigLayoutModel> layout = this.eventManager.getAction(ActionW.LAYOUT);
        if (layout.isPresent()) {
            MigLayoutModel currentLayout = this.cellManager.getLayoutModel();
            for (Object element : ((ComboItemListener)((Object)layout.get())).getAllItem()) {
                if (!(element instanceof MigLayoutModel)) continue;
                MigLayoutModel gbm = (MigLayoutModel)element;
                if ((currentLayout.getIcon() == null || gbm.getIcon() != currentLayout.getIcon()) && !currentLayout.toString().equals(gbm.toString())) continue;
                return gbm;
            }
        }
        return this.cellManager.getLayoutModel();
    }

    public static MigLayoutModel buildMigLayoutModel(int rows, int cols, String type) {
        StringBuilder buf = new StringBuilder();
        buf.append(rows);
        buf.append("x");
        buf.append(cols);
        return new MigLayoutModel(buf.toString(), String.format(F_VIEWS, buf), rows, cols, type);
    }

    public static void registerInDataExplorerModel(DataExplorerModel model, PropertyChangeListener instance) {
        if (model != null && instance != null) {
            model.addPropertyChangeListener(instance);
        }
    }

    public static LayoutModel getLayoutModel(ViewerOpenOptions options, MigLayoutModel defaultModel, ComboItemListener<MigLayoutModel> layoutAction) {
        MigLayoutModel model = defaultModel;
        String uid = null;
        if (options != null) {
            int viewCount = options.seriesCount();
            if (viewCount > 1 && layoutAction != null) {
                model = ImageViewerPlugin.getBestDefaultViewLayout(layoutAction, viewCount, defaultModel);
            }
            uid = options.uid();
        }
        return new LayoutModel(uid, model);
    }

    @Override
    public void addSeries(MediaSeries<E> sequence) {
        if (sequence != null && this.selectedImagePane != null) {
            if (SynchData.Mode.TILE.equals((Object)this.synchView.getSynchData().getMode())) {
                this.selectedImagePane.setSeries(sequence, null);
                this.updateTileOffset();
                return;
            }
            ViewCanvas<E> viewPane = this.getSelectedViewCanvas();
            if (viewPane != null) {
                viewPane.setSeries(sequence);
                viewPane.getJComponent().repaint();
                this.setSelectedImagePane(this.getNextSelectedImagePane());
            }
        }
    }

    @Override
    public void removeSeries(MediaSeries<E> series) {
        if (series != null) {
            for (ViewCanvas<E> v : this.cellManager) {
                if (v.getSeries() != series) continue;
                v.setSeries(null, null);
            }
        }
    }

    public boolean closeIfNoContent() {
        if (this.getOpenSeries().isEmpty()) {
            this.close();
            this.handleFocusAfterClosing();
            return true;
        }
        return false;
    }

    @Override
    public List<MediaSeries<E>> getOpenSeries() {
        ArrayList list = new ArrayList();
        for (ViewCanvas<E> v : this.cellManager) {
            MediaSeries s = v.getSeries();
            if (s == null) continue;
            list.add(s);
        }
        return list;
    }

    public void changeLayoutModel(MigLayoutModel layoutModel) {
        this.eventManager.getAction(ActionW.LAYOUT).ifPresent(itemListener -> itemListener.setSelectedItem(layoutModel));
    }

    protected void removeComponents() {
        for (Component c : this.cellManager.getNonViewCanvasComponents()) {
            if (!(c instanceof SeriesViewerListener)) continue;
            SeriesViewerListener viewerListener = (SeriesViewerListener)((Object)c);
            this.eventManager.removeSeriesViewerListener(viewerListener);
        }
    }

    protected JComponent buildInstance(Class<?> cl) throws Exception {
        JComponent component = this.hasSeriesViewerConstructor(cl) ? (JComponent)cl.getConstructor(SeriesViewer.class).newInstance(this) : (JComponent)cl.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
        if (component instanceof SeriesViewerListener) {
            SeriesViewerListener viewerListener = (SeriesViewerListener)((Object)component);
            this.eventManager.addSeriesViewerListener(viewerListener);
        }
        return component;
    }

    private boolean hasSeriesViewerConstructor(Class<?> clazz) {
        for (Constructor<?> constructor : clazz.getConstructors()) {
            Class<?>[] types = constructor.getParameterTypes();
            if (types.length != 1 || !types[0].isAssignableFrom(SeriesViewer.class)) continue;
            return true;
        }
        return false;
    }

    protected synchronized void setLayoutModel(MigLayoutModel layoutModel) {
        MigLayoutModel newLayout = layoutModel == null ? VIEWS_1x1.copy() : layoutModel.copy();
        List<ViewCanvas<E>> preservedViews = this.preserveViewsWithImages(newLayout);
        this.removeComponents();
        this.cellManager.setLayoutModel(newLayout);
        this.grid.removeAll();
        this.updateMigLayoutContraints(newLayout);
        GraphicSelectionListener graphicListener = this.populateGridWithComponents(newLayout, preservedViews);
        this.configureViewsAndFireEvents(graphicListener);
    }

    private List<ViewCanvas<E>> preserveViewsWithImages(MigLayoutModel newLayout) {
        Object object;
        List<ViewCanvas<E>> allCanvases = this.cellManager.getAllViewCanvases();
        List viewsWithImages = allCanvases.stream().filter(v -> v.getSeries() != null && v.getImage() != null).toList();
        allCanvases.stream().filter(v -> v.getSeries() == null || v.getImage() == null).forEach(Canvas::disposeView);
        int layoutCapacity = this.getViewTypeNumber(newLayout, view2dClass);
        if (viewsWithImages.size() > layoutCapacity) {
            viewsWithImages.subList(layoutCapacity, viewsWithImages.size()).forEach(Canvas::disposeView);
            return new ArrayList<ViewCanvas<E>>(viewsWithImages.subList(0, layoutCapacity));
        }
        ComboItemListener synch = this.eventManager.getAction(ActionW.SYNCH).orElse(null);
        if (synch != null && (object = synch.getSelectedItem()) instanceof SynchView) {
            SynchView sel = (SynchView)object;
            sel.resetSynchData();
        }
        return new ArrayList<ViewCanvas<E>>(viewsWithImages);
    }

    private GraphicSelectionListener populateGridWithComponents(MigLayoutModel layout, List<ViewCanvas<E>> preservedViews) {
        HistogramView graphicListener = null;
        for (MigCell cell : layout.getCells()) {
            Component component = this.createComponentForCell(cell, preservedViews);
            if (component instanceof HistogramView) {
                HistogramView histogramView;
                graphicListener = histogramView = (HistogramView)component;
            }
            if (component == null) continue;
            this.grid.add(component, cell.getFullConstraints());
        }
        layout.applyConstraintsToLayout(this.grid);
        return graphicListener;
    }

    private Component createComponentForCell(MigCell cell, List<ViewCanvas<E>> preservedViews) {
        boolean isView2d = this.isViewType(view2dClass, cell.type());
        if (isView2d) {
            return this.createViewCanvasForCell(cell, preservedViews);
        }
        return this.createNonViewComponent(cell);
    }

    private Component createViewCanvasForCell(MigCell cell, List<ViewCanvas<E>> preservedViews) {
        ViewCanvas<E> viewCanvas;
        ViewCanvas<E> viewCanvas2 = viewCanvas = preservedViews.isEmpty() ? this.createFreshViewCanvas(cell.type()) : (ViewCanvas<E>)preservedViews.removeFirst();
        if (viewCanvas != null) {
            this.cellManager.addViewCanvas(cell.position(), viewCanvas);
            if (viewCanvas.getSeries() != null) {
                viewCanvas.getSeries().setOpen(true);
            }
            return viewCanvas.getJComponent();
        }
        return null;
    }

    private ViewCanvas<E> createFreshViewCanvas(String type) {
        ViewCanvas<E> viewCanvas = this.createDefaultView(type);
        if (viewCanvas != null) {
            viewCanvas.registerDefaultListeners();
        }
        return viewCanvas;
    }

    private Component createNonViewComponent(MigCell cell) {
        Component component = this.createComponent(cell.type());
        if (component != null) {
            if (component instanceof JComponent) {
                JComponent jComponent = (JComponent)component;
                jComponent.setOpaque(true);
            }
            this.cellManager.addComponent(cell.position(), component);
        }
        return component;
    }

    private void configureViewsAndFireEvents(GraphicSelectionListener graphicListener) {
        List<ViewCanvas<E>> allViews = this.cellManager.getAllViewCanvases();
        if (allViews.isEmpty()) {
            return;
        }
        this.selectedImagePane = (ViewCanvas)allViews.getFirst();
        MouseActions mouseActions = this.eventManager.getMouseActions();
        boolean tiledMode = SynchData.Mode.TILE.equals((Object)this.synchView.getSynchData().getMode());
        for (int i = 0; i < allViews.size(); ++i) {
            ViewCanvas<E> view = allViews.get(i);
            this.configureView(view, i, tiledMode, mouseActions, graphicListener);
        }
        this.selectedImagePane.setSelected(true);
        this.eventManager.updateComponentsListener(this.selectedImagePane);
        if (this.selectedImagePane.getSeries() instanceof Series) {
            this.eventManager.fireSeriesViewerListeners(new SeriesViewerEvent(this, this.selectedImagePane.getSeries(), (MediaElement)this.selectedImagePane.getImage(), SeriesViewerEvent.EVENT.LAYOUT));
        }
    }

    private void configureView(ViewCanvas<E> view, int index, boolean tiledMode, MouseActions mouseActions, GraphicSelectionListener graphicListener) {
        view.closeLens();
        if (tiledMode) {
            view.setTileOffset(index);
            view.setSeries(this.selectedImagePane.getSeries(), null);
        }
        view.enableMouseAndKeyListener(mouseActions);
        if (graphicListener != null) {
            view.getGraphicManager().addGraphicSelectionListener(graphicListener);
        }
    }

    public void replaceView(ViewCanvas<E> oldView, ViewCanvas<E> newView) {
        if (oldView == null || newView == null) {
            return;
        }
        int position = this.cellManager.findPositionOfViewCanvas(oldView);
        if (position == -1) {
            return;
        }
        this.performViewReplacement(oldView, newView, position);
        this.updateViewsAfterReplacement();
    }

    private void performViewReplacement(ViewCanvas<E> oldView, ViewCanvas<E> newView, int position) {
        if (this.selectedImagePane == oldView) {
            this.selectedImagePane = newView;
        }
        oldView.disposeView();
        this.cellManager.replaceViewCanvas(position, newView);
        if (newView.getSeries() != null) {
            newView.getSeries().setOpen(true);
        }
        this.rebuildGridLayout();
    }

    private void updateViewsAfterReplacement() {
        List<ViewCanvas<E>> allViews = this.cellManager.getAllViewCanvases();
        if (allViews.isEmpty()) {
            return;
        }
        this.ensureSelectedImagePane(allViews);
        MouseActions mouseActions = this.eventManager.getMouseActions();
        boolean tiledMode = SynchData.Mode.TILE.equals((Object)this.synchView.getSynchData().getMode());
        this.configureViewsAfterReplacement(allViews, tiledMode, mouseActions);
        this.selectedImagePane.setSelected(true);
        this.eventManager.updateComponentsListener(this.selectedImagePane);
    }

    private void ensureSelectedImagePane(List<ViewCanvas<E>> allViews) {
        if (this.selectedImagePane == null) {
            this.selectedImagePane = (ViewCanvas)allViews.getFirst();
        }
    }

    private void configureViewsAfterReplacement(List<ViewCanvas<E>> allViews, boolean tiledMode, MouseActions mouseActions) {
        for (int i = 0; i < allViews.size(); ++i) {
            ViewCanvas<E> view = allViews.get(i);
            view.closeLens();
            if (tiledMode) {
                view.setTileOffset(i);
                view.setSeries(this.selectedImagePane.getSeries(), null);
            }
            view.enableMouseAndKeyListener(mouseActions);
        }
    }

    private void rebuildGridLayout() {
        this.grid.removeAll();
        MigLayoutModel currentLayout = this.cellManager.getLayoutModel();
        this.migLayout.setLayoutConstraints((Object)currentLayout.getLayoutConstraints());
        for (MigCell cell : currentLayout.getCells()) {
            Component component = this.cellManager.getComponent(cell.position());
            if (component == null) continue;
            this.grid.add(component, cell.getFullConstraints());
        }
        currentLayout.applyConstraintsToLayout(this.grid);
    }

    public void resetMaximizedSelectedImagePane(ViewCanvas<E> viewCanvas) {
        Dialog fullscreenDialog;
        if (this.grid.getComponentCount() == 1 && (fullscreenDialog = WinUtil.getParentDialog(this.grid)) != null && fullscreenDialog.getTitle().equals(Messages.getString("ImageViewerPlugin.fullscreen"))) {
            this.maximizedSelectedImagePane(viewCanvas, null);
        }
    }

    public void maximizedSelectedImagePane(ViewCanvas<E> defaultView2d, MouseEvent evt) {
        if (this.shouldPreventMaximization(defaultView2d, evt)) {
            return;
        }
        this.removeFocusListenersFromViews();
        String titleDialog = Messages.getString("ImageViewerPlugin.fullscreen");
        Dialog fullscreenDialog = WinUtil.getParentDialog(this.grid);
        boolean isDetached = fullscreenDialog != null && !titleDialog.equals(fullscreenDialog.getTitle());
        this.grid.removeAll();
        if (isDetached || fullscreenDialog == null) {
            this.enterFullscreenMode(defaultView2d, titleDialog, fullscreenDialog, isDetached);
        } else {
            this.exitFullscreenMode(defaultView2d, fullscreenDialog);
        }
    }

    private boolean shouldPreventMaximization(ViewCanvas<E> viewCanvas, MouseEvent evt) {
        List<DragGraphic> selectedGraphics = viewCanvas.getGraphicManager().getSelectedDraggableGraphics();
        if (selectedGraphics.stream().anyMatch(g -> Objects.equals(g.getPtsNumber(), Graphic.UNDEFINED))) {
            return true;
        }
        if (evt != null) {
            MouseEventDouble mouseEvt = new MouseEventDouble(evt);
            mouseEvt.setImageCoordinates(viewCanvas.getImageCoordinatesFromMouse(evt.getX(), evt.getY()));
            return viewCanvas.getGraphicManager().getFirstGraphicIntersecting(mouseEvt).isPresent();
        }
        return false;
    }

    private void removeFocusListenersFromViews() {
        this.cellManager.getAllViewCanvases().forEach(v -> v.getJComponent().removeFocusListener((FocusListener)v));
    }

    private void addFocusListenersToViews() {
        this.cellManager.getAllViewCanvases().forEach(v -> v.getJComponent().addFocusListener((FocusListener)v));
    }

    private void enterFullscreenMode(final ViewCanvas<E> viewCanvas, String title, Dialog existingDialog, boolean isDetached) {
        this.remove(this.grid);
        this.grid.setLayout((LayoutManager)new MigLayout("fill, ins 0", "[grow,fill]", "[grow,fill]"));
        this.grid.add((Component)viewCanvas.getJComponent(), "grow");
        viewCanvas.getJComponent().addFocusListener(viewCanvas);
        Frame parentFrame = WinUtil.getParentFrame(this);
        JDialog fullscreenDialog = new JDialog(isDetached ? existingDialog : parentFrame, title, Dialog.ModalityType.APPLICATION_MODAL);
        fullscreenDialog.add((Component)this.grid, "Center");
        fullscreenDialog.addWindowListener(new WindowAdapter(this){
            final /* synthetic */ ImageViewerPlugin this$0;
            {
                ImageViewerPlugin imageViewerPlugin = this$0;
                Objects.requireNonNull(imageViewerPlugin);
                this.this$0 = imageViewerPlugin;
            }

            @Override
            public void windowClosing(WindowEvent e) {
                this.this$0.maximizedSelectedImagePane(viewCanvas, null);
            }
        });
        this.configureDialogBounds(fullscreenDialog, parentFrame, existingDialog, isDetached);
    }

    private void configureDialogBounds(Dialog dialog, Frame parentFrame, Dialog existingDialog, boolean isDetached) {
        if (!isDetached && (parentFrame.getExtendedState() & 6) == 6) {
            dialog.setBounds(parentFrame.getBounds());
            dialog.setVisible(true);
        } else {
            Monitor monitor = Monitor.getMonitor(isDetached ? existingDialog.getGraphicsConfiguration() : parentFrame.getGraphicsConfiguration());
            if (monitor != null) {
                dialog.setBounds(monitor.getFullscreenBounds());
                dialog.setVisible(true);
            }
        }
    }

    private void exitFullscreenMode(ViewCanvas<E> viewCanvas, Dialog fullscreenDialog) {
        this.rebuildGridLayout();
        this.addFocusListenersToViews();
        fullscreenDialog.removeAll();
        fullscreenDialog.dispose();
        this.add((Component)this.grid, "Center");
        viewCanvas.getJComponent().requestFocusInWindow();
    }

    private void updateMigLayoutContraints(MigLayoutModel layoutModel) {
        this.migLayout.setLayoutConstraints((Object)layoutModel.getLayoutConstraints());
        this.migLayout.setColumnConstraints((Object)layoutModel.getColumnConstraints());
        this.migLayout.setRowConstraints((Object)layoutModel.getRowConstraints());
    }

    public static int getViewTypeNumber(MigLayoutModel layout) {
        int val = 0;
        if (layout != null) {
            for (MigCell cell : layout.getCells()) {
                try {
                    Class<?> clazz = Class.forName(cell.type());
                    if (!view2dClass.isAssignableFrom(clazz)) continue;
                    ++val;
                }
                catch (Exception exception) {}
            }
        }
        return val;
    }

    public void setSelectedImagePaneFromFocus(ViewCanvas<E> viewCanvas) {
        this.setSelectedImagePane(viewCanvas);
    }

    public void setSelectedImagePane(ViewCanvas<E> viewCanvas) {
        boolean newView;
        if (this.selectedImagePane != null && this.selectedImagePane.getSeries() != null) {
            this.selectedImagePane.getSeries().setSelected(false, null);
            this.selectedImagePane.getSeries().setFocused(false);
        }
        if (viewCanvas != null && viewCanvas.getSeries() != null) {
            viewCanvas.getSeries().setSelected(true, viewCanvas.getImage());
            viewCanvas.getSeries().setFocused(this.eventManager.getSelectedView2dContainer() == this);
        }
        boolean bl = newView = this.selectedImagePane != viewCanvas && viewCanvas != null;
        if (newView) {
            if (this.selectedImagePane != null) {
                this.selectedImagePane.setSelected(false);
            }
            viewCanvas.setSelected(true);
            this.selectedImagePane = viewCanvas;
            this.eventManager.updateComponentsListener(viewCanvas);
        }
        if (newView && viewCanvas.getSeries() instanceof Series) {
            this.eventManager.fireSeriesViewerListeners(new SeriesViewerEvent(this, this.selectedImagePane.getSeries(), (MediaElement)this.selectedImagePane.getImage(), SeriesViewerEvent.EVENT.SELECT));
        }
        this.eventManager.fireSeriesViewerListeners(new SeriesViewerEvent(this, viewCanvas == null ? null : viewCanvas.getSeries(), null, SeriesViewerEvent.EVENT.SELECT_VIEW));
    }

    public E getImage(int i) {
        ViewCanvas<E> viewCanvas = this.cellManager.getViewCanvasByIndex(i);
        if (viewCanvas != null) {
            return viewCanvas.getImage();
        }
        return null;
    }

    public List<ViewCanvas<E>> getImagePanels() {
        return this.getImagePanels(false);
    }

    public List<ViewCanvas<E>> getImagePanels(boolean selectedImagePaneLast) {
        ArrayList<ViewCanvas<ViewCanvas<E>>> viewList = new ArrayList<ViewCanvas<ViewCanvas<E>>>(this.cellManager.getAllViewCanvases());
        if (selectedImagePaneLast) {
            ViewCanvas<E> selectedView = this.getSelectedViewCanvas();
            if (selectedView != null && viewList.size() > 1) {
                viewList.remove(selectedView);
                viewList.add(selectedView);
            }
            return viewList;
        }
        return viewList;
    }

    public ViewCanvas<E> getNextSelectedImagePane() {
        List<ViewCanvas<E>> allViews = this.cellManager.getAllViewCanvases();
        for (int i = 0; i < allViews.size() - 1; ++i) {
            if (allViews.get(i) != this.selectedImagePane) continue;
            return allViews.get(i + 1);
        }
        return this.selectedImagePane;
    }

    public abstract List<SynchView> getSynchList();

    public abstract List<MigLayoutModel> getLayoutList();

    public List<SynchOptionsCheckBoxGroup.SyncOption> getSyncOptions() {
        return SynchOptionsCheckBoxGroup.getSyncOptions();
    }

    public boolean isAutoSyncContainerScoped() {
        return false;
    }

    public Boolean isContainingView(ViewCanvas<?> view2DPane) {
        return this.cellManager.getAllViewCanvases().stream().filter(v -> Objects.equals(v, view2DPane)).findFirst().map(viewCanvas -> Boolean.TRUE).orElse(Boolean.FALSE);
    }

    public SynchView getSynchView() {
        return this.synchView;
    }

    public void setSynchView(SynchView synchView) {
        this.synchView = Objects.requireNonNull(synchView);
        this.updateTileOffset();
        this.eventManager.updateAllListeners(this, synchView);
    }

    public void updateTileOffset() {
        boolean isTileMode = SynchData.Mode.TILE.equals((Object)this.synchView.getSynchData().getMode());
        if (isTileMode && this.selectedImagePane != null) {
            this.updateTileModeLayout();
        } else {
            this.resetTileOffsets();
        }
    }

    private void updateTileModeLayout() {
        MediaSeries<E> series = this.findAvailableSeries();
        if (series == null) {
            return;
        }
        ViewCanvas<E> selectedView = this.findViewWithSeries(series);
        int seriesLimit = this.calculateSeriesLimit(series, selectedView);
        this.distributeSeriesAcrossViews(series, seriesLimit);
    }

    private MediaSeries<E> findAvailableSeries() {
        if (this.selectedImagePane.getSeries() != null) {
            return this.selectedImagePane.getSeries();
        }
        return this.cellManager.getAllViewCanvases().stream().map(Image2DViewer::getSeries).filter(Objects::nonNull).findFirst().orElse(null);
    }

    private ViewCanvas<E> findViewWithSeries(MediaSeries<E> series) {
        return this.cellManager.getAllViewCanvases().stream().filter(v -> v.getSeries() == series).findFirst().orElse(this.selectedImagePane);
    }

    private int calculateSeriesLimit(MediaSeries<E> series, ViewCanvas<E> view) {
        return series.size((Filter)view.getActionValue(ActionW.FILTERED_SERIES.cmd()));
    }

    private void distributeSeriesAcrossViews(MediaSeries<E> series, int limit) {
        List<ViewCanvas<E>> allViews = this.cellManager.getAllViewCanvases();
        for (int i = 0; i < allViews.size(); ++i) {
            ViewCanvas<E> view = allViews.get(i);
            if (i < limit) {
                view.setTileOffset(i);
                view.setSeries(series, null);
                continue;
            }
            view.setSeries(null, null);
        }
    }

    private void resetTileOffsets() {
        this.cellManager.getAllViewCanvases().forEach(v -> v.setTileOffset(0));
    }

    public synchronized void setMouseActions(MouseActions mouseActions) {
        if (mouseActions == null) {
            for (ViewCanvas<E> v : this.cellManager) {
                v.disableMouseAndKeyListener();
                v.iniDefaultMouseListener();
            }
        } else {
            for (ViewCanvas<E> v : this.cellManager) {
                v.enableMouseAndKeyListener(mouseActions);
            }
        }
    }

    public MigLayoutModel getBestDefaultViewLayout(int size) {
        if (size <= 1) {
            return this.getDefaultLayoutModel();
        }
        return this.eventManager.getAction(ActionW.LAYOUT).map(layout -> this.findBestLayoutForSize(layout.getAllItem(), size)).orElse(this.getDefaultLayoutModel());
    }

    private MigLayoutModel findBestLayoutForSize(Object[] layouts, int targetSize) {
        record LayoutCandidate(MigLayoutModel model, int sizeDiff, int dimensionDiff) {
        }
        return Arrays.stream(layouts).filter(MigLayoutModel.class::isInstance).map(MigLayoutModel.class::cast).map(model -> {
            int layoutSize = this.getViewTypeNumber((MigLayoutModel)model, this.getSeriesViewerClass());
            int sizeDiff = Math.abs(layoutSize - targetSize);
            Dimension dim = model.getGridSize();
            int dimensionDiff = Math.abs(dim.width - dim.height);
            return new LayoutCandidate((MigLayoutModel)model, sizeDiff, dimensionDiff);
        }).filter(candidate -> {
            int layoutSize = this.getViewTypeNumber(candidate.model, this.getSeriesViewerClass());
            return layoutSize >= targetSize;
        }).min(Comparator.comparingInt(LayoutCandidate::sizeDiff).thenComparingInt(LayoutCandidate::dimensionDiff)).map(LayoutCandidate::model).orElse(this.getDefaultLayoutModel());
    }

    public static MigLayoutModel getBestDefaultViewLayout(ActionState layout, int size, MigLayoutModel defaultModel) {
        if (size <= 1) {
            return defaultModel;
        }
        if (layout instanceof ComboItemListener) {
            ComboItemListener comboItemListener = (ComboItemListener)layout;
            return ImageViewerPlugin.findBestStaticLayoutForSize(comboItemListener.getAllItem(), size, defaultModel);
        }
        return defaultModel;
    }

    private static MigLayoutModel findBestStaticLayoutForSize(Object[] layouts, int targetSize, MigLayoutModel defaultModel) {
        record LayoutCandidate(MigLayoutModel model, int sizeDiff, int dimensionDiff, boolean landscape, boolean hasComponent) {
        }
        return Arrays.stream(layouts).filter(MigLayoutModel.class::isInstance).map(MigLayoutModel.class::cast).map(model -> {
            int layoutSize = ImageViewerPlugin.getViewTypeNumber(model);
            int sizeDiff = Math.abs(layoutSize - targetSize);
            boolean hasComponent = model.getCellCount() > layoutSize;
            Dimension dim = model.getGridSize();
            int dimensionDiff = Math.abs(dim.width - dim.height);
            boolean landscape = dim.width > dim.height;
            return new LayoutCandidate((MigLayoutModel)model, sizeDiff, dimensionDiff, landscape, hasComponent);
        }).filter(candidate -> {
            int layoutSize = ImageViewerPlugin.getViewTypeNumber(candidate.model);
            return layoutSize >= targetSize && !candidate.hasComponent;
        }).min(Comparator.comparingInt(LayoutCandidate::sizeDiff).thenComparingInt(LayoutCandidate::dimensionDiff).thenComparing(LayoutCandidate::landscape)).map(LayoutCandidate::model).orElse(defaultModel);
    }

    protected static ArrayList<MigLayoutModel> getLayoutList(ImageViewerPlugin<?> viewerPlugin, List<MigLayoutModel> baseLayoutModels) {
        LayoutDimensions dims = ImageViewerPlugin.calculateLayoutDimensions(viewerPlugin, baseLayoutModels);
        ArrayList<MigLayoutModel> layouts = new ArrayList<MigLayoutModel>(baseLayoutModels);
        if (dims.shouldGenerateDynamicLayouts()) {
            ImageViewerPlugin.generateDynamicLayouts(layouts, dims);
        }
        layouts.sort(Comparator.comparingInt(MigLayoutModel::getCellCount));
        return layouts;
    }

    private static LayoutDimensions calculateLayoutDimensions(ImageViewerPlugin<?> viewerPlugin, List<MigLayoutModel> baseModels) {
        int height;
        int width = viewerPlugin.getWidth();
        double ratio = (double)width / (double)(height = viewerPlugin.getHeight());
        int cols = ratio >= 1.0 ? (int)Math.round(ratio * 1.5) : 1;
        int rows = ratio < 1.0 ? (int)Math.round(1.0 / ratio * 1.5) : 1;
        int minCols = cols;
        int minRows = rows;
        for (MigLayoutModel model : baseModels) {
            Dimension dim = model.getGridSize();
            minCols = Math.max(minCols, dim.width);
            minRows = Math.max(minRows, dim.height);
        }
        int factorLimit = cols == 1 ? (int)Math.round((double)width / 512.0) : (int)Math.round((double)height / 512.0);
        factorLimit = Math.max(1, factorLimit);
        return new LayoutDimensions(cols, rows, minCols, minRows, factorLimit);
    }

    private static void generateDynamicLayouts(ArrayList<MigLayoutModel> layouts, LayoutDimensions dims) {
        if (dims.cols > dims.rows) {
            int step = 1 + dims.cols / 20;
            for (int i = dims.cols / 2; i < dims.cols; i += step) {
                ImageViewerPlugin.addDynamicLayout(layouts, dims.factorLimit, i, dims.rows, dims.minCols, dims.minRows);
            }
        } else {
            int step = 1 + dims.rows / 20;
            for (int i = dims.rows / 2; i < dims.rows; i += step) {
                ImageViewerPlugin.addDynamicLayout(layouts, dims.factorLimit, dims.cols, i, dims.minCols, dims.minRows);
            }
        }
        ImageViewerPlugin.addDynamicLayout(layouts, dims.factorLimit, dims.cols, dims.rows, dims.minCols, dims.minRows);
    }

    private static void addDynamicLayout(List<MigLayoutModel> layouts, int factorLimit, int cols, int rows, int minCols, int minRows) {
        for (int factor = 1; factor <= factorLimit; ++factor) {
            if (factor <= 2 && factor * rows <= minRows && factor * cols <= minCols) continue;
            int scaledRows = factor * rows;
            int scaledCols = factor * cols;
            if (scaledRows >= 50 || scaledCols >= 50) continue;
            layouts.add(ImageViewerPlugin.buildMigLayoutModel(scaledRows, scaledCols, view2dClass.getName()));
        }
    }

    public MigLayoutModel getViewLayout(String title) {
        Optional<MigLayoutModel> layout;
        if (title != null && (layout = this.eventManager.getAction(ActionW.LAYOUT)).isPresent()) {
            Object[] list;
            for (Object m : list = ((ComboItemListener)((Object)layout.get())).getAllItem()) {
                MigLayoutModel model;
                if (!(m instanceof MigLayoutModel) || !title.equals((model = (MigLayoutModel)m).getId())) continue;
                return model;
            }
        }
        return VIEWS_1x1;
    }

    public void addSeriesList(List<MediaSeries<E>> seriesList, boolean bestDefaultLayout) {
        if (seriesList == null || seriesList.isEmpty()) {
            return;
        }
        if (SynchData.Mode.TILE.equals((Object)this.synchView.getSynchData().getMode())) {
            this.addSeries((MediaSeries)seriesList.getFirst());
            return;
        }
        this.setSelectedAndGetFocus();
        if (bestDefaultLayout) {
            this.addSeriesWithBestLayout(seriesList);
        } else {
            this.addSeriesToAvailableSlots(seriesList);
        }
        this.repaint();
    }

    private void addSeriesWithBestLayout(List<MediaSeries<E>> seriesList) {
        this.changeLayoutModel(this.getBestDefaultViewLayout(seriesList.size()));
        List<ViewCanvas<E>> allViews = this.cellManager.getAllViewCanvases();
        this.clearExcessViews(allViews, seriesList.size());
        if (!allViews.isEmpty()) {
            this.setSelectedImagePane((ViewCanvas)allViews.getFirst());
            seriesList.forEach(this::addSeries);
        }
    }

    private void clearExcessViews(List<ViewCanvas<E>> views, int requiredCount) {
        if (views.size() > requiredCount) {
            this.setSelectedImagePane(views.get(requiredCount));
            for (int i = requiredCount; i < views.size(); ++i) {
                ViewCanvas<Object> viewPane = this.getSelectedViewCanvas();
                if (viewPane != null) {
                    viewPane.setSeries(null, null);
                }
                this.getNextSelectedImagePane();
            }
        }
    }

    private void addSeriesToAvailableSlots(List<MediaSeries<E>> seriesList) {
        int emptyViewCount = this.countEmptyViews();
        List<ViewCanvas<E>> allViews = this.cellManager.getAllViewCanvases();
        if (emptyViewCount < seriesList.size()) {
            int totalNeeded = allViews.size() + seriesList.size();
            this.changeLayoutModel(this.getBestDefaultViewLayout(totalNeeded));
        }
        int seriesIndex = 0;
        for (ViewCanvas<E> view : this.cellManager) {
            if (view.getSeries() != null || seriesIndex >= seriesList.size()) continue;
            this.setSelectedImagePane(view);
            this.addSeries(seriesList.get(seriesIndex++));
        }
    }

    private int countEmptyViews() {
        return (int)this.cellManager.getAllViewCanvases().stream().filter(v -> v.getSeries() == null).count();
    }

    public void selectLayoutPositionForAddingSeries(List<MediaSeries<E>> seriesList) {
        List<ViewCanvas<E>> allViews;
        int pos;
        int nbSeriesToAdd = 1;
        if (seriesList != null && (nbSeriesToAdd = seriesList.size()) < 1) {
            nbSeriesToAdd = 1;
        }
        if ((pos = (allViews = this.cellManager.getAllViewCanvases()).size() - nbSeriesToAdd) < 0) {
            pos = 0;
        }
        this.setSelectedImagePane(allViews.get(pos));
    }

    public record LayoutModel(String uid, MigLayoutModel model) {
    }

    private record LayoutDimensions(int cols, int rows, int minCols, int minRows, int factorLimit) {
        boolean shouldGenerateDynamicLayouts() {
            return this.cols != this.rows && this.cols != 0 && this.rows != 0;
        }
    }
}

