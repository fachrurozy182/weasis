/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.felix.framework.Felix
 *  org.osgi.framework.Bundle
 *  org.osgi.framework.Version
 *  org.osgi.util.tracker.ServiceTracker
 */
package org.weasis.launcher;

import com.formdev.flatlaf.util.SystemInfo;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.desktop.OpenFilesEvent;
import java.awt.desktop.OpenURIEvent;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import javax.management.ObjectName;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import org.apache.felix.framework.Felix;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.launcher.FileUtil;
import org.weasis.launcher.LookAndFeels;
import org.weasis.launcher.Messages;
import org.weasis.launcher.RemotePrefService;
import org.weasis.launcher.SwingResources;
import org.weasis.launcher.Utils;
import org.weasis.launcher.WeasisLoader;
import org.weasis.launcher.WeasisMainFrame;
import org.weasis.pref.ConfigData;

public class WeasisLauncher {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeasisLauncher.class);
    protected Felix mFelix = null;
    protected ServiceTracker mTracker = null;
    protected volatile boolean frameworkLoaded = false;
    protected String look = null;
    protected RemotePrefService remotePrefs;
    protected String localPrefsDir;
    protected volatile boolean closing = false;
    protected final Properties modulesi18n;
    protected final ConfigData configData;
    private final List<String> pendingCommands = new ArrayList<String>();

    public WeasisLauncher(ConfigData configData) {
        this.configData = Objects.requireNonNull(configData);
        this.modulesi18n = new Properties();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void launch(Type type) throws Exception {
        Map<String, String> serverProp = this.configData.getFelixProps();
        String cacheDir = serverProp.get("org.osgi.framework.storage") + "-" + this.configData.getSourceID();
        serverProp.put("org.osgi.framework.storage", cacheDir);
        WeasisLoader loader = this.loadProperties(serverProp, this.configData.getConfigOutput());
        WeasisMainFrame mainFrame = loader.getMainFrame();
        String minVersion = System.getProperty("weasis.min.native.version");
        if (Utils.hasText(minVersion)) {
            EventQueue.invokeAndWait(() -> {
                String appName = System.getProperty("weasis.name");
                int response = JOptionPane.showOptionDialog(mainFrame.getWindow(), String.format("%s\n\n%s", String.format(Messages.getString("WeasisLauncher.update_min"), appName, minVersion), Messages.getString("WeasisLauncher.continue_local")), null, 0, 0, null, null, null);
                if (response != 0) {
                    LOGGER.error("Do not continue the launch with the local version");
                    System.exit(-1);
                }
            });
        }
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownHook));
        WeasisLauncher.displayStartingAsciiIcon();
        int exitStatus = 0;
        try {
            List<String> deferred;
            String goshArgs = WeasisLauncher.getGoshArgs(serverProp);
            if (this.closing) {
                return;
            }
            this.mFelix = new Felix(serverProp);
            this.mFelix.init();
            loader.setFelix(serverProp, this.mFelix.getBundleContext(), this.modulesi18n);
            loader.writeLabel(String.format(Messages.getString("WeasisLauncher.starting"), System.getProperty("weasis.name")));
            this.mTracker = new ServiceTracker(this.mFelix.getBundleContext(), "org.apache.felix.service.command.CommandProcessor", null);
            this.mTracker.open();
            if (this.closing) {
                return;
            }
            this.mFelix.start();
            loader.close();
            loader = null;
            String logActivation = serverProp.get("org.apache.sling.commons.log.file");
            if (Utils.hasText(logActivation)) {
                LOGGER.info("Logs has been delegated to the OSGI service and can be read in {}", (Object)logActivation);
            }
            this.executeCommands(this.configData.getArguments(), goshArgs);
            this.checkBundleUI(serverProp);
            List<String> list = this.pendingCommands;
            synchronized (list) {
                this.frameworkLoaded = true;
                deferred = List.copyOf(this.pendingCommands);
                this.pendingCommands.clear();
            }
            if (!deferred.isEmpty()) {
                this.executeCommands(deferred, null);
            }
            this.showMessage(mainFrame, serverProp);
            if (this.closing) {
                return;
            }
            this.mFelix.waitForStop(0L);
            System.exit(0);
        }
        catch (Throwable ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            exitStatus = -1;
            LOGGER.error("Cannot not start framework.", ex);
            LOGGER.error("Weasis cache will be cleaned at next launch.");
            LOGGER.error("State of the framework:");
            for (Bundle b : this.mFelix.getBundleContext().getBundles()) {
                LOGGER.error(" * {}-{} {}", b.getSymbolicName(), b.getVersion().toString(), State.valueOf(b.getState()));
            }
            WeasisLauncher.resetBundleCache();
        }
        finally {
            Runtime.getRuntime().halt(exitStatus);
        }
    }

    private void checkBundleUI(Map<String, String> serverProp) {
        String mainUI = serverProp.getOrDefault("weasis.main.ui", "").trim();
        if (Utils.hasText(mainUI)) {
            boolean uiStarted = false;
            for (Bundle b : this.mFelix.getBundleContext().getBundles()) {
                if (!b.getSymbolicName().equals(mainUI) || b.getState() != 32) continue;
                uiStarted = true;
                break;
            }
            if (!uiStarted) {
                throw new IllegalStateException("Main User Interface bundle cannot be started");
            }
        }
    }

    private static String getGoshArgs(Map<String, String> serverProp) {
        String val;
        String goshArgs = System.getProperty("gosh.args", serverProp.getOrDefault("gosh.args", ""));
        if (goshArgs.isEmpty() && !(val = System.getProperty("gosh.port", "")).isEmpty()) {
            try {
                goshArgs = String.format("-sc telnetd -p %d start", Integer.parseInt(val));
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        serverProp.put("gosh.args", "--nointeractive --noshutdown");
        return goshArgs;
    }

    private static void displayStartingAsciiIcon() {
        String asciiArt = "\nStarting OSGI Bundles...\n\n        __        __             _     \n        \\ \\      / /__  __ _ ___(_)___ \n         \\ \\ /\\ / / _ \\/ _` / __| / __|\n          \\ V  V /  __/ (_| \\__ \\ \\__ \\\n           \\_/\\_/ \\___|\\__,_|___/_|___/\n\n";
        LOGGER.info("\u001b[32m{}\u001b[0m", (Object)asciiArt);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void executeCommandsWhenLoaded(List<String> commandList) {
        List<String> list = this.pendingCommands;
        synchronized (list) {
            if (!this.frameworkLoaded) {
                this.pendingCommands.addAll(commandList);
                return;
            }
        }
        this.executeCommands(commandList, null);
    }

    protected void executeCommands(List<String> commandList, String goshArgs) {
        SwingUtilities.invokeLater(() -> {
            this.mTracker.open();
            Object commandSession = WeasisLauncher.getCommandSession(this.mTracker.getService(), new Object[]{new FileInputStream(FileDescriptor.in), new FileOutputStream(FileDescriptor.out), new FileOutputStream(FileDescriptor.err)});
            if (commandSession != null) {
                if (goshArgs == null) {
                    WeasisLauncher.commandSessionExecute(commandSession, "weasis:ui -v");
                } else {
                    for (Bundle b : this.mFelix.getBundleContext().getBundles()) {
                        if (!b.getSymbolicName().equals("org.apache.felix.gogo.shell") || b.getState() != 32) continue;
                        WeasisLauncher.initCommandSession(commandSession, goshArgs);
                        break;
                    }
                }
                for (String command : commandList) {
                    WeasisLauncher.commandSessionExecute(commandSession, command);
                }
                WeasisLauncher.commandSessionClose(commandSession);
            }
            this.mTracker.close();
        });
    }

    private static void resetBundleCache() {
        Path sourceIdProps = Path.of(System.getProperty("weasis.path", ""), System.getProperty("weasis.source.id") + ".properties");
        Properties localSourceProp = new Properties();
        FileUtil.loadProperties(sourceIdProps, localSourceProp);
        localSourceProp.setProperty("weasis.clean.cache", Boolean.TRUE.toString());
        FileUtil.storeProperties(sourceIdProps, localSourceProp, null);
    }

    private void showMessage(WeasisMainFrame mainFrame, Map<String, String> serverProp) {
        String val;
        String versionOld = serverProp.get("prev.weasis.version");
        String versionNew = serverProp.getOrDefault("weasis.version", "0.0.0");
        if (versionOld == null) {
            String val2 = serverProp.get("prev.weasis.show.disclaimer");
            String accept = serverProp.get("weasis.accept.disclaimer");
            if (Utils.emptyToTrue(val2) && !Utils.emptyToFalse(accept)) {
                EventQueue.invokeLater(() -> {
                    Object[] options = new Object[]{Messages.getString("WeasisLauncher.ok"), Messages.getString("WeasisLauncher.no")};
                    String appName = System.getProperty("weasis.name");
                    int response = JOptionPane.showOptionDialog(mainFrame.getWindow(), String.format(Messages.getString("WeasisLauncher.msg"), appName), String.format(Messages.getString("WeasisLauncher.first"), appName), 0, 2, null, options, null);
                    if (response == 0) {
                        System.setProperty("weasis.accept.disclaimer", Boolean.TRUE.toString());
                    } else {
                        Path path = Path.of(System.getProperty("weasis.path", ""), System.getProperty("weasis.source.id") + ".properties");
                        FileUtil.delete(path);
                        LOGGER.error("Refusing the disclaimer");
                        System.exit(-1);
                    }
                });
            }
        } else if (versionNew != null && !versionNew.equals(versionOld) && Utils.emptyToTrue(val = serverProp.get("prev.weasis.show.release"))) {
            try {
                Version vOld = WeasisLauncher.getVersion(versionOld);
                Version vNew = WeasisLauncher.getVersion(versionNew);
                if (vNew.compareTo(vOld) > 0) {
                    String lastTag = serverProp.get("weasis.version.release");
                    if (lastTag != null && vNew.compareTo(vOld = WeasisLauncher.getVersion(lastTag)) <= 0) {
                        return;
                    }
                    System.setProperty("weasis.version.release", vNew.toString());
                }
            }
            catch (Exception e2) {
                LOGGER.error("Cannot read version", e2);
                return;
            }
            String releaseNotesUrl = serverProp.get("weasis.releasenotes");
            StringBuilder message = new StringBuilder("<P>");
            message.append(String.format(Messages.getString("WeasisLauncher.change.version"), System.getProperty("weasis.name"), versionOld, versionNew));
            EventQueue.invokeLater(() -> {
                JTextPane jTextPane1 = new JTextPane();
                jTextPane1.setBorder(new EmptyBorder(5, 5, 15, 5));
                jTextPane1.setContentType("text/html");
                jTextPane1.setEditable(false);
                jTextPane1.addHyperlinkListener(e -> {
                    JTextPane pane = (JTextPane)e.getSource();
                    if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                        pane.setToolTipText(e.getDescription());
                    } else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
                        pane.setToolTipText(null);
                    } else if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        Utils.openInDefaultBrowser(e.getURL());
                    }
                });
                message.append("<BR>");
                String rn = Messages.getString("WeasisLauncher.release");
                message.append(String.format("<a href=\"%s", releaseNotesUrl));
                message.append("\">");
                message.append(rn);
                message.append("</a>");
                message.append("</P>");
                jTextPane1.setText(message.toString());
                JOptionPane.showMessageDialog(mainFrame.getWindow(), jTextPane1, Messages.getString("WeasisLauncher.News"), 1);
            });
        }
    }

    private static Version getVersion(String version) {
        String v = "";
        if (version != null) {
            int index = version.indexOf(45);
            v = index > 0 ? version.substring(0, index) : version;
        }
        return new Version(v);
    }

    public static Object getCommandSession(Object commandProcessor, Object[] arguments) {
        if (commandProcessor == null) {
            return null;
        }
        Class[] parameterTypes = new Class[]{InputStream.class, OutputStream.class, OutputStream.class};
        try {
            Method nameMethod = commandProcessor.getClass().getMethod("createSession", parameterTypes);
            Object commandSession = nameMethod.invoke(commandProcessor, arguments);
            WeasisLauncher.addCommandSessionListener(commandProcessor);
            return commandSession;
        }
        catch (Exception ex) {
            LOGGER.error("Create a command session", ex);
            return null;
        }
    }

    private static void addCommandSessionListener(Object commandProcessor) {
        try {
            ClassLoader loader = commandProcessor.getClass().getClassLoader();
            Class<?> c = loader.loadClass("org.apache.felix.service.command.CommandSessionListener");
            Method nameMethod = commandProcessor.getClass().getMethod("addListener", c);
            Object listener = Proxy.newProxyInstance(loader, new Class[]{c}, (proxy, method, args) -> {
                String listenerMethod = method.getName();
                if (listenerMethod.equals("beforeExecute")) {
                    String arg = args[1].toString();
                    if (arg.startsWith("gosh") || arg.startsWith("gogo:gosh")) {
                        WeasisLauncher.commandSessionExecute(args[0], "gogo.option.noglob=on");
                    }
                } else if (listenerMethod.equals("equals")) {
                    return proxy.getClass().isAssignableFrom(args[0].getClass());
                }
                return null;
            });
            nameMethod.invoke(commandProcessor, listener);
        }
        catch (Exception e) {
            LOGGER.error("Add command session listener", e);
        }
    }

    public static boolean initCommandSession(Object commandSession, String args) {
        try {
            for (int i = 0; i < 100 && WeasisLauncher.commandSessionGet(commandSession, "gogo:gosh") == null; ++i) {
                TimeUnit.MILLISECONDS.sleep(10L);
            }
            Class[] parameterTypes = new Class[]{CharSequence.class};
            Object[] arguments = new Object[]{"gogo:gosh --login " + (args == null ? "" : args)};
            Method nameMethod = commandSession.getClass().getMethod("execute", parameterTypes);
            nameMethod.invoke(commandSession, arguments);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        catch (Exception e) {
            LOGGER.error("Init command session", e);
        }
        return false;
    }

    public static Object commandSessionGet(Object commandSession, String key) {
        if (commandSession == null || key == null) {
            return null;
        }
        Class[] parameterTypes = new Class[]{String.class};
        Object[] arguments = new Object[]{key};
        try {
            Method nameMethod = commandSession.getClass().getMethod("get", parameterTypes);
            return nameMethod.invoke(commandSession, arguments);
        }
        catch (Exception ex) {
            LOGGER.error("Invoke a command", ex);
            return null;
        }
    }

    public static boolean commandSessionClose(Object commandSession) {
        if (commandSession == null) {
            return false;
        }
        try {
            Method nameMethod = commandSession.getClass().getMethod("close", new Class[0]);
            nameMethod.invoke(commandSession, new Object[0]);
            return true;
        }
        catch (Exception ex) {
            LOGGER.error("Close command session", ex);
            return false;
        }
    }

    public static Object commandSessionExecute(Object commandSession, CharSequence charSequence) {
        if (commandSession == null) {
            return false;
        }
        Class[] parameterTypes = new Class[]{CharSequence.class};
        Object[] arguments = new Object[]{charSequence};
        try {
            Method nameMethod = commandSession.getClass().getMethod("execute", parameterTypes);
            return nameMethod.invoke(commandSession, arguments);
        }
        catch (Exception ex) {
            LOGGER.error("Execute command", ex);
            return null;
        }
    }

    public static void readProperties(URI propURI, Properties props) {
        try (InputStream is = FileUtil.getAdaptedConnection(propURI.toURL(), false).getInputStream();){
            props.load(is);
        }
        catch (Exception ex) {
            LOGGER.error("Cannot read properties file: {}", (Object)propURI, (Object)ex);
        }
    }

    private static String getGeneralProperty(String key, String defaultValue, Map<String, String> serverProp, Properties localProp, boolean storeInLocalPref, boolean serviceProperty) {
        String value = localProp.getProperty(key, null);
        String defaultVal = System.getProperty(key, null);
        if (defaultVal == null) {
            defaultVal = serverProp.getOrDefault(key, defaultValue);
        }
        if (value == null) {
            value = defaultVal;
            if (storeInLocalPref && value != null) {
                localProp.setProperty(key, value);
            }
        }
        if (serviceProperty) {
            serverProp.put(key, value);
            serverProp.put("def." + key, defaultVal);
        }
        LOGGER.info("Config of {} = {}", (Object)key, (Object)value);
        return value;
    }

    public WeasisLoader loadProperties(Map<String, String> serverProp, StringBuilder conf) {
        String showRelease;
        String showDisclaimer;
        WeasisMainFrame mainFrame;
        Properties currentProps;
        File prefDir;
        String profileName;
        String dir;
        block43: {
            Desktop desktopApp;
            String useSystemFileChooser;
            String localLook;
            dir = this.configData.getProperty("weasis.path");
            profileName = this.configData.getProperty("weasis.profile", "default");
            String user = this.configData.getProperty("weasis.user");
            this.configData.applyProxy(dir + File.separator + "data" + File.separator + "weasis-core");
            String bufDir = dir + File.separator + "preferences" + File.separator + user + File.separator + profileName;
            prefDir = new File(bufDir);
            try {
                prefDir.mkdirs();
            }
            catch (Exception e2) {
                prefDir = new File(dir);
                LOGGER.error("Cannot create preferences folders", e2);
            }
            this.localPrefsDir = prefDir.getPath();
            serverProp.put("weasis.pref.dir", prefDir.getPath());
            currentProps = new Properties();
            FileUtil.loadProperties(Path.of(prefDir.getPath(), "weasis.properties"), currentProps);
            currentProps.stringPropertyNames().forEach(key -> serverProp.put("wp.init." + key, currentProps.getProperty((String)key)));
            String remotePrefURL = this.configData.getProperty("weasis.pref.url");
            if (Utils.hasText(remotePrefURL)) {
                String storeLocalSession = "weasis.pref.store.local.session";
                String defaultVal = this.configData.getProperty(storeLocalSession, null);
                if (defaultVal == null) {
                    defaultVal = serverProp.getOrDefault(storeLocalSession, Boolean.FALSE.toString());
                }
                serverProp.put(storeLocalSession, defaultVal);
                try {
                    this.remotePrefs = new RemotePrefService(remotePrefURL, serverProp, user, profileName);
                    Properties remote = this.remotePrefs.readLauncherPref(null);
                    currentProps.putAll((Map<?, ?>)remote);
                    if (remote.size() < currentProps.size()) {
                        serverProp.put("wp.init.diff.remote.pref", Boolean.TRUE.toString());
                    }
                }
                catch (Exception e3) {
                    LOGGER.error("Cannot read Launcher preference for user: {}", (Object)user, (Object)e3);
                }
            }
            String lang = WeasisLauncher.getGeneralProperty("locale.lang.code", "en", serverProp, currentProps, true, false);
            WeasisLauncher.getGeneralProperty("locale.format.code", "system", serverProp, currentProps, true, false);
            WeasisLauncher.getGeneralProperty("org.apache.sling.commons.log.level", "INFO", serverProp, currentProps, true, true);
            String logActivation = WeasisLauncher.getGeneralProperty("org.apache.sling.commons.log.file.activate", Boolean.FALSE.toString(), serverProp, currentProps, true, true);
            if (Utils.emptyToFalse(logActivation)) {
                String logFile = dir + File.separator + "log" + File.separator + "default.log";
                serverProp.put("org.apache.sling.commons.log.file", logFile);
                currentProps.remove("org.apache.sling.commons.log.file");
            }
            WeasisLauncher.getGeneralProperty("org.apache.sling.commons.log.file.number", "20", serverProp, currentProps, true, true);
            WeasisLauncher.getGeneralProperty("org.apache.sling.commons.log.file.size", "10MB", serverProp, currentProps, true, true);
            WeasisLauncher.getGeneralProperty("org.apache.sling.commons.log.stack.limit", "3", serverProp, currentProps, true, true);
            WeasisLauncher.getGeneralProperty("org.apache.sling.commons.log.pattern", "%d{dd.MM.yyyy HH:mm:ss.SSS} *%-5level* [%thread] %logger{36}: %msg%ex{3}%n", serverProp, currentProps, false, true);
            this.loadI18nModules();
            Locale locale = WeasisLauncher.textToLocale(lang);
            Locale.setDefault(locale);
            UIManager.getDefaults().setDefaultLocale(locale);
            JComponent.setDefaultLocale(locale);
            if (Locale.ENGLISH.equals(locale)) {
                this.modulesi18n.clear();
            } else {
                String suffix = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_PROPERTIES).toBundleName("", locale);
                SwingResources.loadResources("/swing/basic" + suffix + ".properties");
                SwingResources.loadResources("/swing/synth" + suffix + ".properties");
            }
            String sysSpec = System.getProperty("native.library.spec", "unknown");
            int index = sysSpec.indexOf(45);
            if (index > 0) {
                String nativeLook = "weasis.theme." + sysSpec.substring(0, index);
                this.look = System.getProperty(nativeLook, null);
                if (this.look == null) {
                    this.look = serverProp.get(nativeLook);
                }
            }
            if (this.look == null) {
                this.look = System.getProperty("weasis.theme", null);
                if (this.look == null) {
                    this.look = serverProp.get("weasis.theme");
                }
            }
            if ((localLook = currentProps.getProperty("weasis.theme", null)) != null) {
                this.look = localLook;
            }
            LookAndFeels lookAndFeels = new LookAndFeels();
            LookAndFeels.ReadableLookAndFeelInfo lookAndFeelInfo = lookAndFeels.getAvailableLookAndFeel(this.look, profileName);
            if (SystemInfo.isLinux) {
                String decoration = WeasisLauncher.getGeneralProperty("weasis.linux.windows.decoration", Boolean.FALSE.toString(), serverProp, currentProps, true, true);
                if (Utils.emptyToFalse(decoration)) {
                    JFrame.setDefaultLookAndFeelDecorated(true);
                    JDialog.setDefaultLookAndFeelDecorated(true);
                }
            } else if (SystemInfo.isMacOS) {
                System.setProperty("apple.laf.useScreenMenuBar", "true");
                System.setProperty("apple.awt.application.name", System.getProperty("weasis.name"));
                System.setProperty("apple.awt.application.appearance", lookAndFeelInfo.isDark() ? "NSAppearanceNameDarkAqua" : "NSAppearanceNameAqua");
            }
            UIManager.setInstalledLookAndFeels(lookAndFeels.getLookAndFeels().toArray(new UIManager.LookAndFeelInfo[0]));
            String scaleFactor = WeasisLauncher.getGeneralProperty("flatlaf.uiScale", null, serverProp, currentProps, true, false);
            if (scaleFactor != null) {
                System.setProperty("flatlaf.uiScale", scaleFactor);
            }
            if ((useSystemFileChooser = WeasisLauncher.getGeneralProperty("weasis.use.system.file.chooser", null, serverProp, currentProps, true, false)) != null) {
                System.setProperty("flatlaf.useSystemFileChooser", useSystemFileChooser);
            }
            if ((desktopApp = Desktop.getDesktop()).isSupported(Desktop.Action.APP_OPEN_URI)) {
                long time = System.currentTimeMillis();
                boolean noArgs = this.configData.getArguments().isEmpty();
                desktopApp.setOpenURIHandler(e -> this.handleOpenURI(e, time, noArgs));
            }
            if (desktopApp.isSupported(Desktop.Action.APP_OPEN_FILE)) {
                desktopApp.setOpenFileHandler(this::handleOpenFile);
            }
            mainFrame = new WeasisMainFrame();
            try {
                SwingUtilities.invokeAndWait(() -> {
                    this.look = lookAndFeels.setLookAndFeel(lookAndFeelInfo);
                    try {
                        mainFrame.setConfigData(this.configData);
                        ObjectName objectName2 = new ObjectName("weasis:name=MainWindow");
                        mainFrame.setRootPaneContainer(new JFrame());
                        ManagementFactory.getPlatformMBeanServer().registerMBean(mainFrame, objectName2);
                    }
                    catch (Exception e1) {
                        LOGGER.error("Cannot register the main frame", e1);
                    }
                });
            }
            catch (Exception e4) {
                LOGGER.error("Unable to set the Look&Feel {}", (Object)this.look);
                if (!(e4 instanceof InterruptedException)) break block43;
                Thread.currentThread().interrupt();
            }
        }
        currentProps.put("weasis.theme", this.look);
        Path sourceIDProps = Path.of(dir, this.configData.getProperty("weasis.source.id") + ".properties");
        Properties localSourceProp = new Properties();
        FileUtil.loadProperties(sourceIDProps, localSourceProp);
        String versionOld = localSourceProp.getProperty("weasis.version");
        if (Utils.hasText(versionOld)) {
            serverProp.put("prev.weasis.version", versionOld);
        }
        String versionNew = serverProp.getOrDefault("weasis.version", "0.0.0");
        String cleanCacheAfterCrash = localSourceProp.getProperty("weasis.clean.cache");
        boolean update = false;
        String defaultResources = "/resources.zip";
        boolean mavenRepo = Utils.hasText(System.getProperty("maven.localRepository", null));
        Object resPath = this.configData.getProperty("weasis.resources.url", null);
        if (!Utils.hasText((CharSequence)resPath)) {
            String cdb;
            resPath = serverProp.getOrDefault("weasis.resources.url", null);
            if (!mavenRepo && !Utils.hasText((CharSequence)resPath) && Utils.hasText(cdb = this.configData.getProperty("weasis.codebase.url", null)) && !cdb.startsWith("file:")) {
                resPath = cdb + defaultResources;
            }
        }
        Path cacheDir = null;
        try {
            String date;
            if (WeasisLauncher.isZipResource((String)resPath) && (date = FileUtil.writeResources((String)resPath, cacheDir = Path.of(dir + File.separator + "data" + File.separator + System.getProperty("weasis.source.id"), "resources"), localSourceProp.getProperty("weasis.resources.date"))) != null) {
                update = true;
                localSourceProp.put("weasis.resources.date", date);
            }
        }
        catch (Exception e5) {
            cacheDir = null;
            LOGGER.error("Loads the resource folder", e5);
        }
        if (cacheDir == null) {
            if (mavenRepo) {
                File f = new File(System.getProperty("user.dir"));
                cacheDir = Path.of(f.getParent(), "weasis-distributions" + File.separator + "resources");
            } else {
                String cdbl = this.configData.getProperty("weasis.codebase.local");
                cacheDir = Path.of(cdbl, "resources");
            }
        }
        serverProp.put("weasis.resources.path", cacheDir.toString());
        WeasisLoader loader = new WeasisLoader(cacheDir, mainFrame);
        loader.open();
        if (versionNew != null) {
            localSourceProp.put("weasis.version", versionNew);
            if (versionOld == null || !versionOld.equals(versionNew)) {
                update = true;
            }
        }
        if (Utils.hasText(showDisclaimer = WeasisLauncher.getGeneralProperty("weasis.show.disclaimer", Boolean.TRUE.toString(), serverProp, currentProps, false, false))) {
            serverProp.put("prev.weasis.show.disclaimer", showDisclaimer);
        }
        if (Utils.hasText(showRelease = WeasisLauncher.getGeneralProperty("weasis.show.release", Boolean.TRUE.toString(), serverProp, currentProps, false, false))) {
            serverProp.put("prev.weasis.show.release", showRelease);
        }
        boolean cleanCache = Boolean.parseBoolean(serverProp.get("weasis.clean.previous.version"));
        if (Boolean.TRUE.toString().equals(cleanCacheAfterCrash)) {
            serverProp.put("org.osgi.framework.storage.clean", "onFirstInit");
            localSourceProp.remove("weasis.clean.cache");
            update = true;
            LOGGER.info("Clean plug-in cache because Weasis has crashed during the previous launch");
        } else if (cleanCache && versionNew != null && !versionNew.equals(versionOld)) {
            LOGGER.info("Clean previous Weasis version: {}", (Object)versionOld);
            serverProp.put("org.osgi.framework.storage.clean", "onFirstInit");
            LOGGER.info("Clean plug-in cache because the version has changed");
        }
        if (update) {
            FileUtil.storeProperties(sourceIDProps, localSourceProp, null);
            currentProps.setProperty("weasis.show.update.next.release", Boolean.TRUE.toString());
        }
        Set<String> pKeys = currentProps.stringPropertyNames();
        serverProp.put("wp.list", String.join((CharSequence)",", pKeys));
        pKeys.forEach(key -> serverProp.put((String)key, currentProps.getProperty((String)key)));
        String pevConf = conf.toString();
        conf.setLength(0);
        conf.append("\n***** Configuration *****");
        conf.append("\n  Last running version = ");
        conf.append(versionOld);
        conf.append("\n  Current version = ");
        conf.append(versionNew);
        conf.append("\n  Application name = ");
        conf.append(this.configData.getProperty("weasis.name"));
        conf.append("\n  Application Source ID = ");
        conf.append(System.getProperty("weasis.source.id"));
        conf.append("\n  Application Profile = ");
        conf.append(profileName);
        conf.append(pevConf);
        conf.append("\n  User = ");
        conf.append(System.getProperty("weasis.user", "user"));
        conf.append("\n  User home directory = ");
        conf.append(dir);
        conf.append("\n  Resources path = ");
        conf.append(cacheDir);
        conf.append("\n  Preferences directory = ");
        conf.append(prefDir.getPath());
        conf.append("\n  Look and Feel = ");
        conf.append(this.look);
        String i18nPath = System.getProperty("weasis.i18n");
        if (Utils.hasText(i18nPath)) {
            conf.append("\n  Languages path = ");
            conf.append(i18nPath);
        }
        conf.append("\n  Languages available = ");
        conf.append(System.getProperty("weasis.languages", "en"));
        conf.append("\n  OSGI native specs = ");
        conf.append(System.getProperty("native.library.spec"));
        conf.append("\n  HTTP user agent = ");
        conf.append(System.getProperty("http.agent"));
        conf.append("\n  Operating system = ");
        conf.append(System.getProperty("os.name"));
        conf.append(' ');
        conf.append(System.getProperty("os.version"));
        conf.append(' ');
        conf.append(System.getProperty("os.arch"));
        conf.append("\n  Java vendor = ");
        conf.append(System.getProperty("java.vendor"));
        conf.append("\n  Java version = ");
        conf.append(System.getProperty("java.version"));
        conf.append("\n  Java Path = ");
        conf.append(System.getProperty("java.home"));
        conf.append("\n  Java max memory (less survivor space) = ");
        conf.append(FileUtil.humanReadableByte(Runtime.getRuntime().maxMemory(), false));
        conf.append("\n***** End of Configuration *****");
        LOGGER.info(conf.toString());
        return loader;
    }

    private static boolean isZipResource(String path) {
        if (Utils.hasText(path) && path.endsWith(".zip")) {
            if (path.startsWith("file:")) {
                try {
                    URLConnection connection = new URL(path).openConnection();
                    return connection.getContentLength() > 0;
                }
                catch (IOException iOException) {}
            } else {
                return true;
            }
        }
        return false;
    }

    private void loadI18nModules() {
        try {
            String cdb;
            String cdbl = this.configData.getProperty("weasis.codebase.local");
            Object path = this.configData.getProperty("weasis.i18n", null);
            if (Utils.hasText((CharSequence)path)) {
                path = (String)path + (((String)path).endsWith("/") ? "buildNumber.properties" : "/buildNumber.properties");
                WeasisLauncher.readProperties(new URI((String)path), this.modulesi18n);
            } else if (cdbl == null && Utils.hasText(cdb = this.configData.getProperty("weasis.codebase.url", null))) {
                path = cdb.substring(0, cdb.lastIndexOf(47)) + "/weasis-i18n";
                WeasisLauncher.readProperties(new URI((String)path + "/buildNumber.properties"), this.modulesi18n);
                if (!this.modulesi18n.isEmpty()) {
                    System.setProperty("weasis.i18n", (String)path);
                }
            }
            if (this.modulesi18n.isEmpty()) {
                File file;
                if (cdbl == null) {
                    cdbl = ConfigData.findLocalCodebase().getPath();
                }
                if ((file = new File(cdbl, "bundle-i18n" + File.separator + "buildNumber.properties")).canRead()) {
                    WeasisLauncher.readProperties(file.toURI(), this.modulesi18n);
                    if (!this.modulesi18n.isEmpty()) {
                        System.setProperty("weasis.i18n", file.getParentFile().toURI().toString());
                    }
                }
            }
            if (!this.modulesi18n.isEmpty()) {
                System.setProperty("weasis.languages", this.modulesi18n.getProperty("languages", ""));
                System.setProperty("weasis.languages.percentage", this.modulesi18n.getProperty("percentages", ""));
            }
        }
        catch (Exception e) {
            LOGGER.error("Cannot load translation modules", e);
        }
    }

    public static Locale textToLocale(String value) {
        if (!Utils.hasText(value)) {
            return Locale.ENGLISH;
        }
        if (!"system".equals(value)) {
            return Locale.forLanguageTag(value.replace("_", "-"));
        }
        return Locale.getDefault();
    }

    private void handleOpenURI(OpenURIEvent e, long time, boolean noArgs) {
        Object uri = e.getURI().toString();
        LOGGER.info("Get URI event from OS. URI: {}", uri);
        int index = Utils.getWeasisProtocolIndex(new String[]{uri});
        if (index < 0) {
            uri = "dicom:get -r \"" + (String)uri + "\"";
            this.executeCommandsWhenLoaded(List.of(uri));
        } else {
            boolean sameInstance = System.currentTimeMillis() - time < 3000L;
            String[] args = WeasisLauncher.getArgsForURI((String)uri);
            Thread.ofVirtual().start(() -> this.launchProcess(args, sameInstance, noArgs));
        }
    }

    private static String[] getArgsForURI(String uri) {
        if (SystemInfo.isMacOS) {
            return new String[]{"open", "-n", "-b", "org.weasis.launcher", "--args", uri};
        }
        if (SystemInfo.isWindows) {
            return new String[]{"cmd", "/c", "start", uri};
        }
        return new String[]{"xdg-open", uri};
    }

    private void launchProcess(String[] args, boolean sameInstance, boolean noArgs) {
        block3: {
            try {
                ProcessBuilder pb = new ProcessBuilder(args);
                pb.start().waitFor(15L, TimeUnit.SECONDS);
                if (sameInstance && noArgs) {
                    LOGGER.info("Configuration with URI is different, restart Weasis with new configuration");
                    this.shutdownHook();
                }
            }
            catch (Exception ex) {
                LOGGER.error("Cannot start Weasis from URI", ex);
                if (!(ex instanceof InterruptedException)) break block3;
                Thread.currentThread().interrupt();
            }
        }
    }

    private void handleOpenFile(OpenFilesEvent e) {
        List files = e.getFiles().stream().map(f -> "dicom:get -l \"" + f.getPath() + "\"").toList();
        LOGGER.info("Get open file event from OS. Files: {}", (Object)files);
        this.executeCommandsWhenLoaded(files);
    }

    protected void shutdownHook() {
        this.closing = true;
        if (this.mFelix == null || (this.mFelix.getState() & 6) != 0) {
            return;
        }
        LOGGER.info("Stop the OSGI framework and clean files before closing");
        try {
            this.mFelix.stop();
            this.mFelix.waitForStop(30000L);
        }
        catch (Exception ex) {
            LOGGER.error("Error stopping framework", ex);
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
        finally {
            WeasisLauncher.cleanImageCache();
            this.stopSingletonServer();
            this.cleanOldFolders();
            Timer timer = new Timer();
            timer.schedule((TimerTask)new HaltTask(), 15000L);
        }
    }

    private void cleanOldFolders() {
        Path path;
        String dir = System.getProperty("weasis.path");
        if (Utils.hasText(dir) && Files.isDirectory(path = Paths.get(dir, new String[0]), new LinkOption[0])) {
            try {
                List<Path> folders = this.listOldFolders(path);
                folders.forEach(p -> {
                    LOGGER.error("Delete old folder: {}", p);
                    FileUtil.delete(p);
                    Optional<String> id = this.getID(p.getFileName().toString());
                    if (id.isPresent()) {
                        Path data;
                        Path file = Paths.get(dir, id.get() + ".properties");
                        if (Files.isReadable(file)) {
                            FileUtil.delete(file);
                        }
                        if (Files.isReadable(data = Paths.get(dir, "data", id.get()))) {
                            FileUtil.delete(data);
                        }
                    }
                });
            }
            catch (IOException e) {
                LOGGER.error("Cannot clean old folders", e);
            }
        }
    }

    private List<Path> listOldFolders(Path dir) throws IOException {
        long days = Math.max(Long.parseLong(System.getProperty("weasis.clean.old.version.days", "100")), 30L);
        try (Stream<Path> stream = Files.list(dir);){
            List list = stream.filter(path -> Files.isDirectory(path, new LinkOption[0]) && path.getFileName().toString().startsWith("cache-") && this.isOlderThan((Path)path, days)).toList();
            return list;
        }
    }

    private boolean isOlderThan(Path path, long days) {
        try {
            FileTime fileTime = Files.getLastModifiedTime(path, new LinkOption[0]);
            LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
            LocalDateTime convertedFileTime = LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault());
            long daysBetween = ChronoUnit.DAYS.between(convertedFileTime, now);
            return daysBetween > days;
        }
        catch (Exception e) {
            LOGGER.error("Cannot get the last modified time", e);
            return false;
        }
    }

    public Optional<String> getID(String filename) {
        return Optional.ofNullable(filename).filter(f -> f.contains("-")).map(f -> f.substring(filename.lastIndexOf("-") + 1));
    }

    protected void stopSingletonServer() {
    }

    static void cleanImageCache() {
        String dir = System.getProperty("weasis.tmp.dir");
        if (Utils.hasText(dir)) {
            long startTime = System.currentTimeMillis();
            FileUtil.deleteDirectoryContents(Path.of(dir, new String[0]), 3, 0);
            LOGGER.info("*PERF* Clean temp folder, type:CLOSE time:{}", (Object)(System.currentTimeMillis() - startTime));
        }
    }

    public static enum State {
        UNINSTALLED(1),
        INSTALLED(2),
        RESOLVED(4),
        STARTING(8),
        STOPPING(16),
        ACTIVE(32);

        private final int index;

        private State(int state) {
            this.index = state;
        }

        public static String valueOf(int state) {
            for (State s : State.values()) {
                if (s.index != state) continue;
                return s.name();
            }
            return "UNKNOWN";
        }
    }

    static class HaltTask
    extends TimerTask {
        HaltTask() {
        }

        @Override
        public void run() {
            LOGGER.info("Force to close the application");
            Runtime.getRuntime().halt(1);
        }
    }

    public static enum Type {
        DEFAULT,
        NATIVE;

    }
}
