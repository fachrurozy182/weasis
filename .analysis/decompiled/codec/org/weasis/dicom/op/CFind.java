/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.dicom.op;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Objects;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.service.QueryRetrieveLevel;
import org.dcm4che3.tool.findscu.FindSCU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.dicom.param.AdvancedParams;
import org.weasis.dicom.param.DeviceOpService;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.DicomParam;
import org.weasis.dicom.param.DicomState;
import org.weasis.dicom.util.ServiceUtil;

public final class CFind {
    private static final Logger LOGGER = LoggerFactory.getLogger(CFind.class);
    public static final DicomParam PatientID = new DicomParam(0x100020, new String[0]);
    public static final DicomParam IssuerOfPatientID = new DicomParam(0x100021, new String[0]);
    public static final DicomParam PatientName = new DicomParam(0x100010, new String[0]);
    public static final DicomParam PatientBirthDate = new DicomParam(0x100030, new String[0]);
    public static final DicomParam PatientSex = new DicomParam(0x100040, new String[0]);
    public static final DicomParam StudyInstanceUID = new DicomParam(0x20000D, new String[0]);
    public static final DicomParam AccessionNumber = new DicomParam(524368, new String[0]);
    public static final DicomParam IssuerOfAccessionNumberSequence = new DicomParam(524369, new String[0]);
    public static final DicomParam StudyID = new DicomParam(0x200010, new String[0]);
    public static final DicomParam ReferringPhysicianName = new DicomParam(524432, new String[0]);
    public static final DicomParam StudyDescription = new DicomParam(528432, new String[0]);
    public static final DicomParam StudyDate = new DicomParam(524320, new String[0]);
    public static final DicomParam StudyTime = new DicomParam(524336, new String[0]);
    public static final DicomParam SeriesInstanceUID = new DicomParam(0x20000E, new String[0]);
    public static final DicomParam Modality = new DicomParam(524384, new String[0]);
    public static final DicomParam SeriesNumber = new DicomParam(0x200011, new String[0]);
    public static final DicomParam SeriesDescription = new DicomParam(528446, new String[0]);
    public static final DicomParam SeriesDate = new DicomParam(524321, new String[0]);
    public static final DicomParam SeriesTime = new DicomParam(524337, new String[0]);
    public static final DicomParam SOPInstanceUID = new DicomParam(524312, new String[0]);
    public static final DicomParam InstanceNumber = new DicomParam(2097171, new String[0]);
    public static final DicomParam SopClassUID = new DicomParam(524310, new String[0]);

    private CFind() {
    }

    public static DicomState process(DicomNode callingNode, DicomNode calledNode, DicomParam ... keys) {
        return CFind.process(null, callingNode, calledNode, 0, QueryRetrieveLevel.STUDY, keys);
    }

    public static DicomState process(AdvancedParams params, DicomNode callingNode, DicomNode calledNode, DicomParam ... keys) {
        return CFind.process(params, callingNode, calledNode, 0, QueryRetrieveLevel.STUDY, keys);
    }

    public static DicomState process(AdvancedParams params, DicomNode callingNode, DicomNode calledNode, int cancelAfter, QueryRetrieveLevel level, DicomParam ... keys) {
        DicomState dicomState;
        CFind.validateNodes(callingNode, calledNode);
        AdvancedParams options = Objects.requireNonNullElse(params, new AdvancedParams());
        FindSCU findSCU = new FindSCU();
        try {
            CFind.configureFindSCU(findSCU, options, callingNode, calledNode, level);
            CFind.addQueryKeys(findSCU, keys);
            findSCU.setCancelAfter(cancelAfter);
            findSCU.setPriority(options.getPriority());
            dicomState = CFind.executeQuery(findSCU);
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
                return CFind.handleException(e, "DICOM Find failed");
            }
        }
        findSCU.close();
        return dicomState;
    }

    private static void validateNodes(DicomNode callingNode, DicomNode calledNode) {
        if (callingNode == null || calledNode == null) {
            throw new IllegalArgumentException("callingNode and calledNode cannot be null");
        }
    }

    private static void configureFindSCU(FindSCU findSCU, AdvancedParams options, DicomNode callingNode, DicomNode calledNode, QueryRetrieveLevel level) throws IOException {
        Connection remote = findSCU.getRemoteConnection();
        Connection connection = findSCU.getConnection();
        options.configureConnect(findSCU.getAAssociateRQ(), remote, calledNode);
        options.configureBind(findSCU.getApplicationEntity(), connection, callingNode);
        options.configure(connection);
        options.configureTLS(connection, remote);
        findSCU.setInformationModel(CFind.getInformationModel(options), options.getTsuidOrder(), options.getQueryOptions());
        if (level != null) {
            findSCU.addLevel(level.name());
        }
    }

    private static void addQueryKeys(FindSCU findSCU, DicomParam ... keys) {
        DicomState dcmState = findSCU.getState();
        for (DicomParam param : keys) {
            CFind.addAttributes(findSCU.getKeys(), param);
            String[] values = param.getValues();
            if (values == null || values.length <= 0) continue;
            dcmState.addDicomMatchingKeys(param);
        }
    }

    private static DicomState executeQuery(FindSCU findSCU) throws Exception {
        try {
            DeviceOpService service = new DeviceOpService(findSCU.getDevice());
            service.start();
            long startTime = System.currentTimeMillis();
            findSCU.open();
            long connectTime = System.currentTimeMillis();
            findSCU.query();
            ServiceUtil.forceGettingAttributes(findSCU.getState(), findSCU);
            long queryTime = System.currentTimeMillis();
            DicomState dcmState = findSCU.getState();
            String timeMessage = CFind.formatTimeMessage(findSCU, startTime, connectTime, queryTime);
            dcmState = DicomState.buildMessage(dcmState, timeMessage, null);
            dcmState.addProcessTime(startTime, connectTime, queryTime);
            return dcmState;
        }
        catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.error("C-FIND query execution failed", (Throwable)e);
            ServiceUtil.forceGettingAttributes(findSCU.getState(), findSCU);
            return DicomState.buildMessage(findSCU.getState(), null, e);
        }
    }

    private static String formatTimeMessage(FindSCU findSCU, long startTime, long connectTime, long queryTime) {
        return MessageFormat.format("DICOM C-Find connected in {2}ms from {0} to {1}. Query in {3}ms.", findSCU.getAAssociateRQ().getCallingAET(), findSCU.getAAssociateRQ().getCalledAET(), connectTime - startTime, queryTime - connectTime);
    }

    private static DicomState handleException(Exception e, String message) {
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        LOGGER.error("C-FIND operation failed", (Throwable)e);
        return DicomState.buildMessage(new DicomState(49152, message + ": " + e.getMessage(), null), null, e);
    }

    private static FindSCU.InformationModel getInformationModel(AdvancedParams options) {
        FindSCU.InformationModel informationModel;
        Object model = options.getInformationModel();
        return model instanceof FindSCU.InformationModel ? (informationModel = (FindSCU.InformationModel)((Object)model)) : FindSCU.InformationModel.StudyRoot;
    }

    public static void addAttributes(Attributes attrs, DicomParam param) {
        int tag = param.getTag();
        String[] values = param.getValues();
        VR vr = ElementDictionary.vrOf(tag, attrs.getPrivateCreator(tag));
        if (values == null || values.length == 0) {
            CFind.addReturnKey(attrs, tag, vr);
        } else {
            attrs.setString(tag, vr, values);
        }
    }

    private static void addReturnKey(Attributes attrs, int tag, VR vr) {
        if (vr == VR.SQ) {
            attrs.newSequence(tag, 1).add(new Attributes(0));
        } else {
            attrs.setNull(tag, vr);
        }
    }
}

