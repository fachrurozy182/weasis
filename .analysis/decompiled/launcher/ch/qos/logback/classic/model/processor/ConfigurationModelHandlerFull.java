/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.model.processor;

import ch.qos.logback.classic.joran.ReconfigureOnChangeTask;
import ch.qos.logback.classic.model.ConfigurationModel;
import ch.qos.logback.classic.model.processor.ConfigurationModelHandler;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.ConfigurationWatchList;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.spi.ConfigurationEvent;
import ch.qos.logback.core.util.Duration;
import ch.qos.logback.core.util.OptionHelper;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ConfigurationModelHandlerFull
extends ConfigurationModelHandler {
    public static final String FAILED_WATCH_PREDICATE_MESSAGE_1 = "Missing watchable .xml or .properties files.";
    public static final String FAILED_WATCH_PREDICATE_MESSAGE_2 = "Watching .xml files requires that the main configuration file is reachable as a URL";

    public ConfigurationModelHandlerFull(Context context) {
        super(context);
    }

    public static ModelHandlerBase makeInstance(Context context, ModelInterpretationContext mic) {
        return new ConfigurationModelHandlerFull(context);
    }

    @Override
    public void postHandle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        ConfigurationModel configurationModel = (ConfigurationModel)model;
        if (this.scanning != Boolean.TRUE) {
            return;
        }
        String scanPeriodStr = mic.subst(configurationModel.getScanPeriodStr());
        this.scheduleReconfigureOnChangeTask(scanPeriodStr);
        ConfigurationWatchList cwl = ConfigurationWatchListUtil.getConfigurationWatchList(this.getContext());
        if (cwl != null) {
            try {
                this.addInfo("Main configuration file URL: " + String.valueOf(cwl.getTopURL()));
                this.addInfo("FileWatchList= {" + cwl.getFileWatchListAsStr() + "}");
                this.addInfo("URLWatchList= {" + cwl.getUrlWatchListAsStr() + "}");
            }
            catch (NoSuchMethodError e) {
                this.addWarn("It looks like the version of logback-classic is more recent than");
                this.addWarn("the version of logback-core. Please align the two versions.");
            }
        }
    }

    public void detachedPostProcess(String scanStr, String scanPeriodStr) {
        if (OptionHelper.isNullOrEmptyOrAllSpaces(scanStr) || "false".equalsIgnoreCase(scanStr)) {
            return;
        }
        this.scheduleReconfigureOnChangeTask(scanPeriodStr);
    }

    public void detachedPostProcess(String scanPeriodStr) {
        this.scheduleReconfigureOnChangeTask(scanPeriodStr);
    }

    private void scheduleReconfigureOnChangeTask(String scanPeriodStr) {
        boolean watchPredicateFulfilled = ConfigurationWatchListUtil.watchPredicateFulfilled(this.context);
        if (!watchPredicateFulfilled) {
            this.addWarn(FAILED_WATCH_PREDICATE_MESSAGE_1);
            this.addWarn(FAILED_WATCH_PREDICATE_MESSAGE_2);
            return;
        }
        ReconfigureOnChangeTask rocTask = new ReconfigureOnChangeTask();
        rocTask.setContext(this.context);
        this.addInfo("Registering a new ReconfigureOnChangeTask " + String.valueOf(rocTask));
        this.context.fireConfigurationEvent(ConfigurationEvent.newConfigurationChangeDetectorRegisteredEvent(rocTask));
        Duration duration = this.getDurationOfScanPeriodAttribute(scanPeriodStr, SCAN_PERIOD_DEFAULT);
        ConfigurationWatchList cwl = ConfigurationWatchListUtil.getConfigurationWatchList(this.context);
        String fileWatchListAsStr = cwl != null ? cwl.getFileWatchListAsStr() : "";
        this.addInfo("Will scan for changes in [" + fileWatchListAsStr + "] ");
        this.addInfo("Setting ReconfigureOnChangeTask scanning period to " + String.valueOf(duration));
        ScheduledExecutorService scheduledExecutorService = this.context.getScheduledExecutorService();
        ScheduledFuture<?> scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(rocTask, duration.getMilliseconds(), duration.getMilliseconds(), TimeUnit.MILLISECONDS);
        rocTask.setScheduledFuture(scheduledFuture);
        this.context.addScheduledFuture(scheduledFuture);
    }

    private Duration getDurationOfScanPeriodAttribute(String scanPeriodAttrib, Duration defaultDuration) {
        Duration duration = null;
        if (!OptionHelper.isNullOrEmptyOrAllSpaces(scanPeriodAttrib)) {
            try {
                duration = Duration.valueOf(scanPeriodAttrib);
            }
            catch (IllegalArgumentException | IllegalStateException e) {
                this.addWarn("Failed to parse 'scanPeriod' attribute [" + scanPeriodAttrib + "]", e);
            }
        }
        if (duration == null) {
            this.addInfo("No 'scanPeriod' specified. Defaulting to " + defaultDuration.toString());
            duration = defaultDuration;
        }
        return duration;
    }
}

