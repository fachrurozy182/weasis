/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.JsonNode
 *  com.github.scribejava.core.extractors.OAuth2AccessTokenJsonExtractor
 */
package org.weasis.core.api.net.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.scribejava.core.extractors.OAuth2AccessTokenJsonExtractor;
import org.weasis.core.api.net.auth.OpenIdOAuth2AccessToken;

public class OpenIdJsonTokenExtractor
extends OAuth2AccessTokenJsonExtractor {
    private static final String ID_TOKEN_FIELD = "id_token";

    private OpenIdJsonTokenExtractor() {
    }

    public static OpenIdJsonTokenExtractor instance() {
        return InstanceHolder.INSTANCE;
    }

    protected OpenIdOAuth2AccessToken createToken(String accessToken, String tokenType, Integer expiresIn, String refreshToken, String scope, JsonNode response, String rawResponse) {
        JsonNode idToken = response.get(ID_TOKEN_FIELD);
        return new OpenIdOAuth2AccessToken(accessToken, tokenType, expiresIn, refreshToken, scope, idToken != null ? idToken.asText() : null, rawResponse);
    }

    private static final class InstanceHolder {
        private static final OpenIdJsonTokenExtractor INSTANCE = new OpenIdJsonTokenExtractor();

        private InstanceHolder() {
        }
    }
}

