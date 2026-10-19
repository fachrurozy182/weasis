/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.util;

import ch.qos.logback.core.util.Duration;
import ch.qos.logback.core.util.InvocationGate;
import java.util.concurrent.atomic.AtomicLong;

public class SimpleInvocationGate
implements InvocationGate {
    AtomicLong atomicNext = new AtomicLong(0L);
    final Duration increment;
    public static final Duration DEFAULT_INCREMENT = Duration.buildBySeconds(60.0);

    public SimpleInvocationGate() {
        this(DEFAULT_INCREMENT);
    }

    public SimpleInvocationGate(Duration anIncrement) {
        this.increment = anIncrement;
    }

    @Override
    public boolean isTooSoon(long currentTime) {
        if (currentTime == -1L) {
            return false;
        }
        long localNext = this.atomicNext.get();
        if (currentTime >= localNext) {
            long next2 = currentTime + this.increment.getMilliseconds();
            boolean success = this.atomicNext.compareAndSet(localNext, next2);
            return !success;
        }
        return true;
    }
}

