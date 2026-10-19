/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.gui.task.SeriesProgressMonitor
 *  org.weasis.core.api.media.data.Series
 */
package org.weasis.dicom.explorer.wado;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;
import org.weasis.core.api.gui.task.SeriesProgressMonitor;
import org.weasis.core.api.media.data.Series;

public class DicomSeriesProgressMonitor
extends SeriesProgressMonitor {
    private volatile boolean wadoRequest;
    private byte[] header;

    public DicomSeriesProgressMonitor(Series series, InputStream in, boolean wadoRequest) {
        super(series, in);
        this.wadoRequest = wadoRequest;
        if (wadoRequest) {
            this.header = new byte[512];
        }
    }

    public int read(byte[] b) throws IOException {
        int nr = super.read(b);
        if (this.wadoRequest) {
            if (this.nread < this.header.length) {
                System.arraycopy(b, 0, this.header, this.nread - nr, nr);
            } else {
                this.wadoRequest = false;
                int length = this.header.length - (this.nread - nr);
                System.arraycopy(b, 0, this.header, this.nread - nr, length);
                DicomSeriesProgressMonitor.readMetaInfo(this, this.header);
                this.header = null;
            }
        }
        return nr;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int nr = super.read(b, off, len);
        if (this.wadoRequest) {
            if (this.nread < this.header.length) {
                System.arraycopy(b, off, this.header, this.nread - nr, nr);
            } else {
                this.wadoRequest = false;
                int length = this.header.length - (this.nread - nr);
                System.arraycopy(b, off, this.header, this.nread - nr, length);
                DicomSeriesProgressMonitor.readMetaInfo(this, this.header);
                this.header = null;
            }
        }
        return nr;
    }

    public static void readMetaInfo(DicomSeriesProgressMonitor progress, byte[] b) throws IOException {
        int vl;
        if (b == null) {
            return;
        }
        if (b.length <= 132 || !"DICM".equals(new String(b, 128, 4, StandardCharsets.UTF_8))) {
            InterruptedIOException exc = new InterruptedIOException("Not a DICOM file");
            exc.bytesTransferred = progress.nread;
            progress.series.setFileSize(progress.series.getFileSize() - (long)progress.nread);
            progress.nread = 0;
            throw exc;
        }
        int endByteOffset = b.length - 1;
        for (int byteOffset = 132; byteOffset < endByteOffset; byteOffset += vl) {
            int group = DicomSeriesProgressMonitor.extractUnsigned16(b, byteOffset);
            int element = DicomSeriesProgressMonitor.extractUnsigned16(b, byteOffset + 2);
            byteOffset += 4;
            if (group == 2 && element > 16 || group > 2) break;
            byte[] vr = new byte[]{b[byteOffset], b[byteOffset + 1]};
            byteOffset += 2;
            if (DicomSeriesProgressMonitor.isShortValueLengthVR(vr)) {
                vl = DicomSeriesProgressMonitor.extractUnsigned16(b, byteOffset);
                byteOffset += 2;
            } else {
                vl = (int)DicomSeriesProgressMonitor.extractUnsigned32(b, byteOffset + 2);
                byteOffset += 6;
            }
            if (element == 16 && vl != 0 && byteOffset + vl < b.length) break;
        }
    }

    static int extractUnsigned16(byte[] b, int offset) {
        if (offset + 1 >= b.length) {
            return 0;
        }
        int v1 = b[offset] & 0xFF;
        int v2 = b[offset + 1] & 0xFF;
        return v2 << 8 | v1;
    }

    static long extractUnsigned32(byte[] b, int offset) {
        if (offset + 4 >= b.length) {
            return 0L;
        }
        long v1 = (long)b[offset] & 0xFFL;
        long v2 = (long)b[offset + 1] & 0xFFL;
        long v3 = (long)b[offset + 2] & 0xFFL;
        long v4 = (long)b[offset + 3] & 0xFFL;
        return ((v4 << 8 | v3) << 8 | v2) << 8 | v1;
    }

    static boolean isShortValueLengthVR(byte[] vr) {
        return vr[0] == 65 && (vr[1] == 69 || vr[1] == 83 || vr[1] == 84) || vr[0] == 67 && vr[1] == 83 || vr[0] == 68 && (vr[1] == 65 || vr[1] == 83 || vr[1] == 84) || vr[0] == 70 && (vr[1] == 68 || vr[1] == 76) || vr[0] == 73 && vr[1] == 83 || vr[0] == 76 && (vr[1] == 79 || vr[1] == 84) || vr[0] == 80 && vr[1] == 78 || vr[0] == 83 && (vr[1] == 72 || vr[1] == 76 || vr[1] == 83 || vr[1] == 84) || vr[0] == 84 && vr[1] == 77 || vr[0] == 85 && (vr[1] == 73 || vr[1] == 76 || vr[1] == 83);
    }
}

