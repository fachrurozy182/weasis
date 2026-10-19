/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.media;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.RAFInputStreamAdapter;
import org.dcm4che3.media.RecordFactory;
import org.dcm4che3.media.RecordType;
import org.dcm4che3.util.IntHashMap;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StringUtils;

public class DicomDirReader
implements Closeable {
    protected final File file;
    protected final RandomAccessFile raf;
    protected final DicomInputStream in;
    protected final Attributes fmi;
    protected final Attributes fsInfo;
    protected final IntHashMap<Attributes> cache = new IntHashMap();

    public DicomDirReader(File file) throws IOException {
        this(file, "r");
    }

    protected DicomDirReader(File file, String mode) throws IOException {
        this.file = file;
        this.raf = new RandomAccessFile(file, mode);
        try {
            this.in = new DicomInputStream(new RAFInputStreamAdapter(this.raf));
            this.fmi = this.in.readFileMetaInformation();
            this.fsInfo = this.in.readDataset(o -> o.tag() == 266784);
            if (this.in.tag() != 266784) {
                throw new IOException("Missing Directory Record Sequence");
            }
        }
        catch (IOException e) {
            SafeClose.close(this.raf);
            throw e;
        }
    }

    public final File getFile() {
        return this.file;
    }

    public final Attributes getFileMetaInformation() {
        return this.fmi;
    }

    public final Attributes getFileSetInformation() {
        return this.fsInfo;
    }

    @Override
    public void close() throws IOException {
        this.raf.close();
    }

    public String getFileSetUID() {
        return this.fmi.getString(131075, null);
    }

    public String getTransferSyntaxUID() {
        return this.fmi.getString(131088, null);
    }

    public String getFileSetID() {
        return this.fsInfo.getString(266544, null);
    }

    public File getDescriptorFile() {
        return this.toFile(this.fsInfo.getStrings(266561));
    }

    public File toFile(String[] fileIDs) {
        if (fileIDs == null || fileIDs.length == 0) {
            return null;
        }
        return new File(this.file.getParent(), StringUtils.concat(fileIDs, File.separatorChar));
    }

    public String getDescriptorFileCharacterSet() {
        return this.fsInfo.getString(266562, null);
    }

    public int getFileSetConsistencyFlag() {
        return this.fsInfo.getInt(266770, 0);
    }

    protected void setFileSetConsistencyFlag(int i) {
        this.fsInfo.setInt(266770, VR.US, i);
    }

    public boolean knownInconsistencies() {
        return this.getFileSetConsistencyFlag() != 0;
    }

    public int getOffsetOfFirstRootDirectoryRecord() {
        return this.fsInfo.getInt(266752, 0);
    }

    protected void setOffsetOfFirstRootDirectoryRecord(int i) {
        this.fsInfo.setInt(266752, VR.UL, i);
    }

    public int getOffsetOfLastRootDirectoryRecord() {
        return this.fsInfo.getInt(266754, 0);
    }

    protected void setOffsetOfLastRootDirectoryRecord(int i) {
        this.fsInfo.setInt(266754, VR.UL, i);
    }

    public boolean isEmpty() {
        return this.getOffsetOfFirstRootDirectoryRecord() == 0;
    }

    public void clearCache() {
        this.cache.clear();
    }

    public Attributes readFirstRootDirectoryRecord() throws IOException {
        return this.readRecord(this.getOffsetOfFirstRootDirectoryRecord());
    }

    public Attributes readLastRootDirectoryRecord() throws IOException {
        return this.readRecord(this.getOffsetOfLastRootDirectoryRecord());
    }

    public Attributes readNextDirectoryRecord(Attributes rec) throws IOException {
        return this.readRecord(rec.getInt(267264, 0));
    }

    public Attributes readLowerDirectoryRecord(Attributes rec) throws IOException {
        return this.readRecord(rec.getInt(267296, 0));
    }

    protected Attributes findLastLowerDirectoryRecord(Attributes rec) throws IOException {
        Attributes next;
        Attributes lower = this.readLowerDirectoryRecord(rec);
        if (lower == null) {
            return null;
        }
        while ((next = this.readNextDirectoryRecord(lower)) != null) {
            lower = next;
        }
        return lower;
    }

    public Attributes findFirstRootDirectoryRecordInUse(boolean ignorePrivate) throws IOException {
        return this.findRootDirectoryRecord(ignorePrivate, null, false, false);
    }

    public Attributes findRootDirectoryRecord(Attributes keys, boolean ignorePrivate, boolean ignoreCaseOfPN, boolean matchNoValue) throws IOException {
        return this.findRecordInUse(this.getOffsetOfFirstRootDirectoryRecord(), ignorePrivate, keys, ignoreCaseOfPN, matchNoValue);
    }

    public Attributes findRootDirectoryRecord(boolean ignorePrivate, Attributes keys, boolean ignoreCaseOfPN, boolean matchNoValue) throws IOException {
        return this.findRootDirectoryRecord(keys, ignorePrivate, ignoreCaseOfPN, matchNoValue);
    }

    public Attributes findNextDirectoryRecordInUse(Attributes rec, boolean ignorePrivate) throws IOException {
        return this.findNextDirectoryRecord(rec, ignorePrivate, null, false, false);
    }

    public Attributes findNextDirectoryRecord(Attributes rec, boolean ignorePrivate, Attributes keys, boolean ignoreCaseOfPN, boolean matchNoValue) throws IOException {
        return this.findRecordInUse(rec.getInt(267264, 0), ignorePrivate, keys, ignoreCaseOfPN, matchNoValue);
    }

    public Attributes findLowerDirectoryRecordInUse(Attributes rec, boolean ignorePrivate) throws IOException {
        return this.findLowerDirectoryRecord(rec, ignorePrivate, null, false, false);
    }

    public Attributes findLowerDirectoryRecord(Attributes rec, boolean ignorePrivate, Attributes keys, boolean ignoreCaseOfPN, boolean matchNoValue) throws IOException {
        return this.findRecordInUse(rec.getInt(267296, 0), ignorePrivate, keys, ignoreCaseOfPN, matchNoValue);
    }

    public Attributes findPatientRecord(String ... ids) throws IOException {
        return this.findRootDirectoryRecord(false, this.pk("PATIENT", 0x100020, VR.LO, ids), false, false);
    }

    public Attributes findPatientRecord(Attributes keys, RecordFactory recFact, boolean ignoreCaseOfPN, boolean matchNoValue) throws IOException {
        return this.findRootDirectoryRecord(false, this.keys(RecordType.PATIENT, keys, recFact), ignoreCaseOfPN, matchNoValue);
    }

    public Attributes findNextPatientRecord(Attributes patRec, String ... ids) throws IOException {
        return this.findNextDirectoryRecord(patRec, false, this.pk("PATIENT", 0x100020, VR.LO, ids), false, false);
    }

    public Attributes findNextPatientRecord(Attributes patRec, Attributes keys, RecordFactory recFact, boolean ignoreCaseOfPN, boolean matchNoValue) throws IOException {
        return this.findNextDirectoryRecord(patRec, false, this.keys(RecordType.PATIENT, keys, recFact), ignoreCaseOfPN, matchNoValue);
    }

    public Attributes findStudyRecord(Attributes patRec, String ... iuids) throws IOException {
        return this.findLowerDirectoryRecord(patRec, false, this.pk("STUDY", 0x20000D, VR.UI, iuids), false, false);
    }

    public Attributes findStudyRecord(Attributes patRec, Attributes keys, RecordFactory recFact, boolean ignoreCaseOfPN, boolean matchNoValue) throws IOException {
        return this.findLowerDirectoryRecord(patRec, false, this.keys(RecordType.STUDY, keys, recFact), ignoreCaseOfPN, matchNoValue);
    }

    public Attributes findNextStudyRecord(Attributes studyRec, String ... iuids) throws IOException {
        return this.findNextDirectoryRecord(studyRec, false, this.pk("STUDY", 0x20000D, VR.UI, iuids), false, false);
    }

    public Attributes findNextStudyRecord(Attributes studyRec, Attributes keys, RecordFactory recFact, boolean ignoreCaseOfPN, boolean matchNoValue) throws IOException {
        return this.findNextDirectoryRecord(studyRec, false, this.keys(RecordType.STUDY, keys, recFact), ignoreCaseOfPN, matchNoValue);
    }

    public Attributes findSeriesRecord(Attributes studyRec, String ... iuids) throws IOException {
        return this.findLowerDirectoryRecord(studyRec, false, this.pk("SERIES", 0x20000E, VR.UI, iuids), false, false);
    }

    public Attributes findSeriesRecord(Attributes studyRec, Attributes keys, RecordFactory recFact, boolean ignoreCaseOfPN, boolean matchNoValue) throws IOException {
        return this.findLowerDirectoryRecord(studyRec, false, this.keys(RecordType.SERIES, keys, recFact), ignoreCaseOfPN, matchNoValue);
    }

    public Attributes findNextSeriesRecord(Attributes seriesRec, String ... iuids) throws IOException {
        return this.findNextDirectoryRecord(seriesRec, false, this.pk("SERIES", 0x20000E, VR.UI, iuids), false, false);
    }

    public Attributes findNextSeriesRecord(Attributes seriesRec, Attributes keys, RecordFactory recFact, boolean ignoreCaseOfPN, boolean matchNoValue) throws IOException {
        return this.findNextDirectoryRecord(seriesRec, false, this.keys(RecordType.SERIES, keys, recFact), ignoreCaseOfPN, matchNoValue);
    }

    public Attributes findLowerInstanceRecord(Attributes seriesRec, boolean ignorePrivate, String ... iuids) throws IOException {
        return this.findLowerDirectoryRecord(seriesRec, ignorePrivate, this.pk(iuids), false, false);
    }

    public Attributes findLowerInstanceRecord(Attributes seriesRec, Attributes keys, RecordFactory recFact, boolean ignoreCaseOfPN, boolean matchNoValue) throws IOException {
        return this.findLowerDirectoryRecord(seriesRec, false, this.keys(keys, recFact), ignoreCaseOfPN, matchNoValue);
    }

    public Attributes findNextInstanceRecord(Attributes instRec, boolean ignorePrivate, String ... iuids) throws IOException {
        return this.findNextDirectoryRecord(instRec, ignorePrivate, this.pk(iuids), false, false);
    }

    public Attributes findNextInstanceRecord(Attributes instRec, Attributes keys, RecordFactory recFact, boolean ignoreCaseOfPN, boolean matchNoValue) throws IOException {
        return this.findNextDirectoryRecord(instRec, false, this.keys(keys, recFact), ignoreCaseOfPN, matchNoValue);
    }

    public Attributes findRootInstanceRecord(boolean ignorePrivate, String ... iuids) throws IOException {
        return this.findRootDirectoryRecord(ignorePrivate, this.pk(iuids), false, false);
    }

    private Attributes pk(String type, int tag, VR vr, String ... ids) {
        Attributes pk = new Attributes(2);
        pk.setString(267312, VR.CS, type);
        if (ids != null && ids.length != 0) {
            pk.setString(tag, vr, ids);
        }
        return pk;
    }

    private Attributes pk(String ... iuids) {
        if (iuids == null || iuids.length == 0) {
            return null;
        }
        Attributes pk = new Attributes(1);
        pk.setString(267537, VR.UI, iuids);
        return pk;
    }

    private Attributes keys(RecordType type, Attributes attrs, RecordFactory recFact) {
        int[] selection = recFact.getRecordKeys(type);
        Attributes keys = new Attributes(selection.length + 1);
        keys.setString(267312, VR.CS, type.name());
        keys.addSelected(attrs, selection);
        return keys;
    }

    private Attributes keys(Attributes attrs, RecordFactory recFact) {
        int[] selection = recFact.getRecordKeys(RecordType.SR_DOCUMENT);
        Attributes keys = new Attributes(selection.length + 1);
        String[] iuids = attrs.getStrings(524312);
        if (iuids != null && iuids.length > 0) {
            keys.setString(267537, VR.CS, iuids);
        }
        keys.addSelected(attrs, selection);
        return keys;
    }

    private Attributes findRecordInUse(int offset, boolean ignorePrivate, Attributes keys, boolean ignoreCaseOfPN, boolean matchNoValue) throws IOException {
        while (offset != 0) {
            Attributes item = this.readRecord(offset);
            if (!(!DicomDirReader.inUse(item) || ignorePrivate && DicomDirReader.isPrivate(item) || keys != null && !item.matches(keys, ignoreCaseOfPN, matchNoValue))) {
                return item;
            }
            offset = item.getInt(267264, 0);
        }
        return null;
    }

    private synchronized Attributes readRecord(int offset) throws IOException {
        if (offset == 0) {
            return null;
        }
        Attributes item = this.cache.get(offset);
        if (item == null) {
            long off = (long)offset & 0xFFFFFFFFL;
            this.raf.seek(off);
            this.in.setPosition(off);
            item = this.in.readItem();
            this.cache.put(offset, item);
        }
        return item;
    }

    public static boolean inUse(Attributes rec) {
        return rec.getInt(267280, 0) != 0;
    }

    public static boolean isPrivate(Attributes rec) {
        return "PRIVATE".equals(rec.getString(267312));
    }
}

