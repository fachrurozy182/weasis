/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.FileUtil
 */
package org.dcm4che3.tool.storescu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import javax.xml.parsers.ParserConfigurationException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.img.stream.BytesWithImageDescriptor;
import org.dcm4che3.img.stream.ImageAdapter;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.DataWriter;
import org.dcm4che3.net.DimseRSP;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.net.InputStreamDataWriter;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.tool.storescu.RelatedGeneralSOPClasses;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.FileUtil;
import org.weasis.dicom.param.AttributeEditor;
import org.weasis.dicom.param.AttributeEditorContext;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.DicomProgress;
import org.weasis.dicom.param.DicomState;
import org.weasis.dicom.util.ServiceUtil;
import org.weasis.dicom.util.StoreFromStreamSCU;
import org.xml.sax.SAXException;

public class StoreSCU
implements AutoCloseable {
    private static final Logger LOG;
    private static final String DEFAULT_TMP_PREFIX = "storescu-";
    private static final FileAttribute<?>[] OWNER_ONLY;
    private final ApplicationEntity ae;
    private final Connection remote;
    private final AAssociateRQ rq = new AAssociateRQ();
    public final RelatedGeneralSOPClasses relSOPClasses = new RelatedGeneralSOPClasses();
    private final List<AttributeEditor> dicomEditors;
    private final DicomState state;
    private Attributes attrs;
    private String uidSuffix;
    private boolean relExtNeg;
    private int priority;
    private String tmpPrefix = "storescu-";
    private String tmpSuffix;
    private Path tmpDir;
    private Path tmpFile;
    private final Path ownedTmpDir;
    private boolean ownsTmpFile;
    private Association as;
    private long totalSize = 0L;
    private int filesScanned;
    private RSPHandlerFactory rspHandlerFactory = this::createDefaultRspHandler;

    public StoreSCU(ApplicationEntity ae, DicomProgress progress) {
        this(ae, progress, null);
    }

    public StoreSCU(ApplicationEntity ae, DicomProgress progress, List<AttributeEditor> dicomEditors) {
        this.remote = new Connection();
        this.ae = Objects.requireNonNull(ae, "ApplicationEntity cannot be null");
        this.state = new DicomState(progress);
        this.dicomEditors = dicomEditors;
        try {
            this.tmpDir = this.ownedTmpDir = Files.createTempDirectory(DEFAULT_TMP_PREFIX, OWNER_ONLY);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.initializeAssociationRequest();
    }

    private void initializeAssociationRequest() {
        this.rq.addPresentationContext(new PresentationContext(1, "1.2.840.10008.1.1", "1.2.840.10008.1.2"));
    }

    private DimseRSPHandler createDefaultRspHandler(final Path file) {
        return new DimseRSPHandler(this.as.nextMessageID()){

            @Override
            public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                super.onDimseRSP(as, cmd, data);
                StoreSCU.this.onCStoreRSP(cmd, file);
                DicomProgress progress = StoreSCU.this.state.getProgress();
                if (progress != null) {
                    progress.setProcessedFile(file);
                    progress.setAttributes(cmd);
                }
            }
        };
    }

    public void setRspHandlerFactory(RSPHandlerFactory rspHandlerFactory) {
        this.rspHandlerFactory = rspHandlerFactory != null ? rspHandlerFactory : this::createDefaultRspHandler;
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

    public void setTmpFile(Path tmpFile) {
        this.tmpFile = tmpFile;
        this.ownsTmpFile = false;
    }

    public final void setPriority(int priority) {
        this.priority = priority;
    }

    public final void setUIDSuffix(String uidSuffix) {
        this.uidSuffix = uidSuffix;
    }

    public final void setTmpFilePrefix(String prefix) {
        this.tmpPrefix = prefix != null ? prefix : DEFAULT_TMP_PREFIX;
    }

    public final void setTmpFileSuffix(String suffix) {
        this.tmpSuffix = suffix;
    }

    public final void setTmpFileDirectory(Path tmpDir) {
        if (tmpDir != null && !Files.isDirectory(tmpDir, new LinkOption[0])) {
            throw new IllegalArgumentException("Not a directory: " + String.valueOf(tmpDir));
        }
        this.tmpDir = tmpDir;
    }

    public final void enableSOPClassRelationshipExtNeg(boolean enable) {
        this.relExtNeg = enable;
    }

    public void scanFiles(List<String> fnames) throws IOException {
        this.scanFiles(fnames, true);
    }

    public void scanFiles(List<String> fnames, boolean printout) throws IOException {
        this.tmpFile = this.createTempFile();
        this.ownsTmpFile = true;
        try (BufferedWriter writer = Files.newBufferedWriter(this.tmpFile, StandardCharsets.UTF_8, new OpenOption[0]);){
            for (String fname : fnames) {
                this.scanPath(Paths.get(fname, new String[0]), writer, printout);
            }
        }
    }

    private Path createTempFile() throws IOException {
        return Files.createTempFile(this.tmpDir, this.tmpPrefix, this.tmpSuffix, new FileAttribute[0]);
    }

    private void scanPath(Path path, Writer writer, boolean printout) {
        if (Files.isDirectory(path, new LinkOption[0]) && Files.isReadable(path)) {
            this.scanDirectory(path, writer, printout);
        } else {
            this.scanFile(path, writer, printout);
        }
    }

    private void scanDirectory(Path dir, Writer writer, boolean printout) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir);){
            for (Path entry : stream) {
                this.scanPath(entry, writer, printout);
            }
        }
        catch (IOException e) {
            LOG.error("Failed to scan directory {}", (Object)dir, (Object)e);
        }
    }

    private void scanFile(Path file, Writer writer, boolean printout) {
        try (DicomInputStream in = new DicomInputStream(Files.newInputStream(file, new OpenOption[0]));){
            boolean success;
            in.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
            Attributes fmi = in.readFileMetaInformation();
            long dsPos = in.getPosition();
            if (this.isInvalidFileMetaInformation(fmi)) {
                Attributes ds = in.readDataset(524313);
                fmi = ds.createFileMetaInformation(in.getTransferSyntax());
            }
            if (success = this.addFile(writer, file, dsPos, fmi)) {
                ++this.filesScanned;
            }
            this.printProgress(printout, success);
        }
        catch (Exception e) {
            this.handleScanError(file, e);
        }
    }

    private boolean isInvalidFileMetaInformation(Attributes fmi) {
        return fmi == null || !fmi.containsValue(131088) || !fmi.containsValue(131074) || !fmi.containsValue(131075);
    }

    private void printProgress(boolean printout, boolean success) {
        if (printout) {
            System.out.print(success ? (char)'.' : 'I');
        }
    }

    private void handleScanError(Path file, Exception e) {
        System.out.println();
        System.out.println("Failed to scan file " + String.valueOf(file) + ": " + e.getMessage());
        LOG.error("Failed to scan file {}", (Object)file, (Object)e);
    }

    public void sendFiles() throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(this.tmpFile, StandardCharsets.UTF_8);){
            String line;
            while (this.as.isReadyForDataTransfer() && (line = reader.readLine()) != null) {
                if (this.isProgressCancelled()) {
                    LOG.info("Aborting C-Store: cancel by progress");
                    this.as.abort();
                    break;
                }
                String[] fileInfo = StringUtils.split(line, '\t');
                this.sendFileFromInfo(fileInfo);
            }
            this.waitForOutstandingResponses();
        }
    }

    private boolean isProgressCancelled() {
        DicomProgress progress = this.state.getProgress();
        return progress != null && progress.isCancelled();
    }

    private void sendFileFromInfo(String[] fileInfo) {
        try {
            Path file = Path.of(fileInfo[4], new String[0]);
            long dsPos = Long.parseLong(fileInfo[3]);
            String cuid = fileInfo[1];
            String iuid = fileInfo[0];
            String tsuid = fileInfo[2];
            this.send(file, dsPos, cuid, iuid, tsuid);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.warn("File send interrupted", (Throwable)e);
        }
        catch (Exception e) {
            LOG.error("Cannot send file", (Throwable)e);
        }
    }

    private void waitForOutstandingResponses() {
        try {
            this.as.waitForOutstandingRSP();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Waiting for RSP interrupted", (Throwable)e);
        }
    }

    public boolean addFile(Writer writer, Path path, long endFmi, Attributes fmi) throws IOException {
        String cuid = fmi.getString(131074);
        String iuid = fmi.getString(131075);
        String tsuid = fmi.getString(131088);
        if (cuid == null || iuid == null) {
            return false;
        }
        this.writeFileInfo(writer, path, endFmi, cuid, iuid, tsuid);
        this.addPresentationContextsIfNeeded(cuid, tsuid);
        return true;
    }

    private void writeFileInfo(Writer writer, Path path, long endFmi, String cuid, String iuid, String tsuid) throws IOException {
        writer.write(String.join((CharSequence)"\t", iuid, cuid, tsuid, Long.toString(endFmi), path.toString()));
        writer.write(10);
    }

    private void addPresentationContextsIfNeeded(String cuid, String tsuid) {
        if (this.rq.containsPresentationContextFor(cuid, tsuid)) {
            return;
        }
        if (!this.rq.containsPresentationContextFor(cuid)) {
            this.addRelatedExtendedNegotiation(cuid);
            this.addStandardTransferSyntaxes(cuid, tsuid);
        }
        this.addPresentationContext(cuid, tsuid);
    }

    private void addRelatedExtendedNegotiation(String cuid) {
        if (this.relExtNeg) {
            this.rq.addCommonExtendedNegotiation(this.relSOPClasses.getCommonExtendedNegotiation(cuid));
        }
    }

    private void addStandardTransferSyntaxes(String cuid, String currentTsuid) {
        if (!currentTsuid.equals("1.2.840.10008.1.2.1")) {
            this.addPresentationContext(cuid, "1.2.840.10008.1.2.1");
        }
        if (!currentTsuid.equals("1.2.840.10008.1.2")) {
            this.addPresentationContext(cuid, "1.2.840.10008.1.2");
        }
    }

    private void addPresentationContext(String cuid, String tsuid) {
        PresentationContext pc = new PresentationContext(this.rq.getNumberOfPresentationContexts() * 2 + 1, cuid, tsuid);
        this.rq.addPresentationContext(pc);
    }

    public Attributes echo() throws IOException, InterruptedException {
        DimseRSP response = this.as.cecho();
        response.next();
        return response.getCommand();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void send(Path path, long fmiEndPos, String cuid, String iuid, String tsuid) throws IOException, InterruptedException, ParserConfigurationException, SAXException {
        ImageAdapter.AdaptTransferSyntax syntax = new ImageAdapter.AdaptTransferSyntax(tsuid, StoreFromStreamSCU.selectTransferSyntax(this.as, cuid, tsuid));
        boolean isNoChange = this.shouldSkipProcessing(syntax, tsuid);
        DataWriter dataWriter = null;
        Attributes data = null;
        InputStream in = this.createInputStream(path, fmiEndPos, isNoChange);
        try (InputStream inputStream = in;){
            if (path.getFileName().toString().endsWith(".xml")) {
                data = SAXReader.parse(in);
                isNoChange = false;
            } else if (isNoChange) {
                dataWriter = new InputStreamDataWriter(in);
            } else {
                data = this.readDicomDataset((DicomInputStream)in);
            }
            if (!isNoChange) {
                data = this.processAttributes(data, syntax);
                String[] updatedIds = this.updateInstanceIdentifiers(data);
                cuid = updatedIds[0];
                iuid = updatedIds[1];
                BytesWithImageDescriptor desc = ImageAdapter.imageTranscode(data, syntax, this.createEditorContext(syntax));
                dataWriter = ImageAdapter.buildDataWriter(data, syntax, this.createEditorContext(syntax).getEditable(), desc);
            }
            this.as.cstore(cuid, iuid, this.priority, dataWriter, syntax.getSuitable(), this.rspHandlerFactory.createDimseRSPHandler(path));
        }
        finally {
            if (in instanceof DicomInputStream) {
                DicomInputStream dis = (DicomInputStream)in;
                ServiceUtil.safeClose(dis);
            }
        }
    }

    private boolean shouldSkipProcessing(ImageAdapter.AdaptTransferSyntax syntax, String tsuid) {
        return !(this.uidSuffix != null || this.attrs != null && !this.attrs.isEmpty() || !syntax.getRequested().equals(tsuid) || this.dicomEditors != null && !this.dicomEditors.isEmpty());
    }

    private InputStream createInputStream(Path path, long fmiEndPos, boolean isNoChange) throws IOException {
        if (path.getFileName().toString().endsWith(".xml")) {
            return new DicomInputStream(Files.newInputStream(path, new OpenOption[0]));
        }
        if (!isNoChange) {
            DicomInputStream in = new DicomInputStream(path.toFile());
            in.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
            return in;
        }
        InputStream in = Files.newInputStream(path, new OpenOption[0]);
        in.skip(fmiEndPos);
        return in;
    }

    private Attributes readDicomDataset(DicomInputStream in) throws IOException {
        return in.readDataset();
    }

    private Attributes processAttributes(Attributes data, ImageAdapter.AdaptTransferSyntax syntax) {
        AttributeEditorContext context = this.createEditorContext(syntax);
        if (this.dicomEditors != null && !this.dicomEditors.isEmpty()) {
            this.dicomEditors.forEach(editor -> editor.apply(data, context));
        }
        if (this.attrs != null && !this.attrs.isEmpty()) {
            CLIUtils.updateAttributes(data, this.attrs, this.uidSuffix);
        }
        return data;
    }

    private AttributeEditorContext createEditorContext(ImageAdapter.AdaptTransferSyntax syntax) {
        return new AttributeEditorContext(syntax.getOriginal(), DicomNode.buildLocalDicomNode(this.as), DicomNode.buildRemoteDicomNode(this.as));
    }

    private String[] updateInstanceIdentifiers(Attributes data) {
        return new String[]{data.getString(524310), data.getString(524312)};
    }

    @Override
    public void close() throws IOException, InterruptedException {
        try {
            if (this.as != null) {
                if (this.as.isReadyForDataTransfer()) {
                    this.as.release();
                }
                this.as.waitForSocketClose();
            }
        }
        finally {
            if (this.ownsTmpFile) {
                FileUtil.delete((Path)this.tmpFile);
            }
            FileUtil.recursiveDelete((Path)this.ownedTmpDir);
        }
    }

    public void open() throws IOException, InterruptedException, IncompatibleConnectionException, GeneralSecurityException {
        this.as = this.ae.connect(this.remote, this.rq);
    }

    private void onCStoreRSP(Attributes cmd, Path file) {
        int status = cmd.getInt(2304, -1);
        this.state.setStatus(status);
        ServiceUtil.ProgressStatus progressStatus = this.determineProgressStatus(status, file, cmd);
        this.updateTotalSize(status, file);
        ServiceUtil.notifyProgression(this.state.getProgress(), cmd, progressStatus, this.filesScanned);
    }

    private ServiceUtil.ProgressStatus determineProgressStatus(int status, Path file, Attributes cmd) {
        return switch (status) {
            case 0 -> ServiceUtil.ProgressStatus.COMPLETED;
            case 45056, 45062, 45063 -> {
                this.logWarning(status, file, cmd);
                yield ServiceUtil.ProgressStatus.WARNING;
            }
            default -> {
                this.logError(status, file, cmd);
                yield ServiceUtil.ProgressStatus.FAILED;
            }
        };
    }

    private void updateTotalSize(int status, Path file) {
        if (status == 0 || status == 45056 || status == 45062 || status == 45063) {
            try {
                this.totalSize += Files.size(file);
            }
            catch (IOException e) {
                LOG.warn("Cannot get file size for {}", (Object)file, (Object)e);
            }
        }
    }

    private void logWarning(int status, Path file, Attributes cmd) {
        System.err.println(MessageFormat.format("WARNING: Received C-STORE-RSP with Status {0}H for {1}", TagUtils.shortToHexString(status), file));
        System.err.println(cmd);
    }

    private void logError(int status, Path file, Attributes cmd) {
        System.err.println(MessageFormat.format("ERROR: Received C-STORE-RSP with Status {0}H for {1}", TagUtils.shortToHexString(status), file));
        System.err.println(cmd);
    }

    public ApplicationEntity getApplicationEntity() {
        return this.ae;
    }

    public int getFilesScanned() {
        return this.filesScanned;
    }

    public long getTotalSize() {
        return this.totalSize;
    }

    public DicomState getState() {
        return this.state;
    }

    static {
        FileAttribute[] fileAttributeArray;
        LOG = LoggerFactory.getLogger(StoreSCU.class);
        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            FileAttribute[] fileAttributeArray2 = new FileAttribute[1];
            fileAttributeArray = fileAttributeArray2;
            fileAttributeArray2[0] = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));
        } else {
            fileAttributeArray = new FileAttribute[]{};
        }
        OWNER_ONLY = fileAttributeArray;
    }

    @FunctionalInterface
    public static interface RSPHandlerFactory {
        public DimseRSPHandler createDimseRSPHandler(Path var1);
    }
}

