/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.spi;

import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.SequenceNumberGenerator;
import java.util.concurrent.atomic.AtomicLong;

public class BasicSequenceNumberGenerator
extends ContextAwareBase
implements SequenceNumberGenerator {
    private final AtomicLong atomicLong = new AtomicLong();

    @Override
    public long nextSequenceNumber() {
        return this.atomicLong.incrementAndGet();
    }
}

