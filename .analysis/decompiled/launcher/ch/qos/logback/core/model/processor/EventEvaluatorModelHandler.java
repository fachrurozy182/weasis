/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.boolex.EventEvaluator;
import ch.qos.logback.core.joran.spi.DefaultNestedComponentRegistry;
import ch.qos.logback.core.model.EventEvaluatorModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.util.OptionHelper;
import java.util.Map;

public class EventEvaluatorModelHandler
extends ModelHandlerBase {
    EventEvaluator<?> evaluator;
    boolean inError = false;

    public EventEvaluatorModelHandler(Context context) {
        super(context);
    }

    public static ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new EventEvaluatorModelHandler(context);
    }

    protected Class<EventEvaluatorModel> getSupportedModelClass() {
        return EventEvaluatorModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext intercon, Model model) throws ModelHandlerException {
        EventEvaluatorModel eem = (EventEvaluatorModel)model;
        String className = eem.getClassName();
        if (OptionHelper.isNullOrEmptyOrAllSpaces(className)) {
            String defaultClassName = this.defaultClassName(intercon, eem);
            if (OptionHelper.isNullOrEmptyOrAllSpaces(defaultClassName)) {
                this.inError = true;
                this.addError("Mandatory \"class\" attribute missing for <evaluator>");
                this.addError("No default classname could be found.");
                return;
            }
            this.addInfo("Assuming default evaluator class [" + defaultClassName + "]");
            className = defaultClassName;
        } else {
            className = intercon.getImport(className);
        }
        String evaluatorName = intercon.subst(eem.getName());
        try {
            this.evaluator = (EventEvaluator)OptionHelper.instantiateByClassName(className, EventEvaluator.class, this.context);
            this.evaluator.setContext(this.context);
            this.evaluator.setName(evaluatorName);
            intercon.pushObject(this.evaluator);
        }
        catch (Exception oops) {
            this.inError = true;
            this.addError("Could not create evaluator of type " + className + "].", oops);
        }
    }

    private String defaultClassName(ModelInterpretationContext mic, EventEvaluatorModel model) {
        DefaultNestedComponentRegistry registry = mic.getDefaultNestedComponentRegistry();
        return registry.findDefaultComponentTypeByTag(model.getTag());
    }

    @Override
    public void postHandle(ModelInterpretationContext intercon, Model model) throws ModelHandlerException {
        Object o;
        if (this.inError) {
            return;
        }
        if (this.evaluator instanceof LifeCycle) {
            this.evaluator.start();
            this.addInfo("Starting evaluator named [" + this.evaluator.getName() + "]");
        }
        if ((o = intercon.peekObject()) != this.evaluator) {
            this.addWarn("The object on the top the of the stack is not the evaluator pushed earlier.");
        } else {
            intercon.popObject();
            try {
                Map evaluatorMap = (Map)this.context.getObject("EVALUATOR_MAP");
                if (evaluatorMap == null) {
                    this.addError("Could not find EvaluatorMap");
                } else {
                    evaluatorMap.put(this.evaluator.getName(), this.evaluator);
                }
            }
            catch (Exception ex) {
                this.addError("Could not set evaluator named [" + String.valueOf(this.evaluator) + "].", ex);
            }
        }
    }
}

