/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.util;

import ch.qos.logback.core.joran.spi.DefaultClass;
import ch.qos.logback.core.joran.spi.DefaultNestedComponentRegistry;
import ch.qos.logback.core.joran.util.StringToObjectConverter;
import ch.qos.logback.core.joran.util.beans.BeanDescription;
import ch.qos.logback.core.joran.util.beans.BeanDescriptionCache;
import ch.qos.logback.core.joran.util.beans.BeanUtil;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.AggregationType;
import ch.qos.logback.core.util.StringUtil;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AggregationAssessor
extends ContextAwareBase {
    protected final Class<?> objClass;
    protected final BeanDescription beanDescription;

    public AggregationAssessor(BeanDescriptionCache beanDescriptionCache, Class objClass) {
        this.objClass = objClass;
        this.beanDescription = beanDescriptionCache.getBeanDescription(objClass);
    }

    public AggregationType computeAggregationType(String name) {
        Method setter;
        String cName = StringUtil.capitalizeFirstLetter(name);
        Method addMethod = this.findAdderMethod(cName);
        if (addMethod != null) {
            AggregationType type = this.computeRawAggregationType(addMethod);
            switch (type) {
                case NOT_FOUND: {
                    return AggregationType.NOT_FOUND;
                }
                case AS_BASIC_PROPERTY: {
                    return AggregationType.AS_BASIC_PROPERTY_COLLECTION;
                }
                case AS_COMPLEX_PROPERTY: {
                    return AggregationType.AS_COMPLEX_PROPERTY_COLLECTION;
                }
                case AS_BASIC_PROPERTY_COLLECTION: 
                case AS_COMPLEX_PROPERTY_COLLECTION: {
                    this.addError("Unexpected AggregationType " + String.valueOf((Object)type));
                }
            }
        }
        if ((setter = this.findSetterMethod(name)) != null) {
            return this.computeRawAggregationType(setter);
        }
        return AggregationType.NOT_FOUND;
    }

    public Method findAdderMethod(String name) {
        String propertyName = BeanUtil.toLowerCamelCase(name);
        return this.beanDescription.getAdder(propertyName);
    }

    public Method findSetterMethod(String name) {
        String propertyName = BeanUtil.toLowerCamelCase(name);
        return this.beanDescription.getSetter(propertyName);
    }

    private AggregationType computeRawAggregationType(Method method) {
        Class<?> parameterClass = this.getParameterClassForMethod(method);
        if (parameterClass == null) {
            return AggregationType.NOT_FOUND;
        }
        if (StringToObjectConverter.canBeBuiltFromSimpleString(parameterClass)) {
            return AggregationType.AS_BASIC_PROPERTY;
        }
        return AggregationType.AS_COMPLEX_PROPERTY;
    }

    private Class<?> getParameterClassForMethod(Method method) {
        if (method == null) {
            return null;
        }
        Class<?>[] classArray = method.getParameterTypes();
        if (classArray.length != 1) {
            return null;
        }
        return classArray[0];
    }

    public Class<?> getClassNameViaImplicitRules(String name, AggregationType aggregationType, DefaultNestedComponentRegistry registry) {
        Class<?> registryResult = registry.findDefaultComponentType(this.objClass, name);
        if (registryResult != null) {
            return registryResult;
        }
        Method relevantMethod = this.getRelevantMethod(name, aggregationType);
        if (relevantMethod == null) {
            return null;
        }
        Class<?> byAnnotation = this.getDefaultClassNameByAnnonation(name, relevantMethod);
        if (byAnnotation != null) {
            return byAnnotation;
        }
        return this.getByConcreteType(name, relevantMethod);
    }

    <T extends Annotation> T getAnnotation(String name, Class<T> annonationClass, Method relevantMethod) {
        if (relevantMethod != null) {
            return relevantMethod.getAnnotation(annonationClass);
        }
        return null;
    }

    Class<?> getDefaultClassNameByAnnonation(String name, Method relevantMethod) {
        DefaultClass defaultClassAnnon = this.getAnnotation(name, DefaultClass.class, relevantMethod);
        if (defaultClassAnnon != null) {
            return defaultClassAnnon.value();
        }
        return null;
    }

    Method getRelevantMethod(String name, AggregationType aggregationType) {
        Method relevantMethod;
        if (aggregationType == AggregationType.AS_COMPLEX_PROPERTY_COLLECTION) {
            relevantMethod = this.findAdderMethod(name);
        } else if (aggregationType == AggregationType.AS_COMPLEX_PROPERTY) {
            relevantMethod = this.findSetterMethod(name);
        } else {
            throw new IllegalStateException(String.valueOf((Object)aggregationType) + " not allowed here");
        }
        return relevantMethod;
    }

    Class<?> getByConcreteType(String name, Method relevantMethod) {
        Class<?> paramType = this.getParameterClassForMethod(relevantMethod);
        if (paramType == null) {
            return null;
        }
        boolean isUnequivocallyInstantiable = this.isUnequivocallyInstantiable(paramType);
        if (isUnequivocallyInstantiable) {
            return paramType;
        }
        return null;
    }

    private boolean isUnequivocallyInstantiable(Class<?> clazz) {
        if (clazz.isInterface()) {
            return false;
        }
        try {
            Object o = clazz.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
            return o != null;
        }
        catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            return false;
        }
    }
}

