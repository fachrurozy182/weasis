/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.joran;

import ch.qos.logback.classic.joran.ModelClassToModelHandlerLinker;
import ch.qos.logback.classic.joran.action.ConfigurationAction;
import ch.qos.logback.classic.joran.action.ConsolePluginAction;
import ch.qos.logback.classic.joran.action.ContextNameAction;
import ch.qos.logback.classic.joran.action.InsertFromJNDIAction;
import ch.qos.logback.classic.joran.action.LevelAction;
import ch.qos.logback.classic.joran.action.LoggerAction;
import ch.qos.logback.classic.joran.action.LoggerContextListenerAction;
import ch.qos.logback.classic.joran.action.PropertiesConfiguratorAction;
import ch.qos.logback.classic.joran.action.RootLoggerAction;
import ch.qos.logback.classic.joran.sanity.IfNestedWithinSecondPhaseElementSC;
import ch.qos.logback.classic.model.processor.ConfigurationModelHandlerFull;
import ch.qos.logback.classic.model.processor.LogbackClassicDefaultNestedComponentRules;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.joran.JoranConfiguratorBase;
import ch.qos.logback.core.joran.action.AppenderRefAction;
import ch.qos.logback.core.joran.action.IncludeAction;
import ch.qos.logback.core.joran.spi.DefaultNestedComponentRegistry;
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.DefaultProcessor;

public class JoranConfigurator
extends JoranConfiguratorBase<ILoggingEvent> {
    @Override
    public void addElementSelectorAndActionAssociations(RuleStore rs) {
        super.addElementSelectorAndActionAssociations(rs);
        rs.addRule(new ElementSelector("configuration"), () -> new ConfigurationAction());
        rs.addRule(new ElementSelector("configuration/contextName"), () -> new ContextNameAction());
        rs.addRule(new ElementSelector("configuration/contextListener"), () -> new LoggerContextListenerAction());
        rs.addRule(new ElementSelector("configuration/insertFromJNDI"), () -> new InsertFromJNDIAction());
        rs.addRule(new ElementSelector("configuration/logger"), () -> new LoggerAction());
        rs.addRule(new ElementSelector("configuration/logger/level"), () -> new LevelAction());
        rs.addRule(new ElementSelector("configuration/root"), () -> new RootLoggerAction());
        rs.addRule(new ElementSelector("configuration/root/level"), () -> new LevelAction());
        rs.addRule(new ElementSelector("configuration/logger/appender-ref"), () -> new AppenderRefAction());
        rs.addRule(new ElementSelector("configuration/root/appender-ref"), () -> new AppenderRefAction());
        rs.addRule(new ElementSelector("configuration/include"), () -> new IncludeAction());
        rs.addRule(new ElementSelector("configuration/propertiesConfigurator"), () -> new PropertiesConfiguratorAction());
        rs.addRule(new ElementSelector("configuration/consolePlugin"), () -> new ConsolePluginAction());
    }

    @Override
    protected void sanityCheck(Model topModel) {
        super.sanityCheck(topModel);
        this.performCheck(new IfNestedWithinSecondPhaseElementSC(), topModel);
    }

    @Override
    protected void addDefaultNestedComponentRegistryRules(DefaultNestedComponentRegistry registry) {
        LogbackClassicDefaultNestedComponentRules.addDefaultNestedComponentRegistryRules(registry);
    }

    protected JoranConfigurator makeAnotherInstance() {
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(this.context);
        return jc;
    }

    @Override
    public void buildModelInterpretationContext() {
        super.buildModelInterpretationContext();
        this.modelInterpretationContext.setConfiguratorSupplier(() -> this.makeAnotherInstance());
    }

    @Override
    protected void addModelHandlerAssociations(DefaultProcessor defaultProcessor) {
        ModelClassToModelHandlerLinker m = new ModelClassToModelHandlerLinker(this.context);
        m.setConfigurationModelHandlerFactoryMethod(ConfigurationModelHandlerFull::makeInstance);
        m.link(defaultProcessor);
    }

    private void sealModelFilters(DefaultProcessor defaultProcessor) {
        defaultProcessor.getPhaseOneFilter().denyAll();
        defaultProcessor.getPhaseTwoFilter().allowAll();
    }
}

