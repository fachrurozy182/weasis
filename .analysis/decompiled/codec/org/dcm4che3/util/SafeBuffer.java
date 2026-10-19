/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.util;

import java.nio.Buffer;

public class SafeBuffer {
    public static Buffer clear(Buffer buf) {
        return buf.clear();
    }

    public static Buffer flip(Buffer buf) {
        return buf.flip();
    }

    public static Buffer limit(Buffer buf, int newLimit) {
        return buf.limit(newLimit);
    }

    public static Buffer mark(Buffer buf) {
        return buf.mark();
    }

    public static Buffer position(Buffer buf, int newPosition) {
        return buf.position(newPosition);
    }

    public static Buffer reset(Buffer buf) {
        return buf.reset();
    }

    public static Buffer rewind(Buffer buf) {
        return buf.rewind();
    }
}

