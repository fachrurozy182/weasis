/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran;

import ch.qos.logback.core.joran.GenericXMLConfigurator;
import ch.qos.logback.core.joran.action.AppenderAction;
import ch.qos.logback.core.joran.action.AppenderRefAction;
import ch.qos.logback.core.joran.action.ContextPropertyAction;
import ch.qos.logback.core.joran.action.ConversionRuleAction;
import ch.qos.logback.core.joran.action.DefinePropertyAction;
import ch.qos.logback.core.joran.action.EventEvaluatorAction;
import ch.qos.logback.core.joran.action.ImplicitModelAction;
import ch.qos.logback.core.joran.action.ImportAction;
import ch.qos.logback.core.joran.action.NewRuleAction;
import ch.qos.logback.core.joran.action.ParamAction;
import ch.qos.logback.core.joran.action.PropertyAction;
import ch.qos.logback.core.joran.action.SequenceNumberGeneratorAction;
import ch.qos.logback.core.joran.action.SerializeModelAction;
import ch.qos.logback.core.joran.action.ShutdownHookAction;
import ch.qos.logback.core.joran.action.SiftAction;
import ch.qos.logback.core.joran.action.StatusListenerAction;
import ch.qos.logback.core.joran.action.TimestampAction;
import ch.qos.logback.core.joran.conditional.ByPropertiesConditionAction;
import ch.qos.logback.core.joran.conditional.ElseAction;
import ch.qos.logback.core.joran.conditional.IfAction;
import ch.qos.logback.core.joran.conditional.ThenAction;
import ch.qos.logback.core.joran.sanity.AppenderWithinAppenderSanityChecker;
import ch.qos.logback.core.joran.sanity.SanityChecker;
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.joran.spi.SaxEventInterpreter;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.DefaultProcessor;
import ch.qos.logback.core.spi.ContextAware;

public abstract class JoranConfiguratorBase<E>
extends GenericXMLConfigurator {
    @Override
    protected void addElementSelectorAndActionAssociations(RuleStore rs) {
        rs.addRule(new ElementSelector("*/variable"), PropertyAction::new);
        rs.addRule(new ElementSelector("*/property"), PropertyAction::new);
        rs.addRule(new ElementSelector("*/substitutionProperty"), PropertyAction::new);
        rs.addRule(new ElementSelector("configuration/import"), ImportAction::new);
        rs.addRule(new ElementSelector("configuration/timestamp"), TimestampAction::new);
        rs.addRule(new ElementSelector("configuration/shutdownHook"), ShutdownHookAction::new);
        rs.addRule(new ElementSelector("configuration/sequenceNumberGenerator"), SequenceNumberGeneratorAction::new);
        rs.addRule(new ElementSelector("configuration/serializeModel"), SerializeModelAction::new);
        rs.addRule(new ElementSelector("configuration/define"), DefinePropertyAction::new);
        rs.addRule(new ElementSelector("configuration/evaluator"), EventEvaluatorAction::new);
        rs.addRule(new ElementSelector("configuration/contextProperty"), ContextPropertyAction::new);
        rs.addRule(new ElementSelector("configuration/conversionRule"), ConversionRuleAction::new);
        rs.addRule(new ElementSelector("configuration/statusListener"), StatusListenerAction::new);
        rs.addRule(new ElementSelector("*/appender"), AppenderAction::new);
        rs.addRule(new ElementSelector("configuration/appender/appender-ref"), AppenderRefAction::new);
        rs.addRule(new ElementSelector("configuration/newRule"), NewRuleAction::new);
        rs.addRule(new ElementSelector("*/param"), ParamAction::new);
        rs.addRule(new ElementSelector("*/condition"), ByPropertiesConditionAction::new);
        rs.addRule(new ElementSelector("*/if"), IfAction::new);
        rs.addTransparentPathPart("if");
        rs.addRule(new ElementSelector("*/if/then"), ThenAction::new);
        rs.addTransparentPathPart("then");
        rs.addRule(new ElementSelector("*/if/else"), ElseAction::new);
        rs.addTransparentPathPart("else");
        rs.addRule(new ElementSelector("*/appender/sift"), SiftAction::new);
        rs.addTransparentPathPart("sift");
    }

    @Override
    protected void sanityCheck(Model topModel) {
        this.performCheck(new AppenderWithinAppenderSanityChecker(), topModel);
    }

    protected void performCheck(SanityChecker sc, Model model) {
        if (sc instanceof ContextAware) {
            ((ContextAware)((Object)sc)).setContext(this.context);
        }
        sc.check(model);
    }

    @Override
    protected void setImplicitRuleSupplier(SaxEventInterpreter interpreter) {
        interpreter.setImplicitActionSupplier(ImplicitModelAction::new);
    }

    @Override
    public void buildModelInterpretationContext() {
        super.buildModelInterpretationContext();
        this.modelInterpretationContext.createAppenderBags();
    }

    public SaxEventInterpretationContext getInterpretationContext() {
        return this.saxEventInterpreter.getSaxEventInterpretationContext();
    }

    @Override
    protected void addModelHandlerAssociations(DefaultProcessor defaultProcessor) {
    }
}

