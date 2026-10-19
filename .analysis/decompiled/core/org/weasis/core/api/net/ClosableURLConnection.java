/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.core.api.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.Objects;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.net.HttpStream;

public record ClosableURLConnection(URLConnection urlConnection) implements HttpStream
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ClosableURLConnection.class);

    public ClosableURLConnection {
        Objects.requireNonNull(urlConnection, "urlConnection");
    }

    @Override
    public void close() {
        URLConnection uRLConnection = this.urlConnection;
        if (uRLConnection instanceof HttpURLConnection) {
            HttpURLConnection httpConnection = (HttpURLConnection)uRLConnection;
            httpConnection.disconnect();
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return this.urlConnection.getInputStream();
    }

    @Override
    public int getResponseCode() {
        int n;
        URLConnection uRLConnection = this.urlConnection;
        if (uRLConnection instanceof HttpURLConnection) {
            HttpURLConnection http = (HttpURLConnection)uRLConnection;
            n = ClosableURLConnection.safeGet(http::getResponseCode, 200, "response code");
        } else {
            n = 200;
        }
        return n;
    }

    @Override
    public String getResponseMessage() {
        String string;
        URLConnection uRLConnection = this.urlConnection;
        if (uRLConnection instanceof HttpURLConnection) {
            HttpURLConnection http = (HttpURLConnection)uRLConnection;
            string = ClosableURLConnection.safeGet(http::getResponseMessage, "", "response message");
        } else {
            string = "";
        }
        return string;
    }

    @Override
    public String getHeaderField(String key) {
        return this.urlConnection.getHeaderField(key);
    }

    public OutputStream getOutputStream() throws IOException {
        return this.urlConnection.getOutputStream();
    }

    private static <T> T safeGet(Callable<T> supplier, T fallback, String what) {
        try {
            return supplier.call();
        }
        catch (Exception e) {
            LOGGER.warn("Failed to retrieve HTTP {}", (Object)what, (Object)e);
            return fallback;
        }
    }
}

