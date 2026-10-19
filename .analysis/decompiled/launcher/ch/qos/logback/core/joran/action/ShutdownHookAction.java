/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.action;

import ch.qos.logback.core.joran.action.BaseModelAction;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.ShutdownHookModel;
import org.xml.sax.Attributes;

public class ShutdownHookAction
extends BaseModelAction {
    @Override
    protected boolean validPreconditions(SaxEventInterpretationContext interpretationContext, String name, Attributes attributes) {
        return true;
    }

    @Override
    protected Model buildCurrentModel(SaxEventInterpretationContext interpretationContext, String name, Attributes attributes) {
        ShutdownHookModel shutdownHookModel = new ShutdownHookModel();
        String className = attributes.getValue("class");
        shutdownHookModel.setClassName(className);
        return shutdownHookModel;
    }
}

