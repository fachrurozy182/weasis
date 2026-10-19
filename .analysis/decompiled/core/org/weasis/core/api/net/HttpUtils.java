/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.scribejava.core.httpclient.multipart.MultipartPayload
 *  com.github.scribejava.core.model.OAuthRequest
 *  com.github.scribejava.core.model.Verb
 *  com.github.scribejava.core.oauth.OAuth20Service
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StreamIOException
 */
package org.weasis.core.api.net;

import com.github.scribejava.core.httpclient.multipart.MultipartPayload;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.net.AuthResponse;
import org.weasis.core.api.net.HttpResponseStream;
import org.weasis.core.api.net.HttpStream;
import org.weasis.core.api.net.NetworkUtil;
import org.weasis.core.api.net.RequestStallGuard;
import org.weasis.core.api.net.URLParameters;
import org.weasis.core.api.net.auth.AuthMethod;
import org.weasis.core.api.net.auth.JavaNetHttpClient;
import org.weasis.core.api.net.auth.OAuth2ServiceFactory;
import org.weasis.core.util.StreamIOException;

public final class HttpUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);
    private static final HttpClient SHARED_CLIENT = HttpUtils.buildHttpClient();
    private static final int STALE_RETRY_ATTEMPTS = 3;
    private static final long STALE_RETRY_BACKOFF_MS = 50L;

    private HttpUtils() {
    }

    public static <S> HttpResponse<S> getHttpConnection(URL url, URLParameters urlParameters, HttpResponse.BodyHandler<S> bodyHandler) throws IOException {
        return HttpUtils.createHttpRequest(SHARED_CLIENT, url.toString(), urlParameters, bodyHandler);
    }

    public static <S> HttpResponse<S> getHttpConnection(HttpClient client, URL url, URLParameters urlParameters, HttpResponse.BodyHandler<S> bodyHandler) throws IOException {
        return HttpUtils.createHttpRequest(client, url.toString(), urlParameters, bodyHandler);
    }

    public static HttpClient getDefaulttHttpClient() {
        return SHARED_CLIENT;
    }

    public static HttpClient buildHttpClient() {
        return HttpUtils.buildHttpClient(Duration.ofMillis(NetworkUtil.getUrlConnectionTimeout()));
    }

    public static HttpClient buildHttpClient(Duration timeout) {
        return HttpUtils.buildHttpClient(timeout, HttpClient.Redirect.NORMAL, ProxySelector.getDefault());
    }

    public static HttpClient buildHttpClient(Duration timeout, HttpClient.Redirect redirect, ProxySelector proxySelector) {
        return HttpClient.newBuilder().connectTimeout(timeout).followRedirects(redirect).proxy(proxySelector).build();
    }

    public static HttpStream getHttpResponse(String url, URLParameters urlParameters, AuthMethod authMethod) throws IOException {
        return HttpUtils.getHttpResponse(SHARED_CLIENT, url, urlParameters, authMethod, null);
    }

    public static HttpStream getHttpResponse(HttpClient client, String url, URLParameters urlParameters, AuthMethod authMethod) throws IOException {
        return HttpUtils.getHttpResponse(client, url, urlParameters, authMethod, null);
    }

    public static HttpStream getHttpResponse(String url, URLParameters urlParameters, AuthMethod authMethod, OAuthRequest authRequest) throws IOException {
        return HttpUtils.getHttpResponse(SHARED_CLIENT, url, urlParameters, authMethod, authRequest);
    }

    public static HttpStream getHttpResponse(HttpClient client, String url, URLParameters urlParameters, AuthMethod authMethod, OAuthRequest authRequest) throws IOException {
        if (HttpUtils.isNoAuthRequired(authMethod)) {
            MultipartPayload multipart;
            MultipartPayload multipartPayload = multipart = authRequest == null ? null : authRequest.getMultipartPayload();
            if (multipart != null) {
                return HttpUtils.sendMultipartRequest(client, url, urlParameters, multipart);
            }
            HttpResponse<InputStream> response = HttpUtils.createHttpRequest(client, url, urlParameters, HttpResponse.BodyHandlers.ofInputStream());
            return new HttpResponseStream(response, urlParameters.inactivityTimeoutMillis());
        }
        OAuthRequest request = Objects.requireNonNullElseGet(authRequest, () -> new OAuthRequest(urlParameters.httpPost() ? Verb.POST : Verb.GET, url));
        return HttpUtils.executeAuthenticatedRequest(request, urlParameters, authMethod);
    }

    public static AuthResponse executeAuthenticatedRequest(OAuthRequest request, URLParameters urlParameters, AuthMethod authMethod) throws IOException {
        HttpUtils.applyHeaders(urlParameters.headers(), (arg_0, arg_1) -> ((OAuthRequest)request).addHeader(arg_0, arg_1));
        OAuth20Service service = HttpUtils.getOAuth20Service(authMethod);
        service.signRequest(authMethod.getToken(), request);
        return new AuthResponse(HttpUtils.runInterruptibly(() -> service.execute(request), "Authentication"));
    }

    static boolean isNoAuthRequired(AuthMethod authMethod) {
        return authMethod == null || OAuth2ServiceFactory.NO_AUTH.equals(authMethod);
    }

    private static HttpStream sendMultipartRequest(HttpClient client, String url, URLParameters urlParameters, MultipartPayload multipart) throws IOException {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));
        HttpUtils.applyHeaders(urlParameters.headers(), builder::header);
        RequestStallGuard stallGuard = new RequestStallGuard(urlParameters.inactivityTimeoutMillis());
        JavaNetHttpClient.applyMultipart(builder, multipart, stallGuard);
        HttpResponse<InputStream> response = HttpUtils.send(client, builder.build(), HttpResponse.BodyHandlers.ofInputStream(), stallGuard);
        return new HttpResponseStream(response, urlParameters.inactivityTimeoutMillis());
    }

    private static <S> HttpResponse<S> createHttpRequest(HttpClient client, String url, URLParameters urlParameters, HttpResponse.BodyHandler<S> bodyHandler) throws IOException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(url));
        HttpUtils.applyHeaders(urlParameters.headers(), requestBuilder::header);
        boolean post = urlParameters.httpPost();
        HttpRequest request = post ? requestBuilder.POST(HttpRequest.BodyPublishers.noBody()).build() : requestBuilder.GET().build();
        HttpResponse<S> response = HttpUtils.sendWithStaleConnectionRetry(client, request, bodyHandler, urlParameters.inactivityTimeoutMillis());
        if (!post) {
            HttpUtils.validateResponseStatus(response.statusCode());
        }
        return response;
    }

    private static <S> HttpResponse<S> send(HttpClient client, HttpRequest request, HttpResponse.BodyHandler<S> bodyHandler, RequestStallGuard stallGuard) throws IOException {
        CompletableFuture<HttpResponse<S>> future = stallGuard.guard(client.sendAsync(request, bodyHandler));
        try {
            return future.get();
        }
        catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw new StreamIOException("Request interrupted", (Throwable)e);
        }
        catch (ExecutionException e) {
            Object object;
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                IOException io = (IOException)cause;
                object = io;
            } else {
                object = new StreamIOException("Request failed", cause);
            }
            throw object;
        }
    }

    private static <S> HttpResponse<S> sendWithStaleConnectionRetry(HttpClient client, HttpRequest request, HttpResponse.BodyHandler<S> bodyHandler, int inactivityTimeoutMillis) throws IOException {
        IOException last = null;
        for (int attempt = 0; attempt <= 3; ++attempt) {
            try {
                return HttpUtils.send(client, request, bodyHandler, new RequestStallGuard(inactivityTimeoutMillis));
            }
            catch (IOException e) {
                last = e;
                if (!HttpUtils.isIdempotent(request) || !HttpUtils.isStaleConnectionFailure(e)) {
                    throw e;
                }
                if (attempt == 3) {
                    LOGGER.error("Stale connection retries exhausted ({} attempts) for {}", (Object)3, (Object)request.uri());
                    throw e;
                }
                LOGGER.warn("Stale pooled connection detected for {} (attempt {}/{}, {})", new Object[]{request.uri(), attempt + 1, 3, e.getMessage()});
                HttpUtils.sleepQuietly(50L * (long)(attempt + 1));
                continue;
            }
        }
        throw last;
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    static boolean isIdempotent(HttpRequest request) {
        String method = request.method();
        return "GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method);
    }

    static boolean isStaleConnectionFailure(Throwable t) {
        for (Throwable cur = t; cur != null; cur = cur.getCause()) {
            if (cur instanceof SocketException && HttpUtils.containsIgnoreCase(cur.getMessage(), "connection reset")) {
                return true;
            }
            if (!HttpUtils.containsIgnoreCase(cur.getMessage(), "header parser received no bytes")) continue;
            return true;
        }
        return false;
    }

    private static boolean containsIgnoreCase(String haystack, String needle) {
        return haystack != null && haystack.toLowerCase().contains(needle.toLowerCase());
    }

    static void applyHeaders(Map<String, String> headers, HeaderSetter setter) {
        headers.forEach((k, v) -> HttpUtils.setIfPresent(setter, k, v));
        HttpUtils.setIfPresent(setter, "User-Agent", AppProperties.WEASIS_USER_AGENT);
        HttpUtils.setIfPresent(setter, "Weasis-User", AppProperties.WEASIS_USER);
    }

    private static void setIfPresent(HeaderSetter setter, String name, String value) {
        if (value != null) {
            setter.set(name, value);
        }
    }

    private static <T> T runInterruptibly(Callable<T> task, String what) throws IOException {
        try {
            return task.call();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new StreamIOException(what + " interrupted", (Throwable)e);
        }
        catch (IOException e) {
            throw e;
        }
        catch (Exception e) {
            throw new StreamIOException(what + " failed", (Throwable)e);
        }
    }

    private static void validateResponseStatus(int statusCode) throws IOException {
        if (statusCode != 200) {
            throw new IOException("HTTP request failed with status code: " + statusCode);
        }
    }

    private static OAuth20Service getOAuth20Service(AuthMethod authMethod) throws IOException {
        OAuth20Service service = OAuth2ServiceFactory.getService(authMethod);
        if (service == null) {
            throw new IOException("Invalid authentication method: " + String.valueOf(authMethod));
        }
        return service;
    }

    @FunctionalInterface
    static interface HeaderSetter {
        public void set(String var1, String var2);
    }
}

