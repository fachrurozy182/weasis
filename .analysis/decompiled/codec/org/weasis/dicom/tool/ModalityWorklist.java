/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StreamUtil
 */
package org.weasis.dicom.tool;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Objects;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.net.Connection;
import org.dcm4che3.tool.findscu.FindSCU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.StreamUtil;
import org.weasis.dicom.op.CFind;
import org.weasis.dicom.param.AdvancedParams;
import org.weasis.dicom.param.DeviceOpService;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.DicomParam;
import org.weasis.dicom.param.DicomState;
import org.weasis.dicom.util.ServiceUtil;

public final class ModalityWorklist {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModalityWorklist.class);
    public static final DicomParam PatientWeight = new DicomParam(0x101030, new String[0]);
    public static final DicomParam MedicalAlerts = new DicomParam(0x102000, new String[0]);
    public static final DicomParam Allergies = new DicomParam(0x102110, new String[0]);
    public static final DicomParam PregnancyStatus = new DicomParam(1057216, new String[0]);
    public static final DicomParam RequestingPhysician = new DicomParam(3280946, new String[0]);
    public static final DicomParam RequestingService = new DicomParam(3280947, new String[0]);
    public static final DicomParam RequestedProcedureDescription = new DicomParam(3280992, new String[0]);
    public static final DicomParam RequestedProcedureCodeSequence = new DicomParam(3280996, new String[0]);
    public static final DicomParam AdmissionID = new DicomParam(3670032, new String[0]);
    public static final DicomParam IssuerOfAdmissionIDSequence = new DicomParam(3670036, new String[0]);
    public static final DicomParam SpecialNeeds = new DicomParam(3670096, new String[0]);
    public static final DicomParam CurrentPatientLocation = new DicomParam(0x380300, new String[0]);
    public static final DicomParam PatientState = new DicomParam(3671296, new String[0]);
    public static final DicomParam RequestedProcedureID = new DicomParam(0x401001, new String[0]);
    public static final DicomParam RequestedProcedurePriority = new DicomParam(4198403, new String[0]);
    public static final DicomParam PatientTransportArrangements = new DicomParam(0x401004, new String[0]);
    public static final DicomParam PlacerOrderNumberImagingServiceRequest = new DicomParam(4202518, new String[0]);
    public static final DicomParam FillerOrderNumberImagingServiceRequest = new DicomParam(4202519, new String[0]);
    public static final DicomParam ConfidentialityConstraintOnPatientDataDescription = new DicomParam(4206593, new String[0]);
    private static final int[] SPS_PATH = new int[]{0x400100};
    public static final DicomParam Modality = new DicomParam(SPS_PATH, 524384, new String[0]);
    public static final DicomParam RequestedContrastAgent = new DicomParam(SPS_PATH, 3281008, new String[0]);
    public static final DicomParam ScheduledStationAETitle = new DicomParam(SPS_PATH, 0x400001, new String[0]);
    public static final DicomParam ScheduledProcedureStepStartDate = new DicomParam(SPS_PATH, 0x400002, new String[0]);
    public static final DicomParam ScheduledProcedureStepStartTime = new DicomParam(SPS_PATH, 0x400003, new String[0]);
    public static final DicomParam ScheduledPerformingPhysicianName = new DicomParam(SPS_PATH, 0x400006, new String[0]);
    public static final DicomParam ScheduledProcedureStepDescription = new DicomParam(SPS_PATH, 0x400007, new String[0]);
    public static final DicomParam ScheduledProtocolCodeSequence = new DicomParam(SPS_PATH, 0x400008, new String[0]);
    public static final DicomParam ScheduledProcedureStepID = new DicomParam(SPS_PATH, 0x400009, new String[0]);
    public static final DicomParam ScheduledStationName = new DicomParam(SPS_PATH, 0x400010, new String[0]);
    public static final DicomParam ScheduledProcedureStepLocation = new DicomParam(SPS_PATH, 0x400011, new String[0]);
    public static final DicomParam PreMedication = new DicomParam(SPS_PATH, 4194322, new String[0]);
    public static final DicomParam ScheduledProcedureStepStatus = new DicomParam(SPS_PATH, 0x400020, new String[0]);
    private static final int[] SPC_PATH = new int[]{0x400100, 0x400008};
    public static final DicomParam ScheduledProtocolCodeMeaning = new DicomParam(SPC_PATH, 524548, new String[0]);

    private ModalityWorklist() {
    }

    public static DicomState process(DicomNode callingNode, DicomNode calledNode, DicomParam ... keys) {
        return ModalityWorklist.process(null, callingNode, calledNode, 0, keys);
    }

    public static DicomState process(AdvancedParams params, DicomNode callingNode, DicomNode calledNode, DicomParam ... keys) {
        return ModalityWorklist.process(params, callingNode, calledNode, 0, keys);
    }

    public static DicomState process(AdvancedParams params, DicomNode callingNode, DicomNode calledNode, int cancelAfter, DicomParam ... keys) {
        DicomState dicomState;
        Objects.requireNonNull(callingNode, "callingNode cannot be null");
        Objects.requireNonNull(calledNode, "calledNode cannot be null");
        AdvancedParams options = Objects.requireNonNullElse(params, new AdvancedParams());
        FindSCU findSCU = new FindSCU();
        try {
            ModalityWorklist.configureFindSCU(findSCU, options, callingNode, calledNode, cancelAfter, keys);
            dicomState = ModalityWorklist.executeQuery(findSCU);
        }
        catch (Throwable throwable) {
            try {
                try {
                    findSCU.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (Exception e) {
                return ModalityWorklist.handleException(e, "DICOM Find failed");
            }
        }
        findSCU.close();
        return dicomState;
    }

    private static void configureFindSCU(FindSCU findSCU, AdvancedParams options, DicomNode callingNode, DicomNode calledNode, int cancelAfter, DicomParam[] keys) throws IOException {
        Connection remote = findSCU.getRemoteConnection();
        Connection conn = findSCU.getConnection();
        options.configureConnect(findSCU.getAAssociateRQ(), remote, calledNode);
        options.configureBind(findSCU.getApplicationEntity(), conn, callingNode);
        options.configure(conn);
        options.configureTLS(conn, remote);
        findSCU.setInformationModel(ModalityWorklist.getInformationModel(options), options.getTsuidOrder(), options.getQueryOptions());
        ModalityWorklist.addKeys(findSCU, keys);
        findSCU.setCancelAfter(cancelAfter);
        findSCU.setPriority(options.getPriority());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static DicomState executeQuery(FindSCU findSCU) {
        DeviceOpService service = new DeviceOpService(findSCU.getDevice());
        service.start();
        try {
            DicomState dcmState = findSCU.getState();
            QueryTimings timings = ModalityWorklist.performQuery(findSCU);
            ServiceUtil.forceGettingAttributes(dcmState, findSCU);
            String timeMsg = ModalityWorklist.formatTimingMessage(findSCU, timings);
            DicomState dicomState = DicomState.buildMessage(dcmState, timeMsg, null);
            return dicomState;
        }
        catch (Exception e) {
            DicomState dicomState = ModalityWorklist.handleQueryException(findSCU, e);
            return dicomState;
        }
        finally {
            StreamUtil.safeClose((AutoCloseable)findSCU);
            service.stop();
        }
    }

    private static QueryTimings performQuery(FindSCU findSCU) throws Exception {
        long startTime = System.currentTimeMillis();
        findSCU.open();
        long connectTime = System.currentTimeMillis();
        findSCU.query();
        long queryTime = System.currentTimeMillis();
        return new QueryTimings(startTime, connectTime, queryTime);
    }

    private static String formatTimingMessage(FindSCU findSCU, QueryTimings timings) {
        return MessageFormat.format("DICOM C-Find connected in {2}ms from {0} to {1}. Query in {3}ms.", findSCU.getAAssociateRQ().getCallingAET(), findSCU.getAAssociateRQ().getCalledAET(), timings.connectionTime(), timings.queryTime());
    }

    private static DicomState handleQueryException(FindSCU findSCU, Exception e) {
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        LOGGER.error("DICOM C-Find query failed", (Throwable)e);
        ServiceUtil.forceGettingAttributes(findSCU.getState(), findSCU);
        return DicomState.buildMessage(findSCU.getState(), null, e);
    }

    private static DicomState handleException(Exception e, String message) {
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        LOGGER.error("DICOM operation failed", (Throwable)e);
        return new DicomState(49152, message + ": " + e.getMessage(), null);
    }

    private static void addKeys(FindSCU findSCU, DicomParam[] keys) {
        for (DicomParam param : keys) {
            int[] parentSeq = param.getParentSeqTags();
            if (parentSeq == null || parentSeq.length == 0) {
                CFind.addAttributes(findSCU.getKeys(), param);
                continue;
            }
            Attributes parent = ModalityWorklist.navigateToSequenceLevel(findSCU.getKeys(), parentSeq);
            CFind.addAttributes(parent, param);
        }
    }

    private static Attributes navigateToSequenceLevel(Attributes root, int[] sequencePath) {
        Attributes current = root;
        for (int tag : sequencePath) {
            Sequence sequence = current.getSequence(tag);
            if (sequence == null || sequence.isEmpty()) {
                sequence = current.newSequence(tag, 1);
                sequence.add(new Attributes());
            }
            current = (Attributes)sequence.get(0);
        }
        return current;
    }

    private static FindSCU.InformationModel getInformationModel(AdvancedParams options) {
        FindSCU.InformationModel im;
        Object model = options.getInformationModel();
        return model instanceof FindSCU.InformationModel ? (im = (FindSCU.InformationModel)((Object)model)) : FindSCU.InformationModel.MWL;
    }

    private record QueryTimings(long start, long connected, long completed) {
        long connectionTime() {
            return this.connected - this.start;
        }

        long queryTime() {
            return this.completed - this.connected;
        }
    }
}

