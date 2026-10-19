/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.testUtil;

import ch.qos.logback.core.testUtil.TeeOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class XTeeOutputStream
extends TeeOutputStream {
    boolean closed = false;

    public XTeeOutputStream(PrintStream targetPS) {
        super(targetPS);
    }

    @Override
    public void close() throws IOException {
        this.closed = true;
        super.close();
    }

    public boolean isClosed() {
        return this.closed;
    }
}

