/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.scribejava.core.builder.ServiceBuilder
 *  com.github.scribejava.core.builder.ServiceBuilderOAuth20
 *  com.github.scribejava.core.builder.api.DefaultApi20
 *  com.github.scribejava.core.extractors.OAuth2AccessTokenJsonExtractor
 *  com.github.scribejava.core.extractors.TokenExtractor
 *  com.github.scribejava.core.httpclient.HttpClient
 *  com.github.scribejava.core.model.OAuth2AccessToken
 *  com.github.scribejava.core.oauth.OAuth20Service
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.api.net.auth;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.builder.ServiceBuilderOAuth20;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.extractors.OAuth2AccessTokenJsonExtractor;
import com.github.scribejava.core.extractors.TokenExtractor;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.net.SocketUtil;
import org.weasis.core.api.net.auth.AuthMethod;
import org.weasis.core.api.net.auth.AuthProvider;
import org.weasis.core.api.net.auth.AuthRegistration;
import org.weasis.core.api.net.auth.DefaultAuthMethod;
import org.weasis.core.api.net.auth.JavaNetHttpClient;
import org.weasis.core.api.net.auth.OpenIdJsonTokenExtractor;
import org.weasis.core.util.StringUtil;

public final class OAuth2ServiceFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2ServiceFactory.class);
    public static final String CALLBACK_URL = "http://127.0.0.1:";
    public static final String NO_AUTH_ID = "5aa85854-8f1f-11eb-b339-d3daace59a05";
    public static final DefaultAuthMethod NO_AUTH = OAuth2ServiceFactory.createNoAuthMethod();
    public static final DefaultAuthMethod GOOGLE_AUTH_TEMPLATE = OAuth2ServiceFactory.createGoogleAuthTemplate();
    public static final DefaultAuthMethod KEYCLOAK_TEMPLATE = OAuth2ServiceFactory.createKeycloakTemplate();
    private static final String PORT_PREF_KEY = "weasis.auth.back.port";
    private static final Map<String, OAuth20Service> services = new ConcurrentHashMap<String, OAuth20Service>();

    private OAuth2ServiceFactory() {
    }

    public static OAuth20Service getService(AuthMethod authMethod) {
        int port = GuiUtils.getUICore().getSystemPreferences().getIntProperty(PORT_PREF_KEY, 0);
        return OAuth2ServiceFactory.getService(authMethod, port);
    }

    public static OAuth20Service getService(AuthMethod authMethod, int port) {
        String serviceKey = authMethod.getUid() + ":" + port;
        return services.computeIfAbsent(serviceKey, uid -> OAuth2ServiceFactory.createOAuth20Service(authMethod, port));
    }

    public static void invalidateService(String uid) {
        if (StringUtil.hasText((CharSequence)uid)) {
            services.keySet().removeIf(key -> key.startsWith(uid + ":"));
        }
    }

    public static AuthProvider buildKeycloakProvider(String name, String baseUrl, String realm) {
        Object normalizedUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        String realmUrl = (String)normalizedUrl + "realms/" + realm.trim();
        return new AuthProvider(name, realmUrl + "/protocol/openid-connect/auth", realmUrl + "/protocol/openid-connect/token", realmUrl + "/protocol/openid-connect/revoke", true);
    }

    private static DefaultAuthMethod createNoAuthMethod() {
        return new DefaultAuthMethod(NO_AUTH_ID, new AuthProvider(Messages.getString("no.authentication"), null, null, null, false), AuthRegistration.empty()){

            @Override
            public OAuth2AccessToken getToken() {
                return null;
            }
        };
    }

    private static DefaultAuthMethod createGoogleAuthTemplate() {
        AuthProvider provider = new AuthProvider("Google Cloud Healthcare", "https://accounts.google.com/o/oauth2/v2/auth", "https://oauth2.googleapis.com/token", "https://oauth2.googleapis.com/revoke", true);
        return new DefaultAuthMethod("2c5dc28c-8fa0-11eb-9321-7fffcd64cef1", provider, AuthRegistration.of(null, null, "https://www.googleapis.com/auth/cloud-healthcare https://www.googleapis.com/auth/cloudplatformprojects.readonly", null));
    }

    private static DefaultAuthMethod createKeycloakTemplate() {
        return new DefaultAuthMethod("68c845fc-93c5-11eb-b2f8-0f5db063091d", OAuth2ServiceFactory.buildKeycloakProvider("Default Keycloak 18+", "http://localhost:8080/", "master"), AuthRegistration.of(null, null, "openid", null));
    }

    private static OAuth20Service createOAuth20Service(AuthMethod authMethod, int port) {
        AuthRegistration registration = authMethod.getAuthRegistration();
        AuthProvider provider = authMethod.getAuthProvider();
        if (registration == null || provider == null) {
            LOGGER.debug("Cannot build OAuth service for '{}': missing registration/provider", (Object)authMethod);
            return null;
        }
        ServiceBuilderOAuth20 builder = new ServiceBuilder(registration.clientId()).apiSecret(registration.clientSecret()).httpClient((HttpClient)new JavaNetHttpClient()).defaultScope(registration.scope()).userAgent(System.getProperty("http.agent"));
        if (!registration.isClientCredentialsGrant()) {
            int actualPort = port <= 0 ? SocketUtil.findAvailablePort() : port;
            builder.callback(CALLBACK_URL + actualPort).responseType(registration.getAuthorizationGrantType());
        }
        return builder.build((DefaultApi20)new OAuth2Api(provider));
    }

    static final class OAuth2Api
    extends DefaultApi20 {
        private final AuthProvider provider;

        OAuth2Api(AuthProvider provider) {
            this.provider = provider;
        }

        public String getAccessTokenEndpoint() {
            return this.provider.tokenUri();
        }

        public String getAuthorizationBaseUrl() {
            return this.provider.authorizationUri();
        }

        public String getRevokeTokenEndpoint() {
            return this.provider.revokeTokenUri();
        }

        public TokenExtractor<OAuth2AccessToken> getAccessTokenExtractor() {
            return this.provider.openId() ? OpenIdJsonTokenExtractor.instance() : OAuth2AccessTokenJsonExtractor.instance();
        }
    }
}

