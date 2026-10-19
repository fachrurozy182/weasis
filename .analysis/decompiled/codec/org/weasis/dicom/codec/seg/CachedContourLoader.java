/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.ui.model.graphic.imp.seg.SegContour
 *  org.weasis.core.util.SoftHashMap
 */
package org.weasis.dicom.codec.seg;

import java.util.Set;
import org.weasis.core.ui.model.graphic.imp.seg.SegContour;
import org.weasis.core.util.SoftHashMap;
import org.weasis.dicom.codec.seg.LazyContourLoader;

abstract class CachedContourLoader
implements LazyContourLoader {
    private final SoftHashMap<String, Set<SegContour>> cache;
    private final String cacheKey;

    CachedContourLoader(SoftHashMap<String, Set<SegContour>> cache, String cacheKey) {
        this.cache = cache;
        this.cacheKey = cacheKey;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final Set<SegContour> getLazyContours() {
        SoftHashMap<String, Set<SegContour>> softHashMap = this.cache;
        synchronized (softHashMap) {
            Set cached = (Set)this.cache.get((Object)this.cacheKey);
            if (cached != null) {
                return cached;
            }
        }
        Set<Object> built = this.build();
        if (built == null) {
            built = Set.of();
        }
        SoftHashMap<String, Set<SegContour>> softHashMap2 = this.cache;
        synchronized (softHashMap2) {
            Set existing = (Set)this.cache.get((Object)this.cacheKey);
            if (existing != null) {
                return existing;
            }
            this.cache.put((Object)this.cacheKey, built);
            return built;
        }
    }

    protected abstract Set<SegContour> build();
}

