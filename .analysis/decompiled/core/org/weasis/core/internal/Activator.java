/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  ch.qos.logback.classic.Level
 *  ch.qos.logback.classic.Logger
 *  ch.qos.logback.classic.LoggerContext
 *  ch.qos.logback.classic.encoder.PatternLayoutEncoder
 *  ch.qos.logback.classic.spi.ILoggingEvent
 *  ch.qos.logback.core.rolling.RollingFileAppender
 *  org.apache.felix.prefs.BackingStore
 *  org.osgi.annotation.bundle.Header
 *  org.osgi.framework.BundleActivator
 *  org.osgi.framework.BundleContext
 *  org.osgi.framework.InvalidSyntaxException
 *  org.osgi.framework.ServiceEvent
 *  org.osgi.framework.ServiceListener
 *  org.osgi.framework.ServiceReference
 *  org.osgi.service.prefs.Preferences
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.FileUtil
 *  org.weasis.core.util.PropertiesUtil
 */
package org.weasis.core.internal;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import java.io.File;
import java.nio.file.Path;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import org.apache.felix.prefs.BackingStore;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.model.AbstractFileModel;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.ShortcutManager;
import org.weasis.core.api.media.data.Codec;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.service.AuditLog;
import org.weasis.core.api.service.BundlePreferences;
import org.weasis.core.api.service.BundleTools;
import org.weasis.core.api.service.WProperties;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.internal.StreamBackingStoreImpl;
import org.weasis.core.ui.editor.FileModel;
import org.weasis.core.ui.editor.SeriesViewerFactory;
import org.weasis.core.ui.editor.ViewerPluginBuilder;
import org.weasis.core.ui.editor.image.dockable.MeasureTool;
import org.weasis.core.ui.model.layer.AbstractInfoLayer;
import org.weasis.core.util.FileUtil;
import org.weasis.core.util.PropertiesUtil;

@Header(name="Bundle-Activator", value="${@class}")
public class Activator
implements BundleActivator,
ServiceListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    public void start(BundleContext bundleContext) throws Exception {
        WProperties properties = GuiUtils.getUICore().getSystemPreferences();
        bundleContext.registerService(BackingStore.class.getName(), (Object)new StreamBackingStoreImpl(bundleContext, properties.getProperty("weasis.pref.dir")), null);
        for (ServiceReference service : bundleContext.getServiceReferences(Codec.class, null)) {
            Activator.registerCodecPlugins((Codec)bundleContext.getService(service));
        }
        bundleContext.addServiceListener((ServiceListener)this, BundleTools.createServiceFilter(Codec.class));
        Activator.initLoggerAndAudit(properties);
        File file = ResourceUtil.getResource("presets.xml");
        if (file.canRead()) {
            System.setProperty("dicom.presets.path", file.getPath());
        }
        Activator.registerCommands(bundleContext);
        Preferences prefs = BundlePreferences.getDefaultPreferences(bundleContext);
        AbstractInfoLayer.applyPreferences(prefs);
        MeasureTool.viewSetting.initMonitors();
        MeasureTool.viewSetting.applyPreferences(prefs);
        ShortcutManager shortcutManager = ShortcutManager.getInstance();
        shortcutManager.registerDefaults();
        shortcutManager.loadPreferences(prefs);
        shortcutManager.applyToFeatures();
        GuiExecutor.execute(() -> {
            try {
                for (ServiceReference service : bundleContext.getServiceReferences(SeriesViewerFactory.class, null)) {
                    Activator.registerSeriesViewerFactory((SeriesViewerFactory)bundleContext.getService(service));
                }
            }
            catch (InvalidSyntaxException e) {
                LOGGER.error("", (Throwable)e);
            }
        });
        bundleContext.addServiceListener((ServiceListener)this, BundleTools.createServiceFilter(Codec.class, SeriesViewerFactory.class));
    }

    public void stop(BundleContext bundleContext) throws Exception {
        GuiUtils.getUICore().saveSystemPreferences();
        Preferences prefs = BundlePreferences.getDefaultPreferences(bundleContext);
        AbstractInfoLayer.savePreferences(prefs);
        MeasureTool.viewSetting.savePreferences(prefs);
        ShortcutManager.getInstance().savePreferences(prefs);
        prefs.sync();
        Path dataFolder = AppProperties.getBundleDataFolder(bundleContext);
        if (dataFolder != null) {
            Path filePath = dataFolder.resolve("persistence.properties");
            FileUtil.prepareToWriteFile((Path)filePath);
            PropertiesUtil.storeProperties((Path)filePath, (Properties)GuiUtils.getUICore().getLocalPersistence(), null);
        }
    }

    public synchronized void serviceChanged(ServiceEvent event) {
        ServiceReference sRef = event.getServiceReference();
        BundleContext context = AppProperties.getBundleContext(sRef);
        if (context == null) {
            return;
        }
        Object service = null;
        try {
            service = context.getService(sRef);
        }
        catch (RuntimeException e) {
            LOGGER.error("Cannot get service of {}", (Object)sRef.getBundle(), (Object)e);
        }
        if (service == null) {
            return;
        }
        if (service instanceof Codec) {
            Codec codec = (Codec)service;
            if (event.getType() == 1) {
                Activator.registerCodecPlugins(codec);
            } else if (event.getType() == 4) {
                List<Codec<MediaElement>> codecs = GuiUtils.getUICore().getCodecPlugins();
                if (codecs.contains(codec)) {
                    LOGGER.info("Unregister Image Codec Plug-in: {}", (Object)codec.getCodecName());
                    codecs.remove(codec);
                }
                context.ungetService(sRef);
            }
        } else if (service instanceof SeriesViewerFactory) {
            SeriesViewerFactory viewerFactory = (SeriesViewerFactory)service;
            GuiExecutor.execute(() -> {
                if (event.getType() == 1) {
                    Activator.registerSeriesViewerFactory(viewerFactory);
                } else if (event.getType() == 4) {
                    List<SeriesViewerFactory> viewerFactories = GuiUtils.getUICore().getSeriesViewerFactories();
                    if (viewerFactories.contains(viewerFactory)) {
                        LOGGER.info("Unregister series viewer plug-in: {}", (Object)viewerFactory.getDescription());
                        viewerFactories.remove(viewerFactory);
                    }
                    context.ungetService(sRef);
                }
            });
        }
    }

    private static void registerCodecPlugins(Codec<?> codec) {
        List<Codec<MediaElement>> codecs = GuiUtils.getUICore().getCodecPlugins();
        if (codec != null && !codecs.contains(codec)) {
            codecs.add(codec);
            LOGGER.info("Register Image Codec Plug-in: {}", (Object)codec.getCodecName());
        }
    }

    private static void registerSeriesViewerFactory(SeriesViewerFactory factory) {
        List<SeriesViewerFactory> viewerFactories = GuiUtils.getUICore().getSeriesViewerFactories();
        if (factory != null && !viewerFactories.contains(factory)) {
            viewerFactories.add(factory);
            LOGGER.info("Register series viewer plug-in: {}", (Object)factory.getDescription());
        }
    }

    private static void registerCommands(BundleContext context) {
        Hashtable<String, Object> dict = new Hashtable<String, Object>();
        ((Dictionary)dict).put("osgi.command.scope", "image");
        ((Dictionary)dict).put("osgi.command.function", AbstractFileModel.functions.toArray(new String[0]));
        context.registerService(FileModel.class.getName(), (Object)ViewerPluginBuilder.DefaultDataModel, dict);
    }

    private static void initLoggerAndAudit(WProperties properties) {
        WProperties prefs = GuiUtils.getUICore().getSystemPreferences();
        LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
        loggerContext.reset();
        AuditLog.applyConfig(prefs, loggerContext);
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger((String)"ROOT");
        String loggerKey = "audit.log";
        if (properties.getBooleanProperty(loggerKey, false)) {
            String pattern = prefs.getProperty("org.apache.sling.commons.log.pattern", "%d{dd.MM.yyyy HH:mm:ss.SSS} *%-5level* %msg%ex{0}%nopex%n");
            PatternLayoutEncoder encoder = AuditLog.getPatternLayoutEncoder(loggerContext, pattern);
            RollingFileAppender<ILoggingEvent> rollingFileAppender = AuditLog.getRollingFilesAppender(logger, "AUDIT");
            WProperties p = AuditLog.getAuditProperties();
            AuditLog.updateRollingFilesAppender(rollingFileAppender, loggerContext, p, encoder);
            AuditLog.LOGGER.info("Start audit log session");
        } else {
            logger.detachAppender("AUDIT");
            Logger logger2 = AuditLog.LOGGER;
            if (logger2 instanceof ch.qos.logback.classic.Logger) {
                ch.qos.logback.classic.Logger auditLogger = (ch.qos.logback.classic.Logger)logger2;
                auditLogger.detachAndStopAllAppenders();
                auditLogger.setLevel(Level.OFF);
            }
        }
    }
}

