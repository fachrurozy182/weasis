/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.StatusListenerModel;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.util.OptionHelper;

public class StatusListenerModelHandler
extends ModelHandlerBase {
    boolean inError = false;
    Boolean effectivelyAdded = null;
    StatusListener statusListener = null;

    public StatusListenerModelHandler(Context context) {
        super(context);
    }

    public static ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new StatusListenerModelHandler(context);
    }

    protected Class<StatusListenerModel> getSupportedModelClass() {
        return StatusListenerModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext ic, Model model) throws ModelHandlerException {
        StatusListenerModel slModel = (StatusListenerModel)model;
        String className = slModel.getClassName();
        if (OptionHelper.isNullOrEmptyOrAllSpaces(className)) {
            this.addError("Empty class name for StatusListener");
            this.inError = true;
            return;
        }
        className = ic.getImport(className);
        try {
            this.statusListener = (StatusListener)OptionHelper.instantiateByClassName(className, StatusListener.class, this.context);
            this.effectivelyAdded = ic.getContext().getStatusManager().add(this.statusListener);
            if (this.statusListener instanceof ContextAware) {
                ((ContextAware)((Object)this.statusListener)).setContext(this.context);
            }
            this.addInfo("Added status listener of type [" + slModel.getClassName() + "]");
            ic.pushObject(this.statusListener);
        }
        catch (Exception e) {
            this.inError = true;
            this.addError("Could not create an StatusListener of type [" + slModel.getClassName() + "].", e);
            throw new ModelHandlerException(e);
        }
    }

    @Override
    public void postHandle(ModelInterpretationContext mic, Model m) {
        Object o;
        if (this.inError) {
            return;
        }
        if (this.isEffectivelyAdded() && this.statusListener instanceof LifeCycle) {
            ((LifeCycle)((Object)this.statusListener)).start();
        }
        if ((o = mic.peekObject()) != this.statusListener) {
            this.addWarn("The object at the of the stack is not the statusListener pushed earlier.");
        } else {
            mic.popObject();
        }
    }

    private boolean isEffectivelyAdded() {
        if (this.effectivelyAdded == null) {
            return false;
        }
        return this.effectivelyAdded;
    }
}

