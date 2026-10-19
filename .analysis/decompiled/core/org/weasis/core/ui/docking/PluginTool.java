/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  bibliothek.gui.dock.common.CContentArea
 *  bibliothek.gui.dock.common.CControl
 *  bibliothek.gui.dock.common.CLocation
 *  bibliothek.gui.dock.common.DefaultSingleCDockable
 *  bibliothek.gui.dock.common.SingleCDockable
 *  bibliothek.gui.dock.common.action.CAction
 *  bibliothek.gui.dock.common.event.CVetoFocusListener
 *  bibliothek.gui.dock.common.location.CBaseLocation
 *  bibliothek.gui.dock.common.location.CFlapIndexLocation
 *  bibliothek.gui.dock.common.location.TreeLocationRoot
 *  bibliothek.gui.dock.common.mode.ExtendedMode
 */
package org.weasis.core.ui.docking;

import bibliothek.gui.dock.common.CContentArea;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.action.CAction;
import bibliothek.gui.dock.common.event.CVetoFocusListener;
import bibliothek.gui.dock.common.location.CBaseLocation;
import bibliothek.gui.dock.common.location.CFlapIndexLocation;
import bibliothek.gui.dock.common.location.TreeLocationRoot;
import bibliothek.gui.dock.common.mode.ExtendedMode;
import java.awt.Component;
import java.util.UUID;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import org.weasis.core.api.gui.Insertable;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.ui.docking.DockableTool;

public abstract class PluginTool
extends JPanel
implements DockableTool {
    private final Insertable.Type type;
    private int toolPosition;
    private int dockableWidth = -1;
    protected final DefaultSingleCDockable dockable;
    protected POSITION defaultPosition;
    protected ExtendedMode defaultExtendedMode;
    protected ExtendedMode previousExtendedMode;

    protected PluginTool(String toolName, Insertable.Type type, int position) {
        this(toolName, null, null, type, position);
    }

    protected PluginTool(String toolName, POSITION defaultPosition, ExtendedMode defaultMode, Insertable.Type type, int position) {
        this.type = type;
        this.toolPosition = position;
        this.defaultPosition = defaultPosition;
        this.defaultExtendedMode = defaultMode;
        this.dockable = new DefaultSingleCDockable(UUID.randomUUID().toString(), null, toolName, new CAction[0]);
        this.dockable.setTitleText(toolName);
        this.dockable.setExternalizable(false);
        this.dockable.setMaximizable(false);
        this.dockable.addCDockableLocationListener(event -> {
            if (event.isLocationChanged()) {
                this.changeToolWindowAnchor(event.getNewLocation());
            }
        });
    }

    protected abstract void changeToolWindowAnchor(CLocation var1);

    @Override
    public Insertable.Type getType() {
        return this.type;
    }

    @Override
    public String getComponentName() {
        return this.dockable.getTitleText();
    }

    @Override
    public int getComponentPosition() {
        return this.toolPosition;
    }

    @Override
    public void setComponentPosition(int position) {
        this.toolPosition = position;
    }

    @Override
    public Component getToolComponent() {
        return this;
    }

    protected Component getToolComponentFromJScrollPane(JScrollPane rootPane) {
        JViewport viewPort = rootPane.getViewport();
        if (viewPort == null) {
            viewPort = new JViewport();
            rootPane.setViewport(viewPort);
        }
        if (viewPort.getView() != this) {
            viewPort.setView(this);
        }
        return rootPane;
    }

    @Override
    public boolean isComponentEnabled() {
        return this.isEnabled();
    }

    @Override
    public void setComponentEnabled(boolean enabled) {
        if (enabled != this.isComponentEnabled()) {
            this.setEnabled(enabled);
        }
    }

    protected void setDockableWidth(int width) {
        this.dockableWidth = width;
    }

    protected void updateDockableWidth(int width) {
        this.setDockableWidth(width);
        this.dockable.setVisible(false);
        this.showDockable();
    }

    @Override
    public int getDockableWidth() {
        return this.dockableWidth;
    }

    @Override
    public final DefaultSingleCDockable getDockable() {
        return this.dockable;
    }

    @Override
    public void showDockable() {
        GuiExecutor.execute(this::updateVisibleState);
    }

    private void updateVisibleState() {
        if (!this.dockable.isVisible()) {
            CControl control = GuiUtils.getUICore().getDockingControl();
            CVetoFocusListener vetoFocus = GuiUtils.getUICore().getDockingVetoFocus();
            control.addVetoFocusListener(vetoFocus);
            Component component = this.getToolComponent();
            GuiUtils.setPreferredWidth(component, this.dockableWidth, this.dockableWidth);
            if (this.dockable.getFocusComponent() == component) {
                control.addDockable((SingleCDockable)this.dockable);
                ExtendedMode extMode = this.previousExtendedMode;
                if (extMode == null) {
                    extMode = this.defaultExtendedMode;
                }
                this.dockable.setExtendedMode(extMode == null ? ExtendedMode.MINIMIZED : extMode);
            } else {
                this.dockable.add(component);
                this.dockable.setFocusComponent(component);
                control.addDockable((SingleCDockable)this.dockable);
                POSITION pos = this.defaultPosition == null ? POSITION.EAST : this.defaultPosition;
                ExtendedMode mode = this.defaultExtendedMode == null ? ExtendedMode.MINIMIZED : this.defaultExtendedMode;
                CContentArea baseArea = GuiUtils.getUICore().getBaseArea();
                CBaseLocation base = CLocation.base((CContentArea)baseArea);
                CFlapIndexLocation minimizeLocation = pos == POSITION.EAST ? base.minimalEast() : (pos == POSITION.WEST ? base.minimalWest() : (pos == POSITION.NORTH ? base.minimalNorth() : base.minimalSouth()));
                this.dockable.setDefaultLocation(ExtendedMode.MINIMIZED, (CLocation)minimizeLocation);
                double w = baseArea.getWidth();
                if (w > 0.0) {
                    double ratio = (double)GuiUtils.getScaleLength(this.dockableWidth) / w;
                    if (ratio > 0.9) {
                        ratio = 0.9;
                    }
                    TreeLocationRoot normalizedLocation = pos == POSITION.EAST ? base.normalEast(ratio) : (pos == POSITION.WEST ? base.normalWest(ratio) : (pos == POSITION.NORTH ? base.normalNorth(ratio) : base.normalSouth(ratio)));
                    this.dockable.setDefaultLocation(ExtendedMode.NORMALIZED, (CLocation)normalizedLocation);
                }
                this.dockable.setMinimizedSize(GuiUtils.getDimension(this.dockableWidth, 50));
                this.dockable.setExtendedMode(mode);
            }
            this.dockable.setVisible(true);
            this.dockable.setResizeLocked(true);
            control.removeVetoFocusListener(vetoFocus);
        }
    }

    @Override
    public void closeDockable() {
        if (this.dockable.getControl() != null) {
            this.previousExtendedMode = this.dockable.getExtendedMode();
            GuiExecutor.execute(() -> {
                CControl control = GuiUtils.getUICore().getDockingControl();
                CVetoFocusListener vetoFocus = GuiUtils.getUICore().getDockingVetoFocus();
                control.addVetoFocusListener(vetoFocus);
                control.removeDockable((SingleCDockable)this.dockable);
                control.removeVetoFocusListener(vetoFocus);
            });
        }
    }

    public static enum POSITION {
        NORTH,
        EAST,
        SOUTH,
        WEST;

    }
}

