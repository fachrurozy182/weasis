/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.model.processor.PhaseIndicator;
import ch.qos.logback.core.model.processor.ProcessingPhase;

@PhaseIndicator(phase=ProcessingPhase.DEPENDENCY_ANALYSIS)
public class RefContainerDependencyAnalyser
extends ModelHandlerBase {
    final Class<?> modelClass;

    public RefContainerDependencyAnalyser(Context context, Class<?> modelClass) {
        super(context);
        this.modelClass = modelClass;
    }

    @Override
    protected boolean isSupportedModelType(Model model) {
        if (this.modelClass.isInstance(model)) {
            return true;
        }
        StringBuilder buf = new StringBuilder("This handler can only handle models of type ");
        buf.append(this.modelClass.getName());
        this.addError(buf.toString());
        return false;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        mic.pushModel(model);
    }

    @Override
    public void postHandle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        Model poppedModel = mic.popModel();
        if (model != poppedModel) {
            this.addError("Popped model [" + String.valueOf(poppedModel) + "] different than expected [" + String.valueOf(model) + "]");
        }
    }
}

