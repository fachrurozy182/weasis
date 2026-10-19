/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  bibliothek.gui.DockStation
 *  bibliothek.gui.Dockable
 *  bibliothek.gui.dock.DockElementRepresentative
 *  bibliothek.gui.dock.StackDockStation
 *  bibliothek.gui.dock.action.view.ActionViewConverter
 *  bibliothek.gui.dock.action.view.ViewTarget
 *  bibliothek.gui.dock.common.CControl
 *  bibliothek.gui.dock.common.CLocation
 *  bibliothek.gui.dock.common.CWorkingArea
 *  bibliothek.gui.dock.common.DefaultSingleCDockable
 *  bibliothek.gui.dock.common.SingleCDockable
 *  bibliothek.gui.dock.common.action.CAction
 *  bibliothek.gui.dock.common.action.CButton
 *  bibliothek.gui.dock.common.action.CSeparator
 *  bibliothek.gui.dock.common.action.core.CommonSimpleButtonAction
 *  bibliothek.gui.dock.common.action.predefined.CCloseAction
 *  bibliothek.gui.dock.common.event.CVetoFocusListener
 *  bibliothek.gui.dock.common.intern.AbstractCDockable
 *  bibliothek.gui.dock.common.intern.CDockable
 *  bibliothek.gui.dock.common.intern.CommonDockable
 *  bibliothek.gui.dock.common.intern.DefaultCommonDockable
 *  bibliothek.gui.dock.common.mode.ExtendedMode
 *  bibliothek.gui.dock.control.focus.DefaultFocusRequest
 *  bibliothek.gui.dock.control.focus.FocusRequest
 *  com.formdev.flatlaf.extras.FlatSVGIcon
 */
package org.weasis.core.ui.editor.image;

import bibliothek.gui.DockStation;
import bibliothek.gui.Dockable;
import bibliothek.gui.dock.DockElementRepresentative;
import bibliothek.gui.dock.StackDockStation;
import bibliothek.gui.dock.action.view.ActionViewConverter;
import bibliothek.gui.dock.action.view.ViewTarget;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.CWorkingArea;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.action.CAction;
import bibliothek.gui.dock.common.action.CButton;
import bibliothek.gui.dock.common.action.CSeparator;
import bibliothek.gui.dock.common.action.core.CommonSimpleButtonAction;
import bibliothek.gui.dock.common.action.predefined.CCloseAction;
import bibliothek.gui.dock.common.event.CVetoFocusListener;
import bibliothek.gui.dock.common.intern.AbstractCDockable;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.intern.CommonDockable;
import bibliothek.gui.dock.common.intern.DefaultCommonDockable;
import bibliothek.gui.dock.common.mode.ExtendedMode;
import bibliothek.gui.dock.control.focus.DefaultFocusRequest;
import bibliothek.gui.dock.control.focus.FocusRequest;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.weasis.core.Messages;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.ui.docking.DockableTool;
import org.weasis.core.ui.editor.SeriesViewer;
import org.weasis.core.ui.editor.SeriesViewerUI;
import org.weasis.core.ui.editor.SplitLayout;
import org.weasis.core.ui.editor.TabFocusPolicy;
import org.weasis.core.ui.editor.ViewerOpenOptions;
import org.weasis.core.ui.editor.ViewerPlacement;
import org.weasis.core.ui.editor.ViewerPluginBuilder;
import org.weasis.core.ui.editor.image.TabPlacement;
import org.weasis.core.ui.editor.image.ViewerToolBar;
import org.weasis.core.ui.util.Toolbar;

public abstract class ViewerPlugin<E extends MediaElement>
extends JPanel
implements SeriesViewer<E> {
    private final String dockableUID;
    private MediaSeriesGroup groupID;
    private String pluginName;
    private final Icon icon;
    private final String tooltips;
    private final DefaultSingleCDockable dockable;

    protected ViewerPlugin(String pluginName) {
        this(null, pluginName, null, null);
    }

    protected ViewerPlugin(String uid, String pluginName, Icon icon, String tooltips) {
        Icon icon2;
        this.setLayout(new BorderLayout());
        this.setName(pluginName);
        this.pluginName = pluginName;
        this.icon = icon;
        this.tooltips = tooltips;
        String string = this.dockableUID = uid == null ? UUID.randomUUID().toString() : uid;
        if (icon instanceof FlatSVGIcon) {
            FlatSVGIcon flatSVGIcon = (FlatSVGIcon)icon;
            icon2 = flatSVGIcon.derive(20, 20);
        } else {
            icon2 = icon;
        }
        Icon titleIcon = icon2;
        this.dockable = new DefaultSingleCDockable(this.dockableUID, titleIcon, pluginName, new CAction[0]);
        this.dockable.setTitleText(pluginName);
        this.dockable.setTitleToolTip(tooltips);
        this.dockable.setTitleIcon(titleIcon);
        this.dockable.setFocusComponent((Component)this);
        this.dockable.setStackable(true);
        this.dockable.setSingleTabShown(true);
        this.dockable.putAction("cdockable.close", (CAction)new CCloseAction(this, GuiUtils.getUICore().getDockingControl()){
            final /* synthetic */ ViewerPlugin this$0;
            {
                ViewerPlugin viewerPlugin = this$0;
                Objects.requireNonNull(viewerPlugin);
                this.this$0 = viewerPlugin;
                super(arg0);
            }

            public void close(CDockable dockable) {
                DefaultCommonDockable defaultCommonDockable;
                Dockable prevDockable;
                DockableTool p2;
                CControl control = GuiUtils.getUICore().getDockingControl();
                CVetoFocusListener vetoFocus = GuiUtils.getUICore().getDockingVetoFocus();
                control.addVetoFocusListener(vetoFocus);
                super.close(dockable);
                control.removeVetoFocusListener(vetoFocus);
                Component component = dockable.getFocusComponent();
                if (component instanceof SeriesViewer) {
                    SeriesViewer seriesViewer = (SeriesViewer)((Object)component);
                    List<DockableTool> oldTool = seriesViewer.getSeriesViewerUI().tools;
                    for (DockableTool p2 : oldTool) {
                        p2.closeDockable();
                    }
                    seriesViewer.close();
                }
                if ((prevDockable = control.getController().getFocusHistory().getNewestOn(new DockStation[]{dockable.getWorkingArea().getStation()})) == null) {
                    this.this$0.handleFocusAfterClosing();
                } else if (prevDockable instanceof DefaultCommonDockable && (p2 = (defaultCommonDockable = (DefaultCommonDockable)prevDockable).getDockable()) instanceof AbstractCDockable) {
                    AbstractCDockable abstractCDockable = (AbstractCDockable)p2;
                    Component component2 = abstractCDockable.getFocusComponent();
                    if (component2 instanceof SeriesViewer) {
                        SeriesViewer plugin = (SeriesViewer)((Object)component2);
                        SeriesViewerUI.updateTools(null, plugin, true);
                    }
                    abstractCDockable.toFront();
                }
            }
        });
        this.dockable.setCloseable(true);
        this.dockable.setMinimizable(false);
        this.dockable.setExternalizable(false);
        this.dockable.addAction((CAction)new CloseOthersAction((CDockable)this.dockable, false));
        this.dockable.addAction((CAction)new CloseOthersAction((CDockable)this.dockable, true));
        this.dockable.addAction((CAction)CSeparator.MENU_SEPARATOR);
        TabPlacement.actions(this).forEach(arg_0 -> ((DefaultSingleCDockable)this.dockable).addAction(arg_0));
    }

    @Override
    public MediaSeriesGroup getGroupID() {
        return this.groupID;
    }

    public void setGroupID(MediaSeriesGroup groupID) {
        this.groupID = groupID;
    }

    @Override
    public String getPluginName() {
        return this.pluginName;
    }

    public Icon getIcon() {
        return this.icon;
    }

    public String getTooltips() {
        return this.tooltips;
    }

    @Override
    public String getDockableUID() {
        return this.dockableUID;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
        this.dockable.setTitleText(pluginName);
    }

    public void setSelectedAndGetFocus() {
        GuiUtils.getUICore().getDockingControl().getController().setFocusedDockable((FocusRequest)new DefaultFocusRequest((DockElementRepresentative)this.dockable.intern(), (Component)this, false, true, false));
    }

    public void handleFocusAfterClosing() {
        List<ViewerPlugin<?>> viewerPlugins = GuiUtils.getUICore().getViewerPlugins();
        int size = viewerPlugins.size();
        if (size > 0) {
            ViewerPlugin<?> lp = viewerPlugins.get(size - 1);
            if (lp != null) {
                lp.dockable.toFront();
            }
        } else {
            ViewerPluginBuilder.DefaultDataModel.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.NULL_SELECTION, this.getSeriesViewerUI(), null, null));
        }
    }

    @Override
    public void close() {
        GuiExecutor.execute(() -> {
            GuiUtils.getUICore().getViewerPlugins().remove(this);
            GuiUtils.getUICore().getDockingControl().removeDockable((SingleCDockable)this.dockable);
        });
    }

    public Component getComponent() {
        return this;
    }

    public final DefaultSingleCDockable getDockable() {
        return this.dockable;
    }

    public void showDockable(ViewerOpenOptions options) {
        SplitLayout splitLayout;
        ViewerOpenOptions opts = options == null ? ViewerOpenOptions.defaults() : options;
        ViewerPlacement viewerPlacement = opts.placement();
        if (viewerPlacement instanceof ViewerPlacement.Split) {
            ViewerPlacement.Split split = (ViewerPlacement.Split)viewerPlacement;
            splitLayout = split.splitLayout();
        } else {
            splitLayout = SplitLayout.NONE;
        }
        SplitLayout splitLayout2 = splitLayout;
        TabFocusPolicy focusPolicy = opts.tabFocusPolicy();
        GuiExecutor.execute(() -> {
            if (!this.dockable.isVisible()) {
                List<ViewerPlugin<?>> viewerPlugins = GuiUtils.getUICore().getViewerPlugins();
                if (!viewerPlugins.contains(this)) {
                    viewerPlugins.add(this);
                }
                this.dockable.add(this.getComponent());
                this.dockable.setFocusComponent((Component)this);
                CWorkingArea mainArea = GuiUtils.getUICore().getMainArea();
                mainArea.add((SingleCDockable)this.getDockable());
                this.dockable.setDefaultLocation(ExtendedMode.NORMALIZED, (CLocation)CLocation.working((CWorkingArea)mainArea).stack());
                CControl ctl = GuiUtils.getUICore().getDockingControl();
                StackDockStation viewerStack = null;
                CommonDockable previousFront = null;
                DefaultSingleCDockable singleDockable = null;
                for (ViewerPlugin<?> vp : viewerPlugins) {
                    if (vp == this || !vp.getDockable().isVisible()) continue;
                    DockStation parent = vp.getDockable().intern().getDockParent();
                    if (parent instanceof StackDockStation) {
                        StackDockStation stack;
                        viewerStack = stack = (StackDockStation)parent;
                        previousFront = stack.getFrontDockable();
                        break;
                    }
                    if (singleDockable != null) continue;
                    singleDockable = vp.getDockable();
                }
                if (viewerStack == null && singleDockable != null) {
                    previousFront = singleDockable.intern();
                }
                if (!splitLayout2.isSplit()) {
                    DefaultSingleCDockable lastInStack = null;
                    if (viewerStack != null) {
                        Dockable last;
                        int count = viewerStack.getDockableCount();
                        if (count > 0 && (last = viewerStack.getDockable(count - 1)) instanceof CommonDockable) {
                            CommonDockable commonDockable = (CommonDockable)last;
                            lastInStack = commonDockable.getDockable();
                        }
                    } else if (singleDockable != null) {
                        lastInStack = singleDockable;
                    }
                    if (lastInStack != null) {
                        DefaultSingleCDockable target = lastInStack;
                        this.dockable.setLocationsAside(item -> item == target);
                    } else {
                        this.dockable.setLocationsAsideFocused();
                    }
                } else {
                    CLocation explicitLocation = splitLayout2.toLocation(mainArea);
                    if (explicitLocation != null) {
                        this.dockable.setLocation(explicitLocation);
                    } else {
                        CDockable focused;
                        DockStation focusedParent = (focused = ctl.getFocusedCDockable()) != null ? focused.intern().getDockParent() : null;
                        boolean focusedInStack = focusedParent instanceof StackDockStation;
                        boolean found = this.dockable.setLocationsAside(item -> item != this.dockable && item != focused && item.getWorkingArea() == this.dockable.getWorkingArea() && item.isVisible() && (!focusedInStack || item.intern().getDockParent() != focusedParent));
                        if (!found) {
                            this.dockable.setLocation((CLocation)CLocation.working((CWorkingArea)mainArea).east(splitLayout2.ratio()));
                        }
                    }
                }
                CVetoFocusListener vetoFocus = GuiUtils.getUICore().getDockingVetoFocus();
                ctl.addVetoFocusListener(vetoFocus);
                this.dockable.setVisible(true);
                ctl.removeVetoFocusListener(vetoFocus);
                if (!focusPolicy.shouldBringToFront() && previousFront != null) {
                    DockStation newParent = this.dockable.intern().getDockParent();
                    if (newParent instanceof StackDockStation) {
                        StackDockStation stack = (StackDockStation)newParent;
                        stack.setFrontDockable((Dockable)previousFront);
                    } else if (viewerStack != null) {
                        viewerStack.setFrontDockable((Dockable)previousFront);
                    }
                }
                this.setSelectedAndGetFocus();
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ViewerToolBar getViewerToolBar() {
        List<Toolbar> bars;
        List<Toolbar> list = bars = this.getSeriesViewerUI().toolBars;
        synchronized (list) {
            for (Toolbar t : bars) {
                if (!(t instanceof ViewerToolBar)) continue;
                ViewerToolBar viewerToolBar = (ViewerToolBar)t;
                return viewerToolBar;
            }
        }
        return null;
    }

    private static class CloseOthersAction
    extends CButton {
        private final CDockable dockable;
        private final boolean closeAll;

        public CloseOthersAction(CDockable dockable, boolean closeAll) {
            super(null);
            this.init(new MenuOnlySimpleAction((CAction)this));
            this.dockable = dockable;
            this.closeAll = closeAll;
            this.setText(closeAll ? Messages.getString("ViewerPlugin.close_all") : Messages.getString("ViewerPlugin.close_other"));
        }

        protected void action() {
            super.action();
            Component component = this.dockable.getFocusComponent();
            if (component instanceof SeriesViewer) {
                SeriesViewer plugin = (SeriesViewer)((Object)component);
                SeriesViewerUI.updateTools(plugin, this.closeAll ? null : plugin, true);
            }
            DockStation parent = this.dockable.intern().getDockParent();
            Dockable[] children = new Dockable[parent.getDockableCount()];
            for (int i = 0; i < children.length; ++i) {
                children[i] = parent.getDockable(i);
            }
            for (Dockable child : children) {
                CommonDockable commonDockable;
                CDockable cChild;
                if (!(child instanceof CommonDockable) || !(cChild = (commonDockable = (CommonDockable)child).getDockable()).isCloseable() || !this.closeAll && cChild == this.dockable) continue;
                Component component2 = cChild.getFocusComponent();
                if (component2 instanceof SeriesViewer) {
                    SeriesViewer seriesViewer = (SeriesViewer)((Object)component2);
                    seriesViewer.close();
                    Component component3 = cChild.getFocusComponent();
                    if (!(component3 instanceof ViewerPlugin)) continue;
                    ViewerPlugin viewerPlugin = (ViewerPlugin)component3;
                    viewerPlugin.handleFocusAfterClosing();
                    continue;
                }
                cChild.setVisible(false);
            }
        }
    }

    static class MenuOnlySimpleAction
    extends CommonSimpleButtonAction {
        private final Runnable beforeCreateView;

        public MenuOnlySimpleAction(CAction action) {
            this(action, null);
        }

        public MenuOnlySimpleAction(CAction action, Runnable beforeCreateView) {
            super(action);
            this.beforeCreateView = beforeCreateView;
        }

        public <V> V createView(ViewTarget<V> target, ActionViewConverter converter, Dockable dockable) {
            if (ViewTarget.TITLE == target) {
                return null;
            }
            if (this.beforeCreateView != null) {
                this.beforeCreateView.run();
            }
            return (V)super.createView(target, converter, dockable);
        }
    }
}

