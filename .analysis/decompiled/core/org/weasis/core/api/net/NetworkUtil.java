/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.scribejava.core.model.OAuthRequest
 *  com.github.scribejava.core.model.Verb
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StreamIOException
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.api.net;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.net.ClosableURLConnection;
import org.weasis.core.api.net.HttpStream;
import org.weasis.core.api.net.HttpUtils;
import org.weasis.core.api.net.URLParameters;
import org.weasis.core.api.net.auth.AuthMethod;
import org.weasis.core.util.StreamIOException;
import org.weasis.core.util.StringUtil;

public final class NetworkUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkUtil.class);
    private static final int MAX_REDIRECTS = 3;
    private static final String HTTP_SCHEME_PREFIX = "http";
    private static final String HEADER_LOCATION = "Location";
    private static final String HEADER_COOKIE = "Cookie";
    private static final String HEADER_SET_COOKIE = "Set-Cookie";
    private static final Set<Integer> REDIRECT_CODES = Set.of(Integer.valueOf(302), Integer.valueOf(301), Integer.valueOf(303));

    private NetworkUtil() {
    }

    public static int getUrlConnectTimeoutMillis() {
        return StringUtil.getInt((String)System.getProperty("UrlConnectionTimeout"), (int)5000);
    }

    public static int getUrlInactivityTimeoutMillis() {
        return StringUtil.getInt((String)System.getProperty("UrlReadTimeout"), (int)15000);
    }

    @Deprecated(since="4.7.3")
    public static int getUrlConnectionTimeout() {
        return NetworkUtil.getUrlConnectTimeoutMillis();
    }

    @Deprecated(since="4.7.3")
    public static int getUrlReadTimeout() {
        return NetworkUtil.getUrlInactivityTimeoutMillis();
    }

    public static URI getURI(String pathOrUri) {
        URI fileUri;
        if (!pathOrUri.startsWith(HTTP_SCHEME_PREFIX) && (fileUri = NetworkUtil.tryCreateFileUri(pathOrUri)) != null) {
            return fileUri;
        }
        return URI.create(pathOrUri);
    }

    private static URI tryCreateFileUri(String pathOrUri) {
        try {
            Path path = Path.of(pathOrUri, new String[0]);
            return Files.isReadable(path) ? path.toUri() : null;
        }
        catch (Exception e) {
            LOGGER.debug("Failed to create file URI for path: {}", (Object)pathOrUri, (Object)e);
            return null;
        }
    }

    public static HttpStream getHttpResponse(String url, URLParameters urlParameters, AuthMethod authMethod) throws IOException {
        return NetworkUtil.getHttpResponse(url, urlParameters, authMethod, null);
    }

    public static HttpStream getHttpResponse(String url, URLParameters urlParameters, AuthMethod authMethod, OAuthRequest authRequest) throws IOException {
        if (HttpUtils.isNoAuthRequired(authMethod)) {
            return NetworkUtil.prepareConnection(URI.create(url).toURL().openConnection(), urlParameters);
        }
        OAuthRequest request = Objects.requireNonNullElseGet(authRequest, () -> new OAuthRequest(urlParameters.httpPost() ? Verb.POST : Verb.GET, url));
        return HttpUtils.executeAuthenticatedRequest(request, urlParameters, authMethod);
    }

    public static ClosableURLConnection getUrlConnection(String url, URLParameters urlParameters) throws IOException {
        return NetworkUtil.prepareConnection(URI.create(url).toURL().openConnection(), urlParameters);
    }

    public static ClosableURLConnection getUrlConnection(URL url, URLParameters urlParameters) throws IOException {
        return NetworkUtil.prepareConnection(url.openConnection(), urlParameters);
    }

    private static ClosableURLConnection prepareConnection(URLConnection urlConnection, URLParameters urlParameters) throws StreamIOException {
        HttpUtils.applyHeaders(urlParameters.headers(), urlConnection::setRequestProperty);
        NetworkUtil.configureConnection(urlConnection, urlParameters);
        if (urlConnection instanceof HttpURLConnection) {
            HttpURLConnection httpConn = (HttpURLConnection)urlConnection;
            return NetworkUtil.handleHttpConnection(httpConn, urlParameters);
        }
        return new ClosableURLConnection(urlConnection);
    }

    private static void configureConnection(URLConnection connection, URLParameters parameters) {
        connection.setConnectTimeout(parameters.connectTimeoutMillis());
        connection.setReadTimeout(parameters.inactivityTimeoutMillis());
        connection.setAllowUserInteraction(parameters.allowUserInteraction());
        connection.setUseCaches(parameters.useCaches());
        connection.setIfModifiedSince(parameters.ifModifiedSince());
        connection.setDoInput(true);
        connection.setDoOutput(parameters.httpPost());
    }

    private static ClosableURLConnection handleHttpConnection(HttpURLConnection httpConn, URLParameters parameters) throws StreamIOException {
        try {
            if (parameters.httpPost()) {
                httpConn.setRequestMethod("POST");
                return new ClosableURLConnection(httpConn);
            }
            return new ClosableURLConnection(NetworkUtil.readResponse(httpConn, parameters.headers()));
        }
        catch (IOException e) {
            StreamIOException s;
            throw e instanceof StreamIOException ? (s = (StreamIOException)e) : new StreamIOException((Throwable)e);
        }
    }

    public static URLConnection readResponse(HttpURLConnection httpConnection, Map<String, String> headers) throws IOException {
        int responseCode = httpConnection.getResponseCode();
        if (NetworkUtil.isSuccessResponse(responseCode)) {
            return httpConnection;
        }
        if (REDIRECT_CODES.contains(responseCode)) {
            return NetworkUtil.applyRedirectionStream(httpConnection, headers);
        }
        NetworkUtil.logErrorResponse(httpConnection, responseCode);
        throw new StreamIOException(httpConnection.getResponseMessage());
    }

    private static boolean isSuccessResponse(int code) {
        return code >= 200 && code < 300;
    }

    public static String read(URLConnection urlConnection) throws IOException {
        urlConnection.connect();
        try (InputStream inputStream = urlConnection.getInputStream();){
            String string = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return string;
        }
    }

    public static URLConnection applyRedirectionStream(URLConnection urlConnection, Map<String, String> headers) throws IOException {
        URLConnection connection = urlConnection;
        String redirectUrl = connection.getHeaderField(HEADER_LOCATION);
        for (int i = 0; i < 3 && redirectUrl != null; ++i) {
            connection = NetworkUtil.followRedirect(connection, redirectUrl, headers);
            redirectUrl = connection.getHeaderField(HEADER_LOCATION);
        }
        return connection;
    }

    private static URLConnection followRedirect(URLConnection current, String redirectUrl, Map<String, String> headers) throws IOException {
        String cookies = current.getHeaderField(HEADER_SET_COOKIE);
        if (current instanceof HttpURLConnection) {
            HttpURLConnection httpConn = (HttpURLConnection)current;
            httpConn.disconnect();
        }
        URLConnection newConnection = URI.create(redirectUrl).toURL().openConnection();
        if (cookies != null) {
            newConnection.setRequestProperty(HEADER_COOKIE, cookies);
        }
        if (headers != null && !headers.isEmpty()) {
            headers.forEach(newConnection::addRequestProperty);
        }
        return newConnection;
    }

    public static boolean isValidUrlLikeUri(String uri) {
        try {
            URI parsed = URI.create(uri);
            return parsed.getScheme() != null && parsed.getHost() != null;
        }
        catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static void logErrorResponse(HttpURLConnection connection, int code) throws IOException {
        LOGGER.warn("HTTP Status {} - {}", (Object)code, (Object)connection.getResponseMessage());
        if (!LOGGER.isTraceEnabled()) {
            return;
        }
        try (InputStream errorStream = connection.getErrorStream();){
            if (errorStream == null) {
                return;
            }
            String errorContent = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
            if (StringUtil.hasText((CharSequence)errorContent)) {
                LOGGER.trace("HttpURLConnection ERROR, server response: {}", (Object)errorContent);
            }
        }
    }
}

