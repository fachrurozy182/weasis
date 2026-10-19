/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.hook.DefaultShutdownHook;
import ch.qos.logback.core.hook.ShutdownHook;
import ch.qos.logback.core.hook.ShutdownHookBase;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.ShutdownHookModel;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.util.ContextUtil;
import ch.qos.logback.core.util.DynamicClassLoadingException;
import ch.qos.logback.core.util.IncompatibleClassException;
import ch.qos.logback.core.util.OptionHelper;

public class ShutdownHookModelHandler
extends ModelHandlerBase {
    static final String OLD_SHUTDOWN_HOOK_CLASSNAME = "ch.qos.logback.core.hook.DelayingShutdownHook";
    static final String DEFAULT_SHUTDOWN_HOOK_CLASSNAME = DefaultShutdownHook.class.getName();
    public static final String RENAME_WARNING = "ch.qos.logback.core.hook.DelayingShutdownHook was renamed as " + DEFAULT_SHUTDOWN_HOOK_CLASSNAME;
    boolean inError = false;
    ShutdownHook hook = null;

    public ShutdownHookModelHandler(Context context) {
        super(context);
    }

    public static ModelHandlerBase makeInstance(Context context, ModelInterpretationContext mic) {
        return new ShutdownHookModelHandler(context);
    }

    protected Class<ShutdownHookModel> getSupportedModelClass() {
        return ShutdownHookModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) {
        ShutdownHookModel shutdownHookModel = (ShutdownHookModel)model;
        String className = shutdownHookModel.getClassName();
        if (OptionHelper.isNullOrEmptyOrAllSpaces(className)) {
            className = DEFAULT_SHUTDOWN_HOOK_CLASSNAME;
            this.addInfo("Assuming className [" + className + "]");
        } else if ((className = mic.getImport(className)).equals(OLD_SHUTDOWN_HOOK_CLASSNAME)) {
            className = DEFAULT_SHUTDOWN_HOOK_CLASSNAME;
            this.addWarn(RENAME_WARNING);
            this.addWarn("Please use the new class name");
        }
        this.addInfo("About to instantiate shutdown hook of type [" + className + "]");
        try {
            this.hook = (ShutdownHookBase)OptionHelper.instantiateByClassName(className, ShutdownHookBase.class, this.context);
            this.hook.setContext(this.context);
        }
        catch (DynamicClassLoadingException | IncompatibleClassException e) {
            this.addError("Could not create a shutdown hook of type [" + className + "].", e);
            this.inError = true;
            return;
        }
        mic.pushObject(this.hook);
    }

    @Override
    public void postHandle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        if (this.inError) {
            return;
        }
        Object o = mic.peekObject();
        if (o != this.hook) {
            this.addWarn("The object on the top the of the stack is not the hook object pushed earlier.");
        } else {
            ContextUtil contextUtil = new ContextUtil(this.context);
            contextUtil.addOrReplaceShutdownHook(this.hook);
            mic.popObject();
        }
    }
}

