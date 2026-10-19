/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.dcm4che3.media;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.io.RAFOutputStreamAdapter;
import org.dcm4che3.media.DicomDirReader;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DicomDirWriter
extends DicomDirReader {
    private static final Logger LOG = LoggerFactory.getLogger(DicomDirWriter.class);
    private static final int KNOWN_INCONSISTENCIES = 65535;
    private static final int NO_KNOWN_INCONSISTENCIES = 0;
    private static final int IN_USE = 65535;
    private static final int INACTIVE = 0;
    private final byte[] dirInfoHeader = new byte[]{4, 0, 0, 18, 85, 76, 4, 0, 0, 0, 0, 0, 4, 0, 2, 18, 85, 76, 4, 0, 0, 0, 0, 0, 4, 0, 18, 18, 85, 83, 2, 0, 0, 0, 4, 0, 32, 18, 83, 81, 0, 0, 0, 0, 0, 0};
    private final byte[] dirRecordHeader = new byte[]{4, 0, 0, 20, 85, 76, 4, 0, 0, 0, 0, 0, 4, 0, 16, 20, 85, 83, 2, 0, 0, 0, 4, 0, 32, 20, 85, 76, 4, 0, 0, 0, 0, 0};
    private final DicomOutputStream out;
    private final int firstRecordPos;
    private int nextRecordPos;
    private int rollbackLen = -1;
    private IdentityHashMap<Attributes, Attributes> lastChildRecords = new IdentityHashMap();
    private final ArrayList<Attributes> dirtyRecords = new ArrayList();
    private static final Comparator<Attributes> offsetComparator = new Comparator<Attributes>(){

        @Override
        public int compare(Attributes item1, Attributes item2) {
            long d = item1.getItemPosition() - item2.getItemPosition();
            return d < 0L ? -1 : (d > 0L ? 1 : 0);
        }
    };

    private DicomDirWriter(File file) throws IOException {
        super(file, "rw");
        this.out = new DicomOutputStream(new RAFOutputStreamAdapter(this.raf), super.getTransferSyntaxUID());
        int seqLen = this.in.length();
        boolean undefSeqLen = seqLen <= 0;
        this.setEncodingOptions(new DicomEncodingOptions(false, undefSeqLen, false, undefSeqLen, false));
        this.nextRecordPos = this.firstRecordPos = (int)this.in.getPosition();
        if (!this.isEmpty()) {
            this.nextRecordPos = seqLen > 0 ? (this.nextRecordPos += seqLen) : (int)(this.raf.length() - 8L);
        }
        this.updateDirInfoHeader();
    }

    public DicomEncodingOptions getEncodingOptions() {
        return this.out.getEncodingOptions();
    }

    public void setEncodingOptions(DicomEncodingOptions encOpts) {
        this.out.setEncodingOptions(encOpts);
    }

    public static DicomDirWriter open(File file) throws IOException {
        if (!file.isFile()) {
            throw new FileNotFoundException();
        }
        return new DicomDirWriter(file);
    }

    public static void createEmptyDirectory(File file, String iuid, String id, File descFile, String charset) throws IOException {
        Attributes fmi = Attributes.createFileMetaInformation(iuid, "1.2.840.10008.1.3.10", "1.2.840.10008.1.2.1");
        DicomDirWriter.createEmptyDirectory(file, fmi, id, descFile, charset);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void createEmptyDirectory(File file, Attributes fmi, String id, File descFile, String charset) throws IOException {
        Attributes fsInfo = DicomDirWriter.createFileSetInformation(file, id, descFile, charset);
        try (DicomOutputStream out = new DicomOutputStream(file);){
            out.writeDataset(fmi, fsInfo);
        }
    }

    private static Attributes createFileSetInformation(File file, String id, File descFile, String charset) {
        Attributes fsInfo = new Attributes(7);
        fsInfo.setString(266544, VR.CS, id);
        if (descFile != null) {
            fsInfo.setString(266561, VR.CS, DicomDirWriter.toFileIDs(file, descFile));
            if (charset != null && !charset.isEmpty()) {
                fsInfo.setString(266562, VR.CS, charset);
            }
        }
        fsInfo.setInt(266752, VR.UL, 0);
        fsInfo.setInt(266754, VR.UL, 0);
        fsInfo.setInt(266770, VR.US, 0);
        fsInfo.setNull(266784, VR.SQ);
        return fsInfo;
    }

    public synchronized Attributes addRootDirectoryRecord(Attributes rec) throws IOException {
        Attributes lastRootRecord = this.readLastRootDirectoryRecord();
        if (lastRootRecord == null) {
            this.writeRecord(this.firstRecordPos, rec);
            this.setOffsetOfFirstRootDirectoryRecord(this.firstRecordPos);
        } else {
            this.addRecord(267264, lastRootRecord, rec);
        }
        this.setOffsetOfLastRootDirectoryRecord((int)rec.getItemPosition());
        return rec;
    }

    public synchronized Attributes addLowerDirectoryRecord(Attributes parentRec, Attributes rec) throws IOException {
        Attributes prevRec = this.lastChildRecords.get(parentRec);
        if (prevRec == null) {
            prevRec = this.findLastLowerDirectoryRecord(parentRec);
        }
        if (prevRec != null) {
            this.addRecord(267264, prevRec, rec);
        } else {
            this.addRecord(267296, parentRec, rec);
        }
        this.lastChildRecords.put(parentRec, rec);
        return rec;
    }

    public synchronized Attributes findOrAddPatientRecord(Attributes rec) throws IOException {
        Attributes patRec = super.findPatientRecord(rec.getString(0x100020));
        return patRec != null ? patRec : this.addRootDirectoryRecord(rec);
    }

    public synchronized Attributes findOrAddStudyRecord(Attributes patRec, Attributes rec) throws IOException {
        Attributes studyRec = super.findStudyRecord(patRec, rec.getString(0x20000D));
        return studyRec != null ? studyRec : this.addLowerDirectoryRecord(patRec, rec);
    }

    public synchronized Attributes findOrAddSeriesRecord(Attributes studyRec, Attributes rec) throws IOException {
        Attributes seriesRec = super.findSeriesRecord(studyRec, rec.getString(0x20000E));
        return seriesRec != null ? seriesRec : this.addLowerDirectoryRecord(studyRec, rec);
    }

    public synchronized boolean deleteRecord(Attributes rec) throws IOException {
        if (rec.getInt(267280, 0) == 0) {
            return false;
        }
        Attributes lowerRec = this.readLowerDirectoryRecord(rec);
        while (lowerRec != null) {
            this.deleteRecord(lowerRec);
            lowerRec = this.readNextDirectoryRecord(lowerRec);
        }
        rec.setInt(267280, VR.US, 0);
        this.markAsDirty(rec);
        return true;
    }

    public synchronized void rollback() throws IOException {
        if (this.dirtyRecords.isEmpty()) {
            return;
        }
        this.clearCache();
        this.dirtyRecords.clear();
        if (this.rollbackLen != -1) {
            this.restoreDirInfo();
            this.nextRecordPos = this.rollbackLen;
            if (this.getEncodingOptions().undefSequenceLength) {
                this.writeSequenceDelimitationItem();
                this.raf.setLength(this.raf.getFilePointer());
            } else {
                this.raf.setLength(this.rollbackLen);
            }
            this.writeFileSetConsistencyFlag(0);
            this.rollbackLen = -1;
        }
    }

    @Override
    public void clearCache() {
        this.lastChildRecords.clear();
        super.clearCache();
    }

    public synchronized void commit() throws IOException {
        if (this.dirtyRecords.isEmpty()) {
            return;
        }
        if (this.rollbackLen == -1) {
            this.writeFileSetConsistencyFlag(65535);
        }
        for (Attributes rec : this.dirtyRecords) {
            this.writeDirRecordHeader(rec);
        }
        this.dirtyRecords.clear();
        if (this.rollbackLen != -1 && this.getEncodingOptions().undefSequenceLength) {
            this.writeSequenceDelimitationItem();
        }
        this.writeDirInfoHeader();
        this.rollbackLen = -1;
    }

    @Override
    public void close() throws IOException {
        this.commit();
        super.close();
    }

    public String[] toFileIDs(File f) {
        return DicomDirWriter.toFileIDs(this.file, f);
    }

    private static String[] toFileIDs(File dfile, File f) {
        String dfilepath = dfile.getAbsolutePath();
        int dend = dfilepath.lastIndexOf(File.separatorChar) + 1;
        String dpath = dfilepath.substring(0, dend);
        String fpath = f.getAbsolutePath();
        if (dend == 0 || !fpath.startsWith(dpath)) {
            throw new IllegalArgumentException("file: " + fpath + " not in directory: " + dfile.getAbsoluteFile());
        }
        return StringUtils.split(fpath.substring(dend), File.separatorChar);
    }

    private void updateDirInfoHeader() {
        ByteUtils.intToBytesLE(this.getOffsetOfFirstRootDirectoryRecord(), this.dirInfoHeader, 8);
        ByteUtils.intToBytesLE(this.getOffsetOfLastRootDirectoryRecord(), this.dirInfoHeader, 20);
        ByteUtils.intToBytesLE(this.getEncodingOptions().undefSequenceLength ? -1 : this.nextRecordPos - this.firstRecordPos, this.dirInfoHeader, 42);
    }

    private void restoreDirInfo() {
        this.setOffsetOfFirstRootDirectoryRecord(ByteUtils.bytesToIntLE(this.dirInfoHeader, 8));
        this.setOffsetOfLastRootDirectoryRecord(ByteUtils.bytesToIntLE(this.dirInfoHeader, 20));
    }

    private void writeDirInfoHeader() throws IOException {
        this.updateDirInfoHeader();
        this.raf.seek(this.firstRecordPos - this.dirInfoHeader.length);
        this.raf.write(this.dirInfoHeader);
    }

    private void writeDirRecordHeader(Attributes rec) throws IOException {
        ByteUtils.intToBytesLE(rec.getInt(267264, 0), this.dirRecordHeader, 8);
        ByteUtils.shortToBytesLE(rec.getInt(267280, 0), this.dirRecordHeader, 20);
        ByteUtils.intToBytesLE(rec.getInt(267296, 0), this.dirRecordHeader, 30);
        this.raf.seek(rec.getItemPosition() + 8L);
        this.raf.write(this.dirRecordHeader);
    }

    private void writeSequenceDelimitationItem() throws IOException {
        this.raf.seek(this.nextRecordPos);
        this.out.writeHeader(-73507, null, 0);
    }

    private void addRecord(int tag, Attributes prevRec, Attributes rec) throws IOException {
        prevRec.setInt(tag, VR.UL, this.nextRecordPos);
        this.markAsDirty(prevRec);
        this.writeRecord(this.nextRecordPos, rec);
    }

    private void writeRecord(int offset, Attributes rec) throws IOException {
        if (LOG.isInfoEnabled()) {
            LOG.info("M-UPDATE {}: add {} Record", (Object)this.file, (Object)rec.getString(267312, null));
        }
        LOG.debug("Directory Record:\n{}", (Object)rec);
        rec.setItemPosition(offset);
        if (this.rollbackLen == -1) {
            this.rollbackLen = offset;
            this.writeFileSetConsistencyFlag(65535);
        }
        this.raf.seek(offset);
        rec.setInt(267264, VR.UL, 0);
        rec.setInt(267280, VR.US, 65535);
        rec.setInt(267296, VR.UL, 0);
        rec.writeItemTo(this.out);
        this.nextRecordPos = (int)this.raf.getFilePointer();
        this.cache.put(offset, rec);
    }

    private void writeFileSetConsistencyFlag(int flag) throws IOException {
        this.raf.seek(this.firstRecordPos - 14);
        this.raf.writeShort(flag);
        this.setFileSetConsistencyFlag(flag);
    }

    private void markAsDirty(Attributes rec) {
        int index = Collections.binarySearch(this.dirtyRecords, rec, offsetComparator);
        if (index < 0) {
            this.dirtyRecords.add(-(index + 1), rec);
        }
    }

    public synchronized int purge() throws IOException {
        int[] count = new int[]{0};
        this.purge(this.findFirstRootDirectoryRecordInUse(false), count);
        return count[0];
    }

    private boolean purge(Attributes rec, int[] count) throws IOException {
        boolean purge = true;
        while (rec != null) {
            if (this.purge(this.findLowerDirectoryRecordInUse(rec, false), count) && !rec.containsValue(267520)) {
                this.deleteRecord(rec);
                count[0] = count[0] + 1;
            } else {
                purge = false;
            }
            rec = this.readNextDirectoryRecord(rec);
        }
        return purge;
    }
}

