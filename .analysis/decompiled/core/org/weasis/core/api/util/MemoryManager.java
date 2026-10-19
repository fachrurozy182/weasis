/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.util;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.weasis.core.api.util.NativeMemoryConsumer;
import org.weasis.core.api.util.SystemMemory;

public final class MemoryManager {
    private static final MemoryManager INSTANCE = new MemoryManager();
    private final Set<NativeMemoryConsumer> consumers = ConcurrentHashMap.newKeySet();
    private final long nativeBudget = SystemMemory.getNativeMemoryBudget();

    private MemoryManager() {
    }

    public static MemoryManager getInstance() {
        return INSTANCE;
    }

    public void register(NativeMemoryConsumer consumer) {
        if (consumer != null) {
            this.consumers.add(consumer);
        }
    }

    public void unregister(NativeMemoryConsumer consumer) {
        this.consumers.remove(consumer);
    }

    public long getNativeBudget() {
        return this.nativeBudget;
    }

    public long getUsedNativeMemory() {
        long used = 0L;
        for (NativeMemoryConsumer consumer : this.consumers) {
            used += Math.max(0L, consumer.usedNativeMemory());
        }
        return used;
    }

    public long getAvailableNativeMemory() {
        return Math.max(0L, this.nativeBudget - this.getUsedNativeMemory());
    }

    public boolean isMemoryAvailable() {
        return this.getUsedNativeMemory() < this.nativeBudget;
    }

    public double getPressure() {
        return this.nativeBudget <= 0L ? 1.0 : (double)this.getUsedNativeMemory() / (double)this.nativeBudget;
    }
}

