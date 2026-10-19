/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.net;

import java.io.IOException;
import java.io.InputStream;

public interface HttpStream
extends AutoCloseable {
    public InputStream getInputStream() throws IOException;

    public int getResponseCode();

    public String getHeaderField(String var1);

    public String getResponseMessage();
}

