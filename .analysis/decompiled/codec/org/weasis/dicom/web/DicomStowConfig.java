/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.web;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.weasis.dicom.web.ContentType;

public final class DicomStowConfig {
    private static final String DEFAULT_USER_AGENT = "Weasis STOW-RS Client";
    private static final int DEFAULT_THREAD_POOL_SIZE = 5;
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10L);
    static final HttpClient.Version DEFAULT_HTTP_VERSION = HttpClient.Version.HTTP_1_1;
    private final String requestUrl;
    private final ContentType contentType;
    private final String userAgent;
    private final Map<String, String> headers;
    private final int threadPoolSize;
    private final Duration connectTimeout;
    private final HttpClient.Version httpVersion;

    private DicomStowConfig(Builder builder) {
        this.requestUrl = this.normalizeUrl(builder.requestUrl);
        this.contentType = builder.contentType;
        this.userAgent = builder.userAgent;
        this.headers = Map.copyOf(builder.headers);
        this.threadPoolSize = builder.threadPoolSize;
        this.connectTimeout = builder.connectTimeout;
        this.httpVersion = builder.httpVersion;
    }

    public String getRequestUrl() {
        return this.requestUrl;
    }

    public ContentType getContentType() {
        return this.contentType;
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public int getThreadPoolSize() {
        return this.threadPoolSize;
    }

    public Duration getConnectTimeout() {
        return this.connectTimeout;
    }

    public HttpClient.Version getHttpVersion() {
        return this.httpVersion;
    }

    public static Builder builder() {
        return new Builder();
    }

    private String normalizeUrl(String url) {
        Objects.requireNonNull(url, "Request URL cannot be null");
        Object normalized = url.trim();
        if (((String)normalized).endsWith("/")) {
            normalized = ((String)normalized).substring(0, ((String)normalized).length() - 1);
        }
        if (!((String)normalized).endsWith("/studies")) {
            normalized = (String)normalized + "/studies";
        }
        return normalized;
    }

    public static final class Builder {
        private String requestUrl;
        private ContentType contentType = ContentType.APPLICATION_DICOM;
        private String userAgent = "Weasis STOW-RS Client";
        private final Map<String, String> headers = new HashMap<String, String>();
        private int threadPoolSize = 5;
        private Duration connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        private HttpClient.Version httpVersion = DEFAULT_HTTP_VERSION;

        private Builder() {
        }

        public Builder requestUrl(String requestUrl) {
            this.requestUrl = requestUrl;
            return this;
        }

        public Builder contentType(ContentType contentType) {
            this.contentType = Objects.requireNonNull(contentType);
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent != null ? userAgent : DicomStowConfig.DEFAULT_USER_AGENT;
            return this;
        }

        public Builder header(String name, String value) {
            Objects.requireNonNull(name, "Header name cannot be null");
            if (value != null) {
                this.headers.put(name, value);
            }
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            if (headers != null) {
                this.headers.putAll(headers);
            }
            return this;
        }

        public Builder threadPoolSize(int threadPoolSize) {
            if (threadPoolSize <= 0) {
                throw new IllegalArgumentException("Thread pool size must be positive");
            }
            this.threadPoolSize = threadPoolSize;
            return this;
        }

        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = Objects.requireNonNull(connectTimeout);
            return this;
        }

        public Builder httpVersion(HttpClient.Version httpVersion) {
            this.httpVersion = Objects.requireNonNull(httpVersion);
            return this;
        }

        public DicomStowConfig build() {
            Objects.requireNonNull(this.requestUrl, "Request URL is required");
            return new DicomStowConfig(this);
        }
    }
}

