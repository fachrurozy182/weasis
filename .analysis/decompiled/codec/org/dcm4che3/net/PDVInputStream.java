/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.dcm4che3.data.Attributes;

public abstract class PDVInputStream
extends InputStream {
    public abstract Attributes readDataset(String var1) throws IOException;

    public abstract void copyTo(OutputStream var1, int var2) throws IOException;

    public abstract void copyTo(OutputStream var1) throws IOException;

    public abstract long skipAll() throws IOException;
}

