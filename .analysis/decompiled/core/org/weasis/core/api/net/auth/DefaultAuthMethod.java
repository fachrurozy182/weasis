/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.scribejava.core.model.OAuth2AccessToken
 *  com.github.scribejava.core.oauth.OAuth20Service
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.api.net.auth;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.net.auth.AcceptCallbackHandler;
import org.weasis.core.api.net.auth.AcceptCompletionHandler;
import org.weasis.core.api.net.auth.AsyncCallbackServerHandler;
import org.weasis.core.api.net.auth.AuthMethod;
import org.weasis.core.api.net.auth.AuthProvider;
import org.weasis.core.api.net.auth.AuthRegistration;
import org.weasis.core.api.net.auth.OAuth2ServiceFactory;
import org.weasis.core.util.StringUtil;

public class DefaultAuthMethod
implements AuthMethod {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAuthMethod.class);
    private static final Duration POLLING_TIMEOUT = Duration.ofSeconds(60L);
    private static final Duration TOKEN_RETRIEVAL_TIMEOUT = Duration.ofSeconds(15L);
    private static final Duration POLLING_INTERVAL = Duration.ofSeconds(1L);
    private static final String PARAM_ACCESS_TYPE = "access_type";
    private static final String PARAM_PROMPT = "prompt";
    private static final String PARAM_AUDIENCE = "audience";
    private static final String VALUE_OFFLINE = "offline";
    private static final String VALUE_CONSENT = "consent";
    private final String uid;
    private final AuthProvider authProvider;
    private final AuthRegistration authRegistration;
    private volatile OAuth2AccessToken token;
    private volatile String code;
    private boolean local;

    public DefaultAuthMethod(String uid, AuthProvider authProvider, AuthRegistration authRegistration) {
        this.uid = StringUtil.hasText((CharSequence)uid) ? uid : UUID.randomUUID().toString();
        this.authProvider = Objects.requireNonNull(authProvider);
        this.authRegistration = Objects.requireNonNull(authRegistration);
    }

    @Override
    public String getName() {
        return this.authProvider.name();
    }

    public String toString() {
        return this.authProvider.name();
    }

    @Override
    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String getUid() {
        return this.uid;
    }

    @Override
    public boolean isLocal() {
        return this.local;
    }

    @Override
    public void setLocal(boolean local) {
        this.local = local;
    }

    @Override
    public AuthProvider getAuthProvider() {
        return this.authProvider;
    }

    @Override
    public AuthRegistration getAuthRegistration() {
        return this.authRegistration;
    }

    @Override
    public void resetToken() {
        this.token = null;
    }

    @Override
    public OAuth2AccessToken getToken() {
        if (this.token != null) {
            return this.token;
        }
        if (this.authRegistration.isClientCredentialsGrant()) {
            this.requestClientCredentialsToken();
            return this.token;
        }
        if (StringUtil.hasText((CharSequence)this.code)) {
            this.refreshExistingToken();
        }
        if (this.token == null) {
            this.authenticate();
        }
        return this.token;
    }

    private void requestClientCredentialsToken() {
        OAuth20Service service = OAuth2ServiceFactory.getService(this);
        if (service == null) {
            return;
        }
        try {
            this.token = service.getAccessTokenClientCredentialsGrant();
        }
        catch (InterruptedException e) {
            this.token = null;
            Thread.currentThread().interrupt();
            LOGGER.error("Client-credentials token request interrupted", (Throwable)e);
        }
        catch (Exception e) {
            this.token = null;
            LOGGER.error("Client-credentials token request failed", (Throwable)e);
        }
    }

    private void refreshExistingToken() {
        if (!StringUtil.hasText((CharSequence)this.code)) {
            return;
        }
        OAuth20Service service = OAuth2ServiceFactory.getService(this);
        if (service == null) {
            return;
        }
        try {
            this.token = service.refreshAccessToken(this.code);
        }
        catch (InterruptedException e) {
            this.token = null;
            Thread.currentThread().interrupt();
            LOGGER.error("Cannot refresh existing token", (Throwable)e);
        }
        catch (Exception e) {
            this.token = null;
            LOGGER.error("Cannot refresh existing token", (Throwable)e);
        }
    }

    private void authenticate() {
        AsyncCallbackServerHandler authServer = null;
        try {
            authServer = this.createCallbackHandler();
            authServer.start();
            this.token = this.performOAuthFlow(authServer);
            if (this.token == null) {
                throw new IllegalArgumentException("Authentication failed - unable to obtain access token");
            }
        }
        catch (IOException e) {
            LOGGER.error("Authentication server error", (Throwable)e);
        }
        finally {
            DefaultAuthMethod.shutdownServer(authServer);
        }
    }

    private AsyncCallbackServerHandler createCallbackHandler() throws IOException {
        OAuth20Service service = OAuth2ServiceFactory.getService(this);
        if (service == null) {
            throw new IOException("Cannot build OAuth service for " + this.getName());
        }
        int port = Integer.parseInt(service.getCallback().substring("http://127.0.0.1:".length()));
        return new AsyncCallbackServerHandler(port, new AcceptCompletionHandler(service));
    }

    private OAuth2AccessToken performOAuthFlow(AsyncCallbackServerHandler server) {
        OAuth20Service service = server.getResponseHandler().service();
        try {
            this.openAuthorizationUrl(service);
            return DefaultAuthMethod.waitForAuthorizationCode(server).map(authCode -> this.exchangeAndRefresh(service, (String)authCode)).orElse(null);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Authentication flow interrupted", (Throwable)e);
            return null;
        }
        catch (Exception e) {
            LOGGER.error("OAuth flow failed", (Throwable)e);
            return null;
        }
    }

    private OAuth2AccessToken exchangeAndRefresh(OAuth20Service service, String authCode) {
        try {
            OAuth2AccessToken accessToken = DefaultAuthMethod.retrieveAccessToken(service, authCode);
            if (accessToken == null) {
                LOGGER.warn("Token exchange returned no access token for {}", (Object)this.getName());
                return null;
            }
            String refreshToken = accessToken.getRefreshToken();
            String string = this.code = StringUtil.hasText((CharSequence)refreshToken) ? refreshToken : null;
            if (this.code == null) {
                LOGGER.info("{} did not return a refresh_token; re-authentication will be required when the access token expires", (Object)this.getName());
            }
            return accessToken;
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Token exchange interrupted", (Throwable)e);
            return null;
        }
        catch (Exception e) {
            LOGGER.error("Token exchange failed", (Throwable)e);
            return null;
        }
    }

    private void openAuthorizationUrl(OAuth20Service service) throws IOException {
        String authorizationUrl = service.createAuthorizationUrlBuilder().additionalParams(this.createAuthParams()).build();
        GuiUtils.openInDefaultBrowser(null, URI.create(authorizationUrl).toURL());
    }

    private Map<String, String> createAuthParams() {
        String audience = this.authRegistration.audience();
        return StringUtil.hasText((CharSequence)audience) ? Map.of(PARAM_ACCESS_TYPE, VALUE_OFFLINE, PARAM_PROMPT, VALUE_CONSENT, PARAM_AUDIENCE, audience) : Map.of(PARAM_ACCESS_TYPE, VALUE_OFFLINE, PARAM_PROMPT, VALUE_CONSENT);
    }

    private static Optional<String> waitForAuthorizationCode(AsyncCallbackServerHandler server) throws InterruptedException {
        long deadline = System.nanoTime() + POLLING_TIMEOUT.toNanos();
        AcceptCallbackHandler handler = server.getResponseHandler();
        while (System.nanoTime() < deadline) {
            Optional<String> authCode = handler.code();
            if (authCode.isPresent()) {
                return authCode;
            }
            Thread.sleep(POLLING_INTERVAL.toMillis());
        }
        LOGGER.warn("Timeout waiting for authorization code after {} seconds", (Object)POLLING_TIMEOUT.toSeconds());
        return Optional.empty();
    }

    private static OAuth2AccessToken retrieveAccessToken(OAuth20Service service, String authCode) throws Exception {
        return (OAuth2AccessToken)service.getAccessTokenAsync(authCode).get(TOKEN_RETRIEVAL_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
    }

    private static void shutdownServer(AsyncCallbackServerHandler server) {
        if (server != null) {
            try {
                server.close();
            }
            catch (Exception e) {
                LOGGER.debug("Error shutting down auth server", (Throwable)e);
            }
        }
    }
}

