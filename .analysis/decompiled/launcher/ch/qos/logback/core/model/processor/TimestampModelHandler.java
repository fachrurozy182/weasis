/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.ActionUtil;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.TimestampModel;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.util.CachingDateFormatter;
import ch.qos.logback.core.util.OptionHelper;

public class TimestampModelHandler
extends ModelHandlerBase {
    boolean inError = false;

    public TimestampModelHandler(Context context) {
        super(context);
    }

    public static ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new TimestampModelHandler(context);
    }

    protected Class<TimestampModel> getSupportedModelClass() {
        return TimestampModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext interpretationContext, Model model) {
        long timeReference;
        String timeReferenceStr;
        String datePatternStr;
        TimestampModel timestampModel = (TimestampModel)model;
        String keyStr = timestampModel.getKey();
        if (OptionHelper.isNullOrEmptyOrAllSpaces(keyStr)) {
            this.addError("Attribute named [key] cannot be empty");
            this.inError = true;
        }
        if (OptionHelper.isNullOrEmptyOrAllSpaces(datePatternStr = timestampModel.getDatePattern())) {
            this.addError("Attribute named [datePattern] cannot be empty");
            this.inError = true;
        }
        if ("contextBirth".equalsIgnoreCase(timeReferenceStr = timestampModel.getTimeReference())) {
            this.addInfo("Using context birth as time reference.");
            timeReference = this.context.getBirthTime();
        } else {
            timeReference = System.currentTimeMillis();
            this.addInfo("Using current interpretation time, i.e. now, as time reference.");
        }
        if (this.inError) {
            return;
        }
        String scopeStr = timestampModel.getScopeStr();
        ActionUtil.Scope scope = ActionUtil.stringToScope(scopeStr);
        CachingDateFormatter sdf = new CachingDateFormatter(datePatternStr);
        String val = sdf.format(timeReference);
        this.addInfo("Adding property to the context with key=\"" + keyStr + "\" and value=\"" + val + "\" to the " + String.valueOf((Object)scope) + " scope");
        ActionUtil.setProperty(interpretationContext, keyStr, val, scope);
    }
}

