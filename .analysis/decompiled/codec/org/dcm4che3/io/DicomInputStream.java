/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.dcm4che3.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.ItemPointer;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.BulkDataCreator;
import org.dcm4che3.io.BulkDataDescriptor;
import org.dcm4che3.io.DicomInputHandler;
import org.dcm4che3.io.DicomStreamException;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.LimitedInputStream;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DicomInputStream
extends FilterInputStream
implements DicomInputHandler,
BulkDataCreator {
    private static final Logger LOG = LoggerFactory.getLogger(DicomInputStream.class);
    private static final String UNEXPECTED_NON_ZERO_ITEM_LENGTH = "Unexpected item value of {} #{} @ {} during {}";
    private static final String UNEXPECTED_ATTRIBUTE = "Unexpected attribute {} #{} @ {} during {}";
    private static final String MISSING_TRANSFER_SYNTAX = "Missing Transfer Syntax (0002,0010) - assume Explicit VR Little Endian";
    private static final String MISSING_FMI_LENGTH = "Missing or wrong File Meta Information Group Length (0002,0000)";
    private static final String NOT_A_DICOM_STREAM = "Not a DICOM Stream";
    private static final String IMPLICIT_VR_BIG_ENDIAN = "Implicit VR Big Endian encoded DICOM Stream";
    private static final String DEFLATED_WITH_ZLIB_HEADER = "Deflated DICOM Stream with ZLIB Header";
    private static final String SEQUENCE_EXCEED_ENCODED_LENGTH = "Actual length of Sequence %s exceeds encoded length: %d";
    private static final String TREAT_SQ_AS_UN = "Actual length of Sequence {} exceeds encoded length: {} - treat as UN";
    private static final int TREAT_SQ_AS_UN_MAX_EXCEED_LENGTH = 1024;
    static final String VALUE_TOO_LARGE = "tag value too large, must be less than 2Gib";
    private static final int ZLIB_HEADER = 30876;
    private static final int DEF_ALLOCATE_LIMIT = 0x4000000;
    private static final int DEFAULT_PREAMBLE_LENGTH = 128;
    private static final int UNDEFINED_LENGTH = -1;
    private static final int BYTE_BUF_LENGTH = 8192;
    private byte[] byteBuf;
    private int allocateLimit = 0x4000000;
    private String uri;
    private String tsuid;
    private byte[] preamble;
    private Attributes fileMetaInformation;
    private boolean hasfmi;
    private boolean bigEndian;
    private boolean explicitVR;
    private IncludeBulkData includeBulkData = IncludeBulkData.YES;
    private long pos;
    private long fmiEndPos = -1L;
    private long tagPos;
    private long markPos;
    private int tag;
    private VR vr;
    private int encodedVR;
    private long length;
    private DicomInputHandler handler = this;
    private BulkDataCreator bulkDataCreator = this;
    private BulkDataDescriptor bulkDataDescriptor = BulkDataDescriptor.DEFAULT;
    private final byte[] buffer = new byte[12];
    private List<ItemPointer> itemPointers = new ArrayList<ItemPointer>(4);
    private boolean excludeBulkData;
    private boolean includeBulkDataURI;
    private boolean catBlkFiles = true;
    private String blkFilePrefix = "blk";
    private String blkFileSuffix;
    private File blkDirectory;
    private ArrayList<File> blkFiles;
    private String blkURI;
    private FileOutputStream blkOut;
    private long blkOutPos;
    private Inflater inflater;

    public DicomInputStream(InputStream in, String tsuid) throws IOException {
        super(in);
        this.switchTransferSyntax(tsuid);
    }

    public DicomInputStream(InputStream in) throws IOException {
        this(in, 128);
    }

    public DicomInputStream(InputStream in, int preambleLength) throws IOException {
        super(DicomInputStream.ensureMarkSupported(in));
        this.guessTransferSyntax(preambleLength);
    }

    public DicomInputStream(File file) throws IOException {
        super(new BufferedInputStream(new FileInputStream(file)));
        try {
            this.guessTransferSyntax(128);
        }
        catch (IOException e) {
            SafeClose.close(this.in);
            throw e;
        }
        this.uri = file.toURI().toString();
    }

    public static void parseUNSequence(byte[] buf, Attributes attrs, int sqtag) throws IOException {
        DicomInputStream dis = new DicomInputStream((InputStream)new ByteArrayInputStream(buf), attrs.bigEndian() ? "1.2.840.10008.1.2.2" : "1.2.840.10008.1.2.1");
        dis.encodedVR = 21838;
        dis.readSequence(buf.length, attrs, sqtag);
    }

    public static DicomInputStream createWithLimit(InputStream in, String tsuid, long limit) throws IOException {
        return new DicomInputStream((InputStream)DicomInputStream.limited(DicomInputStream.ensureMarkSupported(in), limit), tsuid);
    }

    public static DicomInputStream createWithLimit(InputStream in, long limit) throws IOException {
        return new DicomInputStream(DicomInputStream.limited(DicomInputStream.ensureMarkSupported(in), limit));
    }

    public static DicomInputStream createWithLimitFromFileLength(File file) throws IOException {
        long fileLength = file.length();
        if (fileLength > 0L) {
            DicomInputStream dicomInputStream;
            LimitedInputStream in = DicomInputStream.limited(new BufferedInputStream(new FileInputStream(file)), fileLength);
            try {
                dicomInputStream = new DicomInputStream(in);
            }
            catch (IOException e) {
                SafeClose.close(in);
                throw e;
            }
            dicomInputStream.setURI(file.toURI().toString());
            return dicomInputStream;
        }
        return new DicomInputStream(file);
    }

    private static InputStream ensureMarkSupported(InputStream in) {
        return in.markSupported() ? in : new BufferedInputStream(in);
    }

    private static LimitedInputStream limited(InputStream in, long limit) {
        return new LimitedInputStream(in, limit, true);
    }

    public final String getTransferSyntax() {
        return this.tsuid;
    }

    public final int getAllocateLimit() {
        return this.allocateLimit;
    }

    public final void setAllocateLimit(int allocateLimit) {
        if (allocateLimit <= 0 && allocateLimit != -1) {
            throw new IllegalArgumentException("allocateLimit must be a positive number or -1");
        }
        this.allocateLimit = allocateLimit;
    }

    public final String getURI() {
        return this.uri;
    }

    public final void setURI(String uri) {
        this.uri = uri;
    }

    public final IncludeBulkData getIncludeBulkData() {
        return this.includeBulkData;
    }

    public final void setIncludeBulkData(IncludeBulkData includeBulkData) {
        if (includeBulkData == null) {
            throw new NullPointerException();
        }
        this.includeBulkData = includeBulkData;
    }

    public final BulkDataDescriptor getBulkDataDescriptor() {
        return this.bulkDataDescriptor;
    }

    public final void setBulkDataDescriptor(BulkDataDescriptor bulkDataDescriptor) {
        this.bulkDataDescriptor = bulkDataDescriptor;
    }

    public final String getBulkDataFilePrefix() {
        return this.blkFilePrefix;
    }

    public final void setBulkDataFilePrefix(String blkFilePrefix) {
        this.blkFilePrefix = blkFilePrefix;
    }

    public final String getBulkDataFileSuffix() {
        return this.blkFileSuffix;
    }

    public final void setBulkDataFileSuffix(String blkFileSuffix) {
        this.blkFileSuffix = blkFileSuffix;
    }

    public final File getBulkDataDirectory() {
        return this.blkDirectory;
    }

    public final void setBulkDataDirectory(File blkDirectory) {
        this.blkDirectory = blkDirectory;
    }

    public final boolean isConcatenateBulkDataFiles() {
        return this.catBlkFiles;
    }

    public final void setConcatenateBulkDataFiles(boolean catBlkFiles) {
        this.catBlkFiles = catBlkFiles;
    }

    public final List<File> getBulkDataFiles() {
        if (this.blkFiles != null) {
            return this.blkFiles;
        }
        return Collections.emptyList();
    }

    public final void setDicomInputHandler(DicomInputHandler handler) {
        if (handler == null) {
            throw new NullPointerException("handler");
        }
        this.handler = handler;
    }

    public final void setSkipAllDicomInputHandler() {
        this.handler = new DicomInputHandler(){

            @Override
            public void readValue(DicomInputStream dis, Attributes attrs) throws IOException {
                if (dis.length() == -1) {
                    dis.skipSequence();
                } else {
                    long n = dis.unsignedLength();
                    StreamUtils.skipFully(dis, n);
                }
            }

            @Override
            public void readValue(DicomInputStream dis, Sequence seq) throws IOException {
                dis.readValue(dis, seq);
            }

            @Override
            public void readValue(DicomInputStream dis, Fragments frags) throws IOException {
                long n = dis.unsignedLength();
                StreamUtils.skipFully(dis, n);
            }

            @Override
            public void startDataset(DicomInputStream dis) throws IOException {
            }

            @Override
            public void endDataset(DicomInputStream dis) throws IOException {
            }
        };
    }

    public void setBulkDataCreator(BulkDataCreator bulkDataCreator) {
        if (bulkDataCreator == null) {
            throw new NullPointerException("bulkDataCreator");
        }
        this.bulkDataCreator = bulkDataCreator;
    }

    public final void setFileMetaInformationGroupLength(byte[] val) {
        this.fmiEndPos = this.pos + (long)ByteUtils.bytesToInt(val, 0, this.bigEndian);
    }

    public final byte[] getPreamble() {
        return this.preamble;
    }

    public Attributes getFileMetaInformation() throws IOException {
        this.readFileMetaInformation();
        return this.fileMetaInformation;
    }

    public final int level() {
        return this.itemPointers.size();
    }

    public final int tag() {
        return this.tag;
    }

    public final VR vr() {
        return this.vr;
    }

    public final int length() {
        return (int)this.length;
    }

    public long unsignedLength() {
        return this.length;
    }

    public final long getPosition() {
        return this.pos;
    }

    public void setPosition(long pos) {
        this.pos = pos;
    }

    public long getTagPosition() {
        return this.tagPos;
    }

    public final boolean bigEndian() {
        return this.bigEndian;
    }

    public final boolean explicitVR() {
        return this.explicitVR;
    }

    public boolean isExcludeBulkData() {
        return this.excludeBulkData;
    }

    public boolean isIncludeBulkDataURI() {
        return this.includeBulkDataURI;
    }

    public static String toAttributePath(List<ItemPointer> itemPointers, int tag) {
        StringBuilder sb = new StringBuilder();
        for (ItemPointer itemPointer : itemPointers) {
            sb.append('/').append(TagUtils.toHexString(itemPointer.sequenceTag)).append('/').append(itemPointer.itemIndex);
        }
        sb.append('/').append(TagUtils.toHexString(tag));
        return sb.toString();
    }

    public String getAttributePath() {
        return DicomInputStream.toAttributePath(this.itemPointers, this.tag);
    }

    @Override
    public void close() throws IOException {
        SafeClose.close(this.blkOut);
        if (this.inflater != null) {
            this.inflater.end();
        }
        super.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        super.mark(readlimit);
        this.markPos = this.pos;
    }

    @Override
    public synchronized void reset() throws IOException {
        super.reset();
        this.pos = this.markPos;
    }

    @Override
    public final int read() throws IOException {
        int read = super.read();
        if (read >= 0) {
            ++this.pos;
        }
        return read;
    }

    @Override
    public final int read(byte[] b, int off, int len) throws IOException {
        int read = super.read(b, off, len);
        if (read > 0) {
            this.pos += (long)read;
        }
        return read;
    }

    @Override
    public final int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public final long skip(long n) throws IOException {
        long skip = super.skip(n);
        this.pos += skip;
        return skip;
    }

    public void skipFully(long n) throws IOException {
        StreamUtils.skipFully(this, n);
    }

    public void readFully(byte[] b) throws IOException {
        this.readFully(b, 0, b.length);
    }

    public void readFully(byte[] b, int off, int len) throws IOException {
        StreamUtils.readFully(this, b, off, len);
    }

    public void readFully(short[] s, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > s.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > s.length!");
        }
        if (this.byteBuf == null) {
            this.byteBuf = new byte[8192];
        }
        while (len > 0) {
            int nelts = Math.min(len, this.byteBuf.length / 2);
            this.readFully(this.byteBuf, 0, nelts * 2);
            ByteUtils.bytesToShorts(this.byteBuf, s, off, nelts, this.bigEndian);
            off += nelts;
            len -= nelts;
        }
    }

    private DicomStreamException tagValueTooLargeException() {
        return new DicomStreamException(String.format("0x%s %s", TagUtils.toHexString(this.tag), VALUE_TOO_LARGE));
    }

    public void readHeader() throws IOException {
        this.readHeader(dis -> false);
    }

    public void readHeader(Predicate<DicomInputStream> stopPredicate) throws IOException {
        byte[] buf = this.buffer;
        this.tagPos = this.pos;
        this.readFully(buf, 0, 8);
        this.encodedVR = 0;
        this.tag = ByteUtils.bytesToTag(buf, 0, this.bigEndian);
        switch (this.tag) {
            case -73728: 
            case -73715: 
            case -73507: {
                this.vr = null;
                break;
            }
            default: {
                if (this.explicitVR) {
                    this.encodedVR = ByteUtils.bytesToVR(buf, 4);
                    this.vr = VR.valueOf(this.encodedVR);
                    if (this.vr == null) {
                        this.vr = ElementDictionary.getStandardElementDictionary().vrOf(this.tag);
                        if (!stopPredicate.test(this)) {
                            LOG.warn("Unrecognized VR code: {}H for {} - treat as {}", new Object[]{TagUtils.shortToHexString(this.encodedVR), TagUtils.toString(this.tag), this.vr});
                        }
                    }
                    if (this.vr.headerLength() == 8) {
                        this.length = ByteUtils.bytesToUShort(buf, 6, this.bigEndian);
                        return;
                    }
                    this.readFully(buf, 4, 4);
                    break;
                }
                this.vr = VR.UN;
            }
        }
        this.length = DicomInputStream.toLongOrUndefined(ByteUtils.bytesToInt(buf, 4, this.bigEndian));
    }

    static long toLongOrUndefined(int length) {
        return length == -1 ? (long)length : (long)length & 0xFFFFFFFFL;
    }

    public boolean readItemHeader() throws IOException {
        String methodName = "readItemHeader()";
        while (true) {
            this.readHeader();
            if (this.tag == -73728) {
                return true;
            }
            if (this.tag == -73507) {
                if (this.length != 0L) {
                    this.skipAttribute(UNEXPECTED_NON_ZERO_ITEM_LENGTH, methodName);
                }
                return false;
            }
            this.skipAttribute(UNEXPECTED_ATTRIBUTE, methodName);
        }
    }

    public Attributes readCommand() throws IOException {
        if (this.bigEndian || this.explicitVR) {
            throw new IllegalStateException("bigEndian=" + this.bigEndian + ", explicitVR=" + this.explicitVR);
        }
        Attributes attrs = new Attributes(9);
        this.readAllAttributes(attrs);
        return attrs;
    }

    public void readAllAttributes(Attributes attrs) throws IOException {
        this.readAttributes(attrs, -1L, o -> false);
    }

    public Attributes readDataset() throws IOException {
        return this.readDataset(o -> false);
    }

    public Attributes readDatasetUntilPixelData() throws IOException {
        return this.readDataset(o -> o.tag == 2145386512);
    }

    @Deprecated
    public Attributes readDataset(int len, int stopTag) throws IOException {
        return this.readDataset((long)len, DicomInputStream.tagEqualOrGreater(stopTag));
    }

    public Attributes readDataset(int stopTag) throws IOException {
        return this.readDataset(DicomInputStream.tagEqualOrGreater(stopTag));
    }

    public Attributes readDataset(Predicate<DicomInputStream> stopPredicate) throws IOException {
        return this.readDataset(-1L, stopPredicate);
    }

    @Deprecated
    public Attributes readDataset(long len, Predicate<DicomInputStream> stopPredicate) throws IOException {
        this.handler.startDataset(this);
        this.readFileMetaInformation();
        Attributes attrs = new Attributes(this.bigEndian, 64);
        this.readAttributes(attrs, len, stopPredicate);
        attrs.trimToSize();
        this.handler.endDataset(this);
        return attrs;
    }

    public Attributes readFileMetaInformation() throws IOException {
        if (!this.hasfmi) {
            return null;
        }
        if (this.fileMetaInformation != null) {
            return this.fileMetaInformation;
        }
        Attributes attrs = new Attributes(this.bigEndian, 9);
        while (this.pos != this.fmiEndPos) {
            this.mark(12);
            this.readHeader();
            if (TagUtils.groupNumber(this.tag) != 2) {
                LOG.warn(MISSING_FMI_LENGTH);
                this.reset();
                break;
            }
            if (this.vr != null) {
                if (this.vr == VR.UN) {
                    this.vr = ElementDictionary.getStandardElementDictionary().vrOf(this.tag);
                }
                this.handler.readValue(this, attrs);
                continue;
            }
            this.skipAttribute(UNEXPECTED_ATTRIBUTE, "readFileMetaInformation()");
        }
        this.fileMetaInformation = attrs;
        String tsuid = attrs.getString(131088, null);
        if (tsuid == null) {
            LOG.warn(MISSING_TRANSFER_SYNTAX);
            tsuid = "1.2.840.10008.1.2.1";
        }
        this.switchTransferSyntax(tsuid);
        return attrs;
    }

    public void readAttributes(Attributes attrs, long len, int stopTag) throws IOException {
        this.readAttributes(attrs, len, DicomInputStream.tagEqualOrGreater(stopTag));
    }

    private static Predicate<DicomInputStream> tagEqualOrGreater(int stopTag) {
        return stopTag != -1 ? o -> Integer.compareUnsigned(o.tag, stopTag) >= 0 : o -> false;
    }

    public void readAttributes(Attributes attrs, long len, Predicate<DicomInputStream> stopPredicate) throws IOException {
        boolean undeflen = len == -1L;
        long endPos = this.pos + (len & 0xFFFFFFFFL);
        while (undeflen || this.pos < endPos) {
            try {
                this.readHeader(stopPredicate);
            }
            catch (EOFException e) {
                if (undeflen && this.pos == this.tagPos) break;
                throw e;
            }
            if (stopPredicate.test(this)) break;
            if (this.vr != null) {
                if (this.vr == VR.UN) {
                    switch (this.tag) {
                        case 2621700: 
                        case 2621701: 
                        case 2621702: 
                        case 2621703: 
                        case 2621704: 
                        case 2621705: 
                        case 2621712: 
                        case 2621713: 
                        case 2621728: 
                        case 2621729: 
                        case 2625792: 
                        case 2625793: 
                        case 2625794: 
                        case 2625795: 
                        case 0x281111: 
                        case 0x281112: 
                        case 2625811: 
                        case 4231697: 
                        case 4231702: 
                        case 6303748: 
                        case 0x603006: {
                            this.vr = attrs.getRoot().getInt(2621699, 0) == 0 ? VR.US : VR.SS;
                            break;
                        }
                        case 4235632: {
                            this.vr = this.probeObservationClass() ? VR.CS : VR.SQ;
                            break;
                        }
                        default: {
                            this.vr = ElementDictionary.vrOf(this.tag, attrs.getPrivateCreator(this.tag));
                            if (this.vr != VR.UN || this.length != -1L) break;
                            this.vr = VR.SQ;
                        }
                    }
                }
                this.excludeBulkData = this.includeBulkData == IncludeBulkData.NO && this.isBulkData(attrs);
                this.includeBulkDataURI = this.length != 0L && this.vr != VR.SQ && this.includeBulkData == IncludeBulkData.URI && this.isBulkData(attrs);
                this.handler.readValue(this, attrs);
                continue;
            }
            this.skipAttribute(UNEXPECTED_ATTRIBUTE, "readAttributes()");
        }
    }

    private boolean probeObservationClass() {
        return !this.itemPointers.isEmpty() && this.itemPointers.get((int)0).sequenceTag == 4235296;
    }

    @Override
    public void readValue(DicomInputStream dis, Attributes attrs) throws IOException {
        this.checkIsThis(dis);
        if (this.excludeBulkData) {
            if (this.length == -1L) {
                this.skipSequence();
            } else {
                this.skipFully(this.length);
            }
        } else if (this.length == 0L) {
            attrs.setNull(this.tag, this.vr);
        } else if (this.vr == VR.SQ) {
            this.readSequence(this.length, attrs, this.tag);
        } else if (this.length == -1L) {
            this.readFragments(attrs, this.tag, this.vr);
        } else if (this.length == 64507L && this.in instanceof ObjectInputStream) {
            attrs.setValue(this.tag, this.vr, this.deserializeBulkData((ObjectInputStream)this.in));
        } else if (this.includeBulkDataURI) {
            attrs.setValue(this.tag, this.vr, this.bulkDataCreator.createBulkData(this));
        } else {
            byte[] b = this.readValue();
            if (!TagUtils.isGroupLength(this.tag)) {
                if (this.bigEndian != attrs.bigEndian()) {
                    this.vr.toggleEndian(b, false);
                }
                attrs.setBytes(this.tag, this.vr, b);
            } else if (this.tag == 131072) {
                this.setFileMetaInformationGroupLength(b);
            }
        }
    }

    private Object deserializeBulkData(ObjectInputStream ois) throws IOException {
        try {
            return ois.readObject();
        }
        catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    public void skipSequence() throws IOException {
        while (this.readItemHeader()) {
            this.skipItem();
        }
    }

    public void skipItem() throws IOException {
        if (this.length == -1L) {
            while (true) {
                this.readHeader();
                if (this.length == -1L) {
                    this.skipSequence();
                    continue;
                }
                this.skipFully(this.length);
                if (this.tag == -73715) break;
            }
        } else {
            this.skipFully(this.length);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public BulkData createBulkData(DicomInputStream dis) throws IOException {
        BulkData bulkData;
        if (this.uri != null && !(this.in instanceof InflaterInputStream)) {
            bulkData = new BulkData(this.uri, this.pos, this.length, this.bigEndian);
            this.skipFully(this.length);
        } else {
            if (this.blkOut == null) {
                File blkfile = File.createTempFile(this.blkFilePrefix, this.blkFileSuffix, this.blkDirectory);
                if (this.blkFiles == null) {
                    this.blkFiles = new ArrayList();
                }
                this.blkFiles.add(blkfile);
                this.blkURI = blkfile.toURI().toString();
                this.blkOut = new FileOutputStream(blkfile);
                this.blkOutPos = 0L;
            }
            try {
                StreamUtils.copy((InputStream)this, (OutputStream)this.blkOut, this.length);
            }
            finally {
                if (!this.catBlkFiles) {
                    SafeClose.close(this.blkOut);
                    this.blkOut = null;
                }
            }
            bulkData = new BulkData(this.blkURI, this.blkOutPos, this.length, this.bigEndian);
            this.blkOutPos += this.length;
        }
        return bulkData;
    }

    private boolean isBulkData(Attributes attrs) {
        return this.bulkDataDescriptor.isBulkData(this.itemPointers, attrs.getPrivateCreator(this.tag), this.tag, this.vr, (int)this.length);
    }

    @Override
    public void readValue(DicomInputStream dis, Sequence seq) throws IOException {
        this.checkIsThis(dis);
        if (this.length == 0L) {
            seq.add(new Attributes(seq.getParent().bigEndian(), 0));
            return;
        }
        Attributes attrs = new Attributes(seq.getParent().bigEndian());
        seq.add(attrs);
        this.readItemValue(attrs, this.length);
        attrs.trimToSize();
    }

    @Override
    public void readValue(DicomInputStream dis, Fragments frags) throws IOException {
        this.checkIsThis(dis);
        if (this.excludeBulkData) {
            this.skipFully(this.length);
        } else if (this.length == 0L) {
            frags.add(ByteUtils.EMPTY_BYTES);
        } else if (this.length == 64507L && this.in instanceof ObjectInputStream) {
            frags.add(this.deserializeBulkData((ObjectInputStream)this.in));
        } else if (this.includeBulkDataURI) {
            frags.add(this.bulkDataCreator.createBulkData(this));
        } else {
            byte[] b = this.readValue();
            if (this.bigEndian != frags.bigEndian()) {
                frags.vr().toggleEndian(b, false);
            }
            frags.add(b);
        }
    }

    @Override
    public void startDataset(DicomInputStream dis) {
    }

    @Override
    public void endDataset(DicomInputStream dis) {
    }

    private void checkIsThis(DicomInputStream dis) {
        if (dis != this) {
            throw new IllegalArgumentException("dis != this");
        }
    }

    private void skipAttribute(String message, String methodName) throws IOException {
        String tagAsString = TagUtils.toString(this.tag);
        LOG.warn(message, new Object[]{tagAsString, this.length, this.tagPos, methodName});
        this.skipFully(this.length);
    }

    private void readSequence(long len, Attributes attrs, int sqtag) throws IOException {
        boolean recoverSequenceExceedsEncodedLength;
        if (len == 0L) {
            attrs.setNull(sqtag, VR.SQ);
            return;
        }
        Sequence seq = attrs.newSequence(sqtag, 10);
        String privateCreator = attrs.getPrivateCreator(sqtag);
        boolean undefLen = len == -1L;
        long endPos = this.pos + (len & 0xFFFFFFFFL);
        boolean explicitVR0 = this.explicitVR;
        boolean bigEndian0 = this.bigEndian;
        if (this.encodedVR == 21838 && !this.probeExplicitVR()) {
            this.explicitVR = false;
            this.bigEndian = false;
        }
        boolean bl = recoverSequenceExceedsEncodedLength = !undefLen && this.markSupported() && len < (long)this.allocateLimit;
        if (recoverSequenceExceedsEncodedLength) {
            this.mark((int)len + 1024);
        }
        int i = 0;
        while ((undefLen || this.pos < endPos) && this.readItemHeader()) {
            this.addItemPointer(sqtag, privateCreator, i);
            this.handler.readValue(this, seq);
            this.removeItemPointer();
            ++i;
        }
        this.explicitVR = explicitVR0;
        this.bigEndian = bigEndian0;
        if (seq.isEmpty()) {
            attrs.setNull(sqtag, VR.SQ);
        } else if (!undefLen && this.pos != endPos) {
            if (!recoverSequenceExceedsEncodedLength || this.pos - endPos > 1024L) {
                throw new DicomStreamException(String.format(SEQUENCE_EXCEED_ENCODED_LENGTH, TagUtils.toString(sqtag), len));
            }
            LOG.info(TREAT_SQ_AS_UN, (Object)TagUtils.toString(sqtag), (Object)len);
            this.reset();
            this.tag = sqtag;
            this.vr = VR.UN;
            this.length = len;
            this.handler.readValue(this, attrs);
        } else {
            seq.trimToSize();
        }
    }

    private boolean probeExplicitVR() throws IOException {
        byte[] buf = new byte[14];
        if (this.in.markSupported()) {
            this.in.mark(14);
            this.in.read(buf);
            this.in.reset();
        } else {
            if (!(this.in instanceof PushbackInputStream)) {
                this.in = new PushbackInputStream(this.in, 14);
            }
            int len = this.in.read(buf);
            ((PushbackInputStream)this.in).unread(buf, 0, len);
        }
        return VR.valueOf(ByteUtils.bytesToVR(buf, 12)) != null;
    }

    private void addItemPointer(int sqtag, String privateCreator, int itemIndex) {
        this.itemPointers.add(new ItemPointer(privateCreator, sqtag, itemIndex));
    }

    private void removeItemPointer() {
        this.itemPointers.remove(this.itemPointers.size() - 1);
    }

    public Attributes readItem() throws IOException {
        this.readHeader();
        if (this.tag != -73728) {
            throw new IOException("Unexpected attribute " + TagUtils.toString(this.tag) + " #" + this.length + " @ " + this.pos);
        }
        Attributes attrs = new Attributes(this.bigEndian);
        attrs.setItemPosition(this.tagPos);
        this.readItemValue(attrs, this.length);
        attrs.trimToSize();
        return attrs;
    }

    public void readItemValue(Attributes attrs, long length) throws IOException {
        this.readAttributes(attrs, length, dis -> dis.tag == -73715);
    }

    private void readFragments(Attributes attrs, int fragsTag, VR vr) throws IOException {
        Fragments frags = new Fragments(vr, attrs.bigEndian(), 10);
        String privateCreator = attrs.getPrivateCreator(fragsTag);
        int i = 0;
        while (this.readItemHeader()) {
            this.addItemPointer(fragsTag, privateCreator, i);
            this.handler.readValue(this, frags);
            this.removeItemPointer();
            ++i;
        }
        if (frags.isEmpty()) {
            attrs.setNull(fragsTag, vr);
        } else {
            frags.trimToSize();
            attrs.setValue(fragsTag, vr, frags);
        }
    }

    public byte[] readValue() throws IOException {
        int valLen = (int)this.length;
        if (valLen < 0) {
            throw this.tagValueTooLargeException();
        }
        try {
            boolean limitedStream = this.in instanceof LimitedInputStream;
            if (limitedStream && (long)valLen > ((LimitedInputStream)this.in).getRemaining()) {
                throw new EOFException("Length " + valLen + " for tag " + TagUtils.toString(this.tag) + " @ " + this.tagPos + " exceeds remaining " + ((LimitedInputStream)this.in).getRemaining() + " (pos: " + this.pos + ")");
            }
            int allocLen = this.allocateLimit != -1 && !limitedStream ? Math.min(valLen, this.allocateLimit) : valLen;
            byte[] value = new byte[allocLen];
            this.readFully(value, 0, allocLen);
            while (allocLen < valLen) {
                int newLength = allocLen << 1;
                if (newLength <= 0) {
                    newLength = Integer.MAX_VALUE;
                }
                if (newLength > valLen) {
                    newLength = valLen;
                }
                value = Arrays.copyOf(value, newLength);
                this.readFully(value, allocLen, newLength - allocLen);
                allocLen = newLength;
            }
            return value;
        }
        catch (IOException e) {
            LOG.warn("IOException during read of {} #{} @ {}", new Object[]{TagUtils.toString(this.tag), this.length, this.tagPos, e});
            throw e;
        }
    }

    private void switchTransferSyntax(String tsuid) throws IOException {
        this.tsuid = tsuid;
        this.bigEndian = tsuid.equals("1.2.840.10008.1.2.2");
        boolean bl = this.explicitVR = !tsuid.equals("1.2.840.10008.1.2");
        if (tsuid.equals("1.2.840.10008.1.2.1.99") || tsuid.equals("1.2.840.10008.1.2.4.95") || tsuid.equals("1.2.840.10008.1.2.4.205")) {
            if (this.hasZLIBHeader()) {
                LOG.warn(DEFLATED_WITH_ZLIB_HEADER);
                this.in = new InflaterInputStream(this.in);
            } else {
                this.inflater = new Inflater(true);
                this.in = new InflaterInputStream(this.in, this.inflater);
            }
        }
    }

    private boolean hasZLIBHeader() throws IOException {
        if (!this.markSupported()) {
            return false;
        }
        byte[] buf = this.buffer;
        this.mark(2);
        this.read(buf, 0, 2);
        this.reset();
        return ByteUtils.bytesToUShortBE(buf, 0) == 30876;
    }

    private void guessTransferSyntax(int preambleLength) throws IOException {
        byte[] b134 = new byte[preambleLength + 6];
        this.mark(b134.length);
        int rlen = StreamUtils.readAvailable(this, b134, 0, b134.length);
        if (rlen == b134.length && b134[preambleLength] == 68 && b134[preambleLength + 1] == 73 && b134[preambleLength + 2] == 67 && b134[preambleLength + 3] == 77 && b134[preambleLength + 4] == 2 && b134[preambleLength + 5] == 0) {
            this.preamble = new byte[preambleLength];
            System.arraycopy(b134, 0, this.preamble, 0, preambleLength);
            this.reset();
            StreamUtils.skipFully(this, preambleLength + 4);
            this.mark(b134.length);
            rlen = StreamUtils.readAvailable(this, b134, 0, b134.length);
        }
        if (rlen < 8 || !this.guessTransferSyntax(b134, rlen, false) && !this.guessTransferSyntax(b134, rlen, true)) {
            throw new DicomStreamException(NOT_A_DICOM_STREAM);
        }
        this.reset();
        this.hasfmi = TagUtils.isFileMetaInformation(ByteUtils.bytesToTag(b134, 0, this.bigEndian));
    }

    private boolean guessTransferSyntax(byte[] b132, int rlen, boolean bigEndian) throws DicomStreamException {
        int tag1 = ByteUtils.bytesToTag(b132, 0, bigEndian);
        VR vr = ElementDictionary.vrOf(tag1, null);
        if (vr == VR.UN) {
            return false;
        }
        if (ByteUtils.bytesToVR(b132, 4) == vr.code()) {
            this.tsuid = bigEndian ? "1.2.840.10008.1.2.2" : "1.2.840.10008.1.2.1";
            this.bigEndian = bigEndian;
            this.explicitVR = true;
            return true;
        }
        int len = ByteUtils.bytesToInt(b132, 4, bigEndian);
        if (len < 0 || 8 + len > rlen) {
            return false;
        }
        if (bigEndian) {
            throw new DicomStreamException(IMPLICIT_VR_BIG_ENDIAN);
        }
        this.tsuid = "1.2.840.10008.1.2";
        this.bigEndian = false;
        this.explicitVR = false;
        return true;
    }

    public static enum IncludeBulkData {
        NO,
        YES,
        URI;

    }
}

