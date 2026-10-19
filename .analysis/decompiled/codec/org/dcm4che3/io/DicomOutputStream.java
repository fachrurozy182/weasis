/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.SpecificCharacterSet;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.Value;
import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.CountingOutputStream;
import org.dcm4che3.util.TagUtils;

public class DicomOutputStream
extends FilterOutputStream {
    private static final byte[] DICM = new byte[]{68, 73, 67, 77};
    private byte[] preamble = new byte[128];
    private boolean explicitVR;
    private boolean bigEndian;
    private CountingOutputStream countingOutputStream;
    private DicomEncodingOptions encOpts = DicomEncodingOptions.DEFAULT;
    private final byte[] buf = new byte[12];
    private Deflater deflater;

    public DicomOutputStream(OutputStream out, String tsuid) throws IOException {
        super(out);
        this.switchTransferSyntax(tsuid);
    }

    public DicomOutputStream(File file) throws IOException {
        this(new BufferedOutputStream(new FileOutputStream(file)), "1.2.840.10008.1.2.1");
    }

    public final void setPreamble(byte[] preamble) {
        if (preamble.length != 128) {
            throw new IllegalArgumentException("preamble.length=" + preamble.length);
        }
        this.preamble = (byte[])preamble.clone();
    }

    public final boolean isExplicitVR() {
        return this.explicitVR;
    }

    public final boolean isBigEndian() {
        return this.bigEndian;
    }

    public final DicomEncodingOptions getEncodingOptions() {
        return this.encOpts;
    }

    public final void setEncodingOptions(DicomEncodingOptions encOpts) {
        if (encOpts == null) {
            throw new NullPointerException();
        }
        this.encOpts = encOpts;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.out.write(b, off, len);
    }

    public void writeCommand(Attributes cmd) throws IOException {
        if (this.explicitVR || this.bigEndian) {
            throw new IllegalStateException("explicitVR=" + this.explicitVR + ", bigEndian=" + this.bigEndian);
        }
        cmd.writeGroupTo(this, 0);
    }

    public void writeFileMetaInformation(Attributes fmi) throws IOException {
        if (!this.explicitVR || this.bigEndian || this.countingOutputStream != null) {
            throw new IllegalStateException("explicitVR=" + this.explicitVR + ", bigEndian=" + this.bigEndian + ", deflated=" + (this.countingOutputStream != null));
        }
        this.write(this.preamble);
        this.write(DICM);
        fmi.writeGroupTo(this, 131072);
    }

    public void writeDataset(Attributes fmi, Attributes dataset) throws IOException {
        if (fmi != null) {
            this.writeFileMetaInformation(fmi);
            this.switchTransferSyntax(fmi.getString(131088, null));
        }
        if (dataset.bigEndian() != this.bigEndian || this.encOpts.groupLength || !this.encOpts.undefSequenceLength || !this.encOpts.undefItemLength) {
            dataset = new Attributes(dataset, this.bigEndian);
        }
        if (this.encOpts.groupLength) {
            dataset.calcLength(this.encOpts, this.explicitVR);
        }
        dataset.writeTo(this);
    }

    public void switchTransferSyntax(String tsuid) {
        this.bigEndian = tsuid.equals("1.2.840.10008.1.2.2");
        boolean bl = this.explicitVR = !tsuid.equals("1.2.840.10008.1.2");
        if (tsuid.equals("1.2.840.10008.1.2.1.99") || tsuid.equals("1.2.840.10008.1.2.4.95") || tsuid.equals("1.2.840.10008.1.2.4.205")) {
            this.countingOutputStream = new CountingOutputStream(this.out);
            this.deflater = new Deflater(-1, true);
            this.out = new DeflaterOutputStream((OutputStream)this.countingOutputStream, this.deflater);
        }
    }

    public void writeHeader(int tag, VR vr, int len) throws IOException {
        int headerLen;
        byte[] b = this.buf;
        ByteUtils.tagToBytes(tag, b, 0, this.bigEndian);
        if (!TagUtils.isItem(tag) && this.explicitVR) {
            if ((len & 0xFFFF0000) != 0 && vr.headerLength() == 8) {
                vr = VR.UN;
            }
            ByteUtils.shortToBytesBE(vr.code(), b, 4);
            headerLen = vr.headerLength();
            if (headerLen == 8) {
                ByteUtils.shortToBytes(len, b, 6, this.bigEndian);
            } else {
                b[7] = 0;
                b[6] = 0;
                ByteUtils.intToBytes(len, b, 8, this.bigEndian);
            }
        } else {
            ByteUtils.intToBytes(len, b, 4, this.bigEndian);
            headerLen = 8;
        }
        this.out.write(b, 0, headerLen);
    }

    public void writeAttribute(int tag, VR vr, Object value, SpecificCharacterSet cs) throws IOException {
        if (value instanceof Value) {
            this.writeAttribute(tag, vr, (Value)value);
        } else {
            this.writeAttribute(tag, vr, value instanceof byte[] ? (byte[])value : vr.toBytes(value, cs));
        }
    }

    public void writeAttribute(int tag, VR vr, byte[] val) throws IOException {
        int padlen = val.length & 1;
        this.writeHeader(tag, vr, val.length + padlen);
        this.out.write(val);
        if (padlen > 0) {
            this.out.write(vr.paddingByte());
        }
    }

    public void writeAttribute(int tag, VR vr, Value val) throws IOException {
        if (val instanceof BulkData && this.out instanceof ObjectOutputStream) {
            this.writeHeader(tag, vr, 64507);
            ((ObjectOutputStream)this.out).writeObject(val);
        } else {
            int length = val.getEncodedLength(this.encOpts, this.explicitVR, vr);
            this.writeHeader(tag, vr, length);
            val.writeTo(this, vr);
            if (length == -1) {
                this.writeHeader(-73507, null, 0);
            }
        }
    }

    public void writeGroupLength(int tag, int len) throws IOException {
        byte[] b = this.buf;
        ByteUtils.tagToBytes(tag, b, 0, this.bigEndian);
        if (this.explicitVR) {
            ByteUtils.shortToBytesBE(VR.UL.code(), b, 4);
            ByteUtils.shortToBytes(4, b, 6, this.bigEndian);
        } else {
            ByteUtils.intToBytes(4, b, 4, this.bigEndian);
        }
        ByteUtils.intToBytes(len, b, 8, this.bigEndian);
        this.out.write(b, 0, 12);
    }

    public void finish() throws IOException {
        if (this.countingOutputStream != null) {
            ((DeflaterOutputStream)this.out).finish();
            if ((this.countingOutputStream.getCount() & 1L) != 0L) {
                this.countingOutputStream.write(0);
            }
        }
    }

    @Override
    public void close() throws IOException {
        try {
            this.finish();
        }
        catch (IOException iOException) {
            // empty catch block
        }
        if (this.deflater != null) {
            this.deflater.end();
        }
        super.close();
    }
}

