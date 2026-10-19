/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.dcm4che3.tool.movescu;

import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.ExtendedNegotiation;
import org.dcm4che3.net.pdu.PresentationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.dicom.param.CancelListener;
import org.weasis.dicom.param.DicomProgress;
import org.weasis.dicom.param.DicomState;

public class MoveSCU
extends Device
implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MoveSCU.class);
    private static final int[] DEFAULT_INPUT_FILTER = new int[]{524312, 0x20000D, 0x20000E};
    private final ApplicationEntity ae = new ApplicationEntity("MOVESCU");
    private final Connection conn = new Connection();
    private final Connection remote = new Connection();
    private final AAssociateRQ rq = new AAssociateRQ();
    private final Attributes keys = new Attributes();
    private final DicomState state;
    private int priority;
    private String destination;
    private InformationModel model;
    private int[] inFilter = DEFAULT_INPUT_FILTER;
    private Association as;
    private int cancelAfter;
    private boolean releaseEager;
    private DimseRSPHandler rspHandler;
    private final AtomicBoolean cancelSent = new AtomicBoolean();
    private final CancelListener cancelListener = this::handleCancel;

    public MoveSCU() {
        this((DicomProgress)null);
    }

    public MoveSCU(DicomProgress progress) {
        super("movescu");
        this.addConnection(this.conn);
        this.addApplicationEntity(this.ae);
        this.ae.addConnection(this.conn);
        this.state = new DicomState(progress);
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setCancelAfter(int cancelAfter) {
        this.cancelAfter = cancelAfter;
    }

    public void setReleaseEager(boolean releaseEager) {
        this.releaseEager = releaseEager;
    }

    public void setInformationModel(InformationModel model, String[] transferSyntaxes, boolean relational) {
        this.model = model;
        this.rq.addPresentationContext(new PresentationContext(1, model.cuid, transferSyntaxes));
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

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void addKey(int tag, String ... values) {
        VR vr = ElementDictionary.vrOf(tag, this.keys.getPrivateCreator(tag));
        this.keys.setString(tag, vr, values);
    }

    public void setInputFilter(int[] inFilter) {
        this.inFilter = inFilter != null ? (int[])inFilter.clone() : DEFAULT_INPUT_FILTER;
    }

    public ApplicationEntity getApplicationEntity() {
        return this.ae;
    }

    public Connection getRemoteConnection() {
        return this.remote;
    }

    public Connection getConnection() {
        return this.conn;
    }

    public AAssociateRQ getAAssociateRQ() {
        return this.rq;
    }

    public Association getAssociation() {
        return this.as;
    }

    public Attributes getKeys() {
        return this.keys;
    }

    public DicomState getState() {
        return this.state;
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

    public void retrieve(Path dicomFile) throws IOException, InterruptedException {
        Attributes attrs = this.readDicomAttributes(dicomFile);
        attrs.addAll(this.keys);
        this.performMove(attrs);
    }

    public void retrieve() throws IOException, InterruptedException {
        this.performMove(this.keys);
    }

    private Attributes readDicomAttributes(Path dicomFile) throws IOException {
        Attributes attrs = new Attributes();
        try (DicomInputStream dis = new DicomInputStream(dicomFile.toFile());){
            attrs.addSelected(dis.readDataset(), this.inFilter);
        }
        return attrs;
    }

    private void performMove(Attributes queryKeys) throws IOException, InterruptedException {
        this.rspHandler = this.createResponseHandler();
        DicomProgress progress = this.state.getProgress();
        if (progress != null) {
            progress.addCancelListener(this.cancelListener);
            if (progress.isCancelled()) {
                return;
            }
        }
        this.as.cmove(this.model.cuid, this.priority, queryKeys, null, this.destination, this.rspHandler);
        if (this.cancelAfter > 0) {
            this.scheduleCancellation();
        }
    }

    private DimseRSPHandler createResponseHandler() {
        return new DimseRSPHandler(this.as.nextMessageID()){

            @Override
            public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                super.onDimseRSP(as, cmd, data);
                MoveSCU.this.handleProgressUpdate(cmd);
            }
        };
    }

    private void handleProgressUpdate(Attributes cmd) {
        DicomProgress progress = this.state.getProgress();
        if (progress != null) {
            progress.setAttributes(cmd);
            if (progress.isCancelled()) {
                this.handleCancel();
            }
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

    public void abortRetrieve() {
        if (this.as != null && this.as.isReadyForDataTransfer()) {
            LOGGER.info("Aborting the C-MOVE association of {}", (Object)this.rq.getCalledAET());
            this.as.abort();
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
                LOGGER.error("Cancel C-MOVE", (Throwable)e);
            }
        }
    }

    private void scheduleCancellation() {
        this.schedule(() -> {
            this.cancelRetrieve();
            if (this.releaseEager) {
                try {
                    this.as.release();
                }
                catch (IOException e) {
                    LOGGER.error("Release association after cancelling C-MOVE", (Throwable)e);
                }
            }
        }, this.cancelAfter, TimeUnit.MILLISECONDS);
    }

    public static enum InformationModel {
        PatientRoot("1.2.840.10008.5.1.4.1.2.1.2", "STUDY"),
        StudyRoot("1.2.840.10008.5.1.4.1.2.2.2", "STUDY"),
        PatientStudyOnly("1.2.840.10008.5.1.4.1.2.3.2", "STUDY"),
        CompositeInstanceRoot("1.2.840.10008.5.1.4.1.2.4.2", "IMAGE"),
        HangingProtocol("1.2.840.10008.5.1.4.38.3", null),
        ColorPalette("1.2.840.10008.5.1.4.39.3", null);

        private final String cuid;
        private final String level;

        private InformationModel(String cuid, String level) {
            this.cuid = cuid;
            this.level = level;
        }

        public String getCuid() {
            return this.cuid;
        }
    }
}

