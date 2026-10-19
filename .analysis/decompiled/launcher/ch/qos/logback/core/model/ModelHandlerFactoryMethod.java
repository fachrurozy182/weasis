/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;

@FunctionalInterface
public interface ModelHandlerFactoryMethod {
    public ModelHandlerBase make(Context var1, ModelInterpretationContext var2);
}

