/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.util;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.DefaultNestedComponentRegistry;
import ch.qos.logback.core.joran.util.AggregationAssessor;
import ch.qos.logback.core.joran.util.StringToObjectConverter;
import ch.qos.logback.core.joran.util.beans.BeanDescription;
import ch.qos.logback.core.joran.util.beans.BeanDescriptionCache;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.AggregationType;
import ch.qos.logback.core.util.PropertySetterException;
import ch.qos.logback.core.util.StringUtil;
import java.lang.reflect.Method;

public class PropertySetter
extends ContextAwareBase {
    protected final Object obj;
    protected final Class<?> objClass;
    protected final BeanDescription beanDescription;
    protected final AggregationAssessor aggregationAssessor;

    public PropertySetter(BeanDescriptionCache beanDescriptionCache, Object obj) {
        this.obj = obj;
        this.objClass = obj.getClass();
        this.beanDescription = beanDescriptionCache.getBeanDescription(this.objClass);
        this.aggregationAssessor = new AggregationAssessor(beanDescriptionCache, this.objClass);
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        this.aggregationAssessor.setContext(context);
    }

    public void setProperty(String name, String value) {
        if (value == null) {
            return;
        }
        Method setter = this.aggregationAssessor.findSetterMethod(name);
        if (setter == null) {
            this.addWarn("No setter for property [" + name + "] in " + this.objClass.getName() + ".");
        } else {
            try {
                this.setProperty(setter, value);
            }
            catch (PropertySetterException ex) {
                this.addWarn("Failed to set property [" + name + "] to value \"" + value + "\". ", ex);
            }
        }
    }

    private void setProperty(Method setter, String value) throws PropertySetterException {
        Object arg;
        Class<?>[] paramTypes = setter.getParameterTypes();
        try {
            arg = StringToObjectConverter.convertArg(this, value, paramTypes[0]);
        }
        catch (Throwable t) {
            throw new PropertySetterException("Conversion to type [" + String.valueOf(paramTypes[0]) + "] failed. ", t);
        }
        if (arg == null) {
            throw new PropertySetterException("Conversion to type [" + String.valueOf(paramTypes[0]) + "] failed.");
        }
        try {
            setter.invoke(this.obj, arg);
        }
        catch (Exception ex) {
            throw new PropertySetterException(ex);
        }
    }

    public AggregationType computeAggregationType(String name) {
        return this.aggregationAssessor.computeAggregationType(name);
    }

    public Class<?> getObjClass() {
        return this.objClass;
    }

    public void addComplexProperty(String name, Object complexProperty) {
        Method adderMethod = this.aggregationAssessor.findAdderMethod(name);
        if (adderMethod != null) {
            Class<?>[] paramTypes = adderMethod.getParameterTypes();
            if (!this.isSanityCheckSuccessful(name, adderMethod, paramTypes, complexProperty)) {
                return;
            }
            this.invokeMethodWithSingleParameterOnThisObject(adderMethod, complexProperty);
        } else {
            this.addError("Could not find method [add" + name + "] in class [" + this.objClass.getName() + "].");
        }
    }

    void invokeMethodWithSingleParameterOnThisObject(Method method, Object parameter) {
        Class<?> ccc = parameter.getClass();
        try {
            method.invoke(this.obj, parameter);
        }
        catch (Exception e) {
            this.addError("Could not invoke method " + method.getName() + " in class " + this.obj.getClass().getName() + " with parameter of type " + ccc.getName(), e);
        }
    }

    public void addBasicProperty(String name, String strValue) {
        Object arg;
        if (strValue == null) {
            return;
        }
        Method adderMethod = this.aggregationAssessor.findAdderMethod(name = StringUtil.capitalizeFirstLetter(name));
        if (adderMethod == null) {
            this.addError("No adder for property [" + name + "].");
            return;
        }
        Class<?>[] paramTypes = adderMethod.getParameterTypes();
        this.isSanityCheckSuccessful(name, adderMethod, paramTypes, strValue);
        try {
            arg = StringToObjectConverter.convertArg(this, strValue, paramTypes[0]);
        }
        catch (Throwable t) {
            this.addError("Conversion to type [" + String.valueOf(paramTypes[0]) + "] failed. ", t);
            return;
        }
        if (arg != null) {
            this.invokeMethodWithSingleParameterOnThisObject(adderMethod, arg);
        }
    }

    public void setComplexProperty(String name, Object complexProperty) {
        Method setter = this.aggregationAssessor.findSetterMethod(name);
        if (setter == null) {
            this.addWarn("Not setter method for property [" + name + "] in " + this.obj.getClass().getName());
            return;
        }
        Class<?>[] paramTypes = setter.getParameterTypes();
        if (!this.isSanityCheckSuccessful(name, setter, paramTypes, complexProperty)) {
            return;
        }
        try {
            this.invokeMethodWithSingleParameterOnThisObject(setter, complexProperty);
        }
        catch (Exception e) {
            this.addError("Could not set component " + String.valueOf(this.obj) + " for parent component " + String.valueOf(this.obj), e);
        }
    }

    private boolean isSanityCheckSuccessful(String name, Method method, Class<?>[] params, Object complexProperty) {
        Class<?> ccc = complexProperty.getClass();
        if (params.length != 1) {
            this.addError("Wrong number of parameters in setter method for property [" + name + "] in " + this.obj.getClass().getName());
            return false;
        }
        if (!params[0].isAssignableFrom(complexProperty.getClass())) {
            this.addError("A \"" + ccc.getName() + "\" object is not assignable to a \"" + params[0].getName() + "\" variable.");
            this.addError("The class \"" + params[0].getName() + "\" was loaded by ");
            this.addError("[" + String.valueOf(params[0].getClassLoader()) + "] whereas object of type ");
            this.addError("\"" + ccc.getName() + "\" was loaded by [" + String.valueOf(ccc.getClassLoader()) + "].");
            return false;
        }
        return true;
    }

    public Object getObj() {
        return this.obj;
    }

    public Class<?> getClassNameViaImplicitRules(String name, AggregationType aggregationType, DefaultNestedComponentRegistry registry) {
        return this.aggregationAssessor.getClassNameViaImplicitRules(name, aggregationType, registry);
    }

    public Class<?> getTypeForComplexProperty(String nestedElementTagName, AggregationType aggregationType) {
        Method aMethod = null;
        switch (aggregationType) {
            case AS_COMPLEX_PROPERTY: {
                aMethod = this.aggregationAssessor.findSetterMethod(nestedElementTagName);
                break;
            }
            case AS_COMPLEX_PROPERTY_COLLECTION: {
                aMethod = this.aggregationAssessor.findAdderMethod(nestedElementTagName);
            }
        }
        this.checkParameterCount(aMethod, nestedElementTagName);
        Class<?>[] paramTypes = aMethod.getParameterTypes();
        return paramTypes[0];
    }

    private void checkParameterCount(Method aMethod, String nestedElementTagName) {
        if (aMethod == null) {
            String msg = "Could not find method for property [" + nestedElementTagName + "].";
            this.addError(msg);
            throw new IllegalStateException(msg);
        }
        int parameterCount = aMethod.getParameterCount();
        if (parameterCount != 1) {
            String msg = "Expected [" + aMethod.getName() + "] for property [" + nestedElementTagName + "] to have exactly one parameter.";
            this.addError(msg);
            throw new IllegalStateException(msg);
        }
    }
}

