/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.FileUtil
 */
package org.dcm4che3.tool.getscu;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.ExtendedNegotiation;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.pdu.RoleSelection;
import org.dcm4che3.net.service.BasicCStoreSCP;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.FileUtil;
import org.weasis.dicom.param.CancelListener;
import org.weasis.dicom.param.DicomProgress;
import org.weasis.dicom.param.DicomState;
import org.weasis.dicom.util.ServiceUtil;

public class GetSCU
implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetSCU.class);
    private static final int[] DEF_IN_FILTER = new int[]{524312, 0x20000D, 0x20000E};
    private static final String TMP_DIR = "tmp";
    private final Device device = new Device("getscu");
    private final ApplicationEntity ae;
    private final Connection conn = new Connection();
    private final Connection remote = new Connection();
    private final AAssociateRQ rq = new AAssociateRQ();
    private int priority;
    private InformationModel model;
    private Path storageDir;
    private final Attributes keys = new Attributes();
    private int[] inFilter = DEF_IN_FILTER;
    private Association as;
    private int cancelAfter;
    private final DicomState state;
    private DimseRSPHandler rspHandler;
    private long totalSize = 0L;
    private final AtomicBoolean cancelSent = new AtomicBoolean();
    private final CancelListener cancelListener = this::handleCancel;
    private final BasicCStoreSCP storageSCP = new BasicCStoreSCP(new String[]{"*"}){

        @Override
        protected void store(Association as, PresentationContext pc, Attributes rq, PDVInputStream data, Attributes rsp) throws IOException {
            if (GetSCU.this.storageDir == null) {
                return;
            }
            String iuid = rq.getString(4096);
            String cuid = rq.getString(2);
            String tsuid = pc.getTransferSyntax();
            Path tempPath = GetSCU.this.storageDir.resolve(GetSCU.TMP_DIR).resolve(iuid);
            Path finalPath = GetSCU.this.storageDir.resolve(iuid);
            try {
                Attributes fmi = as.createFileMetaInformation(iuid, cuid, tsuid);
                GetSCU.storeTo(as, fmi, data, tempPath);
                GetSCU.this.totalSize += Files.size(tempPath);
                GetSCU.renameTo(as, tempPath, finalPath);
                DicomProgress progress = GetSCU.this.state.getProgress();
                if (progress != null) {
                    progress.setProcessedFile(finalPath);
                }
            }
            catch (Exception e) {
                throw new DicomServiceException(272, (Throwable)e);
            }
            GetSCU.this.updateProgress(null);
        }
    };

    public GetSCU() {
        this(null);
    }

    public GetSCU(DicomProgress progress) {
        this.ae = new ApplicationEntity("GETSCU");
        this.device.addConnection(this.conn);
        this.device.addApplicationEntity(this.ae);
        this.ae.addConnection(this.conn);
        this.device.setDimseRQHandler(this.createServiceRegistry());
        this.state = new DicomState(progress);
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

    public static void storeTo(Association as, Attributes fmi, PDVInputStream data, Path path) throws IOException {
        LOGGER.debug("{}: M-WRITE {}", (Object)as, (Object)path);
        Files.createDirectories(path.getParent(), new FileAttribute[0]);
        try (DicomOutputStream out = new DicomOutputStream(path.toFile());){
            out.writeFileMetaInformation(fmi);
            data.copyTo(out);
        }
    }

    private static void renameTo(Association as, Path from, Path dest) throws IOException {
        LOGGER.info("{}: M-RENAME {} to {}", new Object[]{as, from, dest});
        FileUtil.prepareToWriteFile((Path)dest);
        Files.move(from, dest, StandardCopyOption.REPLACE_EXISTING);
    }

    private DicomServiceRegistry createServiceRegistry() {
        DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
        serviceRegistry.addDicomService(this.storageSCP);
        return serviceRegistry;
    }

    public void setStorageDirectory(Path storageDir) {
        if (storageDir != null) {
            try {
                Files.createDirectories(storageDir, new FileAttribute[0]);
                System.out.println("M-WRITE " + String.valueOf(storageDir));
            }
            catch (IOException e) {
                LOGGER.warn("Failed to create storage directory: {}", (Object)storageDir, (Object)e);
            }
        }
        this.storageDir = storageDir;
    }

    public final void setPriority(int priority) {
        this.priority = priority;
    }

    public void setCancelAfter(int cancelAfter) {
        this.cancelAfter = cancelAfter;
    }

    public final void setInformationModel(InformationModel model, String[] tss, boolean relational) {
        this.model = model;
        this.rq.addPresentationContext(new PresentationContext(1, model.cuid, tss));
        if (relational) {
            this.rq.addExtendedNegotiation(new ExtendedNegotiation(model.cuid, new byte[]{1}));
        }
        if (model.level != null) {
            this.addLevel(model.level);
        }
    }

    public void addLevel(String s) {
        this.keys.setString(524370, VR.CS, s);
    }

    public void addKey(int tag, String ... ss) {
        VR vr = ElementDictionary.vrOf(tag, this.keys.getPrivateCreator(tag));
        this.keys.setString(tag, vr, ss);
    }

    public final void setInputFilter(int[] inFilter) {
        this.inFilter = inFilter;
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
        DicomProgress progress = this.state.getProgress();
        if (progress != null) {
            progress.removeCancelListener(this.cancelListener);
        }
        if (this.as != null && this.as.isReadyForDataTransfer()) {
            this.as.waitForOutstandingRSP();
            this.as.release();
        }
    }

    public void retrieve(Path filePath) throws IOException, InterruptedException {
        Attributes attrs = new Attributes();
        try (DicomInputStream dis = new DicomInputStream(Files.newInputStream(filePath, new OpenOption[0]));){
            attrs.addSelected(dis.readDataset(), this.inFilter);
        }
        attrs.addAll(this.keys);
        this.retrieve(attrs);
    }

    public void retrieve() throws IOException, InterruptedException {
        this.retrieve(this.keys);
    }

    private void retrieve(Attributes keys) throws IOException, InterruptedException {
        this.retrieve(keys, this.createResponseHandler());
        this.scheduleCancellationIfNeeded();
    }

    private DimseRSPHandler createResponseHandler() {
        return new DimseRSPHandler(this.as.nextMessageID()){

            @Override
            public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                super.onDimseRSP(as, cmd, data);
                GetSCU.this.updateProgress(cmd);
            }
        };
    }

    private void scheduleCancellationIfNeeded() {
        if (this.cancelAfter > 0) {
            this.device.schedule(this::cancelRetrieve, this.cancelAfter, TimeUnit.MILLISECONDS);
        }
    }

    private void handleCancel() {
        DicomProgress progress = this.state.getProgress();
        if (progress != null && progress.isAborted()) {
            this.abortRetrieve();
        } else {
            this.cancelRetrieve();
        }
    }

    public void cancelRetrieve() {
        if (this.rspHandler == null || this.as == null || !this.as.isReadyForDataTransfer()) {
            return;
        }
        if (this.cancelSent.compareAndSet(false, true)) {
            try {
                this.rspHandler.cancel(this.as);
            }
            catch (IOException e) {
                LOGGER.error("Cancel C-GET", (Throwable)e);
            }
        }
    }

    public void abortRetrieve() {
        if (this.as != null && this.as.isReadyForDataTransfer()) {
            LOGGER.info("Aborting the C-GET association of {}", (Object)this.rq.getCalledAET());
            this.as.abort();
        }
    }

    public void retrieve(DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        this.retrieve(this.keys, rspHandler);
    }

    private void retrieve(Attributes keys, DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        this.rspHandler = rspHandler;
        DicomProgress progress = this.state.getProgress();
        if (progress != null) {
            progress.addCancelListener(this.cancelListener);
            if (progress.isCancelled()) {
                return;
            }
        }
        this.as.cget(this.model.getCuid(), this.priority, keys, null, rspHandler);
    }

    public Connection getConnection() {
        return this.conn;
    }

    public DicomState getState() {
        return this.state;
    }

    public long getTotalSize() {
        return this.totalSize;
    }

    public void stop() {
        try {
            this.close();
        }
        catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.error("Stop GetSCU", (Throwable)e);
        }
        ServiceUtil.shutdownService((ExecutorService)this.device.getExecutor());
        ServiceUtil.shutdownService(this.device.getScheduledExecutor());
    }

    private void updateProgress(Attributes cmd) {
        DicomProgress progress = this.state.getProgress();
        if (progress != null) {
            progress.setAttributes(cmd);
            if (progress.isCancelled()) {
                this.handleCancel();
            }
        }
    }

    public static enum InformationModel {
        PatientRoot("1.2.840.10008.5.1.4.1.2.1.3", "STUDY"),
        StudyRoot("1.2.840.10008.5.1.4.1.2.2.3", "STUDY"),
        PatientStudyOnly("1.2.840.10008.5.1.4.1.2.3.3", "STUDY"),
        CompositeInstanceRoot("1.2.840.10008.5.1.4.1.2.4.3", "IMAGE"),
        WithoutBulkData("1.2.840.10008.5.1.4.1.2.5.3", null),
        HangingProtocol("1.2.840.10008.5.1.4.38.4", null),
        ColorPalette("1.2.840.10008.5.1.4.39.4", null);

        private final String cuid;
        final String level;

        private InformationModel(String cuid, String level) {
            this.cuid = cuid;
            this.level = level;
        }

        public String getCuid() {
            return this.cuid;
        }
    }
}

