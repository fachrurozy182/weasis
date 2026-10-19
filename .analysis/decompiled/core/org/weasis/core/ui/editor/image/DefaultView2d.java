/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.CvType
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.LangUtil
 *  org.weasis.core.util.MathUtil
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.lut.ColorLut
 */
package org.weasis.core.ui.editor.image;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import org.opencv.core.CvType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.Image2DViewer;
import org.weasis.core.api.gui.model.ViewModel;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.ComboItemListener;
import org.weasis.core.api.gui.util.Feature;
import org.weasis.core.api.gui.util.Filter;
import org.weasis.core.api.gui.util.GeomUtil;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.SliderCineListener;
import org.weasis.core.api.image.AffineTransformOp;
import org.weasis.core.api.image.FilterOp;
import org.weasis.core.api.image.ImageOpEvent;
import org.weasis.core.api.image.ImageOpNode;
import org.weasis.core.api.image.OpManager;
import org.weasis.core.api.image.PseudoColorOp;
import org.weasis.core.api.image.SimpleOpManager;
import org.weasis.core.api.image.WindowOp;
import org.weasis.core.api.image.ZoomOp;
import org.weasis.core.api.image.util.KernelData;
import org.weasis.core.api.image.util.MeasurableLayer;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.SeriesComparator;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.service.AuditLog;
import org.weasis.core.api.util.FontItem;
import org.weasis.core.ui.docking.DockableTool;
import org.weasis.core.ui.editor.image.FocusHandler;
import org.weasis.core.ui.editor.image.FrameOfReferenceColor;
import org.weasis.core.ui.editor.image.GraphicMouseHandler;
import org.weasis.core.ui.editor.image.GraphicsPane;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.ManualSynchViewButton;
import org.weasis.core.ui.editor.image.Panner;
import org.weasis.core.ui.editor.image.PixelInfo;
import org.weasis.core.ui.editor.image.ScreenshotDialog;
import org.weasis.core.ui.editor.image.SynchCineEvent;
import org.weasis.core.ui.editor.image.SynchData;
import org.weasis.core.ui.editor.image.SynchEvent;
import org.weasis.core.ui.editor.image.SynchOptionsCheckBoxGroup;
import org.weasis.core.ui.editor.image.SynchView;
import org.weasis.core.ui.editor.image.SynchViewButton;
import org.weasis.core.ui.editor.image.ViewButton;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.editor.image.ViewProgress;
import org.weasis.core.ui.editor.image.ViewSynchData;
import org.weasis.core.ui.editor.image.ViewerPlugin;
import org.weasis.core.ui.editor.image.ZoomWin;
import org.weasis.core.ui.editor.image.dockable.MeasureTool;
import org.weasis.core.ui.model.AbstractGraphicModel;
import org.weasis.core.ui.model.GraphicModel;
import org.weasis.core.ui.model.graphic.DragGraphic;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.graphic.GraphicSelectionListener;
import org.weasis.core.ui.model.graphic.imp.seg.SegGraphic;
import org.weasis.core.ui.model.graphic.imp.seg.SegRegion;
import org.weasis.core.ui.model.imp.XmlGraphicModel;
import org.weasis.core.ui.model.layer.LayerAnnotation;
import org.weasis.core.ui.model.layer.LayerType;
import org.weasis.core.ui.model.layer.imp.RenderedImageLayer;
import org.weasis.core.ui.model.utils.Draggable;
import org.weasis.core.ui.model.utils.bean.GraphicClipboard;
import org.weasis.core.ui.model.utils.bean.PanPoint;
import org.weasis.core.ui.model.utils.imp.DefaultViewModel;
import org.weasis.core.ui.pref.Monitor;
import org.weasis.core.ui.util.DefaultAction;
import org.weasis.core.ui.util.MouseEventDouble;
import org.weasis.core.ui.util.TitleMenuItem;
import org.weasis.core.util.LangUtil;
import org.weasis.core.util.MathUtil;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.lut.ColorLut;

public abstract class DefaultView2d<E extends ImageElement>
extends GraphicsPane
implements ViewCanvas<E>,
ViewProgress {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultView2d.class);
    public static final int MINIMAL_IMAGES_FOR_3D = 5;
    private static final float SEG_HOVER_TOLERANCE = 4.0f;
    public static final GraphicClipboard GRAPHIC_CLIPBOARD = new GraphicClipboard();
    public static final Cursor EDIT_CURSOR = Feature.getImageCursor("editPoint.png", "Edit Point", 0.5f, 0.5f);
    public static final Cursor HAND_CURSOR = Feature.getSvgCursor("hand.svg", "hand", 0.5f, 0.5f);
    public static final Cursor WAIT_CURSOR = DefaultView2d.getNewCursor(3);
    public static final Cursor CROSS_CURSOR = DefaultView2d.getNewCursor(1);
    public static final Cursor MOVE_CURSOR = DefaultView2d.getNewCursor(13);
    public static final Cursor DEFAULT_CURSOR = DefaultView2d.getNewCursor(0);
    protected final FocusHandler<E> focusHandler;
    protected final GraphicMouseHandler<E> graphicMouseHandler;
    private final SegHoverHandler segHoverHandler = new SegHoverHandler(this);
    private SegRegion<?> highlightedSegRegion;
    private JProgressBar progressBar;
    private final PanPoint highlightedPosition = new PanPoint(PanPoint.State.CENTER);
    private final PanPoint startedDragPoint = new PanPoint(PanPoint.State.DRAGSTART);
    private int pointerType = 0;
    protected final RenderedImageLayer<E> imageLayer;
    private E pinnedImage;
    protected Panner<E> panner;
    protected ZoomWin<E> lens;
    private final List<ViewButton> viewButtons;
    protected SynchViewButton synchButton;
    protected ManualSynchViewButton manualSynchButton;
    protected MediaSeries<E> series = null;
    protected LayerAnnotation<E> infoLayer;
    protected int tileOffset;
    protected final ImageViewerEventManager<E> eventManager;

    protected DefaultView2d(ImageViewerEventManager<E> eventManager) {
        this(eventManager, null);
    }

    protected DefaultView2d(ImageViewerEventManager<E> eventManager, ViewModel viewModel) {
        super(viewModel);
        this.eventManager = Objects.requireNonNull(eventManager);
        this.viewButtons = new ArrayList<ViewButton>();
        this.tileOffset = 0;
        this.imageLayer = new RenderedImageLayer();
        this.actionsInView.put(ActionW.LENS.cmd(), false);
        this.initActionWState();
        this.graphicMouseHandler = new GraphicMouseHandler(this);
        this.focusHandler = new FocusHandler(this);
        this.setBorder(viewBorder);
        this.setFocusable(true);
        this.setPreferredSize(new Dimension(4096, 4096));
        this.setMinimumSize(new Dimension(50, 50));
    }

    @Override
    public void registerDefaultListeners() {
        this.addFocusListener(this);
        ToolTipManager.sharedInstance().registerComponent(this);
        this.imageLayer.addLayerChangeListener(this);
    }

    protected void buildPanner() {
        this.panner = Optional.ofNullable(this.panner).orElseGet(() -> new Panner(this));
    }

    @Override
    public void copyActionWState(HashMap<String, Object> actionsInView) {
        actionsInView.putAll(this.actionsInView);
    }

    protected void initActionWState() {
        E img = this.getImage();
        this.actionsInView.put(ActionW.SPATIAL_UNIT.cmd(), img == null ? Unit.PIXEL : ((ImageElement)img).getPixelSpacingUnit());
        this.actionsInView.put("zoom.type", ZoomType.BEST_FIT);
        this.actionsInView.put(ActionW.ZOOM.cmd(), 0.0);
        this.actionsInView.put(ActionW.DRAWINGS.cmd(), true);
        this.actionsInView.put(LayerType.CROSSLINES.name(), true);
        this.actionsInView.put(ActionW.INVERSE_STACK.cmd(), false);
        this.actionsInView.put(ActionW.FILTERED_SERIES.cmd(), null);
        this.actionsInView.put(ActionW.FLIP.cmd(), false);
        this.actionsInView.put(ActionW.ROTATION.cmd(), 0);
        OpManager disOp = this.getDisplayOpManager();
        disOp.setParamValue(WindowOp.OP_NAME, "weasis.color.wl.apply", this.eventManager.getOptions().getBooleanProperty("weasis.color.wl.apply", true));
        disOp.setParamValue(AffineTransformOp.OP_NAME, "interpolation", (Object)ZoomOp.Interpolation.fromPosition(this.eventManager.getZoomSetting().getInterpolation()));
        disOp.setParamValue(AffineTransformOp.OP_NAME, "affine.matrix", null);
        disOp.setParamValue(FilterOp.OP_NAME, "kernel", KernelData.NONE);
        disOp.setParamValue(PseudoColorOp.OP_NAME, PseudoColorOp.P_LUT, ColorLut.IMAGE.getByteLut());
        disOp.setParamValue(PseudoColorOp.OP_NAME, PseudoColorOp.P_LUT_INVERSE, false);
    }

    @Override
    public ImageViewerEventManager<E> getEventManager() {
        return this.eventManager;
    }

    @Override
    public void updateSynchState() {
        ViewSynchData synchData = (ViewSynchData)this.actionsInView.get(ActionW.SYNCH_LINK.cmd());
        this.ensureSynchButtons();
        if (synchData == null) {
            this.hideSyncButtons();
            this.repaint();
            return;
        }
        this.renderAutoSyncButton(synchData);
        this.renderManualSyncButton(synchData);
        this.repaint();
    }

    private void ensureSynchButtons() {
        if (this.synchButton == null) {
            this.synchButton = new SynchViewButton(this::showPerViewSyncPopup);
            this.synchButton.setPosition(14);
            if (!this.getViewButtons().contains(this.synchButton)) {
                this.getViewButtons().add(this.synchButton);
            }
        }
        if (this.manualSynchButton == null) {
            this.manualSynchButton = new ManualSynchViewButton(this::onManualSyncButtonClicked);
            this.manualSynchButton.setPosition(14);
            if (!this.getViewButtons().contains(this.manualSynchButton)) {
                this.getViewButtons().add(this.manualSynchButton);
            }
        }
    }

    private void showPerViewSyncPopup(Component invoker, int x, int y) {
        ViewSynchData sd = (ViewSynchData)this.actionsInView.get(ActionW.SYNCH_LINK.cmd());
        if (sd == null) {
            this.onAutoSyncButtonClicked();
            return;
        }
        boolean manualSyncOn = sd.isManualSynchActivated();
        if (manualSyncOn) {
            sd.setUserActionOverride(ActionW.SCROLL_SERIES.cmd(), true);
        }
        HashMap<String, Boolean> effective = new HashMap<String, Boolean>(sd.getActions());
        effective.putAll(sd.getUserActionOverrides());
        ImageViewerPlugin<E> container = this.eventManager.getSelectedView2dContainer();
        SynchOptionsCheckBoxGroup group = new SynchOptionsCheckBoxGroup(container != null ? container.getSyncOptions() : SynchOptionsCheckBoxGroup.getSyncOptions());
        JPopupMenu popup = group.buildPopupMenu(effective, (cmd, selected) -> {
            if (sd.isManualSynchActivated() && ActionW.SCROLL_SERIES.cmd().equals(cmd) && !selected.booleanValue()) {
                return;
            }
            sd.setUserActionOverride((String)cmd, (boolean)selected);
            sd.setOriginal(false);
            this.getJComponent().repaint();
        });
        if (manualSyncOn) {
            for (Component c : popup.getComponents()) {
                if (!(c instanceof JCheckBoxMenuItem)) continue;
                JCheckBoxMenuItem item = (JCheckBoxMenuItem)c;
                if (!ActionW.SCROLL_SERIES.cmd().equals(item.getActionCommand())) continue;
                item.setSelected(true);
                item.setEnabled(false);
                break;
            }
        }
        JCheckBoxMenuItem syncToggle = new JCheckBoxMenuItem(Messages.getString("ViewerToolBar.synch_this_view"), sd.isAutoSynchActivated());
        syncToggle.setToolTipText(Messages.getString("ViewerToolBar.synch_this_view_tooltip"));
        syncToggle.addActionListener(e -> this.onAutoSyncButtonClicked());
        popup.insert(syncToggle, 0);
        popup.insert(new JPopupMenu.Separator(), 1);
        popup.addSeparator();
        JMenuItem applyToAll = new JMenuItem(Messages.getString("ViewerToolBar.synch_apply_to_all"));
        applyToAll.setIcon(FrameOfReferenceColor.swatch(this.resolveChipColor(sd.getFrameOfReferenceUID()), GuiUtils.getScaleLength(12)));
        applyToAll.addActionListener(e -> this.applySyncOptionsToOtherSynchronizedViews(sd));
        popup.add(applyToAll);
        popup.addSeparator();
        JMenuItem closeItem = new JMenuItem(Messages.getString("AbstractWizardDialog.close"));
        closeItem.addActionListener(e -> popup.setVisible(false));
        popup.add(closeItem);
        popup.show(invoker, x, y);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void applySyncOptionsToOtherSynchronizedViews(ViewSynchData source) {
        List<ViewerPlugin<?>> plugins;
        if (source == null) {
            return;
        }
        String sourceFruid = source.getFrameOfReferenceUID();
        if (sourceFruid == null) {
            return;
        }
        HashMap<String, Boolean> effective = new HashMap<String, Boolean>(source.getActions());
        effective.putAll(source.getUserActionOverrides());
        List<ViewerPlugin<?>> list = plugins = GuiUtils.getUICore().getViewerPlugins();
        synchronized (list) {
            for (ViewerPlugin<?> p : plugins) {
                if (!(p instanceof ImageViewerPlugin)) continue;
                ImageViewerPlugin ivp = (ImageViewerPlugin)p;
                this.applyOverridesInContainer(ivp, sourceFruid, effective);
            }
        }
    }

    private void applyOverridesInContainer(ImageViewerPlugin<?> container, String sourceFruid, Map<String, Boolean> effective) {
        List<String> managedCommands = SynchOptionsCheckBoxGroup.getAllManagedCommands(container.getSyncOptions());
        for (ViewCanvas<?> v : container.getImagePanels()) {
            ViewSynchData target;
            Iterator<String> iterator;
            if (v == this || !((iterator = v.getActionValue(ActionW.SYNCH_LINK.cmd())) instanceof ViewSynchData) || !sourceFruid.equals((target = (ViewSynchData)((Object)iterator)).getFrameOfReferenceUID())) continue;
            target.setOriginal(false);
            iterator = managedCommands.iterator();
            while (iterator.hasNext()) {
                String cmd = iterator.next();
                if (target.isManualSynchActivated() && ActionW.SCROLL_SERIES.cmd().equals(cmd)) {
                    target.setUserActionOverride(cmd, true);
                    continue;
                }
                target.setUserActionOverride(cmd, Boolean.TRUE.equals(effective.get(cmd)));
            }
            v.updateSynchState();
        }
    }

    private void hideSyncButtons() {
        if (this.synchButton != null) {
            this.synchButton.setState(SynchData.SyncState.OFF);
            this.synchButton.setVisible(false);
        }
        if (this.manualSynchButton != null) {
            this.manualSynchButton.setState(SynchData.SyncState.OFF);
            this.manualSynchButton.setVisible(false);
        }
    }

    private void renderAutoSyncButton(ViewSynchData synchData) {
        if (synchData.getMode() == SynchData.Mode.TILE) {
            this.synchButton.setVisible(false);
            return;
        }
        boolean activated = synchData.getAutoSyncState() == SynchData.SyncState.ON;
        this.synchButton.setState(activated ? SynchData.SyncState.ON : SynchData.SyncState.OFF);
        this.synchButton.setChipColor(this.resolveChipColor(synchData.getFrameOfReferenceUID()));
        this.synchButton.setVisible(activated || synchData.canBeAutoSynced());
    }

    private Color resolveChipColor(String fruid) {
        if (fruid == null || fruid.isBlank()) {
            return null;
        }
        ImageViewerPlugin container = (ImageViewerPlugin)SwingUtilities.getAncestorOfClass(ImageViewerPlugin.class, this.getJComponent());
        if (container == null) {
            return null;
        }
        if (DefaultView2d.collectDistinctFrameOfReferenceUIDs(container).size() < 2) {
            return null;
        }
        return FrameOfReferenceColor.colorFor(fruid, DefaultView2d.collectGlobalDistinctFrameOfReferenceUIDs());
    }

    private static LinkedHashSet<String> collectDistinctFrameOfReferenceUIDs(ImageViewerPlugin<?> container) {
        LinkedHashSet<String> distinct = new LinkedHashSet<String>();
        for (ViewCanvas<?> v : container.getImagePanels()) {
            ViewSynchData vsd;
            String uid;
            Object object = v.getActionValue(ActionW.SYNCH_LINK.cmd());
            if (!(object instanceof ViewSynchData) || (uid = (vsd = (ViewSynchData)object).getFrameOfReferenceUID()) == null || uid.isBlank()) continue;
            distinct.add(uid);
        }
        return distinct;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static LinkedHashSet<String> collectGlobalDistinctFrameOfReferenceUIDs() {
        List<ViewerPlugin<?>> plugins;
        LinkedHashSet<String> distinct = new LinkedHashSet<String>();
        List<ViewerPlugin<?>> list = plugins = GuiUtils.getUICore().getViewerPlugins();
        synchronized (list) {
            for (ViewerPlugin<?> p : plugins) {
                if (!(p instanceof ImageViewerPlugin)) continue;
                ImageViewerPlugin ivp = (ImageViewerPlugin)p;
                distinct.addAll(DefaultView2d.collectDistinctFrameOfReferenceUIDs(ivp));
            }
        }
        return distinct;
    }

    protected JMenu buildSynchOptionsMenu(String title) {
        ViewSynchData sd = (ViewSynchData)this.actionsInView.get(ActionW.SYNCH_LINK.cmd());
        if (sd == null) {
            return null;
        }
        boolean manualSyncOn = sd.isManualSynchActivated();
        if (manualSyncOn) {
            sd.setUserActionOverride(ActionW.SCROLL_SERIES.cmd(), true);
        }
        HashMap<String, Boolean> effective = new HashMap<String, Boolean>(sd.getActions());
        effective.putAll(sd.getUserActionOverrides());
        JMenu menu = new JMenu(title);
        new SynchOptionsCheckBoxGroup().addStayOpenItemsTo(menu, effective, (cmd, selected) -> {
            if (sd.isManualSynchActivated() && ActionW.SCROLL_SERIES.cmd().equals(cmd) && !selected.booleanValue()) {
                return;
            }
            sd.setUserActionOverride((String)cmd, (boolean)selected);
            sd.setOriginal(false);
            this.getJComponent().repaint();
        });
        if (manualSyncOn) {
            for (Component c : menu.getMenuComponents()) {
                if (!(c instanceof JCheckBoxMenuItem)) continue;
                JCheckBoxMenuItem item = (JCheckBoxMenuItem)c;
                if (!ActionW.SCROLL_SERIES.cmd().equals(item.getActionCommand())) continue;
                item.setSelected(true);
                item.setEnabled(false);
                break;
            }
        }
        menu.addSeparator();
        JMenuItem applyToAll = new JMenuItem(Messages.getString("ViewerToolBar.synch_apply_to_all"));
        applyToAll.setIcon(FrameOfReferenceColor.swatch(this.resolveChipColor(sd.getFrameOfReferenceUID()), GuiUtils.getScaleLength(12)));
        applyToAll.addActionListener(e -> this.applySyncOptionsToOtherSynchronizedViews(sd));
        menu.add(applyToAll);
        menu.addSeparator();
        JMenuItem closeItem = new JMenuItem(Messages.getString("AbstractWizardDialog.close"));
        closeItem.addActionListener(e -> MenuSelectionManager.defaultManager().clearSelectedPath());
        menu.add(closeItem);
        return menu;
    }

    private void renderManualSyncButton(ViewSynchData synchData) {
        if (synchData.getMode() == SynchData.Mode.TILE) {
            this.manualSynchButton.setVisible(false);
            return;
        }
        if (synchData.canBeManuallySynced() || synchData.isManualSynchActivated()) {
            this.manualSynchButton.setVisible(true);
            this.manualSynchButton.setState(synchData.isManualSynchActivated() ? SynchData.SyncState.ON : SynchData.SyncState.OFF);
        } else {
            this.manualSynchButton.setVisible(false);
        }
    }

    private void onAutoSyncButtonClicked() {
        SynchData sd = (SynchData)this.actionsInView.get(ActionW.SYNCH_LINK.cmd());
        if (sd == null) {
            return;
        }
        sd.setOriginal(false);
        if (this.synchButton.getState() == SynchData.SyncState.OFF) {
            sd.setAutoSyncState(SynchData.SyncState.ON);
            this.synchButton.setState(SynchData.SyncState.ON);
        } else {
            sd.setAutoSyncState(SynchData.SyncState.OFF);
            this.synchButton.setState(SynchData.SyncState.OFF);
        }
        this.refreshAllListeners();
    }

    private void onManualSyncButtonClicked(Component invoker, int x, int y) {
        ViewSynchData currentSynchData = (ViewSynchData)this.actionsInView.get(ActionW.SYNCH_LINK.cmd());
        if (currentSynchData == null) {
            return;
        }
        if (this.manualSynchButton.getState() == SynchData.SyncState.OFF) {
            this.activateManualSync(invoker, x, y);
        } else {
            this.deactivateManualSync(currentSynchData);
        }
        this.refreshAllListeners();
    }

    private void refreshAllListeners() {
        SynchView sv;
        Object object;
        ComboItemListener synchAction = this.eventManager.getAction(ActionW.SYNCH).orElse(null);
        SynchView selected = synchAction != null && (object = synchAction.getSelectedItem()) instanceof SynchView ? (sv = (SynchView)object) : SynchView.DEFAULT_STACK;
        this.eventManager.synchManager.updateAllListeners(this.eventManager.getSelectedView2dContainer(), selected);
    }

    private void activateManualSync(Component invoker, int x, int y) {
        ImageViewerPlugin<E> container = this.eventManager.getSelectedView2dContainer();
        if (container == null) {
            return;
        }
        ViewCanvas<E> pane = container.getSelectedViewCanvas();
        if (pane == null || pane.getSeries() == null) {
            return;
        }
        List candidates = container.getImagePanels().stream().filter(view -> view != null && view != pane && view.getSeries() != null && this.eventManager.synchManager.hasSameOrientation(view.getSeries(), pane.getSeries()) && !this.eventManager.synchManager.hasSameFrUid(pane.getSeries(), view.getSeries())).toList();
        if (candidates.isEmpty()) {
            return;
        }
        SynchData globalSynchData = this.getGlobalSynchData();
        if (globalSynchData != null && globalSynchData.isManualSynchActivated()) {
            LinkedHashSet existingGroup = new LinkedHashSet();
            for (ViewCanvas viewCanvas : candidates) {
                ViewSynchData sd = (ViewSynchData)viewCanvas.getActionsInView().get(ActionW.SYNCH_LINK.cmd());
                if (sd == null || !sd.isManualSynchActivated()) continue;
                existingGroup.add(viewCanvas);
                for (ViewSynchData.ManualSyncData msd : sd.getManualSyncDataSet()) {
                    existingGroup.add(msd.getTargetPane());
                }
            }
            existingGroup.remove(pane);
            for (ViewCanvas viewCanvas : existingGroup) {
                this.setupManualSync(pane, viewCanvas);
            }
        } else if (candidates.size() == 1) {
            this.setupManualSync(pane, (ViewCanvas)candidates.getFirst());
        } else {
            JPopupMenu popupMenu = new JPopupMenu();
            this.addOrphanSeriesSelectionMenu(popupMenu, pane, candidates);
            popupMenu.show(invoker, x, y);
        }
    }

    private void deactivateManualSync(ViewSynchData currentSynchData) {
        currentSynchData.setManualSyncState(SynchData.SyncState.OFF);
        for (ViewSynchData.ManualSyncData msd : new HashSet<ViewSynchData.ManualSyncData>(currentSynchData.getManualSyncDataSet())) {
            ViewCanvas<?> targetPane = msd.getTargetPane();
            ViewSynchData targetSd = (ViewSynchData)targetPane.getActionsInView().get(ActionW.SYNCH_LINK.cmd());
            if (targetSd == null) continue;
            targetSd.getManualSyncDataSet().removeIf(d -> d.getTargetPane() == this);
            if (!targetSd.getManualSyncDataSet().isEmpty()) continue;
            targetSd.setManualSyncState(SynchData.SyncState.OFF);
            this.eventManager.removePropertyChangeListener(ActionW.SYNCH.cmd(), targetPane);
        }
        currentSynchData.clearManualSyncData();
        this.eventManager.removePropertyChangeListener(ActionW.SYNCH.cmd(), this);
        SynchData globalSd = this.getGlobalSynchData();
        if (globalSd != null) {
            boolean anyRemains;
            ImageViewerPlugin<E> container = this.eventManager.getSelectedView2dContainer();
            boolean bl = anyRemains = container != null && container.getImagePanels().stream().anyMatch(v -> {
                ViewSynchData sd = (ViewSynchData)v.getActionsInView().get(ActionW.SYNCH_LINK.cmd());
                return sd != null && sd.isManualSynchActivated();
            });
            if (!anyRemains) {
                globalSd.setManualSyncState(SynchData.SyncState.OFF);
            }
        }
    }

    private void setupManualSync(ViewCanvas<?> source, ViewCanvas<?> target) {
        if (source == target) {
            return;
        }
        ViewSynchData synchDataSource = (ViewSynchData)source.getActionsInView().get(ActionW.SYNCH_LINK.cmd());
        ViewSynchData synchDataTarget = (ViewSynchData)target.getActionsInView().get(ActionW.SYNCH_LINK.cmd());
        if (synchDataSource == null || synchDataTarget == null) {
            return;
        }
        if (synchDataSource.getManualSyncDataByPane(target) != null) {
            return;
        }
        Double sourcePosition = (Double)((MediaElement)source.getImage()).getTagValue(TagW.SlicePosition);
        Double targetPosition = (Double)((MediaElement)target.getImage()).getTagValue(TagW.SlicePosition);
        if (sourcePosition == null || targetPosition == null) {
            LOGGER.warn("Cannot set up manual sync: SlicePosition tag is missing on one of the views");
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this.getJComponent()), Messages.getString("DefaultView2d.manual_sync_no_position"), Messages.getString("DefaultView2d.manual_sync_no_position_title"), 2);
            return;
        }
        synchDataSource.setManualSyncState(SynchData.SyncState.ON);
        synchDataTarget.setManualSyncState(SynchData.SyncState.ON);
        synchDataSource.setOrphan(false);
        synchDataTarget.setOrphan(false);
        synchDataSource.setOriginal(false);
        synchDataTarget.setOriginal(false);
        synchDataSource.addManualSyncData(sourcePosition, targetPosition, target);
        synchDataTarget.addManualSyncData(targetPosition, sourcePosition, source);
        source.setActionsInView(ActionW.SYNCH_LINK.cmd(), synchDataSource);
        target.setActionsInView(ActionW.SYNCH_LINK.cmd(), synchDataTarget);
        this.eventManager.addPropertyChangeListener(ActionW.SYNCH.cmd(), source);
        this.eventManager.addPropertyChangeListener(ActionW.SYNCH.cmd(), target);
        this.eventManager.synchManager.applyManualSync();
    }

    private void addOrphanSeriesSelectionMenu(JPopupMenu popupMenu, ViewCanvas<E> pane, List<ViewCanvas<E>> list) {
        if (list.isEmpty()) {
            return;
        }
        popupMenu.add(new TitleMenuItem(Messages.getString("DefaultView2d.select_series_to_sync") + ":"));
        popupMenu.addSeparator();
        for (ViewCanvas<E> entry : list) {
            MediaSeries entrySeries = entry.getSeries();
            JMenuItem menuItem = new JMenuItem(entrySeries.toString() + " -- " + entrySeries.size(null));
            menuItem.addActionListener(e -> {
                this.setupManualSync(pane, entry);
                this.refreshAllListeners();
            });
            popupMenu.add(menuItem);
        }
    }

    protected PlanarImage getPreprocessedImage(E imageElement) {
        return ((ImageElement)imageElement).getImage((OpManager)this.actionsInView.get(ActionW.PREPROCESSING.cmd()));
    }

    protected void fillPixelInfo(PixelInfo pixelInfo, E imageElement, double[] c) {
        if (c != null && c.length > 0) {
            pixelInfo.setValues(c);
        }
    }

    @Override
    public PixelInfo getPixelInfo(Point p) {
        PixelInfo pixelInfo = new PixelInfo();
        E imageElement = this.imageLayer.getSourceImage();
        PlanarImage image = this.imageLayer.getSourceRenderedImage();
        if (imageElement != null && image != null) {
            Rectangle2D area = this.viewModel.getModelArea();
            Point offset = ((RenderedImageLayer)this.getImageLayer()).getOffset();
            if (offset != null) {
                area.setRect(offset.getX(), offset.getY(), area.getWidth(), area.getHeight());
                p.translate(-((int)area.getX()), -((int)area.getY()));
            }
            if (area.contains(p)) {
                try {
                    pixelInfo.setPosition(new Point(p.x, p.y));
                    pixelInfo.setPixelSpacingUnit(((ImageElement)imageElement).getPixelSpacingUnit());
                    pixelInfo.setPixelSize(((ImageElement)imageElement).getPixelSize());
                    double[] c = image.get(p.y, p.x);
                    pixelInfo.setPixelValueUnit(((ImageElement)imageElement).getPixelValueUnit());
                    this.fillPixelInfo(pixelInfo, imageElement, c);
                    if (c != null && c.length >= 1) {
                        pixelInfo.setChannelNames(DefaultView2d.getChannelNames(image));
                    }
                }
                catch (Exception e) {
                    LOGGER.error("Get pixel value", (Throwable)e);
                }
            }
        }
        return pixelInfo;
    }

    protected static String[] getChannelNames(PlanarImage image) {
        if (image != null) {
            int channels = CvType.channels((int)image.type());
            if (channels == 3) {
                return new String[]{Messages.getString("DefaultView2d.blue"), Messages.getString("DefaultView2d.green"), Messages.getString("DefaultView2d.red")};
            }
            if (channels == 1) {
                return new String[]{Messages.getString("DefaultView2d.gray")};
            }
        }
        return null;
    }

    @Override
    public Panner getPanner() {
        return this.panner;
    }

    @Override
    public void closeLens() {
        if (this.lens != null) {
            this.lens.showLens(false);
            this.remove(this.lens);
            this.actionsInView.put(ActionW.LENS.cmd(), false);
            this.lens = null;
        }
    }

    @Override
    public void setSeries(MediaSeries<E> series) {
        this.setSeries(series, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setSeries(MediaSeries<E> newSeries, E selectedMedia) {
        MediaSeries<E> oldSequence = this.series;
        this.series = newSeries;
        if (oldSequence == null && newSeries == null) {
            return;
        }
        if (oldSequence != null && oldSequence.equals(newSeries) && this.imageLayer.getSourceImage() != null) {
            if (this.eventManager.isActionRegistered(ActionW.VOLUME) && !this.eventManager.isActionEnabled(ActionW.VOLUME) && newSeries.size(null) >= 5) {
                this.eventManager.updateComponentsListener(this);
            }
            return;
        }
        GuiUtils.getUICore().closeSeries(oldSequence);
        this.initActionWState();
        try {
            if (newSeries == null) {
                this.setImage(null);
                this.resetSynchState();
            } else {
                SynchView sel;
                Object object;
                Object media = selectedMedia;
                if (selectedMedia == null) {
                    media = (ImageElement)newSeries.getMedia(Math.max(this.tileOffset, 0), (Filter)this.actionsInView.get(ActionW.FILTERED_SERIES.cmd()), this.getCurrentSortComparator());
                }
                this.imageLayer.fireOpEvent(new ImageOpEvent(ImageOpEvent.OpEvent.SERIES_CHANGE, (MediaSeries<? extends ImageElement>)this.series, (ImageElement)media, null));
                if (this.lens != null) {
                    this.lens.setFreezeImage(null);
                }
                this.setImage(media);
                ComboItemListener synchAction = this.eventManager.getAction(ActionW.SYNCH).orElse(null);
                if (synchAction != null && (object = synchAction.getSelectedItem()) instanceof SynchView && !(sel = (SynchView)object).getSynchData().getMode().equals((Object)SynchData.Mode.TILE)) {
                    this.resetSynchState();
                }
            }
        }
        catch (Exception e) {
            AuditLog.logError(LOGGER, e, "Unexpected error:");
            this.imageLayer.setImage(null, null);
            this.closeLens();
        }
        finally {
            this.eventManager.updateComponentsListener(this);
        }
        if (newSeries != null) {
            newSeries.setOpen(true);
        }
    }

    protected int getImageSize(E img, TagW tag1, TagW tag2) {
        Integer size = (Integer)((MediaElement)img).getTagValue(tag1);
        if (size == null) {
            size = (Integer)((MediaElement)img).getTagValue(tag2);
        }
        return size == null ? 512 : size;
    }

    protected Rectangle getImageBounds(E img) {
        if (img != null) {
            PlanarImage source = this.getPreprocessedImage(img);
            boolean nosquarePixel = MathUtil.isDifferent((double)((ImageElement)img).getRescaleX(), (double)((ImageElement)img).getRescaleY());
            int width = source == null || nosquarePixel ? ((ImageElement)img).getRescaleWidth(this.getImageSize(img, TagW.ImageWidth, TagW.get("Columns"))) : source.width();
            int height = source == null || nosquarePixel ? ((ImageElement)img).getRescaleHeight(this.getImageSize(img, TagW.ImageHeight, TagW.get("Rows"))) : source.height();
            return new Rectangle(0, 0, width, height);
        }
        return new Rectangle(0, 0, 512, 512);
    }

    protected void updateCanvas(E img, boolean triggerViewModelChangeListeners) {
        Rectangle modelArea = this.getImageBounds(img);
        if (!modelArea.equals(this.getViewModel().getModelArea())) {
            DefaultViewModel m = (DefaultViewModel)this.getViewModel();
            boolean oldVal = m.isEnableViewModelChangeListeners();
            if (!triggerViewModelChangeListeners) {
                m.setEnableViewModelChangeListeners(false);
            }
            m.adjustMinViewScaleFromImage(modelArea.width, modelArea.height);
            m.setModelArea(modelArea);
            if (!triggerViewModelChangeListeners) {
                m.setEnableViewModelChangeListeners(oldVal);
            }
        }
    }

    @Override
    public void updateCanvas(boolean triggerViewModelChangeListeners) {
        this.updateCanvas(this.getImage(), triggerViewModelChangeListeners);
    }

    private void updatePinnedImage(E img) {
        if (!Objects.equals(this.pinnedImage, img)) {
            if (this.pinnedImage != null) {
                ((ImageElement)this.pinnedImage).unpinFromCache();
            }
            if (img != null) {
                ((ImageElement)img).pinInCache();
            }
            this.pinnedImage = img;
        }
    }

    protected void setImage(E img) {
        this.updatePinnedImage(img);
        boolean updateGraphics = false;
        this.imageLayer.setEnableDispOperations(false);
        if (img == null) {
            this.eventManager.getAction(ActionW.SCROLL_SERIES).ifPresent(SliderCineListener::stop);
            this.actionsInView.put(ActionW.SPATIAL_UNIT.cmd(), Unit.PIXEL);
            if (this.eventManager.getSelectedViewPane() == this) {
                this.eventManager.getAction(ActionW.SPATIAL_UNIT).ifPresent(c -> c.setSelectedItemWithoutTriggerAction(this.actionsInView.get(ActionW.SPATIAL_UNIT.cmd())));
            }
            this.imageLayer.setEnableDispOperations(true);
            this.imageLayer.setImage(null, null);
            this.imageLayer.setEnableDispOperations(false);
            this.setGraphicManager(new XmlGraphicModel());
            this.closeLens();
        } else {
            E oldImage = this.imageLayer.getSourceImage();
            if (!img.equals(oldImage)) {
                PlanarImage image;
                updateGraphics = true;
                Object oldUnit = this.actionsInView.get(ActionW.SPATIAL_UNIT.cmd());
                if (oldUnit == null || Unit.PIXEL.equals(oldUnit)) {
                    this.actionsInView.put(ActionW.SPATIAL_UNIT.cmd(), ((ImageElement)img).getPixelSpacingUnit());
                }
                if (this.eventManager.getSelectedViewPane() == this) {
                    this.eventManager.getAction(ActionW.SPATIAL_UNIT).ifPresent(c -> c.setSelectedItemWithoutTriggerAction(this.actionsInView.get(ActionW.SPATIAL_UNIT.cmd())));
                }
                this.actionsInView.put(ActionW.PREPROCESSING.cmd(), null);
                this.updateCanvas(img, false);
                this.imageLayer.fireOpEvent(new ImageOpEvent(ImageOpEvent.OpEvent.IMAGE_CHANGE, (MediaSeries<? extends ImageElement>)this.series, (ImageElement)img, null));
                this.resetZoom();
                this.imageLayer.setImage(img, (OpManager)this.actionsInView.get(ActionW.PREPROCESSING.cmd()));
                if (AuditLog.LOGGER.isInfoEnabled() && (image = ((ImageElement)img).getImage()) != null) {
                    int elemSize = CvType.ELEM_SIZE((int)image.type());
                    int channels = CvType.channels((int)image.type());
                    int bpp = elemSize * 8 / channels;
                    Object[] elements = new String[channels];
                    Arrays.fill(elements, Integer.toString(bpp));
                    String pixSize = String.join((CharSequence)",", (CharSequence[])elements);
                    AuditLog.LOGGER.info("open:image size:{},{} depth:{}", new Object[]{image.width(), image.height(), pixSize});
                }
            }
            this.imageLayer.setEnableDispOperations(true);
            this.updateGraphicManager(img, updateGraphics);
            if (this.panner != null) {
                this.panner.updateImage();
            }
            if (this.lens != null) {
                this.lens.updateImage();
                this.lens.updateZoom();
            }
        }
    }

    public void updateGraphicManager(E img, boolean updateGraphics) {
        if (updateGraphics) {
            GraphicModel modelList = (GraphicModel)((MediaElement)img).getTagValue(TagW.PresentationModel);
            if (modelList == null) {
                modelList = new XmlGraphicModel((ImageElement)img);
                ((MediaElement)img).setTag(TagW.PresentationModel, modelList);
            }
            ArrayList<GraphicSelectionListener> gListeners = new ArrayList<GraphicSelectionListener>(this.graphicManager.getGraphicSelectionListeners());
            this.setGraphicManager(modelList);
            gListeners.forEach(l -> this.graphicManager.addGraphicSelectionListener((GraphicSelectionListener)l));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void updateGraphicSelectionListener(ImageViewerPlugin<E> viewerPlugin) {
        if (viewerPlugin != null) {
            List<DockableTool> tools;
            List<DockableTool> list = tools = viewerPlugin.getSeriesViewerUI().tools;
            synchronized (list) {
                for (DockableTool p : tools) {
                    if (!(p instanceof GraphicSelectionListener)) continue;
                    GraphicSelectionListener selectionListener = (GraphicSelectionListener)((Object)p);
                    this.graphicManager.addGraphicSelectionListener(selectionListener);
                }
            }
        }
    }

    @Override
    public double getBestFitViewScale() {
        return this.adjustViewScale(super.getBestFitViewScale());
    }

    @Override
    public double getRealWorldViewScale() {
        Unit imgUnit;
        double realFactor;
        GraphicsConfiguration config;
        Monitor monitor;
        Window win;
        double viewScale = 0.0;
        E img = this.getImage();
        if (img != null && (win = SwingUtilities.getWindowAncestor(this)) != null && (monitor = MeasureTool.viewSetting.getMonitor((config = win.getGraphicsConfiguration()).getDevice())) != null && (realFactor = monitor.getRealScaleFactor()) > 0.0 && !Unit.PIXEL.equals((Object)(imgUnit = ((ImageElement)img).getPixelSpacingUnit()))) {
            viewScale = imgUnit.getFactorToMeters() * ((ImageElement)img).getPixelSize() / realFactor;
            viewScale = -this.adjustViewScale(viewScale);
        }
        return viewScale;
    }

    @Override
    public double adjustViewScale(double viewScale) {
        double ratio = viewScale;
        if (ratio < 0.08333333333333333) {
            ratio = 0.08333333333333333;
        } else if (ratio > 12.0) {
            ratio = 12.0;
        }
        return this.adjustViewScale(this.eventManager, ratio);
    }

    @Override
    public RenderedImageLayer<E> getImageLayer() {
        return this.imageLayer;
    }

    @Override
    public MeasurableLayer getMeasurableLayer() {
        return this.imageLayer;
    }

    @Override
    public LayerAnnotation getInfoLayer() {
        return this.infoLayer;
    }

    @Override
    public int getTileOffset() {
        return this.tileOffset;
    }

    @Override
    public void setTileOffset(int tileOffset) {
        this.tileOffset = tileOffset;
    }

    @Override
    public MediaSeries<E> getSeries() {
        return this.series;
    }

    @Override
    public E getImage() {
        return this.imageLayer.getSourceImage();
    }

    @Override
    public PlanarImage getSourceImage() {
        E image = this.getImage();
        return image == null ? null : this.getPreprocessedImage(image);
    }

    @Override
    public final void center() {
        this.setCenter(0.0, 0.0);
    }

    @Override
    public final void setCenter(Double modelOffsetX, Double modelOffsetY) {
        if (this.getWidth() != 0 && this.getHeight() != 0) {
            this.getViewModel().setModelOffset(modelOffsetX, modelOffsetY);
            Optional.ofNullable(this.panner).ifPresent(Panner::updateImageSize);
            Optional.ofNullable(this.lens).ifPresent(ZoomWin::updateZoom);
            this.updateAffineTransform();
        }
    }

    public final void moveOrigin(double x, double y) {
        this.setCenter(this.getViewModel().getModelOffsetX() + x, this.getViewModel().getModelOffsetY() + y);
    }

    @Override
    public final void moveOrigin(PanPoint point) {
        if (point != null) {
            if (PanPoint.State.CENTER.equals((Object)point.getState())) {
                this.highlightedPosition.setHighlightedPosition(point.isHighlightedPosition());
                this.highlightedPosition.setLocation(point);
                Rectangle2D area = this.getViewModel().getModelArea();
                this.setCenter(area.getWidth() * 0.5 - point.getX(), area.getHeight() * 0.5 - point.getY());
            } else if (PanPoint.State.MOVE.equals((Object)point.getState())) {
                this.moveOrigin(point.getX(), point.getY());
            } else if (PanPoint.State.DRAGSTART.equals((Object)point.getState())) {
                this.startedDragPoint.setLocation(this.getViewModel().getModelOffsetX(), this.getViewModel().getModelOffsetY());
            } else if (PanPoint.State.DRAGGING.equals((Object)point.getState())) {
                this.setCenter(this.startedDragPoint.getX() + point.getX(), this.startedDragPoint.getY() + point.getY());
            }
        }
    }

    @Override
    public Comparator<E> getCurrentSortComparator() {
        SeriesComparator sort = (SeriesComparator)this.actionsInView.get(ActionW.SORT_STACK.cmd());
        Boolean reverse = (Boolean)this.actionsInView.get(ActionW.INVERSE_STACK.cmd());
        return reverse != null && reverse != false ? sort.getReversOrderComparator() : sort;
    }

    @Override
    public int getFrameIndex() {
        if (this.series instanceof Series) {
            return ((Series)this.series).getImageIndex(this.imageLayer.getSourceImage(), (Filter)this.actionsInView.get(ActionW.FILTERED_SERIES.cmd()), this.getCurrentSortComparator());
        }
        return -1;
    }

    @Override
    public void setActionsInView(String action, Object value) {
        this.setActionsInView(action, value, false);
    }

    @Override
    public void setActionsInView(String action, Object value, Boolean repaint) {
        if (action != null) {
            this.actionsInView.put(action, value);
            if (repaint.booleanValue()) {
                this.repaint();
            }
        }
    }

    @Override
    public void setSelected(Boolean selected) {
        this.setBorder(selected != false ? focusBorder : viewBorder);
        this.graphicManager.setSelectedGraphic(null);
        this.graphicManager.fireGraphicsSelectionChanged(this.imageLayer);
        if (selected.booleanValue() && this.series != null) {
            AuditLog.LOGGER.info("select:series nb:{}", (Object)this.series.getSeriesNumber());
        }
    }

    @Override
    public Font getLayerFont() {
        Font font = FontItem.DEFAULT_SEMIBOLD.getFont();
        return DefaultView2d.getLayerFont(this.getFontMetrics(font), this.getWidth());
    }

    public static Font getLayerFont(FontMetrics fontMetrics, int width) {
        int minSize = fontMetrics.stringWidth("Cannot read this media!");
        if (minSize * 6 > width) {
            double ratio = ((double)minSize * 6.0 - (double)width) / (double)minSize;
            if (ratio < 1.0) {
                return FontItem.SMALL_SEMIBOLD.getFont();
            }
            if (ratio < 2.0) {
                return FontItem.MINI_SEMIBOLD.getFont();
            }
            return FontItem.MICRO_SEMIBOLD.getFont();
        }
        return fontMetrics.getFont();
    }

    @Override
    public void paintComponent(Graphics g) {
        if (g instanceof Graphics2D) {
            Graphics2D graphics2D = (Graphics2D)g;
            this.draw(graphics2D);
        }
    }

    protected void draw(Graphics2D g2d) {
        Stroke oldStroke = g2d.getStroke();
        Paint oldColor = g2d.getPaint();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
        Font defaultFont = this.getFont();
        g2d.setFont(defaultFont);
        Point2D p = this.getClipViewCoordinatesOffset();
        g2d.translate(p.getX(), p.getY());
        this.imageLayer.drawImage(g2d);
        this.drawLayers(g2d, this.affineTransform, this.inverseTransform);
        g2d.translate(-p.getX(), -p.getY());
        this.drawPointer(g2d, this.pointerType);
        this.drawAffineInvariant(g2d);
        if (this.infoLayer != null) {
            g2d.setFont(this.getLayerFont());
            this.infoLayer.paint(g2d);
        }
        this.drawOnTop(g2d);
        g2d.setFont(defaultFont);
        g2d.setPaint(oldColor);
        g2d.setStroke(oldStroke);
    }

    private void drawAffineInvariant(Graphics2D g2d) {
    }

    protected void drawOnTop(Graphics2D g2d) {
        this.drawProgressBar(g2d, this.progressBar);
    }

    @Override
    public void setProgressBar(JProgressBar bar) {
        this.progressBar = bar;
    }

    @Override
    public JProgressBar getProgressBar() {
        return this.progressBar;
    }

    @Override
    public boolean requiredTextAntialiasing() {
        Optional<SliderCineListener> cineAction = this.eventManager.getAction(ActionW.SCROLL_SERIES);
        return cineAction.isEmpty() || !cineAction.get().isActionEnabled() || !cineAction.get().isCining();
    }

    @Override
    public void drawLayers(Graphics2D g2d, AffineTransform transform, AffineTransform inverseTransform) {
        if (LangUtil.nullToTrue((Boolean)((Boolean)this.actionsInView.get(ActionW.DRAWINGS.cmd())))) {
            Object[] oldRenderingHints = GuiUtils.setRenderingHints(g2d, true, false, this.requiredTextAntialiasing());
            this.graphicManager.draw(g2d, transform, inverseTransform, null);
            GuiUtils.resetRenderingHints(g2d, oldRenderingHints);
        }
    }

    @Override
    public void zoom(Double viewScale) {
        double ratio = this.getZoomRatioFromType(viewScale);
        this.actionsInView.put(ActionW.ZOOM.cmd(), ratio);
        super.zoom(Math.abs(ratio));
        if (MathUtil.isEqualToZero((double)viewScale)) {
            this.center();
        }
        this.updateAffineTransform();
        if (this.panner != null) {
            this.panner.updateImageSize();
        }
    }

    protected void updateAffineTransform() {
        Optional<ImageOpNode> node = this.getDisplayOpManager().getNode(AffineTransformOp.OP_NAME);
        super.updateAffineTransform(this, node.orElse(null), this.imageLayer, 0.0);
    }

    @Override
    public void setDrawingsVisibility(Boolean visible) {
        if (!Objects.equals(this.actionsInView.get(ActionW.DRAWINGS.cmd()), visible)) {
            this.actionsInView.put(ActionW.DRAWINGS.cmd(), visible);
            this.repaint();
        }
    }

    @Override
    public Object getLensActionValue(String action) {
        if (this.lens == null) {
            return null;
        }
        return this.lens.getActionValue(action);
    }

    @Override
    public void changeZoomInterpolation(ZoomOp.Interpolation interpolation) {
        boolean update;
        Optional<ZoomOp.Interpolation> val = this.getDisplayOpManager().getParamValue(AffineTransformOp.OP_NAME, "interpolation", ZoomOp.Interpolation.class);
        boolean bl = update = !Objects.equals(val.orElse(null), (Object)interpolation);
        if (update) {
            this.getDisplayOpManager().setParamValue(AffineTransformOp.OP_NAME, "interpolation", (Object)interpolation);
            if (this.lens != null) {
                this.lens.getDisplayOpManager().setParamValue(AffineTransformOp.OP_NAME, "interpolation", (Object)interpolation);
                this.lens.updateZoom();
            }
            this.imageLayer.updateDisplayOperations();
        }
    }

    @Override
    public OpManager getDisplayOpManager() {
        return this.imageLayer.getDisplayOpManager();
    }

    public void propertyChange(SynchCineEvent synch) {
        this.graphicManager.deleteByLayerType(LayerType.CROSSLINES);
        ViewSynchData issuerSyncData = (ViewSynchData)synch.getView().getActionsInView().get(ActionW.SYNCH_LINK.cmd());
        ViewSynchData selfSyncData = (ViewSynchData)this.getActionsInView().get(ActionW.SYNCH_LINK.cmd());
        if (synch.getView() != this && !this.shouldAcceptSyncEvent(synch, issuerSyncData, selfSyncData)) {
            return;
        }
        E imgElement = this.resolveImageForSyncEvent(synch, issuerSyncData, selfSyncData);
        if (imgElement == null) {
            return;
        }
        this.applyImagePreservingZoom(imgElement);
    }

    private boolean shouldAcceptSyncEvent(SynchCineEvent synch, ViewSynchData issuer, ViewSynchData self) {
        boolean linked;
        if (issuer != null && !issuer.isSynchActivated()) {
            return false;
        }
        if (self != null && !self.isSynchActivated()) {
            return false;
        }
        boolean bl = linked = this.isAutoSyncMatch(issuer, self) || this.isManualSyncMatch(issuer, self) || this.isTileSyncMatch(issuer, self);
        if (!linked) {
            return false;
        }
        return issuer.isActionEnable(ActionW.SCROLL_SERIES.cmd()) && self.isActionEnable(ActionW.SCROLL_SERIES.cmd());
    }

    private boolean isAutoSyncMatch(ViewSynchData issuer, ViewSynchData self) {
        return issuer != null && self != null && issuer.isAutoSynchActivated() && self.isAutoSynchActivated() && issuer.getFrameOfReferenceUID() != null && issuer.getFrameOfReferenceUID().equals(self.getFrameOfReferenceUID());
    }

    private boolean isManualSyncMatch(ViewSynchData issuer, ViewSynchData self) {
        return issuer != null && self != null && issuer.isManualSynchActivated() && self.isManualSynchActivated() && issuer.getManualSyncDataByPane(this) != null;
    }

    private boolean isTileSyncMatch(ViewSynchData issuer, ViewSynchData self) {
        return issuer != null && self != null && issuer.getMode() == SynchData.Mode.TILE && self.getMode() == SynchData.Mode.TILE && issuer.isAutoSynchActivated() && self.isAutoSynchActivated();
    }

    private E resolveImageForSyncEvent(SynchCineEvent synch, ViewSynchData issuer, ViewSynchData self) {
        boolean sameOrientation;
        Filter filter = (Filter)this.actionsInView.get(ActionW.FILTERED_SERIES.cmd());
        if (synch.getView() == this) {
            if (this.tileOffset != 0) {
                return (E)((ImageElement)this.series.getMedia(synch.getSeriesIndex() + this.tileOffset, filter, this.getCurrentSortComparator()));
            }
            if (synch.getMedia() instanceof ImageElement) {
                return (E)((ImageElement)synch.getMedia());
            }
            return this.getImage();
        }
        if (synch.getLocation() == null) {
            AuditLog.LOGGER.info("synch:series nb:{}", (Object)this.series.getSeriesNumber());
            return (E)((ImageElement)this.series.getMedia(synch.getSeriesIndex() + this.tileOffset, filter, this.getCurrentSortComparator()));
        }
        Boolean cutlines = (Boolean)this.actionsInView.get(ActionW.SYNCH_CROSSLINE.cmd());
        boolean bl = sameOrientation = synch.getMedia() instanceof ImageElement && this.eventManager.synchManager.hasSameOrientation(this.series, synch.getView().getSeries());
        if (Boolean.TRUE.equals(cutlines) && !sameOrientation) {
            if (LangUtil.nullToTrue((Boolean)((Boolean)this.actionsInView.get(LayerType.CROSSLINES.name())))) {
                this.computeCrosslines(synch.getLocation().doubleValue());
            }
            return null;
        }
        double location = this.applyManualSyncOffset(synch.getLocation().doubleValue(), issuer, self);
        AuditLog.LOGGER.info("synch:series nb:{}", (Object)this.series.getSeriesNumber());
        return (E)((ImageElement)this.series.getNearestImage(location, this.tileOffset, filter, this.getCurrentSortComparator()));
    }

    private double applyManualSyncOffset(double location, ViewSynchData issuer, ViewSynchData self) {
        if (self == null || issuer == null || self.getManualSyncState() != SynchData.SyncState.ON || issuer.getManualSyncState() != SynchData.SyncState.ON) {
            return location;
        }
        ViewSynchData.ManualSyncData msd = issuer.getManualSyncDataByPane(this);
        return msd == null ? location : location + msd.getTargetLocation() - msd.getSourceLocation();
    }

    private void applyImagePreservingZoom(E imgElement) {
        Double zoomFactor = (Double)this.actionsInView.get(ActionW.ZOOM.cmd());
        if (zoomFactor != null && zoomFactor >= 0.0) {
            Object zoomType = this.actionsInView.get("zoom.type");
            this.actionsInView.put("zoom.type", ZoomType.CURRENT);
            this.setImage(imgElement);
            this.actionsInView.put("zoom.type", zoomType);
        } else {
            this.setImage(imgElement);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (this.series == null) {
            return;
        }
        PlanarImage dispImage = this.imageLayer.getDisplayImage();
        SimpleOpManager manager = this.imageLayer.getDisplayOpManager();
        String command = evt.getPropertyName();
        if (command.equals(ActionW.SYNCH.cmd())) {
            SynchEvent synch = (SynchEvent)evt.getNewValue();
            if (synch instanceof SynchCineEvent) {
                SynchCineEvent cineEvent = (SynchCineEvent)synch;
                this.propertyChange(cineEvent);
            } else {
                this.propertyChange(synch);
            }
        } else if (command.equals(ActionW.IMAGE_PIX_PADDING.cmd())) {
            if (manager.setParamValue(WindowOp.OP_NAME, command, evt.getNewValue())) {
                this.imageLayer.updateDisplayOperations();
            }
        } else if (command.equals(ActionW.PROGRESSION.cmd())) {
            this.actionsInView.put(command, evt.getNewValue());
            this.imageLayer.updateDisplayOperations();
        }
        if (Objects.nonNull(this.lens) && !Objects.equals(dispImage, this.imageLayer.getDisplayImage())) {
            this.lens.setCommandFromParentView(command, evt.getNewValue());
            this.lens.updateZoom();
        }
    }

    private void propertyChange(SynchEvent synch) {
        SynchData issuerSyncData = synch.getView() != null ? (SynchData)synch.getView().getActionsInView().get(ActionW.SYNCH_LINK.cmd()) : null;
        SynchData synchData = (SynchData)this.getActionsInView().get(ActionW.SYNCH_LINK.cmd());
        if (synch.getView() != this) {
            if (synchData != null && !synchData.isSynchActivated()) {
                return;
            }
            if (issuerSyncData != null && !issuerSyncData.isSynchActivated()) {
                return;
            }
        }
        SimpleOpManager manager = this.imageLayer.getDisplayOpManager();
        for (Map.Entry<String, Object> entry : synch.getEvents().entrySet()) {
            Object old;
            String command = entry.getKey();
            if (this != synch.getView() && (synchData != null && !synchData.isActionEnable(command) || issuerSyncData != null && !issuerSyncData.isActionEnable(command))) continue;
            if (command.equals(ActionW.WINDOW.cmd()) || command.equals(ActionW.LEVEL.cmd())) {
                if (!manager.setParamValue(WindowOp.OP_NAME, command, ((Number)entry.getValue()).doubleValue())) continue;
                this.imageLayer.updateDisplayOperations();
                continue;
            }
            if (command.equals(ActionW.ROTATION.cmd())) {
                old = this.actionsInView.put(ActionW.ROTATION.cmd(), entry.getValue());
                if (Objects.equals(old, entry.getValue())) continue;
                this.updateAffineTransform();
                continue;
            }
            if (command.equals(ActionW.RESET.cmd())) {
                this.reset();
                continue;
            }
            if (command.equals(ActionW.ZOOM.cmd())) {
                double val = (Double)entry.getValue();
                if (MathUtil.isDifferent((double)val, (double)-200.0) && MathUtil.isDifferent((double)val, (double)-100.0)) {
                    this.zoom(val);
                    continue;
                }
                Object zoomType = this.actionsInView.get("zoom.type");
                this.actionsInView.put("zoom.type", MathUtil.isEqual((double)val, (double)-100.0) ? ZoomType.REAL : ZoomType.BEST_FIT);
                this.zoom(0.0);
                this.actionsInView.put("zoom.type", zoomType);
                continue;
            }
            if (command.equals(ActionW.LENS_ZOOM.cmd())) {
                if (this.lens == null) continue;
                this.lens.setActionInView(ActionW.ZOOM.cmd(), entry.getValue());
                this.lens.updateZoom();
                continue;
            }
            if (command.equals(ActionW.LENS.cmd())) {
                Boolean showLens = (Boolean)entry.getValue();
                this.actionsInView.put(command, showLens);
                if (showLens.booleanValue()) {
                    if (this.lens == null) {
                        this.lens = new ZoomWin(this);
                    }
                    int maxWidth = this.getWidth() / 3;
                    int maxHeight = this.getHeight() / 3;
                    this.lens.setSize(Math.min(this.lens.getWidth(), maxWidth), Math.min(this.lens.getHeight(), maxHeight));
                    this.add(this.lens);
                    this.lens.showLens(true);
                    continue;
                }
                this.closeLens();
                continue;
            }
            if (command.equals(ActionW.PAN.cmd())) {
                Object point = entry.getValue();
                if (!(point instanceof PanPoint)) continue;
                this.moveOrigin((PanPoint)entry.getValue());
                continue;
            }
            if (command.equals(ActionW.FLIP.cmd())) {
                old = this.actionsInView.put(ActionW.FLIP.cmd(), entry.getValue());
                if (Objects.equals(old, entry.getValue())) continue;
                this.updateAffineTransform();
                continue;
            }
            if (command.equals(ActionW.LUT.cmd())) {
                if (!manager.setParamValue(PseudoColorOp.OP_NAME, PseudoColorOp.P_LUT, entry.getValue())) continue;
                this.imageLayer.updateDisplayOperations();
                continue;
            }
            if (command.equals(ActionW.INVERT_LUT.cmd())) {
                if (!manager.setParamValue(WindowOp.OP_NAME, command, entry.getValue())) continue;
                manager.setParamValue(PseudoColorOp.OP_NAME, PseudoColorOp.P_LUT_INVERSE, entry.getValue());
                this.imageLayer.updateDisplayOperations();
                continue;
            }
            if (command.equals(ActionW.FILTER.cmd())) {
                if (!manager.setParamValue(FilterOp.OP_NAME, "kernel", entry.getValue())) continue;
                this.imageLayer.updateDisplayOperations();
                continue;
            }
            if (!command.equals(ActionW.SPATIAL_UNIT.cmd())) continue;
            this.actionsInView.put(command, entry.getValue());
            this.graphicManager.updateLabels(Boolean.TRUE, this);
        }
    }

    protected void computeCrosslines(double location) {
    }

    @Override
    public void disposeView() {
        this.disableMouseAndKeyListener();
        this.removeFocusListener(this);
        ToolTipManager.sharedInstance().unregisterComponent(this);
        this.imageLayer.removeLayerChangeListener(this);
        Optional.ofNullable(this.lens).ifPresent(l -> l.showLens(false));
        this.resetSynchState();
        if (this.series != null) {
            this.setSeries(null);
        }
        super.disposeView();
    }

    @Override
    public synchronized void disableMouseAndKeyListener() {
        ViewCanvas.super.disableMouseAndKeyListener(this);
        Optional.ofNullable(this.lens).ifPresent(ZoomWin::disableMouseAndKeyListener);
    }

    @Override
    public synchronized void iniDefaultMouseListener() {
        this.addMouseListener(this.focusHandler);
        this.addMouseMotionListener(this.focusHandler);
        this.addMouseWheelListener(this.focusHandler);
        this.addMouseListener(this.segHoverHandler);
        this.addMouseMotionListener(this.segHoverHandler);
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        SegRegion<?> region = this.findSegRegionAt(event);
        this.setHighlightedSegRegion(region);
        return region == null ? null : region.getToolTipHtml();
    }

    private SegRegion<?> findSegRegionAt(MouseEvent event) {
        if (!LangUtil.nullToTrue((Boolean)((Boolean)this.actionsInView.get(ActionW.DRAWINGS.cmd())))) {
            return null;
        }
        Point2D p = this.getImageCoordinatesFromMouse(event.getX(), event.getY());
        double scale = GeomUtil.extractScalingFactor(this.affineTransform);
        List<Graphic> graphics = this.graphicManager.getAllGraphics();
        for (int i = graphics.size() - 1; i >= 0; --i) {
            double tolerance;
            SegGraphic seg;
            Graphic graphic = graphics.get(i);
            if (!(graphic instanceof SegGraphic) || (seg = (SegGraphic)graphic).getRegion() == null || !seg.isOnOutline(p, tolerance = (double)Math.max(seg.getLineThickness().floatValue(), 4.0f) / scale)) continue;
            return seg.getRegion();
        }
        return null;
    }

    private void setHighlightedSegRegion(SegRegion<?> region) {
        this.highlightedSegRegion = region;
        Rectangle dirty = null;
        for (Graphic graphic : this.graphicManager.getAllGraphics()) {
            boolean highlight;
            if (!(graphic instanceof SegGraphic)) continue;
            SegGraphic seg = (SegGraphic)graphic;
            boolean bl = highlight = region != null && seg.getRegion() == region;
            if (seg.isHighlighted() == highlight) continue;
            seg.setHighlighted(highlight);
            Rectangle bounds = this.getSegHighlightBounds(seg);
            if (bounds == null) continue;
            dirty = dirty == null ? bounds : dirty.union(bounds);
        }
        if (dirty != null) {
            this.repaint(dirty);
        }
    }

    private Rectangle getSegHighlightBounds(SegGraphic graphic) {
        if (graphic.getShape() == null) {
            return null;
        }
        Rectangle rect = graphic.getTransformedBounds(graphic.getShape(), this.affineTransform);
        if (rect == null) {
            return null;
        }
        int pad = (int)Math.ceil(2.0);
        rect.grow(pad, pad);
        Point2D offset = this.getClipViewCoordinatesOffset();
        rect.translate((int)offset.getX(), (int)offset.getY());
        return rect;
    }

    @Override
    public synchronized void iniDefaultKeyListener() {
        this.addKeyListener(this);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        this.defaultKeyPressed(this.eventManager, e);
    }

    @Override
    public int getPointerType() {
        return this.pointerType;
    }

    @Override
    public void setPointerType(int pointerType) {
        this.pointerType = pointerType;
    }

    @Override
    public void addPointerType(int i) {
        this.pointerType |= i;
    }

    @Override
    public void resetPointerType(int i) {
        this.pointerType &= ~i;
    }

    @Override
    public Point2D getHighlightedPosition() {
        return this.highlightedPosition;
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
    }

    @Override
    public List<Action> getExportActions() {
        ArrayList<Action> list = new ArrayList<Action>();
        DefaultAction exportAction = new DefaultAction(ActionW.EXPORT_VIEW.getTitle(), (Icon)ActionW.EXPORT_VIEW.getIcon(), actionEvent -> ScreenshotDialog.showDialog(this));
        list.add(exportAction);
        return list;
    }

    public static AffineTransform getAffineTransform(MouseEvent mouseevent) {
        Object object;
        if (mouseevent != null && (object = mouseevent.getSource()) instanceof Image2DViewer) {
            Image2DViewer viewer = (Image2DViewer)object;
            return viewer.getAffineTransform();
        }
        return null;
    }

    @Override
    public void resetZoom() {
        ZoomType type = (ZoomType)((Object)this.actionsInView.get("zoom.type"));
        if (!ZoomType.CURRENT.equals((Object)type)) {
            this.zoom(0.0);
        }
    }

    @Override
    public void resetPan() {
        this.center();
    }

    @Override
    public void reset() {
        this.imageLayer.setEnableDispOperations(false);
        ImageViewerPlugin<E> pane = this.eventManager.getSelectedView2dContainer();
        if (pane != null) {
            pane.resetMaximizedSelectedImagePane(this);
        }
        Object oldUnit = this.actionsInView.get(ActionW.SPATIAL_UNIT.cmd());
        this.initActionWState();
        this.imageLayer.fireOpEvent(new ImageOpEvent(ImageOpEvent.OpEvent.RESET_DISPLAY, (MediaSeries<? extends ImageElement>)this.series, (ImageElement)this.getImage(), null));
        this.resetZoom();
        if (ZoomType.CURRENT.equals(this.actionsInView.get("zoom.type"))) {
            this.zoom(0.0);
        }
        this.resetPan();
        this.imageLayer.setEnableDispOperations(true);
        this.eventManager.updateComponentsListener(this);
        if (!Objects.equals(oldUnit, this.actionsInView.get(ActionW.SPATIAL_UNIT.cmd()))) {
            this.graphicManager.updateLabels(Boolean.TRUE, this);
        }
    }

    @Override
    public List<ViewButton> getViewButtons() {
        return this.viewButtons;
    }

    protected void copyGraphicsFromClipboard() {
        List<Graphic> graphs = GRAPHIC_CLIPBOARD.getGraphics();
        if (graphs != null) {
            int option;
            Rectangle2D area = this.getViewModel().getModelArea();
            if (graphs.stream().anyMatch(g -> !g.getBounds(null).intersects(area)) && (option = JOptionPane.showConfirmDialog(this, "At least one graphic is outside the image.\n Do you want to continue?")) != 0) {
                return;
            }
            graphs.forEach(g -> AbstractGraphicModel.addGraphicToModel(this, (Graphic)g.copy()));
            this.repaint();
        }
    }

    public static Cursor getNewCursor(int type) {
        return new Cursor(type);
    }

    protected void resetSynchState() {
        Set<ViewSynchData.ManualSyncData> manualSyncDataSet;
        ViewSynchData synchData = (ViewSynchData)this.actionsInView.get(ActionW.SYNCH_LINK.cmd());
        if (synchData == null) {
            return;
        }
        SynchData globalSynchData = this.getGlobalSynchData();
        if (globalSynchData != null && globalSynchData.isAutoSynchActivated()) {
            globalSynchData.setOriginal(true);
            globalSynchData.setAutoSyncState(SynchData.SyncState.OFF);
        }
        if (!(manualSyncDataSet = synchData.getManualSyncDataSet()).isEmpty()) {
            boolean anyRemains;
            ImageViewerPlugin<E> container;
            for (ViewSynchData.ManualSyncData msd : new HashSet<ViewSynchData.ManualSyncData>(manualSyncDataSet)) {
                ViewCanvas<?> targetPane = msd.getTargetPane();
                ViewSynchData targetSd = (ViewSynchData)targetPane.getActionsInView().get(ActionW.SYNCH_LINK.cmd());
                if (targetSd == null) continue;
                targetSd.getManualSyncDataSet().removeIf(d -> d.getTargetPane() == this);
                if (!targetSd.getManualSyncDataSet().isEmpty()) continue;
                targetSd.setManualSyncState(SynchData.SyncState.OFF);
            }
            manualSyncDataSet.clear();
            synchData.setManualSyncState(SynchData.SyncState.OFF);
            if (globalSynchData != null && globalSynchData.isManualSynchActivated() && (container = this.eventManager.getSelectedView2dContainer()) != null && !(anyRemains = container.getImagePanels().stream().filter(v -> v != this).anyMatch(v -> {
                ViewSynchData sd = (ViewSynchData)v.getActionsInView().get(ActionW.SYNCH_LINK.cmd());
                return sd != null && sd.isManualSynchActivated();
            }))) {
                globalSynchData.setManualSyncState(SynchData.SyncState.OFF);
            }
        }
        synchData.setAutoSyncState(SynchData.SyncState.OFF);
        synchData.setOriginal(true);
        synchData.setFrameOfReferenceUID(null);
    }

    public void syncManuallyWith(ViewCanvas<?> target) {
        this.setupManualSync(this, target);
    }

    private SynchData getGlobalSynchData() {
        ComboItemListener synchAction = this.eventManager.getAction(ActionW.SYNCH).orElse(null);
        return synchAction == null ? null : ((SynchView)synchAction.getSelectedItem()).getSynchData();
    }

    private class SegHoverHandler
    extends MouseAdapter {
        final /* synthetic */ DefaultView2d this$0;

        private SegHoverHandler(DefaultView2d defaultView2d) {
            DefaultView2d defaultView2d2 = defaultView2d;
            Objects.requireNonNull(defaultView2d2);
            this.this$0 = defaultView2d2;
        }

        @Override
        public void mouseExited(MouseEvent e) {
            this.this$0.setHighlightedSegRegion(null);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (this.this$0.highlightedSegRegion != null) {
                this.this$0.setHighlightedSegRegion(this.this$0.findSegRegionAt(e));
            }
        }
    }

    public static enum ZoomType {
        CURRENT,
        BEST_FIT,
        PIXEL_SIZE,
        REAL;

    }

    protected static class BulkDragSequence
    implements Draggable {
        private final List<Draggable> childDS;

        BulkDragSequence(List<DragGraphic> dragGraphList, MouseEventDouble mouseEvent) {
            this.childDS = new ArrayList<Draggable>(dragGraphList.size());
            for (DragGraphic dragGraph : dragGraphList) {
                Draggable dragSequence = dragGraph.createMoveDrag();
                if (dragSequence == null) continue;
                this.childDS.add(dragSequence);
            }
        }

        @Override
        public void startDrag(MouseEventDouble mouseevent) {
            int j = this.childDS.size();
            for (int i = 0; i < j; ++i) {
                this.childDS.get(i).startDrag(mouseevent);
            }
        }

        @Override
        public void drag(MouseEventDouble mouseevent) {
            int j = this.childDS.size();
            for (int i = 0; i < j; ++i) {
                this.childDS.get(i).drag(mouseevent);
            }
        }

        @Override
        public Boolean completeDrag(MouseEventDouble mouseevent) {
            int j = this.childDS.size();
            for (int i = 0; i < j; ++i) {
                this.childDS.get(i).completeDrag(mouseevent);
            }
            return true;
        }
    }
}

