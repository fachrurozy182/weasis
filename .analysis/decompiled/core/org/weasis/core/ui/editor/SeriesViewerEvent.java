/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.editor;

import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.ui.editor.SeriesViewer;

public class SeriesViewerEvent {
    private final SeriesViewer<? extends MediaElement> seriesViewer;
    private final MediaSeries<? extends MediaElement> series;
    private final MediaElement mediaElement;
    private final EVENT eventType;
    private Object sharedObject;

    public SeriesViewerEvent(SeriesViewer<? extends MediaElement> seriesViewer, MediaSeries<? extends MediaElement> series, MediaElement mediaElement, EVENT eventType) {
        if (seriesViewer == null) {
            throw new IllegalArgumentException("SeriesViewer parameter cannot be null");
        }
        this.seriesViewer = seriesViewer;
        this.series = series;
        this.mediaElement = mediaElement;
        this.eventType = eventType;
    }

    public Object getSharedObject() {
        return this.sharedObject;
    }

    public void setShareObject(Object sharedObject) {
        this.sharedObject = sharedObject;
    }

    public SeriesViewer<? extends MediaElement> getSeriesViewer() {
        return this.seriesViewer;
    }

    public MediaSeries<? extends MediaElement> getSeries() {
        return this.series;
    }

    public MediaElement getMediaElement() {
        return this.mediaElement;
    }

    public EVENT getEventType() {
        return this.eventType;
    }

    public static enum EVENT {
        SELECT,
        ADD,
        LAYOUT,
        SELECT_VIEW,
        ANONYM,
        TOGGLE_INFO,
        WIN_LEVEL,
        LUT;

    }
}

