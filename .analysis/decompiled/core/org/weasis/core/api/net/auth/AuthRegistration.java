/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.api.net.auth;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.weasis.core.util.StringUtil;

public record AuthRegistration(String clientId, String clientSecret, String scope, String audience, String user, String grantType) {
    public static final String CODE = "code";
    public static final String CLIENT_CREDENTIALS = "client_credentials";

    public AuthRegistration {
        clientId = AuthRegistration.normalizeString(clientId);
        clientSecret = AuthRegistration.normalizeString(clientSecret);
        scope = AuthRegistration.normalizeScope(scope);
        audience = AuthRegistration.normalizeString(audience);
        user = AuthRegistration.normalizeString(user);
        grantType = StringUtil.hasText((CharSequence)grantType) ? grantType.strip() : CODE;
    }

    public static AuthRegistration empty() {
        return new AuthRegistration(null, null, null, null, null, CODE);
    }

    public static AuthRegistration of(String clientId, String clientSecret, String scope, String audience) {
        return new AuthRegistration(clientId, clientSecret, scope, audience, null, CODE);
    }

    public static AuthRegistration of(String clientId, String clientSecret, String scope, String audience, String grantType) {
        return new AuthRegistration(clientId, clientSecret, scope, audience, null, grantType);
    }

    public String getAuthorizationGrantType() {
        return this.grantType;
    }

    public boolean isClientCredentialsGrant() {
        return CLIENT_CREDENTIALS.equals(this.grantType);
    }

    public boolean hasClientId() {
        return StringUtil.hasText((CharSequence)this.clientId);
    }

    public boolean hasClientSecret() {
        return StringUtil.hasText((CharSequence)this.clientSecret);
    }

    public boolean isComplete() {
        return this.hasClientId() && this.hasClientSecret();
    }

    private static String normalizeString(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.strip();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String normalizeScope(String value) {
        if (value == null) {
            return null;
        }
        String joined = Arrays.stream(value.split("[,;\\s]+")).filter(s -> !s.isEmpty()).collect(Collectors.joining(" "));
        return joined.isEmpty() ? null : joined;
    }
}

