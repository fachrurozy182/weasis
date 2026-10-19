/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.action;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.util.OptionHelper;
import org.xml.sax.Attributes;

public class NewRuleAction
extends Action {
    boolean inError = false;

    @Override
    public void begin(SaxEventInterpretationContext ec, String localName, Attributes attributes) {
        this.inError = false;
        String pattern = attributes.getValue("pattern");
        String actionClass = attributes.getValue("actionClass");
        if (OptionHelper.isNullOrEmptyOrAllSpaces(pattern)) {
            this.inError = true;
            String errorMsg = "No 'pattern' attribute in <newRule>";
            this.addError(errorMsg);
            return;
        }
        if (OptionHelper.isNullOrEmptyOrAllSpaces(actionClass)) {
            this.inError = true;
            String errorMsg = "No 'actionClass' attribute in <newRule>";
            this.addError(errorMsg);
            return;
        }
        try {
            this.addInfo("About to add new Joran parsing rule [" + pattern + "," + actionClass + "].");
            ec.getSaxEventInterpreter().getRuleStore().addRule(new ElementSelector(pattern), actionClass);
        }
        catch (Exception oops) {
            this.inError = true;
            String errorMsg = "Could not add new Joran parsing rule [" + pattern + "," + actionClass + "]";
            this.addError(errorMsg);
        }
    }

    @Override
    public void end(SaxEventInterpretationContext ec, String n) {
    }

    public void finish(SaxEventInterpretationContext ec) {
    }
}

