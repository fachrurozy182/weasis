/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.Value;
import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.StringUtils;

public class BulkData
implements Value,
Serializable {
    public static final int MAGIC_LEN = 64507;
    private String uuid;
    private String uri;
    private int uriPathEnd;
    private boolean bigEndian;
    private long offset = 0L;
    private long length = -1L;
    private static final long serialVersionUID = -6563845357491618094L;

    public BulkData(String uuid, String uri, boolean bigEndian) {
        this.uuid = uuid;
        this.setURI(uri);
        this.bigEndian = bigEndian;
    }

    public BulkData(String uri, long offset, long length, boolean bigEndian) {
        this.uuid = null;
        this.uriPathEnd = uri.length();
        this.uri = uri + "?offset=" + offset + "&length=" + length;
        this.offset = offset;
        this.length = length;
        this.bigEndian = bigEndian;
    }

    public String getUUID() {
        return this.uuid;
    }

    public String getURI() {
        return this.uri;
    }

    public void setURI(String uri) {
        this.uri = uri;
        this.offset = 0L;
        this.length = -1L;
        this.uriPathEnd = 0;
        if (uri == null) {
            return;
        }
        int pathEnd = uri.indexOf(63);
        if (pathEnd < 0) {
            this.uriPathEnd = uri.length();
            return;
        }
        this.uriPathEnd = pathEnd;
        for (String qparam : StringUtils.split(uri.substring(pathEnd + 1), '&')) {
            try {
                if (qparam.startsWith("offset=")) {
                    this.offset = Long.parseLong(qparam.substring(7));
                    continue;
                }
                if (!qparam.startsWith("length=")) continue;
                this.length = Long.parseLong(qparam.substring(7));
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
    }

    public boolean bigEndian() {
        return this.bigEndian;
    }

    public int length() {
        return (int)this.length;
    }

    public long offset() {
        return this.offset;
    }

    @Override
    public boolean isEmpty() {
        return this.length == 0L;
    }

    public String toString() {
        return "BulkData[uuid=" + this.uuid + ", uri=" + this.uri + ", bigEndian=" + this.bigEndian + "]";
    }

    public File getFile() {
        try {
            return new File(new URI(this.uriWithoutOffsetAndLength()));
        }
        catch (URISyntaxException e) {
            throw new IllegalStateException("uri: " + this.uri);
        }
        catch (IllegalArgumentException e) {
            throw new IllegalStateException("uri: " + this.uri);
        }
    }

    public String uriWithoutOffsetAndLength() {
        if (this.uri == null) {
            throw new IllegalStateException("uri: null");
        }
        return this.uri.substring(0, this.uriPathEnd);
    }

    public InputStream openStream() throws IOException {
        if (this.uri == null) {
            throw new IllegalStateException("uri: null");
        }
        if (!this.uri.startsWith("file:")) {
            return new URL(this.uri).openStream();
        }
        FileInputStream in = new FileInputStream(this.getFile());
        StreamUtils.skipFully(in, this.offset);
        return in;
    }

    @Override
    public int calcLength(DicomEncodingOptions encOpts, boolean explicitVR, VR vr) {
        if (this.length == -1L) {
            throw new UnsupportedOperationException();
        }
        return (int)(this.length + 1L) & 0xFFFFFFFE;
    }

    @Override
    public int getEncodedLength(DicomEncodingOptions encOpts, boolean explicitVR, VR vr) {
        return (int)(this.length == -1L ? -1L : this.length + 1L & 0xFFFFFFFFFFFFFFFEL);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public byte[] toBytes(VR vr, boolean bigEndian) throws IOException {
        int intLength = (int)this.length;
        if (intLength < 0) {
            throw new UnsupportedOperationException();
        }
        if (intLength == 0) {
            return ByteUtils.EMPTY_BYTES;
        }
        try (InputStream in = this.openStream();){
            byte[] b = new byte[intLength];
            StreamUtils.readFully(in, b, 0, b.length);
            if (this.bigEndian != bigEndian) {
                vr.toggleEndian(b, false);
            }
            byte[] byArray = b;
            return byArray;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void writeTo(DicomOutputStream out, VR vr) throws IOException {
        try (InputStream in = this.openStream();){
            if (this.bigEndian != out.isBigEndian()) {
                StreamUtils.copy(in, (OutputStream)out, this.length, vr.numEndianBytes());
            } else {
                StreamUtils.copy(in, (OutputStream)out, this.length);
            }
            if ((this.length & 1L) != 0L) {
                out.write(vr.paddingByte());
            }
        }
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeUTF(StringUtils.maskNull(this.uuid, ""));
        oos.writeUTF(StringUtils.maskNull(this.uri, ""));
        oos.writeBoolean(this.bigEndian);
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        this.uuid = StringUtils.maskEmpty(ois.readUTF(), null);
        this.setURI(StringUtils.maskEmpty(ois.readUTF(), null));
        this.bigEndian = ois.readBoolean();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        BulkData other = (BulkData)obj;
        if (this.bigEndian != other.bigEndian) {
            return false;
        }
        if (this.uri == null ? other.uri != null : !this.uri.equals(other.uri)) {
            return false;
        }
        return !(this.uuid == null ? other.uuid != null : !this.uuid.equals(other.uuid));
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + (this.bigEndian ? 1231 : 1237);
        result = 31 * result + (this.uri == null ? 0 : this.uri.hashCode());
        result = 31 * result + (this.uuid == null ? 0 : this.uuid.hashCode());
        return result;
    }

    public long getSegmentEnd() {
        if (this.length == -1L) {
            return -1L;
        }
        return this.offset() + (this.length & 0xFFFFFFFFL);
    }

    public long longLength() {
        return this.length;
    }

    public void setOffset(long offset) {
        this.offset = offset;
        this.uri = this.uri.substring(0, this.uriPathEnd) + "?offset=" + offset + "&length=" + this.length;
    }

    public void setLength(long length) {
        if (length < -1L || length > 0xFFFFFFFEL) {
            throw new IllegalArgumentException("BulkData length limited to -1..2^32-2 but was " + length);
        }
        this.length = length;
        this.uri = this.uri.substring(0, this.uriPathEnd) + "?offset=" + this.offset + "&length=" + length;
    }

    @FunctionalInterface
    public static interface Creator {
        public BulkData create(String var1, String var2, boolean var3);
    }
}

