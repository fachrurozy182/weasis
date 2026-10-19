/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.boolex;

import ch.qos.logback.core.boolex.PropertyConditionBase;

public class PropertyEqualityCondition
extends PropertyConditionBase {
    String key;
    String value;

    @Override
    public void start() {
        if (this.key == null) {
            this.addError("In PropertyEqualsValue 'key' parameter cannot be null");
            return;
        }
        if (this.value == null) {
            this.addError("In PropertyEqualsValue 'value' parameter cannot be null");
            return;
        }
        super.start();
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public boolean evaluate() {
        if (this.key == null) {
            this.addError("key cannot be null");
            return false;
        }
        String val = this.p(this.key);
        if (val == null) {
            return false;
        }
        return val.equals(this.value);
    }
}

