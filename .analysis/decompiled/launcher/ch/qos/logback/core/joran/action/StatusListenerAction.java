/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.action;

import ch.qos.logback.core.joran.action.BaseModelAction;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.StatusListenerModel;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.util.OptionHelper;
import org.xml.sax.Attributes;

public class StatusListenerAction
extends BaseModelAction {
    boolean inError = false;
    Boolean effectivelyAdded = null;
    StatusListener statusListener = null;

    @Override
    protected boolean validPreconditions(SaxEventInterpretationContext interpretationContext, String name, Attributes attributes) {
        String className = attributes.getValue("class");
        if (OptionHelper.isNullOrEmptyOrAllSpaces(className)) {
            this.addError("Missing class name for statusListener. Near [" + name + "] line " + StatusListenerAction.getLineNumber(interpretationContext));
            return false;
        }
        return true;
    }

    @Override
    protected Model buildCurrentModel(SaxEventInterpretationContext interpretationContext, String name, Attributes attributes) {
        StatusListenerModel statusListenerModel = new StatusListenerModel();
        statusListenerModel.setClassName(attributes.getValue("class"));
        return statusListenerModel;
    }
}

