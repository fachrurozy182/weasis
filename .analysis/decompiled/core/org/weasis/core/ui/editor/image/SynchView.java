/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.extras.FlatSVGIcon
 */
package org.weasis.core.ui.editor.image;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.util.HashMap;
import java.util.Map;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.GUIEntry;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.editor.image.SynchData;

public class SynchView
implements GUIEntry {
    public static final SynchView DEFAULT_TILE;
    public static final SynchView DEFAULT_STACK;
    private final String name;
    private final String command;
    private final FlatSVGIcon svgIcon;
    private SynchData synchData;
    private SynchData originalSynchData;

    public SynchView(String name, String command, SynchData.Mode mode, boolean synch, ResourceUtil.ActionIcon icon, Map<String, Boolean> actions) {
        if (name == null) {
            throw new IllegalArgumentException("A parameter is null!");
        }
        this.synchData = new SynchData(mode, actions, synch);
        this.originalSynchData = new SynchData(mode, actions, synch);
        this.name = name;
        this.command = command;
        this.svgIcon = ResourceUtil.getIcon(icon);
    }

    public String getName() {
        return this.name;
    }

    public SynchData getSynchData() {
        return this.synchData;
    }

    public void resetSynchData() {
        this.synchData = this.originalSynchData.copy();
    }

    public void setActionEnabled(String cmd, boolean enabled) {
        if (cmd == null) {
            return;
        }
        this.synchData.getActions().put(cmd, enabled);
        this.originalSynchData.getActions().put(cmd, enabled);
    }

    public boolean isSynch() {
        return this.synchData != null && this.synchData.isSynchActivated();
    }

    public String toString() {
        return this.name;
    }

    public String getCommand() {
        return this.command;
    }

    @Override
    public String getDescription() {
        return null;
    }

    public FlatSVGIcon getIcon() {
        return this.svgIcon;
    }

    @Override
    public String getUIName() {
        return this.name;
    }

    static {
        HashMap<String, Boolean> actions = new HashMap<String, Boolean>();
        actions.put(ActionW.SCROLL_SERIES.cmd(), true);
        actions.put(ActionW.PAN.cmd(), true);
        actions.put(ActionW.ZOOM.cmd(), true);
        actions.put(ActionW.ROTATION.cmd(), true);
        actions.put(ActionW.FLIP.cmd(), true);
        actions.put(ActionW.WINDOW.cmd(), true);
        actions.put(ActionW.LEVEL.cmd(), true);
        actions.put(ActionW.PRESET.cmd(), true);
        actions.put(ActionW.LUT_SHAPE.cmd(), true);
        actions.put(ActionW.LUT.cmd(), true);
        actions.put(ActionW.INVERT_LUT.cmd(), true);
        actions.put(ActionW.FILTER.cmd(), true);
        actions.put(ActionW.INVERSE_STACK.cmd(), true);
        actions.put(ActionW.SORT_STACK.cmd(), true);
        actions.put(ActionW.SPATIAL_UNIT.cmd(), true);
        DEFAULT_TILE = new SynchView(Messages.getString("SynchView.def_t"), "Tile", SynchData.Mode.TILE, true, ResourceUtil.ActionIcon.TILE, actions);
        actions = new HashMap();
        actions.put(ActionW.SCROLL_SERIES.cmd(), true);
        actions.put(ActionW.PAN.cmd(), false);
        actions.put(ActionW.ZOOM.cmd(), false);
        actions.put(ActionW.ROTATION.cmd(), false);
        actions.put(ActionW.FLIP.cmd(), false);
        actions.put(ActionW.WINDOW.cmd(), false);
        actions.put(ActionW.LEVEL.cmd(), false);
        actions.put(ActionW.PRESET.cmd(), false);
        actions.put(ActionW.LUT_SHAPE.cmd(), false);
        actions.put(ActionW.LUT.cmd(), false);
        actions.put(ActionW.INVERT_LUT.cmd(), false);
        actions.put(ActionW.FILTER.cmd(), false);
        actions.put(ActionW.SPATIAL_UNIT.cmd(), false);
        DEFAULT_STACK = new SynchView(Messages.getString("SynchView.def_s"), "Stack", SynchData.Mode.STACK, false, ResourceUtil.ActionIcon.SEQUENCE, actions);
    }
}

