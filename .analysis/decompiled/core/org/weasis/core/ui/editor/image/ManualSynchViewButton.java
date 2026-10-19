/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.extras.FlatSVGIcon
 *  com.formdev.flatlaf.extras.FlatSVGIcon$ColorFilter
 */
package org.weasis.core.ui.editor.image;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Color;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.Icon;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.editor.image.ShowPopup;
import org.weasis.core.ui.editor.image.SynchData;
import org.weasis.core.ui.editor.image.ViewButton;

public class ManualSynchViewButton
extends ViewButton {
    private static final FlatSVGIcon BASE_ICON = ResourceUtil.getIcon(ResourceUtil.ActionIcon.HAND, 20, 20);
    private static final Color BASE_COLOR = new Color(0x6E6E6E);
    private static final Map<SynchData.SyncState, FlatSVGIcon> ICONS = new EnumMap<SynchData.SyncState, FlatSVGIcon>(SynchData.SyncState.class);
    protected SynchData.SyncState state = SynchData.SyncState.OFF;

    private static FlatSVGIcon derive(Color color) {
        return GuiUtils.getDerivedIcon(BASE_ICON, new FlatSVGIcon.ColorFilter().add(BASE_COLOR, color));
    }

    public ManualSynchViewButton(ShowPopup popup) {
        super(popup, (Icon)ICONS.get((Object)SynchData.SyncState.OFF), "manualsync");
    }

    public SynchData.SyncState getState() {
        return this.state;
    }

    public void setState(SynchData.SyncState state) {
        this.state = state;
    }

    @Override
    public Icon getIcon() {
        return (Icon)ICONS.get((Object)this.state);
    }

    static {
        ICONS.put(SynchData.SyncState.OFF, ManualSynchViewButton.derive(GuiUtils.IconColor.ACTIONS_RED.getColor()));
        ICONS.put(SynchData.SyncState.ON, ManualSynchViewButton.derive(GuiUtils.IconColor.ACTIONS_GREEN.getColor()));
    }
}

