/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.processor.conditional;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.conditional.ElseModel;
import ch.qos.logback.core.model.conditional.IfModel;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;

public class ElseModelHandler
extends ModelHandlerBase {
    public ElseModelHandler(Context context) {
        super(context);
    }

    public static ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new ElseModelHandler(context);
    }

    protected Class<ElseModel> getSupportedModelClass() {
        return ElseModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        ElseModel elseModel = (ElseModel)model;
        Model parent = mic.peekModel();
        if (!(parent instanceof IfModel)) {
            this.addError("Unexpected type for parent model [" + String.valueOf(parent) + "]");
            elseModel.markAsSkipped();
            return;
        }
        IfModel ifModel = (IfModel)parent;
        if (ifModel.getBranchState() != IfModel.BranchState.ELSE_BRANCH) {
            elseModel.deepMarkAsSkipped();
        }
    }
}

