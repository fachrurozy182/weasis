/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.ImcplicitActionDataForBasicProperty;
import ch.qos.logback.core.joran.action.ImplicitModelData;
import ch.qos.logback.core.joran.action.ImplicitModelDataForComplexProperty;
import ch.qos.logback.core.joran.spi.NoAutoStartUtil;
import ch.qos.logback.core.joran.util.PropertySetter;
import ch.qos.logback.core.joran.util.beans.BeanDescriptionCache;
import ch.qos.logback.core.model.ComponentModel;
import ch.qos.logback.core.model.ImplicitModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.util.AggregationType;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;

public class ImplicitModelHandler
extends ModelHandlerBase {
    private final BeanDescriptionCache beanDescriptionCache;
    private ImplicitModelData implicitModelData;
    public static final String IGNORING_UNKNOWN_PROP = "Ignoring unknown property";
    boolean inError = false;

    public ImplicitModelHandler(Context context, BeanDescriptionCache beanDescriptionCache) {
        super(context);
        this.beanDescriptionCache = beanDescriptionCache;
    }

    protected Class<? extends ImplicitModel> getSupportedModelClass() {
        return ImplicitModel.class;
    }

    public static ImplicitModelHandler makeInstance(Context context, ModelInterpretationContext mic) {
        BeanDescriptionCache beanDescriptionCache = mic.getBeanDescriptionCache();
        return new ImplicitModelHandler(context, beanDescriptionCache);
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) {
        ImplicitModel implicitModel = (ImplicitModel)model;
        if (mic.isObjectStackEmpty()) {
            this.inError = true;
            return;
        }
        String nestedElementTagName = implicitModel.getTag();
        Object o = mic.peekObject();
        PropertySetter parentBean = new PropertySetter(this.beanDescriptionCache, o);
        parentBean.setContext(this.context);
        AggregationType aggregationType = parentBean.computeAggregationType(nestedElementTagName);
        switch (aggregationType) {
            case NOT_FOUND: {
                this.addWarn("Ignoring unknown property [" + nestedElementTagName + "] in [" + o.getClass().getName() + "]");
                this.inError = true;
                implicitModel.markAsSkipped();
                return;
            }
            case AS_BASIC_PROPERTY: 
            case AS_BASIC_PROPERTY_COLLECTION: {
                ImcplicitActionDataForBasicProperty adBasicProperty = new ImcplicitActionDataForBasicProperty(parentBean, aggregationType, nestedElementTagName);
                this.implicitModelData = adBasicProperty;
                this.doBasicProperty(mic, implicitModel, adBasicProperty);
                return;
            }
            case AS_COMPLEX_PROPERTY_COLLECTION: 
            case AS_COMPLEX_PROPERTY: {
                Class<?> propertyType = parentBean.getTypeForComplexProperty(nestedElementTagName, aggregationType);
                ImplicitModelDataForComplexProperty imdForComplexProperty = new ImplicitModelDataForComplexProperty(parentBean, aggregationType, nestedElementTagName);
                imdForComplexProperty.setExpectedPropertyType(propertyType);
                this.implicitModelData = imdForComplexProperty;
                this.doComplex(mic, implicitModel, imdForComplexProperty);
                return;
            }
        }
        this.addError("PropertySetter.computeAggregationType returned " + String.valueOf((Object)aggregationType));
    }

    void doBasicProperty(ModelInterpretationContext interpretationContext, Model model, ImcplicitActionDataForBasicProperty actionData) {
        String finalBody = interpretationContext.subst(model.getBodyText());
        switch (actionData.aggregationType) {
            case AS_BASIC_PROPERTY: {
                actionData.parentBean.setProperty(actionData.propertyName, finalBody);
                break;
            }
            case AS_BASIC_PROPERTY_COLLECTION: {
                actionData.parentBean.addBasicProperty(actionData.propertyName, finalBody);
                break;
            }
            default: {
                this.addError("Unexpected aggregationType " + String.valueOf((Object)actionData.aggregationType));
            }
        }
    }

    public void doComplex(ModelInterpretationContext interpretationContext, ComponentModel componentModel, ImplicitModelDataForComplexProperty imdForComplexProperty) {
        String propertyClassName = componentModel.getClassName();
        String substPropertyClassName = interpretationContext.subst(propertyClassName);
        String fqcn = interpretationContext.getImport(substPropertyClassName);
        Class<?> propertyClass = null;
        try {
            if (!OptionHelper.isNullOrEmptyOrAllSpaces(fqcn)) {
                propertyClass = Loader.loadClass(fqcn, this.context);
            } else {
                PropertySetter parentBean = imdForComplexProperty.parentBean;
                propertyClass = parentBean.getClassNameViaImplicitRules(imdForComplexProperty.propertyName, imdForComplexProperty.getAggregationType(), interpretationContext.getDefaultNestedComponentRegistry());
            }
            if (propertyClass == null) {
                imdForComplexProperty.inError = true;
                String errMsg = "Could not find an appropriate class for property [" + componentModel.getTag() + "]";
                this.addError(errMsg);
                return;
            }
            if (OptionHelper.isNullOrEmptyOrAllSpaces(fqcn)) {
                this.addInfo("Assuming default type [" + propertyClass.getName() + "] for [" + componentModel.getTag() + "] property");
            }
            Class<?> expectedPropertyType = imdForComplexProperty.getExpectedPropertyType();
            Object object = OptionHelper.instantiateClassWithSuperclassRestriction(propertyClass, expectedPropertyType);
            imdForComplexProperty.setNestedComplexProperty(object);
            if (imdForComplexProperty.getNestedComplexProperty() instanceof ContextAware) {
                ((ContextAware)imdForComplexProperty.getNestedComplexProperty()).setContext(this.context);
            }
            interpretationContext.pushObject(imdForComplexProperty.getNestedComplexProperty());
        }
        catch (Exception oops) {
            imdForComplexProperty.inError = true;
            String msg = "Could not create component [" + componentModel.getTag() + "] of type [" + fqcn + "]";
            this.addError(msg, oops);
        }
    }

    @Override
    public void postHandle(ModelInterpretationContext intercon, Model model) {
        if (this.inError) {
            return;
        }
        if (this.implicitModelData == null) {
            return;
        }
        if (this.implicitModelData.inError) {
            return;
        }
        if (this.implicitModelData instanceof ImplicitModelDataForComplexProperty) {
            this.postHandleComplex(intercon, model, (ImplicitModelDataForComplexProperty)this.implicitModelData);
        }
    }

    private void postHandleComplex(ModelInterpretationContext mic, Model model, ImplicitModelDataForComplexProperty imdComplex) {
        Object o;
        Object nestedComplexProperty;
        PropertySetter nestedBean = new PropertySetter(this.beanDescriptionCache, imdComplex.getNestedComplexProperty());
        nestedBean.setContext(this.context);
        if (nestedBean.computeAggregationType("parent") == AggregationType.AS_COMPLEX_PROPERTY) {
            nestedBean.setComplexProperty("parent", imdComplex.parentBean.getObj());
        }
        if (NoAutoStartUtil.shouldBeStarted(nestedComplexProperty = imdComplex.getNestedComplexProperty())) {
            ((LifeCycle)nestedComplexProperty).start();
        }
        if ((o = mic.peekObject()) != imdComplex.getNestedComplexProperty()) {
            this.addError("The object on the top the of the stack is not the component pushed earlier.");
        } else {
            mic.popObject();
            switch (imdComplex.aggregationType) {
                case AS_COMPLEX_PROPERTY: {
                    imdComplex.parentBean.setComplexProperty(model.getTag(), imdComplex.getNestedComplexProperty());
                    break;
                }
                case AS_COMPLEX_PROPERTY_COLLECTION: {
                    imdComplex.parentBean.addComplexProperty(model.getTag(), imdComplex.getNestedComplexProperty());
                    break;
                }
                default: {
                    this.addError("Unexpected aggregationType " + String.valueOf((Object)imdComplex.aggregationType));
                }
            }
        }
    }
}

