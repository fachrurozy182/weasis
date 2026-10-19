/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.model.processor;

import ch.qos.logback.classic.model.ContextNameModel;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;

public class ContextNameModelHandler
extends ModelHandlerBase {
    public ContextNameModelHandler(Context context) {
        super(context);
    }

    public static ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new ContextNameModelHandler(context);
    }

    protected Class<ContextNameModel> getSupportedModelClass() {
        return ContextNameModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        ContextNameModel contextNameModel = (ContextNameModel)model;
        String finalBody = mic.subst(contextNameModel.getBodyText());
        this.addInfo("Setting logger context name as [" + finalBody + "]");
        try {
            this.context.setName(finalBody);
        }
        catch (IllegalStateException e) {
            this.addError("Failed to rename context [" + this.context.getName() + "] as [" + finalBody + "]", e);
        }
    }
}

