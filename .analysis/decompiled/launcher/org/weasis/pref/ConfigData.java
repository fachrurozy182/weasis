/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.osgi.framework.Version
 */
package org.weasis.pref;

import com.formdev.flatlaf.util.SystemInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.launcher.FileUtil;
import org.weasis.launcher.Utils;
import org.weasis.launcher.WeasisLauncher;
import org.weasis.pref.AppPreferences;
import org.weasis.pref.Preference;

public class ConfigData {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigData.class);
    public static final String CONFIG_DIRECTORY = "conf";
    public static final String APP_PROPERTY_FILE = "weasis.properties";
    public static final String P_WEASIS_VERSION = "weasis.version";
    public static final String P_WEASIS_PROFILE = "weasis.profile";
    public static final String P_WEASIS_NAME = "weasis.name";
    public static final String P_WEASIS_PATH = "weasis.path";
    public static final String P_WEASIS_RES_DATE = "weasis.resources.date";
    public static final String P_WEASIS_CODEBASE_LOCAL = "weasis.codebase.local";
    public static final String P_WEASIS_SOURCE_ID = "weasis.source.id";
    public static final String P_WEASIS_CODEBASE_URL = "weasis.codebase.url";
    public static final String P_WEASIS_CONFIG_HASH = "weasis.config.hash";
    public static final String P_WEASIS_PREFS_URL = "weasis.pref.url";
    public static final String P_WEASIS_CONFIG_URL = "weasis.config.url";
    public static final String P_WEASIS_USER = "weasis.user";
    public static final String P_WEASIS_SHOW_DISCLAIMER = "weasis.show.disclaimer";
    public static final String P_WEASIS_ACCEPT_DISCLAIMER = "weasis.accept.disclaimer";
    public static final String P_WEASIS_SHOW_RELEASE = "weasis.show.release";
    public static final String P_WEASIS_VERSION_RELEASE = "weasis.version.release";
    public static final String P_WEASIS_I18N = "weasis.i18n";
    public static final String P_OS_NAME = "os.name";
    public static final String P_WEASIS_LOOK = "weasis.theme";
    public static final String P_GOSH_ARGS = "gosh.args";
    public static final String P_WEASIS_CLEAN_CACHE = "weasis.clean.cache";
    public static final String P_HTTP_AUTHORIZATION = "http.authorization";
    public static final String P_NATIVE_LIB_SPEC = "native.library.spec";
    public static final String P_WEASIS_MIN_NATIVE_VERSION = "weasis.min.native.version";
    public static final String P_WEASIS_RESOURCES_URL = "weasis.resources.url";
    public static final String F_RESOURCES = "resources";
    private static final Set<String> PROTECTED_PROPERTIES = Set.of("weasis.source.id", "weasis.path", "weasis.codebase.local", "weasis.config.hash");
    private static final List<String> PROTECTED_PREFIXES = List.of("java.", "javax.", "jdk.", "sun.", "os.", "user.");
    private static final Set<String> FOLDER_NAME_PROPERTIES = Set.of("weasis.profile", "weasis.user");
    private static final Pattern PATH_CHARS = Pattern.compile("[\\\\/:*?\"<>|\\p{Cntrl}]");
    public static final String CONFIG_PROPERTIES_PROP = "felix.config.properties";
    public static final String CONFIG_PROPERTIES_FILE_VALUE = "base.json";
    public static final String EXTENDED_PROPERTIES_PROP = "felix.extended.config.properties";
    public static final String PARAM_CONFIG_URL = "wcfg";
    public static final String PARAM_ARGUMENT = "arg";
    public static final String PARAM_PROPERTY = "pro";
    public static final String PARAM_CODEBASE = "cdb";
    public static final String PARAM_AUTHORIZATION = "auth";
    private final List<String> arguments = new ArrayList<String>();
    private final Properties properties = new Properties();
    private final StringBuilder configOutput = new StringBuilder();
    private final Map<String, String> felixProps = new HashMap<String, String>();
    private final AppPreferences preferences = new AppPreferences();

    public ConfigData(String[] args) {
        this.init(args);
    }

    public void init(String[] args) {
        this.clear();
        LOGGER.info("Starting Weasis...");
        LOGGER.info("Initialization of the launch configuration...");
        if (args != null) {
            for (int i = 0; i < args.length; ++i) {
                LOGGER.info("Main arg {} = {}", (Object)i, (Object)args[i]);
            }
            int index = Utils.getWeasisProtocolIndex(args);
            if (index < 0) {
                this.splitArgToCmd(args);
            } else {
                this.extractArgFromUri(args[index]);
                if (args.length > 1) {
                    ArrayList<String> otherArgs = new ArrayList<String>(args.length - 1);
                    for (int i = 0; i < args.length; ++i) {
                        if (i == index) continue;
                        otherArgs.add(args[i]);
                    }
                    this.splitArgToCmd(otherArgs.toArray(new String[0]));
                }
            }
        }
        System.setProperty("http.agent", this.getHttpAgent("", ""));
        this.applyJavaProperties();
        this.applyConfigFromArguments();
        this.loadConfigProperties();
        this.initWeasisProperties(this.preferences);
    }

    private String getHttpAgent(String revision, String profile) {
        return "Weasis/" + System.getProperty("jpackage.app-version", "") + " (" + System.getProperty(P_OS_NAME) + "; " + System.getProperty("os.version") + "; " + System.getProperty("os.arch") + "; pf:" + profile + "; rv:" + revision + ") Java/" + System.getProperty("java.version");
    }

    private void applyJavaProperties() {
        this.applyJavaProperty(CONFIG_PROPERTIES_PROP);
        this.applyJavaProperty(EXTENDED_PROPERTIES_PROP);
        for (String propertyName : System.getProperties().stringPropertyNames()) {
            if (!propertyName.startsWith("weasis.")) continue;
            this.applyJavaProperty(propertyName);
        }
    }

    private void applyJavaProperty(String key) {
        this.addProperty(key, System.getProperty(key));
    }

    public Map<String, String> getFelixProps() {
        return this.felixProps;
    }

    private void initWeasisProperties(AppPreferences preferences) {
        ConfigData.setOsgiNativeLibSpecification();
        String profile = preferences.getValue(P_WEASIS_PROFILE, "default");
        this.addProperty(P_WEASIS_PROFILE, profile);
        String name = preferences.getValue(P_WEASIS_NAME, "Weasis");
        this.addProperty(P_WEASIS_NAME, name);
        String version = preferences.getValue(P_WEASIS_VERSION, "0.0.0");
        this.addProperty(P_WEASIS_VERSION, version);
        String codebase = this.properties.getProperty(P_WEASIS_CODEBASE_URL);
        this.addProperty(P_WEASIS_SOURCE_ID, ConfigData.toHex((codebase + profile).hashCode()));
        String user = this.properties.getProperty(P_WEASIS_USER);
        if (!Utils.hasText(user)) {
            user = System.getProperty("user.name", "unknown");
            this.addProperty(P_WEASIS_USER, user);
            this.addProperty("weasis.pref.local.session", Boolean.TRUE.toString());
        }
        this.addProperty("http.agent", this.getHttpAgent(version, profile));
        String portable = this.properties.getProperty("weasis.portable.dir");
        if (portable != null) {
            LOGGER.info("Set default relative folders");
            String pkey = "weasis.portable.dicom.directory";
            this.addProperty(pkey, preferences.getValue(pkey, "dicom,DICOM,IMAGES,images"));
        }
        this.applyConfigToSystemProperties();
        this.filterConfigProperties(preferences);
        if (LOGGER.isTraceEnabled()) {
            this.felixProps.forEach((k, v) -> LOGGER.trace("Felix config: {} = {}", k, v));
        }
        File appFolder = new File(this.felixProps.get("org.osgi.framework.storage")).getParentFile();
        appFolder.mkdirs();
        this.addProperty(P_WEASIS_PATH, appFolder.getPath());
        System.setProperty(P_WEASIS_PATH, appFolder.getPath());
        LOGGER.info("Properties: {}", (Object)this.properties);
    }

    private void filterConfigProperties(AppPreferences preferences) {
        String mvnRepo = System.getProperty("maven.localRepository", preferences.getValue("maven.local.repo"));
        if (mvnRepo != null) {
            System.setProperty("maven.localRepository", Utils.adaptPathToUri(mvnRepo));
        }
        for (Preference p : preferences.values()) {
            String name = p.getCode();
            this.felixProps.put(name, preferences.substVars(p.getValue(), name, null));
        }
    }

    public void extractArgFromUri(String uri) {
        String url = URLDecoder.decode(uri, StandardCharsets.UTF_8);
        String[] cmds = url.split("\\$");
        boolean windows = System.getProperty(P_OS_NAME, "").toLowerCase().startsWith("win");
        if (cmds.length > 0) {
            for (int i = 1; i < cmds.length; ++i) {
                if (windows && i == cmds.length - 1 && cmds[i].endsWith("/")) {
                    cmds[i] = cmds[i].substring(0, cmds[i].length() - 1);
                }
                this.arguments.add(cmds[i]);
            }
        }
    }

    private void applyConfigFromArguments() {
        List<String> configArgs = this.extractWeasisConfigArguments();
        this.applyConfigParams(this.getConfigParamsFromArgs(configArgs));
        this.applyConfigParams(this.getConfigParamsFromServicePath());
        String codeBaseUrl = this.properties.getProperty(P_WEASIS_CODEBASE_URL, "");
        if (!Utils.hasText(codeBaseUrl)) {
            this.applyLocalCodebase();
        }
        if (!this.properties.containsKey(CONFIG_PROPERTIES_PROP) && Utils.hasText(codeBaseUrl)) {
            String configProp = String.format("%s/%s/%s", codeBaseUrl, CONFIG_DIRECTORY, CONFIG_PROPERTIES_FILE_VALUE);
            this.addProperty(CONFIG_PROPERTIES_PROP, configProp);
        }
        this.configOutput.append("\n  Application local codebase = ");
        this.configOutput.append(this.properties.getProperty(P_WEASIS_CODEBASE_LOCAL));
        this.configOutput.append("\n  Application codebase URL = ");
        this.configOutput.append(this.properties.getProperty(P_WEASIS_CODEBASE_URL));
    }

    private void applyLocalCodebase() {
        this.applyLocalCodebase(false);
    }

    private void applyLocalCodebase(boolean force) {
        File localCodebase = ConfigData.findLocalCodebase();
        Object baseURI = localCodebase.toURI().toString();
        if (((String)baseURI).endsWith("/")) {
            baseURI = ((String)baseURI).substring(0, ((String)baseURI).length() - 1);
        }
        try {
            this.addProperty(P_WEASIS_CODEBASE_LOCAL, localCodebase.getAbsolutePath(), force);
            this.addProperty(P_WEASIS_CODEBASE_URL, (String)baseURI, force);
            baseURI = (String)baseURI + "/conf/";
            this.addProperty(CONFIG_PROPERTIES_PROP, (String)baseURI + CONFIG_PROPERTIES_FILE_VALUE, force);
        }
        catch (Exception e) {
            LOGGER.error("Apply Codebase", e);
        }
    }

    private void applyConfigParams(Map<String, List<String>> configParams) {
        if (configParams == null) {
            return;
        }
        configParams.forEach((k, v) -> {
            switch (k) {
                case "wcfg": {
                    this.addAndReplaceProperty(P_WEASIS_CONFIG_URL, (String)v.getFirst());
                    break;
                }
                case "cdb": {
                    this.addProperty(P_WEASIS_CODEBASE_URL, (String)v.getFirst());
                    break;
                }
                case "auth": {
                    this.addProperty(P_HTTP_AUTHORIZATION, (String)v.getFirst());
                    break;
                }
                case "pro": {
                    this.addProperties((Collection<String>)v);
                    break;
                }
                case "arg": {
                    this.addArguments((Collection<String>)v);
                    break;
                }
                default: {
                    throw new IllegalStateException("Unexpected value: " + k);
                }
            }
        });
    }

    public AppPreferences getPreferences() {
        return this.preferences;
    }

    public List<String> getArguments() {
        return this.arguments;
    }

    private void addArgument(String arg) {
        if (arg != null) {
            this.arguments.add(arg);
        }
    }

    private void addArguments(Collection<String> args) {
        if (args != null) {
            this.arguments.addAll(args);
        }
    }

    public Properties getProperties() {
        return this.properties;
    }

    private void addProperty(String key, String val) {
        this.addProperty(key, val, false);
    }

    private void addAndReplaceProperty(String key, String val) {
        this.addProperty(key, val, true);
    }

    private void addProperty(String key, String val, boolean forceIfPresent) {
        if (Utils.hasText(key) && Utils.hasText(val)) {
            if (forceIfPresent) {
                this.properties.put(key, val);
            } else {
                this.properties.putIfAbsent(key, val);
            }
        }
    }

    private void addProperties(Collection<String> properties) {
        Pattern pattern = Pattern.compile("\\s+");
        properties.forEach(value -> {
            String[] result = pattern.split((CharSequence)value, 2);
            if (result.length != 2) {
                LOGGER.warn("Cannot parse property: {}", value);
            } else if (ConfigData.isProtected(result[0])) {
                LOGGER.warn("Reject the launch property {}: it cannot be set externally", (Object)result[0]);
            } else if (ConfigData.isInvalidFolderName(result[0], result[1])) {
                LOGGER.warn("Reject the launch property {}: '{}' is not a folder name", (Object)result[0], (Object)result[1]);
            } else {
                this.addProperty(result[0], result[1]);
            }
        });
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private static boolean isProtected(String key) {
        if (PROTECTED_PROPERTIES.contains(key)) return true;
        if (!PROTECTED_PREFIXES.stream().anyMatch(key::startsWith)) return false;
        return true;
    }

    private static boolean isInvalidFolderName(String key, String value) {
        if (!FOLDER_NAME_PROPERTIES.contains(key)) {
            return false;
        }
        String name = value.strip();
        return PATH_CHARS.matcher(name).find() || name.chars().allMatch(c -> c == 46);
    }

    public StringBuilder getConfigOutput() {
        return this.configOutput;
    }

    public boolean isPropertyValueSimilar(String prop, String value) {
        String p = this.properties.getProperty(Objects.requireNonNull(prop));
        return Objects.equals(p, value);
    }

    public String getSourceID() {
        return this.properties.getProperty(P_WEASIS_SOURCE_ID);
    }

    private void clear() {
        this.arguments.clear();
        this.properties.clear();
    }

    private void applyConfigToSystemProperties() {
        for (String key : this.properties.stringPropertyNames()) {
            System.setProperty(key, this.properties.getProperty(key));
        }
    }

    public void applyProxy(String dir) {
        File oldFile;
        File file = new File(dir, "persistence.properties");
        if (!(file.canRead() || (oldFile = new File(dir, "persitence.properties")).canRead() && oldFile.renameTo(file))) {
            return;
        }
        Properties p = new Properties();
        FileUtil.loadProperties(file.toPath(), p);
        boolean mproxy = Utils.emptyToFalse(p.getProperty("proxy.manual"));
        if (mproxy) {
            String exceptions = p.getProperty("proxy.exceptions");
            String val = p.getProperty("proxy.http.host");
            ConfigData.applyProxyProperty("http.proxyHost", val, mproxy);
            if (Utils.hasText(val)) {
                ConfigData.applyProxyProperty("http.proxyPort", p.getProperty("proxy.http.port"), mproxy);
                ConfigData.applyProxyProperty("http.nonProxyHosts", exceptions, mproxy);
            }
            val = p.getProperty("proxy.https.host");
            ConfigData.applyProxyProperty("https.proxyHost", val, mproxy);
            if (Utils.hasText(val)) {
                ConfigData.applyProxyProperty("https.proxyPort", p.getProperty("proxy.https.port"), mproxy);
                ConfigData.applyProxyProperty("http.nonProxyHosts", exceptions, mproxy);
            }
            val = p.getProperty("proxy.ftp.host");
            ConfigData.applyProxyProperty("ftp.proxyHost", val, mproxy);
            if (Utils.hasText(val)) {
                ConfigData.applyProxyProperty("ftp.proxyPort", p.getProperty("proxy.ftp.port"), mproxy);
                ConfigData.applyProxyProperty("ftp.nonProxyHosts", exceptions, mproxy);
            }
            val = p.getProperty("proxy.socks.host");
            ConfigData.applyProxyProperty("socksProxyHost", val, mproxy);
            if (Utils.hasText(val)) {
                ConfigData.applyProxyProperty("socksProxyPort", p.getProperty("proxy.socks.port"), mproxy);
            }
        }
    }

    private static void applyProxyProperty(String key, String value, boolean manual) {
        if (manual && Utils.hasText(value)) {
            System.setProperty(key, value);
        }
    }

    private void splitArgToCmd(String ... args) {
        boolean files = true;
        int length = args.length;
        for (int i = 0; i < length; ++i) {
            if (!args[i].startsWith("$") || args[i].length() <= 1) continue;
            files = false;
            StringBuilder command = new StringBuilder(args[i].substring(1));
            while (i + 1 < length && !args[i + 1].startsWith("$")) {
                command.append(' ');
                command.append(args[++i]);
            }
            this.arguments.add(command.toString());
        }
        if (files) {
            for (String arg : args) {
                Object val = arg;
                if (((String)val).startsWith("file:")) {
                    try {
                        URI u = new URI(arg);
                        val = u.getAuthority() != null && SystemInfo.isWindows ? "\\\\" + u.getAuthority() + u.getPath() : Paths.get(u).toFile().getPath();
                    }
                    catch (URISyntaxException e) {
                        LOGGER.error("Convert URI to file", e);
                    }
                }
                this.arguments.add("dicom:get -l \"" + (String)val + "\"");
            }
        }
    }

    private List<String> extractWeasisConfigArguments() {
        String configCmd = "weasis:config";
        for (String cmd : this.arguments) {
            if (!cmd.startsWith(configCmd) || cmd.length() <= configCmd.length() + 2) continue;
            this.arguments.remove(cmd);
            return Utils.splitSpaceExceptInQuotes(cmd.substring(configCmd.length() + 1));
        }
        return Collections.emptyList();
    }

    private Map<String, List<String>> getConfigParamsFromArgs(List<String> configArguments) {
        HashMap<String, List<String>> configParams = new HashMap<String, List<String>>();
        configArguments.forEach(a -> this.addConfigParam((Map<String, List<String>>)configParams, (String)a));
        return configParams;
    }

    private void addConfigParam(Map<String, List<String>> configParams, String argument) {
        if (argument == null) {
            return;
        }
        String[] vals = argument.split("=", 2);
        if (vals.length != 2) {
            return;
        }
        this.addConfigParam(configParams, vals[0], vals[1]);
    }

    private void addConfigParam(Map<String, List<String>> configParams, String name, String value) {
        if (!Utils.hasText(name) || value == null) {
            return;
        }
        List paramList = configParams.computeIfAbsent(name, p -> new LinkedList());
        paramList.add(Utils.removeEnglobingQuotes(value));
    }

    private URI adaptConfigServiceURI(URI configServiceUri) throws URISyntaxException {
        List<String> paramsToOverride = List.of("user", "host");
        String configServiceQueryString = configServiceUri.getQuery();
        Map<Object, Object> queryParamsMap = !Utils.hasText(configServiceQueryString) ? new LinkedHashMap<String, List<String>>() : Arrays.stream(configServiceQueryString.split("&")).filter(s -> s != null && !s.isEmpty()).map(s -> Arrays.copyOf(s.split("=", 2), 2)).collect(Collectors.groupingBy(s -> paramsToOverride.contains(s[0].toLowerCase()) ? s[0].toLowerCase() : s[0], Collectors.mapping(s -> s[1], Collectors.toList())));
        String user = this.properties.getProperty(P_WEASIS_USER);
        if (Utils.hasText(user)) {
            queryParamsMap.put("user", List.of(user));
        } else if (!queryParamsMap.containsKey("user")) {
            user = System.getProperty("user.name");
            if (user != null) {
                queryParamsMap.putIfAbsent("user", List.of(user));
            } else {
                LOGGER.error("Cannot get system user.name from Launcher");
            }
        }
        String hostFromLocal = null;
        List queryParamHosts = (List)queryParamsMap.get("host");
        String queryParamHost = queryParamHosts != null ? (String)queryParamHosts.getFirst() : null;
        try {
            hostFromLocal = ConfigData.getHostName();
            queryParamsMap.put("host", List.of(hostFromLocal));
        }
        catch (Exception e) {
            Object err = "Cannot get local hostname from Launcher";
            if (Utils.hasText(queryParamHost)) {
                err = (String)err + ", the query param 'host' given is used instead : " + queryParamHost;
            }
            LOGGER.error((String)err, e);
        }
        if (Utils.hasText(hostFromLocal) && Utils.hasText(queryParamHost) && !queryParamHost.equalsIgnoreCase(hostFromLocal)) {
            LOGGER.info("LocalHost name found '{}' differs from the one given in LaunchConfigService URL parameter '{}'", (Object)hostFromLocal, (Object)queryParamHost);
        }
        String queryParamString = queryParamsMap.entrySet().stream().flatMap(entry -> ((List)entry.getValue()).stream().map(val -> (String)entry.getKey() + "=" + val)).collect(Collectors.joining("&"));
        return new URI(configServiceUri.getScheme(), null, configServiceUri.getHost(), configServiceUri.getPort(), configServiceUri.getPath(), queryParamString, null);
    }

    private static String getHostName() throws UnknownHostException, SocketException {
        InetAddress localhost = InetAddress.getLocalHost();
        String hostName = localhost.getHostName();
        if (hostName.equals(localhost.getHostAddress())) {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (inetAddress.isLoopbackAddress() || !inetAddress.isSiteLocalAddress()) continue;
                    hostName = inetAddress.getCanonicalHostName();
                    break;
                }
                if (hostName.equals(localhost.getHostAddress())) continue;
                break;
            }
        }
        return hostName;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     */
    private Map<String, List<String>> getConfigParamsFromServicePath() {
        String configServicePath = this.properties.getProperty(P_WEASIS_CONFIG_URL);
        if (!Utils.hasText(configServicePath)) {
            return null;
        }
        InputStream stream = null;
        URI configServiceUri = new URI(configServicePath);
        if (configServiceUri.getScheme().startsWith("file")) {
            stream = new FileInputStream(new File(configServiceUri));
        } else {
            HttpURLConnection httpURLConnection;
            URI adaptedConfigServiceUri = this.adaptConfigServiceURI(configServiceUri);
            URLConnection urlConnection = FileUtil.getAdaptedConnection(adaptedConfigServiceUri.toURL(), false);
            urlConnection.setRequestProperty("Accept", "application/xml");
            urlConnection.setConnectTimeout(Integer.parseInt(System.getProperty("UrlConnectionTimeout", "1000")));
            urlConnection.setReadTimeout(Integer.parseInt(System.getProperty("UrlReadTimeout", "2000")));
            if (urlConnection instanceof HttpURLConnection && (httpURLConnection = (HttpURLConnection)urlConnection).getResponseCode() != 200) {
                throw new IOException(httpURLConnection.getResponseMessage());
            }
            stream = urlConnection.getInputStream();
        }
        XMLStreamReader xmler = null;
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty("javax.xml.stream.isSupportingExternalEntities", Boolean.FALSE);
        factory.setProperty("javax.xml.stream.supportDTD", Boolean.FALSE);
        xmler = factory.createXMLStreamReader(stream);
        Map<String, List<String>> map = this.readServiceConfigStream(xmler);
        FileUtil.safeClose(xmler);
        FileUtil.safeClose(stream);
        return map;
        {
            catch (Throwable throwable) {
                try {
                    try {
                        FileUtil.safeClose(xmler);
                        throw throwable;
                    }
                    catch (Exception e) {
                        LOGGER.error("Error Loading config service {}", (Object)configServicePath, (Object)e);
                        FileUtil.safeClose(stream);
                    }
                }
                catch (Throwable throwable2) {
                    FileUtil.safeClose(stream);
                    throw throwable2;
                }
            }
        }
        return null;
    }

    private Map<String, List<String>> readServiceConfigStream(XMLStreamReader xmler) throws XMLStreamException {
        HashMap<String, List<String>> configParams = new HashMap<String, List<String>>();
        while (xmler.hasNext()) {
            if (xmler.next() != 1) continue;
            String localName = xmler.getLocalName();
            if ("property".equals(localName)) {
                String name = xmler.getAttributeValue(null, "name");
                String value = xmler.getAttributeValue(null, "value");
                this.addConfigParam(configParams, PARAM_PROPERTY, String.format("%s %s", name, value));
                continue;
            }
            if (!"argument".equals(localName)) continue;
            this.addConfigParam(configParams, PARAM_ARGUMENT, xmler.getElementText());
        }
        return configParams;
    }

    public void loadConfigProperties() {
        URI propURI = this.getPropertiesURI(CONFIG_PROPERTIES_PROP, CONFIG_PROPERTIES_FILE_VALUE);
        if (propURI != null) {
            URI localURI;
            this.configOutput.append("\n  Application configuration file = ");
            this.configOutput.append(propURI);
            if (!this.preferences.readJson(propURI) && (localURI = this.getLocalPropertiesURI(CONFIG_PROPERTIES_FILE_VALUE)) != null && !localURI.equals(propURI)) {
                LOGGER.warn("Cannot load configuration from {}, falling back to the local installation {}", (Object)propURI, (Object)localURI);
                this.applyLocalCodebase(true);
                this.configOutput.append("\n  Fallback to local configuration file = ");
                this.configOutput.append(localURI);
                this.preferences.readJson(localURI);
            }
        } else {
            LOGGER.error("No base.json path found, Weasis cannot start!");
        }
        if ((propURI = this.getPropertiesURI(EXTENDED_PROPERTIES_PROP, null)) != null) {
            this.configOutput.append("\n  Application extension configuration file = ");
            this.configOutput.append(propURI);
            this.preferences.readJson(propURI);
        }
        if (WeasisLauncher.Type.NATIVE.name().equals(System.getProperty("weasis.launch.type"))) {
            this.checkMinimalVersion(this.preferences);
        }
        if (this.preferences.isEmpty()) {
            throw new IllegalStateException("Cannot load weasis configuration file!");
        }
        this.properties.put(P_WEASIS_CONFIG_HASH, String.valueOf(this.preferences.hashCode()));
    }

    private void checkMinimalVersion(AppPreferences preferences) {
        String val = preferences.getValue(P_WEASIS_MIN_NATIVE_VERSION);
        if (Utils.hasText(val) && this.getProperty(P_WEASIS_CODEBASE_LOCAL) == null) {
            try {
                URI propURI = this.getLocalPropertiesURI(CONFIG_PROPERTIES_FILE_VALUE);
                AppPreferences localPrefs = new AppPreferences();
                localPrefs.readJson(propURI);
                Version loc = new Version(localPrefs.getValue(P_WEASIS_VERSION).replaceFirst("-", "."));
                Version min = new Version(val.replaceFirst("-", "."));
                if (loc.compareTo(min) < 0) {
                    LOGGER.warn("Start only with the native plug-ins because the version ({}) is lower the minimal version ({}) required remotely", (Object)loc, (Object)min);
                    preferences.clear();
                    preferences.putAll(localPrefs);
                    System.setProperty(P_WEASIS_MIN_NATIVE_VERSION, val);
                }
            }
            catch (Exception e) {
                LOGGER.error("Cannot check compatibility with remote package", e);
            }
        }
    }

    public URI getPropertiesURI(String configProp, String configFile) {
        URI propURL;
        String custom = this.properties.getProperty(configProp);
        if (Utils.hasText(custom)) {
            try {
                if (custom.startsWith("file:conf/")) {
                    propURL = new File(ConfigData.findLocalCodebase(), custom.substring(5)).toURI();
                }
                propURL = new URI(custom);
            }
            catch (URISyntaxException e) {
                LOGGER.error(configProp, e);
                return null;
            }
        } else {
            propURL = this.getLocalPropertiesURI(configFile);
        }
        return propURL;
    }

    private URI getLocalPropertiesURI(String configFile) {
        if (Utils.hasText(configFile)) {
            File confDir = new File(ConfigData.findLocalCodebase(), CONFIG_DIRECTORY);
            try {
                return new File(confDir, configFile).toURI();
            }
            catch (Exception ex) {
                LOGGER.error(configFile, ex);
            }
        }
        return null;
    }

    private static String toHex(int val) {
        char[] hexDigit = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] ch8 = new char[8];
        int i = 8;
        while (--i >= 0) {
            ch8[i] = hexDigit[val & 0xF];
            val >>= 4;
        }
        return String.valueOf(ch8);
    }

    public static File findLocalCodebase() {
        String[] vals;
        String jarLocation = null;
        String classpath = System.getProperty("java.class.path");
        for (String cp : vals = classpath.split(File.pathSeparator)) {
            if (!cp.endsWith("weasis-launcher.jar")) continue;
            jarLocation = cp;
        }
        if (jarLocation == null) {
            return new File(ConfigData.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile();
        }
        return new File(new File(jarLocation).getAbsolutePath()).getParentFile();
    }

    public String getProperty(String key) {
        return this.properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return this.properties.getProperty(key, defaultValue);
    }

    public static void setOsgiNativeLibSpecification() {
        String osName = System.getProperty(P_OS_NAME);
        String osArch = System.getProperty("os.arch");
        if (Utils.hasText(osName) && Utils.hasText(osArch)) {
            osName = osName.toLowerCase().startsWith("win") ? "windows" : (osName.toLowerCase().startsWith("mac") ? "macosx" : (osName.toLowerCase().startsWith("linux") ? "linux" : (osName.equals("SymbianOS") ? "epoc32" : (osName.equals("hp-ux") ? "hpux" : (osName.equals("OS/2") ? "os2" : (osName.equals("procnto") ? "qnx" : osName.toLowerCase()))))));
            osArch = osArch.equals("pentium") || osArch.equals("i386") || osArch.equals("i486") || osArch.equals("i586") || osArch.equals("i686") ? "x86" : (osArch.equals("amd64") || osArch.equals("em64t") || osArch.equals("x86_64") ? "x86-64" : (osArch.equals("arm") ? "armv7a" : (osArch.equals("power ppc") ? "powerpc" : (osArch.equals("psc1k") ? "ignite" : osArch.toLowerCase()))));
            System.setProperty(P_NATIVE_LIB_SPEC, osName + "-" + osArch);
        }
    }
}

