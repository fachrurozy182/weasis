/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.action;

import ch.qos.logback.core.joran.action.BaseModelAction;
import ch.qos.logback.core.joran.action.PreconditionValidator;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.DefineModel;
import ch.qos.logback.core.model.Model;
import org.xml.sax.Attributes;

public class DefinePropertyAction
extends BaseModelAction {
    @Override
    protected boolean validPreconditions(SaxEventInterpretationContext ic, String name, Attributes attributes) {
        PreconditionValidator validator = new PreconditionValidator(this, ic, name, attributes);
        validator.validateClassAttribute();
        validator.validateNameAttribute();
        return validator.isValid();
    }

    @Override
    protected Model buildCurrentModel(SaxEventInterpretationContext interpretationContext, String name, Attributes attributes) {
        DefineModel defineModel = new DefineModel();
        defineModel.setClassName(attributes.getValue("class"));
        defineModel.setName(attributes.getValue("name"));
        defineModel.setScopeStr(attributes.getValue("scope"));
        return defineModel;
    }
}

