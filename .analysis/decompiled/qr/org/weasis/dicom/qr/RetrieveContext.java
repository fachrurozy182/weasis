/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.dcm4che3.data.Attributes
 *  org.dcm4che3.img.util.DicomUtils
 *  org.dcm4che3.net.service.QueryRetrieveLevel
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.util.ResourceUtil
 *  org.weasis.core.api.util.ResourceUtil$ResourcePath
 *  org.weasis.core.util.StringUtil
 *  org.weasis.dicom.codec.DicomSeries
 *  org.weasis.dicom.codec.TagD
 *  org.weasis.dicom.codec.utils.DicomResource
 *  org.weasis.dicom.codec.utils.SeriesInstanceList
 *  org.weasis.dicom.explorer.DicomModel
 *  org.weasis.dicom.explorer.PluginOpeningStrategy
 *  org.weasis.dicom.explorer.pref.node.AbstractDicomNode$RetrieveType
 *  org.weasis.dicom.explorer.pref.node.DefaultDicomNode
 *  org.weasis.dicom.mf.SopInstance
 *  org.weasis.dicom.op.CFind
 *  org.weasis.dicom.param.AdvancedParams
 *  org.weasis.dicom.param.DicomNode
 *  org.weasis.dicom.param.DicomParam
 *  org.weasis.dicom.param.DicomState
 *  org.weasis.dicom.param.ListenerParams
 *  org.weasis.dicom.tool.DicomListener
 */
package org.weasis.dicom.qr;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.img.util.DicomUtils;
import org.dcm4che3.net.service.QueryRetrieveLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.utils.DicomResource;
import org.weasis.dicom.codec.utils.SeriesInstanceList;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.PluginOpeningStrategy;
import org.weasis.dicom.explorer.pref.node.AbstractDicomNode;
import org.weasis.dicom.explorer.pref.node.DefaultDicomNode;
import org.weasis.dicom.mf.SopInstance;
import org.weasis.dicom.op.CFind;
import org.weasis.dicom.param.AdvancedParams;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.DicomParam;
import org.weasis.dicom.param.DicomState;
import org.weasis.dicom.param.ListenerParams;
import org.weasis.dicom.qr.LoadQrSeries;
import org.weasis.dicom.qr.Messages;
import org.weasis.dicom.tool.DicomListener;

public class RetrieveContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(RetrieveContext.class);
    private final AbstractDicomNode.RetrieveType type;
    private final AdvancedParams params;
    private final DefaultDicomNode callingNode;
    private final DefaultDicomNode calledNode;
    private final DicomModel dicomModel;
    private final PluginOpeningStrategy openingStrategy;
    private final DicomListener dicomListener;
    private final Path storageDir;
    private final Set<LoadQrSeries> pendingTasks = ConcurrentHashMap.newKeySet();

    public RetrieveContext(AbstractDicomNode.RetrieveType type, AdvancedParams params, DefaultDicomNode callingNode, DefaultDicomNode calledNode, DicomModel dicomModel, PluginOpeningStrategy openingStrategy, DicomListener dicomListener, Path storageDir) {
        this.type = type;
        this.params = params;
        this.callingNode = callingNode;
        this.calledNode = calledNode;
        this.dicomModel = dicomModel;
        this.openingStrategy = openingStrategy;
        this.dicomListener = dicomListener;
        this.storageDir = storageDir;
    }

    public AbstractDicomNode.RetrieveType getType() {
        return this.type;
    }

    public AdvancedParams getParams() {
        return this.params;
    }

    public DicomModel getDicomModel() {
        return this.dicomModel;
    }

    public PluginOpeningStrategy getOpeningStrategy() {
        return this.openingStrategy;
    }

    public Path getStorageDir() {
        return this.storageDir;
    }

    public String getDestinationAet() {
        return this.callingNode.getAeTitle();
    }

    public DicomNode getCallingNodeForQuery() {
        return this.callingNode.getDicomNodeWithOnlyAET();
    }

    public DicomNode getCallingNodeForMove() {
        return this.callingNode.getDicomNode();
    }

    public DicomNode getCalledNode() {
        return this.calledNode.getDicomNode();
    }

    public boolean hasConcurrentRetrieve() {
        return AbstractDicomNode.RetrieveType.CMOVE != this.type;
    }

    public void fillInstances(DicomSeries series) {
        SeriesInstanceList instanceList = (SeriesInstanceList)series.getTagValue(TagW.WadoInstanceReferenceList);
        if (instanceList == null || !instanceList.isEmpty()) {
            return;
        }
        MediaSeriesGroup study = this.dicomModel.getParent((MediaSeriesGroup)series, DicomModel.study);
        String studyUid = (String)TagD.getTagValue((TagReadable)study, (int)0x20000D, String.class);
        String seriesUid = (String)TagD.getTagValue((TagReadable)series, (int)0x20000E, String.class);
        if (!StringUtil.hasText((CharSequence)studyUid) || !StringUtil.hasText((CharSequence)seriesUid)) {
            return;
        }
        DicomParam[] keys = new DicomParam[]{new DicomParam(0x20000D, new String[]{studyUid}), new DicomParam(0x20000E, new String[]{seriesUid}), CFind.SOPInstanceUID, CFind.InstanceNumber};
        DicomState state = CFind.process((AdvancedParams)this.params, (DicomNode)this.getCallingNodeForQuery(), (DicomNode)this.getCalledNode(), (int)0, (QueryRetrieveLevel)QueryRetrieveLevel.IMAGE, (DicomParam[])keys);
        List instances = state.getDicomRSP();
        if (instances == null) {
            LOGGER.warn("No instance found for series {}: {}", (Object)seriesUid, (Object)state.getMessage());
            return;
        }
        for (Attributes instanceDataset : instances) {
            String sopUid = instanceDataset.getString(524312);
            Integer instanceNumber = DicomUtils.getIntegerFromDicomElement((Attributes)instanceDataset, (int)2097171, null);
            if (!StringUtil.hasText((CharSequence)sopUid) || instanceList.getSopInstance(sopUid) != null) continue;
            instanceList.addSopInstance(new SopInstance(sopUid, instanceNumber));
        }
    }

    public void register(LoadQrSeries task) {
        this.pendingTasks.add(task);
    }

    public void unregister(LoadQrSeries task) {
        this.pendingTasks.remove((Object)task);
        if (this.pendingTasks.isEmpty()) {
            this.stopListener();
        }
    }

    public synchronized String ensureListenerStarted() {
        if (this.dicomListener == null) {
            return Messages.getString("RetrieveTask.msg_start_listener");
        }
        if (this.dicomListener.isRunning()) {
            return null;
        }
        try {
            this.dicomListener.start(this.getCallingNodeForMove(), new ListenerParams(this.params, true));
            return null;
        }
        catch (Exception e) {
            LOGGER.error("Start DICOM listener", (Throwable)e);
            return Messages.getString("RetrieveTask.msg_start_listener");
        }
    }

    private synchronized void stopListener() {
        if (this.dicomListener != null && this.dicomListener.isRunning()) {
            this.dicomListener.stop();
        }
    }

    public static URL getCGetSopClassUrl() {
        File sopClass = ResourceUtil.getResource((ResourceUtil.ResourcePath)DicomResource.CGET_SOP_UID);
        if (sopClass.canRead()) {
            try {
                return sopClass.toURI().toURL();
            }
            catch (MalformedURLException e) {
                LOGGER.error("SOP Class url conversion", (Throwable)e);
            }
        }
        return null;
    }
}

