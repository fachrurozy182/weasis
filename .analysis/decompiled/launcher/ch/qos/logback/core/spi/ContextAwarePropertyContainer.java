/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.spi;

import ch.qos.logback.core.joran.GenericXMLConfigurator;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.PropertyContainer;
import java.util.function.Supplier;

public interface ContextAwarePropertyContainer
extends PropertyContainer,
ContextAware {
    public String subst(String var1);

    default public Supplier<? extends GenericXMLConfigurator> getConfiguratorSupplier() {
        return null;
    }
}

