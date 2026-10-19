/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.annotations.Generated
 */
package org.weasis.dicom.web;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.Optional;
import org.weasis.core.util.annotations.Generated;

@Generated
public final class HttpException
extends IOException {
    private static final long serialVersionUID = 1L;
    private final int statusCode;
    private final String responseBody;
    private final HttpErrorCategory category;

    public HttpException(String message, int statusCode, Throwable cause) {
        this(message, statusCode, null, cause);
    }

    public HttpException(String message, int statusCode, String responseBody, Throwable cause) {
        super(HttpException.createDetailedMessage(message, statusCode, responseBody), cause);
        this.statusCode = HttpException.validateStatusCode(statusCode);
        this.responseBody = responseBody;
        this.category = HttpErrorCategory.fromStatusCode(statusCode);
    }

    public static HttpException fromResponse(HttpResponse<String> response) {
        return HttpException.fromResponse("HTTP request failed", response);
    }

    public static HttpException fromResponse(String message, HttpResponse<String> response) {
        Objects.requireNonNull(response, "HTTP response cannot be null");
        return new HttpException(Objects.requireNonNull(message, "Message cannot be null"), response.statusCode(), response.body(), null);
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public Optional<String> getResponseBody() {
        return Optional.ofNullable(this.responseBody);
    }

    public HttpErrorCategory getCategory() {
        return this.category;
    }

    public boolean isClientError() {
        return this.category == HttpErrorCategory.CLIENT_ERROR;
    }

    public boolean isServerError() {
        return this.category == HttpErrorCategory.SERVER_ERROR;
    }

    public boolean isRecoverable() {
        return this.isServerError() || this.statusCode == 408 || this.statusCode == 429;
    }

    public String getStatusText() {
        return HttpStatusText.getText(this.statusCode);
    }

    private static String createDetailedMessage(String message, int statusCode, String responseBody) {
        StringBuilder sb = new StringBuilder(message);
        sb.append(" (HTTP ").append(statusCode);
        String statusText = HttpStatusText.getText(statusCode);
        if (!statusText.isEmpty()) {
            sb.append(" ").append(statusText);
        }
        sb.append(")");
        if (responseBody != null && !responseBody.trim().isEmpty()) {
            String truncatedBody = responseBody.length() > 200 ? responseBody.substring(0, 197) + "..." : responseBody;
            sb.append(": ").append(truncatedBody);
        }
        return sb.toString();
    }

    private static int validateStatusCode(int statusCode) {
        if (statusCode < 100 || statusCode > 599) {
            throw new IllegalArgumentException("Invalid HTTP status code: " + statusCode);
        }
        return statusCode;
    }

    public static enum HttpErrorCategory {
        INFORMATIONAL,
        SUCCESS,
        REDIRECTION,
        CLIENT_ERROR,
        SERVER_ERROR,
        UNKNOWN;


        static HttpErrorCategory fromStatusCode(int statusCode) {
            return switch (statusCode / 100) {
                case 1 -> INFORMATIONAL;
                case 2 -> SUCCESS;
                case 3 -> REDIRECTION;
                case 4 -> CLIENT_ERROR;
                case 5 -> SERVER_ERROR;
                default -> UNKNOWN;
            };
        }
    }

    private static final class HttpStatusText {
        static String getText(int statusCode) {
            return switch (statusCode) {
                case 400 -> "Bad Request";
                case 401 -> "Unauthorized";
                case 403 -> "Forbidden";
                case 404 -> "Not Found";
                case 405 -> "Method Not Allowed";
                case 408 -> "Request Timeout";
                case 409 -> "Conflict";
                case 410 -> "Gone";
                case 413 -> "Payload Too Large";
                case 415 -> "Unsupported Media Type";
                case 422 -> "Unprocessable Entity";
                case 429 -> "Too Many Requests";
                case 500 -> "Internal Server Error";
                case 501 -> "Not Implemented";
                case 502 -> "Bad Gateway";
                case 503 -> "Service Unavailable";
                case 504 -> "Gateway Timeout";
                case 505 -> "HTTP Version Not Supported";
                case 200 -> "OK";
                case 201 -> "Created";
                case 202 -> "Accepted";
                case 204 -> "No Content";
                case 301 -> "Moved Permanently";
                case 302 -> "Found";
                case 304 -> "Not Modified";
                case 307 -> "Temporary Redirect";
                case 308 -> "Permanent Redirect";
                default -> "";
            };
        }

        private HttpStatusText() {
        }
    }
}

