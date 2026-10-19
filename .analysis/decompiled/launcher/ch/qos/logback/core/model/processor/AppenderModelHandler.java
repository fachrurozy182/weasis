/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.AppenderModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.util.OptionHelper;
import java.util.Map;

public class AppenderModelHandler<E>
extends ModelHandlerBase {
    Appender<E> appender;
    private boolean inError = false;
    private boolean skipped = false;
    AppenderAttachable<E> appenderAttachable;

    public AppenderModelHandler(Context context) {
        super(context);
    }

    public static ModelHandlerBase makeInstance(Context context, ModelInterpretationContext mic) {
        return new AppenderModelHandler(context);
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        this.appender = null;
        this.inError = false;
        AppenderModel appenderModel = (AppenderModel)model;
        String appenderName = mic.subst(appenderModel.getName());
        if (!mic.hasDependers(appenderName)) {
            this.addWarn("Appender named [" + appenderName + "] not referenced. Skipping further processing.");
            this.skipped = true;
            appenderModel.markAsSkipped();
            return;
        }
        this.addInfo("Processing appender named [" + appenderName + "]");
        String originalClassName = appenderModel.getClassName();
        String className = mic.getImport(originalClassName);
        try {
            this.addInfo("About to instantiate appender of type [" + className + "]");
            this.appender = (Appender)OptionHelper.instantiateByClassName(className, Appender.class, this.context);
            this.appender.setContext(this.context);
            this.appender.setName(appenderName);
            mic.pushObject(this.appender);
        }
        catch (Exception oops) {
            this.inError = true;
            this.addError("Could not create an Appender of type [" + className + "].", oops);
            throw new ModelHandlerException(oops);
        }
    }

    @Override
    public void postHandle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        if (this.inError || this.skipped) {
            return;
        }
        if (this.appender instanceof LifeCycle) {
            this.appender.start();
        }
        mic.markStartOfNamedDependee(this.appender.getName());
        Object o = mic.peekObject();
        Map appenderBag = (Map)mic.getObjectMap().get("APPENDER_BAG");
        appenderBag.put(this.appender.getName(), this.appender);
        if (o != this.appender) {
            this.addWarn("The object at the of the stack is not the appender named [" + this.appender.getName() + "] pushed earlier.");
        } else {
            mic.popObject();
        }
    }
}

