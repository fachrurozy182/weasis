/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.imageio.codec.jpeg;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.codec.jpeg.JPEG;
import org.dcm4che3.util.ByteUtils;

public class JPEGHeader {
    private final byte[] data;
    private final int[] offsets;

    public JPEGHeader(byte[] data, int lastMarker) {
        int n = 0;
        int offset = 0;
        while ((offset = JPEGHeader.nextMarker(data, offset)) != -1) {
            int marker;
            ++n;
            if (JPEG.isStandalone(marker = data[offset++] & 0xFF)) continue;
            if (offset + 1 >= data.length || marker == lastMarker) break;
            offset += ByteUtils.bytesToUShortBE(data, offset);
        }
        this.data = data;
        this.offsets = new int[n];
        int offset2 = 0;
        for (int i = 0; i < n; ++i) {
            this.offsets[i] = offset2 = JPEGHeader.nextMarker(data, offset2);
            if (JPEG.isStandalone(data[offset2++] & 0xFF)) continue;
            offset2 += ByteUtils.bytesToUShortBE(data, offset2);
        }
    }

    private static int nextMarker(byte[] data, int from) {
        for (int i = from + 1; i < data.length; ++i) {
            if (data[i - 1] != -1 || data[i] == -1 || data[i] == 0) continue;
            return i;
        }
        return -1;
    }

    public int offsetOf(int marker) {
        for (int i = 0; i < this.offsets.length; ++i) {
            if (this.marker(i) != marker) continue;
            return this.offsets[i];
        }
        return -1;
    }

    public int offsetSOF() {
        for (int i = 0; i < this.offsets.length; ++i) {
            if (!JPEG.isSOF(this.marker(i))) continue;
            return this.offsets[i];
        }
        return -1;
    }

    public int offsetAfterAPP() {
        for (int i = 1; i < this.offsets.length; ++i) {
            if (JPEG.isAPP(this.marker(i))) continue;
            return this.offsets[i];
        }
        return -1;
    }

    public int offset(int index) {
        return this.offsets[index];
    }

    public int marker(int index) {
        return this.data[this.offsets[index]] & 0xFF;
    }

    public int numberOfMarkers() {
        return this.offsets.length;
    }

    public Attributes toAttributes(Attributes attrs) {
        int offsetSOF = this.offsetSOF();
        if (offsetSOF == -1) {
            return null;
        }
        if (attrs == null) {
            attrs = new Attributes(10);
        }
        int sof = this.data[offsetSOF] & 0xFF;
        int p = this.data[offsetSOF + 3] & 0xFF;
        int y = (this.data[offsetSOF + 3 + 1] & 0xFF) << 8 | this.data[offsetSOF + 3 + 2] & 0xFF;
        int x = (this.data[offsetSOF + 3 + 3] & 0xFF) << 8 | this.data[offsetSOF + 3 + 4] & 0xFF;
        int nf = this.data[offsetSOF + 3 + 5] & 0xFF;
        attrs.setInt(0x280002, VR.US, nf);
        if (nf == 3) {
            attrs.setString(2621444, VR.CS, sof == 195 || sof == 247 ? "RGB" : "YBR_FULL_422");
            attrs.setInt(2621446, VR.US, 0);
        } else {
            attrs.setString(2621444, VR.CS, "MONOCHROME2");
        }
        attrs.setInt(2621456, VR.US, y);
        attrs.setInt(2621457, VR.US, x);
        attrs.setInt(2621696, VR.US, p > 8 ? 16 : 8);
        attrs.setInt(2621697, VR.US, p);
        attrs.setInt(2621698, VR.US, p - 1);
        attrs.setInt(2621699, VR.US, 0);
        if (sof != 195 && (sof != 247 || this.ss() != 0)) {
            attrs.setString(2629904, VR.CS, "01");
        }
        return attrs;
    }

    public String getTransferSyntaxUID() {
        int sofOffset = this.offsetSOF();
        if (sofOffset == -1) {
            return null;
        }
        return switch (this.data[sofOffset] & 0xFF) {
            case 192 -> "1.2.840.10008.1.2.4.50";
            case 193 -> "1.2.840.10008.1.2.4.51";
            case 194 -> "1.2.840.10008.1.2.4.55";
            case 195 -> {
                if (this.ss() == 1) {
                    yield "1.2.840.10008.1.2.4.70";
                }
                yield "1.2.840.10008.1.2.4.57";
            }
            case 247 -> {
                if (this.ss() == 0) {
                    yield "1.2.840.10008.1.2.4.80";
                }
                yield "1.2.840.10008.1.2.4.81";
            }
            default -> null;
        };
    }

    private int ss() {
        int offsetSOS = this.offsetOf(218);
        return offsetSOS != -1 ? this.data[offsetSOS + 6] & 0xFF : -1;
    }
}

