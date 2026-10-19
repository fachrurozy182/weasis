/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.processor.conditional;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.conditional.IfModel;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.util.OptionHelper;

public class IfModelHandler
extends ModelHandlerBase {
    public static final String MISSING_JANINO_MSG = "Could not find Janino library on the class path. Skipping conditional processing.";
    public static final String MISSING_JANINO_SEE = "See also https://logback.qos.ch/codes.html#ifJanino";
    public static final String BLACKLISTED_REF_DISALLOWED_MSG = "The 'condition' attribute may not contain blacklisted references.";
    public static final String BLACKLISTED_REF_DISALLOWED_SEE = "See also https://logback.qos.ch/codes.html#conditionBlacklisted";
    public static final String UNICODE_DISALLOWED_MSG = "The 'condition' attribute may not contain unicode escape characters.";
    public static final String UNICODE_DISALLOWED_SEE = "See also https://logback.qos.ch/codes.html#conditionUnicode";
    public static final String CONDITION_ATTR_DEPRECATED_MSG = "The 'condition' attribute in <if> element is deprecated and slated for removal. Use <condition> element instead.";
    public static final String CONDITION_ATTR_DEPRECATED_SEE = "See also https://logback.qos.ch/codes.html#conditionAttributeDeprecation";
    IfModel ifModel = null;

    public IfModelHandler(Context context) {
        super(context);
    }

    public static ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new IfModelHandler(context);
    }

    protected Class<IfModel> getSupportedModelClass() {
        return IfModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        this.ifModel = (IfModel)model;
        mic.pushModel(this.ifModel);
        Object micTopObject = mic.peekObject();
        String conditionStr = this.ifModel.getCondition();
        this.emitDeprecationWarningIfNecessary(conditionStr);
        if (micTopObject instanceof IfModel.BranchState) {
            IfModel.BranchState branchState = (IfModel.BranchState)((Object)micTopObject);
            this.ifModel.setBranchState(branchState);
            mic.popObject();
        }
    }

    private void emitDeprecationWarningIfNecessary(String conditionStr) {
        if (!OptionHelper.isNullOrEmptyOrAllSpaces(conditionStr)) {
            this.addWarn(CONDITION_ATTR_DEPRECATED_MSG);
            this.addWarn(CONDITION_ATTR_DEPRECATED_SEE);
        }
    }

    @Override
    public void postHandle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        if (mic.isModelStackEmpty()) {
            this.addError("Unexpected unexpected empty model stack.");
            return;
        }
        Model o = mic.peekModel();
        if (o != this.ifModel) {
            this.addWarn("The object [" + String.valueOf(o) + "] on the top the of the stack is not the expected [" + String.valueOf(this.ifModel));
        } else {
            mic.popModel();
        }
    }

    static enum Branch {
        IF_BRANCH,
        ELSE_BRANCH;

    }
}

