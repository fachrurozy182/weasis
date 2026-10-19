/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.action;

import ch.qos.logback.core.joran.action.ImplicitModelData;
import ch.qos.logback.core.joran.util.PropertySetter;
import ch.qos.logback.core.util.AggregationType;

public class ImplicitModelDataForComplexProperty
extends ImplicitModelData {
    private Object nestedComplexProperty;
    private Class<?> expectedPropertyType;

    public ImplicitModelDataForComplexProperty(PropertySetter parentBean, AggregationType aggregationType, String propertyName) {
        super(parentBean, aggregationType, propertyName);
    }

    public Object getNestedComplexProperty() {
        return this.nestedComplexProperty;
    }

    public Class<?> getExpectedPropertyType() {
        return this.expectedPropertyType;
    }

    public void setExpectedPropertyType(Class<?> expectedPropertyType) {
        this.expectedPropertyType = expectedPropertyType;
    }

    public void setNestedComplexProperty(Object nestedComplexProperty) {
        this.nestedComplexProperty = nestedComplexProperty;
    }
}

