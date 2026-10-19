/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.dicom.codec.seg;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.dicom.codec.seg.SegSpecialElement;

public final class SegVolumeCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(SegVolumeCache.class);
    private static final double HEAP_FRACTION = 0.25;
    private static final long MIN_BUDGET = 0x8000000L;
    private static final long BUDGET = Math.max(0x8000000L, (long)((double)Runtime.getRuntime().maxMemory() * 0.25));
    private static final Object LOCK = new Object();
    private static final Map<String, Entry> TRACKED = new LinkedHashMap<String, Entry>(16, 0.75f, true);
    private static long used;
    private static final ExecutorService EVICTOR;

    private SegVolumeCache() {
    }

    private static String key(SegSpecialElement owner) {
        String uid = owner.getRegionUID();
        return uid == null ? Integer.toHexString(System.identityHashCode(owner)) : uid;
    }

    public static long getBudget() {
        return BUDGET;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static long getUsed() {
        Object object = LOCK;
        synchronized (object) {
            return used;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void register(SegSpecialElement owner, long bytes) {
        List<SegSpecialElement> victims;
        if (owner == null || bytes <= 0L) {
            return;
        }
        Object object = LOCK;
        synchronized (object) {
            SegVolumeCache.purge();
            Entry previous = TRACKED.put(SegVolumeCache.key(owner), new Entry(new WeakReference<SegSpecialElement>(owner), bytes));
            used += bytes - (previous == null ? 0L : previous.bytes());
            victims = SegVolumeCache.collectVictims(SegVolumeCache.key(owner));
        }
        if (victims.isEmpty()) {
            return;
        }
        EVICTOR.execute(() -> {
            victims.forEach(SegSpecialElement::releaseCanonicalVolume);
            LOGGER.debug("Segmentation volume cache over budget: disposed {} volume(s), {} MB of {} MB in use", new Object[]{victims.size(), SegVolumeCache.getUsed() >> 20, BUDGET >> 20});
        });
    }

    static void awaitEvictions() {
        try {
            EVICTOR.submit(() -> {}).get(10L, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | TimeoutException e) {
            throw new IllegalStateException("Segmentation volume evictions did not settle", e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void touch(SegSpecialElement owner) {
        if (owner != null) {
            Object object = LOCK;
            synchronized (object) {
                TRACKED.computeIfPresent(SegVolumeCache.key(owner), (k, entry) -> entry);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void remove(SegSpecialElement owner) {
        if (owner != null) {
            Object object = LOCK;
            synchronized (object) {
                SegVolumeCache.purge();
                Entry entry = TRACKED.remove(SegVolumeCache.key(owner));
                if (entry != null) {
                    used -= entry.bytes();
                }
            }
        }
    }

    private static void purge() {
        Iterator<Map.Entry<String, Entry>> it = TRACKED.entrySet().iterator();
        while (it.hasNext()) {
            Entry entry = it.next().getValue();
            if (!entry.owner().refersTo(null)) continue;
            used -= entry.bytes();
            it.remove();
        }
    }

    private static List<SegSpecialElement> collectVictims(String keep) {
        ArrayList<SegSpecialElement> victims = new ArrayList<SegSpecialElement>();
        Iterator<Map.Entry<String, Entry>> it = TRACKED.entrySet().iterator();
        while (used > BUDGET && it.hasNext()) {
            Map.Entry<String, Entry> entry = it.next();
            if (entry.getKey().equals(keep)) continue;
            used -= entry.getValue().bytes();
            SegSpecialElement owner = (SegSpecialElement)entry.getValue().owner().get();
            if (owner != null) {
                victims.add(owner);
            }
            it.remove();
        }
        return victims;
    }

    static {
        EVICTOR = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "seg-volume-evict");
            t.setDaemon(true);
            return t;
        });
    }

    private record Entry(WeakReference<SegSpecialElement> owner, long bytes) {
    }
}

