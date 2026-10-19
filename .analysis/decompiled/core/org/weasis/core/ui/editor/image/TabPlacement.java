/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  bibliothek.gui.DockStation
 *  bibliothek.gui.Dockable
 *  bibliothek.gui.dock.SplitDockStation
 *  bibliothek.gui.dock.StackDockStation
 *  bibliothek.gui.dock.action.DefaultDockActionSource
 *  bibliothek.gui.dock.action.DockAction
 *  bibliothek.gui.dock.action.DockActionSource
 *  bibliothek.gui.dock.action.view.ActionViewConverter
 *  bibliothek.gui.dock.action.view.ViewTarget
 *  bibliothek.gui.dock.common.DefaultSingleCDockable
 *  bibliothek.gui.dock.common.action.CAction
 *  bibliothek.gui.dock.common.action.CButton
 *  bibliothek.gui.dock.common.action.core.CommonDecoratableDockAction
 *  bibliothek.gui.dock.common.action.core.CommonSimpleMenuAction
 *  bibliothek.gui.dock.common.intern.DefaultCommonDockable
 *  bibliothek.gui.dock.common.intern.action.CDecorateableAction
 *  bibliothek.gui.dock.layout.DockableProperty
 *  bibliothek.gui.dock.station.split.SplitDockProperty
 *  bibliothek.gui.dock.util.DockUtilities
 */
package org.weasis.core.ui.editor.image;

import bibliothek.gui.DockStation;
import bibliothek.gui.Dockable;
import bibliothek.gui.dock.SplitDockStation;
import bibliothek.gui.dock.StackDockStation;
import bibliothek.gui.dock.action.DefaultDockActionSource;
import bibliothek.gui.dock.action.DockAction;
import bibliothek.gui.dock.action.DockActionSource;
import bibliothek.gui.dock.action.view.ActionViewConverter;
import bibliothek.gui.dock.action.view.ViewTarget;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.action.CAction;
import bibliothek.gui.dock.common.action.CButton;
import bibliothek.gui.dock.common.action.core.CommonDecoratableDockAction;
import bibliothek.gui.dock.common.action.core.CommonSimpleMenuAction;
import bibliothek.gui.dock.common.intern.DefaultCommonDockable;
import bibliothek.gui.dock.common.intern.action.CDecorateableAction;
import bibliothek.gui.dock.layout.DockableProperty;
import bibliothek.gui.dock.station.split.SplitDockProperty;
import bibliothek.gui.dock.util.DockUtilities;
import java.awt.Component;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.ui.editor.SplitPosition;
import org.weasis.core.ui.editor.image.AreaIcon;
import org.weasis.core.ui.editor.image.ViewerPlugin;

final class TabPlacement {
    private static final int MAX_MENU_LABEL_LENGTH = 60;

    private TabPlacement() {
    }

    static List<CAction> actions(ViewerPlugin<?> plugin) {
        return List.of(new SplitAction(plugin, SplitPosition.RIGHT), new SplitAction(plugin, SplitPosition.BOTTOM), new MoveToGroupMenu(plugin));
    }

    private static Dockable tabGroupOf(Dockable dockable) {
        Dockable dockable2;
        DockStation dockStation = dockable.getDockParent();
        if (dockStation instanceof StackDockStation) {
            StackDockStation stack = (StackDockStation)dockStation;
            dockable2 = stack;
        } else {
            dockable2 = dockable;
        }
        return dockable2;
    }

    private static SplitDockStation workingArea() {
        return GuiUtils.getUICore().getMainArea().getStation();
    }

    private static boolean detachTab(SplitDockStation area, Dockable dockable) {
        DockStation parent = dockable.getDockParent();
        if (parent == null || !DockUtilities.acceptable((DockStation)area, (Dockable)dockable)) {
            return false;
        }
        parent.drag(dockable);
        return true;
    }

    private static void dropTab(SplitDockStation area, Dockable dockable, DockableProperty location) {
        if (location == null || !area.drop(dockable, location)) {
            area.drop(dockable);
        }
    }

    private static Rectangle areaBounds(Component component) {
        SplitDockStation area = TabPlacement.workingArea();
        if (component == null || !component.isShowing() || area.getWidth() <= 0 || area.getHeight() <= 0) {
            return null;
        }
        return SwingUtilities.convertRectangle(component, new Rectangle(component.getSize()), (Component)area);
    }

    private static SplitDockProperty splitProperty(SplitDockStation area, Rectangle bounds) {
        double width = area.getWidth();
        double height = area.getHeight();
        return new SplitDockProperty((double)bounds.x / width, (double)bounds.y / height, (double)bounds.width / width, (double)bounds.height / height);
    }

    private static String menuLabel(String text) {
        return text.length() > 60 ? text.substring(0, 60) + "\u2026" : text;
    }

    private static String groupLabel(List<ViewerPlugin<?>> members) {
        return members.stream().map(ViewerPlugin::getPluginName).distinct().collect(Collectors.joining(", "));
    }

    private static Icon areaIcon(Component component) {
        Rectangle bounds = TabPlacement.areaBounds(component);
        if (bounds == null) {
            return null;
        }
        SplitDockProperty place = TabPlacement.splitProperty(TabPlacement.workingArea(), bounds);
        return new AreaIcon(place.getX(), place.getY(), place.getWidth(), place.getHeight());
    }

    private static class SplitAction
    extends CButton {
        private final ViewerPlugin<?> plugin;
        private final boolean down;

        public SplitAction(ViewerPlugin<?> plugin, SplitPosition position) {
            super(null);
            this.plugin = plugin;
            this.down = position == SplitPosition.BOTTOM;
            this.init(new ViewerPlugin.MenuOnlySimpleAction((CAction)this, this::refreshEnabled));
            this.setText(Messages.getString(this.down ? "ViewerPlugin.split_down" : "ViewerPlugin.split_right"));
            this.setTooltip(Messages.getString(this.down ? "ViewerPlugin.split_down_tip" : "ViewerPlugin.split_right_tip"));
        }

        private void refreshEnabled() {
            this.setEnabled(this.currentTabGroup() != null);
        }

        private StackDockStation currentTabGroup() {
            StackDockStation stack;
            DockStation dockStation = this.plugin.getDockable().intern().getDockParent();
            return dockStation instanceof StackDockStation && (stack = (StackDockStation)dockStation).getDockableCount() > 1 ? stack : null;
        }

        protected void action() {
            Rectangle bounds;
            super.action();
            StackDockStation group = this.currentTabGroup();
            Rectangle rectangle = bounds = group == null ? null : TabPlacement.areaBounds(group.getComponent());
            if (bounds == null) {
                return;
            }
            if (this.down) {
                bounds.height /= 2;
                bounds.y += bounds.height;
            } else {
                bounds.width /= 2;
                bounds.x += bounds.width;
            }
            SplitDockStation area = TabPlacement.workingArea();
            DefaultCommonDockable dockable = this.plugin.getDockable().intern();
            if (TabPlacement.detachTab(area, (Dockable)dockable)) {
                TabPlacement.dropTab(area, (Dockable)dockable, (DockableProperty)TabPlacement.splitProperty(area, bounds));
                this.plugin.setSelectedAndGetFocus();
            }
        }
    }

    private static class MoveToGroupMenu
    extends CDecorateableAction<CommonSimpleMenuAction> {
        private final ViewerPlugin<?> plugin;
        private final DefaultDockActionSource entries = new DefaultDockActionSource(new DockAction[0]);

        public MoveToGroupMenu(ViewerPlugin<?> plugin) {
            super(null);
            this.plugin = plugin;
            this.init((CommonDecoratableDockAction)new MenuOnlySimpleMenuAction((CAction)this, (DockActionSource)this.entries, this::rebuild));
            this.setText(Messages.getString("ViewerPlugin.move_group"));
            this.setTooltip(Messages.getString("ViewerPlugin.move_group_tip"));
        }

        private void rebuild() {
            this.entries.removeAll();
            this.buildEntries().forEach(entry -> this.entries.add(new DockAction[]{entry.intern()}));
            this.setEnabled(this.entries.getDockActionCount() > 0);
        }

        private List<CAction> buildEntries() {
            DefaultSingleCDockable dock = this.plugin.getDockable();
            Dockable current = TabPlacement.tabGroupOf((Dockable)dock.intern());
            LinkedHashMap<Dockable, List> groups = new LinkedHashMap<Dockable, List>();
            for (ViewerPlugin<?> viewer : GuiUtils.getUICore().getViewerPlugins()) {
                Dockable group2;
                DefaultSingleCDockable other = viewer.getDockable();
                if (!other.isVisible() || other.getWorkingArea() != dock.getWorkingArea() || (group2 = TabPlacement.tabGroupOf((Dockable)other.intern())) == current) continue;
                groups.computeIfAbsent(group2, g -> new ArrayList()).add(viewer);
            }
            return groups.entrySet().stream().map(group -> new MoveToGroupAction(this.plugin, (Dockable)group.getKey(), (List)group.getValue())).toList();
        }
    }

    private static class MenuOnlySimpleMenuAction
    extends CommonSimpleMenuAction {
        private final Runnable beforeCreateView;

        public MenuOnlySimpleMenuAction(CAction action, DockActionSource menu, Runnable beforeCreateView) {
            super(action, menu);
            this.beforeCreateView = beforeCreateView;
        }

        public <V> V createView(ViewTarget<V> target, ActionViewConverter converter, Dockable dockable) {
            if (ViewTarget.TITLE == target) {
                return null;
            }
            this.beforeCreateView.run();
            return (V)super.createView(target, converter, dockable);
        }
    }

    private static class MoveToGroupAction
    extends CButton {
        private final ViewerPlugin<?> plugin;
        private final Dockable group;

        public MoveToGroupAction(ViewerPlugin<?> plugin, Dockable group, List<ViewerPlugin<?>> members) {
            super(TabPlacement.menuLabel(TabPlacement.groupLabel(members)), TabPlacement.areaIcon(group.getComponent()));
            this.plugin = plugin;
            this.group = group;
        }

        protected void action() {
            DockableProperty location;
            StackDockStation s;
            super.action();
            SplitDockStation area = TabPlacement.workingArea();
            DefaultCommonDockable dockable = this.plugin.getDockable().intern();
            Dockable dockable2 = this.group;
            StackDockStation stack = dockable2 instanceof StackDockStation ? (s = (StackDockStation)dockable2) : null;
            DockableProperty dockableProperty = location = stack == null ? DockUtilities.getPropertyChain((DockStation)area, (Dockable)this.group) : null;
            if (!TabPlacement.detachTab(area, (Dockable)dockable)) {
                return;
            }
            if (stack == null) {
                TabPlacement.dropTab(area, (Dockable)dockable, location);
            } else {
                stack.drop((Dockable)dockable);
            }
            this.plugin.setSelectedAndGetFocus();
        }
    }
}

