/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.testUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class TeeOutputStream
extends OutputStream {
    final PrintStream targetPS;
    public final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    public TeeOutputStream(PrintStream targetPS) {
        this.targetPS = targetPS;
    }

    @Override
    public void write(int b) throws IOException {
        this.baos.write(b);
        if (this.targetPS != null) {
            this.targetPS.write(b);
        }
    }

    public String toString() {
        return this.baos.toString();
    }

    public byte[] toByteArray() {
        return this.baos.toByteArray();
    }
}

