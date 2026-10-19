/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.sift;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.processor.DefaultProcessor;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;

public class SiftProcessor<E>
extends DefaultProcessor {
    public SiftProcessor(Context context, ModelInterpretationContext mic) {
        super(mic.getContext(), mic);
    }

    ModelInterpretationContext getModelInterpretationContext() {
        return this.mic;
    }
}

