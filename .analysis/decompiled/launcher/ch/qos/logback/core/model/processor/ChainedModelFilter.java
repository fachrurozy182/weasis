/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.AllowAllModelFilter;
import ch.qos.logback.core.model.processor.AllowModelFilter;
import ch.qos.logback.core.model.processor.DenyAllModelFilter;
import ch.qos.logback.core.model.processor.DenyModelFilter;
import ch.qos.logback.core.model.processor.ModelFilter;
import ch.qos.logback.core.spi.FilterReply;
import java.util.ArrayList;
import java.util.List;

public class ChainedModelFilter
implements ModelFilter {
    List<ModelFilter> modelFilters = new ArrayList<ModelFilter>();

    public static ChainedModelFilter newInstance() {
        return new ChainedModelFilter();
    }

    public ChainedModelFilter allow(Class<? extends Model> allowedType) {
        this.modelFilters.add(new AllowModelFilter(allowedType));
        return this;
    }

    public ChainedModelFilter deny(Class<? extends Model> allowedType) {
        this.modelFilters.add(new DenyModelFilter(allowedType));
        return this;
    }

    public ChainedModelFilter denyAll() {
        this.modelFilters.add(new DenyAllModelFilter());
        return this;
    }

    public ChainedModelFilter allowAll() {
        this.modelFilters.add(new AllowAllModelFilter());
        return this;
    }

    @Override
    public FilterReply decide(Model model) {
        for (ModelFilter modelFilter : this.modelFilters) {
            FilterReply reply = modelFilter.decide(model);
            switch (reply) {
                case ACCEPT: 
                case DENY: {
                    return reply;
                }
            }
        }
        return FilterReply.NEUTRAL;
    }
}

