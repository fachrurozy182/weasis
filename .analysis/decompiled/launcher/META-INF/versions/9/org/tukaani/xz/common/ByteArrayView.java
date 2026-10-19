/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.common;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/*
 * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
 */
public final class ByteArrayView {
    public static final int ALIGN_SHORT;
    public static final int ALIGN_INT;
    public static final int ALIGN_LONG;
    private static final VarHandle bytesAsShortLE;
    private static final VarHandle bytesAsShortBE;
    private static final VarHandle bytesAsIntLE;
    private static final VarHandle bytesAsIntBE;
    private static final VarHandle bytesAsLongLE;
    private static final VarHandle bytesAsLongBE;

    public static short getShortBE(byte[] buf, int index) {
        return bytesAsShortBE.get(buf, index);
    }

    public static short getShortLE(byte[] buf, int index) {
        return bytesAsShortLE.get(buf, index);
    }

    public static int getIntBE(byte[] buf, int index) {
        return bytesAsIntBE.get(buf, index);
    }

    public static int getIntLE(byte[] buf, int index) {
        return bytesAsIntLE.get(buf, index);
    }

    public static long getLongBE(byte[] buf, int index) {
        return bytesAsLongBE.get(buf, index);
    }

    public static long getLongLE(byte[] buf, int index) {
        return bytesAsLongLE.get(buf, index);
    }

    public static void setShortBE(byte[] buf, int index, short value) {
        bytesAsShortBE.set(buf, index, value);
    }

    public static void setShortLE(byte[] buf, int index, short value) {
        bytesAsShortLE.set(buf, index, value);
    }

    public static void setIntBE(byte[] buf, int index, int value) {
        bytesAsIntBE.set(buf, index, value);
    }

    public static void setIntLE(byte[] buf, int index, int value) {
        bytesAsIntLE.set(buf, index, value);
    }

    public static void setLongBE(byte[] buf, int index, long value) {
        bytesAsLongBE.set(buf, index, value);
    }

    public static void setLongLE(byte[] buf, int index, long value) {
        bytesAsLongLE.set(buf, index, value);
    }

    private ByteArrayView() {
    }

    static {
        ByteBuffer bb = ByteBuffer.wrap(new byte[0]);
        int aoShort = 0;
        int aoInt = 0;
        int aoLong = 0;
        try {
            aoShort = bb.alignmentOffset(0, 2);
            aoInt = bb.alignmentOffset(0, 4);
            aoLong = bb.alignmentOffset(0, 8);
        }
        catch (UnsupportedOperationException unsupportedOperationException) {
            // empty catch block
        }
        ALIGN_SHORT = 2 - aoShort & 1;
        ALIGN_INT = 4 - aoInt & 3;
        ALIGN_LONG = 8 - aoLong & 7;
        bytesAsShortLE = MethodHandles.byteArrayViewVarHandle(short[].class, ByteOrder.LITTLE_ENDIAN);
        bytesAsShortBE = MethodHandles.byteArrayViewVarHandle(short[].class, ByteOrder.BIG_ENDIAN);
        bytesAsIntLE = MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.LITTLE_ENDIAN);
        bytesAsIntBE = MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.BIG_ENDIAN);
        bytesAsLongLE = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.LITTLE_ENDIAN);
        bytesAsLongBE = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.BIG_ENDIAN);
    }
}

