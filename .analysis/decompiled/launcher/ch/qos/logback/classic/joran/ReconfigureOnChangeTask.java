/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.joran;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.joran.PropertiesConfigurator;
import ch.qos.logback.classic.joran.ReconfigureOnChangeTaskListener;
import ch.qos.logback.core.joran.spi.ConfigurationWatchList;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.ModelUtil;
import ch.qos.logback.core.spi.ConfigurationEvent;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.status.StatusUtil;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class ReconfigureOnChangeTask
extends ContextAwareBase
implements Runnable {
    public static final String DETECTED_CHANGE_IN_CONFIGURATION_FILES = "Detected change in configuration files.";
    public static final String RE_REGISTERING_PREVIOUS_SAFE_CONFIGURATION = "Re-registering previous fallback configuration once more as a fallback configuration point";
    public static final String FALLING_BACK_TO_SAFE_CONFIGURATION = "Given previous errors, falling back to previously registered safe configuration.";
    long birthdate = System.currentTimeMillis();
    List<ReconfigureOnChangeTaskListener> listeners = null;
    ScheduledFuture<?> scheduledFuture;

    @Override
    public void run() {
        this.context.fireConfigurationEvent(ConfigurationEvent.newConfigurationChangeDetectorRunningEvent(this));
        ConfigurationWatchList configurationWatchList = ConfigurationWatchListUtil.getConfigurationWatchList(this.context);
        if (configurationWatchList == null) {
            this.addWarn("Empty ConfigurationWatchList in context");
            return;
        }
        if (configurationWatchList.emptyWatchLists()) {
            this.addInfo("Both watch lists are empty. Disabling ");
            return;
        }
        File changedFile = configurationWatchList.changeDetectedInFile();
        URL changedURL = configurationWatchList.changeDetectedInURL();
        if (changedFile == null && changedURL == null) {
            return;
        }
        this.context.fireConfigurationEvent(ConfigurationEvent.newConfigurationChangeDetectedEvent(this));
        this.addInfo(DETECTED_CHANGE_IN_CONFIGURATION_FILES);
        if (changedFile != null) {
            this.changeInFile(changedFile, configurationWatchList);
        }
        if (changedURL != null) {
            this.changeInURL(changedURL);
        }
    }

    private void changeInURL(URL url) {
        String path = url.getPath();
        if (path.endsWith(".properties")) {
            this.runPropertiesConfigurator(url);
        }
    }

    private void changeInFile(File changedFile, ConfigurationWatchList configurationWatchList) {
        if (changedFile.getName().endsWith(".properties")) {
            this.runPropertiesConfigurator(changedFile);
            return;
        }
        this.addInfo("Will reset and reconfigure context named [" + this.context.getName() + "]");
        this.cancelFutureInvocationsOfThisTaskInstance();
        URL mainConfigurationURL = configurationWatchList.getTopURL();
        LoggerContext lc = (LoggerContext)this.context;
        if (mainConfigurationURL.toString().endsWith("xml")) {
            this.performXMLConfiguration(lc, mainConfigurationURL);
        }
    }

    private void runPropertiesConfigurator(Object changedObject) {
        this.addInfo("Will run PropertyConfigurator on " + String.valueOf(changedObject));
        PropertiesConfigurator propertiesConfigurator = new PropertiesConfigurator();
        propertiesConfigurator.setContext(this.context);
        try {
            if (changedObject instanceof File) {
                File changedFile = (File)changedObject;
                propertiesConfigurator.doConfigure(changedFile);
            } else if (changedObject instanceof URL) {
                URL changedURL = (URL)changedObject;
                propertiesConfigurator.doConfigure(changedURL);
            }
            this.context.fireConfigurationEvent(ConfigurationEvent.newPartialConfigurationEndedSuccessfullyEvent(this));
        }
        catch (JoranException e) {
            this.addError("Failed to reload " + String.valueOf(changedObject));
        }
    }

    private void cancelFutureInvocationsOfThisTaskInstance() {
        boolean result = this.scheduledFuture.cancel(false);
        if (!result) {
            this.addWarn("could not cancel " + this.toString());
        }
    }

    private void performXMLConfiguration(LoggerContext loggerContext, URL mainConfigurationURL) {
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(loggerContext);
        StatusUtil statusUtil = new StatusUtil(loggerContext);
        Model failsafeTop = jc.recallSafeConfiguration();
        URL topURL = ConfigurationWatchListUtil.getMainWatchURL(this.context);
        this.addInfo("Resetting loggerContext [" + loggerContext.getName() + "]");
        loggerContext.reset();
        long threshold = System.currentTimeMillis();
        try {
            jc.doConfigure(mainConfigurationURL);
            if (statusUtil.hasXMLParsingErrors(threshold)) {
                this.fallbackConfiguration(loggerContext, failsafeTop, topURL);
            }
        }
        catch (JoranException e) {
            this.addWarn("Exception occurred during reconfiguration", e);
            this.fallbackConfiguration(loggerContext, failsafeTop, topURL);
        }
    }

    private void fallbackConfiguration(LoggerContext loggerContext, Model failsafeTopModel, URL topURL) {
        JoranConfigurator joranConfigurator = new JoranConfigurator();
        joranConfigurator.setContext(loggerContext);
        joranConfigurator.setTopURL(topURL);
        if (failsafeTopModel == null) {
            this.addWarn("No previous configuration to fall back on.");
            return;
        }
        this.addWarn(FALLING_BACK_TO_SAFE_CONFIGURATION);
        this.addInfo("Safe model " + String.valueOf(failsafeTopModel));
        try {
            loggerContext.reset();
            ModelUtil.resetForReuse(failsafeTopModel);
            joranConfigurator.processModel(failsafeTopModel);
            this.addInfo(RE_REGISTERING_PREVIOUS_SAFE_CONFIGURATION);
            joranConfigurator.registerSafeConfiguration(failsafeTopModel);
            this.context.fireConfigurationEvent(ConfigurationEvent.newConfigurationEndedSuccessfullyEvent(this));
        }
        catch (Exception e) {
            this.addError("Unexpected exception thrown by a configuration considered safe.", e);
        }
    }

    public String toString() {
        return "ReconfigureOnChangeTask(born:" + this.birthdate + ")";
    }

    @Deprecated
    public void setScheduredFuture(ScheduledFuture<?> aScheduledFuture) {
        this.setScheduledFuture(aScheduledFuture);
    }

    public void setScheduledFuture(ScheduledFuture<?> aScheduledFuture) {
        this.scheduledFuture = aScheduledFuture;
    }
}

