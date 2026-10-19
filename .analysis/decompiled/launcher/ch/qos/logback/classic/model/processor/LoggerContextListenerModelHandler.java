/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.model.processor;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.model.LoggerContextListenerModel;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.util.OptionHelper;

public class LoggerContextListenerModelHandler
extends ModelHandlerBase {
    boolean inError = false;
    LoggerContextListener lcl;

    public LoggerContextListenerModelHandler(Context context) {
        super(context);
    }

    public static ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new LoggerContextListenerModelHandler(context);
    }

    protected Class<LoggerContextListenerModel> getSupportedModelClass() {
        return LoggerContextListenerModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        LoggerContextListenerModel lclModel = (LoggerContextListenerModel)model;
        String className = lclModel.getClassName();
        if (OptionHelper.isNullOrEmptyOrAllSpaces(className)) {
            this.addError("Empty class name for LoggerContextListener");
            this.inError = true;
        } else {
            className = mic.getImport(className);
        }
        try {
            this.lcl = (LoggerContextListener)OptionHelper.instantiateByClassName(className, LoggerContextListener.class, this.context);
            if (this.lcl instanceof ContextAware) {
                ((ContextAware)((Object)this.lcl)).setContext(this.context);
            }
            mic.pushObject(this.lcl);
            this.addInfo("Adding LoggerContextListener of type [" + className + "] to the object stack");
        }
        catch (Exception oops) {
            this.inError = true;
            this.addError("Could not create LoggerContextListener of type " + className + "].", oops);
        }
    }

    @Override
    public void postHandle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        if (this.inError) {
            return;
        }
        Object o = mic.peekObject();
        if (o != this.lcl) {
            this.addWarn("The object on the top the of the stack is not the LoggerContextListener pushed earlier.");
        } else {
            if (this.lcl instanceof LifeCycle) {
                ((LifeCycle)((Object)this.lcl)).start();
                this.addInfo("Starting LoggerContextListener");
            }
            ((LoggerContext)this.context).addListener(this.lcl);
            mic.popObject();
        }
    }
}

