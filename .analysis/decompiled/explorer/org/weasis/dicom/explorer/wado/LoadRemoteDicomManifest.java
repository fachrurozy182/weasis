/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.explorer.ObservableEvent
 *  org.weasis.core.api.explorer.ObservableEvent$BasicAction
 *  org.weasis.core.api.explorer.model.DataExplorerModel
 *  org.weasis.core.api.gui.util.GuiExecutor
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.net.URIUtils
 *  org.weasis.core.util.StreamIOException
 *  org.weasis.core.util.StringUtil
 *  org.weasis.core.util.StringUtil$Suffix
 */
package org.weasis.dicom.explorer.wado;

import java.beans.PropertyChangeListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.net.URIUtils;
import org.weasis.core.util.StreamIOException;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.PluginOpeningStrategy;
import org.weasis.dicom.explorer.exp.ExplorerTask;
import org.weasis.dicom.explorer.wado.DownloadException;
import org.weasis.dicom.explorer.wado.DownloadManager;
import org.weasis.dicom.explorer.wado.LoadSeries;

public class LoadRemoteDicomManifest
extends ExplorerTask<Boolean, String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadRemoteDicomManifest.class);
    private final DicomModel dicomModel;
    private final List<String> xmlFiles;
    private final AtomicInteger retryNb = new AtomicInteger(0);
    private final List<LoadSeries> loadSeriesList = new ArrayList<LoadSeries>();
    private final PropertyChangeListener propertyChangeListener = evt -> {
        ObservableEvent event;
        Object patt0$temp;
        if (evt instanceof ObservableEvent && (patt0$temp = (event = (ObservableEvent)evt).getNewValue()) instanceof LoadSeries) {
            LoadSeries series = (LoadSeries)patt0$temp;
            ObservableEvent.BasicAction cmd = event.getActionCommand();
            if (ObservableEvent.BasicAction.LOADING_STOP.equals((Object)cmd) || ObservableEvent.BasicAction.LOADING_CANCEL.equals((Object)cmd)) {
                this.checkDownloadIssues(series);
            } else if (ObservableEvent.BasicAction.LOADING_START.equals((Object)cmd) && !this.loadSeriesList.contains(series)) {
                this.loadSeriesList.add(series);
            }
        }
    };

    public LoadRemoteDicomManifest(List<String> xmlFiles, DataExplorerModel explorerModel) {
        super(Messages.getString("DicomExplorer.loading"), true);
        if (xmlFiles == null || !(explorerModel instanceof DicomModel)) {
            throw new IllegalArgumentException("invalid parameters");
        }
        this.xmlFiles = xmlFiles.stream().filter(Objects::nonNull).collect(Collectors.toList());
        this.dicomModel = (DicomModel)explorerModel;
    }

    private void checkDownloadIssues(LoadSeries loadSeries) {
        if (!loadSeries.hasDownloadFailed()) {
            this.loadSeriesList.remove(loadSeries);
            return;
        }
        if (DownloadManager.getTasks().isEmpty() || DownloadManager.getTasks().stream().allMatch(LoadSeries::isStopped)) {
            if (!this.loadSeriesList.isEmpty() && this.tryDownloadingAgain(null)) {
                LOGGER.info("Try downloading ({}) the missing elements", (Object)this.retryNb.get());
                ArrayList<LoadSeries> oldList = new ArrayList<LoadSeries>(this.loadSeriesList);
                this.loadSeriesList.clear();
                this.dicomModel.removePropertyChangeListener(this.propertyChangeListener);
                for (LoadSeries s : oldList) {
                    LoadSeries task = s.isStopped() ? s : s.cancelAndReplace(s, true);
                    this.loadSeriesList.add(task);
                }
                this.startDownloadingSeries(this.loadSeriesList, true, true);
                this.dicomModel.addPropertyChangeListener(this.propertyChangeListener);
            } else {
                this.dicomModel.removePropertyChangeListener(this.propertyChangeListener);
            }
        }
    }

    private boolean tryDownloadingAgain(DownloadException e) {
        if (this.retryNb.getAndIncrement() == 0) {
            return true;
        }
        boolean[] ret = new boolean[]{false};
        GuiExecutor.invokeAndWait(() -> {
            int confirm = JOptionPane.showConfirmDialog(GuiUtils.getUICore().getApplicationWindow(), LoadRemoteDicomManifest.getErrorMessage(e), Messages.getString("LoadRemoteDicomManifest.net_err_msg"), 0);
            ret[0] = 0 == confirm;
        });
        return ret[0];
    }

    private static String getErrorMessage(DownloadException e) {
        StringBuilder buf = new StringBuilder();
        if (e == null) {
            buf.append(Messages.getString("LoadRemoteDicomManifest.cannot_download"));
        } else {
            String serverMessage;
            buf.append(StringUtil.getTruncatedString((String)e.getMessage(), (int)130, (StringUtil.Suffix)StringUtil.Suffix.THREE_PTS));
            if (e.getCause() instanceof StreamIOException && StringUtil.hasText((CharSequence)(serverMessage = e.getCause().getMessage()))) {
                buf.append("\n");
                buf.append(Messages.getString("LoadRemoteDicomManifest.server_resp"));
                buf.append(": ");
                buf.append(StringUtil.getTruncatedString((String)serverMessage, (int)100, (StringUtil.Suffix)StringUtil.Suffix.THREE_PTS));
            }
        }
        buf.append("\n\n");
        buf.append(Messages.getString("LoadRemoteDicomManifest.download_again"));
        return buf.toString();
    }

    @Override
    protected void done() {
        DownloadManager.CONCURRENT_EXECUTOR.prestartAllCoreThreads();
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        block3: {
            try {
                for (String xmlFile : this.xmlFiles) {
                    this.downloadManifest(xmlFile);
                }
            }
            catch (DownloadException e) {
                LOGGER.error("Download failed", (Throwable)e);
                if (!this.tryDownloadingAgain(e)) break block3;
                LOGGER.info("Try downloading again: {}", this.xmlFiles);
                LoadRemoteDicomManifest mf = new LoadRemoteDicomManifest(this.xmlFiles, this.dicomModel);
                mf.retryNb.set(this.retryNb.get());
                mf.execute();
            }
        }
        this.dicomModel.addPropertyChangeListener(this.propertyChangeListener);
        return true;
    }

    private void downloadManifest(String path) throws DownloadException {
        try {
            URI uri = URIUtils.getURI((String)path);
            Collection<LoadSeries> wadoTasks = DownloadManager.buildDicomSeries(uri, this.dicomModel);
            this.loadSeriesList.addAll(wadoTasks);
            boolean downloadImmediately = GuiUtils.getUICore().getSystemPreferences().getBooleanProperty("weasis.download.immediately", true);
            this.startDownloadingSeries(wadoTasks, downloadImmediately, false);
            if (!downloadImmediately) {
                LoadSeries.notifyDownloadCompletion(this.dicomModel);
            }
        }
        catch (URISyntaxException e) {
            LOGGER.error("Loading manifest", (Throwable)e);
        }
    }

    private void startDownloadingSeries(Collection<LoadSeries> wadoTasks, boolean downloadImmediately, boolean retry) {
        if (!wadoTasks.isEmpty()) {
            PluginOpeningStrategy openingStrategy = new PluginOpeningStrategy(DownloadManager.getOpeningViewer());
            if (!retry) {
                openingStrategy.prepareImport();
            }
            for (LoadSeries loadSeries : wadoTasks) {
                loadSeries.setPOpeningStrategy(openingStrategy);
                DownloadManager.addLoadSeries(loadSeries, this.dicomModel, downloadImmediately);
            }
            DownloadManager.getTasks().sort(Collections.reverseOrder(new DownloadManager.PriorityTaskComparator()));
        }
    }
}

