/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.index;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import org.tukaani.xz.XZIOException;
import org.tukaani.xz.common.EncoderUtil;
import org.tukaani.xz.index.IndexBase;
import org.tukaani.xz.index.IndexRecord;

public class IndexEncoder
extends IndexBase {
    private final ArrayList<IndexRecord> records = new ArrayList();

    public IndexEncoder() {
        super(new XZIOException("XZ Stream or its Index has grown too big"));
    }

    @Override
    public void add(long unpaddedSize, long uncompressedSize) throws XZIOException {
        super.add(unpaddedSize, uncompressedSize);
        this.records.add(new IndexRecord(unpaddedSize, uncompressedSize));
    }

    public void encode(OutputStream out) throws IOException {
        CRC32 crc32 = new CRC32();
        CheckedOutputStream outChecked = new CheckedOutputStream(out, crc32);
        outChecked.write(0);
        EncoderUtil.encodeVLI(outChecked, this.recordCount);
        for (IndexRecord record : this.records) {
            EncoderUtil.encodeVLI(outChecked, record.unpadded);
            EncoderUtil.encodeVLI(outChecked, record.uncompressed);
        }
        for (int i = this.getIndexPaddingSize(); i > 0; --i) {
            outChecked.write(0);
        }
        long value = crc32.getValue();
        for (int i = 0; i < 4; ++i) {
            out.write((byte)(value >>> i * 8));
        }
    }
}

