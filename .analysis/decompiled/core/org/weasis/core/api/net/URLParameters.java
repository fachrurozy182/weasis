/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.net;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.weasis.core.api.net.NetworkUtil;

public record URLParameters(Map<String, String> headers, long ifModifiedSince, int connectTimeoutMillis, int inactivityTimeoutMillis, boolean httpPost, boolean useCaches, boolean allowUserInteraction) {
    public static final URLParameters DEFAULT = new URLParameters();

    public URLParameters {
        if (connectTimeoutMillis < 0) {
            throw new IllegalArgumentException("Connect timeout cannot be negative: " + connectTimeoutMillis);
        }
        if (inactivityTimeoutMillis < 0) {
            throw new IllegalArgumentException("Inactivity timeout cannot be negative: " + inactivityTimeoutMillis);
        }
        if (ifModifiedSince < 0L) {
            throw new IllegalArgumentException("If-Modified-Since cannot be negative: " + ifModifiedSince);
        }
        headers = headers == null ? Collections.emptyMap() : Map.copyOf(headers);
    }

    public URLParameters() {
        this(Collections.emptyMap());
    }

    public URLParameters(Map<String, String> headers) {
        this(headers, false);
    }

    public URLParameters(Map<String, String> headers, boolean httpPost) {
        this(headers, 0L, NetworkUtil.getUrlConnectTimeoutMillis(), NetworkUtil.getUrlInactivityTimeoutMillis(), httpPost, true, false);
    }

    public URLParameters(Map<String, String> headers, int connectTimeoutMillis, int inactivityTimeoutMillis) {
        this(headers, 0L, connectTimeoutMillis, inactivityTimeoutMillis, false, true, false);
    }

    @Deprecated(since="4.7.3")
    public int connectTimeout() {
        return this.connectTimeoutMillis;
    }

    @Deprecated(since="4.7.3")
    public int readTimeout() {
        return this.inactivityTimeoutMillis;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static Map<String, String> splitParameter(URL url) {
        String query = URLParameters.queryOf(url);
        if (query.isEmpty()) {
            return Collections.emptyMap();
        }
        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        for (String pair : query.split("&")) {
            String[] kv = URLParameters.parsePair(pair, "");
            result.put(kv[0], kv[1]);
        }
        return Map.copyOf(result);
    }

    public static Map<String, List<String>> splitMultipleValuesParameter(URL url) {
        String query = URLParameters.queryOf(url);
        if (query.isEmpty()) {
            return Collections.emptyMap();
        }
        LinkedHashMap<String, List> result = new LinkedHashMap<String, List>();
        for (String pair : query.split("&")) {
            String[] kv = URLParameters.parsePair(pair, null);
            result.computeIfAbsent(kv[0], string -> new ArrayList()).add(kv[1]);
        }
        return Map.copyOf(result);
    }

    private static String queryOf(URL url) {
        Objects.requireNonNull(url, "url");
        String query = url.getQuery();
        return query == null ? "" : query;
    }

    private static String[] parsePair(String pair, String missingValue) {
        int idx = pair.indexOf(61);
        String key = URLParameters.decodeUrlComponent(idx > 0 ? pair.substring(0, idx) : pair);
        String value = idx > 0 && pair.length() > idx + 1 ? URLParameters.decodeUrlComponent(pair.substring(idx + 1)) : missingValue;
        return new String[]{key, value};
    }

    private static String decodeUrlComponent(String component) {
        return URLDecoder.decode(component, StandardCharsets.UTF_8);
    }

    public static final class Builder {
        private Map<String, String> headers = Collections.emptyMap();
        private long ifModifiedSince = 0L;
        private int connectTimeoutMillis = NetworkUtil.getUrlConnectTimeoutMillis();
        private int inactivityTimeoutMillis = NetworkUtil.getUrlInactivityTimeoutMillis();
        private boolean httpPost = false;
        private boolean useCaches = true;
        private boolean allowUserInteraction = false;

        Builder() {
        }

        Builder(URLParameters parameters) {
            this.headers = parameters.headers;
            this.ifModifiedSince = parameters.ifModifiedSince;
            this.connectTimeoutMillis = parameters.connectTimeoutMillis;
            this.inactivityTimeoutMillis = parameters.inactivityTimeoutMillis;
            this.httpPost = parameters.httpPost;
            this.useCaches = parameters.useCaches;
            this.allowUserInteraction = parameters.allowUserInteraction;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder ifModifiedSince(long ifModifiedSince) {
            this.ifModifiedSince = ifModifiedSince;
            return this;
        }

        public Builder connectTimeoutMillis(int connectTimeoutMillis) {
            this.connectTimeoutMillis = connectTimeoutMillis;
            return this;
        }

        public Builder inactivityTimeoutMillis(int inactivityTimeoutMillis) {
            this.inactivityTimeoutMillis = inactivityTimeoutMillis;
            return this;
        }

        @Deprecated(since="4.7.3")
        public Builder connectTimeout(int connectTimeout) {
            return this.connectTimeoutMillis(connectTimeout);
        }

        @Deprecated(since="4.7.3")
        public Builder readTimeout(int readTimeout) {
            return this.inactivityTimeoutMillis(readTimeout);
        }

        public Builder httpPost(boolean httpPost) {
            this.httpPost = httpPost;
            return this;
        }

        public Builder useCaches(boolean useCaches) {
            this.useCaches = useCaches;
            return this;
        }

        public Builder allowUserInteraction(boolean allowUserInteraction) {
            this.allowUserInteraction = allowUserInteraction;
            return this;
        }

        public URLParameters build() {
            return new URLParameters(this.headers, this.ifModifiedSince, this.connectTimeoutMillis, this.inactivityTimeoutMillis, this.httpPost, this.useCaches, this.allowUserInteraction);
        }
    }
}

