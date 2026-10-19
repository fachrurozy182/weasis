/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StreamUtil
 */
package org.weasis.dicom.op;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.VR;
import org.dcm4che3.img.stream.BytesWithImageDescriptor;
import org.dcm4che3.img.stream.ImageAdapter;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.DataWriter;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.net.InputStreamDataWriter;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.ExtendedNegotiation;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.pdu.RoleSelection;
import org.dcm4che3.net.service.BasicCStoreSCP;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.tool.getscu.GetSCU;
import org.dcm4che3.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.StreamUtil;
import org.weasis.dicom.param.AdvancedParams;
import org.weasis.dicom.param.AttributeEditorContext;
import org.weasis.dicom.param.CstoreParams;
import org.weasis.dicom.param.DeviceOpService;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.DicomProgress;
import org.weasis.dicom.param.DicomState;
import org.weasis.dicom.util.ServiceUtil;
import org.weasis.dicom.util.StoreFromStreamSCU;

public class CGetForward
implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(CGetForward.class);
    private final Device device = new Device("getscu");
    private final ApplicationEntity ae;
    private final Connection conn = new Connection();
    private final Connection remote = new Connection();
    private final AAssociateRQ rq = new AAssociateRQ();
    private int priority;
    private InformationModel model;
    private final Attributes keys = new Attributes();
    private Association as;
    private final StoreFromStreamSCU streamSCU;
    private final DeviceOpService streamSCUService;
    private final CstoreParams cstoreParams;
    private final BasicCStoreSCP storageSCP = new BasicCStoreSCP(new String[]{"*"}){

        @Override
        protected void store(Association as, PresentationContext pc, Attributes rq, PDVInputStream data, Attributes rsp) throws IOException {
            DicomProgress progress = CGetForward.this.streamSCU.getState().getProgress();
            if (progress != null && progress.isCancelled()) {
                StreamUtil.safeClose((AutoCloseable)CGetForward.this);
                return;
            }
            try {
                String cuid = rq.getString(2);
                String iuid = rq.getString(4096);
                String tsuid = pc.getTransferSyntax();
                Fmi fmi = new Fmi(tsuid, cuid, iuid);
                this.ensureStreamSCUReady(cuid, tsuid);
                DataWriter dataWriter = this.createDataWriter(as, data, fmi);
                CGetForward.this.streamSCU.cstore(cuid, iuid, CGetForward.this.priority, dataWriter, tsuid);
            }
            catch (AbortException e) {
                this.notifyProgressionFailure(rq);
                throw e;
            }
            catch (Exception e) {
                this.handleStoreException(e, rq);
            }
        }

        private void ensureStreamSCUReady(String cuid, String tsuid) throws IOException {
            if (CGetForward.this.streamSCU.hasAssociation()) {
                this.handleExistingAssociation(cuid, tsuid);
            } else {
                this.initializeNewAssociation(cuid, tsuid);
            }
        }

        private void handleExistingAssociation(String cuid, String tsuid) throws IOException {
            Set<String> supportedSyntaxes = CGetForward.this.streamSCU.getTransferSyntaxesFor(cuid);
            if (!supportedSyntaxes.contains(tsuid)) {
                CGetForward.this.streamSCU.close(true);
            }
            CGetForward.this.streamSCU.addData(cuid, tsuid);
            if (!CGetForward.this.streamSCU.isReadyForDataTransfer()) {
                CGetForward.this.streamSCU.open();
            }
        }

        private void initializeNewAssociation(String cuid, String tsuid) throws IOException {
            CGetForward.this.streamSCUService.start();
            CGetForward.this.streamSCU.addData(cuid, tsuid);
            CGetForward.this.streamSCU.open();
        }

        private DataWriter createDataWriter(Association as, PDVInputStream data, Fmi fmi) throws IOException {
            if (!CGetForward.this.streamSCU.isReadyForDataTransfer()) {
                throw new IllegalStateException("Association not ready for transfer.");
            }
            ImageAdapter.AdaptTransferSyntax syntax = new ImageAdapter.AdaptTransferSyntax(fmi.tsuid, CGetForward.this.streamSCU.selectTransferSyntax(fmi.cuid, fmi.tsuid));
            if (this.canUseDirectStream(syntax, fmi.tsuid)) {
                return new InputStreamDataWriter(data);
            }
            return this.createProcessedDataWriter(as, data, syntax, fmi);
        }

        private boolean canUseDirectStream(ImageAdapter.AdaptTransferSyntax syntax, String tsuid) {
            return (CGetForward.this.cstoreParams == null || !CGetForward.this.cstoreParams.hasDicomEditors()) && syntax.getRequested().equals(tsuid);
        }

        private DataWriter createProcessedDataWriter(Association as, PDVInputStream data, ImageAdapter.AdaptTransferSyntax syntax, Fmi fmi) throws IOException {
            AttributeEditorContext context = this.createAttributeEditorContext(as, syntax);
            try (DicomInputStream dicomInputStream = new DicomInputStream((InputStream)data, syntax.getOriginal());){
                dicomInputStream.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
                Attributes attributes = dicomInputStream.readDataset();
                if (CGetForward.this.cstoreParams != null && CGetForward.this.cstoreParams.hasDicomEditors()) {
                    CGetForward.this.cstoreParams.getDicomEditors().forEach(editor -> editor.apply(attributes, context));
                    fmi.cuid = attributes.getString(524310);
                    fmi.iuid = attributes.getString(524312);
                }
                this.handleAbortConditions(context, data, as);
                BytesWithImageDescriptor desc = ImageAdapter.imageTranscode(attributes, syntax, context);
                DataWriter dataWriter = ImageAdapter.buildDataWriter(attributes, syntax, context.getEditable(), desc);
                return dataWriter;
            }
        }

        private AttributeEditorContext createAttributeEditorContext(Association as, ImageAdapter.AdaptTransferSyntax syntax) {
            return new AttributeEditorContext(syntax.getOriginal(), DicomNode.buildRemoteDicomNode(as), CGetForward.this.streamSCU.getRemoteDicomNode());
        }

        private void handleAbortConditions(AttributeEditorContext context, PDVInputStream data, Association as) throws IOException {
            AttributeEditorContext.Abort abortType = context.getAbort();
            if (abortType == AttributeEditorContext.Abort.FILE_EXCEPTION) {
                data.skipAll();
                throw new IllegalStateException(context.getAbortMessage());
            }
            if (abortType == AttributeEditorContext.Abort.CONNECTION_EXCEPTION) {
                as.abort();
                throw new AbortException("DICOM association abort. " + context.getAbortMessage());
            }
        }

        private void notifyProgressionFailure(Attributes rq) {
            ServiceUtil.notifyProgression(CGetForward.this.streamSCU.getState(), rq.getString(4096), rq.getString(2), 272, ServiceUtil.ProgressStatus.FAILED, CGetForward.this.streamSCU.getNumberOfSuboperations());
        }

        private void handleStoreException(Exception e, Attributes rq) throws DicomServiceException {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.error("Error when forwarding to the final destination", (Throwable)e);
            this.notifyProgressionFailure(rq);
            throw new DicomServiceException(272, (Throwable)e);
        }

        private static class Fmi {
            String tsuid;
            String cuid;
            String iuid;

            public Fmi(String tsuid, String cuid, String iuid) {
                this.tsuid = tsuid;
                this.cuid = cuid;
                this.iuid = iuid;
            }
        }

        private static class AbortException
        extends IllegalStateException {
            private static final long serialVersionUID = -1153741718853819887L;

            public AbortException(String s) {
                super(s);
            }
        }
    };

    public CGetForward(DicomNode callingNode, DicomNode destinationNode, DicomProgress progress) throws IOException {
        this(null, callingNode, destinationNode, progress, null);
    }

    public CGetForward(DicomNode callingNode, DicomNode destinationNode, DicomProgress progress, CstoreParams cstoreParams) throws IOException {
        this(null, callingNode, destinationNode, progress, cstoreParams);
    }

    public CGetForward(AdvancedParams forwardParams, DicomNode callingNode, DicomNode destinationNode, DicomProgress progress, CstoreParams cstoreParams) throws IOException {
        this.cstoreParams = cstoreParams;
        this.ae = new ApplicationEntity("GETSCU");
        this.device.addConnection(this.conn);
        this.device.addApplicationEntity(this.ae);
        this.ae.addConnection(this.conn);
        this.device.setDimseRQHandler(this.createServiceRegistry());
        this.streamSCU = new StoreFromStreamSCU(forwardParams, callingNode, destinationNode, progress);
        this.streamSCUService = new DeviceOpService(this.streamSCU.getDevice());
    }

    public ApplicationEntity getApplicationEntity() {
        return this.ae;
    }

    public Connection getRemoteConnection() {
        return this.remote;
    }

    public AAssociateRQ getAAssociateRQ() {
        return this.rq;
    }

    public Association getAssociation() {
        return this.as;
    }

    public Device getDevice() {
        return this.device;
    }

    public Attributes getKeys() {
        return this.keys;
    }

    private DicomServiceRegistry createServiceRegistry() {
        DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
        serviceRegistry.addDicomService(this.storageSCP);
        return serviceRegistry;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setInformationModel(InformationModel model, String[] tss, boolean relational) {
        this.model = model;
        this.rq.addPresentationContext(new PresentationContext(1, model.cuid, tss));
        if (relational) {
            this.rq.addExtendedNegotiation(new ExtendedNegotiation(model.cuid, new byte[]{1}));
        }
        if (model.level != null) {
            this.addLevel(model.level);
        }
    }

    public void addLevel(String level) {
        this.keys.setString(524370, VR.CS, level);
    }

    public void addKey(int tag, String ... values) {
        VR vr = ElementDictionary.vrOf(tag, this.keys.getPrivateCreator(tag));
        this.keys.setString(tag, vr, values);
    }

    public void addOfferedStorageSOPClass(String cuid, String ... tsuids) {
        if (!this.rq.containsPresentationContextFor(cuid)) {
            this.rq.addRoleSelection(new RoleSelection(cuid, false, true));
        }
        this.rq.addPresentationContext(new PresentationContext(2 * this.rq.getNumberOfPresentationContexts() + 1, cuid, tsuids));
    }

    public void open() throws IOException, InterruptedException, IncompatibleConnectionException, GeneralSecurityException {
        this.as = this.ae.connect(this.conn, this.remote, this.rq);
    }

    @Override
    public void close() throws IOException, InterruptedException {
        if (this.as != null && this.as.isReadyForDataTransfer()) {
            this.as.waitForOutstandingRSP();
            this.as.release();
        }
        this.streamSCU.close(true);
    }

    public void retrieve() throws IOException, InterruptedException {
        this.retrieve(this.keys);
    }

    private void retrieve(Attributes keys) throws IOException, InterruptedException {
        DimseRSPHandler rspHandler = this.createResponseHandler();
        this.as.cget(this.model.cuid, this.priority, keys, null, rspHandler);
    }

    private DimseRSPHandler createResponseHandler() {
        return new DimseRSPHandler(this.as.nextMessageID()){

            @Override
            public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                super.onDimseRSP(as, cmd, data);
                DicomProgress progress = CGetForward.this.streamSCU.getState().getProgress();
                if (progress != null) {
                    if (CGetForward.this.streamSCU.getNumberOfSuboperations() == 0) {
                        CGetForward.this.streamSCU.setNumberOfSuboperations(ServiceUtil.getTotalOfSuboperations(cmd));
                    }
                    if (progress.isCancelled()) {
                        try {
                            this.cancel(as);
                        }
                        catch (IOException e) {
                            LOGGER.error("Cancel C-GET", (Throwable)e);
                        }
                    }
                }
            }
        };
    }

    public Connection getConnection() {
        return this.conn;
    }

    public DeviceOpService getStreamSCUService() {
        return this.streamSCUService;
    }

    public StoreFromStreamSCU getStreamSCU() {
        return this.streamSCU;
    }

    public DicomState getState() {
        return this.streamSCU.getState();
    }

    public static DicomState processStudy(DicomNode callingNode, DicomNode calledNode, DicomNode destinationNode, DicomProgress progress, String studyUID) {
        return CGetForward.process(null, null, callingNode, calledNode, destinationNode, progress, "STUDY", studyUID, null);
    }

    public static DicomState processStudy(AdvancedParams getParams, AdvancedParams forwardParams, DicomNode callingNode, DicomNode calledNode, DicomNode destinationNode, DicomProgress progress, String studyUID) {
        return CGetForward.process(getParams, forwardParams, callingNode, calledNode, destinationNode, progress, "STUDY", studyUID, null);
    }

    public static DicomState processStudy(AdvancedParams getParams, AdvancedParams forwardParams, DicomNode callingNode, DicomNode calledNode, DicomNode destinationNode, DicomProgress progress, String studyUID, CstoreParams cstoreParams) {
        return CGetForward.process(getParams, forwardParams, callingNode, calledNode, destinationNode, progress, "STUDY", studyUID, cstoreParams);
    }

    public static DicomState processSeries(DicomNode callingNode, DicomNode calledNode, DicomNode destinationNode, DicomProgress progress, String seriesUID) {
        return CGetForward.process(null, null, callingNode, calledNode, destinationNode, progress, "SERIES", seriesUID, null);
    }

    public static DicomState processSeries(AdvancedParams getParams, AdvancedParams forwardParams, DicomNode callingNode, DicomNode calledNode, DicomNode destinationNode, DicomProgress progress, String seriesUID) {
        return CGetForward.process(getParams, forwardParams, callingNode, calledNode, destinationNode, progress, "SERIES", seriesUID, null);
    }

    public static DicomState processSeries(AdvancedParams getParams, AdvancedParams forwardParams, DicomNode callingNode, DicomNode calledNode, DicomNode destinationNode, DicomProgress progress, String seriesUID, CstoreParams cstoreParams) {
        return CGetForward.process(getParams, forwardParams, callingNode, calledNode, destinationNode, progress, "SERIES", seriesUID, cstoreParams);
    }

    private static DicomState process(AdvancedParams getParams, AdvancedParams forwardParams, DicomNode callingNode, DicomNode calledNode, DicomNode destinationNode, DicomProgress progress, String queryRetrieveLevel, String queryUID, CstoreParams cstoreParams) {
        DicomState dicomState;
        CGetForward.validateProcessParameters(callingNode, calledNode, destinationNode);
        AdvancedParams options = Objects.requireNonNullElse(getParams, new AdvancedParams());
        CGetForward forward = new CGetForward(forwardParams, callingNode, destinationNode, progress, cstoreParams);
        try {
            CGetForward.setupForward(forward, options, callingNode, calledNode, queryRetrieveLevel, queryUID);
            forward.getStreamSCUService().start();
            forward.open();
            forward.retrieve();
            dicomState = forward.getState();
        }
        catch (Throwable throwable) {
            try {
                try {
                    forward.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (Exception e) {
                LOGGER.error("DICOM C-GET forward operation failed", (Throwable)e);
                return new DicomState(49152, "DICOM Get failed: " + e.getMessage(), null);
            }
        }
        forward.close();
        return dicomState;
    }

    private static void validateProcessParameters(DicomNode callingNode, DicomNode calledNode, DicomNode destinationNode) {
        if (callingNode == null || calledNode == null || destinationNode == null) {
            throw new IllegalArgumentException("callingNode, calledNode or destinationNode cannot be null!");
        }
    }

    private static void setupForward(CGetForward forward, AdvancedParams options, DicomNode callingNode, DicomNode calledNode, String queryRetrieveLevel, String queryUID) throws IOException {
        CGetForward.configureConnection(options, forward, callingNode, calledNode);
        CGetForward.configureInformationModel(forward, options);
        CGetForward.setQueryAttributes(forward, queryRetrieveLevel, queryUID);
    }

    private static void configureConnection(AdvancedParams options, CGetForward forward, DicomNode callingNode, DicomNode calledNode) throws IOException {
        Connection remote = forward.getRemoteConnection();
        Connection conn = forward.getConnection();
        options.configureConnect(forward.getAAssociateRQ(), remote, calledNode);
        options.configureBind(forward.getApplicationEntity(), conn, callingNode);
        options.configure(conn);
        options.configureTLS(conn, remote);
    }

    private static void configureInformationModel(CGetForward forward, AdvancedParams options) {
        forward.setInformationModel(CGetForward.getInformationModel(options), options.getTsuidOrder(), options.getQueryOptions().contains((Object)QueryOption.RELATIONAL));
        CGetForward.configureRelatedSOPClass(forward);
    }

    private static void setQueryAttributes(CGetForward forward, String queryRetrieveLevel, String queryUID) {
        forward.addLevel(queryRetrieveLevel);
        switch (queryRetrieveLevel) {
            case "STUDY": {
                forward.addKey(0x20000D, queryUID);
                break;
            }
            case "SERIES": {
                forward.addKey(0x20000E, queryUID);
                break;
            }
            case "IMAGE": {
                forward.addKey(524312, queryUID);
                break;
            }
            default: {
                throw new IllegalArgumentException("Unsupported query retrieve level: " + queryRetrieveLevel);
            }
        }
    }

    private static void configureRelatedSOPClass(CGetForward getSCU) {
        Properties p = new Properties();
        try {
            p.load(GetSCU.class.getResourceAsStream("store-tcs.properties"));
            for (Map.Entry<Object, Object> entry : p.entrySet()) {
                CGetForward.configureStorageSOPClass(getSCU, (String)entry.getKey(), (String)entry.getValue());
            }
        }
        catch (Exception e) {
            LOGGER.error("Read sop classes", (Throwable)e);
        }
    }

    private static void configureStorageSOPClass(CGetForward getSCU, String cuid, String tsuids) {
        String[] ts = StringUtils.split(tsuids, ';');
        for (int i = 0; i < ts.length; ++i) {
            ts[i] = CLIUtils.toUID(ts[i]);
        }
        getSCU.addOfferedStorageSOPClass(CLIUtils.toUID(cuid), ts);
    }

    private static InformationModel getInformationModel(AdvancedParams options) {
        Object model = options.getInformationModel();
        if (model instanceof InformationModel) {
            InformationModel informationModel = (InformationModel)((Object)model);
            return informationModel;
        }
        return InformationModel.StudyRoot;
    }

    public static enum InformationModel {
        PatientRoot("1.2.840.10008.5.1.4.1.2.1.3", "STUDY"),
        StudyRoot("1.2.840.10008.5.1.4.1.2.2.3", "STUDY"),
        PatientStudyOnly("1.2.840.10008.5.1.4.1.2.3.3", "STUDY"),
        CompositeInstanceRoot("1.2.840.10008.5.1.4.1.2.4.3", "IMAGE"),
        WithoutBulkData("1.2.840.10008.5.1.4.1.2.5.3", null),
        HangingProtocol("1.2.840.10008.5.1.4.38.4", null),
        ColorPalette("1.2.840.10008.5.1.4.39.4", null);

        final String cuid;
        final String level;

        private InformationModel(String cuid, String level) {
            this.cuid = cuid;
            this.level = level;
        }
    }
}

