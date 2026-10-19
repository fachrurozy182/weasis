/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.explorer.model.DataExplorerModel
 */
package org.weasis.dicom.explorer;

import java.util.List;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.HangingProtocols;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.PluginOpeningStrategy;
import org.weasis.dicom.explorer.exp.ExplorerTask;
import org.weasis.dicom.explorer.wado.DownloadManager;
import org.weasis.dicom.explorer.wado.LoadSeries;

public class LoadDicomDir
extends ExplorerTask<Boolean, String> {
    private final List<LoadSeries> seriesList;
    private final DicomModel dicomModel;
    private final PluginOpeningStrategy openingStrategy;

    public LoadDicomDir(List<LoadSeries> loadSeries, DataExplorerModel explorerModel, HangingProtocols.OpeningViewer openingViewer) {
        super(Messages.getString("DicomExplorer.loading"), true);
        if (loadSeries == null || !(explorerModel instanceof DicomModel)) {
            throw new IllegalArgumentException("invalid parameters");
        }
        this.seriesList = loadSeries;
        this.dicomModel = (DicomModel)explorerModel;
        this.openingStrategy = new PluginOpeningStrategy(openingViewer);
    }

    @Override
    protected void done() {
        this.openingStrategy.reset();
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        if (!this.seriesList.isEmpty()) {
            this.openingStrategy.prepareImport();
            for (LoadSeries s : this.seriesList) {
                s.setPOpeningStrategy(this.openingStrategy);
                DownloadManager.addLoadSeries(s, this.dicomModel, true);
            }
            DownloadManager.UNIQUE_EXECUTOR.prestartAllCoreThreads();
        }
        return true;
    }
}

