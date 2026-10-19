/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.FileUtil
 */
package org.weasis.dicom.op;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.tool.storescu.StoreSCU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.FileUtil;
import org.weasis.dicom.param.AdvancedParams;
import org.weasis.dicom.param.CstoreParams;
import org.weasis.dicom.param.DeviceOpService;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.DicomProgress;
import org.weasis.dicom.param.DicomState;
import org.weasis.dicom.util.ServiceUtil;

public final class CStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(CStore.class);
    private static final String DEVICE_NAME = "storescu";

    private CStore() {
    }

    public static DicomState process(DicomNode callingNode, DicomNode calledNode, List<String> files) {
        return CStore.process(null, callingNode, calledNode, files, null, null);
    }

    public static DicomState process(DicomNode callingNode, DicomNode calledNode, List<String> files, DicomProgress progress) {
        return CStore.process(null, callingNode, calledNode, files, progress, null);
    }

    public static DicomState process(AdvancedParams params, DicomNode callingNode, DicomNode calledNode, List<String> files) {
        return CStore.process(params, callingNode, calledNode, files, null, null);
    }

    public static DicomState process(AdvancedParams params, DicomNode callingNode, DicomNode calledNode, List<String> files, DicomProgress progress) {
        return CStore.process(params, callingNode, calledNode, files, progress, null);
    }

    public static DicomState process(AdvancedParams params, DicomNode callingNode, DicomNode calledNode, List<String> files, DicomProgress progress, CstoreParams cstoreParams) {
        CStore.validateParameters(callingNode, calledNode);
        AdvancedParams options = Objects.requireNonNullElse(params, new AdvancedParams());
        CstoreParams storeOptions = Objects.requireNonNullElse(cstoreParams, new CstoreParams(null, false, null));
        try {
            return CStore.executeStoreOperation(options, storeOptions, callingNode, calledNode, files, progress);
        }
        catch (Exception e) {
            LOGGER.error("C-STORE operation failed", (Throwable)e);
            return CStore.createErrorState(e);
        }
    }

    private static void validateParameters(DicomNode callingNode, DicomNode calledNode) {
        Objects.requireNonNull(callingNode, "callingNode cannot be null");
        Objects.requireNonNull(calledNode, "calledNode cannot be null");
    }

    private static DicomState executeStoreOperation(AdvancedParams options, CstoreParams storeOptions, DicomNode callingNode, DicomNode calledNode, List<String> files, DicomProgress progress) throws Exception {
        try (StoreSCU storeSCU = CStore.createStoreSCU(options, storeOptions, callingNode, calledNode, progress);){
            DicomState dicomState = CStore.performStore(storeSCU, files);
            return dicomState;
        }
    }

    private static StoreSCU createStoreSCU(AdvancedParams options, CstoreParams storeOptions, DicomNode callingNode, DicomNode calledNode, DicomProgress progress) throws IOException {
        Device device = new Device(DEVICE_NAME);
        Connection conn = new Connection();
        device.addConnection(conn);
        ApplicationEntity ae = new ApplicationEntity(callingNode.getAet());
        device.addApplicationEntity(ae);
        ae.addConnection(conn);
        StoreSCU storeSCU = new StoreSCU(ae, progress, storeOptions.getDicomEditors());
        Connection remote = storeSCU.getRemoteConnection();
        CStore.configureConnection(options, storeSCU, conn, remote, callingNode, calledNode);
        CStore.configureStoreOptions(storeSCU, storeOptions, options);
        return storeSCU;
    }

    private static void configureConnection(AdvancedParams options, StoreSCU storeSCU, Connection conn, Connection remote, DicomNode callingNode, DicomNode calledNode) throws IOException {
        options.configureConnect(storeSCU.getAAssociateRQ(), remote, calledNode);
        options.configureBind(new ApplicationEntity(callingNode.getAet()), conn, callingNode);
        options.configure(conn);
        options.configureTLS(conn, remote);
    }

    private static void configureStoreOptions(StoreSCU storeSCU, CstoreParams storeOptions, AdvancedParams options) throws IOException {
        storeSCU.setAttributes(new Attributes());
        if (storeOptions.isExtendNegotiation()) {
            CStore.configureRelatedSOPClass(storeSCU, storeOptions.getExtendSopClassesURL());
        }
        storeSCU.setPriority(options.getPriority());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static DicomState performStore(StoreSCU storeSCU, List<String> files) throws Exception {
        storeSCU.scanFiles(files, false);
        int filesScanned = storeSCU.getFilesScanned();
        if (filesScanned == 0) {
            return new DicomState(49152, "No DICOM files found", null);
        }
        DeviceOpService service = new DeviceOpService(storeSCU.getApplicationEntity().getDevice());
        service.start();
        try {
            DicomState dicomState = CStore.executeTransfer(storeSCU);
            return dicomState;
        }
        finally {
            service.stop();
        }
    }

    private static DicomState executeTransfer(StoreSCU storeSCU) throws Exception {
        long startTime = System.currentTimeMillis();
        storeSCU.open();
        long connectTime = System.currentTimeMillis();
        storeSCU.sendFiles();
        long transferTime = System.currentTimeMillis();
        ServiceUtil.forceGettingAttributes(storeSCU.getState(), storeSCU);
        return CStore.createSuccessState(storeSCU, startTime, connectTime, transferTime);
    }

    private static DicomState createSuccessState(StoreSCU storeSCU, long startTime, long connectTime, long transferTime) {
        AAssociateRQ associateRQ = storeSCU.getAAssociateRQ();
        String timeMsg = MessageFormat.format("DICOM C-STORE connected in {2}ms from {0} to {1}. Stored files in {3}ms. Total size {4}", associateRQ.getCallingAET(), associateRQ.getCalledAET(), connectTime - startTime, transferTime - connectTime, FileUtil.humanReadableByte((long)storeSCU.getTotalSize(), (boolean)false));
        DicomState dcmState = DicomState.buildMessage(storeSCU.getState(), timeMsg, null);
        dcmState.addProcessTime(startTime, connectTime, transferTime);
        dcmState.setBytesSize(storeSCU.getTotalSize());
        return dcmState;
    }

    private static DicomState createErrorState(Exception e) {
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        return DicomState.buildMessage(new DicomState(49152, "DICOM Store failed: " + e.getMessage(), null), null, e);
    }

    private static void configureRelatedSOPClass(StoreSCU storeSCU, URL url) throws IOException {
        storeSCU.enableSOPClassRelationshipExtNeg(true);
        Properties properties = new Properties();
        try (InputStream inputStream = CStore.getSOPClassesInputStream(storeSCU, url);){
            properties.load(inputStream);
        }
        catch (Exception e) {
            LOGGER.error("Failed to read SOP classes configuration", (Throwable)e);
        }
        storeSCU.relSOPClasses.init(properties);
    }

    private static InputStream getSOPClassesInputStream(StoreSCU storeSCU, URL url) throws IOException {
        return url != null ? url.openStream() : storeSCU.getClass().getResourceAsStream("rel-sop-classes.properties");
    }
}

