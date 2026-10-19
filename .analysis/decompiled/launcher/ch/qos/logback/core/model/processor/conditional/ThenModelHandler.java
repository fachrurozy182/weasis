/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.processor.conditional;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.conditional.IfModel;
import ch.qos.logback.core.model.conditional.ThenModel;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;

public class ThenModelHandler
extends ModelHandlerBase {
    public ThenModelHandler(Context context) {
        super(context);
    }

    public static ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new ThenModelHandler(context);
    }

    protected Class<ThenModel> getSupportedModelClass() {
        return ThenModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        ThenModel thenModel = (ThenModel)model;
        if (mic.isModelStackEmpty()) {
            this.addError("Unexpected empty model stack. Have you omitted the <if> part?");
            thenModel.markAsSkipped();
            return;
        }
        Model parent = mic.peekModel();
        if (!(parent instanceof IfModel)) {
            this.addError("Unexpected type for parent model [" + String.valueOf(parent) + "]");
            thenModel.markAsSkipped();
            return;
        }
        IfModel ifModel = (IfModel)parent;
        if (ifModel.getBranchState() != IfModel.BranchState.IF_BRANCH) {
            thenModel.deepMarkAsSkipped();
        }
    }
}

