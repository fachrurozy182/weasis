/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.dicom.util;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.img.DicomOutputData;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.DataWriter;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.tool.storescu.RelatedGeneralSOPClasses;
import org.dcm4che3.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.dicom.param.AdvancedParams;
import org.weasis.dicom.param.DeviceOpService;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.DicomProgress;
import org.weasis.dicom.param.DicomState;
import org.weasis.dicom.util.ServiceUtil;

public class StoreFromStreamSCU {
    private static final Logger LOGGER = LoggerFactory.getLogger(StoreFromStreamSCU.class);
    private static final int MAX_STATUS_LOG_ENTRIES = 3;
    private static final int CLOSE_DELAY_SECONDS = 15;
    private static final int WAIT_SLEEP_MS = 20;
    private static final int MAX_WAIT_LOOPS = 3000;
    private static final long OUTSTANDING_RSP_TIMEOUT_MS = 10000L;
    private final Map<String, Integer> instanceUidsCurrentlyProcessed = new ConcurrentHashMap<String, Integer>();
    private final ApplicationEntity ae;
    private final Connection remote;
    private final AAssociateRQ rq = new AAssociateRQ();
    private final RelatedGeneralSOPClasses relSOPClasses = new RelatedGeneralSOPClasses();
    private final Device device;
    private final Connection conn;
    private final DicomState state;
    private final AdvancedParams options;
    private final AtomicBoolean countdown = new AtomicBoolean(false);
    private final ScheduledExecutorService closeAssociationExecutor = Executors.newSingleThreadScheduledExecutor();
    private final RSPHandlerFactory rspHandlerFactory = this::createResponseHandler;
    private Attributes attrs;
    private boolean relExtNeg;
    private Association as;
    private int lastStatusCode = Integer.MIN_VALUE;
    private int nbStatusLog = 0;
    private int numberOfSuboperations = 0;
    private ScheduledFuture<?> scheduledFuture;
    private final TimerTask closeAssociationTask = new TimerTask(){

        @Override
        public void run() {
            StoreFromStreamSCU.this.close(false);
        }
    };

    public StoreFromStreamSCU(DicomNode callingNode, DicomNode calledNode) throws IOException {
        this(null, callingNode, calledNode, null);
    }

    public StoreFromStreamSCU(AdvancedParams params, DicomNode callingNode, DicomNode calledNode) throws IOException {
        this(params, callingNode, calledNode, null);
    }

    public StoreFromStreamSCU(AdvancedParams params, DicomNode callingNode, DicomNode calledNode, DicomProgress progress) throws IOException {
        Objects.requireNonNull(callingNode, "callingNode cannot be null");
        Objects.requireNonNull(calledNode, "calledNode cannot be null");
        this.options = Objects.requireNonNullElseGet(params, AdvancedParams::new);
        this.state = new DicomState(progress);
        this.device = new Device("storescu");
        this.conn = new Connection();
        this.device.addConnection(this.conn);
        this.ae = new ApplicationEntity(callingNode.getAet());
        this.device.addApplicationEntity(this.ae);
        this.ae.addConnection(this.conn);
        this.remote = new Connection();
        this.configureAssociation(callingNode, calledNode);
        this.setAttributes(new Attributes());
    }

    private void configureAssociation(DicomNode callingNode, DicomNode calledNode) throws IOException {
        this.rq.addPresentationContext(new PresentationContext(1, "1.2.840.10008.1.1", "1.2.840.10008.1.2"));
        this.options.configureConnect(this.rq, this.remote, calledNode);
        this.options.configureBind(this.ae, this.conn, callingNode);
        this.options.configure(this.conn);
        this.options.configureTLS(this.conn, this.remote);
    }

    private DimseRSPHandler createResponseHandler() {
        return new DimseRSPHandler(this.as.nextMessageID()){

            @Override
            public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                super.onDimseRSP(as, cmd, data);
                StoreFromStreamSCU.this.handleCStoreResponse(cmd);
                StoreFromStreamSCU.this.updateProgress(cmd);
            }
        };
    }

    private void handleCStoreResponse(Attributes cmd) {
        int status = cmd.getInt(2304, -1);
        this.state.setStatus(status);
        ServiceUtil.ProgressStatus progressStatus = this.determineProgressStatus(status);
        this.logStatusIfNeeded(status, cmd);
        ServiceUtil.notifyProgression(this.state.getProgress(), cmd, progressStatus, this.numberOfSuboperations);
    }

    private ServiceUtil.ProgressStatus determineProgressStatus(int status) {
        return switch (status) {
            case 0 -> ServiceUtil.ProgressStatus.COMPLETED;
            case 45056, 45062, 45063 -> ServiceUtil.ProgressStatus.WARNING;
            default -> ServiceUtil.ProgressStatus.FAILED;
        };
    }

    private void logStatusIfNeeded(int status, Attributes cmd) {
        if (this.lastStatusCode == status || this.nbStatusLog >= 3) {
            return;
        }
        ++this.nbStatusLog;
        this.lastStatusCode = status;
        String hexStatus = TagUtils.shortToHexString(status);
        if (this.isWarningStatus(status)) {
            this.logWarningStatus(hexStatus, cmd);
        } else {
            this.logErrorStatus(hexStatus, cmd);
        }
    }

    private boolean isWarningStatus(int status) {
        return status == 45056 || status == 45062 || status == 45063;
    }

    private void logWarningStatus(String hexStatus, Attributes cmd) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.warn("Received C-STORE-RSP with Status {}H{}", (Object)hexStatus, (Object)("\r\n" + String.valueOf(cmd)));
        } else {
            LOGGER.warn("Received C-STORE-RSP with Status {}H", (Object)hexStatus);
        }
    }

    private void logErrorStatus(String hexStatus, Attributes cmd) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.error("Received C-STORE-RSP with Status {}H{}", (Object)hexStatus, (Object)("\r\n" + String.valueOf(cmd)));
        } else {
            LOGGER.error("Received C-STORE-RSP with Status {}H", (Object)hexStatus);
        }
    }

    private void updateProgress(Attributes cmd) {
        DicomProgress progress = this.state.getProgress();
        if (progress != null) {
            progress.setAttributes(cmd);
        }
    }

    public void cstore(String cuid, String iuid, int priority, DataWriter dataWriter, String tsuid) throws IOException, InterruptedException {
        if (this.as == null) {
            throw new IllegalStateException("Association is null!");
        }
        this.as.cstore(cuid, iuid, priority, dataWriter, tsuid, this.rspHandlerFactory.createDimseRSPHandler());
    }

    public DicomNode getCallingNode() {
        return new DicomNode(this.ae.getAETitle(), this.conn.getHostname(), this.conn.getPort());
    }

    public DicomNode getCalledNode() {
        return new DicomNode(this.rq.getCalledAET(), this.remote.getHostname(), this.remote.getPort());
    }

    public DicomNode getLocalDicomNode() {
        return this.as != null ? DicomNode.buildLocalDicomNode(this.as) : null;
    }

    public DicomNode getRemoteDicomNode() {
        return this.as != null ? DicomNode.buildRemoteDicomNode(this.as) : null;
    }

    public String selectTransferSyntax(String cuid, String tsuid) {
        return StoreFromStreamSCU.selectTransferSyntax(this.as, cuid, tsuid);
    }

    public Device getDevice() {
        return this.device;
    }

    public AAssociateRQ getAAssociateRQ() {
        return this.rq;
    }

    public Connection getRemoteConnection() {
        return this.remote;
    }

    public Attributes getAttributes() {
        return this.attrs;
    }

    public void setAttributes(Attributes attrs) {
        this.attrs = attrs;
    }

    public void enableSOPClassRelationshipExtNeg(boolean enable) {
        this.relExtNeg = enable;
    }

    public AdvancedParams getOptions() {
        return this.options;
    }

    public boolean hasAssociation() {
        return this.as != null;
    }

    public boolean isReadyForDataTransfer() {
        return this.as != null && this.as.isReadyForDataTransfer();
    }

    public Set<String> getTransferSyntaxesFor(String cuid) {
        return this.as != null ? this.as.getTransferSyntaxesFor(cuid) : Collections.emptySet();
    }

    public int getNumberOfSuboperations() {
        return this.numberOfSuboperations;
    }

    public void setNumberOfSuboperations(int numberOfSuboperations) {
        this.numberOfSuboperations = numberOfSuboperations;
    }

    public DicomState getState() {
        return this.state;
    }

    public RSPHandlerFactory getRspHandlerFactory() {
        return this.rspHandlerFactory;
    }

    public synchronized void open() throws IOException {
        this.countdown.set(false);
        try {
            this.as = this.ae.connect(this.remote, this.rq);
        }
        catch (Exception e) {
            this.handleConnectionException(e);
        }
        if (this.as == null) {
            throw new IOException("Cannot connect to the remote destination");
        }
    }

    private void handleConnectionException(Exception e) {
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        this.as = null;
        LOGGER.trace("Connecting to remote destination", (Throwable)e);
    }

    public synchronized void close(boolean force) {
        if (force) {
            this.closeAssociation();
            return;
        }
        if (!this.countdown.compareAndSet(true, false)) {
            return;
        }
        if (!this.instanceUidsCurrentlyProcessed.isEmpty()) {
            LOGGER.debug("Idle close deferred: {} C-STORE transfer(s) still in flight", (Object)this.instanceUidsCurrentlyProcessed.size());
            this.countdown.set(true);
            this.scheduledFuture = this.closeAssociationExecutor.schedule(this.closeAssociationTask, 15L, TimeUnit.SECONDS);
            return;
        }
        this.closeAssociation();
    }

    private void closeAssociation() {
        this.cancelScheduledClose();
        if (this.as != null) {
            try {
                LOGGER.info("Closing DICOM association");
                if (this.as.isReadyForDataTransfer()) {
                    this.drainOutstandingResponses(this.as);
                    this.as.release();
                }
                this.as.waitForSocketClose();
            }
            catch (Exception e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                LOGGER.trace("Cannot close association", (Throwable)e);
            }
            this.as = null;
        }
    }

    private void drainOutstandingResponses(Association association) {
        Thread waiter = new Thread(() -> {
            try {
                association.waitForOutstandingRSP();
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "storescu-drain-rsp");
        waiter.setDaemon(true);
        waiter.start();
        try {
            waiter.join(10000L);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (waiter.isAlive()) {
            LOGGER.warn("Timeout after {} ms waiting for outstanding C-STORE responses before closing association", (Object)10000L);
        }
    }

    public boolean addData(String cuid, String tsuid) {
        this.countdown.set(false);
        if (cuid == null || tsuid == null) {
            return false;
        }
        if (this.rq.containsPresentationContextFor(cuid, tsuid)) {
            return true;
        }
        this.addPresentationContextsForSopClass(cuid, tsuid);
        return true;
    }

    private void addPresentationContextsForSopClass(String cuid, String tsuid) {
        if (!this.rq.containsPresentationContextFor(cuid)) {
            this.addRelatedNegotiationIfEnabled(cuid);
            this.addStandardTransferSyntaxes(cuid, tsuid);
        }
        this.addPresentationContext(cuid, tsuid);
    }

    private void addRelatedNegotiationIfEnabled(String cuid) {
        if (this.relExtNeg) {
            this.rq.addCommonExtendedNegotiation(this.relSOPClasses.getCommonExtendedNegotiation(cuid));
        }
    }

    private void addStandardTransferSyntaxes(String cuid, String tsuid) {
        if (!"1.2.840.10008.1.2.1".equals(tsuid)) {
            this.addPresentationContext(cuid, "1.2.840.10008.1.2.1");
        }
        if (!"1.2.840.10008.1.2".equals(tsuid)) {
            this.addPresentationContext(cuid, "1.2.840.10008.1.2");
        }
    }

    private void addPresentationContext(String cuid, String tsuid) {
        this.rq.addPresentationContext(new PresentationContext(this.rq.getNumberOfPresentationContexts() * 2 + 1, cuid, tsuid));
    }

    public synchronized void triggerCloseExecutor() {
        this.cancelScheduledClose();
        this.countdown.set(true);
        this.scheduledFuture = this.closeAssociationExecutor.schedule(this.closeAssociationTask, 15L, TimeUnit.SECONDS);
    }

    private void cancelScheduledClose() {
        if (this.scheduledFuture != null) {
            this.scheduledFuture.cancel(false);
            this.scheduledFuture = null;
        }
    }

    public static String selectTransferSyntax(Association as, String cuid, String filets) {
        Set<String> tss = as.getTransferSyntaxesFor(cuid);
        if (tss.contains(filets)) {
            return filets;
        }
        if (tss.contains("1.2.840.10008.1.2.1")) {
            return "1.2.840.10008.1.2.1";
        }
        return "1.2.840.10008.1.2";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void prepareTransfer(DeviceOpService service, String iuid, String cuid, String dstTsuid) throws IOException {
        StoreFromStreamSCU storeFromStreamSCU = this;
        synchronized (storeFromStreamSCU) {
            this.cancelScheduledClose();
            this.countdown.set(false);
            if (this.hasAssociation()) {
                this.handleExistingAssociation(cuid, dstTsuid);
            } else {
                this.handleNewAssociation(service, cuid, dstTsuid);
            }
            this.addIUIDProcessed(iuid);
        }
    }

    private void handleExistingAssociation(String cuid, String dstTsuid) throws IOException {
        this.checkNewSopClassUID(cuid, dstTsuid);
        this.addPresentationContexts(cuid, dstTsuid);
        if (!this.isReadyForDataTransfer()) {
            LOGGER.debug("prepareTransfer: as not ready for data transfer, reopen");
            this.open();
        }
    }

    private void handleNewAssociation(DeviceOpService service, String cuid, String dstTsuid) throws IOException {
        service.start();
        this.addPresentationContexts(cuid, dstTsuid);
        if (!"1.2.840.10008.1.2.1".equals(dstTsuid)) {
            this.addData(cuid, "1.2.840.10008.1.2.1");
        }
        LOGGER.debug("prepareTransfer: connecting to the remote destination");
        this.open();
    }

    private void addPresentationContexts(String cuid, String dstTsuid) {
        this.addData(cuid, dstTsuid);
        if (DicomOutputData.isAdaptableSyntax(dstTsuid)) {
            this.addData(cuid, "1.2.840.10008.1.2.4.70");
        }
    }

    private void checkNewSopClassUID(String cuid, String dstTsuid) {
        Set<String> tss = this.getTransferSyntaxesFor(cuid);
        if (!tss.contains(dstTsuid)) {
            this.handleNewTransferSyntax(cuid, dstTsuid);
        }
    }

    private void handleNewTransferSyntax(String cuid, String dstTsuid) {
        LOGGER.debug("prepareTransfer: New output transfer syntax {}: closing streamSCU", (Object)dstTsuid);
        this.countdown.set(false);
        this.waitForPendingTransfers();
        LOGGER.info("prepareTransfer: Close association to handle dynamically new SOPClassUID: {}", (Object)cuid);
        this.close(true);
    }

    private void waitForPendingTransfers() {
        int loop;
        for (loop = 0; !this.instanceUidsCurrentlyProcessed.isEmpty() && loop < 3000; ++loop) {
            try {
                LOGGER.debug("prepareTransfer: StreamSCU has some IUID to process: waiting {} ms", (Object)20);
                TimeUnit.MILLISECONDS.sleep(20L);
                continue;
            }
            catch (InterruptedException e) {
                LOGGER.error("prepareTransfer: InterruptedException {}", (Object)e.getMessage());
                Thread.currentThread().interrupt();
                break;
            }
        }
        if (loop >= 3000) {
            LOGGER.warn("prepareTransfer: StreamSCU timeout reached");
            this.instanceUidsCurrentlyProcessed.clear();
        } else {
            LOGGER.debug("prepareTransfer: StreamSCU has no more IUID to process: stop waiting");
        }
    }

    public void removeIUIDProcessed(String iuid) {
        this.instanceUidsCurrentlyProcessed.compute(iuid, (k, v) -> v == null || v <= 1 ? null : Integer.valueOf(v - 1));
    }

    private void addIUIDProcessed(String iuid) {
        this.instanceUidsCurrentlyProcessed.merge(iuid, 1, Integer::sum);
    }

    @FunctionalInterface
    public static interface RSPHandlerFactory {
        public DimseRSPHandler createDimseRSPHandler();
    }
}

