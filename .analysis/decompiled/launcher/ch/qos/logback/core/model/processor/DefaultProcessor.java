/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.ModelHandlerFactoryMethod;
import ch.qos.logback.core.model.NamedComponentModel;
import ch.qos.logback.core.model.processor.AppenderDeclarationAnalyser;
import ch.qos.logback.core.model.processor.ChainedModelFilter;
import ch.qos.logback.core.model.processor.ModelFilter;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.model.processor.PhaseIndicator;
import ch.qos.logback.core.model.processor.ProcessingPhase;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.FilterReply;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class DefaultProcessor
extends ContextAwareBase {
    protected final ModelInterpretationContext mic;
    final HashMap<Class<? extends Model>, ModelHandlerFactoryMethod> modelClassToHandlerMap = new HashMap();
    final HashMap<Class<? extends Model>, List<Supplier<ModelHandlerBase>>> modelClassToDependencyAnalyserMap = new HashMap();
    ChainedModelFilter phaseOneFilter = new ChainedModelFilter();
    ChainedModelFilter phaseTwoFilter = new ChainedModelFilter();
    static final int DENIED = -1;

    public DefaultProcessor(Context context, ModelInterpretationContext mic) {
        this.setContext(context);
        this.mic = mic;
    }

    public void addHandler(Class<? extends Model> modelClass, ModelHandlerFactoryMethod modelFactoryMethod) {
        this.modelClassToHandlerMap.put(modelClass, modelFactoryMethod);
        ProcessingPhase phase = this.determineProcessingPhase(modelClass);
        switch (phase) {
            case FIRST: {
                this.getPhaseOneFilter().allow(modelClass);
                break;
            }
            case SECOND: {
                this.getPhaseTwoFilter().allow(modelClass);
                break;
            }
            default: {
                throw new IllegalArgumentException("unexpected value " + String.valueOf((Object)phase) + " for model class " + modelClass.getName());
            }
        }
    }

    private ProcessingPhase determineProcessingPhase(Class<? extends Model> modelClass) {
        PhaseIndicator phaseIndicator = modelClass.getAnnotation(PhaseIndicator.class);
        if (phaseIndicator == null) {
            return ProcessingPhase.FIRST;
        }
        ProcessingPhase phase = phaseIndicator.phase();
        return phase;
    }

    public void addAnalyser(Class<? extends Model> modelClass, Supplier<ModelHandlerBase> analyserSupplier) {
        this.modelClassToDependencyAnalyserMap.computeIfAbsent(modelClass, x -> new ArrayList()).add(analyserSupplier);
    }

    private void traversalLoop(TraverseMethod traverseMethod, Model model, ModelFilter modelfFilter, String phaseName) {
        int handledModelCount;
        int LIMIT = 3;
        for (int i = 0; i < LIMIT && (handledModelCount = traverseMethod.traverse(model, modelfFilter)) != 0; ++i) {
        }
    }

    public void process(Model model) {
        if (model == null) {
            this.addError("Expecting non null model to process");
            return;
        }
        this.initialObjectPush();
        this.mainTraverse(model, this.getPhaseOneFilter());
        this.analyseDependencies(model);
        this.traversalLoop(this::secondPhaseTraverse, model, this.getPhaseTwoFilter(), "phase 2");
        this.addInfo("End of configuration.");
        this.finalObjectPop();
    }

    private void finalObjectPop() {
        this.mic.popObject();
    }

    private void initialObjectPush() {
        this.mic.pushObject(this.context);
    }

    public ChainedModelFilter getPhaseOneFilter() {
        return this.phaseOneFilter;
    }

    public ChainedModelFilter getPhaseTwoFilter() {
        return this.phaseTwoFilter;
    }

    protected void analyseDependencies(Model model) {
        List<Supplier<ModelHandlerBase>> analyserSupplierList = this.modelClassToDependencyAnalyserMap.get(model.getClass());
        if (analyserSupplierList != null) {
            for (Supplier supplier : analyserSupplierList) {
                ModelHandlerBase analyser = null;
                if (supplier != null) {
                    analyser = (ModelHandlerBase)supplier.get();
                }
                if (analyser != null && !model.isSkipped()) {
                    this.callAnalyserHandleOnModel(model, analyser);
                }
                if (analyser == null || model.isSkipped()) continue;
                this.callAnalyserPostHandleOnModel(model, analyser);
            }
        }
        for (Model model2 : model.getSubModels()) {
            this.analyseDependencies(model2);
        }
    }

    private void callAnalyserPostHandleOnModel(Model model, ModelHandlerBase analyser) {
        try {
            analyser.postHandle(this.mic, model);
        }
        catch (ModelHandlerException e) {
            this.addError("Failed to invoke postHandle on model " + model.getTag(), e);
        }
    }

    private void callAnalyserHandleOnModel(Model model, ModelHandlerBase analyser) {
        try {
            analyser.handle(this.mic, model);
        }
        catch (ModelHandlerException e) {
            this.addError("Failed to traverse model " + model.getTag(), e);
        }
    }

    private ModelHandlerBase createHandler(Model model) {
        ModelHandlerFactoryMethod modelFactoryMethod = this.modelClassToHandlerMap.get(model.getClass());
        if (modelFactoryMethod == null) {
            this.addError("Can't handle model of type " + String.valueOf(model.getClass()) + "  with tag: " + model.getTag() + " at line " + model.getLineNumber());
            return null;
        }
        ModelHandlerBase handler = modelFactoryMethod.make(this.context, this.mic);
        if (handler == null) {
            return null;
        }
        if (!handler.isSupportedModelType(model)) {
            this.addWarn("Handler [" + String.valueOf(handler.getClass()) + "] does not support " + model.idString());
            return null;
        }
        return handler;
    }

    protected int mainTraverse(Model model, ModelFilter modelFiler) {
        FilterReply filterReply = modelFiler.decide(model);
        if (filterReply == FilterReply.DENY) {
            return -1;
        }
        int count = 0;
        try {
            ModelHandlerBase handler = null;
            boolean unhandled = model.isUnhandled();
            if (unhandled && (handler = this.createHandler(model)) != null) {
                handler.handle(this.mic, model);
                model.markAsHandled();
                ++count;
            }
            if (!model.isSkipped()) {
                for (Model m : model.getSubModels()) {
                    count += this.mainTraverse(m, modelFiler);
                }
            }
            if (unhandled && handler != null) {
                handler.postHandle(this.mic, model);
            }
        }
        catch (ModelHandlerException e) {
            this.addError("Failed to traverse model " + model.getTag(), e);
        }
        return count;
    }

    protected int secondPhaseTraverse(Model model, ModelFilter modelFilter) {
        FilterReply filterReply = modelFilter.decide(model);
        if (filterReply == FilterReply.DENY) {
            return 0;
        }
        int count = 0;
        try {
            boolean allDependenciesStarted = this.allDependenciesStarted(model);
            ModelHandlerBase handler = null;
            if (model.isUnhandled() && allDependenciesStarted && (handler = this.createHandler(model)) != null) {
                handler.handle(this.mic, model);
                model.markAsHandled();
                ++count;
            }
            if (!allDependenciesStarted && !this.dependencyIsLocatedInASubmodel(model)) {
                return count;
            }
            if (!model.isSkipped()) {
                for (Model m : model.getSubModels()) {
                    count += this.secondPhaseTraverse(m, modelFilter);
                }
            }
            if (handler != null) {
                handler.postHandle(this.mic, model);
            }
        }
        catch (ModelHandlerException e) {
            this.addError("Failed to traverse model " + model.getTag(), e);
        }
        return count;
    }

    private boolean dependencyIsLocatedInASubmodel(Model model) {
        List<String> dependencyNames = this.mic.getDependencyNamesForModel(model);
        if (dependencyNames == null || dependencyNames.isEmpty()) {
            return false;
        }
        return this.recursiveIsDependencyPredicate(dependencyNames, model);
    }

    private boolean recursiveIsDependencyPredicate(List<String> dependencyNames, Model model) {
        NamedComponentModel namedComponentModel;
        String modelName;
        if (model instanceof NamedComponentModel && dependencyNames.contains(modelName = (namedComponentModel = (NamedComponentModel)model).getName())) {
            return true;
        }
        for (Model submodel : model.getSubModels()) {
            boolean result = this.recursiveIsDependencyPredicate(dependencyNames, submodel);
            if (!result) continue;
            return true;
        }
        return false;
    }

    private boolean allDependenciesStarted(Model model) {
        List<String> dependencyNames = this.mic.getDependencyNamesForModel(model);
        if (dependencyNames == null || dependencyNames.isEmpty()) {
            return true;
        }
        for (String name : dependencyNames) {
            boolean isStarted;
            boolean isRegistered = AppenderDeclarationAnalyser.isAppenderDeclared(this.mic, name);
            if (!isRegistered || (isStarted = this.mic.isNamedDependemcyStarted(name))) continue;
            return false;
        }
        return true;
    }

    static interface TraverseMethod {
        public int traverse(Model var1, ModelFilter var2);
    }
}

