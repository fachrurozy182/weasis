/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.editor.image;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.swing.BoundedRangeModel;
import javax.swing.SwingUtilities;
import javax.swing.event.SwingPropertyChangeSupport;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.layout.MigLayoutModel;
import org.weasis.core.api.gui.util.ActionState;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.ComboItemListener;
import org.weasis.core.api.gui.util.DecFormatter;
import org.weasis.core.api.gui.util.Feature;
import org.weasis.core.api.gui.util.Filter;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.ShortcutManager;
import org.weasis.core.api.gui.util.SliderChangeListener;
import org.weasis.core.api.gui.util.SliderCineListener;
import org.weasis.core.api.gui.util.ToggleButtonListener;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.service.WProperties;
import org.weasis.core.ui.editor.SeriesViewerEvent;
import org.weasis.core.ui.editor.SeriesViewerListener;
import org.weasis.core.ui.editor.image.CrosshairListener;
import org.weasis.core.ui.editor.image.DefaultSynchManager;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.MouseActions;
import org.weasis.core.ui.editor.image.PannerListener;
import org.weasis.core.ui.editor.image.SynchCineEvent;
import org.weasis.core.ui.editor.image.SynchData;
import org.weasis.core.ui.editor.image.SynchEvent;
import org.weasis.core.ui.editor.image.SynchManager;
import org.weasis.core.ui.editor.image.SynchView;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.editor.image.ViewerPlugin;
import org.weasis.core.ui.editor.image.ViewerToolBar;
import org.weasis.core.ui.editor.image.dockable.MeasureTool;
import org.weasis.core.ui.launcher.Launcher;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.utils.bean.PanPoint;
import org.weasis.core.ui.pref.ZoomSetting;
import org.weasis.core.ui.util.ColorLayerUI;
import org.weasis.core.ui.util.PrintDialog;

public abstract class ImageViewerEventManager<E extends ImageElement>
implements KeyListener {
    public static final int WINDOW_SMALLEST = 0;
    public static final int WINDOW_LARGEST = 4096;
    public static final int WINDOW_DEFAULT = 700;
    public static final int LEVEL_SMALLEST = 0;
    public static final int LEVEL_LARGEST = 4096;
    public static final int LEVEL_DEFAULT = 300;
    protected final ArrayList<SeriesViewerListener> seriesViewerListeners = new ArrayList();
    protected final MouseActions mouseActions = new MouseActions(null);
    protected final ZoomSetting zoomSetting = new ZoomSetting();
    protected final WProperties options = new WProperties();
    protected final SwingPropertyChangeSupport propertySupport = new SwingPropertyChangeSupport(this);
    protected final HashMap<Feature<? extends ActionState>, ActionState> actions = new HashMap();
    protected final SynchManager<E> synchManager = this.createSynchManager();
    protected volatile boolean enabledAction = true;
    protected ImageViewerPlugin<E> selectedView2dContainer;

    protected ImageViewerEventManager() {
    }

    public void setAction(ActionState action) {
        this.actions.put(action.getActionW(), action);
    }

    public void removeAction(Feature<?> action) {
        this.actions.remove(action);
    }

    protected SynchManager<E> createSynchManager() {
        return new DefaultSynchManager(this);
    }

    protected SliderCineListener getMoveTroughSliceAction(double speed, SliderCineListener.TIME time, double mouseSensitivity) {
        return new SliderCineListener(this, ActionW.SCROLL_SERIES, 1, 2, 1, speed, time, mouseSensitivity){
            final /* synthetic */ ImageViewerEventManager this$0;
            {
                ImageViewerEventManager imageViewerEventManager = this$0;
                Objects.requireNonNull(imageViewerEventManager);
                this.this$0 = imageViewerEventManager;
                super((Feature<? extends ActionState>)action, min, max, value, speed, time, mouseSensitivity);
            }

            @Override
            public void stateChanged(BoundedRangeModel model) {
                SynchData synchData;
                ViewCanvas view2d = null;
                Series series = null;
                SynchCineEvent mediaEvent = null;
                ImageElement image = null;
                if (this.this$0.selectedView2dContainer != null) {
                    view2d = this.this$0.selectedView2dContainer.getSelectedViewCanvas();
                }
                if (view2d != null && view2d.getSeries() instanceof Series && (series = (Series)view2d.getSeries()) != null) {
                    int index = model.getValue() - 1;
                    image = (ImageElement)series.getMedia(index, (Filter)view2d.getActionValue(ActionW.FILTERED_SERIES.cmd()), view2d.getCurrentSortComparator());
                    mediaEvent = new SynchCineEvent(view2d, image, index);
                    if (image != null && !image.isImageAvailable()) {
                        image.getImage();
                    }
                }
                if ((synchData = (SynchData)this.this$0.getSelectedViewPane().getActionsInView().get(ActionW.SYNCH_LINK.cmd())) != null && synchData.isSynchActivated()) {
                    this.this$0.firePropertyChange(ActionW.SYNCH.cmd(), null, mediaEvent);
                }
                if (image != null) {
                    this.this$0.fireSeriesViewerListeners(new SeriesViewerEvent(this.this$0.selectedView2dContainer, series, image, SeriesViewerEvent.EVENT.SELECT));
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                this.setSliderValue(this.getSliderValue() + e.getWheelRotation());
            }
        };
    }

    protected ToggleButtonListener newLoopSweepAction() {
        return new ToggleButtonListener(this, ActionW.CINE_SWEEP, false){
            final /* synthetic */ ImageViewerEventManager this$0;
            {
                ImageViewerEventManager imageViewerEventManager = this$0;
                Objects.requireNonNull(imageViewerEventManager);
                this.this$0 = imageViewerEventManager;
                super(action, selected);
            }

            @Override
            public void actionPerformed(boolean selected) {
                this.this$0.getAction(ActionW.SCROLL_SERIES).ifPresent(a -> a.setSweeping(selected));
            }
        };
    }

    protected SliderChangeListener newWindowAction() {
        return new SliderChangeListener(this, ActionW.WINDOW, 0, 4096, 700, true, 1.25){
            final /* synthetic */ ImageViewerEventManager this$0;
            {
                ImageViewerEventManager imageViewerEventManager = this$0;
                Objects.requireNonNull(imageViewerEventManager);
                this.this$0 = imageViewerEventManager;
                super(action, min, max, value, triggerWhenAdjusting, mouseSensitivity);
            }

            @Override
            public void stateChanged(BoundedRangeModel model) {
                this.this$0.firePropertyChange(ActionW.SYNCH.cmd(), null, new SynchEvent(this.this$0.getSelectedViewPane(), this.getActionW().cmd(), this.toModelValue(model.getValue())));
                if (this.this$0.selectedView2dContainer != null) {
                    this.this$0.fireSeriesViewerListeners(new SeriesViewerEvent(this.this$0.selectedView2dContainer, null, null, SeriesViewerEvent.EVENT.WIN_LEVEL));
                }
            }
        };
    }

    protected SliderChangeListener newLevelAction() {
        return new SliderChangeListener(this, ActionW.LEVEL, 0, 4096, 300, true, 1.25){
            final /* synthetic */ ImageViewerEventManager this$0;
            {
                ImageViewerEventManager imageViewerEventManager = this$0;
                Objects.requireNonNull(imageViewerEventManager);
                this.this$0 = imageViewerEventManager;
                super(action, min, max, value, triggerWhenAdjusting, mouseSensitivity);
            }

            @Override
            public void stateChanged(BoundedRangeModel model) {
                this.this$0.firePropertyChange(ActionW.SYNCH.cmd(), null, new SynchEvent(this.this$0.getSelectedViewPane(), this.getActionW().cmd(), this.toModelValue(model.getValue())));
                if (this.this$0.selectedView2dContainer != null) {
                    this.this$0.fireSeriesViewerListeners(new SeriesViewerEvent(this.this$0.selectedView2dContainer, null, null, SeriesViewerEvent.EVENT.WIN_LEVEL));
                }
            }
        };
    }

    protected SliderChangeListener newRotateAction() {
        return new SliderChangeListener(this, ActionW.ROTATION, 0, 360, 0, true, 0.25){
            final /* synthetic */ ImageViewerEventManager this$0;
            {
                ImageViewerEventManager imageViewerEventManager = this$0;
                Objects.requireNonNull(imageViewerEventManager);
                this.this$0 = imageViewerEventManager;
                super(action, min, max, value, triggerWhenAdjusting, mouseSensitivity);
            }

            @Override
            public void stateChanged(BoundedRangeModel model) {
                this.this$0.firePropertyChange(ActionW.SYNCH.cmd(), null, new SynchEvent(this.this$0.getSelectedViewPane(), this.getActionW().cmd(), model.getValue()));
            }

            @Override
            public String getValueToDisplay() {
                return this.getSliderValue() + " \u00b0";
            }
        };
    }

    protected SliderChangeListener newZoomAction() {
        return new SliderChangeListener(this, ActionW.ZOOM, 0.08333333333333333, 12.0, 1.0, true, 0.3, 300){
            final /* synthetic */ ImageViewerEventManager this$0;
            {
                ImageViewerEventManager imageViewerEventManager = this$0;
                Objects.requireNonNull(imageViewerEventManager);
                this.this$0 = imageViewerEventManager;
                super(action, min, max, value, triggerWhenAdjusting, mouseSensitivity, sliderRange);
            }

            @Override
            public void stateChanged(BoundedRangeModel model) {
                this.this$0.firePropertyChange(ActionW.SYNCH.cmd(), null, new SynchEvent(this.this$0.getSelectedViewPane(), this.getActionW().cmd(), this.toModelValue(model.getValue())));
            }

            @Override
            public String getValueToDisplay() {
                return DecFormatter.percentTwoDecimal(this.getRealValue());
            }

            @Override
            public int toSliderValue(double viewScale) {
                double v = Math.log(viewScale) / Math.log(12.0) * (double)this.getSliderMax();
                return (int)Math.round(v);
            }

            @Override
            public double toModelValue(int sliderValue) {
                double v = (double)sliderValue / (double)this.getSliderMax();
                double viewScale = Math.exp(v * Math.log(12.0));
                return ImageViewerEventManager.roundAndCropViewScale(viewScale, 0.08333333333333333, 12.0);
            }
        };
    }

    protected PannerListener newPanAction() {
        return new PannerListener(this, ActionW.PAN, null){
            final /* synthetic */ ImageViewerEventManager this$0;
            {
                ImageViewerEventManager imageViewerEventManager = this$0;
                Objects.requireNonNull(imageViewerEventManager);
                this.this$0 = imageViewerEventManager;
                super(action, point);
            }

            @Override
            public void pointChanged(Point2D point) {
                this.this$0.firePropertyChange(ActionW.SYNCH.cmd(), null, new SynchEvent(this.this$0.getSelectedViewPane(), this.getActionW().cmd(), point));
            }
        };
    }

    protected CrosshairListener newCrosshairAction() {
        return new CrosshairListener(this, ActionW.CROSSHAIR, null){
            final /* synthetic */ ImageViewerEventManager this$0;
            {
                ImageViewerEventManager imageViewerEventManager = this$0;
                Objects.requireNonNull(imageViewerEventManager);
                this.this$0 = imageViewerEventManager;
                super(action, point);
            }

            @Override
            public void pointChanged(PanPoint point) {
                this.this$0.firePropertyChange(ActionW.SYNCH.cmd(), null, new SynchEvent(this.this$0.getSelectedViewPane(), this.getActionW().cmd(), point));
            }
        };
    }

    protected ToggleButtonListener newFlipAction() {
        return new ToggleButtonListener(this, ActionW.FLIP, false){
            final /* synthetic */ ImageViewerEventManager this$0;
            {
                ImageViewerEventManager imageViewerEventManager = this$0;
                Objects.requireNonNull(imageViewerEventManager);
                this.this$0 = imageViewerEventManager;
                super(action, selected);
            }

            @Override
            public void actionPerformed(boolean selected) {
                this.this$0.firePropertyChange(ActionW.SYNCH.cmd(), null, new SynchEvent(this.this$0.getSelectedViewPane(), this.action.cmd(), selected));
            }
        };
    }

    protected ToggleButtonListener newInverseLutAction() {
        return new ToggleButtonListener(this, ActionW.INVERT_LUT, false){
            final /* synthetic */ ImageViewerEventManager this$0;
            {
                ImageViewerEventManager imageViewerEventManager = this$0;
                Objects.requireNonNull(imageViewerEventManager);
                this.this$0 = imageViewerEventManager;
                super(action, selected);
            }

            @Override
            public void actionPerformed(boolean selected) {
                this.this$0.firePropertyChange(ActionW.SYNCH.cmd(), null, new SynchEvent(this.this$0.getSelectedViewPane(), this.action.cmd(), selected));
                if (this.this$0.selectedView2dContainer != null) {
                    this.this$0.fireSeriesViewerListeners(new SeriesViewerEvent(this.this$0.selectedView2dContainer, null, null, SeriesViewerEvent.EVENT.LUT));
                }
            }
        };
    }

    protected ToggleButtonListener newLensAction() {
        return new ToggleButtonListener(this, ActionW.LENS, false){
            final /* synthetic */ ImageViewerEventManager this$0;
            {
                ImageViewerEventManager imageViewerEventManager = this$0;
                Objects.requireNonNull(imageViewerEventManager);
                this.this$0 = imageViewerEventManager;
                super(action, selected);
            }

            @Override
            public void actionPerformed(boolean selected) {
                this.this$0.firePropertyChange(ActionW.SYNCH.cmd(), null, new SynchEvent(this.this$0.getSelectedViewPane(), this.action.cmd(), selected));
            }
        };
    }

    protected SliderChangeListener newLensZoomAction() {
        return new SliderChangeListener(this, ActionW.LENS_ZOOM, 0.08333333333333333, 12.0, 2.0, true, 0.3, 300){
            final /* synthetic */ ImageViewerEventManager this$0;
            {
                ImageViewerEventManager imageViewerEventManager = this$0;
                Objects.requireNonNull(imageViewerEventManager);
                this.this$0 = imageViewerEventManager;
                super(action, min, max, value, triggerWhenAdjusting, mouseSensitivity, sliderRange);
            }

            @Override
            public void stateChanged(BoundedRangeModel model) {
                this.this$0.firePropertyChange(ActionW.SYNCH.cmd(), null, new SynchEvent(this.this$0.getSelectedViewPane(), this.getActionW().cmd(), this.toModelValue(model.getValue())));
            }

            @Override
            public String getValueToDisplay() {
                return DecFormatter.percentTwoDecimal(this.getRealValue());
            }

            @Override
            public int toSliderValue(double viewScale) {
                double v = Math.log(viewScale) / Math.log(12.0) * (double)this.getSliderMax();
                return (int)Math.round(v);
            }

            @Override
            public double toModelValue(int sliderValue) {
                double v = (double)sliderValue / (double)this.getSliderMax();
                double viewScale = Math.exp(v * Math.log(12.0));
                return ImageViewerEventManager.roundAndCropViewScale(viewScale, 0.08333333333333333, 12.0);
            }
        };
    }

    protected ComboItemListener<MigLayoutModel> newLayoutAction(MigLayoutModel[] layouts) {
        return new ComboItemListener<MigLayoutModel>(this, ActionW.LAYOUT, Optional.ofNullable(layouts).orElseGet(() -> new MigLayoutModel[0])){
            final /* synthetic */ ImageViewerEventManager this$0;
            {
                ImageViewerEventManager imageViewerEventManager = this$0;
                Objects.requireNonNull(imageViewerEventManager);
                this.this$0 = imageViewerEventManager;
                super(action, objects);
            }

            @Override
            public void itemStateChanged(Object object) {
                if (this.this$0.selectedView2dContainer != null && object instanceof MigLayoutModel) {
                    MigLayoutModel layoutModel = (MigLayoutModel)object;
                    this.this$0.clearAllPropertyChangeListeners();
                    ViewCanvas view = this.this$0.selectedView2dContainer.getSelectedViewCanvas();
                    this.this$0.selectedView2dContainer.setLayoutModel(layoutModel);
                    if (!this.this$0.selectedView2dContainer.isContainingView(view).booleanValue()) {
                        view = this.this$0.selectedView2dContainer.getSelectedViewCanvas();
                    }
                    this.this$0.selectedView2dContainer.setSelectedImagePane(view);
                    this.this$0.getAction(ActionW.SYNCH).ifPresent(s -> this.this$0.selectedView2dContainer.setSynchView((SynchView)s.getSelectedItem()));
                }
            }
        };
    }

    protected ComboItemListener<SynchView> newSynchAction(SynchView[] synchViewList) {
        return new ComboItemListener<SynchView>(this, ActionW.SYNCH, Optional.ofNullable(synchViewList).orElseGet(() -> new SynchView[0])){
            final /* synthetic */ ImageViewerEventManager this$0;
            {
                ImageViewerEventManager imageViewerEventManager = this$0;
                Objects.requireNonNull(imageViewerEventManager);
                this.this$0 = imageViewerEventManager;
                super(action, objects);
            }

            @Override
            public void itemStateChanged(Object object) {
                if (object instanceof SynchView) {
                    SynchView synchView = (SynchView)object;
                    if (this.this$0.selectedView2dContainer != null) {
                        this.this$0.selectedView2dContainer.setSynchView(synchView);
                    }
                }
            }
        };
    }

    protected ToggleButtonListener newSynchModeAction() {
        return new ToggleButtonListener(this, ActionW.SYNCH_MODE, false){
            final /* synthetic */ ImageViewerEventManager this$0;
            {
                ImageViewerEventManager imageViewerEventManager = this$0;
                Objects.requireNonNull(imageViewerEventManager);
                this.this$0 = imageViewerEventManager;
                super(action, selected);
            }

            @Override
            public void actionPerformed(boolean selected) {
                this.this$0.getAction(ActionW.SYNCH).ifPresent(a -> {
                    Object patt0$temp = a.getSelectedItem();
                    if (patt0$temp instanceof SynchView) {
                        SynchView sel = (SynchView)patt0$temp;
                        sel.getSynchData().setManualSyncState(SynchData.SyncState.OFF);
                        sel.getSynchData().setAutoSyncState(SynchData.SyncState.OFF);
                        sel.getSynchData().setOriginal(selected);
                        this.this$0.refreshAllContainers(sel);
                    }
                });
            }
        };
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void refreshAllContainers(SynchView sel) {
        List<ViewerPlugin<?>> plugins;
        this.clearAllPropertyChangeListeners();
        ImageViewerPlugin<E> selectedContainer = this.getSelectedView2dContainer();
        if (selectedContainer != null) {
            this.synchManager.updateAllListeners(selectedContainer, sel);
        }
        List<ViewerPlugin<?>> list = plugins = GuiUtils.getUICore().getViewerPlugins();
        synchronized (list) {
            for (ViewerPlugin<?> p : plugins) {
                ImageViewerPlugin ivp;
                if (!(p instanceof ImageViewerPlugin) || (ivp = (ImageViewerPlugin)p) == selectedContainer || ivp.getEventManager() != this) continue;
                ImageViewerPlugin typed = ivp;
                this.synchManager.updateAllListeners(typed, sel);
            }
        }
    }

    protected ToggleButtonListener newInverseStackAction() {
        return new ToggleButtonListener(this, ActionW.INVERSE_STACK, false){
            final /* synthetic */ ImageViewerEventManager this$0;
            {
                ImageViewerEventManager imageViewerEventManager = this$0;
                Objects.requireNonNull(imageViewerEventManager);
                this.this$0 = imageViewerEventManager;
                super(action, selected);
            }

            @Override
            public void actionPerformed(boolean selected) {
                this.this$0.firePropertyChange(ActionW.SYNCH.cmd(), null, new SynchEvent(this.this$0.getSelectedViewPane(), this.action.cmd(), selected));
            }
        };
    }

    protected static ComboItemListener<Graphic> newMeasurementAction(Graphic[] graphics) {
        return new ComboItemListener<Graphic>(ActionW.DRAW_MEASURE, Optional.ofNullable(graphics).orElseGet(() -> new Graphic[0])){

            @Override
            public void itemStateChanged(Object object) {
            }
        };
    }

    protected static ComboItemListener<Graphic> newDrawAction(Graphic[] graphics) {
        return new ComboItemListener<Graphic>(ActionW.DRAW_GRAPHICS, Optional.ofNullable(graphics).orElseGet(() -> new Graphic[0])){

            @Override
            public void itemStateChanged(Object object) {
            }
        };
    }

    protected ToggleButtonListener newDrawOnlyOnceAction() {
        return new ToggleButtonListener(this, ActionW.DRAW_ONLY_ONCE, true){
            final /* synthetic */ ImageViewerEventManager this$0;
            {
                ImageViewerEventManager imageViewerEventManager = this$0;
                Objects.requireNonNull(imageViewerEventManager);
                this.this$0 = imageViewerEventManager;
                super(action, selected);
            }

            @Override
            public void actionPerformed(boolean selected) {
                this.this$0.firePropertyChange(ActionW.SYNCH.cmd(), null, new SynchEvent(this.this$0.getSelectedViewPane(), this.action.cmd(), selected));
                MeasureTool.viewSetting.setDrawOnlyOnce(selected);
            }
        };
    }

    protected ComboItemListener<Unit> newSpatialUnit(Unit[] units) {
        return new ComboItemListener<Unit>(this, ActionW.SPATIAL_UNIT, Optional.ofNullable(units).orElseGet(() -> new Unit[0])){
            final /* synthetic */ ImageViewerEventManager this$0;
            {
                ImageViewerEventManager imageViewerEventManager = this$0;
                Objects.requireNonNull(imageViewerEventManager);
                this.this$0 = imageViewerEventManager;
                super(action, objects);
            }

            @Override
            public void itemStateChanged(Object object) {
                this.this$0.firePropertyChange(ActionW.SYNCH.cmd(), null, new SynchEvent(this.this$0.getSelectedViewPane(), this.action.cmd(), object));
            }
        };
    }

    public abstract boolean updateComponentsListener(ViewCanvas<E> var1);

    public static double roundAndCropViewScale(double viewScale, double minViewScale, double maxViewScale) {
        double v;
        double ratio = viewScale;
        if ((ratio *= 1000.0) - (v = Math.floor(ratio)) >= 0.5) {
            v += 0.5;
        }
        if ((ratio = v / 1000.0) < minViewScale) {
            ratio = minViewScale;
        }
        if (ratio > maxViewScale) {
            ratio = maxViewScale;
        }
        return ratio;
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        this.propertySupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        this.propertySupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        this.propertySupport.removePropertyChangeListener(propertyName, listener);
    }

    public void addSeriesViewerListener(SeriesViewerListener listener) {
        if (!this.seriesViewerListeners.contains(listener)) {
            this.seriesViewerListeners.add(listener);
        }
    }

    public void removeSeriesViewerListener(SeriesViewerListener listener) {
        this.seriesViewerListeners.remove(listener);
    }

    public void fireSeriesViewerListeners(SeriesViewerEvent event) {
        if (event != null) {
            for (SeriesViewerListener listener : this.seriesViewerListeners) {
                listener.changingViewContentEvent(event);
            }
        }
    }

    public void clearAllPropertyChangeListeners() {
        PropertyChangeListener[] changeListeners;
        for (PropertyChangeListener propertyChangeListener : changeListeners = this.propertySupport.getPropertyChangeListeners()) {
            this.propertySupport.removePropertyChangeListener(propertyChangeListener);
        }
    }

    public <T> Optional<T> getAction(Feature<T> feature) {
        return Optional.ofNullable(this.actions.get(feature));
    }

    public boolean isActionRegistered(Feature<?> feature) {
        if (feature != null) {
            return this.actions.containsKey(feature);
        }
        return false;
    }

    public boolean isActionEnabled(Feature<?> feature) {
        ActionState action;
        if (feature != null && (action = this.actions.get(feature)) != null) {
            return action.isActionEnabled();
        }
        return false;
    }

    public Optional<Feature<? extends ActionState>> getActionKey(String command) {
        if (command == null) {
            return Optional.empty();
        }
        return this.actions.keySet().stream().filter(k -> k.cmd().equals(command)).findFirst();
    }

    public <T> Optional<T> getActionFromActionKey(String command, Class<T> type) {
        if (command == null) {
            return Optional.empty();
        }
        Objects.requireNonNull(type);
        return this.actions.keySet().stream().filter(k -> k.cmd().equals(command)).findFirst().map(this.actions::get).filter(type::isInstance).map(type::cast);
    }

    public Optional<Feature<? extends ActionState>> getLeftMouseActionFromKeyEvent(int keyEvent, int modifier) {
        if (keyEvent == 0) {
            return Optional.empty();
        }
        return this.actions.keySet().stream().filter(k -> k != null && k.getKeyCode() == keyEvent && k.getModifier() == modifier).findFirst();
    }

    public void changeLeftMouseAction(String command) {
        MouseActions mActions;
        ViewerToolBar toolBar;
        ImageViewerPlugin<E> view = this.getSelectedView2dContainer();
        if (view != null && (toolBar = view.getViewerToolBar()) != null && !command.equals((mActions = this.getMouseActions()).getAction("left"))) {
            mActions.setAction("left", command);
            view.setMouseActions(mActions);
            toolBar.changeButtonState("left", command);
        }
    }

    public void nextLeftMouseAction() {
        ViewerToolBar toolBar;
        ImageViewerPlugin<E> view = this.getSelectedView2dContainer();
        if (view != null && (toolBar = view.getViewerToolBar()) != null) {
            String command = ViewerToolBar.getNextCommand(ViewerToolBar.actionsButtons, toolBar.getMouseLeft().getActionCommand()).cmd();
            this.changeLeftMouseAction(command);
        }
    }

    public Optional<Feature<? extends ActionState>> getMouseAction(int modifiers) {
        Optional<Feature<? extends ActionState>> action = Optional.empty();
        if ((modifiers & 0x400) != 0) {
            action = this.getActionKey(this.mouseActions.getLeft());
        } else if ((modifiers & 0x800) != 0 && (this.mouseActions.getActiveButtons() & 0x800) != 0) {
            action = this.getActionKey(this.mouseActions.getMiddle());
        } else if ((modifiers & 0x1000) != 0 && (this.mouseActions.getActiveButtons() & 0x1000) != 0) {
            action = this.getActionKey(this.mouseActions.getRight());
        }
        return action;
    }

    public Collection<ActionState> getAllActionValues() {
        return this.actions.values().stream().filter(Objects::nonNull).toList();
    }

    public MouseActions getMouseActions() {
        return this.mouseActions;
    }

    public ZoomSetting getZoomSetting() {
        return this.zoomSetting;
    }

    public WProperties getOptions() {
        return this.options;
    }

    public synchronized void enableActions(boolean enabled) {
        this.enabledAction = enabled;
        for (ActionState a : this.getAllActionValues()) {
            a.enableAction(enabled);
        }
    }

    public ViewCanvas<E> getSelectedViewPane() {
        ImageViewerPlugin<E> container = this.selectedView2dContainer;
        if (container != null) {
            return container.getSelectedViewCanvas();
        }
        return null;
    }

    public ImageViewerPlugin<E> getSelectedView2dContainer() {
        return this.selectedView2dContainer;
    }

    public boolean isSelectedView2dContainerInTileMode() {
        ImageViewerPlugin<E> container = this.selectedView2dContainer;
        if (container != null) {
            return SynchData.Mode.TILE.equals((Object)container.getSynchView().getSynchData().getMode());
        }
        return false;
    }

    public void updateAllListeners(ImageViewerPlugin<E> viewerPlugin, SynchView synchView) {
        this.clearAllPropertyChangeListeners();
        this.synchManager.updateAllListeners(viewerPlugin, synchView);
    }

    protected boolean commonDisplayShortcuts(KeyEvent e) {
        int keyEvent = e.getKeyCode();
        int modifiers = e.getModifiers();
        ShortcutManager sm = ShortcutManager.getInstance();
        if (sm.matches("viewer.escape", keyEvent, modifiers)) {
            this.resetDisplay();
        } else if (keyEvent == ActionW.CINESTART.getKeyCode() && ActionW.CINESTART.getModifier() == modifiers) {
            Optional<SliderCineListener> cineAction = this.getAction(ActionW.SCROLL_SERIES);
            if (cineAction.isPresent() && cineAction.get().isActionEnabled()) {
                if (cineAction.get().isCining()) {
                    cineAction.get().stop();
                } else {
                    cineAction.get().start();
                }
            }
        } else if (sm.matches("viewer.print", keyEvent, modifiers)) {
            ImageViewerPlugin<E> view = this.getSelectedView2dContainer();
            if (view != null) {
                ColorLayerUI layer = ColorLayerUI.createTransparentLayerUI(view);
                PrintDialog dialog = new PrintDialog(SwingUtilities.getWindowAncestor(view), Messages.getString("ImageViewerPlugin.print_layout"), this);
                ColorLayerUI.showCenterScreen(dialog, layer);
            }
        } else if (sm.matches("viewer.scrollUp", keyEvent, modifiers)) {
            this.getAction(ActionW.SCROLL_SERIES).ifPresent(a -> a.setSliderValue(a.getSliderValue() - 1));
        } else if (sm.matches("viewer.scrollUpFast", keyEvent, modifiers)) {
            this.getAction(ActionW.SCROLL_SERIES).ifPresent(a -> a.setSliderValue(a.getSliderValue() - 10));
        } else if (sm.matches("viewer.scrollDown", keyEvent, modifiers)) {
            this.getAction(ActionW.SCROLL_SERIES).ifPresent(a -> a.setSliderValue(a.getSliderValue() + 1));
        } else if (sm.matches("viewer.scrollDownFast", keyEvent, modifiers)) {
            this.getAction(ActionW.SCROLL_SERIES).ifPresent(a -> a.setSliderValue(a.getSliderValue() + 10));
        } else if (sm.matches("viewer.scrollFirst", keyEvent, modifiers)) {
            this.getAction(ActionW.SCROLL_SERIES).ifPresent(a -> a.setSliderValue(a.getSliderMin()));
        } else if (sm.matches("viewer.scrollLast", keyEvent, modifiers)) {
            this.getAction(ActionW.SCROLL_SERIES).ifPresent(a -> a.setSliderValue(a.getSliderMax()));
        } else if (sm.matches("viewer.zoomOut", keyEvent, modifiers)) {
            this.getAction(ActionW.ZOOM).ifPresent(a -> a.setSliderValue(a.getSliderValue() - 1));
        } else if (sm.matches("viewer.zoomIn", keyEvent, modifiers)) {
            this.getAction(ActionW.ZOOM).ifPresent(a -> a.setSliderValue(a.getSliderValue() + 1));
        } else if (sm.matches("viewer.bestFit", keyEvent, modifiers)) {
            this.firePropertyChange(ActionW.SYNCH.cmd(), null, new SynchEvent(this.getSelectedViewPane(), ActionW.ZOOM.cmd(), -200.0));
        } else {
            return false;
        }
        return true;
    }

    protected void triggerDrawingToolKeyEvent(int keyEvent, int modifiers) {
        this.triggerDrawActionKeyEvent(ActionW.DRAW_MEASURE, ActionW.MEASURE.cmd(), keyEvent, modifiers);
        this.triggerDrawActionKeyEvent(ActionW.DRAW_GRAPHICS, ActionW.DRAW.cmd(), keyEvent, modifiers);
    }

    private void triggerDrawActionKeyEvent(Feature<? extends ComboItemListener<Graphic>> action, String cmd, int keyEvent, int modifiers) {
        Optional<? extends ComboItemListener<Graphic>> drawAction = this.getAction(action);
        if (drawAction.isPresent() && drawAction.get().isActionEnabled()) {
            for (Object obj : drawAction.get().getAllItem()) {
                ViewerToolBar toolBar;
                Graphic g;
                if (!(obj instanceof Graphic) || (g = (Graphic)obj).getKeyCode() != keyEvent || g.getModifier() != modifiers) continue;
                ImageViewerPlugin<E> view = this.getSelectedView2dContainer();
                if (view != null && (toolBar = view.getViewerToolBar()) != null && !toolBar.isCommandActive(cmd)) {
                    this.mouseActions.setAction("left", cmd);
                    view.setMouseActions(this.mouseActions);
                    toolBar.changeButtonState("left", cmd);
                }
                drawAction.get().setSelectedItem(obj);
                return;
            }
        }
    }

    protected boolean isCompatible(MediaSeries<E> series, MediaSeries<E> series2) {
        return true;
    }

    public void setSelectedView2dContainer(ImageViewerPlugin<E> selectedView2dContainer) {
        if (this.selectedView2dContainer != null) {
            this.selectedView2dContainer.setMouseActions(null);
        }
        this.selectedView2dContainer = selectedView2dContainer;
        if (selectedView2dContainer != null) {
            selectedView2dContainer.setMouseActions(this.mouseActions);
        }
    }

    public abstract void resetDisplay();

    public String resolvePlaceholders(String template) {
        return template;
    }

    public void dicomExportAction(Launcher launcher) {
    }
}

