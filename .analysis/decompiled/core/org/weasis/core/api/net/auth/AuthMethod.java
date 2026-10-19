/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.scribejava.core.model.OAuth2AccessToken
 */
package org.weasis.core.api.net.auth;

import com.github.scribejava.core.model.OAuth2AccessToken;
import org.weasis.core.api.net.auth.AuthProvider;
import org.weasis.core.api.net.auth.AuthRegistration;

public interface AuthMethod {
    public String getCode();

    public String getUid();

    public void resetToken();

    public OAuth2AccessToken getToken();

    public AuthRegistration getAuthRegistration();

    public boolean isLocal();

    public void setLocal(boolean var1);

    public AuthProvider getAuthProvider();

    default public String getName() {
        return this.getAuthProvider().name();
    }

    default public boolean hasValidToken() {
        OAuth2AccessToken token = this.getToken();
        return token != null && !token.getAccessToken().isEmpty();
    }
}

