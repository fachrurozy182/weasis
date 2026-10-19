/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.AppenderRefModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.DependencyDefinition;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.model.processor.PhaseIndicator;
import ch.qos.logback.core.model.processor.ProcessingPhase;
import java.util.ArrayList;
import java.util.List;

@PhaseIndicator(phase=ProcessingPhase.DEPENDENCY_ANALYSIS)
public class AppenderRefDependencyAnalyser
extends ModelHandlerBase {
    public AppenderRefDependencyAnalyser(Context context) {
        super(context);
    }

    protected Class<Model> getSupportedModelClass() {
        return Model.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model parentModel) throws ModelHandlerException {
        ArrayList<AppenderRefModel> appenderRefModelList = new ArrayList<AppenderRefModel>();
        this.collectAllAppenderRefModels(appenderRefModelList, parentModel);
        for (AppenderRefModel appenderRefModel : appenderRefModelList) {
            String ref = mic.subst(appenderRefModel.getRef());
            DependencyDefinition dd = new DependencyDefinition(parentModel, ref);
            mic.addDependencyDefinition(dd);
        }
    }

    public void collectAllAppenderRefModels(List<AppenderRefModel> list, Model model) {
        if (model == null) {
            return;
        }
        if (model instanceof AppenderRefModel) {
            list.add((AppenderRefModel)model);
        }
        model.getSubModels().forEach(subModel -> this.collectAllAppenderRefModels(list, (Model)subModel));
    }
}

