/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.launcher;

import com.formdev.flatlaf.util.SystemInfo;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.launcher.FileUtil;
import org.weasis.launcher.Utils;

public class RemotePrefService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemotePrefService.class);
    public static final String P_REMOTE_PREF_USER = "user";
    public static final String P_REMOTE_PREF_PROFILE = "profile";
    private static final String TEXT_X_JAVA_PROP = "text/x-java-properties";
    private final String remotePrefURL;
    private final String user;
    private final String profile;
    private final boolean localSessionUser;
    private final boolean storeLocalSession;

    public RemotePrefService(String url, Map<String, String> serverProp, String user, String profile) {
        this.remotePrefURL = Objects.requireNonNull(url);
        this.user = RemotePrefService.getUserName(Objects.requireNonNull(user));
        this.localSessionUser = Utils.emptyToFalse(serverProp.get("weasis.pref.local.session"));
        this.storeLocalSession = Utils.emptyToFalse(serverProp.get("weasis.pref.store.local.session"));
        this.profile = Objects.requireNonNull(profile);
    }

    private static String getUserName(String user) {
        return SystemInfo.isWindows ? user.trim().toUpperCase() : user.trim();
    }

    public final String getUser() {
        return this.user;
    }

    public String getProfile() {
        return this.profile;
    }

    public boolean isLocalSessionUser() {
        return this.localSessionUser;
    }

    private String getEncodedValue(String val) {
        return URLEncoder.encode(val, StandardCharsets.UTF_8);
    }

    private String getRemoteLauncherUrl() {
        return String.format("%s?%s=%s&%s=%s", this.remotePrefURL, P_REMOTE_PREF_USER, this.getEncodedValue(this.user), P_REMOTE_PREF_PROFILE, this.getEncodedValue(this.profile));
    }

    public Properties readLauncherPref(Properties props) throws IOException {
        Properties p;
        Properties properties = p = props == null ? new Properties() : props;
        if (!this.localSessionUser || this.storeLocalSession) {
            this.readRemoteProperties(p);
        }
        return p;
    }

    private void readRemoteProperties(Properties props) throws IOException {
        HttpURLConnection httpURLConnection;
        String remoteURL = this.getRemoteLauncherUrl();
        URLConnection prefSv = FileUtil.getAdaptedConnection(new URL(remoteURL), false);
        prefSv.setRequestProperty("Accept", TEXT_X_JAVA_PROP);
        prefSv.setConnectTimeout(7000);
        prefSv.setReadTimeout(10000);
        if (prefSv instanceof HttpURLConnection && (httpURLConnection = (HttpURLConnection)prefSv).getResponseCode() == 200) {
            try (InputStream is = httpURLConnection.getInputStream();){
                props.load(is);
            }
            catch (Exception e) {
                LOGGER.error("Loading {}", (Object)remoteURL, (Object)e);
            }
        }
    }
}

