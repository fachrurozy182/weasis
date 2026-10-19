/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  bibliothek.gui.DockStation
 *  bibliothek.gui.Dockable
 *  bibliothek.gui.dock.DockElement
 *  bibliothek.gui.dock.StackDockStation
 *  bibliothek.gui.dock.common.CContentArea
 *  bibliothek.gui.dock.common.CControl
 *  bibliothek.gui.dock.common.CWorkingArea
 *  bibliothek.gui.dock.common.DefaultSingleCDockable
 *  bibliothek.gui.dock.common.event.CVetoFocusListener
 *  bibliothek.gui.dock.common.intern.CDockable
 *  bibliothek.gui.dock.common.intern.CommonDockable
 *  bibliothek.gui.dock.event.KeyboardListener
 *  org.osgi.framework.BundleContext
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.PropertiesUtil
 *  org.weasis.core.util.StringUtil
 *  org.weasis.pref.ConfigData
 */
package org.weasis.core.api.service;

import bibliothek.gui.DockStation;
import bibliothek.gui.Dockable;
import bibliothek.gui.dock.DockElement;
import bibliothek.gui.dock.StackDockStation;
import bibliothek.gui.dock.common.CContentArea;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CWorkingArea;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.event.CVetoFocusListener;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.intern.CommonDockable;
import bibliothek.gui.dock.event.KeyboardListener;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.DataExplorerView;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.gui.util.ShortcutManager;
import org.weasis.core.api.gui.util.WinUtil;
import org.weasis.core.api.media.data.Codec;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.net.ClosableURLConnection;
import org.weasis.core.api.net.NetworkUtil;
import org.weasis.core.api.net.URLParameters;
import org.weasis.core.api.service.WProperties;
import org.weasis.core.api.util.LocalUtil;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.editor.SeriesViewer;
import org.weasis.core.ui.editor.SeriesViewerFactory;
import org.weasis.core.ui.editor.image.ViewerPlugin;
import org.weasis.core.ui.launcher.Launcher;
import org.weasis.core.ui.util.ToolBarContainer;
import org.weasis.core.ui.util.Toolbar;
import org.weasis.core.util.PropertiesUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.pref.ConfigData;

public final class UICore {
    public static final String P_FORMAT_CODE = "locale.format.code";
    public static final String CONFIRM_CLOSE = "weasis.confirm.closing";
    public static final String CONFIRM_DELETE_MEASUREMENT = "weasis.confirm.delete.measurement";
    public static final String LINUX_WINDOWS_DECORATION = "weasis.linux.windows.decoration";
    public static final String USE_SYSTEM_FILE_CHOOSER = "weasis.use.system.file.chooser";
    private static final Logger LOGGER = LoggerFactory.getLogger(UICore.class);
    private final ToolBarContainer toolbarContainer;
    private final List<Launcher> dicomLaunchers;
    private final List<Launcher> otherLaunchers;
    public final List<ViewerPlugin<?>> viewerPlugins;
    private final List<DataExplorerView> explorerPlugins;
    private final List<Toolbar> explorerPluginToolbars;
    private final List<SeriesViewerFactory> seriesViewerFactories;
    private final CVetoFocusListener dockingVetoFocus;
    private final CControl dockingControl = new CControl();
    private final CContentArea baseArea = GraphicsEnvironment.isHeadless() ? null : this.dockingControl.getContentArea();
    private final CWorkingArea mainArea = this.dockingControl.createWorkingArea("mainArea");
    private final List<Codec<MediaElement>> codecPlugins;
    private final WProperties systemPreferences;
    private final WProperties localPersistence;
    private final WProperties initialSystemPreferences;
    private final ConfigData configData;
    private final HashMap<String, WProperties> pluginPersistenceMap;
    private final File propsFile;

    private UICore() {
        this.toolbarContainer = new ToolBarContainer();
        this.configData = UICore.retrieveconfigData();
        this.initialSystemPreferences = new WProperties();
        this.systemPreferences = new WProperties();
        this.pluginPersistenceMap = new HashMap();
        this.codecPlugins = Collections.synchronizedList(new ArrayList());
        this.localPersistence = new WProperties();
        this.viewerPlugins = Collections.synchronizedList(new ArrayList());
        this.explorerPlugins = Collections.synchronizedList(new ArrayList());
        this.explorerPluginToolbars = Collections.synchronizedList(new ArrayList());
        this.seriesViewerFactories = Collections.synchronizedList(new ArrayList());
        this.dockingVetoFocus = new CVetoFocusListener(this){
            {
                Objects.requireNonNull(this$0);
            }

            public boolean willLoseFocus(CDockable dockable) {
                return false;
            }

            public boolean willGainFocus(CDockable dockable) {
                return false;
            }
        };
        this.dockingControl.getController().getKeyboardController().addListener((KeyboardListener)new StackTabSwitcher());
        BundleContext context = AppProperties.getBundleContext();
        this.readSystemPreferences(context);
        this.propsFile = new File(this.systemPreferences.getProperty("weasis.pref.dir"), "weasis.properties");
        if (!this.propsFile.canRead()) {
            try {
                if (!this.propsFile.createNewFile()) {
                    LOGGER.warn("File already exist {}", (Object)this.propsFile.getPath());
                }
            }
            catch (IOException e) {
                LOGGER.error("Cannot write {}", (Object)this.propsFile.getPath(), (Object)e);
            }
        }
        String code = this.systemPreferences.getProperty(P_FORMAT_CODE);
        Locale.setDefault(Locale.Category.FORMAT, LocalUtil.textToLocale(code));
        String path = this.systemPreferences.getProperty("weasis.resources.path");
        ResourceUtil.setResourcePath(path);
        Path dataFolder = AppProperties.getBundleDataFolder(context);
        if (dataFolder != null) {
            PropertiesUtil.loadProperties((Path)dataFolder.resolve("persistence.properties"), (Properties)this.localPersistence);
        }
        this.dicomLaunchers = Launcher.loadLaunchers(Launcher.Type.DICOM);
        this.otherLaunchers = Launcher.loadLaunchers(Launcher.Type.OTHER);
    }

    private static ConfigData retrieveconfigData() {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName objectName = ObjectName.getInstance("weasis:name=MainWindow");
            Object preferences = server.getAttribute(objectName, "ConfigData");
            if (preferences instanceof ConfigData) {
                ConfigData configData = (ConfigData)preferences;
                return configData;
            }
        }
        catch (InstanceNotFoundException objectName) {
        }
        catch (JMException e) {
            LOGGER.debug("Error while receiving main window", (Throwable)e);
        }
        throw new IllegalStateException("Cannot retrieve ConfigData");
    }

    public static UICore getInstance() {
        return Holder.INSTANCE;
    }

    private void readSystemPreferences(BundleContext context) {
        String pkeys;
        this.systemPreferences.clear();
        if (context != null && StringUtil.hasText((CharSequence)(pkeys = context.getProperty("wp.list")))) {
            for (String key : pkeys.split(",")) {
                this.systemPreferences.setProperty(key, context.getProperty(key));
                this.initialSystemPreferences.setProperty(key, context.getProperty("wp.init." + key));
            }
            String diffRemote = "wp.init.diff.remote.pref";
            this.initialSystemPreferences.setProperty(diffRemote, context.getProperty(diffRemote));
            this.saveSystemPreferences();
        }
    }

    private void storeLauncherPref(Properties props, String remotePrefURL) throws IOException {
        if (!this.isLocalSession() || this.isStoreLocalSession()) {
            String sURL = String.format("%s?user=%s&profile=%s", remotePrefURL, UICore.getUrlEncoding(AppProperties.WEASIS_USER), UICore.getUrlEncoding(AppProperties.WEASIS_PROFILE));
            URLParameters urlParameters = UICore.getURLParameters(true);
            ClosableURLConnection http = NetworkUtil.getUrlConnection(sURL, urlParameters);
            try (OutputStream out = http.getOutputStream();){
                props.store(new DataOutputStream(out), null);
            }
            URLConnection uRLConnection = http.urlConnection();
            if (uRLConnection instanceof HttpURLConnection) {
                HttpURLConnection httpURLConnection = (HttpURLConnection)uRLConnection;
                NetworkUtil.readResponse(httpURLConnection, urlParameters.headers());
            }
        }
    }

    public void storeRemotePref(Path filePath, String contentType) {
        block10: {
            String remotePrefURL = this.getPrefServiceUrl();
            if (remotePrefURL != null) {
                try {
                    if (this.isLocalSession() && !this.isStoreLocalSession()) break block10;
                    String sURL = String.format("%s?user=%s&profile=%s", remotePrefURL, UICore.getUrlEncoding(AppProperties.WEASIS_USER), UICore.getUrlEncoding(AppProperties.WEASIS_PROFILE));
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("Content-Type", contentType);
                    URLParameters urlParameters = new URLParameters(map, true);
                    ClosableURLConnection http = NetworkUtil.getUrlConnection(sURL, urlParameters);
                    try (OutputStream out = http.getOutputStream();){
                        Files.copy(filePath, out);
                    }
                    URLConnection uRLConnection = http.urlConnection();
                    if (uRLConnection instanceof HttpURLConnection) {
                        HttpURLConnection httpURLConnection = (HttpURLConnection)uRLConnection;
                        NetworkUtil.readResponse(httpURLConnection, urlParameters.headers());
                    }
                }
                catch (Exception e) {
                    LOGGER.error("Cannot store remote preference for user: {}", (Object)AppProperties.WEASIS_USER, (Object)e);
                }
            }
        }
    }

    public static String getUrlEncoding(String val) {
        return URLEncoder.encode(val, StandardCharsets.UTF_8);
    }

    private static URLParameters getURLParameters(boolean post) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(post ? "Content-Type" : "Accept", "text/x-java-properties");
        return new URLParameters(map, post);
    }

    public String getPrefServiceUrl() {
        return this.systemPreferences.getProperty("weasis.pref.url");
    }

    public String getConfigServiceUrl() {
        return this.systemPreferences.getProperty("weasis.config.url");
    }

    public String getStatisticServiceUrl() {
        return this.systemPreferences.getProperty("weasis.stat.url");
    }

    public boolean isLocalSession() {
        return this.systemPreferences.getBooleanProperty("weasis.pref.local.session", false);
    }

    public boolean isStoreLocalSession() {
        return this.systemPreferences.getBooleanProperty("weasis.pref.store.local.session", false);
    }

    public synchronized void saveSystemPreferences() {
        String key = "weasis.accept.disclaimer";
        this.systemPreferences.setProperty(key, System.getProperty(key));
        key = "weasis.version.release";
        this.systemPreferences.setProperty(key, System.getProperty(key));
        if (!this.systemPreferences.equals(this.initialSystemPreferences)) {
            if (this.propsFile != null) {
                PropertiesUtil.storeProperties((Path)this.propsFile.toPath(), (Properties)this.systemPreferences, null);
                String remotePrefURL = this.getPrefServiceUrl();
                if (remotePrefURL != null) {
                    try {
                        this.storeLauncherPref(this.systemPreferences, remotePrefURL);
                    }
                    catch (Exception e) {
                        LOGGER.error("Cannot store Launcher preference for user: {}", (Object)AppProperties.WEASIS_USER, (Object)e);
                    }
                }
            }
            this.initialSystemPreferences.clear();
            this.initialSystemPreferences.putAll((Map<?, ?>)this.systemPreferences);
        }
    }

    public CControl getDockingControl() {
        return this.dockingControl;
    }

    public List<Launcher> getDicomLaunchers() {
        return this.dicomLaunchers;
    }

    public List<Launcher> getOtherLaunchers() {
        return this.otherLaunchers;
    }

    public CWorkingArea getMainArea() {
        return this.mainArea;
    }

    public CContentArea getBaseArea() {
        return this.baseArea;
    }

    public Window getApplicationWindow() {
        return WinUtil.getParentWindow((Component)this.baseArea);
    }

    public List<DataExplorerView> getExplorerPlugins() {
        return this.explorerPlugins;
    }

    public ToolBarContainer getToolbarContainer() {
        return this.toolbarContainer;
    }

    public List<ViewerPlugin<?>> getViewerPlugins() {
        return this.viewerPlugins;
    }

    public List<Toolbar> getExplorerPluginToolbars() {
        return this.explorerPluginToolbars;
    }

    public List<SeriesViewerFactory> getSeriesViewerFactories() {
        return this.seriesViewerFactories;
    }

    public CVetoFocusListener getDockingVetoFocus() {
        return this.dockingVetoFocus;
    }

    public List<Codec<MediaElement>> getCodecPlugins() {
        return this.codecPlugins;
    }

    public ConfigData getConfigData() {
        return this.configData;
    }

    public WProperties getSystemPreferences() {
        return this.systemPreferences;
    }

    public WProperties getLocalPersistence() {
        return this.localPersistence;
    }

    public WProperties getPluginPersistence(String key) {
        return this.pluginPersistenceMap.computeIfAbsent(key, k -> new WProperties());
    }

    public File getPropsFile() {
        return this.propsFile;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public DataExplorerView getExplorerPlugin(String name) {
        if (name != null) {
            List<DataExplorerView> list = this.explorerPlugins;
            synchronized (list) {
                for (DataExplorerView view : this.explorerPlugins) {
                    if (!name.equals(view.getUIName())) continue;
                    return view;
                }
            }
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SeriesViewerFactory getViewerFactory(Class<? extends SeriesViewerFactory> clazz) {
        if (clazz != null) {
            List<SeriesViewerFactory> list = this.seriesViewerFactories;
            synchronized (list) {
                for (SeriesViewerFactory factory : this.seriesViewerFactories) {
                    if (!clazz.isInstance(factory)) continue;
                    return factory;
                }
            }
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SeriesViewerFactory getViewerFactory(SeriesViewer seriesViewer) {
        if (seriesViewer != null) {
            List<SeriesViewerFactory> list = this.seriesViewerFactories;
            synchronized (list) {
                for (SeriesViewerFactory factory : this.seriesViewerFactories) {
                    if (factory == null || !factory.isViewerCreatedByThisFactory(seriesViewer)) continue;
                    return factory;
                }
            }
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SeriesViewerFactory getViewerFactory(String mimeType) {
        if (mimeType != null) {
            List<SeriesViewerFactory> list = this.seriesViewerFactories;
            synchronized (list) {
                int level = Integer.MAX_VALUE;
                SeriesViewerFactory best = null;
                for (SeriesViewerFactory f : this.seriesViewerFactories) {
                    if (f == null || !f.canReadMimeType(mimeType) || f.getLevel() >= level) continue;
                    level = f.getLevel();
                    best = f;
                }
                return best;
            }
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SeriesViewerFactory getViewerFactory(String[] mimeTypeList) {
        if (mimeTypeList != null && mimeTypeList.length > 0) {
            List<SeriesViewerFactory> list = this.seriesViewerFactories;
            synchronized (list) {
                int level = Integer.MAX_VALUE;
                SeriesViewerFactory best = null;
                for (SeriesViewerFactory f : this.seriesViewerFactories) {
                    if (f == null) continue;
                    for (String mime : mimeTypeList) {
                        if (!f.canReadMimeType(mime) || f.getLevel() >= level) continue;
                        best = f;
                    }
                }
                return best;
            }
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public List<SeriesViewerFactory> getViewerFactoryList(String[] mimeTypeList) {
        if (mimeTypeList != null && mimeTypeList.length > 0) {
            ArrayList<SeriesViewerFactory> plugins = new ArrayList<SeriesViewerFactory>();
            List<SeriesViewerFactory> list = this.seriesViewerFactories;
            synchronized (list) {
                for (SeriesViewerFactory viewerFactory : this.seriesViewerFactories) {
                    if (viewerFactory == null) continue;
                    for (String mime : mimeTypeList) {
                        if (!viewerFactory.canReadMimeType(mime)) continue;
                        plugins.add(viewerFactory);
                    }
                }
            }
            plugins.sort(Comparator.comparingInt(SeriesViewerFactory::getLevel));
            return plugins;
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean isSeriesOpenInViewer(MediaSeries<?> mediaSeries) {
        if (mediaSeries == null) {
            return false;
        }
        List<ViewerPlugin<?>> list = this.viewerPlugins;
        synchronized (list) {
            for (ViewerPlugin<?> plugin : this.viewerPlugins) {
                List openSeries = plugin.getOpenSeries();
                if (openSeries == null) continue;
                for (MediaSeries s : openSeries) {
                    if (mediaSeries != s) continue;
                    return true;
                }
            }
        }
        return false;
    }

    public void closeSeries(MediaSeries<?> mediaSeries) {
        if (mediaSeries == null) {
            return;
        }
        mediaSeries.setOpen(this.isSeriesOpenInViewer(mediaSeries));
        mediaSeries.setSelected(false, null);
        mediaSeries.setFocused(false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void closeSeriesViewerType(Class<? extends SeriesViewer<?>> clazz) {
        ArrayList pluginsToRemove = new ArrayList();
        List<ViewerPlugin<?>> list = this.viewerPlugins;
        synchronized (list) {
            for (ViewerPlugin<?> plugin : this.viewerPlugins) {
                if (!clazz.isInstance(plugin)) continue;
                pluginsToRemove.add(plugin);
            }
        }
        this.closeSeriesViewer(pluginsToRemove);
    }

    public void closeSeriesViewer(List<? extends ViewerPlugin<?>> pluginsToRemove) {
        if (pluginsToRemove != null) {
            GuiExecutor.execute(() -> {
                for (ViewerPlugin viewerPlugin : pluginsToRemove) {
                    viewerPlugin.close();
                    viewerPlugin.handleFocusAfterClosing();
                }
            });
        }
    }

    public static final class StackTabSwitcher
    implements KeyboardListener {
        public DockElement getTreeLocation() {
            return null;
        }

        public boolean keyPressed(DockElement element, KeyEvent event) {
            ShortcutManager sm = ShortcutManager.getInstance();
            if (sm.matches("docking.nextTab", event)) {
                return this.shift(element, 1);
            }
            if (sm.matches("docking.prevTab", event)) {
                return this.shift(element, -1);
            }
            return false;
        }

        public boolean keyReleased(DockElement element, KeyEvent event) {
            return false;
        }

        public boolean keyTyped(DockElement element, KeyEvent event) {
            return false;
        }

        private boolean shift(DockElement element, int delta) {
            StackDockStation stack = StackTabSwitcher.resolveStackDockStation(element);
            if (stack == null) {
                return false;
            }
            Dockable focused = stack.getFrontDockable();
            int index = -1;
            int n = stack.getDockableCount();
            for (int i = 0; i < n; ++i) {
                if (stack.getDockable(i) != focused) continue;
                index = i;
                break;
            }
            if (index != -1) {
                CommonDockable commonDockable;
                CDockable cDockable;
                index = Math.floorMod(index + delta, stack.getDockableCount());
                Dockable next = stack.getDockable(index);
                stack.getController().setFocusedDockable(next, true);
                if (next instanceof CommonDockable && (cDockable = (commonDockable = (CommonDockable)next).getDockable()) instanceof DefaultSingleCDockable) {
                    DefaultSingleCDockable single = (DefaultSingleCDockable)cDockable;
                    single.toFront();
                }
                return true;
            }
            return false;
        }

        private static StackDockStation resolveStackDockStation(DockElement element) {
            DockStation dockStation;
            DockStation asStation = element.asDockStation();
            if (asStation instanceof StackDockStation) {
                StackDockStation stack = (StackDockStation)asStation;
                return stack;
            }
            if (asStation == null) {
                DefaultSingleCDockable single;
                DockStation dockStation2;
                CommonDockable commonDockable;
                CDockable cDockable;
                if (element instanceof CommonDockable && (cDockable = (commonDockable = (CommonDockable)element).getDockable()) instanceof DefaultSingleCDockable && (dockStation2 = (single = (DefaultSingleCDockable)cDockable).intern().getDockParent()) instanceof StackDockStation) {
                    StackDockStation stack = (StackDockStation)dockStation2;
                    return stack;
                }
                return null;
            }
            DockStation container = asStation;
            Dockable front = container.getFrontDockable();
            if (front != null && (dockStation = front.asDockStation()) instanceof StackDockStation) {
                StackDockStation stack = (StackDockStation)dockStation;
                return stack;
            }
            return null;
        }
    }

    private static final class Holder {
        private static final UICore INSTANCE = new UICore();

        private Holder() {
        }
    }
}

