/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.dcm4che3.data.Attributes
 *  org.dcm4che3.io.DicomInputStream
 *  org.dcm4che3.io.DicomInputStream$IncludeBulkData
 *  org.dcm4che3.io.DicomOutputStream
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.explorer.model.DataExplorerModel
 *  org.weasis.core.api.gui.util.GuiExecutor
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.Series
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.util.FileUtil
 *  org.weasis.core.util.StringUtil
 *  org.weasis.dicom.codec.DicomSeries
 *  org.weasis.dicom.codec.TagD
 *  org.weasis.dicom.codec.utils.SeriesInstanceList
 *  org.weasis.dicom.explorer.DicomModel
 *  org.weasis.dicom.explorer.LoadLocalDicom
 *  org.weasis.dicom.explorer.pref.node.AbstractDicomNode$RetrieveType
 *  org.weasis.dicom.explorer.wado.LoadSeries
 *  org.weasis.dicom.explorer.wado.SeriesDownloadManager
 *  org.weasis.dicom.mf.SopInstance
 *  org.weasis.dicom.op.CGet
 *  org.weasis.dicom.op.CMove
 *  org.weasis.dicom.param.AdvancedParams
 *  org.weasis.dicom.param.DicomNode
 *  org.weasis.dicom.param.DicomParam
 *  org.weasis.dicom.param.DicomProgress
 *  org.weasis.dicom.param.DicomState
 */
package org.weasis.dicom.qr;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JProgressBar;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.util.FileUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.utils.SeriesInstanceList;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.LoadLocalDicom;
import org.weasis.dicom.explorer.pref.node.AbstractDicomNode;
import org.weasis.dicom.explorer.wado.LoadSeries;
import org.weasis.dicom.explorer.wado.SeriesDownloadManager;
import org.weasis.dicom.mf.SopInstance;
import org.weasis.dicom.op.CGet;
import org.weasis.dicom.op.CMove;
import org.weasis.dicom.param.AdvancedParams;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.DicomParam;
import org.weasis.dicom.param.DicomProgress;
import org.weasis.dicom.param.DicomState;
import org.weasis.dicom.qr.GracefulCancel;
import org.weasis.dicom.qr.RetrieveContext;

public class LoadQrSeries
extends LoadSeries {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadQrSeries.class);
    private static final int UID_BATCH_SIZE = 500;
    private static final long IMPORT_TIMEOUT_SEC = 120L;
    private static final long PREVIOUS_TIMEOUT_SEC = 30L;
    private static final String LEVEL_SERIES = "SERIES";
    private static final String LEVEL_IMAGE = "IMAGE";
    private static final int[] IDENTITY_OVERRIDES = new int[]{0x100020, 0x100021, 0x100010, 0x100030, 0x100040};
    private final RetrieveContext context;
    private final AtomicBoolean cancelRequested = new AtomicBoolean();
    private final AtomicBoolean retrieving = new AtomicBoolean();
    private final DicomProgress progress = new DicomProgress();
    private final LoadQrSeries previous;
    private final AtomicInteger completedBefore = new AtomicInteger();
    private final AtomicInteger received = new AtomicInteger();

    public LoadQrSeries(DicomSeries dicomSeries, RetrieveContext context, boolean startDownloading) {
        super(dicomSeries, context.getDicomModel(), null, 1, true, startDownloading);
        this.context = context;
        this.previous = null;
        this.setPOpeningStrategy(context.getOpeningStrategy());
        context.register(this);
    }

    private LoadQrSeries(LoadQrSeries previous) {
        super(previous.getDicomSeries(), previous.context.getDicomModel(), null, previous.getProgressBar(), 1, true, previous.isStartDownloading());
        this.context = previous.context;
        this.previous = previous;
        this.setPOpeningStrategy(previous.getOpeningStrategy());
        this.context.register(this);
    }

    protected Boolean doInBackground() {
        boolean done;
        List<SopInstance> missing;
        this.awaitPrevious();
        if (this.getDicomSeries().size(null) > 0) {
            this.context.fillInstances(this.getDicomSeries());
        }
        if ((missing = this.getMissingInstances()) != null && missing.isEmpty()) {
            return true;
        }
        this.initProgressBar(missing);
        this.progress.addProgressListener(this::handleProgress);
        if (AbstractDicomNode.RetrieveType.CGET == this.context.getType()) {
            this.progress.addProgressListener(this::importStoredFile);
        }
        this.retrieving.set(true);
        try {
            if (missing == null) {
                done = this.retrieveSeries();
            } else {
                done = this.retrieveInstances(missing);
                if (!done && !this.cancelRequested.get()) {
                    LOGGER.info("Instance level retrieve failed, falling back to a series level retrieve");
                    done = this.retrieveSeries();
                }
            }
        }
        finally {
            this.retrieving.set(false);
        }
        this.setHasError(!done);
        this.awaitPendingImports();
        return done;
    }

    private List<SopInstance> getMissingInstances() {
        SeriesInstanceList instanceList = this.getSeriesInstanceList();
        if (instanceList.isEmpty()) {
            return null;
        }
        DicomSeries series = this.getDicomSeries();
        MediaSeriesGroup study = this.getDicomModel().getParent((MediaSeriesGroup)series, DicomModel.study);
        List missing = instanceList.getSortedList().stream().filter(sop -> !LoadSeries.isSOPInstanceUIDExist((DicomModel)this.getDicomModel(), (MediaSeriesGroup)study, (Series)series, (String)sop.getSopInstanceUID())).toList();
        return missing.size() == instanceList.size() ? null : missing;
    }

    private int totalInstances() {
        int enumerated = this.getSeriesInstanceList().size();
        if (enumerated > 0) {
            return enumerated;
        }
        Integer queried = (Integer)TagD.getTagValue((TagReadable)this.getDicomSeries(), (int)2101769, Integer.class);
        return queried == null ? 0 : queried;
    }

    private void initProgressBar(List<SopInstance> missing) {
        int total = this.totalInstances();
        int alreadyLoaded = missing == null ? 0 : total - missing.size();
        this.received.set(alreadyLoaded);
        this.completedBefore.set(alreadyLoaded);
        GuiExecutor.execute(() -> {
            if (total > 0) {
                this.getProgressBar().setIndeterminate(false);
                this.getProgressBar().setMaximum(total);
                this.getProgressBar().setValue(alreadyLoaded);
            } else {
                this.getProgressBar().setIndeterminate(true);
            }
        });
    }

    private boolean retrieveSeries() {
        return this.execute(this.buildKeys(LEVEL_SERIES, null));
    }

    private boolean retrieveInstances(List<SopInstance> missing) {
        for (List<String> batch : LoadQrSeries.batchUids(LoadQrSeries.instanceUids(missing))) {
            if (this.isCancelled() || this.cancelRequested.get()) {
                return true;
            }
            if (this.execute(this.buildKeys(LEVEL_IMAGE, batch))) continue;
            return false;
        }
        return true;
    }

    private DicomParam[] buildKeys(String level, List<String> sopInstanceUids) {
        MediaSeriesGroup study = this.getDicomModel().getParent((MediaSeriesGroup)this.getDicomSeries(), DicomModel.study);
        return LoadQrSeries.buildKeys((String)TagD.getTagValue((TagReadable)study, (int)0x20000D, String.class), (String)TagD.getTagValue((TagReadable)this.getDicomSeries(), (int)0x20000E, String.class), level, sopInstanceUids);
    }

    static DicomParam[] buildKeys(String studyUid, String seriesUid, String level, List<String> sopInstanceUids) {
        ArrayList<DicomParam> keys = new ArrayList<DicomParam>(4);
        keys.add(new DicomParam(524370, new String[]{level}));
        keys.add(new DicomParam(0x20000D, new String[]{studyUid}));
        keys.add(new DicomParam(0x20000E, new String[]{seriesUid}));
        if (sopInstanceUids != null) {
            keys.add(new DicomParam(524312, sopInstanceUids.toArray(new String[0])));
        }
        return keys.toArray(new DicomParam[0]);
    }

    static List<String> instanceUids(List<SopInstance> instances) {
        return instances.stream().map(SopInstance::getSopInstanceUID).distinct().toList();
    }

    static List<List<String>> batchUids(List<String> uids) {
        ArrayList<List<String>> batches = new ArrayList<List<String>>();
        for (int start = 0; start < uids.size(); start += 500) {
            batches.add(uids.subList(start, Math.min(start + 500, uids.size())));
        }
        return batches;
    }

    private boolean execute(DicomParam[] keys) {
        DicomState state;
        this.completedBefore.set(this.received.get());
        DicomState dicomState = state = AbstractDicomNode.RetrieveType.CGET == this.context.getType() ? this.executeGet(keys) : this.executeMove(keys);
        if (state == null) {
            return false;
        }
        int status = state.getStatus();
        if (status == 0 || status == 65024) {
            return true;
        }
        LOGGER.error("Retrieve of series {}: {}", (Object)this.getDicomSeries(), (Object)state.getMessage());
        return false;
    }

    private DicomState executeGet(DicomParam[] keys) {
        return CGet.process((AdvancedParams)this.context.getParams(), (DicomNode)this.context.getCallingNodeForQuery(), (DicomNode)this.context.getCalledNode(), (DicomProgress)this.progress, (Path)this.context.getStorageDir(), (URL)RetrieveContext.getCGetSopClassUrl(), (DicomParam[])keys);
    }

    private DicomState executeMove(DicomParam[] keys) {
        String error = this.context.ensureListenerStarted();
        if (error != null) {
            LOGGER.error("Retrieve of series {}: {}", (Object)this.getDicomSeries(), (Object)error);
            return null;
        }
        return CMove.process((AdvancedParams)this.context.getParams(), (DicomNode)this.context.getCallingNodeForMove(), (DicomNode)this.context.getCalledNode(), (String)this.context.getDestinationAet(), (DicomProgress)this.progress, (DicomParam[])keys);
    }

    private void importStoredFile(DicomProgress p) {
        if (p.getAttributes() != null || p.getProcessedFile() == null || this.cancelRequested.get()) {
            return;
        }
        Path file = p.getProcessedFile();
        LoadQrSeries.applyIdentityOverrides(file, this.getDicomModel(), this.getDicomSeries());
        LoadLocalDicom task = new LoadLocalDicom(new File[]{file.toFile()}, false, (DataExplorerModel)this.getDicomModel(), this.context.getOpeningStrategy());
        DicomModel.LOADING_EXECUTOR.execute((Runnable)task);
    }

    public static void applyIdentityOverrides(Path file, DicomModel model, DicomSeries target) {
        Path rewritten = file.resolveSibling(String.valueOf(file.getFileName()) + ".ovr");
        try {
            String tsuid;
            Attributes dataset;
            try (DicomInputStream in = new DicomInputStream(file.toFile());){
                in.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
                dataset = in.readDataset();
                tsuid = in.getTransferSyntax();
            }
            DicomSeries series = target != null ? target : LoadQrSeries.findSeries(model, dataset.getString(0x20000E));
            MediaSeriesGroup patient = model.getParent((MediaSeriesGroup)series, DicomModel.patient);
            MediaSeriesGroup study = model.getParent((MediaSeriesGroup)series, DicomModel.study);
            if (patient == null && study == null) {
                return;
            }
            Attributes before = LoadQrSeries.identity(dataset);
            SeriesDownloadManager.applyOverrides((Attributes)dataset, (int[])IDENTITY_OVERRIDES, (MediaSeriesGroup)patient, (MediaSeriesGroup)study);
            if (before.equals((Object)LoadQrSeries.identity(dataset))) {
                return;
            }
            try (DicomOutputStream out = new DicomOutputStream(rewritten.toFile());){
                out.writeDataset(dataset.createFileMetaInformation(tsuid), dataset);
                out.finish();
            }
            Files.move(rewritten, file, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.debug("Patient identity of {} aligned with the query answer", (Object)file);
        }
        catch (Exception e) {
            LOGGER.error("Applying the patient identity to {}", (Object)file, (Object)e);
            FileUtil.delete((Path)rewritten);
        }
    }

    private static MediaSeriesGroup findSeries(DicomModel model, String seriesUid) {
        return StringUtil.hasText((CharSequence)seriesUid) ? model.getSeriesNode(seriesUid) : null;
    }

    private static Attributes identity(Attributes dataset) {
        Attributes identity = new Attributes(IDENTITY_OVERRIDES.length);
        identity.addSelected(dataset, IDENTITY_OVERRIDES);
        return identity;
    }

    private void handleProgress(DicomProgress p) {
        int value;
        if (this.cancelRequested.get()) {
            return;
        }
        if (p.getAttributes() == null && p.getProcessedFile() != null) {
            value = this.received.incrementAndGet();
        } else {
            int completed = p.getNumberOfCompletedSuboperations();
            if (completed < 0) {
                return;
            }
            int handled = completed + Math.max(p.getNumberOfFailedSuboperations(), 0) + Math.max(p.getNumberOfWarningSuboperations(), 0);
            int batchTotal = this.completedBefore.get() + handled;
            value = this.received.updateAndGet(current -> Math.max(current, batchTotal));
            int remaining = p.getNumberOfRemainingSuboperations();
            if (remaining >= 0 && this.getSeriesInstanceList().isEmpty()) {
                int total = batchTotal + remaining;
                GuiExecutor.execute(() -> {
                    this.getProgressBar().setIndeterminate(false);
                    this.getProgressBar().setMaximum(total);
                });
            }
        }
        GuiExecutor.execute(() -> {
            JProgressBar bar = this.getProgressBar();
            if (value > bar.getMaximum()) {
                bar.setMaximum(value);
            }
            bar.setValue(value);
        });
    }

    private void awaitPrevious() {
        if (this.previous == null) {
            return;
        }
        try {
            this.previous.get(30L, TimeUnit.SECONDS);
        }
        catch (CancellationException cancellationException) {
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        catch (TimeoutException e) {
            LOGGER.warn("Timeout while stopping the previous retrieve of series {}", (Object)this.getDicomSeries());
        }
        catch (ExecutionException e) {
            LOGGER.error("Previous retrieve of series {}", (Object)this.getDicomSeries(), (Object)e);
        }
    }

    private void awaitPendingImports() {
        if (this.cancelRequested.get() || this.isCancelled()) {
            return;
        }
        Future<?> barrier = DicomModel.LOADING_EXECUTOR.submit(() -> {});
        try {
            barrier.get(120L, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        catch (TimeoutException e) {
            LOGGER.warn("Timeout while importing the objects of series {}", (Object)this.getDicomSeries());
        }
        catch (ExecutionException e) {
            LOGGER.error("Importing the objects of series {}", (Object)this.getDicomSeries(), (Object)e);
        }
    }

    protected LoadSeries createResumeTask() {
        return new LoadQrSeries(this);
    }

    public LoadSeries cancelAndReplace(LoadSeries s, boolean restartAllDownload) {
        if (s instanceof LoadQrSeries) {
            LoadQrSeries qrSeries = (LoadQrSeries)s;
            qrSeries.preempt();
        }
        return super.cancelAndReplace(s, restartAllDownload);
    }

    private void preempt() {
        if (this.isDone()) {
            return;
        }
        this.cancelRequested.set(true);
        this.progress.abort();
        super.cancel();
    }

    public boolean cancel() {
        boolean first = this.cancelRequested.compareAndSet(false, true);
        if (this.isDone() || !first || !this.retrieving.get()) {
            return super.cancel();
        }
        this.stopProgress();
        GracefulCancel.request(() -> this.isDone(), () -> ((DicomProgress)this.progress).cancel(), () -> ((DicomProgress)this.progress).abort(), () -> super.cancel());
        return true;
    }

    public boolean isStopped() {
        return this.cancelRequested.get() || super.isStopped();
    }

    protected void done() {
        this.context.unregister(this);
        super.done();
        if (this.isStopped()) {
            LoadSeries.notifyDownloadCompletion((DicomModel)this.getDicomModel());
        }
    }
}

