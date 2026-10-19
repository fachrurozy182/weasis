/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.ActionUtil;
import ch.qos.logback.core.model.DefineModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.spi.PropertyDefiner;
import ch.qos.logback.core.util.OptionHelper;

public class DefineModelHandler
extends ModelHandlerBase {
    boolean inError;
    PropertyDefiner definer;
    String propertyName;
    ActionUtil.Scope scope;

    public DefineModelHandler(Context context) {
        super(context);
    }

    public static DefineModelHandler makeInstance(Context context, ModelInterpretationContext ic) {
        return new DefineModelHandler(context);
    }

    protected Class<DefineModel> getSupportedModelClass() {
        return DefineModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext interpretationContext, Model model) throws ModelHandlerException {
        String className;
        this.definer = null;
        this.inError = false;
        this.propertyName = null;
        DefineModel defineModel = (DefineModel)model;
        this.propertyName = defineModel.getName();
        String scopeStr = defineModel.getScopeStr();
        this.scope = ActionUtil.stringToScope(scopeStr);
        if (OptionHelper.isNullOrEmptyOrAllSpaces(this.propertyName)) {
            this.addError("Missing property name for property definer. Near [" + model.getTag() + "] line " + model.getLineNumber());
            this.inError = true;
        }
        if (OptionHelper.isNullOrEmptyOrAllSpaces(className = defineModel.getClassName())) {
            this.addError("Missing class name for property definer. Near [" + model.getTag() + "] line " + model.getLineNumber());
            this.inError = true;
        } else {
            className = interpretationContext.getImport(className);
        }
        if (this.inError) {
            return;
        }
        try {
            this.addInfo("About to instantiate property definer of type [" + className + "]");
            this.definer = (PropertyDefiner)OptionHelper.instantiateByClassName(className, PropertyDefiner.class, this.context);
            this.definer.setContext(this.context);
            interpretationContext.pushObject(this.definer);
        }
        catch (Exception oops) {
            this.inError = true;
            this.addError("Could not create an PropertyDefiner of type [" + className + "].", oops);
            throw new ModelHandlerException(oops);
        }
    }

    @Override
    public void postHandle(ModelInterpretationContext interpretationContext, Model model) throws ModelHandlerException {
        if (this.inError) {
            return;
        }
        Object o = interpretationContext.peekObject();
        if (o != this.definer) {
            this.addWarn("The object at the of the stack is not the property definer for property named [" + this.propertyName + "] pushed earlier.");
        } else {
            String propertyValue;
            interpretationContext.popObject();
            if (this.definer instanceof LifeCycle) {
                ((LifeCycle)((Object)this.definer)).start();
            }
            if ((propertyValue = this.definer.getPropertyValue()) != null) {
                this.addInfo("Setting property " + this.propertyName + "=" + propertyValue + " in scope " + String.valueOf((Object)this.scope));
                ActionUtil.setProperty(interpretationContext, this.propertyName, propertyValue, this.scope);
            }
        }
    }
}

