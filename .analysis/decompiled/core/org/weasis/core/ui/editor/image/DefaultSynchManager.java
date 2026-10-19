/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.editor.image;

import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.SynchManager;
import org.weasis.core.ui.editor.image.SynchView;
import org.weasis.core.ui.editor.image.ViewCanvas;

public class DefaultSynchManager<E extends ImageElement>
extends SynchManager<E> {
    public DefaultSynchManager(ImageViewerEventManager<E> eventManager) {
        super(eventManager);
    }

    @Override
    public void updateAllListeners(ImageViewerPlugin<E> viewerPlugin, SynchView synchView) {
        if (viewerPlugin == null) {
            return;
        }
        ViewCanvas<E> viewPane = viewerPlugin.getSelectedViewCanvas();
        if (viewPane == null || viewPane.getSeries() == null) {
            return;
        }
        viewPane.setActionsInView(ActionW.SYNCH_LINK.cmd(), null);
        this.eventManager.addPropertyChangeListener(ActionW.SYNCH.cmd(), viewPane);
    }

    @Override
    public boolean hasSameOrientation(MediaSeries series1, MediaSeries series2) {
        return false;
    }
}

