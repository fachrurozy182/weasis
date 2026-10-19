/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.dcm4che3.util.UIDUtils
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.explorer.model.DataExplorerModel
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.MediaSeriesGroupNode
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.dicom.codec.DicomSeries
 *  org.weasis.dicom.codec.TagD
 *  org.weasis.dicom.codec.TagD$Level
 *  org.weasis.dicom.codec.utils.SeriesInstanceList
 *  org.weasis.dicom.mf.SopInstance
 *  org.weasis.dicom.mf.WadoParameters
 */
package org.weasis.dicom.explorer.wado;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import org.dcm4che3.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.MediaSeriesGroupNode;
import org.weasis.core.api.media.data.TagW;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.utils.SeriesInstanceList;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.PluginOpeningStrategy;
import org.weasis.dicom.explorer.exp.ExplorerTask;
import org.weasis.dicom.explorer.wado.DownloadManager;
import org.weasis.dicom.explorer.wado.DownloadPriority;
import org.weasis.dicom.explorer.wado.LoadSeries;
import org.weasis.dicom.mf.SopInstance;
import org.weasis.dicom.mf.WadoParameters;

public class LoadRemoteDicomURL
extends ExplorerTask<Boolean, String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadRemoteDicomURL.class);
    private final URL[] urls;
    private final DicomModel dicomModel;

    public LoadRemoteDicomURL(String[] urls, DataExplorerModel explorerModel) {
        super(Messages.getString("DicomExplorer.loading"), true);
        if (urls == null || !(explorerModel instanceof DicomModel)) {
            throw new IllegalArgumentException("invalid parameters");
        }
        URL[] urlRef = new URL[urls.length];
        for (int i = 0; i < urls.length; ++i) {
            if (urls[i] == null) continue;
            try {
                urlRef[i] = URI.create(urls[i]).toURL();
                continue;
            }
            catch (IllegalArgumentException | MalformedURLException e) {
                LOGGER.error("Not a valid URL", (Throwable)e);
            }
        }
        this.urls = urlRef;
        this.dicomModel = (DicomModel)explorerModel;
    }

    public LoadRemoteDicomURL(URL[] urls, DataExplorerModel explorerModel) {
        super(Messages.getString("DicomExplorer.loading"), true);
        if (urls == null || !(explorerModel instanceof DicomModel)) {
            throw new IllegalArgumentException("invalid parameters");
        }
        this.urls = urls;
        this.dicomModel = (DicomModel)explorerModel;
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        String seriesUID = null;
        for (URL item : this.urls) {
            if (item == null) continue;
            seriesUID = item.toString();
            break;
        }
        if (seriesUID != null) {
            String unknown = "UNKNOWN";
            MediaSeriesGroupNode patient = new MediaSeriesGroupNode(TagD.getUID((TagD.Level)TagD.Level.PATIENT), (Object)UIDUtils.createUID(), DicomModel.patient.tagView());
            patient.setTag(TagD.get((int)0x100020), (Object)unknown);
            patient.setTag(TagD.get((int)0x100010), (Object)unknown);
            this.dicomModel.addHierarchyNode(MediaSeriesGroupNode.rootNode, (MediaSeriesGroup)patient);
            MediaSeriesGroupNode study = new MediaSeriesGroupNode(TagD.getUID((TagD.Level)TagD.Level.STUDY), (Object)UIDUtils.createUID(), DicomModel.study.tagView());
            this.dicomModel.addHierarchyNode((MediaSeriesGroup)patient, (MediaSeriesGroup)study);
            DicomSeries dicomSeries = new DicomSeries(seriesUID);
            dicomSeries.setTag(TagW.ExplorerModel, (Object)this.dicomModel);
            dicomSeries.setTag(TagD.get((int)0x20000E), (Object)seriesUID);
            WadoParameters wadoParameters = new WadoParameters("", false);
            dicomSeries.setTag(TagW.WadoParameters, (Object)wadoParameters);
            SeriesInstanceList seriesInstanceList = new SeriesInstanceList();
            dicomSeries.setTag(TagW.WadoInstanceReferenceList, (Object)seriesInstanceList);
            this.dicomModel.addHierarchyNode((MediaSeriesGroup)study, (MediaSeriesGroup)dicomSeries);
            for (URL value : this.urls) {
                String url;
                SopInstance sop;
                if (value == null || (sop = seriesInstanceList.getSopInstance(url = value.toString(), null)) != null) continue;
                sop = new SopInstance(url, null);
                sop.setDirectDownloadFile(url);
                seriesInstanceList.addSopInstance(sop);
            }
            if (!seriesInstanceList.isEmpty()) {
                LoadSeries loadSeries = new LoadSeries(dicomSeries, this.dicomModel, GuiUtils.getUICore().getSystemPreferences().getIntProperty("download.concurrent.series.images", 4), true);
                if (!DicomModel.isHiddenModality(dicomSeries)) {
                    loadSeries.startDownloadImageReference(wadoParameters);
                }
                PluginOpeningStrategy openingStrategy = new PluginOpeningStrategy(DownloadManager.getOpeningViewer());
                openingStrategy.prepareImport();
                loadSeries.setPOpeningStrategy(openingStrategy);
                loadSeries.setPriority(new DownloadPriority((MediaSeriesGroup)patient, (MediaSeriesGroup)study, (MediaSeriesGroup)dicomSeries, true));
                DownloadManager.addLoadSeries(loadSeries, this.dicomModel, true);
                DownloadManager.CONCURRENT_EXECUTOR.prestartAllCoreThreads();
            }
        }
        return true;
    }
}

