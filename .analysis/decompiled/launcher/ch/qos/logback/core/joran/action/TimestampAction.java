/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.action;

import ch.qos.logback.core.joran.action.BaseModelAction;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.TimestampModel;
import ch.qos.logback.core.util.OptionHelper;
import org.xml.sax.Attributes;

public class TimestampAction
extends BaseModelAction {
    public static final String DATE_PATTERN_ATTRIBUTE = "datePattern";
    public static final String TIME_REFERENCE_ATTRIBUTE = "timeReference";

    @Override
    protected boolean validPreconditions(SaxEventInterpretationContext interpretationContext, String name, Attributes attributes) {
        String datePatternStr;
        boolean valid = true;
        String keyStr = attributes.getValue("key");
        if (OptionHelper.isNullOrEmptyOrAllSpaces(keyStr)) {
            this.addError("Attribute named [key] cannot be empty");
            valid = false;
        }
        if (OptionHelper.isNullOrEmptyOrAllSpaces(datePatternStr = attributes.getValue(DATE_PATTERN_ATTRIBUTE))) {
            this.addError("Attribute named [datePattern] cannot be empty");
            valid = false;
        }
        return valid;
    }

    @Override
    protected Model buildCurrentModel(SaxEventInterpretationContext interpretationContext, String name, Attributes attributes) {
        TimestampModel timestampModel = new TimestampModel();
        timestampModel.setKey(attributes.getValue("key"));
        timestampModel.setDatePattern(attributes.getValue(DATE_PATTERN_ATTRIBUTE));
        timestampModel.setTimeReference(attributes.getValue(TIME_REFERENCE_ATTRIBUTE));
        timestampModel.setScopeStr(attributes.getValue("scope"));
        return timestampModel;
    }
}

