/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.joran.action;

import ch.qos.logback.classic.model.LevelModel;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.BaseModelAction;
import ch.qos.logback.core.joran.action.PreconditionValidator;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.Model;
import org.xml.sax.Attributes;

public class LevelAction
extends BaseModelAction {
    @Override
    protected boolean validPreconditions(SaxEventInterpretationContext interpcont, String name, Attributes attributes) {
        PreconditionValidator pv = new PreconditionValidator(this, interpcont, name, attributes);
        pv.validateValueAttribute();
        this.addWarn("<level> element is deprecated. Near [" + name + "] on line " + Action.getLineNumber(interpcont));
        this.addWarn("Please use \"level\" attribute within <logger> or <root> elements instead.");
        return pv.isValid();
    }

    @Override
    protected Model buildCurrentModel(SaxEventInterpretationContext interpretationContext, String name, Attributes attributes) {
        LevelModel lm = new LevelModel();
        String value = attributes.getValue("value");
        lm.setValue(value);
        return lm;
    }
}

