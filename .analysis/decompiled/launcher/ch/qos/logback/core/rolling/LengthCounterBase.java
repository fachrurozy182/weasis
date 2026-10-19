/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.rolling;

import ch.qos.logback.core.rolling.LengthCounter;
import java.util.concurrent.atomic.LongAdder;

public class LengthCounterBase
implements LengthCounter {
    LongAdder counter = new LongAdder();

    @Override
    public void add(long len) {
        this.counter.add(len);
    }

    @Override
    public long getLength() {
        return this.counter.longValue();
    }

    @Override
    public void reset() {
        this.counter.reset();
    }
}

