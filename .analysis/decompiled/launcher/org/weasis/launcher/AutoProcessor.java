/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.osgi.framework.Bundle
 *  org.osgi.framework.BundleContext
 *  org.osgi.service.startlevel.StartLevel
 */
package org.weasis.launcher;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.startlevel.StartLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tukaani.xz.XZInputStream;
import org.weasis.launcher.FileUtil;
import org.weasis.launcher.Messages;
import org.weasis.launcher.Utils;
import org.weasis.launcher.WeasisLoader;

public class AutoProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoProcessor.class);
    public static final String AUTO_INSTALL_PROP = "felix.auto.install";
    public static final String AUTO_START_PROP = "felix.auto.start";
    public static final String XZ_COMPRESSION = ".xz";

    private AutoProcessor() {
    }

    public static void process(Map<String, String> configMap, Properties modulesI18n, BundleContext context, WeasisLoader weasisLoader) {
        HashMap<String, String> map = configMap == null ? new HashMap<String, String>() : configMap;
        AutoProcessor.processAutoProperties(map, modulesI18n, context, weasisLoader);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void processAutoProperties(Map<String, String> configMap, Properties modulesi18n, BundleContext context, WeasisLoader weasisLoader) {
        BundleElement bundle;
        String bundleName;
        Bundle[] bundles;
        BundleElement b;
        StartLevel sl = (StartLevel)context.getService(context.getServiceReference(StartLevel.class.getName()));
        HashMap<String, BundleElement> bundleList = new HashMap<String, BundleElement>();
        Set<String> set = configMap.keySet();
        for (String o : set) {
            Bundle[] key = o.toLowerCase();
            if (!key.startsWith(AUTO_INSTALL_PROP) && !key.startsWith(AUTO_START_PROP)) continue;
            int startLevel = sl.getInitialBundleStartLevel();
            try {
                startLevel = Integer.parseInt(key.substring(key.lastIndexOf(46) + 1));
            }
            catch (NumberFormatException ex) {
                LOGGER.error("Invalid start level {}", (Object)key, (Object)ex);
            }
            boolean canBeStarted = key.startsWith(AUTO_START_PROP);
            StringTokenizer st = new StringTokenizer(configMap.get(key), "\" ", true);
            String location = AutoProcessor.nextLocation(st);
            while (location != null) {
                String bundleName2 = AutoProcessor.getBundleNameFromLocation(location);
                if (!"System Bundle".equals(bundleName2)) {
                    b = new BundleElement(startLevel, location, canBeStarted);
                    bundleList.put(bundleName2, b);
                }
                location = AutoProcessor.nextLocation(st);
            }
        }
        weasisLoader.setMax(bundleList.size());
        HashMap<String, Bundle> installedBundleMap = new HashMap<String, Bundle>();
        for (Bundle value : bundles = context.getBundles()) {
            bundleName = AutoProcessor.getBundleNameFromLocation(value.getLocation());
            if (bundleName == null) continue;
            try {
                BundleElement b2 = (BundleElement)bundleList.get(bundleName);
                if (b2 == null) {
                    if ("System Bundle".equals(bundleName)) continue;
                    value.uninstall();
                    LOGGER.info("Uninstall unused bundle: {}", (Object)bundleName);
                    continue;
                }
                if (value.getVersion().getQualifier().endsWith("SNAPSHOT")) {
                    value.uninstall();
                    LOGGER.info("Uninstall SNAPSHOT bundle: {}", (Object)bundleName);
                    continue;
                }
                installedBundleMap.put(bundleName, value);
            }
            catch (Exception e) {
                LOGGER.error("Cannot remove from OSGI cache the bundle {}", (Object)bundleName, (Object)e);
            }
        }
        boolean cache = Boolean.TRUE.toString().equals(System.getProperty("http.bundle.cache", Boolean.TRUE.toString()));
        int bundleIter = 0;
        for (Map.Entry element : bundleList.entrySet()) {
            bundleName = (String)element.getKey();
            bundle = (BundleElement)element.getValue();
            if (bundle == null) continue;
            try {
                weasisLoader.writeLabel(WeasisLoader.LBL_DOWNLOADING + " " + bundleName);
                b = (Bundle)installedBundleMap.get(bundleName);
                if (b == null) {
                    b = AutoProcessor.installBundle(context, bundle.getLocation(), cache);
                    installedBundleMap.put(bundleName, (Bundle)b);
                }
                sl.setBundleStartLevel((Bundle)b, bundle.getStartLevel());
                AutoProcessor.loadTranslationBundle(context, (Bundle)b, installedBundleMap, modulesi18n, cache);
            }
            catch (Exception ex) {
                if (bundleName.contains(System.getProperty("native.library.spec"))) {
                    LOGGER.error("Cannot install a native bundle {}}", (Object)bundleName, (Object)ex);
                    continue;
                }
                LOGGER.error("Cannot install bundle {}}", (Object)bundleName, (Object)ex);
                if ("onFirstInit".equals(configMap.get("org.osgi.framework.storage.clean"))) continue;
                throw new IllegalStateException("A bundle cannot be started");
            }
            finally {
                weasisLoader.setValue(++bundleIter);
            }
        }
        weasisLoader.writeLabel(Messages.getString("AutoProcessor.start"));
        for (Map.Entry element : bundleList.entrySet()) {
            bundleName = (String)element.getKey();
            bundle = (BundleElement)element.getValue();
            if (bundle == null || !bundle.isCanBeStarted()) continue;
            try {
                b = (Bundle)installedBundleMap.get(bundleName);
                if (b == null) {
                    b = AutoProcessor.installBundle(context, bundle.getLocation(), cache);
                }
                if (b == null) continue;
                b.start();
            }
            catch (Exception ex) {
                LOGGER.error("Cannot start bundle {}", (Object)bundleName, (Object)ex);
            }
        }
    }

    static String getBundleNameFromLocation(String location) {
        if (location != null) {
            int index = location.lastIndexOf(47);
            String name = index >= 0 ? location.substring(index + 1) : location;
            index = name.lastIndexOf(".jar");
            return index >= 0 ? name.substring(0, index) : name;
        }
        return null;
    }

    private static void loadTranslationBundle(BundleContext context, Bundle b, Map<String, Bundle> installedBundleMap, Properties modulesi18n, boolean cache) {
        if (!modulesi18n.isEmpty() && b != null) {
            String baseURL;
            StringBuilder p = new StringBuilder(b.getSymbolicName());
            p.append("-i18n.jar.xz");
            String filename = p.toString();
            String value = modulesi18n.getProperty(filename);
            if (value != null && (baseURL = System.getProperty("weasis.i18n")) != null) {
                String uri = baseURL + (String)(baseURL.endsWith("/") ? filename : "/" + filename);
                String bundleName = AutoProcessor.getBundleNameFromLocation(filename);
                try {
                    Bundle b2 = installedBundleMap.get(bundleName);
                    if (b2 == null) {
                        b2 = AutoProcessor.installBundle(context, uri, cache);
                        installedBundleMap.put(bundleName, b);
                    }
                    if (b2 != null && !value.equals(b2.getVersion().getQualifier())) {
                        if (b2.getLocation().startsWith(baseURL)) {
                            b2.update();
                        } else {
                            try {
                                b2.uninstall();
                                context.installBundle(uri, FileUtil.getAdaptedConnection(new URI(uri).toURL(), false).getInputStream());
                                installedBundleMap.put(bundleName, b);
                            }
                            catch (Exception exc) {
                                LOGGER.error("Cannot install a translation bundle {}", (Object)uri, (Object)exc);
                            }
                        }
                    }
                }
                catch (Exception e) {
                    LOGGER.error("Cannot install a translation bundle {}", (Object)uri, (Object)e);
                }
            }
        }
    }

    private static String nextLocation(StringTokenizer st) {
        String retVal = null;
        if (st.countTokens() > 0) {
            String tokenList = "\" ";
            StringBuilder tokBuf = new StringBuilder(10);
            String tok = null;
            boolean inQuote = false;
            boolean tokStarted = false;
            boolean exit = false;
            while (st.hasMoreTokens() && !exit) {
                tok = st.nextToken(tokenList);
                if (tok.equals("\"")) {
                    boolean bl = inQuote = !inQuote;
                    if (inQuote) {
                        tokenList = "\"";
                        continue;
                    }
                    tokenList = "\" ";
                    continue;
                }
                if (tok.equals(" ")) {
                    if (!tokStarted) continue;
                    retVal = tokBuf.toString();
                    tokStarted = false;
                    tokBuf = new StringBuilder(10);
                    exit = true;
                    continue;
                }
                tokStarted = true;
                tokBuf.append(tok.trim());
            }
            if (!exit && tokStarted) {
                retVal = tokBuf.toString();
            }
            if (Utils.hasText(retVal)) {
                retVal = Utils.adaptPathToUri(retVal);
            }
        }
        return retVal;
    }

    private static boolean isFragment(Bundle bundle) {
        return bundle.getHeaders().get("Fragment-Host") != null;
    }

    private static Bundle installBundle(BundleContext context, String location, boolean httpCache) throws Exception {
        boolean pack = location.endsWith(XZ_COMPRESSION);
        if (pack) {
            boolean bl = pack = context.getBundle(location = location.substring(0, location.length() - 3)) == null;
        }
        if (pack) {
            Bundle bundle;
            URL url = new URL(location + XZ_COMPRESSION);
            XZInputStream xzStream = new XZInputStream(FileUtil.getAdaptedConnection(url, httpCache).getInputStream());
            try {
                bundle = context.installBundle(location, (InputStream)xzStream);
            }
            catch (Throwable throwable) {
                try {
                    try {
                        xzStream.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                    throw throwable;
                }
                catch (Exception e) {
                    LOGGER.error("Cannot install xz compressed bundle {}", (Object)url, (Object)e);
                }
            }
            xzStream.close();
            return bundle;
        }
        return context.installBundle(location, FileUtil.getAdaptedConnection(new URI(location).toURL(), httpCache).getInputStream());
    }

    static class BundleElement {
        private final int startLevel;
        private final String location;
        private final boolean canBeStarted;

        public BundleElement(int startLevel, String location, boolean canBeStarted) {
            this.startLevel = startLevel;
            this.location = location;
            this.canBeStarted = canBeStarted;
        }

        public int getStartLevel() {
            return this.startLevel;
        }

        public String getLocation() {
            return this.location;
        }

        public boolean isCanBeStarted() {
            return this.canBeStarted;
        }
    }
}

