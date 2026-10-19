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
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.tool.storescu.StoreSCU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.dicom.param.AdvancedParams;
import org.weasis.dicom.param.DeviceOpService;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.DicomState;

public final class Echo {
    private static final Logger LOGGER = LoggerFactory.getLogger(Echo.class);
    private static final String DEVICE_NAME = "echo-scu";

    private Echo() {
    }

    public static DicomState process(String callingAET, DicomNode calledNode) {
        Objects.requireNonNull(callingAET, "callingAET cannot be null");
        Objects.requireNonNull(calledNode, "calledNode cannot be null");
        return Echo.process(new DicomNode(callingAET), calledNode);
    }

    public static DicomState process(DicomNode callingNode, DicomNode calledNode) {
        return Echo.process(null, callingNode, calledNode);
    }

    public static DicomState process(AdvancedParams params, DicomNode callingNode, DicomNode calledNode) {
        Objects.requireNonNull(callingNode, "callingNode cannot be null");
        Objects.requireNonNull(calledNode, "calledNode cannot be null");
        AdvancedParams options = Objects.requireNonNullElse(params, new AdvancedParams());
        try {
            return Echo.performEcho(options, callingNode, calledNode);
        }
        catch (Exception e) {
            return Echo.handleException(e);
        }
    }

    private static DicomState performEcho(AdvancedParams options, DicomNode callingNode, DicomNode calledNode) throws Exception {
        Device device = Echo.createDevice(callingNode);
        StoreSCU storeSCU = Echo.createStoreSCU(device, callingNode);
        DeviceOpService service = new DeviceOpService(device);
        Echo.configureConnections(options, storeSCU, callingNode, calledNode);
        service.start();
        try {
            DicomState dicomState;
            block9: {
                StoreSCU storeSCU2 = storeSCU;
                try {
                    dicomState = Echo.executeEcho(storeSCU);
                    if (storeSCU2 == null) break block9;
                }
                catch (Throwable throwable) {
                    if (storeSCU2 != null) {
                        try {
                            storeSCU2.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                storeSCU2.close();
            }
            return dicomState;
        }
        finally {
            service.stop();
        }
    }

    private static Device createDevice(DicomNode callingNode) {
        Device device = new Device(DEVICE_NAME);
        Connection conn = new Connection();
        device.addConnection(conn);
        ApplicationEntity ae = new ApplicationEntity(callingNode.getAet());
        device.addApplicationEntity(ae);
        ae.addConnection(conn);
        return device;
    }

    private static StoreSCU createStoreSCU(Device device, DicomNode callingNode) throws IOException {
        ApplicationEntity ae = device.getApplicationEntity(callingNode.getAet());
        return new StoreSCU(ae, null);
    }

    private static void configureConnections(AdvancedParams options, StoreSCU storeSCU, DicomNode callingNode, DicomNode calledNode) throws IOException {
        Connection remote = storeSCU.getRemoteConnection();
        ApplicationEntity ae = storeSCU.getApplicationEntity();
        Connection conn = ae.getConnections().get(0);
        options.configureConnect(storeSCU.getAAssociateRQ(), remote, calledNode);
        options.configureBind(ae, conn, callingNode);
        options.configure(conn);
        options.configureTLS(conn, remote);
        storeSCU.setPriority(options.getPriority());
    }

    private static DicomState executeEcho(StoreSCU storeSCU) throws Exception {
        long connectStart = System.currentTimeMillis();
        storeSCU.open();
        long echoStart = System.currentTimeMillis();
        Attributes response = storeSCU.echo();
        long echoEnd = System.currentTimeMillis();
        String message = Echo.formatSuccessMessage(storeSCU, connectStart, echoStart, echoEnd);
        DicomState state = new DicomState(response.getInt(2304, 0), message, null);
        state.addProcessTime(connectStart, echoStart, echoEnd);
        return state;
    }

    private static String formatSuccessMessage(StoreSCU storeSCU, long connectStart, long echoStart, long echoEnd) {
        AAssociateRQ rq = storeSCU.getAAssociateRQ();
        return MessageFormat.format("Successful DICOM Echo. Connected in {2}ms from {0} to {1}. Service execution in {3}ms.", rq.getCallingAET(), rq.getCalledAET(), echoStart - connectStart, echoEnd - echoStart);
    }

    private static DicomState handleException(Exception e) {
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        String message = "DICOM Echo failed: " + e.getMessage();
        LOGGER.error(message, (Throwable)e);
        return DicomState.buildMessage(new DicomState(49152, message, null), null, e);
    }
}

