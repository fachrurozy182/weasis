/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.scribejava.core.model.OAuth2AccessToken
 */
package org.weasis.core.api.net.auth;

import com.github.scribejava.core.model.OAuth2AccessToken;
import java.util.Objects;

public class OpenIdOAuth2AccessToken
extends OAuth2AccessToken {
    private final String openIdToken;

    public OpenIdOAuth2AccessToken(String accessToken, String openIdToken, String rawResponse) {
        this(accessToken, null, null, null, null, openIdToken, rawResponse);
    }

    public OpenIdOAuth2AccessToken(String accessToken, String tokenType, Integer expiresIn, String refreshToken, String scope, String openIdToken, String rawResponse) {
        super(accessToken, tokenType, expiresIn, refreshToken, scope, rawResponse);
        this.openIdToken = openIdToken;
    }

    public String getOpenIdToken() {
        return this.openIdToken;
    }

    public int hashCode() {
        return Objects.hash(super.hashCode(), this.openIdToken);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof OpenIdOAuth2AccessToken)) return false;
        OpenIdOAuth2AccessToken other = (OpenIdOAuth2AccessToken)((Object)obj);
        if (!super.equals(obj)) return false;
        if (!Objects.equals(this.openIdToken, other.openIdToken)) return false;
        return true;
    }
}

