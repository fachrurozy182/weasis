/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.joran.action;

import ch.qos.logback.core.joran.action.BaseModelAction;
import ch.qos.logback.core.joran.action.PreconditionValidator;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.InsertFromJNDIModel;
import ch.qos.logback.core.model.Model;
import org.xml.sax.Attributes;

public class InsertFromJNDIAction
extends BaseModelAction {
    public static final String ENV_ENTRY_NAME_ATTR = "env-entry-name";
    public static final String AS_ATTR = "as";

    @Override
    protected Model buildCurrentModel(SaxEventInterpretationContext interpretationContext, String name, Attributes attributes) {
        InsertFromJNDIModel ifjm = new InsertFromJNDIModel();
        ifjm.setEnvEntryName(attributes.getValue(ENV_ENTRY_NAME_ATTR));
        ifjm.setAs(attributes.getValue(AS_ATTR));
        ifjm.setScopeStr(attributes.getValue("scope"));
        return ifjm;
    }

    @Override
    protected boolean validPreconditions(SaxEventInterpretationContext seic, String name, Attributes attributes) {
        PreconditionValidator validator = new PreconditionValidator(this, seic, name, attributes);
        validator.validateGivenAttribute(ENV_ENTRY_NAME_ATTR);
        validator.validateGivenAttribute(AS_ATTR);
        return validator.isValid();
    }
}

