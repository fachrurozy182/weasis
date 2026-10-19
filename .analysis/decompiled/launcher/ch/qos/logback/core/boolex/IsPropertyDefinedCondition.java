/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.boolex;

import ch.qos.logback.core.boolex.PropertyConditionBase;

public class IsPropertyDefinedCondition
extends PropertyConditionBase {
    String key;

    @Override
    public void start() {
        if (this.key == null) {
            this.addError("In IsPropertyDefinedEvaluator 'key' parameter cannot be null");
            return;
        }
        super.start();
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public boolean evaluate() {
        return this.isDefined(this.key);
    }
}

