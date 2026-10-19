/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.scribejava.core.model.Response
 *  org.weasis.core.util.StreamUtil
 */
package org.weasis.core.api.net;

import com.github.scribejava.core.model.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import org.weasis.core.api.net.HttpStream;
import org.weasis.core.util.StreamUtil;

public record AuthResponse(Response response) implements HttpStream
{
    public AuthResponse {
        Objects.requireNonNull(response, "response");
    }

    @Override
    public void close() {
        StreamUtil.safeClose((AutoCloseable)this.response);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return this.response.getStream();
    }

    @Override
    public int getResponseCode() {
        return this.response.getCode();
    }

    @Override
    public String getResponseMessage() {
        return this.response.getMessage();
    }

    @Override
    public String getHeaderField(String key) {
        return this.response.getHeader(key);
    }
}

