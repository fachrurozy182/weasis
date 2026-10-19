/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.editor.image;

import java.awt.GraphicsConfiguration;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.DropButtonIcon;
import org.weasis.core.api.gui.util.DropDownButton;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.service.AuditLog;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.SynchEvent;
import org.weasis.core.ui.editor.image.dockable.MeasureTool;
import org.weasis.core.ui.pref.Monitor;
import org.weasis.core.ui.util.WtoolBar;

public class ZoomToolBar
extends WtoolBar {
    public ZoomToolBar(final ImageViewerEventManager<?> eventManager, int index, boolean showLens) {
        super(Messages.getString("ResetTools.zoom"), index);
        if (eventManager == null) {
            throw new IllegalArgumentException("EventManager cannot be null");
        }
        DropDownButton zoom = new DropDownButton(this, "zoom", (Icon)new DropButtonIcon((Icon)ResourceUtil.getToolBarIcon(ResourceUtil.ActionIcon.ZOOM))){
            final /* synthetic */ ZoomToolBar this$0;
            {
                ZoomToolBar zoomToolBar = this$0;
                Objects.requireNonNull(zoomToolBar);
                this.this$0 = zoomToolBar;
                super(type, icon);
            }

            @Override
            protected JPopupMenu getPopupMenu() {
                return this.this$0.getZoomPopupMenuButton(this, eventManager);
            }
        };
        zoom.setToolTipText(Messages.getString("ZoomToolBar.zoom_type"));
        this.add(zoom);
        eventManager.getAction(ActionW.ZOOM).ifPresent(s -> s.registerActionState(zoom));
        if (showLens) {
            JToggleButton jButtonLens = new JToggleButton((Icon)ResourceUtil.getToolBarIcon(ResourceUtil.ActionIcon.ZOOM_PAN));
            jButtonLens.setToolTipText(Messages.getString("ViewerToolBar.show_lens"));
            eventManager.getAction(ActionW.LENS).ifPresent(s -> s.registerActionState(jButtonLens));
            this.add(jButtonLens);
        }
    }

    private JPopupMenu getZoomPopupMenuButton(DropDownButton dropDownButton, ImageViewerEventManager<?> eventManager) {
        JPopupMenu popupMouseButtons = new JPopupMenu();
        for (JMenuItem jMenuItem : ZoomToolBar.getZoomListMenuItems(eventManager)) {
            popupMouseButtons.add(jMenuItem);
        }
        return popupMouseButtons;
    }

    public static List<JMenuItem> getZoomListMenuItems(ImageViewerEventManager<?> eventManager) {
        double realFactor;
        GraphicsConfiguration config;
        Monitor monitor;
        Window win;
        ArrayList<JMenuItem> list = new ArrayList<JMenuItem>();
        JMenuItem actualZoomMenu = new JMenuItem(Messages.getString("ViewerToolBar.zoom_1"), (Icon)ResourceUtil.getIcon(ResourceUtil.ActionIcon.ZOOM_ORIGINAL));
        GuiUtils.applySelectedIconEffect(actualZoomMenu);
        actualZoomMenu.addActionListener(e -> eventManager.getAction(ActionW.ZOOM).ifPresent(s -> s.setRealValue(1.0)));
        list.add(actualZoomMenu);
        ImageViewerPlugin<?> selCt = eventManager.getSelectedView2dContainer();
        Window window = win = selCt == null ? null : SwingUtilities.getWindowAncestor(selCt);
        if (win != null && (monitor = MeasureTool.viewSetting.getMonitor((config = win.getGraphicsConfiguration()).getDevice())) != null && (realFactor = monitor.getRealScaleFactor()) > 0.0) {
            JMenuItem realSizeMenu = new JMenuItem(Messages.getString("ZoomToolBar.real_zoom"), (Icon)ResourceUtil.getIcon(ResourceUtil.ActionIcon.ZOOM_REAL_WORLD));
            GuiUtils.applySelectedIconEffect(realSizeMenu);
            realSizeMenu.addActionListener(e -> {
                eventManager.firePropertyChange(ActionW.SYNCH.cmd(), null, new SynchEvent(null, ActionW.ZOOM.cmd(), -100.0));
                AuditLog.LOGGER.info("action:{} val:-100.0", (Object)ActionW.ZOOM.cmd());
            });
            list.add(realSizeMenu);
        }
        JMenuItem bestFitMenu = new JMenuItem(Messages.getString("ViewerToolBar.zoom_b"), (Icon)ResourceUtil.getIcon(ResourceUtil.ActionIcon.ZOOM_BEST_FIT));
        GuiUtils.applySelectedIconEffect(bestFitMenu);
        bestFitMenu.addActionListener(e -> {
            eventManager.firePropertyChange(ActionW.SYNCH.cmd(), null, new SynchEvent(null, ActionW.ZOOM.cmd(), -200.0));
            AuditLog.LOGGER.info("action:{} val:-200.0", (Object)ActionW.ZOOM.cmd());
        });
        list.add(bestFitMenu);
        return list;
    }
}

