/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.qr;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class RetrieveSelection {
    private final Map<String, Set<String>> seriesByStudy = new LinkedHashMap<String, Set<String>>();

    public void addStudy(String studyUid) {
        this.seriesByStudy.put(studyUid, Set.of());
    }

    public void addSeries(String studyUid, String seriesUid) {
        Set<String> series = this.seriesByStudy.get(studyUid);
        if (series == null) {
            series = new LinkedHashSet<String>();
            this.seriesByStudy.put(studyUid, series);
        } else if (series.isEmpty()) {
            return;
        }
        series.add(seriesUid);
    }

    public Set<String> getStudyUids() {
        return this.seriesByStudy.keySet();
    }

    public Set<String> getSeriesUids(String studyUid) {
        return this.seriesByStudy.getOrDefault(studyUid, Set.of());
    }

    public boolean contains(String studyUid, String seriesUid) {
        Set<String> series = this.getSeriesUids(studyUid);
        return series.isEmpty() || series.contains(seriesUid);
    }

    public boolean isEmpty() {
        return this.seriesByStudy.isEmpty();
    }
}

