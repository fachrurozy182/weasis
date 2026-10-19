/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.image;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaSeries;

public record ImageOpEvent(OpEvent eventType, MediaSeries<? extends ImageElement> series, ImageElement image, Map<String, Object> params) {
    public ImageOpEvent {
        Objects.requireNonNull(eventType, "Event type cannot be null");
        params = params == null ? Map.of() : new HashMap<String, Object>(params);
    }

    public static ImageOpEvent of(OpEvent eventType) {
        return new ImageOpEvent(eventType, null, null, null);
    }

    public static ImageOpEvent withSeries(OpEvent eventType, MediaSeries<? extends ImageElement> series) {
        return new ImageOpEvent(eventType, series, null, null);
    }

    public static ImageOpEvent withImage(OpEvent eventType, ImageElement image) {
        return new ImageOpEvent(eventType, null, image, null);
    }

    public static enum OpEvent {
        RESET_DISPLAY,
        SERIES_CHANGE,
        IMAGE_CHANGE,
        APPLY_PR;

    }
}

