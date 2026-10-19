/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelFilter;
import ch.qos.logback.core.spi.FilterReply;

public class AllowModelFilter
implements ModelFilter {
    final Class<? extends Model> allowedModelType;

    AllowModelFilter(Class<? extends Model> allowedType) {
        this.allowedModelType = allowedType;
    }

    @Override
    public FilterReply decide(Model model) {
        if (model.getClass() == this.allowedModelType) {
            return FilterReply.ACCEPT;
        }
        return FilterReply.NEUTRAL;
    }
}

