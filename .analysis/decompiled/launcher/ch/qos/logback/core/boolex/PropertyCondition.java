/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.boolex;

import ch.qos.logback.core.joran.conditional.Condition;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.spi.PropertyContainer;

public interface PropertyCondition
extends Condition,
ContextAware,
LifeCycle {
    public PropertyContainer getLocalPropertyContainer();

    public void setLocalPropertyContainer(PropertyContainer var1);
}

