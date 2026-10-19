/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.scribejava.core.httpclient.HttpClient
 *  com.github.scribejava.core.httpclient.HttpClientConfig
 *  com.github.scribejava.core.httpclient.HttpClientProvider
 */
package org.weasis.core.api.net.auth;

import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.httpclient.HttpClientProvider;
import java.util.Objects;
import org.weasis.core.api.net.auth.JavaNetHttpClient;
import org.weasis.core.api.net.auth.JavaNetHttpClientConfig;

public class JavaNetHttpClientProvider
implements HttpClientProvider {
    public HttpClient createClient(HttpClientConfig httpClientConfig) {
        Objects.requireNonNull(httpClientConfig, "HTTP client configuration cannot be null");
        if (httpClientConfig instanceof JavaNetHttpClientConfig) {
            JavaNetHttpClientConfig config = (JavaNetHttpClientConfig)httpClientConfig;
            return new JavaNetHttpClient(config);
        }
        throw new IllegalArgumentException("Unsupported configuration type: " + httpClientConfig.getClass().getName());
    }
}

