/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.boolex;

import ch.qos.logback.core.boolex.PropertyCondition;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.PropertyContainer;
import ch.qos.logback.core.util.OptionHelper;

public abstract class PropertyConditionBase
extends ContextAwareBase
implements PropertyCondition {
    boolean started;
    PropertyContainer localPropertyContainer;

    @Override
    public PropertyContainer getLocalPropertyContainer() {
        return this.localPropertyContainer;
    }

    @Override
    public void setLocalPropertyContainer(PropertyContainer aLocalPropertyContainer) {
        this.localPropertyContainer = aLocalPropertyContainer;
    }

    public boolean isNull(String k) {
        String val = OptionHelper.propertyLookup(k, this.localPropertyContainer, this.getContext());
        return val == null;
    }

    public boolean isDefined(String k) {
        String val = OptionHelper.propertyLookup(k, this.localPropertyContainer, this.getContext());
        return val != null;
    }

    public String p(String k) {
        return this.property(k);
    }

    public String property(String k) {
        String val = OptionHelper.propertyLookup(k, this.localPropertyContainer, this.getContext());
        if (val != null) {
            return val;
        }
        return "";
    }

    public boolean propertyEquals(String propertyKey, String value) {
        String actual = OptionHelper.propertyLookup(propertyKey, this.localPropertyContainer, this.getContext());
        if (actual == null || value == null) {
            return false;
        }
        return actual.equals(value);
    }

    public boolean propertyContains(String k, String inclusion) {
        String actual = OptionHelper.propertyLookup(k, this.localPropertyContainer, this.getContext());
        if (actual == null || inclusion == null) {
            return false;
        }
        return actual.contains(inclusion);
    }

    @Override
    public boolean isStarted() {
        return this.started;
    }

    @Override
    public void start() {
        this.started = true;
    }

    @Override
    public void stop() {
        this.started = false;
    }
}

