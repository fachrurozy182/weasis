/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 */
package org.weasis.core.ui.editor.image;

import javax.swing.Icon;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.editor.image.ShowPopup;
import org.weasis.core.ui.editor.image.ViewButton;

public class PlayViewButton
extends ViewButton {
    protected eState state = eState.PLAY;
    protected int size;

    public PlayViewButton(ShowPopup popup) {
        this(20, popup);
    }

    public PlayViewButton(int size, ShowPopup popup) {
        super(popup, PlayViewButton.getResizedIcon(ResourceUtil.ActionIcon.EXECUTE, size), "play");
        this.size = size;
    }

    public eState getState() {
        return this.state;
    }

    public void setState(eState state) {
        this.state = state;
    }

    @Override
    public Icon getIcon() {
        return switch (this.state.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> PlayViewButton.getResizedIcon(ResourceUtil.ActionIcon.EXECUTE, this.size);
            case 1 -> PlayViewButton.getResizedIcon(ResourceUtil.ActionIcon.SUSPEND, this.size);
        };
    }

    public static enum eState {
        PLAY,
        PAUSE;

    }
}

