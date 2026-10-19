/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.action;

import ch.qos.logback.core.joran.action.BaseModelAction;
import ch.qos.logback.core.joran.action.PreconditionValidator;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.ImportModel;
import ch.qos.logback.core.model.Model;
import org.xml.sax.Attributes;

public class ImportAction
extends BaseModelAction {
    @Override
    protected boolean validPreconditions(SaxEventInterpretationContext intercon, String name, Attributes attributes) {
        PreconditionValidator pv = new PreconditionValidator(this, intercon, name, attributes);
        pv.validateClassAttribute();
        return pv.isValid();
    }

    @Override
    protected Model buildCurrentModel(SaxEventInterpretationContext interpretationContext, String localName, Attributes attributes) {
        ImportModel importModel = new ImportModel();
        importModel.setClassName(attributes.getValue("class"));
        return importModel;
    }
}

