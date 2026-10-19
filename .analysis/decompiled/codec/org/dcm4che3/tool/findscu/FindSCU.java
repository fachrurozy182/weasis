/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.dcm4che3.tool.findscu;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.util.EnumSet;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.io.SAXWriter;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.ExtendedNegotiation;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.util.SafeClose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.dicom.param.DicomProgress;
import org.weasis.dicom.param.DicomState;

public class FindSCU
implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(FindSCU.class);
    private SAXTransformerFactory saxtf;
    private final Device device = new Device("findscu");
    private final ApplicationEntity ae = new ApplicationEntity("FINDSCU");
    private final Connection conn = new Connection();
    private final Connection remote = new Connection();
    private final AAssociateRQ rq = new AAssociateRQ();
    private int priority;
    private int cancelAfter;
    private InformationModel model;
    private final Attributes keys = new Attributes();
    private int[] inFilter;
    private Path outDir;
    private DecimalFormat outFileFormat;
    private boolean catOut = false;
    private boolean xml = false;
    private boolean xmlIndent = false;
    private boolean xmlIncludeKeyword = true;
    private boolean xmlIncludeNamespaceDeclaration = false;
    private Path xsltFile;
    private Templates xsltTpls;
    private Association as;
    private OutputStream out;
    private final AtomicInteger totNumMatches = new AtomicInteger();
    private final DicomState state;

    public FindSCU() {
        this.device.addConnection(this.conn);
        this.device.addApplicationEntity(this.ae);
        this.ae.addConnection(this.conn);
        this.state = new DicomState(new DicomProgress());
    }

    public final void setPriority(int priority) {
        this.priority = priority;
    }

    public final void setInformationModel(InformationModel model, String[] tss, EnumSet<QueryOption> queryOptions) {
        this.model = model;
        this.rq.addPresentationContext(new PresentationContext(1, model.cuid, tss));
        if (!queryOptions.isEmpty()) {
            model.adjustQueryOptions(queryOptions);
            this.rq.addExtendedNegotiation(new ExtendedNegotiation(model.cuid, QueryOption.toExtendedNegotiationInformation(queryOptions)));
        }
        if (model.level != null) {
            this.addLevel(model.level);
        }
    }

    public void addLevel(String level) {
        this.keys.setString(524370, VR.CS, level);
    }

    public final void setCancelAfter(int cancelAfter) {
        this.cancelAfter = cancelAfter;
    }

    public final void setOutputDirectory(Path outDir) {
        this.outDir = outDir;
    }

    public final void setOutputFileFormat(String outFileFormat) {
        this.outFileFormat = new DecimalFormat(outFileFormat);
    }

    public final void setXSLT(Path xsltFile) {
        this.xsltFile = xsltFile;
    }

    public final void setXML(boolean xml) {
        this.xml = xml;
    }

    public final void setXMLIndent(boolean indent) {
        this.xmlIndent = indent;
    }

    public final void setXMLIncludeKeyword(boolean includeKeyword) {
        this.xmlIncludeKeyword = includeKeyword;
    }

    public final void setXMLIncludeNamespaceDeclaration(boolean includeNamespaceDeclaration) {
        this.xmlIncludeNamespaceDeclaration = includeNamespaceDeclaration;
    }

    public final void setConcatenateOutputFiles(boolean catOut) {
        this.catOut = catOut;
    }

    public final void setInputFilter(int[] inFilter) {
        this.inFilter = inFilter;
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

    public Connection getConnection() {
        return this.conn;
    }

    public DicomState getState() {
        return this.state;
    }

    public void open() throws IOException, InterruptedException, IncompatibleConnectionException, GeneralSecurityException {
        this.as = this.ae.connect(this.conn, this.remote, this.rq);
    }

    @Override
    public void close() throws IOException, InterruptedException {
        if (this.as != null && this.as.isReadyForDataTransfer()) {
            this.as.waitForOutstandingRSP();
            this.as.release();
        }
        SafeClose.close(this.out);
        this.out = null;
    }

    public void query(Path filePath) throws Exception {
        Attributes attrs = this.loadAttributesFromFile(filePath);
        attrs = this.applyInputFilter(attrs);
        FindSCU.mergeKeys(attrs, this.keys);
        this.query(attrs);
    }

    public void query() throws IOException, InterruptedException {
        this.query(this.keys);
    }

    public void query(DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        this.query(this.keys, rspHandler);
    }

    private void query(Attributes keys) throws IOException, InterruptedException {
        DimseRSPHandler rspHandler = this.createResponseHandler();
        this.query(keys, rspHandler);
    }

    private void query(Attributes keys, DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        this.as.cfind(this.model.cuid, this.priority, keys, null, rspHandler);
    }

    private Attributes loadAttributesFromFile(Path filePath) throws Exception {
        String fileName = filePath.getFileName().toString().toLowerCase(Locale.ROOT);
        if (fileName.endsWith(".xml")) {
            return SAXReader.parse(filePath.toString());
        }
        try (DicomInputStream dis = new DicomInputStream(filePath.toFile());){
            Attributes attributes = dis.readDataset();
            return attributes;
        }
    }

    private Attributes applyInputFilter(Attributes attrs) {
        if (this.inFilter == null) {
            return attrs;
        }
        Attributes filtered = new Attributes(this.inFilter.length + 1);
        filtered.addSelected(attrs, this.inFilter);
        return filtered;
    }

    private DimseRSPHandler createResponseHandler() {
        return new DimseRSPHandler(this.as.nextMessageID()){
            int cancelAfter;
            int numMatches;
            {
                this.cancelAfter = FindSCU.this.cancelAfter;
            }

            @Override
            public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                super.onDimseRSP(as, cmd, data);
                int status = cmd.getInt(2304, -1);
                if (Status.isPending(status)) {
                    FindSCU.this.onResult(data);
                    ++this.numMatches;
                    if (this.shouldCancel(this.numMatches)) {
                        this.cancelQuery(as);
                    }
                } else {
                    FindSCU.this.state.setStatus(status);
                }
            }

            private boolean shouldCancel(int numMatches) {
                return this.cancelAfter != 0 && numMatches >= this.cancelAfter;
            }

            private void cancelQuery(Association as) {
                try {
                    this.cancel(as);
                    this.cancelAfter = 0;
                }
                catch (IOException e) {
                    LOGGER.error("Failed to cancel query", (Throwable)e);
                }
            }
        };
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void onResult(Attributes data) {
        this.state.addDicomRSP(data);
        int numMatches = this.totNumMatches.incrementAndGet();
        if (this.outDir == null) {
            return;
        }
        try {
            this.ensureOutputStreamReady(numMatches);
            this.writeResult(data);
            this.out.flush();
        }
        catch (Exception e) {
            LOGGER.error("Failed to write result", (Throwable)e);
            this.closeOutputStream();
        }
        finally {
            if (!this.catOut) {
                this.closeOutputStream();
            }
        }
    }

    private void ensureOutputStreamReady(int numMatches) throws IOException {
        if (this.out == null) {
            Files.createDirectories(this.outDir, new FileAttribute[0]);
            Path outputFile = this.outDir.resolve(this.generateFileName(numMatches));
            this.out = new BufferedOutputStream(Files.newOutputStream(outputFile, new OpenOption[0]));
        }
    }

    private void writeResult(Attributes data) throws Exception {
        if (this.xml) {
            this.writeAsXML(data, this.out);
        } else {
            DicomOutputStream dos = new DicomOutputStream(this.out, "1.2.840.10008.1.2");
            dos.writeDataset(null, data);
        }
    }

    private void closeOutputStream() {
        SafeClose.close(this.out);
        this.out = null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String generateFileName(int sequenceNumber) {
        DecimalFormat formatter = this.outFileFormat;
        if (formatter == null) {
            return String.format("%07d", sequenceNumber);
        }
        DecimalFormat decimalFormat = formatter;
        synchronized (decimalFormat) {
            return formatter.format(sequenceNumber);
        }
    }

    private void writeAsXML(Attributes attrs, OutputStream out) throws Exception {
        TransformerHandler th = this.getTransformerHandler();
        th.getTransformer().setOutputProperty("indent", this.xmlIndent ? "yes" : "no");
        th.setResult(new StreamResult(out));
        SAXWriter saxWriter = new SAXWriter(th);
        saxWriter.setIncludeKeyword(this.xmlIncludeKeyword);
        saxWriter.setIncludeNamespaceDeclaration(this.xmlIncludeNamespaceDeclaration);
        saxWriter.write(attrs);
    }

    private TransformerHandler getTransformerHandler() throws Exception {
        SAXTransformerFactory tf = this.getOrCreateSaxTransformerFactory();
        return this.xsltTpls != null ? tf.newTransformerHandler(this.xsltTpls) : tf.newTransformerHandler();
    }

    private SAXTransformerFactory getOrCreateSaxTransformerFactory() throws Exception {
        if (this.saxtf == null) {
            this.saxtf = (SAXTransformerFactory)TransformerFactory.newInstance();
            this.saxtf.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        }
        if (this.xsltFile != null && this.xsltTpls == null) {
            this.xsltTpls = this.saxtf.newTemplates(new StreamSource(this.xsltFile.toFile()));
        }
        return this.saxtf;
    }

    static void mergeKeys(Attributes attrs, Attributes keys) {
        try {
            attrs.accept(new MergeNested(keys), false);
        }
        catch (Exception e) {
            throw new IllegalStateException("Failed to merge keys", e);
        }
        attrs.addAll(keys);
    }

    public static enum InformationModel {
        PatientRoot("1.2.840.10008.5.1.4.1.2.1.1", "STUDY"),
        StudyRoot("1.2.840.10008.5.1.4.1.2.2.1", "STUDY"),
        PatientStudyOnly("1.2.840.10008.5.1.4.1.2.3.1", "STUDY"),
        MWL("1.2.840.10008.5.1.4.31", null),
        UPSPull("1.2.840.10008.5.1.4.34.6.3", null),
        UPSWatch("1.2.840.10008.5.1.4.34.6.2", null),
        UPSQuery("1.2.840.10008.5.1.4.34.6.5", null),
        HangingProtocol("1.2.840.10008.5.1.4.38.2", null),
        ColorPalette("1.2.840.10008.5.1.4.39.2", null);

        final String cuid;
        final String level;

        private InformationModel(String cuid, String level) {
            this.cuid = cuid;
            this.level = level;
        }

        public void adjustQueryOptions(EnumSet<QueryOption> queryOptions) {
            if (this.level == null) {
                queryOptions.add(QueryOption.RELATIONAL);
                queryOptions.add(QueryOption.DATETIME);
            }
        }

        public String getCuid() {
            return this.cuid;
        }
    }

    private static class MergeNested
    implements Attributes.Visitor {
        private final Attributes keys;

        MergeNested(Attributes keys) {
            this.keys = keys;
        }

        @Override
        public boolean visit(Attributes attrs, int tag, VR vr, Object val) {
            Object keyValue;
            if (MergeNested.isNotEmptySequence(val) && MergeNested.isNotEmptySequence(keyValue = this.keys.remove(tag))) {
                ((Attributes)((Sequence)val).get(0)).addAll((Attributes)((Sequence)keyValue).get(0));
            }
            return true;
        }

        private static boolean isNotEmptySequence(Object val) {
            Sequence sequence;
            return val instanceof Sequence && !(sequence = (Sequence)val).isEmpty();
        }
    }
}

