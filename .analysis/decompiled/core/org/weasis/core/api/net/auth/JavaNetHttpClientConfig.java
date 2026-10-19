/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.scribejava.core.httpclient.HttpClientConfig
 */
package org.weasis.core.api.net.auth;

import com.github.scribejava.core.httpclient.HttpClientConfig;
import java.net.ProxySelector;
import org.weasis.core.api.net.NetworkUtil;

public class JavaNetHttpClientConfig
implements HttpClientConfig {
    private final int connectTimeoutMillis;
    private final int inactivityTimeoutMillis;
    private final ProxySelector proxySelector;

    public JavaNetHttpClientConfig() {
        this(NetworkUtil.getUrlConnectTimeoutMillis(), NetworkUtil.getUrlInactivityTimeoutMillis(), ProxySelector.getDefault());
    }

    public JavaNetHttpClientConfig(int connectTimeoutMillis, int inactivityTimeoutMillis, ProxySelector proxySelector) {
        this.connectTimeoutMillis = connectTimeoutMillis;
        this.inactivityTimeoutMillis = inactivityTimeoutMillis;
        this.proxySelector = proxySelector;
    }

    public int getConnectTimeoutMillis() {
        return this.connectTimeoutMillis;
    }

    public int getInactivityTimeoutMillis() {
        return this.inactivityTimeoutMillis;
    }

    @Deprecated(since="4.7.3")
    public int getConnectTimeout() {
        return this.connectTimeoutMillis;
    }

    @Deprecated(since="4.7.3")
    public int getReadTimeout() {
        return this.inactivityTimeoutMillis;
    }

    public ProxySelector getProxy() {
        return this.proxySelector;
    }

    public HttpClientConfig createDefaultConfig() {
        return new JavaNetHttpClientConfig();
    }
}

