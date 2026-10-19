/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.scribejava.core.httpclient.HttpClient
 *  com.github.scribejava.core.httpclient.multipart.BodyPartPayload
 *  com.github.scribejava.core.httpclient.multipart.ByteArrayBodyPartPayload
 *  com.github.scribejava.core.httpclient.multipart.MultipartPayload
 *  com.github.scribejava.core.model.OAuthAsyncRequestCallback
 *  com.github.scribejava.core.model.OAuthRequest$ResponseConverter
 *  com.github.scribejava.core.model.Response
 *  com.github.scribejava.core.model.Verb
 *  java.lang.runtime.SwitchBootstraps
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.api.net.auth;

import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.multipart.BodyPartPayload;
import com.github.scribejava.core.httpclient.multipart.ByteArrayBodyPartPayload;
import com.github.scribejava.core.httpclient.multipart.MultipartPayload;
import com.github.scribejava.core.model.OAuthAsyncRequestCallback;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.runtime.SwitchBootstraps;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.net.HttpUtils;
import org.weasis.core.api.net.RequestStallGuard;
import org.weasis.core.api.net.StallGuardInputStream;
import org.weasis.core.api.net.auth.BodySupplier;
import org.weasis.core.api.net.auth.FileBodyPartPayload;
import org.weasis.core.api.net.auth.JavaNetHttpClientConfig;
import org.weasis.core.util.StringUtil;

public class JavaNetHttpClient
implements HttpClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(JavaNetHttpClient.class);
    private static final String CRLF = "\r\n";
    private static final String BOUNDARY_PREFIX = "--";
    private static final String HEADER_USER_AGENT = "User-Agent";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String DEFAULT_FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String MULTIPART_CT_PREFIX = "multipart/form-data; boundary=";
    private final java.net.http.HttpClient sharedClient;
    private final Duration inactivityTimeout;

    public JavaNetHttpClient() {
        this(new JavaNetHttpClientConfig());
    }

    public JavaNetHttpClient(JavaNetHttpClientConfig config) {
        this.sharedClient = HttpUtils.buildHttpClient(Duration.ofMillis(config.getConnectTimeoutMillis()), HttpClient.Redirect.NORMAL, config.getProxy());
        this.inactivityTimeout = Duration.ofMillis(config.getInactivityTimeoutMillis());
    }

    private RequestStallGuard newStallGuard() {
        return new RequestStallGuard((int)this.inactivityTimeout.toMillis());
    }

    public void close() {
    }

    public static void applyMultipart(HttpRequest.Builder builder, MultipartPayload payload, RequestStallGuard stallGuard) {
        builder.POST(stallGuard.track(MultipartEncoder.applyTo(builder, payload)));
    }

    public <T> Future<T> executeAsync(String userAgent, Map<String, String> headers, Verb httpVerb, String completeUrl, byte[] bodyContents, OAuthAsyncRequestCallback<T> callback, OAuthRequest.ResponseConverter<T> converter) {
        return this.doExecuteAsync(userAgent, headers, httpVerb, completeUrl, bodyContents, callback, converter);
    }

    public <T> Future<T> executeAsync(String userAgent, Map<String, String> headers, Verb httpVerb, String completeUrl, MultipartPayload bodyContents, OAuthAsyncRequestCallback<T> callback, OAuthRequest.ResponseConverter<T> converter) {
        return this.doExecuteAsync(userAgent, headers, httpVerb, completeUrl, bodyContents, callback, converter);
    }

    public <T> Future<T> executeAsync(String userAgent, Map<String, String> headers, Verb httpVerb, String completeUrl, String bodyContents, OAuthAsyncRequestCallback<T> callback, OAuthRequest.ResponseConverter<T> converter) {
        return this.doExecuteAsync(userAgent, headers, httpVerb, completeUrl, bodyContents, callback, converter);
    }

    public <T> Future<T> executeAsync(String userAgent, Map<String, String> headers, Verb httpVerb, String completeUrl, File bodyContents, OAuthAsyncRequestCallback<T> callback, OAuthRequest.ResponseConverter<T> converter) {
        return this.doExecuteAsync(userAgent, headers, httpVerb, completeUrl, bodyContents.toPath(), callback, converter);
    }

    public Response execute(String userAgent, Map<String, String> headers, Verb httpVerb, String completeUrl, byte[] bodyContents) throws InterruptedException, ExecutionException, IOException {
        return this.doExecute(userAgent, headers, httpVerb, completeUrl, bodyContents);
    }

    public Response execute(String userAgent, Map<String, String> headers, Verb httpVerb, String completeUrl, MultipartPayload multipartPayloads) throws InterruptedException, ExecutionException, IOException {
        return this.doExecute(userAgent, headers, httpVerb, completeUrl, multipartPayloads);
    }

    public Response execute(String userAgent, Map<String, String> headers, Verb httpVerb, String completeUrl, String bodyContents) throws InterruptedException, ExecutionException, IOException {
        return this.doExecute(userAgent, headers, httpVerb, completeUrl, bodyContents);
    }

    public Response execute(String userAgent, Map<String, String> headers, Verb httpVerb, String completeUrl, File bodyContents) throws InterruptedException, ExecutionException, IOException {
        return this.doExecute(userAgent, headers, httpVerb, completeUrl, bodyContents.toPath());
    }

    private <T> CompletableFuture<T> doExecuteAsync(String userAgent, Map<String, String> headers, Verb httpVerb, String completeUrl, Object bodyContents, OAuthAsyncRequestCallback<T> callback, OAuthRequest.ResponseConverter<T> converter) {
        RequestStallGuard stallGuard = this.newStallGuard();
        HttpRequest request = JavaNetHttpClient.createRequestBuilder(userAgent, headers, httpVerb, completeUrl, bodyContents, stallGuard).build();
        return ((CompletableFuture)stallGuard.guard(this.sharedClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())).thenApply(httpResponse -> this.processAsyncResponse((HttpResponse<InputStream>)httpResponse, callback, converter))).exceptionally(throwable -> {
            RuntimeException runtimeException;
            Throwable cause = JavaNetHttpClient.unwrap(throwable);
            LOGGER.warn("Async HTTP request to {} failed", (Object)completeUrl, (Object)cause);
            if (callback != null) {
                callback.onThrowable(cause);
            }
            if (cause instanceof RuntimeException) {
                RuntimeException re = (RuntimeException)cause;
                runtimeException = re;
            } else {
                runtimeException = new CompletionException(cause);
            }
            throw runtimeException;
        });
    }

    private static Throwable unwrap(Throwable t) {
        Throwable current = t;
        while ((current instanceof CompletionException || current instanceof UncheckedIOException) && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private Response doExecute(String userAgent, Map<String, String> headers, Verb httpVerb, String completeUrl, Object bodyContents) throws IOException {
        RequestStallGuard stallGuard = this.newStallGuard();
        HttpRequest request = JavaNetHttpClient.createRequestBuilder(userAgent, headers, httpVerb, completeUrl, bodyContents, stallGuard).build();
        CompletableFuture<HttpResponse<InputStream>> future = stallGuard.guard(this.sharedClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream()));
        try {
            return this.toResponse(future.get());
        }
        catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
        catch (ExecutionException e) {
            IOException io;
            Throwable cause = JavaNetHttpClient.unwrap(e.getCause());
            throw cause instanceof IOException ? (io = (IOException)cause) : new IOException("Request failed", cause);
        }
    }

    private Response toResponse(HttpResponse<InputStream> httpResponse) {
        return new Response(httpResponse.statusCode(), httpResponse.version().toString(), JavaNetHttpClient.parseHeaders(httpResponse), StallGuardInputStream.wrap(httpResponse.body(), (int)this.inactivityTimeout.toMillis()), new Closeable[0]);
    }

    private <T> T processAsyncResponse(HttpResponse<InputStream> httpResponse, OAuthAsyncRequestCallback<T> callback, OAuthRequest.ResponseConverter<T> converter) {
        try {
            HttpResponse<InputStream> result;
            HttpResponse<InputStream> httpResponse2 = result = converter == null ? httpResponse : converter.convert(this.toResponse(httpResponse));
            if (callback != null) {
                callback.onCompleted(result);
            }
            return (T)result;
        }
        catch (IOException e) {
            LOGGER.warn("Failed to process async HTTP response (status={})", (Object)httpResponse.statusCode(), (Object)e);
            throw new UncheckedIOException(e);
        }
    }

    private static HttpRequest.Builder createRequestBuilder(String userAgent, Map<String, String> headers, Verb httpVerb, String completeUrl, Object bodyContents, RequestStallGuard stallGuard) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(completeUrl));
        if (StringUtil.hasText((CharSequence)userAgent)) {
            requestBuilder.setHeader(HEADER_USER_AGENT, userAgent);
        }
        boolean callerSetContentType = false;
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.setHeader(entry.getKey(), entry.getValue());
                if (!HEADER_CONTENT_TYPE.equalsIgnoreCase(entry.getKey())) continue;
                callerSetContentType = true;
            }
        }
        if (!callerSetContentType && httpVerb.isPermitBody() && JavaNetHttpClient.needsFormContentType(bodyContents)) {
            requestBuilder.setHeader(HEADER_CONTENT_TYPE, DEFAULT_FORM_CONTENT_TYPE);
        }
        JavaNetHttpClient.setBody(requestBuilder, bodyContents, httpVerb, stallGuard);
        return requestBuilder;
    }

    private static boolean needsFormContentType(Object bodyContents) {
        return bodyContents instanceof byte[] || bodyContents instanceof String || bodyContents == null;
    }

    public static Map<String, String> parseHeaders(HttpResponse<?> response) {
        return response.headers().map().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> String.join((CharSequence)", ", (Iterable)e.getValue()), (existing, replacement) -> existing, () -> new TreeMap(String.CASE_INSENSITIVE_ORDER)));
    }

    private static void setBody(HttpRequest.Builder requestBuilder, Object bodyContents, Verb httpVerb, RequestStallGuard stallGuard) {
        if (!httpVerb.isPermitBody()) {
            requestBuilder.method(httpVerb.name(), HttpRequest.BodyPublishers.noBody());
            return;
        }
        Object object = bodyContents;
        int n = 0;
        HttpRequest.BodyPublisher publisher = switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{byte[].class, String.class, Path.class, MultipartPayload.class}, (Object)object, (int)n)) {
            case -1 -> {
                if (httpVerb.isRequiresBody()) {
                    throw new IllegalArgumentException("Body content is required but null");
                }
                yield HttpRequest.BodyPublishers.noBody();
            }
            case 0 -> {
                byte[] bytes = (byte[])object;
                yield HttpRequest.BodyPublishers.ofByteArray(bytes);
            }
            case 1 -> {
                String str = (String)object;
                yield HttpRequest.BodyPublishers.ofByteArray(str.getBytes(StandardCharsets.UTF_8));
            }
            case 2 -> {
                Path path = (Path)object;
                yield MultipartEncoder.filePublisher(path);
            }
            case 3 -> {
                MultipartPayload multi = (MultipartPayload)object;
                yield MultipartEncoder.applyTo(requestBuilder, multi);
            }
            default -> throw new IllegalArgumentException("Unsupported body type: " + String.valueOf(bodyContents.getClass()));
        };
        requestBuilder.method(httpVerb.name(), stallGuard.track(publisher));
    }

    private static final class MultipartEncoder {
        private MultipartEncoder() {
        }

        static HttpRequest.BodyPublisher filePublisher(Path path) {
            try {
                return HttpRequest.BodyPublishers.ofFile(path);
            }
            catch (IOException e) {
                throw new UncheckedIOException("Failed to read file: " + String.valueOf(path), e);
            }
        }

        static HttpRequest.BodyPublisher applyTo(HttpRequest.Builder requestBuilder, MultipartPayload payload) {
            ArrayList<BodySupplier<InputStream>> parts = new ArrayList<BodySupplier<InputStream>>();
            MultipartEncoder.collectParts(parts, payload);
            try {
                byte[] body = MultipartEncoder.serialize(parts);
                requestBuilder.setHeader(JavaNetHttpClient.HEADER_CONTENT_TYPE, MultipartEncoder.resolveContentType(payload));
                return HttpRequest.BodyPublishers.ofByteArray(body);
            }
            catch (IOException e) {
                throw new UncheckedIOException("Failed to prepare multipart payload", e);
            }
        }

        private static String resolveContentType(MultipartPayload payload) {
            Map headers = payload.getHeaders();
            if (headers != null) {
                for (Map.Entry entry : headers.entrySet()) {
                    if (!JavaNetHttpClient.HEADER_CONTENT_TYPE.equalsIgnoreCase((String)entry.getKey()) || !StringUtil.hasText((CharSequence)((CharSequence)entry.getValue()))) continue;
                    return (String)entry.getValue();
                }
            }
            return JavaNetHttpClient.MULTIPART_CT_PREFIX + payload.getBoundary();
        }

        private static byte[] serialize(List<BodySupplier<InputStream>> parts) throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            for (BodySupplier<InputStream> supplier : parts) {
                InputStream in = supplier.get();
                try {
                    in.transferTo(out);
                }
                finally {
                    if (in == null) continue;
                    in.close();
                }
            }
            return out.toByteArray();
        }

        private static void collectParts(List<BodySupplier<InputStream>> parts, MultipartPayload payload) {
            List bodyParts;
            if (payload.getPreamble() != null) {
                parts.add(BodySupplier.ofString(payload.getPreamble() + JavaNetHttpClient.CRLF));
            }
            if ((bodyParts = payload.getBodyParts()).isEmpty()) {
                parts.add(BodySupplier.empty());
                return;
            }
            String boundary = payload.getBoundary();
            for (BodyPartPayload bodyPart : bodyParts) {
                parts.add(BodySupplier.ofString(MultipartEncoder.renderBoundaryAndHeaders(bodyPart, boundary)));
                MultipartEncoder.appendContent(parts, bodyPart);
                parts.add(BodySupplier.ofString(JavaNetHttpClient.CRLF));
            }
            parts.add(BodySupplier.ofString(JavaNetHttpClient.BOUNDARY_PREFIX + boundary + JavaNetHttpClient.BOUNDARY_PREFIX));
            if (payload.getEpilogue() != null) {
                parts.add(BodySupplier.ofString(JavaNetHttpClient.CRLF + payload.getEpilogue()));
            }
        }

        private static String renderBoundaryAndHeaders(BodyPartPayload bodyPart, String boundary) {
            StringBuilder buf = new StringBuilder().append(JavaNetHttpClient.BOUNDARY_PREFIX).append(boundary).append(JavaNetHttpClient.CRLF);
            Map headers = bodyPart.getHeaders();
            if (headers != null) {
                headers.forEach((k, v) -> buf.append((String)k).append(": ").append((String)v).append(JavaNetHttpClient.CRLF));
            }
            return buf.append(JavaNetHttpClient.CRLF).toString();
        }

        private static void appendContent(List<BodySupplier<InputStream>> parts, BodyPartPayload bodyPart) {
            BodyPartPayload bodyPartPayload = bodyPart;
            Objects.requireNonNull(bodyPartPayload);
            BodyPartPayload bodyPartPayload2 = bodyPartPayload;
            int n = 0;
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{MultipartPayload.class, ByteArrayBodyPartPayload.class, FileBodyPartPayload.class}, (BodyPartPayload)bodyPartPayload2, (int)n)) {
                case 0: {
                    MultipartPayload multi = (MultipartPayload)bodyPartPayload2;
                    MultipartEncoder.collectParts(parts, multi);
                    break;
                }
                case 1: {
                    ByteArrayBodyPartPayload b = (ByteArrayBodyPartPayload)bodyPartPayload2;
                    parts.add(BodySupplier.ofBytes(b.getPayload(), b.getOff(), b.getLen()));
                    break;
                }
                case 2: {
                    FileBodyPartPayload f = (FileBodyPartPayload)bodyPartPayload2;
                    parts.add(f.getPayload());
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Unsupported body part type: " + String.valueOf(bodyPart.getClass()));
                }
            }
        }
    }
}

