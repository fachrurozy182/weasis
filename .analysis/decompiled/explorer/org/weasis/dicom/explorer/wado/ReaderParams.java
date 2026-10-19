/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.explorer.wado;

import java.util.Map;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.pref.node.DicomWebNode;
import org.weasis.dicom.explorer.wado.LoadSeries;

class ReaderParams {
    private final DicomModel model;
    private final Map<String, LoadSeries> seriesMap;
    DicomWebNode wadoUri;
    boolean bulkSeriesRetrieve;

    ReaderParams(DicomModel model, Map<String, LoadSeries> seriesMap) {
        this.model = model;
        this.seriesMap = seriesMap;
    }

    DicomModel getModel() {
        return this.model;
    }

    Map<String, LoadSeries> getSeriesMap() {
        return this.seriesMap;
    }
}

