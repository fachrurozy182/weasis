/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.util;

public class ByteUtils {
    public static final byte[] EMPTY_BYTES = new byte[0];
    public static final int[] EMPTY_INTS = new int[0];
    public static final long[] EMPTY_LONGS = new long[0];
    public static final float[] EMPTY_FLOATS = new float[0];
    public static final double[] EMPTY_DOUBLES = new double[0];

    public static int bytesToVR(byte[] bytes, int off) {
        return ByteUtils.bytesToUShortBE(bytes, off);
    }

    public static int bytesToUShort(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? ByteUtils.bytesToUShortBE(bytes, off) : ByteUtils.bytesToUShortLE(bytes, off);
    }

    public static int bytesToUShortBE(byte[] bytes, int off) {
        return ((bytes[off] & 0xFF) << 8) + (bytes[off + 1] & 0xFF);
    }

    public static int bytesToUShortLE(byte[] bytes, int off) {
        return ((bytes[off + 1] & 0xFF) << 8) + (bytes[off] & 0xFF);
    }

    public static int bytesToShort(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? ByteUtils.bytesToShortBE(bytes, off) : ByteUtils.bytesToShortLE(bytes, off);
    }

    public static int bytesToShortBE(byte[] bytes, int off) {
        return (bytes[off] << 8) + (bytes[off + 1] & 0xFF);
    }

    public static int bytesToShortLE(byte[] bytes, int off) {
        return (bytes[off + 1] << 8) + (bytes[off] & 0xFF);
    }

    public static void bytesToShorts(byte[] b, short[] s, int off, int len, boolean bigEndian) {
        if (bigEndian) {
            ByteUtils.bytesToShortsBE(b, s, off, len);
        } else {
            ByteUtils.bytesToShortsLE(b, s, off, len);
        }
    }

    public static void bytesToShortsLE(byte[] b, short[] s, int off, int len) {
        int boff = 0;
        for (int j = 0; j < len; ++j) {
            byte b0 = b[boff + 1];
            int b1 = b[boff] & 0xFF;
            s[off + j] = (short)(b0 << 8 | b1);
            boff += 2;
        }
    }

    public static void bytesToShortsBE(byte[] b, short[] s, int off, int len) {
        int boff = 0;
        for (int j = 0; j < len; ++j) {
            byte b0 = b[boff];
            int b1 = b[boff + 1] & 0xFF;
            s[off + j] = (short)(b0 << 8 | b1);
            boff += 2;
        }
    }

    public static int bytesToInt(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? ByteUtils.bytesToIntBE(bytes, off) : ByteUtils.bytesToIntLE(bytes, off);
    }

    public static int bytesToIntBE(byte[] bytes, int off) {
        return (bytes[off] << 24) + ((bytes[off + 1] & 0xFF) << 16) + ((bytes[off + 2] & 0xFF) << 8) + (bytes[off + 3] & 0xFF);
    }

    public static int bytesToIntLE(byte[] bytes, int off) {
        return (bytes[off + 3] << 24) + ((bytes[off + 2] & 0xFF) << 16) + ((bytes[off + 1] & 0xFF) << 8) + (bytes[off] & 0xFF);
    }

    public static int bytesToTag(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? ByteUtils.bytesToTagBE(bytes, off) : ByteUtils.bytesToTagLE(bytes, off);
    }

    public static int bytesToTagBE(byte[] bytes, int off) {
        return ByteUtils.bytesToIntBE(bytes, off);
    }

    public static int bytesToTagLE(byte[] bytes, int off) {
        return (bytes[off + 1] << 24) + ((bytes[off] & 0xFF) << 16) + ((bytes[off + 3] & 0xFF) << 8) + (bytes[off + 2] & 0xFF);
    }

    public static float bytesToFloat(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? ByteUtils.bytesToFloatBE(bytes, off) : ByteUtils.bytesToFloatLE(bytes, off);
    }

    public static float bytesToFloatBE(byte[] bytes, int off) {
        return Float.intBitsToFloat(ByteUtils.bytesToIntBE(bytes, off));
    }

    public static float bytesToFloatLE(byte[] bytes, int off) {
        return Float.intBitsToFloat(ByteUtils.bytesToIntLE(bytes, off));
    }

    public static long bytesToLong(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? ByteUtils.bytesToLongBE(bytes, off) : ByteUtils.bytesToLongLE(bytes, off);
    }

    public static long bytesToLongBE(byte[] bytes, int off) {
        return ((long)bytes[off] << 56) + ((long)(bytes[off + 1] & 0xFF) << 48) + ((long)(bytes[off + 2] & 0xFF) << 40) + ((long)(bytes[off + 3] & 0xFF) << 32) + ((long)(bytes[off + 4] & 0xFF) << 24) + (long)((bytes[off + 5] & 0xFF) << 16) + (long)((bytes[off + 6] & 0xFF) << 8) + (long)(bytes[off + 7] & 0xFF);
    }

    public static long bytesToLongLE(byte[] bytes, int off) {
        return ((long)bytes[off + 7] << 56) + ((long)(bytes[off + 6] & 0xFF) << 48) + ((long)(bytes[off + 5] & 0xFF) << 40) + ((long)(bytes[off + 4] & 0xFF) << 32) + ((long)(bytes[off + 3] & 0xFF) << 24) + (long)((bytes[off + 2] & 0xFF) << 16) + (long)((bytes[off + 1] & 0xFF) << 8) + (long)(bytes[off] & 0xFF);
    }

    public static double bytesToDouble(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? ByteUtils.bytesToDoubleBE(bytes, off) : ByteUtils.bytesToDoubleLE(bytes, off);
    }

    public static double bytesToDoubleBE(byte[] bytes, int off) {
        return Double.longBitsToDouble(ByteUtils.bytesToLongBE(bytes, off));
    }

    public static double bytesToDoubleLE(byte[] bytes, int off) {
        return Double.longBitsToDouble(ByteUtils.bytesToLongLE(bytes, off));
    }

    public static byte[] shortToBytes(int i, byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? ByteUtils.shortToBytesBE(i, bytes, off) : ByteUtils.shortToBytesLE(i, bytes, off);
    }

    public static byte[] shortToBytesBE(int i, byte[] bytes, int off) {
        bytes[off] = (byte)(i >> 8);
        bytes[off + 1] = (byte)i;
        return bytes;
    }

    public static byte[] shortToBytesLE(int i, byte[] bytes, int off) {
        bytes[off + 1] = (byte)(i >> 8);
        bytes[off] = (byte)i;
        return bytes;
    }

    public static byte[] intToBytes(int i, byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? ByteUtils.intToBytesBE(i, bytes, off) : ByteUtils.intToBytesLE(i, bytes, off);
    }

    public static byte[] intToBytesBE(int i, byte[] bytes, int off) {
        bytes[off] = (byte)(i >> 24);
        bytes[off + 1] = (byte)(i >> 16);
        bytes[off + 2] = (byte)(i >> 8);
        bytes[off + 3] = (byte)i;
        return bytes;
    }

    public static byte[] intToBytesLE(int i, byte[] bytes, int off) {
        bytes[off + 3] = (byte)(i >> 24);
        bytes[off + 2] = (byte)(i >> 16);
        bytes[off + 1] = (byte)(i >> 8);
        bytes[off] = (byte)i;
        return bytes;
    }

    public static byte[] tagToBytes(int i, byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? ByteUtils.tagToBytesBE(i, bytes, off) : ByteUtils.tagToBytesLE(i, bytes, off);
    }

    public static byte[] tagToBytesBE(int i, byte[] bytes, int off) {
        return ByteUtils.intToBytesBE(i, bytes, off);
    }

    public static byte[] tagToBytesLE(int i, byte[] bytes, int off) {
        bytes[off + 1] = (byte)(i >> 24);
        bytes[off] = (byte)(i >> 16);
        bytes[off + 3] = (byte)(i >> 8);
        bytes[off + 2] = (byte)i;
        return bytes;
    }

    public static byte[] floatToBytes(float f, byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? ByteUtils.floatToBytesBE(f, bytes, off) : ByteUtils.floatToBytesLE(f, bytes, off);
    }

    public static byte[] floatToBytesBE(float f, byte[] bytes, int off) {
        return ByteUtils.intToBytesBE(Float.floatToIntBits(f), bytes, off);
    }

    public static byte[] floatToBytesLE(float f, byte[] bytes, int off) {
        return ByteUtils.intToBytesLE(Float.floatToIntBits(f), bytes, off);
    }

    public static byte[] doubleToBytes(double d, byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? ByteUtils.doubleToBytesBE(d, bytes, off) : ByteUtils.doubleToBytesLE(d, bytes, off);
    }

    public static byte[] doubleToBytesBE(double d, byte[] bytes, int off) {
        return ByteUtils.longToBytesBE(Double.doubleToLongBits(d), bytes, off);
    }

    public static byte[] doubleToBytesLE(double d, byte[] bytes, int off) {
        return ByteUtils.longToBytesLE(Double.doubleToLongBits(d), bytes, off);
    }

    public static byte[] longToBytes(long l, byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? ByteUtils.longToBytesBE(l, bytes, off) : ByteUtils.longToBytesLE(l, bytes, off);
    }

    public static byte[] longToBytesBE(long l, byte[] bytes, int off) {
        bytes[off] = (byte)(l >> 56);
        bytes[off + 1] = (byte)(l >> 48);
        bytes[off + 2] = (byte)(l >> 40);
        bytes[off + 3] = (byte)(l >> 32);
        bytes[off + 4] = (byte)(l >> 24);
        bytes[off + 5] = (byte)(l >> 16);
        bytes[off + 6] = (byte)(l >> 8);
        bytes[off + 7] = (byte)l;
        return bytes;
    }

    public static byte[] longToBytesLE(long l, byte[] bytes, int off) {
        bytes[off + 7] = (byte)(l >> 56);
        bytes[off + 6] = (byte)(l >> 48);
        bytes[off + 5] = (byte)(l >> 40);
        bytes[off + 4] = (byte)(l >> 32);
        bytes[off + 3] = (byte)(l >> 24);
        bytes[off + 2] = (byte)(l >> 16);
        bytes[off + 1] = (byte)(l >> 8);
        bytes[off] = (byte)l;
        return bytes;
    }

    public static byte[][] swapShorts(byte[][] bs) {
        int carry = 0;
        for (int i = 0; i < bs.length; ++i) {
            byte[] b = bs[i];
            if (carry != 0) {
                ByteUtils.swapLastFirst(bs[i - 1], b);
            }
            int len = b.length - carry;
            ByteUtils.swapShorts(b, carry, len & 0xFFFFFFFE);
            carry = len & 1;
        }
        return bs;
    }

    public static byte[] swapShorts(byte[] b, int off, int len) {
        ByteUtils.checkLength(len, 2);
        int n = off + len;
        for (int i = off; i < n; i += 2) {
            ByteUtils.swap(b, i, i + 1);
        }
        return b;
    }

    public static byte[] swapInts(byte[] b, int off, int len) {
        ByteUtils.checkLength(len, 4);
        int n = off + len;
        for (int i = off; i < n; i += 4) {
            ByteUtils.swap(b, i, i + 3);
            ByteUtils.swap(b, i + 1, i + 2);
        }
        return b;
    }

    public static byte[] swapLongs(byte[] b, int off, int len) {
        ByteUtils.checkLength(len, 8);
        int n = off + len;
        for (int i = off; i < n; i += 8) {
            ByteUtils.swap(b, i, i + 7);
            ByteUtils.swap(b, i + 1, i + 6);
            ByteUtils.swap(b, i + 2, i + 5);
            ByteUtils.swap(b, i + 3, i + 4);
        }
        return b;
    }

    private static void checkLength(int len, int numBytes) {
        if (len < 0 || len % numBytes != 0) {
            throw new IllegalArgumentException("length: " + len);
        }
    }

    private static void swap(byte[] bytes, int a, int b) {
        byte t = bytes[a];
        bytes[a] = bytes[b];
        bytes[b] = t;
    }

    private static void swapLastFirst(byte[] b1, byte[] b2) {
        int last = b1.length - 1;
        byte t = b2[0];
        b2[0] = b1[last];
        b1[last] = t;
    }

    public static byte[] intsToBytesLE(int ... values) {
        byte[] ret = new byte[4 * values.length];
        for (int i = 0; i < values.length; ++i) {
            ByteUtils.intToBytesLE(values[i], ret, 4 * i);
        }
        return ret;
    }
}

