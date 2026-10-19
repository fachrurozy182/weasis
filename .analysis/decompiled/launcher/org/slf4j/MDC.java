/*
 * Decompiled with CFR 0.152.
 */
package org.slf4j;

import java.io.Closeable;
import java.util.Deque;
import java.util.Map;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.helpers.Reporter;
import org.slf4j.helpers.SubstituteServiceProvider;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

public class MDC {
    static final String NULL_MDCA_URL = "http://www.slf4j.org/codes.html#null_MDCA";
    private static final String MDC_ADAPTER_CANNOT_BE_NULL_MESSAGE = "MDCAdapter cannot be null. See also http://www.slf4j.org/codes.html#null_MDCA";
    static final String NO_STATIC_MDC_BINDER_URL = "http://www.slf4j.org/codes.html#no_static_mdc_binder";
    static MDCAdapter MDC_ADAPTER;

    private MDC() {
    }

    private static MDCAdapter getMDCAdapterGivenByProvider() {
        SLF4JServiceProvider provider = LoggerFactory.getProvider();
        if (provider != null) {
            MDCAdapter anAdapter = provider.getMDCAdapter();
            MDC.emitTemporaryMDCAdapterWarningIfNeeded(provider);
            return anAdapter;
        }
        Reporter.error("Failed to find provider.");
        Reporter.error("Defaulting to no-operation MDCAdapter implementation.");
        return new NOPMDCAdapter();
    }

    private static void emitTemporaryMDCAdapterWarningIfNeeded(SLF4JServiceProvider provider) {
        boolean isSubstitute = provider instanceof SubstituteServiceProvider;
        if (isSubstitute) {
            Reporter.info("Temporary mdcAdapter given by SubstituteServiceProvider.");
            Reporter.info("This mdcAdapter will be replaced after backend initialization has completed.");
        }
    }

    public static void put(String key, String val) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("key parameter cannot be null");
        }
        if (MDC.getMDCAdapter() == null) {
            throw new IllegalStateException(MDC_ADAPTER_CANNOT_BE_NULL_MESSAGE);
        }
        MDC.getMDCAdapter().put(key, val);
    }

    public static MDCCloseable putCloseable(String key, String val) throws IllegalArgumentException {
        MDC.put(key, val);
        return new MDCCloseable(key);
    }

    public static String get(String key) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("key parameter cannot be null");
        }
        MDC.mdcAdapterNullCheck();
        return MDC.getMDCAdapter().get(key);
    }

    public static void remove(String key) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("key parameter cannot be null");
        }
        MDC.mdcAdapterNullCheck();
        MDC.getMDCAdapter().remove(key);
    }

    public static void clear() {
        MDC.mdcAdapterNullCheck();
        MDC.getMDCAdapter().clear();
    }

    public static Map<String, String> getCopyOfContextMap() {
        MDC.mdcAdapterNullCheck();
        return MDC.getMDCAdapter().getCopyOfContextMap();
    }

    public static void setContextMap(Map<String, String> contextMap) {
        MDC.mdcAdapterNullCheck();
        MDC.getMDCAdapter().setContextMap(contextMap);
    }

    public static MDCAdapter getMDCAdapter() {
        if (MDC_ADAPTER == null) {
            MDC_ADAPTER = MDC.getMDCAdapterGivenByProvider();
        }
        return MDC_ADAPTER;
    }

    static void setMDCAdapter(MDCAdapter anMDCAdapter) {
        if (anMDCAdapter == null) {
            throw new IllegalStateException(MDC_ADAPTER_CANNOT_BE_NULL_MESSAGE);
        }
        MDC_ADAPTER = anMDCAdapter;
    }

    public static void pushByKey(String key, String value) {
        MDC.mdcAdapterNullCheck();
        MDC.getMDCAdapter().pushByKey(key, value);
    }

    public static String popByKey(String key) {
        MDC.mdcAdapterNullCheck();
        return MDC.getMDCAdapter().popByKey(key);
    }

    public Deque<String> getCopyOfDequeByKey(String key) {
        MDC.mdcAdapterNullCheck();
        return MDC.getMDCAdapter().getCopyOfDequeByKey(key);
    }

    private static void mdcAdapterNullCheck() {
        if (MDC.getMDCAdapter() == null) {
            throw new IllegalStateException(MDC_ADAPTER_CANNOT_BE_NULL_MESSAGE);
        }
    }

    public static class MDCCloseable
    implements Closeable {
        private final String key;

        private MDCCloseable(String key) {
            this.key = key;
        }

        @Override
        public void close() {
            MDC.remove(this.key);
        }
    }
}

