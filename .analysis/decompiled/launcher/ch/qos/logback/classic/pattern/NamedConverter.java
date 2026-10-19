/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.pattern.Abbreviator;
import ch.qos.logback.classic.pattern.ClassNameOnlyAbbreviator;
import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.pattern.TargetLengthBasedClassNameAbbreviator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.util.OptionHelper;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class NamedConverter
extends ClassicConverter {
    private static final String DISABLE_CACHE_SYSTEM_PROPERTY = "logback.namedConverter.disableCache";
    private static final int INITIAL_CACHE_SIZE = 512;
    private static final double LOAD_FACTOR = 0.75;
    private static final int MAX_ALLOWED_REMOVAL_THRESHOLD = 1536;
    private static final double CACHE_MISSRATE_TRIGGER = 0.3;
    private static final int MIN_SAMPLE_SIZE = 1024;
    private static final double NEGATIVE = -1.0;
    private volatile boolean cacheEnabled = true;
    private final NameCache cache = new NameCache(512);
    private Abbreviator abbreviator = null;
    private volatile int cacheMisses = 0;
    private volatile int totalCalls = 0;

    protected abstract String getFullyQualifiedName(ILoggingEvent var1);

    @Override
    public void start() {
        String optStr;
        String disableCacheProp = OptionHelper.getSystemProperty(DISABLE_CACHE_SYSTEM_PROPERTY);
        boolean disableCache = OptionHelper.toBoolean(disableCacheProp, false);
        if (disableCache) {
            this.addInfo("Disabling name cache via System.properties");
            this.cacheEnabled = false;
        }
        if ((optStr = this.getFirstOption()) != null) {
            try {
                int targetLen = Integer.parseInt(optStr);
                if (targetLen == 0) {
                    this.abbreviator = new ClassNameOnlyAbbreviator();
                } else if (targetLen > 0) {
                    this.abbreviator = new TargetLengthBasedClassNameAbbreviator(targetLen);
                }
            }
            catch (NumberFormatException nfe) {
                this.addError("failed to parse integer string [" + optStr + "]", nfe);
            }
        }
        super.start();
    }

    @Override
    public String convert(ILoggingEvent event) {
        String fqn = this.getFullyQualifiedName(event);
        if (this.abbreviator == null) {
            return fqn;
        }
        if (this.cacheEnabled) {
            return this.viaCache(fqn);
        }
        return this.abbreviator.abbreviate(fqn);
    }

    private synchronized String viaCache(String fqn) {
        ++this.totalCalls;
        String abbreviated = (String)this.cache.get(fqn);
        if (abbreviated == null) {
            ++this.cacheMisses;
            abbreviated = this.abbreviator.abbreviate(fqn);
            this.cache.put(fqn, abbreviated);
        }
        return abbreviated;
    }

    private void disableCache() {
        if (!this.cacheEnabled) {
            return;
        }
        this.cacheEnabled = false;
        this.cache.clear();
        this.addInfo("Disabling cache at totalCalls=" + this.totalCalls);
    }

    public double getCacheMissRate() {
        return this.cache.cacheMissCalculator.getCacheMissRate();
    }

    public int getCacheMisses() {
        return this.cacheMisses;
    }

    private class NameCache
    extends LinkedHashMap<String, String> {
        private static final long serialVersionUID = 1050866539278406045L;
        int removalThreshold;
        CacheMissCalculator cacheMissCalculator;

        NameCache(int initialCapacity) {
            super(initialCapacity);
            this.cacheMissCalculator = new CacheMissCalculator();
            this.removalThreshold = (int)((double)initialCapacity * 0.75);
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> entry) {
            if (this.shouldDoubleRemovalThreshold()) {
                this.removalThreshold *= 2;
                int missRate = (int)(this.cacheMissCalculator.getCacheMissRate() * 100.0);
                NamedConverter.this.addInfo("Doubling nameCache removalThreshold to " + this.removalThreshold + " previous cacheMissRate=" + missRate + "%");
                this.cacheMissCalculator.updateMilestones();
            }
            return this.size() >= this.removalThreshold;
        }

        private boolean shouldDoubleRemovalThreshold() {
            double rate = this.cacheMissCalculator.getCacheMissRate();
            if (rate < 0.0) {
                return false;
            }
            if (rate < 0.3) {
                return false;
            }
            if (this.removalThreshold >= 1536) {
                NamedConverter.this.disableCache();
                return false;
            }
            return true;
        }
    }

    class CacheMissCalculator {
        int totalsMilestone = 0;
        int cacheMissesMilestone = 0;

        CacheMissCalculator() {
        }

        void updateMilestones() {
            this.totalsMilestone = NamedConverter.this.totalCalls;
            this.cacheMissesMilestone = NamedConverter.this.cacheMisses;
        }

        double getCacheMissRate() {
            int effectiveTotal = NamedConverter.this.totalCalls - this.totalsMilestone;
            if (effectiveTotal < 1024) {
                return -1.0;
            }
            int effectiveCacheMisses = NamedConverter.this.cacheMisses - this.cacheMissesMilestone;
            return 1.0 * (double)effectiveCacheMisses / (double)effectiveTotal;
        }
    }
}

