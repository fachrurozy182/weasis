/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.boolex;

import java.util.List;
import org.slf4j.Marker;

public class MarkerList {
    List<Marker> markers;

    public MarkerList(List<Marker> markers) {
        this.markers = markers;
    }

    public boolean contains(String markerName) {
        if (markerName == null || markerName.trim().length() == 0) {
            return false;
        }
        if (this.markers == null || this.markers.isEmpty()) {
            return false;
        }
        boolean result = this.markers.stream().anyMatch(m -> m.contains(markerName));
        return result;
    }

    public Marker getFirstMarker() {
        if (this.markers == null || this.markers.isEmpty()) {
            return null;
        }
        return this.markers.get(0);
    }
}

