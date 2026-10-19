/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StringUtil
 */
package org.dcm4che3.tool.storescp;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicCEchoSCP;
import org.dcm4che3.net.service.BasicCStoreSCP;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.AttributesFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.DicomProgress;

public class StoreSCP {
    private static final Logger LOGGER = LoggerFactory.getLogger(StoreSCP.class);
    private static final String TMP_DIR = "tmp";
    private static final String REGEX_PATTERN = "\\{(.*?)\\}";
    private final Device device = new Device("storescp");
    private final ApplicationEntity ae = new ApplicationEntity("*");
    private final Connection conn = new Connection();
    private final Path storageDir;
    private final List<DicomNode> authorizedCallingNodes;
    private final DicomProgress progress;
    private AttributesFormat filePathFormat;
    private Pattern regex;
    private volatile int status = 0;
    private int[] receiveDelays;
    private int[] responseDelays;
    private final BasicCStoreSCP cstoreSCP = new BasicCStoreSCP(new String[]{"*"}){

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        protected void store(Association as, PresentationContext pc, Attributes rq, PDVInputStream data, Attributes rsp) throws IOException {
            if (!StoreSCP.this.isNodeAuthorized(as, rq, rsp)) {
                return;
            }
            StoreSCP.this.sleep(as, StoreSCP.this.receiveDelays);
            try {
                StoreSCP.this.storeFile(as, pc, rq, data, rsp);
            }
            finally {
                StoreSCP.this.sleep(as, StoreSCP.this.responseDelays);
            }
        }
    };

    public StoreSCP(Path storageDir) {
        this(storageDir, null, null);
    }

    public StoreSCP(Path storageDir, List<DicomNode> authorizedCallingNodes) {
        this(storageDir, authorizedCallingNodes, null);
    }

    public StoreSCP(Path storageDir, List<DicomNode> authorizedCallingNodes, DicomProgress progress) {
        this.storageDir = Objects.requireNonNull(storageDir, "Storage directory cannot be null");
        this.authorizedCallingNodes = authorizedCallingNodes;
        this.progress = progress;
        this.initializeDevice();
    }

    private void initializeDevice() {
        this.device.setDimseRQHandler(this.createServiceRegistry());
        this.device.addConnection(this.conn);
        this.device.addApplicationEntity(this.ae);
        this.ae.setAssociationAcceptor(true);
        this.ae.addConnection(this.conn);
    }

    private boolean isNodeAuthorized(Association as, Attributes rq, Attributes rsp) {
        if (this.authorizedCallingNodes == null || this.authorizedCallingNodes.isEmpty()) {
            return true;
        }
        DicomNode sourceNode = DicomNode.buildRemoteDicomNode(as);
        boolean valid = this.authorizedCallingNodes.stream().anyMatch(n -> n.getAet().equals(sourceNode.getAet()) && (!n.isValidateHostname() || n.equalsHostname(sourceNode.getHostname())));
        if (!valid) {
            rsp.setInt(2304, VR.US, 292);
            LOGGER.error("Refused: not authorized (124H). Source node: {}. SopUID: {}", (Object)sourceNode, (Object)rq.getString(4096));
        }
        return valid;
    }

    private void storeFile(Association as, PresentationContext pc, Attributes rq, PDVInputStream data, Attributes rsp) throws IOException {
        rsp.setInt(2304, VR.US, this.status);
        String cuid = rq.getString(2);
        String iuid = rq.getString(4096);
        String tsuid = pc.getTransferSyntax();
        Path tempFile = this.createTempFile(iuid);
        try {
            Attributes fmi = as.createFileMetaInformation(iuid, cuid, tsuid);
            this.writeToTempFile(as, fmi, data, tempFile);
            String filename = this.determineFilename(tempFile, fmi, iuid);
            Path finalFile = this.storageDir.resolve(filename);
            this.moveToFinalLocation(as, tempFile, finalFile);
            this.notifyProgress(finalFile);
        }
        catch (Exception e) {
            this.cleanupTempFile(tempFile);
            throw new DicomServiceException(272, (Throwable)e);
        }
    }

    private Path createTempFile(String instanceUid) {
        return this.storageDir.resolve(TMP_DIR).resolve(instanceUid);
    }

    private void writeToTempFile(Association as, Attributes fmi, PDVInputStream data, Path tempFile) throws IOException {
        LOGGER.debug("{}: M-WRITE {}", (Object)as, (Object)tempFile);
        Files.createDirectories(tempFile.getParent(), new FileAttribute[0]);
        try (DicomOutputStream out = new DicomOutputStream(tempFile.toFile());){
            out.writeFileMetaInformation(fmi);
            data.copyTo(out);
        }
    }

    private String determineFilename(Path tempFile, Attributes fmi, String instanceUid) throws IOException {
        if (this.filePathFormat == null) {
            return instanceUid;
        }
        Attributes attributes = fmi;
        if (this.needsAttributeParsing()) {
            attributes = this.parseAttributes(tempFile);
            Attributes fmiToMerge = new Attributes(fmi);
            fmiToMerge.setSpecificCharacterSet(attributes.getSpecificCharacterSet().toCodes());
            attributes.addAll(fmiToMerge);
        }
        return this.filePathFormat.format(attributes);
    }

    private boolean needsAttributeParsing() {
        if (this.regex == null) {
            return false;
        }
        Matcher matcher = this.regex.matcher(this.filePathFormat.toString());
        while (matcher.find()) {
            if (matcher.group(1).startsWith("0002")) continue;
            return true;
        }
        return false;
    }

    private Attributes parseAttributes(Path file) throws IOException {
        try (DicomInputStream in = new DicomInputStream(file.toFile());){
            in.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
            Attributes attributes = in.readDatasetUntilPixelData();
            return attributes;
        }
    }

    private void moveToFinalLocation(Association as, Path source, Path target) throws IOException {
        LOGGER.info("{}: M-RENAME {} to {}", new Object[]{as, source, target});
        Files.createDirectories(target.getParent(), new FileAttribute[0]);
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    private void notifyProgress(Path file) {
        if (this.progress != null) {
            this.progress.setProcessedFile(file);
            this.progress.setAttributes(null);
        }
    }

    private void cleanupTempFile(Path tempFile) {
        try {
            Files.deleteIfExists(tempFile);
        }
        catch (IOException e) {
            LOGGER.warn("Failed to cleanup temporary file: {}", (Object)tempFile, (Object)e);
        }
    }

    private void sleep(Association as, int[] delays) {
        if (delays == null) {
            return;
        }
        int delayMs = delays[(as.getNumberOfReceived(Dimse.C_STORE_RQ) - 1) % delays.length];
        if (delayMs > 0) {
            try {
                Thread.sleep(delayMs);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.debug("Sleep interrupted", (Throwable)e);
            }
        }
    }

    private DicomServiceRegistry createServiceRegistry() {
        DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
        serviceRegistry.addDicomService(new BasicCEchoSCP());
        serviceRegistry.addDicomService(this.cstoreSCP);
        return serviceRegistry;
    }

    public void setStorageFilePathFormat(String pattern) {
        if (StringUtil.hasText((CharSequence)pattern)) {
            this.filePathFormat = new AttributesFormat(pattern);
            this.regex = Pattern.compile(REGEX_PATTERN);
        } else {
            this.filePathFormat = null;
            this.regex = null;
        }
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setReceiveDelays(int[] receiveDelays) {
        this.receiveDelays = receiveDelays != null ? (int[])receiveDelays.clone() : null;
    }

    public void setResponseDelays(int[] responseDelays) {
        this.responseDelays = responseDelays != null ? (int[])responseDelays.clone() : null;
    }

    public void loadDefaultTransferCapability(URL transferCapabilityFile) {
        Properties properties = new Properties();
        try {
            this.loadProperties(properties, transferCapabilityFile);
            this.configureTransferCapabilities(properties);
        }
        catch (IOException e) {
            LOGGER.error("Cannot read sop-classes", (Throwable)e);
        }
    }

    private void loadProperties(Properties properties, URL transferCapabilityFile) throws IOException {
        if (transferCapabilityFile != null) {
            try (InputStream in = transferCapabilityFile.openStream();){
                properties.load(in);
            }
        }
        try (InputStream in = this.getClass().getResourceAsStream("sop-classes.properties");){
            properties.load(in);
        }
    }

    private void configureTransferCapabilities(Properties properties) {
        for (String cuid : properties.stringPropertyNames()) {
            String ts = properties.getProperty(cuid);
            TransferCapability tc = new TransferCapability(null, CLIUtils.toUID(cuid), TransferCapability.Role.SCP, CLIUtils.toUIDs(ts));
            this.ae.addTransferCapability(tc);
        }
    }

    public ApplicationEntity getApplicationEntity() {
        return this.ae;
    }

    public Connection getConnection() {
        return this.conn;
    }

    public Device getDevice() {
        return this.device;
    }

    public Path getStorageDir() {
        return this.storageDir;
    }

    public DicomProgress getProgress() {
        return this.progress;
    }
}

