/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.PropertyModel;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.model.util.PropertyModelHandlerHelper;

public class PropertyModelHandler
extends ModelHandlerBase {
    public PropertyModelHandler(Context context) {
        super(context);
    }

    public static ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new PropertyModelHandler(context);
    }

    protected Class<PropertyModel> getSupportedModelClass() {
        return PropertyModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) {
        PropertyModel propertyModel = (PropertyModel)model;
        PropertyModelHandlerHelper propertyModelHandlerHelper = new PropertyModelHandlerHelper(this);
        propertyModelHandlerHelper.setContext(this.context);
        propertyModelHandlerHelper.handlePropertyModel(mic, propertyModel);
    }
}

