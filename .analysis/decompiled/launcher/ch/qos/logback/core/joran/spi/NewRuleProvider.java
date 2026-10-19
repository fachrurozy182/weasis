/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.spi;

import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.model.processor.DefaultProcessor;

public interface NewRuleProvider {
    public void addPathActionAssociations(RuleStore var1);

    public void addModelHandlerAssociations(DefaultProcessor var1);

    public void addModelAnalyserAssociations(DefaultProcessor var1);
}

