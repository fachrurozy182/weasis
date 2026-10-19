/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.action;

import ch.qos.logback.core.joran.action.BaseModelAction;
import ch.qos.logback.core.joran.action.PreconditionValidator;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.SequenceNumberGeneratorModel;
import org.xml.sax.Attributes;

public class SequenceNumberGeneratorAction
extends BaseModelAction {
    @Override
    protected Model buildCurrentModel(SaxEventInterpretationContext interpretationContext, String name, Attributes attributes) {
        SequenceNumberGeneratorModel sngm = new SequenceNumberGeneratorModel();
        sngm.setClassName(attributes.getValue("class"));
        return sngm;
    }

    @Override
    protected boolean validPreconditions(SaxEventInterpretationContext seic, String name, Attributes attributes) {
        PreconditionValidator validator = new PreconditionValidator(this, seic, name, attributes);
        validator.validateClassAttribute();
        return validator.isValid();
    }
}

