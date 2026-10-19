/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.dcm4che3.data.Attributes
 *  org.dcm4che3.net.service.QueryRetrieveLevel
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.explorer.ObservableEvent
 *  org.weasis.core.api.explorer.ObservableEvent$BasicAction
 *  org.weasis.core.api.gui.util.GuiExecutor
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.gui.util.WinUtil
 *  org.weasis.core.api.media.data.MediaSeries
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.MediaSeriesGroupNode
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.media.data.Taggable
 *  org.weasis.core.api.net.URLParameters
 *  org.weasis.core.api.net.auth.AuthMethod
 *  org.weasis.core.ui.tp.raven.spinner.SpinnerProgress
 *  org.weasis.core.util.StringUtil
 *  org.weasis.dicom.codec.DicomSeries
 *  org.weasis.dicom.codec.TagD
 *  org.weasis.dicom.codec.utils.SeriesInstanceList
 *  org.weasis.dicom.explorer.DicomModel
 *  org.weasis.dicom.explorer.PluginOpeningStrategy
 *  org.weasis.dicom.explorer.exp.ExplorerTask
 *  org.weasis.dicom.explorer.pref.node.AbstractDicomNode
 *  org.weasis.dicom.explorer.pref.node.AbstractDicomNode$RetrieveType
 *  org.weasis.dicom.explorer.pref.node.AbstractDicomNode$Type
 *  org.weasis.dicom.explorer.pref.node.AbstractDicomNode$UsageType
 *  org.weasis.dicom.explorer.pref.node.DefaultDicomNode
 *  org.weasis.dicom.explorer.pref.node.DicomWebNode
 *  org.weasis.dicom.explorer.pref.node.DicomWebNode$WebType
 *  org.weasis.dicom.explorer.rs.RsQueryResult
 *  org.weasis.dicom.explorer.wado.DownloadManager
 *  org.weasis.dicom.explorer.wado.DownloadManager$PriorityTaskComparator
 *  org.weasis.dicom.explorer.wado.DownloadPriority
 *  org.weasis.dicom.explorer.wado.LoadSeries
 *  org.weasis.dicom.mf.WadoParameters
 *  org.weasis.dicom.op.CFind
 *  org.weasis.dicom.param.AdvancedParams
 *  org.weasis.dicom.param.ConnectOptions
 *  org.weasis.dicom.param.DicomNode
 *  org.weasis.dicom.param.DicomParam
 *  org.weasis.dicom.param.DicomState
 *  org.weasis.dicom.tool.DicomListener
 *  org.weasis.dicom.web.MultipartConstants$DicomContentType
 */
package org.weasis.dicom.qr;

import java.awt.Component;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JOptionPane;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.service.QueryRetrieveLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.WinUtil;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.MediaSeriesGroupNode;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.media.data.Taggable;
import org.weasis.core.api.net.URLParameters;
import org.weasis.core.api.net.auth.AuthMethod;
import org.weasis.core.ui.tp.raven.spinner.SpinnerProgress;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.utils.SeriesInstanceList;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.PluginOpeningStrategy;
import org.weasis.dicom.explorer.exp.ExplorerTask;
import org.weasis.dicom.explorer.pref.node.AbstractDicomNode;
import org.weasis.dicom.explorer.pref.node.DefaultDicomNode;
import org.weasis.dicom.explorer.pref.node.DicomWebNode;
import org.weasis.dicom.explorer.rs.RsQueryResult;
import org.weasis.dicom.explorer.wado.DownloadManager;
import org.weasis.dicom.explorer.wado.DownloadPriority;
import org.weasis.dicom.explorer.wado.LoadSeries;
import org.weasis.dicom.mf.WadoParameters;
import org.weasis.dicom.op.CFind;
import org.weasis.dicom.param.AdvancedParams;
import org.weasis.dicom.param.ConnectOptions;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.DicomParam;
import org.weasis.dicom.param.DicomState;
import org.weasis.dicom.qr.DicomQrView;
import org.weasis.dicom.qr.LoadQrSeries;
import org.weasis.dicom.qr.LoadWadoUriSeries;
import org.weasis.dicom.qr.Messages;
import org.weasis.dicom.qr.RetrieveContext;
import org.weasis.dicom.qr.RetrieveSelection;
import org.weasis.dicom.tool.DicomListener;
import org.weasis.dicom.web.MultipartConstants;

public class RetrieveTask
extends ExplorerTask<ExplorerTask<Boolean, String>, String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RetrieveTask.class);
    private final RetrieveSelection selection;
    private final DicomModel explorerDcmModel;
    private final DicomQrView dicomQrView;
    protected final PluginOpeningStrategy openingStrategy;
    private final AtomicBoolean cancelRequested = new AtomicBoolean();

    public RetrieveTask(RetrieveSelection selection, DicomModel explorerDcmModel, DicomQrView dicomQrView) {
        super(AbstractDicomNode.UsageType.RETRIEVE.toString(), false);
        this.selection = selection;
        this.explorerDcmModel = explorerDcmModel;
        this.dicomQrView = dicomQrView;
        this.openingStrategy = new PluginOpeningStrategy(DownloadManager.getOpeningViewer());
    }

    protected ExplorerTask<Boolean, String> doInBackground() throws Exception {
        this.explorerDcmModel.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.LOADING_START, (Object)this.explorerDcmModel, null, (Object)this));
        String errorMessage = null;
        Object selectedItem = this.dicomQrView.getComboDestinationNode().getSelectedItem();
        if (selectedItem instanceof DefaultDicomNode) {
            DefaultDicomNode node = (DefaultDicomNode)selectedItem;
            DefaultDicomNode callingNode = (DefaultDicomNode)this.dicomQrView.getComboCallingNode().getSelectedItem();
            if (callingNode == null) {
                errorMessage = Messages.getString("RetrieveTask.no_calling_node");
            } else {
                AbstractDicomNode.RetrieveType type = (AbstractDicomNode.RetrieveType)this.dicomQrView.getComboDicomRetrieveType().getSelectedItem();
                AdvancedParams params = new AdvancedParams();
                ConnectOptions connectOptions = new ConnectOptions();
                connectOptions.setConnectTimeout(3000);
                connectOptions.setAcceptTimeout(5000);
                params.setConnectOptions(connectOptions);
                if (AbstractDicomNode.RetrieveType.CGET == type || AbstractDicomNode.RetrieveType.CMOVE == type) {
                    errorMessage = this.queueSeriesRetrieve(params, callingNode, node, type, null);
                } else if (AbstractDicomNode.RetrieveType.WADO == type) {
                    WadoParameters wadoParameters = this.buildWadoUriParameters(node);
                    if (wadoParameters == null) {
                        return null;
                    }
                    errorMessage = this.queueSeriesRetrieve(params, callingNode, node, type, wadoParameters);
                } else {
                    errorMessage = Messages.getString("RetrieveTask.msg_retrieve_type");
                    LOGGER.error("Dicom retrieve error: {}", (Object)errorMessage);
                }
            }
        } else if (selectedItem instanceof DicomWebNode) {
            this.fillSeries();
        }
        if (errorMessage != null && !this.cancelRequested.get()) {
            String mes = errorMessage;
            String errorTitle = StringUtil.getEmptyStringIfNull((Object)this.dicomQrView.getComboDicomRetrieveType().getSelectedItem());
            GuiExecutor.execute(() -> JOptionPane.showMessageDialog(WinUtil.getValidComponent((Component)((Object)this.dicomQrView)), mes, errorTitle, 0));
        }
        return null;
    }

    private WadoParameters buildWadoUriParameters(DefaultDicomNode node) {
        String m1;
        List webNodes = AbstractDicomNode.loadDicomNodes((AbstractDicomNode.Type)AbstractDicomNode.Type.WEB, (AbstractDicomNode.UsageType)AbstractDicomNode.UsageType.RETRIEVE, (DicomWebNode.WebType)DicomWebNode.WebType.WADO);
        String host = RetrieveTask.getHostname(node.getDicomNode().getHostname());
        DicomWebNode wnode = RetrieveTask.getWadoUrl(this.dicomQrView, host, webNodes, m1 = Messages.getString("RetrieveTask.no_wado_url_match"));
        if (wnode == null) {
            return null;
        }
        WadoParameters wadoParameters = new WadoParameters("local", wnode.getUrl().toString(), false, null, null, null);
        wnode.getHeaders().forEach((arg_0, arg_1) -> ((WadoParameters)wadoParameters).addHttpTag(arg_0, arg_1));
        return wadoParameters;
    }

    private String queueSeriesRetrieve(AdvancedParams params, DefaultDicomNode callingNode, DefaultDicomNode calledNode, AbstractDicomNode.RetrieveType type, WadoParameters wadoParameters) {
        Path storageDir;
        DicomModel queryModel = this.dicomQrView.getDicomModel();
        if (queryModel == null) {
            return Messages.getString("RetrieveTask.msg_unexpected_error");
        }
        DicomListener dicomListener = this.dicomQrView.getDicomListener();
        if (AbstractDicomNode.RetrieveType.CMOVE == type) {
            if (dicomListener == null) {
                return Messages.getString("RetrieveTask.msg_start_listener");
            }
            if (dicomListener.isRunning()) {
                return Messages.getString("RetrieveTask.msg_running_listener");
            }
            storageDir = dicomListener.getStoreSCP().getStorageDir();
        } else {
            storageDir = DicomQrView.getSessionTempFolder();
        }
        this.openingStrategy.setFullImportSession(false);
        RetrieveContext context = new RetrieveContext(type, params, callingNode, calledNode, this.explorerDcmModel, this.openingStrategy, dicomListener, storageDir);
        boolean queued = false;
        for (String studyUid : this.selection.getStudyUids()) {
            if (this.cancelRequested.get()) break;
            List<LoadSeries> tasks = this.buildStudyTasks(context, queryModel, studyUid, wadoParameters);
            if (tasks.isEmpty()) continue;
            if (!queued) {
                this.openingStrategy.prepareImport();
                queued = true;
            }
            this.startTasks(context, tasks);
        }
        if (!queued) {
            return this.cancelRequested.get() ? null : Messages.getString("RetrieveTask.no_series_found");
        }
        return null;
    }

    private List<LoadSeries> buildStudyTasks(RetrieveContext context, DicomModel queryModel, String studyUid, WadoParameters wadoParameters) {
        DicomParam[] keys = new DicomParam[]{new DicomParam(0x20000D, new String[]{studyUid}), CFind.SeriesInstanceUID, CFind.Modality, CFind.SeriesNumber, CFind.SeriesDescription, new DicomParam(2101769, new String[0])};
        DicomState state = CFind.process((AdvancedParams)context.getParams(), (DicomNode)context.getCallingNodeForQuery(), (DicomNode)context.getCalledNode(), (int)0, (QueryRetrieveLevel)QueryRetrieveLevel.SERIES, (DicomParam[])keys);
        List seriesRsp = state.getDicomRSP();
        if (seriesRsp == null || seriesRsp.isEmpty()) {
            LOGGER.warn("No series found for study {}: {}", (Object)studyUid, (Object)state.getMessage());
            return List.of();
        }
        MediaSeriesGroup study = this.getStudyNode(queryModel, studyUid);
        if (study == null) {
            return List.of();
        }
        boolean startDownloading = GuiUtils.getUICore().getSystemPreferences().getBooleanProperty("weasis.download.immediately", true);
        ArrayList<LoadSeries> tasks = new ArrayList<LoadSeries>(seriesRsp.size());
        int enumerated = 0;
        for (Attributes seriesDataset : seriesRsp) {
            DicomSeries dicomSeries;
            if (this.cancelRequested.get()) break;
            if (!this.selection.contains(studyUid, seriesDataset.getString(0x20000E)) || (dicomSeries = this.getCFindSeries(study, seriesDataset)) == null) continue;
            LoadSeries task = RetrieveTask.createSeriesTask(context, dicomSeries, wadoParameters, startDownloading);
            task.setPriority(new DownloadPriority(this.explorerDcmModel.getParent(study, DicomModel.patient), study, (MediaSeriesGroup)dicomSeries, context.hasConcurrentRetrieve()));
            tasks.add(task);
            this.updateEnumerationProgress(++enumerated, seriesRsp.size());
        }
        return tasks;
    }

    private static LoadSeries createSeriesTask(RetrieveContext context, DicomSeries dicomSeries, WadoParameters wadoParameters, boolean startDownloading) {
        if (wadoParameters == null) {
            return new LoadQrSeries(dicomSeries, context, startDownloading);
        }
        dicomSeries.setTag(TagW.WadoParameters, (Object)wadoParameters);
        return new LoadWadoUriSeries(dicomSeries, context, RetrieveTask.getConcurrentDownloadsInSeries(), startDownloading);
    }

    private static int getConcurrentDownloadsInSeries() {
        return GuiUtils.getUICore().getSystemPreferences().getIntProperty("download.concurrent.series.images", 4);
    }

    private DicomSeries getCFindSeries(MediaSeriesGroup study, Attributes seriesDataset) {
        String seriesUid = seriesDataset.getString(0x20000E);
        if (!StringUtil.hasText((CharSequence)seriesUid)) {
            return null;
        }
        TagW[] tagWArray = this.explorerDcmModel.getHierarchyNode(study, (Object)seriesUid);
        if (tagWArray instanceof DicomSeries) {
            DicomSeries existing = (DicomSeries)tagWArray;
            return existing;
        }
        DicomSeries dicomSeries = new DicomSeries(seriesUid);
        dicomSeries.setTag(TagD.get((int)0x20000E), (Object)seriesUid);
        dicomSeries.setTag(TagW.ExplorerModel, (Object)this.explorerDcmModel);
        dicomSeries.setTag(TagW.WadoInstanceReferenceList, (Object)new SeriesInstanceList());
        for (TagW tag : TagD.getTagFromIDs((int[])new int[]{524384, 0x200011, 528446, 2101769})) {
            tag.readValue((Object)seriesDataset, (Taggable)dicomSeries);
        }
        this.explorerDcmModel.addHierarchyNode(study, (MediaSeriesGroup)dicomSeries);
        return dicomSeries;
    }

    private void startTasks(RetrieveContext context, List<LoadSeries> tasks) {
        for (LoadSeries task : tasks) {
            if (!DicomModel.isHiddenModality((MediaSeries)task.getDicomSeries())) {
                task.createSeriesThumbnail();
            }
            DownloadManager.addLoadSeries((LoadSeries)task, (DicomModel)this.explorerDcmModel, (boolean)task.isStartDownloading());
        }
        DownloadManager.getTasks().sort(Collections.reverseOrder(new DownloadManager.PriorityTaskComparator()));
        if (context.hasConcurrentRetrieve()) {
            DownloadManager.CONCURRENT_EXECUTOR.prestartAllCoreThreads();
        } else {
            DownloadManager.UNIQUE_EXECUTOR.prestartAllCoreThreads();
        }
    }

    private void updateEnumerationProgress(int done, int total) {
        GuiExecutor.execute(() -> {
            if (this.cancelRequested.get()) {
                return;
            }
            SpinnerProgress bar = this.getBar();
            bar.setIndeterminate(false);
            bar.setMaximum(total);
            bar.setValue(done);
            bar.setString(done + "/" + total);
        });
    }

    public boolean cancel() {
        this.cancelRequested.set(true);
        GuiExecutor.execute(() -> this.getBar().setString(Messages.getString("RetrieveTask.cancelling")));
        return super.cancel();
    }

    protected void done() {
        this.removeAllCancelListeners();
        this.openingStrategy.reset();
        this.explorerDcmModel.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.LOADING_STOP, (Object)this.explorerDcmModel, null, (Object)this));
        try {
            ExplorerTask task = (ExplorerTask)this.get();
            if (task != null) {
                DicomModel.LOADING_EXECUTOR.execute((Runnable)task);
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        catch (CancellationException e) {
            LOGGER.info("The DICOM retrieve has been cancelled");
        }
        catch (Exception e) {
            LOGGER.error("Retrieving DICOM data:", (Throwable)e);
        }
    }

    static DicomWebNode getWadoUrl(DicomQrView dicomQrView, String host, List<AbstractDicomNode> webNodes, String message1) {
        ArrayList wadoURLs = new ArrayList();
        for (AbstractDicomNode n : webNodes) {
            if (!(n instanceof DicomWebNode)) continue;
            DicomWebNode wn = (DicomWebNode)n;
            URL url = wn.getUrl();
            if (!DicomWebNode.WebType.WADO.equals((Object)wn.getWebType()) || url == null || !RetrieveTask.getHostname(url.getHost()).contains(host)) continue;
            wadoURLs.add(wn);
        }
        if (wadoURLs.isEmpty()) {
            GuiExecutor.execute(() -> JOptionPane.showMessageDialog(WinUtil.getValidComponent((Component)((Object)dicomQrView)), message1, null, 0));
            return null;
        }
        if (wadoURLs.size() > 1) {
            GuiExecutor.invokeAndWait(() -> {
                Object[] options = wadoURLs.toArray();
                Object response = JOptionPane.showInputDialog(WinUtil.getValidComponent((Component)((Object)dicomQrView)), Messages.getString("RetrieveTask.several_wado_urls"), ((DicomWebNode)wadoURLs.getFirst()).getWebType().toString(), 3, null, options, options[0]);
                if (response != null) {
                    wadoURLs.clear();
                    wadoURLs.add((DicomWebNode)response);
                }
            });
        }
        return (DicomWebNode)wadoURLs.getFirst();
    }

    static String getHostname(String host) {
        if ("127.0.0.1".equals(host) || "127.0.1.1".equals(host) || "::1".equals(host)) {
            return "localhost";
        }
        return host;
    }

    private void fillSeries() {
        DicomModel dicomModel = this.dicomQrView.getDicomModel();
        if (dicomModel == null) {
            return;
        }
        DicomWebNode retrieveNode = this.dicomQrView.getRetrieveNode();
        String baseUrl = retrieveNode.getUrl().toString();
        Properties props = new Properties();
        props.setProperty("dicomweb.url", baseUrl);
        props.setProperty("accept.ext", "transfer-syntax=*");
        HashMap<String, LoadSeries> loadMap = new HashMap<String, LoadSeries>();
        boolean startDownloading = GuiUtils.getUICore().getSystemPreferences().getBooleanProperty("weasis.download.immediately", true);
        WadoParameters wadoParameters = new WadoParameters("", true, true);
        retrieveNode.getHeaders().forEach((arg_0, arg_1) -> ((WadoParameters)wadoParameters).addHttpTag(arg_0, arg_1));
        wadoParameters.addHttpTag("Accept", "multipart/related;type=\"" + String.valueOf(MultipartConstants.DicomContentType.DICOM) + "\";" + props.getProperty("accept.ext"));
        URLParameters queryParameters = RsQueryResult.jsonQueryParameters((Map)retrieveNode.getHeaders());
        for (String studyInstanceUID : this.selection.getStudyUids()) {
            MediaSeriesGroup study = this.getStudyNode(dicomModel, studyInstanceUID);
            String url = RsQueryResult.seriesQueryUrl((String)baseUrl, (String)studyInstanceUID, null);
            try {
                LOGGER.debug("QIDO-RS request: {}", (Object)url);
                List series = RsQueryResult.parseJSON((String)url, (AuthMethod)this.dicomQrView.getAuthMethod(), (URLParameters)queryParameters);
                for (Attributes seriesDataset : series) {
                    if (!this.selection.contains(studyInstanceUID, seriesDataset.getString(0x20000E))) continue;
                    this.getSeries(study, seriesDataset, loadMap, wadoParameters, baseUrl, startDownloading);
                }
            }
            catch (Exception e) {
                LOGGER.error("QIDO-RS all series with studyUID {}", (Object)studyInstanceUID, (Object)e);
            }
        }
        if (!loadMap.isEmpty()) {
            this.openingStrategy.prepareImport();
            WadoParameters wp = new WadoParameters("", true, true);
            retrieveNode.getHeaders().forEach((arg_0, arg_1) -> ((WadoParameters)wp).addHttpTag(arg_0, arg_1));
            wp.addHttpTag("Accept", "image/jpeg");
            for (LoadSeries loadSeries : loadMap.values()) {
                if (!DicomModel.isHiddenModality((MediaSeries)loadSeries.getDicomSeries())) {
                    loadSeries.startDownloadImageReference(wp);
                }
                loadSeries.setPOpeningStrategy(this.openingStrategy);
                DownloadManager.addLoadSeries((LoadSeries)loadSeries, (DicomModel)this.explorerDcmModel, (boolean)loadSeries.isStartDownloading());
            }
            DownloadManager.getTasks().sort(Collections.reverseOrder(new DownloadManager.PriorityTaskComparator()));
            DownloadManager.CONCURRENT_EXECUTOR.prestartAllCoreThreads();
        }
    }

    public MediaSeriesGroup getStudyNode(DicomModel dicomModel, String studyUID) {
        Objects.requireNonNull(studyUID);
        for (MediaSeriesGroup pt : dicomModel.getChildren(MediaSeriesGroupNode.rootNode)) {
            for (MediaSeriesGroup st : dicomModel.getChildren(pt)) {
                MediaSeriesGroup study;
                if (!st.matchIdValue((Object)studyUID)) continue;
                MediaSeriesGroup patient = this.explorerDcmModel.getHierarchyNode(MediaSeriesGroupNode.rootNode, pt.getTagValue(pt.getTagID()));
                if (patient == null) {
                    MediaSeriesGroupNode p = new MediaSeriesGroupNode(pt.getTagID(), pt.getTagValue(pt.getTagID()), DicomModel.patient.tagView());
                    pt.getTagEntrySetIterator().forEachRemaining(e -> p.setTag((TagW)e.getKey(), e.getValue()));
                    this.explorerDcmModel.addHierarchyNode(MediaSeriesGroupNode.rootNode, (MediaSeriesGroup)p);
                    patient = p;
                }
                if ((study = this.explorerDcmModel.getHierarchyNode(patient, (Object)studyUID)) == null) {
                    MediaSeriesGroupNode s = new MediaSeriesGroupNode(st.getTagID(), st.getTagValue(st.getTagID()), DicomModel.study.tagView());
                    st.getTagEntrySetIterator().forEachRemaining(e -> s.setTag((TagW)e.getKey(), e.getValue()));
                    this.explorerDcmModel.addHierarchyNode(patient, (MediaSeriesGroup)s);
                    study = s;
                }
                return study;
            }
        }
        return null;
    }

    private DicomSeries getSeries(MediaSeriesGroup study, Attributes seriesDataset, Map<String, LoadSeries> loadMap, WadoParameters wadoParameters, String baseUrl, boolean startDownloading) {
        if (seriesDataset == null) {
            throw new IllegalArgumentException("seriesDataset cannot be null");
        }
        String seriesUID = seriesDataset.getString(0x20000E);
        DicomSeries dicomSeries = (DicomSeries)this.explorerDcmModel.getHierarchyNode(study, (Object)seriesUID);
        if (dicomSeries == null) {
            TagW[] tags;
            dicomSeries = new DicomSeries(seriesUID);
            dicomSeries.setTag(TagD.get((int)0x20000E), (Object)seriesUID);
            dicomSeries.setTag(TagW.ExplorerModel, (Object)this.explorerDcmModel);
            dicomSeries.setTag(TagW.WadoParameters, (Object)wadoParameters);
            dicomSeries.setTag(TagW.WadoInstanceReferenceList, (Object)new SeriesInstanceList());
            dicomSeries.setTag(LoadSeries.SERIES_BULK_RETRIEVE, (Object)Boolean.TRUE);
            for (TagW tag : tags = TagD.getTagFromIDs((int[])new int[]{524384, 0x200011, 528446, 528784, 2101769})) {
                tag.readValue((Object)seriesDataset, (Taggable)dicomSeries);
            }
            if (!StringUtil.hasText((CharSequence)((CharSequence)TagD.getTagValue((TagReadable)dicomSeries, (int)528784, String.class)))) {
                StringBuilder buf = new StringBuilder(baseUrl);
                buf.append("/studies/");
                buf.append(study.getTagValue(TagD.get((int)0x20000D)));
                buf.append("/series/");
                buf.append(seriesUID);
                dicomSeries.setTag(TagD.get((int)528784), (Object)buf.toString());
            }
            this.explorerDcmModel.addHierarchyNode(study, (MediaSeriesGroup)dicomSeries);
            LoadSeries loadSeries = new LoadSeries(dicomSeries, this.explorerDcmModel, this.dicomQrView.getAuthMethod(), RetrieveTask.getConcurrentDownloadsInSeries(), true, startDownloading);
            loadSeries.setPriority(new DownloadPriority(this.explorerDcmModel.getParent(study, DicomModel.patient), study, (MediaSeriesGroup)dicomSeries, true));
            loadMap.put((String)TagD.getTagValue((TagReadable)dicomSeries, (int)0x20000E, String.class), loadSeries);
        }
        return dicomSeries;
    }
}

