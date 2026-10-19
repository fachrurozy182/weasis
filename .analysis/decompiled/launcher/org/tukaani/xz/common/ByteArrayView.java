/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.common;

/*
 * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
 */
public final class ByteArrayView {
    public static final int ALIGN_SHORT = 0;
    public static final int ALIGN_INT = 0;
    public static final int ALIGN_LONG = 0;

    public static short getShortBE(byte[] buf, int index) {
        return (short)(buf[index] << 8 | buf[index + 1] & 0xFF);
    }

    public static short getShortLE(byte[] buf, int index) {
        return (short)(buf[index] & 0xFF | buf[index + 1] << 8);
    }

    public static int getIntBE(byte[] buf, int index) {
        return buf[index] << 24 | (buf[index + 1] & 0xFF) << 16 | (buf[index + 2] & 0xFF) << 8 | buf[index + 3] & 0xFF;
    }

    public static int getIntLE(byte[] buf, int index) {
        return buf[index] & 0xFF | (buf[index + 1] & 0xFF) << 8 | (buf[index + 2] & 0xFF) << 16 | buf[index + 3] << 24;
    }

    public static long getLongBE(byte[] buf, int index) {
        return (long)buf[index] << 56 | (long)(buf[index + 1] & 0xFF) << 48 | (long)(buf[index + 2] & 0xFF) << 40 | (long)(buf[index + 3] & 0xFF) << 32 | (long)(buf[index + 4] & 0xFF) << 24 | (long)(buf[index + 5] & 0xFF) << 16 | (long)(buf[index + 6] & 0xFF) << 8 | (long)buf[index + 7] & 0xFFL;
    }

    public static long getLongLE(byte[] buf, int index) {
        return (long)buf[index] & 0xFFL | (long)(buf[index + 1] & 0xFF) << 8 | (long)(buf[index + 2] & 0xFF) << 16 | (long)(buf[index + 3] & 0xFF) << 24 | (long)(buf[index + 4] & 0xFF) << 32 | (long)(buf[index + 5] & 0xFF) << 40 | (long)(buf[index + 6] & 0xFF) << 48 | (long)buf[index + 7] << 56;
    }

    public static void setShortBE(byte[] buf, int index, short value) {
        buf[index] = (byte)(value >>> 8);
        buf[index + 1] = (byte)value;
    }

    public static void setShortLE(byte[] buf, int index, short value) {
        buf[index] = (byte)value;
        buf[index + 1] = (byte)(value >>> 8);
    }

    public static void setIntBE(byte[] buf, int index, int value) {
        buf[index] = (byte)(value >>> 24);
        buf[index + 1] = (byte)(value >>> 16);
        buf[index + 2] = (byte)(value >>> 8);
        buf[index + 3] = (byte)value;
    }

    public static void setIntLE(byte[] buf, int index, int value) {
        buf[index] = (byte)value;
        buf[index + 1] = (byte)(value >>> 8);
        buf[index + 2] = (byte)(value >>> 16);
        buf[index + 3] = (byte)(value >>> 24);
    }

    public static void setLongBE(byte[] buf, int index, long value) {
        buf[index] = (byte)(value >>> 56);
        buf[index + 1] = (byte)(value >>> 48);
        buf[index + 2] = (byte)(value >>> 40);
        buf[index + 3] = (byte)(value >>> 32);
        buf[index + 4] = (byte)(value >>> 24);
        buf[index + 5] = (byte)(value >>> 16);
        buf[index + 6] = (byte)(value >>> 8);
        buf[index + 7] = (byte)value;
    }

    public static void setLongLE(byte[] buf, int index, long value) {
        buf[index] = (byte)value;
        buf[index + 1] = (byte)(value >>> 8);
        buf[index + 2] = (byte)(value >>> 16);
        buf[index + 3] = (byte)(value >>> 24);
        buf[index + 4] = (byte)(value >>> 32);
        buf[index + 5] = (byte)(value >>> 40);
        buf[index + 6] = (byte)(value >>> 48);
        buf[index + 7] = (byte)(value >>> 56);
    }

    private ByteArrayView() {
    }
}

