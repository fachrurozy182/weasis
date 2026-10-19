/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.opencv.data.PlanarImage
 */
package org.weasis.core.api.media.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.weasis.core.api.util.MemoryManager;
import org.weasis.core.api.util.NativeMemoryConsumer;
import org.weasis.core.api.util.ResourceMonitor;
import org.weasis.opencv.data.PlanarImage;

public abstract class NativeCache<K, V extends PlanarImage>
implements NativeMemoryConsumer {
    protected final Map<K, V> hash;
    private final Map<K, Integer> pinCount;
    private final long maxNativeMemory;
    private final AtomicLong useNativeMemory;

    protected NativeCache(long maxNativeMemory) {
        this.maxNativeMemory = maxNativeMemory;
        this.useNativeMemory = new AtomicLong(0L);
        this.hash = Collections.synchronizedMap(new LinkedHashMap(64, 0.75f, true));
        this.pinCount = new ConcurrentHashMap<K, Integer>();
        MemoryManager.getInstance().register(this);
    }

    @Override
    public long usedNativeMemory() {
        return this.useNativeMemory.get();
    }

    public V get(K key) {
        return (V)((PlanarImage)this.hash.get(key));
    }

    public boolean isMemoryAvailable() {
        return this.useNativeMemory.get() < this.maxNativeMemory && MemoryManager.getInstance().isMemoryAvailable();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void expungeStaleEntries() {
        if (!this.isMemoryAvailable()) {
            Map<K, V> map = this.hash;
            synchronized (map) {
                ArrayList<K> remKeys = new ArrayList<K>();
                MemoryManager memoryManager = MemoryManager.getInstance();
                long localOverage = this.useNativeMemory.get() - this.maxNativeMemory;
                long globalOverage = memoryManager.getUsedNativeMemory() - memoryManager.getNativeBudget();
                long maxFreeSize = this.maxNativeMemory / 20L + Math.max(0L, Math.max(localOverage, globalOverage));
                long freeSize = 0L;
                for (Map.Entry<K, V> e : this.hash.entrySet()) {
                    K key = e.getKey();
                    if (this.isPinned(key)) continue;
                    if ((freeSize += this.physicalBytes((PlanarImage)e.getValue())) > maxFreeSize) break;
                    remKeys.add(key);
                }
                for (Map.Entry<K, V> key : remKeys) {
                    PlanarImage val = (PlanarImage)this.hash.remove(key);
                    this.useNativeMemory.addAndGet(-this.physicalBytes(val));
                    this.afterEntryRemove(key, val);
                }
                ResourceMonitor.getInstance().recordCacheEviction(remKeys.size());
            }
        }
    }

    private long physicalBytes(V val) {
        if (val != null) {
            return val.physicalBytes();
        }
        return 0L;
    }

    protected abstract void afterEntryRemove(K var1, V var2);

    public void put(K key, V value) {
        this.expungeStaleEntries();
        PlanarImage previous = (PlanarImage)this.hash.put(key, value);
        this.useNativeMemory.addAndGet(this.physicalBytes(value));
        this.useNativeMemory.addAndGet(-this.physicalBytes(previous));
    }

    public void remove(K key) {
        PlanarImage val = (PlanarImage)this.hash.remove(key);
        this.useNativeMemory.addAndGet(-this.physicalBytes(val));
        this.afterEntryRemove(key, val);
    }

    public void pin(K key) {
        if (key != null) {
            this.pinCount.merge(key, 1, Integer::sum);
        }
    }

    public void unpin(K key) {
        if (key != null) {
            this.pinCount.computeIfPresent(key, (k, count) -> count <= 1 ? null : Integer.valueOf(count - 1));
        }
    }

    public boolean isPinned(K key) {
        return this.pinCount.containsKey(key);
    }

    public void clear() {
        this.hash.clear();
        this.pinCount.clear();
        this.useNativeMemory.set(0L);
    }

    public int size() {
        return this.hash.size();
    }
}

