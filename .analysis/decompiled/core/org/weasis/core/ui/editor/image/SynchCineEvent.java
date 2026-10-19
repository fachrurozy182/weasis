/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.editor.image;

import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.ui.editor.image.SynchEvent;
import org.weasis.core.ui.editor.image.ViewCanvas;

public class SynchCineEvent
extends SynchEvent {
    private final MediaElement media;
    private final int seriesIndex;
    private Number location;

    public SynchCineEvent(ViewCanvas<?> view, MediaElement media, int seriesIndex) {
        this(view, media, seriesIndex, null);
    }

    public SynchCineEvent(ViewCanvas<?> view, MediaElement media, int seriesIndex, Number location) {
        super(view);
        this.media = media;
        this.seriesIndex = seriesIndex;
        this.location = location;
    }

    public MediaElement getMedia() {
        return this.media;
    }

    public int getSeriesIndex() {
        return this.seriesIndex;
    }

    public void setLocation(Number location) {
        this.location = location;
    }

    public Number getLocation() {
        return this.location;
    }
}

