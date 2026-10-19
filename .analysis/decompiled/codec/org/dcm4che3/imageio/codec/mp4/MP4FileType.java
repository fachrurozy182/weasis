/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.imageio.codec.mp4;

import java.nio.ByteBuffer;

public class MP4FileType {
    public static final int QT = 1903435808;
    public static final int ISOM = 1769172845;
    public static final MP4FileType ISOM_QT = new MP4FileType(1769172845, 0, 1769172845, 1903435808);
    private final int[] brands;

    public MP4FileType(int majorBrand, int minorVersion, int ... compatibleBrands) {
        this.brands = new int[2 + compatibleBrands.length];
        this.brands[0] = majorBrand;
        this.brands[1] = minorVersion;
        System.arraycopy(compatibleBrands, 0, this.brands, 2, compatibleBrands.length);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        MP4FileType.append4CC(sb.append("ftyp["), this.brands[0]);
        sb.append('.').append(this.brands[1]);
        for (int i = 2; i < this.brands.length; ++i) {
            MP4FileType.append4CC(sb.append(", "), this.brands[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    public byte[] toBytes() {
        ByteBuffer bb = ByteBuffer.allocate(this.size());
        bb.putInt(bb.remaining());
        bb.putInt(1718909296);
        for (int brand : this.brands) {
            bb.putInt(brand);
        }
        return bb.array();
    }

    private static void append4CC(StringBuilder sb, int brand) {
        sb.append((char)(brand >>> 24 & 0xFF));
        sb.append((char)(brand >>> 16 & 0xFF));
        sb.append((char)(brand >>> 8 & 0xFF));
        sb.append((char)(brand & 0xFF));
    }

    public int size() {
        return (2 + this.brands.length) * 4;
    }

    public int majorBrand() {
        return this.brands[0];
    }

    public int minorVersion() {
        return this.brands[1];
    }

    public int[] compatibleBrands() {
        int[] compatibleBrands = new int[this.brands.length - 2];
        System.arraycopy(this.brands, 2, compatibleBrands, 0, compatibleBrands.length);
        return compatibleBrands;
    }
}

