/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public class CachingDateFormatter {
    final DateTimeFormatter dtf;
    final ZoneId zoneId;
    final AtomicReference<CacheTuple> atomicReference;

    public CachingDateFormatter(String pattern) {
        this(pattern, null);
    }

    public CachingDateFormatter(String pattern, ZoneId aZoneId) {
        this(pattern, aZoneId, null);
    }

    public CachingDateFormatter(String pattern, ZoneId aZoneId, Locale aLocale) {
        this.zoneId = aZoneId == null ? ZoneId.systemDefault() : aZoneId;
        Locale locale = aLocale != null ? aLocale : Locale.getDefault();
        this.dtf = DateTimeFormatter.ofPattern(pattern).withZone(this.zoneId).withLocale(locale);
        CacheTuple cacheTuple = new CacheTuple(-1L, null);
        this.atomicReference = new AtomicReference<CacheTuple>(cacheTuple);
    }

    public final String format(long now) {
        CacheTuple localCacheTuple;
        CacheTuple oldCacheTuple = localCacheTuple = this.atomicReference.get();
        if (now != localCacheTuple.lastTimestamp) {
            Instant instant = Instant.ofEpochMilli(now);
            String result = this.dtf.format(instant);
            localCacheTuple = new CacheTuple(now, result);
            this.atomicReference.compareAndSet(oldCacheTuple, localCacheTuple);
        }
        return localCacheTuple.cachedStr;
    }

    static class CacheTuple {
        final long lastTimestamp;
        final String cachedStr;

        public CacheTuple(long lastTimestamp, String cachedStr) {
            this.lastTimestamp = lastTimestamp;
            this.cachedStr = cachedStr;
        }
    }
}

