/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.joran.action;

import ch.qos.logback.classic.model.LoggerContextListenerModel;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.joran.action.BaseModelAction;
import ch.qos.logback.core.joran.action.PreconditionValidator;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.Model;
import org.xml.sax.Attributes;

public class LoggerContextListenerAction
extends BaseModelAction {
    boolean inError = false;
    LoggerContextListener lcl;

    @Override
    protected boolean validPreconditions(SaxEventInterpretationContext ic, String name, Attributes attributes) {
        PreconditionValidator pv = new PreconditionValidator(this, ic, name, attributes);
        pv.validateClassAttribute();
        return pv.isValid();
    }

    @Override
    protected Model buildCurrentModel(SaxEventInterpretationContext interpretationContext, String name, Attributes attributes) {
        LoggerContextListenerModel loggerContextListenerModel = new LoggerContextListenerModel();
        loggerContextListenerModel.setClassName(attributes.getValue("class"));
        return loggerContextListenerModel;
    }
}

