/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.spi;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ElementPath;
import ch.qos.logback.core.joran.spi.ElementSelector;
import java.util.function.Supplier;

public interface RuleStore {
    public void addRule(ElementSelector var1, String var2) throws ClassNotFoundException;

    public void addRule(ElementSelector var1, Supplier<Action> var2);

    public Supplier<Action> matchActions(ElementPath var1);

    public void addTransparentPathPart(String var1);

    public void addPathPathMapping(String var1, String var2);
}

