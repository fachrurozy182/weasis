/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;

public class NOPModelHandler
extends ModelHandlerBase {
    public NOPModelHandler(Context context) {
        super(context);
    }

    public static NOPModelHandler makeInstance(Context context, ModelInterpretationContext ic) {
        return new NOPModelHandler(context);
    }

    @Override
    public void handle(ModelInterpretationContext interpretationContext, Model model) {
    }
}

