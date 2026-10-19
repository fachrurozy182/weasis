/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class PDVOutputStream
extends OutputStream {
    public abstract void copyFrom(InputStream var1, int var2) throws IOException;

    public abstract void copyFrom(InputStream var1) throws IOException;
}

