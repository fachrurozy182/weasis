/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.rolling;

public interface LengthCounter {
    public void add(long var1);

    public long getLength();

    public void reset();
}

