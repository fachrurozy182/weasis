/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.util.PropertySetter;
import ch.qos.logback.core.joran.util.beans.BeanDescriptionCache;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.ParamModel;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;

public class ParamModelHandler
extends ModelHandlerBase {
    private final BeanDescriptionCache beanDescriptionCache;

    public ParamModelHandler(Context context, BeanDescriptionCache beanDescriptionCache) {
        super(context);
        this.beanDescriptionCache = beanDescriptionCache;
    }

    public static ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new ParamModelHandler(context, ic.getBeanDescriptionCache());
    }

    protected Class<ParamModel> getSupportedModelClass() {
        return ParamModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        ParamModel paramModel = (ParamModel)model;
        String valueStr = mic.subst(paramModel.getValue());
        Object o = mic.peekObject();
        PropertySetter propSetter = new PropertySetter(this.beanDescriptionCache, o);
        propSetter.setContext(this.context);
        String finalName = mic.subst(paramModel.getName());
        propSetter.setProperty(finalName, valueStr);
    }
}

