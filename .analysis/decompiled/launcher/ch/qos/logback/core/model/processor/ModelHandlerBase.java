/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.spi.ContextAwareBase;

public abstract class ModelHandlerBase
extends ContextAwareBase {
    public ModelHandlerBase(Context context) {
        this.setContext(context);
    }

    protected Class<? extends Model> getSupportedModelClass() {
        return Model.class;
    }

    protected boolean isSupportedModelType(Model model) {
        Class<? extends Model> modelClass = this.getSupportedModelClass();
        if (modelClass.isInstance(model)) {
            return true;
        }
        this.addError("This handler can only handle models of type [" + String.valueOf(modelClass) + "]");
        return false;
    }

    public abstract void handle(ModelInterpretationContext var1, Model var2) throws ModelHandlerException;

    public void postHandle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
    }

    public String toString() {
        return this.getClass().getName();
    }
}

