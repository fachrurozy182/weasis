/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.dcm4che3.data.Attributes
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.explorer.model.DataExplorerModel
 *  org.weasis.dicom.codec.DicomMediaIO
 *  org.weasis.dicom.codec.DicomMediaIO$Reading
 *  org.weasis.dicom.codec.DicomSeries
 */
package org.weasis.dicom.explorer;

import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Objects;
import org.dcm4che3.data.Attributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.dicom.codec.DicomMediaIO;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.explorer.HangingProtocols;
import org.weasis.dicom.explorer.LoadDicom;
import org.weasis.dicom.explorer.LoadLocalDicom;

public class LoadDicomObjects
extends LoadDicom {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadDicomObjects.class);
    private final Attributes[] dicomObjectsToLoad;

    public LoadDicomObjects(DataExplorerModel explorerModel, HangingProtocols.OpeningViewer openingMode, Attributes ... dcmObjects) {
        super(explorerModel, false, openingMode);
        this.dicomObjectsToLoad = Objects.requireNonNull(dcmObjects);
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        this.startLoadingEvent();
        this.addSelectionAndNotify();
        return true;
    }

    protected void addSelectionAndNotify() {
        if (this.dicomObjectsToLoad.length > 0) {
            this.openingStrategy.prepareImport();
            LinkedHashSet<DicomSeries> uniqueSeriesSet = new LinkedHashSet<DicomSeries>();
            for (Attributes dicom : this.dicomObjectsToLoad) {
                if (this.isCancelled()) {
                    return;
                }
                try {
                    DicomMediaIO loader = new DicomMediaIO(dicom);
                    if (loader.getReadingStatus() != DicomMediaIO.Reading.READABLE) continue;
                    uniqueSeriesSet.add(this.buildDicomStructure(loader));
                }
                catch (URISyntaxException e) {
                    LOGGER.error("Reading DICOM object", (Throwable)e);
                }
            }
            LoadLocalDicom.updateSeriesThumbnail(uniqueSeriesSet, this.dicomModel);
        }
    }
}

