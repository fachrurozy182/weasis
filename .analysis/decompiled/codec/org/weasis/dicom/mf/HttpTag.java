/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.mf;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public final class HttpTag {
    private static final Pattern VALID_HEADER_NAME = Pattern.compile("^[a-zA-Z0-9!#$&'*+\\-.^_`|~]+$");
    private static final int MAX_HEADER_LENGTH = 8192;
    private final String key;
    private final String value;

    public HttpTag(String key, String value) {
        this.key = this.validateKey(Objects.requireNonNull(key, "HTTP header key cannot be null"));
        this.value = this.validateValue(Objects.requireNonNull(value, "HTTP header value cannot be null"));
    }

    public static HttpTag of(String key, String value) {
        return new HttpTag(key, value);
    }

    public static HttpTag authorization(String token) {
        return new HttpTag("Authorization", "Bearer " + Objects.requireNonNull(token, "Token cannot be null"));
    }

    public static HttpTag contentType(String contentType) {
        return new HttpTag("Content-Type", Objects.requireNonNull(contentType, "Content type cannot be null"));
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    public boolean isAuthorizationHeader() {
        return "Authorization".equalsIgnoreCase(this.key);
    }

    public boolean isSensitive() {
        String lowerKey = this.key.toLowerCase(Locale.ROOT);
        return lowerKey.contains("auth") || lowerKey.contains("token") || lowerKey.contains("password") || lowerKey.contains("secret") || lowerKey.contains("key");
    }

    public String toHeaderString() {
        return this.key + ": " + this.value;
    }

    public String toString() {
        return this.isSensitive() ? String.format("HttpTag{key='%s', value='[REDACTED]'}", this.key) : String.format("HttpTag{key='%s', value='%s'}", this.key, this.value);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HttpTag)) {
            return false;
        }
        HttpTag that = (HttpTag)obj;
        return Objects.equals(this.key, that.key) && Objects.equals(this.value, that.value);
    }

    public int hashCode() {
        return Objects.hash(this.key, this.value);
    }

    private String validateKey(String key) {
        if (key.isBlank()) {
            throw new IllegalArgumentException("HTTP header key cannot be blank");
        }
        if (!VALID_HEADER_NAME.matcher(key).matches()) {
            throw new IllegalArgumentException("HTTP header key contains invalid characters: " + key);
        }
        return key.trim();
    }

    private String validateValue(String value) {
        if (value.length() > 8192) {
            throw new IllegalArgumentException("HTTP header value exceeds maximum length: " + value.length());
        }
        String trimmed = value.trim();
        if (trimmed.chars().anyMatch(c -> c < 32 && c != 9)) {
            throw new IllegalArgumentException("HTTP header value contains invalid control characters");
        }
        return trimmed;
    }
}

