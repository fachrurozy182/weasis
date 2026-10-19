/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.processor.conditional;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.boolex.PropertyCondition;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.conditional.ByPropertiesConditionModel;
import ch.qos.logback.core.model.conditional.IfModel;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.util.OptionHelper;

public class ByPropertiesConditionModelHandler
extends ModelHandlerBase {
    static final String EVALUTATOR_IS_IN_ERROR_MSG0 = "The [if] statement will be marked IN_ERROR and none of its branches will be executed.";
    static final String EVALUTATOR_IS_IN_ERROR_MSG1 = "The [if] statement will be marked IN_ERROR and none of its branches will be executed.";
    private boolean inError = false;
    PropertyCondition propertyEvaluator;

    public ByPropertiesConditionModelHandler(Context context) {
        super(context);
    }

    protected Class<ByPropertiesConditionModel> getSupportedModelClass() {
        return ByPropertiesConditionModel.class;
    }

    public static ModelHandlerBase makeInstance(Context context, ModelInterpretationContext mic) {
        return new ByPropertiesConditionModelHandler(context);
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        ByPropertiesConditionModel byPropertiesConditionModel = (ByPropertiesConditionModel)model;
        String className = byPropertiesConditionModel.getClassName();
        if (OptionHelper.isNullOrEmptyOrAllSpaces(className)) {
            this.addWarn("Missing className. This should have been caught earlier.");
            this.inError = true;
            return;
        }
        className = mic.getImport(className);
        try {
            this.addInfo("About to instantiate PropertyEvaluator of type [" + className + "]");
            this.propertyEvaluator = (PropertyCondition)OptionHelper.instantiateByClassName(className, PropertyCondition.class, this.context);
            this.propertyEvaluator.setContext(this.context);
            this.propertyEvaluator.setLocalPropertyContainer(mic);
            mic.pushObject(this.propertyEvaluator);
        }
        catch (Exception e) {
            this.inError = true;
            mic.pushObject((Object)IfModel.BranchState.IN_ERROR);
            this.addError("Could not create a SequenceNumberGenerator of type [" + className + "].", e);
            throw new ModelHandlerException(e);
        }
    }

    @Override
    public void postHandle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        if (this.inError) {
            return;
        }
        Object o = mic.peekObject();
        if (o != this.propertyEvaluator) {
            this.addWarn("The object at the of the stack is not the propertyEvaluator instance pushed earlier.");
        } else {
            mic.popObject();
        }
        this.propertyEvaluator.start();
        if (!this.propertyEvaluator.isStarted()) {
            this.addError("PropertyEvaluator of type [" + this.propertyEvaluator.getClass().getName() + "] did not start successfully.");
            this.addError("The [if] statement will be marked IN_ERROR and none of its branches will be executed.");
            mic.pushObject((Object)IfModel.BranchState.IN_ERROR);
            return;
        }
        boolean evaluationResult = this.propertyEvaluator.evaluate();
        IfModel.BranchState branchState = evaluationResult ? IfModel.BranchState.IF_BRANCH : IfModel.BranchState.ELSE_BRANCH;
        mic.pushObject((Object)branchState);
    }
}

