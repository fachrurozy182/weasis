/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.editor.image;

import java.util.Objects;
import java.util.Optional;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.ComboItemListener;
import org.weasis.core.api.gui.util.ToggleButtonListener;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.SynchData;
import org.weasis.core.ui.editor.image.SynchView;

public abstract class SynchManager<E extends ImageElement> {
    protected final ImageViewerEventManager<E> eventManager;

    public SynchManager(ImageViewerEventManager<E> eventManager) {
        this.eventManager = eventManager;
    }

    public abstract void updateAllListeners(ImageViewerPlugin<E> var1, SynchView var2);

    protected String getFrameOfReferenceUID(MediaSeries<E> series) {
        if (series != null) {
            return (String)series.getTagValue(TagW.get("FrameOfReferenceUID"));
        }
        return null;
    }

    protected boolean hasSameFrUid(MediaSeries<E> series1, MediaSeries<E> series2) {
        if (series1 == null || series2 == null) {
            return false;
        }
        return Objects.equals(this.getFrameOfReferenceUID(series1), this.getFrameOfReferenceUID(series2));
    }

    protected void applyManualSync() {
        Object object;
        ComboItemListener synchAction = this.eventManager.getAction(ActionW.SYNCH).orElse(null);
        if (synchAction != null && (object = synchAction.getSelectedItem()) instanceof SynchView) {
            SynchView sel = (SynchView)object;
            sel.getSynchData().setManualSyncState(SynchData.SyncState.ON);
            Optional<ToggleButtonListener> synchMode = this.eventManager.getAction(ActionW.SYNCH_MODE);
            synchMode.ifPresent(e -> {
                e.setSelectedWithoutTriggerAction(true);
                e.enableAction(true);
            });
        }
    }

    public abstract boolean hasSameOrientation(MediaSeries<E> var1, MediaSeries<E> var2);
}

