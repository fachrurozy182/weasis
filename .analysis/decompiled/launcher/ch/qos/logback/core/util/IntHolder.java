/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.util;

public class IntHolder {
    public int value;

    public IntHolder(int value) {
        this.value = value;
    }

    public void inc() {
        ++this.value;
    }

    public void set(int newValue) {
        this.value = newValue;
    }

    public int get() {
        return this.value;
    }
}

