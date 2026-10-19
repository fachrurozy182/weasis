/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.model.processor;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.model.ConfigurationModel;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.util.ContextUtil;
import ch.qos.logback.core.util.Duration;
import ch.qos.logback.core.util.OptionHelper;
import ch.qos.logback.core.util.StatusListenerConfigHelper;

public class ConfigurationModelHandler
extends ModelHandlerBase {
    static final Duration SCAN_PERIOD_DEFAULT = Duration.buildByMinutes(1.0);
    protected Boolean scanning = null;

    public ConfigurationModelHandler(Context context) {
        super(context);
    }

    public static ModelHandlerBase makeInstance(Context context, ModelInterpretationContext mic) {
        return new ConfigurationModelHandler(context);
    }

    protected Class<ConfigurationModel> getSupportedModelClass() {
        return ConfigurationModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) {
        ConfigurationModel configurationModel = (ConfigurationModel)model;
        String debugAttrib = OptionHelper.getSystemProperty("logback.debug", null);
        if (debugAttrib == null) {
            debugAttrib = mic.subst(configurationModel.getDebugStr());
        }
        if (!(OptionHelper.isNullOrEmptyOrAllSpaces(debugAttrib) || debugAttrib.equalsIgnoreCase(Boolean.FALSE.toString()) || debugAttrib.equalsIgnoreCase("null"))) {
            StatusListenerConfigHelper.addOnConsoleListenerInstance(this.context, new OnConsoleStatusListener());
        }
        this.scanning = this.scanAttrToBoolean(configurationModel);
        mic.setTopScanBoolean(this.scanning);
        this.printScanMessage(this.scanning);
        if (this.scanning == Boolean.TRUE) {
            ConfigurationWatchListUtil.registerNewConfigurationWatchListWithContext(this.getContext());
            ConfigurationWatchListUtil.setMainWatchURL(this.context, mic.getTopURL());
        }
        LoggerContext lc = (LoggerContext)this.context;
        boolean packagingData = OptionHelper.toBoolean(mic.subst(configurationModel.getPackagingDataStr()), false);
        lc.setPackagingDataEnabled(packagingData);
        ContextUtil contextUtil = new ContextUtil(this.context);
        contextUtil.addGroovyPackages(lc.getFrameworkPackages());
    }

    void printScanMessage(Boolean scanning) {
        if (scanning == null) {
            this.addInfo("Scan attribute not set or set to unrecognized value.");
            return;
        }
        if (scanning.booleanValue()) {
            this.addInfo("Scan attribute set to true. Will scan for configuration file changes.");
        } else {
            this.addInfo("Scan attribute set to false.");
        }
    }

    @Override
    public void postHandle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
    }

    private Boolean scanAttrToBoolean(Model model) {
        if (model instanceof ConfigurationModel) {
            ConfigurationModel configurationModel = (ConfigurationModel)model;
            String scanStr = configurationModel.getScanStr();
            return OptionHelper.toBooleanObject(scanStr);
        }
        return null;
    }
}

