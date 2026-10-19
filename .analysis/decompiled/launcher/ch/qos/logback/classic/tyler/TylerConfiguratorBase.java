/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.tyler;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.model.ConfigurationModel;
import ch.qos.logback.classic.util.LevelUtil;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.GenericXMLConfigurator;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.util.PropertyModelHandlerHelper;
import ch.qos.logback.core.model.util.VariableSubstitutionsHelper;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.ContextAwarePropertyContainer;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.util.OptionHelper;
import ch.qos.logback.core.util.StatusListenerConfigHelper;
import ch.qos.logback.core.util.StringUtil;
import java.util.Map;
import java.util.function.Supplier;

public class TylerConfiguratorBase
extends ContextAwareBase
implements ContextAwarePropertyContainer {
    public static final String SET_CONTEXT_METHOD_NAME = "setContext";
    public static final String SET_CONTEXT_NAME_METHOD_NAME = "setContextName";
    public static final String SETUP_LOGGER_METHOD_NAME = "setupLogger";
    public static final String VARIABLE_SUBSTITUTIONS_HELPER_FIELD_NAME = "variableSubstitutionsHelper";
    public static final String PROPERTY_MODEL_HANDLER_HELPER_FIELD_NAME = "propertyModelHandlerHelper";
    protected VariableSubstitutionsHelper variableSubstitutionsHelper;
    protected PropertyModelHandlerHelper propertyModelHandlerHelper = new PropertyModelHandlerHelper(this);

    protected Logger setupLogger(String loggerName, String levelString, Boolean additivity) {
        LoggerContext loggerContext = (LoggerContext)this.context;
        Logger logger = loggerContext.getLogger(loggerName);
        if (!OptionHelper.isNullOrEmptyOrAllSpaces(levelString)) {
            Level level = LevelUtil.levelStringToLevel(levelString);
            logger.setLevel(level);
        }
        if (additivity != null) {
            logger.setAdditive(additivity);
        }
        return logger;
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        this.variableSubstitutionsHelper = new VariableSubstitutionsHelper(context);
        this.propertyModelHandlerHelper.setContext(context);
    }

    protected void setContextName(String name) {
        if (StringUtil.isNullOrEmpty(name)) {
            this.addError("Cannot set context name to null or empty string");
            return;
        }
        try {
            String substName = this.subst(name);
            this.addInfo("Setting context name to [" + substName + "]");
            this.context.setName(substName);
        }
        catch (IllegalStateException e) {
            this.addError("Failed to rename context as [" + name + "]");
        }
    }

    protected void addOnConsoleStatusListener() {
        StatusListenerConfigHelper.addOnConsoleListenerInstance(this.context, new OnConsoleStatusListener());
    }

    @Override
    public String subst(String ref) {
        return this.variableSubstitutionsHelper.subst(ref);
    }

    @Override
    public void addSubstitutionProperty(String key, String value) {
        this.variableSubstitutionsHelper.addSubstitutionProperty(key, value);
    }

    @Override
    public String getProperty(String key) {
        return this.variableSubstitutionsHelper.getProperty(key);
    }

    @Override
    public Map<String, String> getCopyOfPropertyMap() {
        return this.variableSubstitutionsHelper.getCopyOfPropertyMap();
    }

    public boolean isNull(String k) {
        String val = OptionHelper.propertyLookup(k, this, this.context);
        return val == null;
    }

    public boolean isDefined(String k) {
        String val = OptionHelper.propertyLookup(k, this, this.context);
        return val != null;
    }

    public String p(String k) {
        return this.property(k);
    }

    public String property(String k) {
        String val = OptionHelper.propertyLookup(k, this, this.context);
        if (val != null) {
            return val;
        }
        return "";
    }

    private JoranConfigurator makeAnotherInstance() {
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(this.context);
        return jc;
    }

    @Override
    public Supplier<? extends GenericXMLConfigurator> getConfiguratorSupplier() {
        Supplier<GenericXMLConfigurator> supplier = () -> this.makeAnotherInstance();
        return supplier;
    }

    protected void processModelFromIncludedFile(Model modelFromIncludedFile) {
        Supplier<? extends GenericXMLConfigurator> configuratorSupplier = this.getConfiguratorSupplier();
        GenericXMLConfigurator genericXMLConfigurator = configuratorSupplier.get();
        ConfigurationModel configurationModel = new ConfigurationModel();
        configurationModel.addSubModel(modelFromIncludedFile);
        genericXMLConfigurator.processModel(configurationModel);
    }
}

