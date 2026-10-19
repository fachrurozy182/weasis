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
import java.text.MessageFormat;
import java.util.Objects;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.tool.movescu.MoveSCU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.StreamUtil;
import org.weasis.dicom.param.AdvancedParams;
import org.weasis.dicom.param.DeviceOpService;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.DicomParam;
import org.weasis.dicom.param.DicomProgress;
import org.weasis.dicom.param.DicomState;
import org.weasis.dicom.util.ServiceUtil;

public final class CMove {
    private static final Logger LOGGER = LoggerFactory.getLogger(CMove.class);

    private CMove() {
    }

    public static DicomState process(DicomNode callingNode, DicomNode calledNode, String destinationAet, DicomProgress progress, DicomParam ... keys) {
        return CMove.process(null, callingNode, calledNode, destinationAet, progress, keys);
    }

    public static DicomState process(AdvancedParams params, DicomNode callingNode, DicomNode calledNode, String destinationAet, DicomProgress progress, DicomParam ... keys) {
        DicomState dicomState;
        CMove.validateRequiredParameters(callingNode, calledNode, destinationAet);
        AdvancedParams options = params != null ? params : new AdvancedParams();
        MoveSCU moveSCU = new MoveSCU(progress);
        try {
            DeviceOpService service = CMove.configureMoveSCU(moveSCU, options, callingNode, calledNode);
            CMove.addKeysToRequest(moveSCU, keys);
            moveSCU.setDestination(destinationAet);
            dicomState = CMove.executeMove(moveSCU, service);
        }
        catch (Throwable throwable) {
            try {
                try {
                    moveSCU.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (Exception e) {
                return CMove.handleTopLevelException(e);
            }
        }
        moveSCU.close();
        return dicomState;
    }

    private static void validateRequiredParameters(DicomNode callingNode, DicomNode calledNode, String destinationAet) {
        Objects.requireNonNull(callingNode, "callingNode cannot be null");
        Objects.requireNonNull(calledNode, "calledNode cannot be null");
        Objects.requireNonNull(destinationAet, "destinationAet cannot be null");
    }

    private static DeviceOpService configureMoveSCU(MoveSCU moveSCU, AdvancedParams options, DicomNode callingNode, DicomNode calledNode) throws IOException {
        Connection remote = moveSCU.getRemoteConnection();
        Connection conn = moveSCU.getConnection();
        options.configureConnect(moveSCU.getAAssociateRQ(), remote, calledNode);
        options.configureBind(moveSCU.getApplicationEntity(), conn, callingNode);
        options.configure(conn);
        options.configureTLS(conn, remote);
        moveSCU.setInformationModel(CMove.getInformationModel(options), options.getTsuidOrder(), options.getQueryOptions().contains((Object)QueryOption.RELATIONAL));
        return new DeviceOpService(moveSCU);
    }

    private static void addKeysToRequest(MoveSCU moveSCU, DicomParam ... keys) {
        DicomState dcmState = moveSCU.getState();
        for (DicomParam param : keys) {
            String[] values = param.getValues();
            moveSCU.addKey(param.getTag(), values);
            if (values == null || values.length <= 0) continue;
            dcmState.addDicomMatchingKeys(param);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static DicomState executeMove(MoveSCU moveSCU, DeviceOpService service) {
        service.start();
        try {
            long t1 = System.currentTimeMillis();
            moveSCU.open();
            long t2 = System.currentTimeMillis();
            moveSCU.retrieve();
            ServiceUtil.forceGettingAttributes(moveSCU.getState(), moveSCU);
            long t3 = System.currentTimeMillis();
            DicomState dcmState = moveSCU.getState();
            String timeMsg = CMove.createTimeMessage(moveSCU, t1, t2, t3);
            dcmState = DicomState.buildMessage(dcmState, timeMsg, null);
            dcmState.addProcessTime(t1, t2, t3);
            DicomState dicomState = dcmState;
            return dicomState;
        }
        catch (Exception e) {
            DicomState dicomState = CMove.handleMoveException(moveSCU, e);
            return dicomState;
        }
        finally {
            CMove.cleanupResources(moveSCU, service);
        }
    }

    private static String createTimeMessage(MoveSCU moveSCU, long t1, long t2, long t3) {
        return MessageFormat.format("DICOM C-MOVE connected in {2}ms from {0} to {1}. Sent files in {3}ms.", moveSCU.getAAssociateRQ().getCallingAET(), moveSCU.getAAssociateRQ().getCalledAET(), t2 - t1, t3 - t2);
    }

    private static DicomState handleMoveException(MoveSCU moveSCU, Exception e) {
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        LOGGER.error("C-MOVE operation failed", (Throwable)e);
        ServiceUtil.forceGettingAttributes(moveSCU.getState(), moveSCU);
        return DicomState.buildMessage(moveSCU.getState(), null, e);
    }

    private static void cleanupResources(MoveSCU moveSCU, DeviceOpService service) {
        StreamUtil.safeClose((AutoCloseable)moveSCU);
        service.stop();
    }

    private static DicomState handleTopLevelException(Exception e) {
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        LOGGER.error("C-MOVE setup failed", (Throwable)e);
        return DicomState.buildMessage(new DicomState(49152, "DICOM Move failed: " + e.getMessage(), null), null, e);
    }

    private static MoveSCU.InformationModel getInformationModel(AdvancedParams options) {
        MoveSCU.InformationModel informationModel;
        Object model = options.getInformationModel();
        return model instanceof MoveSCU.InformationModel ? (informationModel = (MoveSCU.InformationModel)((Object)model)) : MoveSCU.InformationModel.StudyRoot;
    }
}

