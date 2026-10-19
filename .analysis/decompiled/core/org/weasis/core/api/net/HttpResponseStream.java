/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.scribejava.core.model.Response
 *  org.weasis.core.util.StreamUtil
 */
package org.weasis.core.api.net;

import com.github.scribejava.core.model.Response;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.Objects;
import org.weasis.core.api.net.HttpStream;
import org.weasis.core.api.net.StallGuardInputStream;
import org.weasis.core.api.net.auth.JavaNetHttpClient;
import org.weasis.core.util.StreamUtil;

public record HttpResponseStream(Response response) implements HttpStream
{
    public HttpResponseStream {
        Objects.requireNonNull(response, "response");
    }

    public HttpResponseStream(HttpResponse<InputStream> httpResponse) {
        this(httpResponse, 0);
    }

    public HttpResponseStream(HttpResponse<InputStream> httpResponse, int stallTimeoutMillis) {
        this(HttpResponseStream.toResponse(httpResponse, stallTimeoutMillis));
    }

    private static Response toResponse(HttpResponse<InputStream> httpResponse, int stallTimeoutMillis) {
        return new Response(httpResponse.statusCode(), httpResponse.version().toString(), JavaNetHttpClient.parseHeaders(httpResponse), StallGuardInputStream.wrap(httpResponse.body(), stallTimeoutMillis), new Closeable[0]);
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

