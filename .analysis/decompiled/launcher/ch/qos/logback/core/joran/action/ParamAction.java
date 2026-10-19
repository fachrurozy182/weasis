/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.action;

import ch.qos.logback.core.joran.action.BaseModelAction;
import ch.qos.logback.core.joran.action.PreconditionValidator;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.ParamModel;
import org.xml.sax.Attributes;

public class ParamAction
extends BaseModelAction {
    @Override
    protected boolean validPreconditions(SaxEventInterpretationContext intercon, String name, Attributes attributes) {
        PreconditionValidator pv = new PreconditionValidator(this, intercon, name, attributes);
        pv.validateNameAttribute();
        pv.validateValueAttribute();
        this.addWarn("<param> element is deprecated in favor of a more direct syntax." + this.atLine(intercon));
        this.addWarn("For details see https://logback.qos.ch/codes.html#param");
        return pv.isValid();
    }

    @Override
    protected Model buildCurrentModel(SaxEventInterpretationContext interpretationContext, String name, Attributes attributes) {
        ParamModel paramModel = new ParamModel();
        paramModel.setName(attributes.getValue("name"));
        paramModel.setValue(attributes.getValue("value"));
        return paramModel;
    }
}

