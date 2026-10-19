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
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.Icon;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.editor.image.ShowPopup;
import org.weasis.core.ui.editor.image.SynchData;
import org.weasis.core.ui.editor.image.ViewButton;

public class SynchViewButton
extends ViewButton {
    private static final FlatSVGIcon BASE_ICON = ResourceUtil.getIcon(ResourceUtil.ActionIcon.SYNCH, 20, 20);
    private static final Color BASE_COLOR = new Color(0x6E6E6E);
    private static final Map<SynchData.SyncState, FlatSVGIcon> ICONS = new EnumMap<SynchData.SyncState, FlatSVGIcon>(SynchData.SyncState.class);
    protected SynchData.SyncState state = SynchData.SyncState.OFF;
    private Color chipColor;

    private static FlatSVGIcon derive(Color color) {
        return GuiUtils.getDerivedIcon(BASE_ICON, new FlatSVGIcon.ColorFilter().add(BASE_COLOR, color));
    }

    public SynchViewButton(ShowPopup popup) {
        super(popup, (Icon)ICONS.get((Object)SynchData.SyncState.OFF), "sync");
    }

    public SynchData.SyncState getState() {
        return this.state;
    }

    public void setState(SynchData.SyncState state) {
        this.state = state;
    }

    public void setChipColor(Color chipColor) {
        this.chipColor = chipColor;
    }

    @Override
    public Icon getIcon() {
        return (Icon)ICONS.get((Object)this.state);
    }

    @Override
    protected void paintOverlay(Graphics2D g2d, int x, int y, int width, int height) {
        Color chip = this.chipColor;
        if (chip == null) {
            return;
        }
        int side = Math.max(4, Math.round((float)width * 0.35f));
        int cx = x + (width - side) / 2;
        int cy = y + (height - side) / 2;
        Object oldAA = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        Color oldColor = g2d.getColor();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(chip);
        g2d.fillRect(cx, cy, side, side);
        g2d.setColor(chip.darker());
        g2d.drawRect(cx, cy, side - 1, side - 1);
        g2d.setColor(oldColor);
        if (oldAA != null) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
        }
    }

    static {
        ICONS.put(SynchData.SyncState.OFF, SynchViewButton.derive(GuiUtils.IconColor.ACTIONS_RED.getColor()));
        ICONS.put(SynchData.SyncState.ON, SynchViewButton.derive(GuiUtils.IconColor.ACTIONS_GREEN.getColor()));
    }
}

