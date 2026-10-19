/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.util;

import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.helpers.ThreadLocalMapOfStacks;
import org.slf4j.spi.MDCAdapter;

public class LogbackMDCAdapterSimple
implements MDCAdapter {
    final ThreadLocal<Map<String, String>> threadLocalUnmodifiableMap = new ThreadLocal();
    private final ThreadLocalMapOfStacks threadLocalMapOfDeques = new ThreadLocalMapOfStacks();

    private Map<String, String> duplicateMap(Map<String, String> oldMap) {
        if (oldMap != null) {
            return new HashMap<String, String>(oldMap);
        }
        return new HashMap<String, String>();
    }

    @Override
    public void put(String key, String val) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        Map<String, String> oldMap = this.threadLocalUnmodifiableMap.get();
        Map<String, String> newMap = this.duplicateMap(oldMap);
        newMap.put(key, val);
        this.makeUnmodifiableAndThreadLocalSet(newMap);
    }

    private void makeUnmodifiableAndThreadLocalSet(Map<String, String> aMap) {
        Map<String, String> unmodifiable = Collections.unmodifiableMap(aMap);
        this.threadLocalUnmodifiableMap.set(unmodifiable);
    }

    @Override
    public void remove(String key) {
        if (key == null) {
            return;
        }
        Map<String, String> oldMap = this.threadLocalUnmodifiableMap.get();
        if (oldMap == null) {
            return;
        }
        Map<String, String> newMap = this.duplicateMap(oldMap);
        newMap.remove(key);
        this.makeUnmodifiableAndThreadLocalSet(newMap);
    }

    @Override
    public void clear() {
        this.threadLocalUnmodifiableMap.remove();
    }

    @Override
    public String get(String key) {
        Map<String, String> map = this.threadLocalUnmodifiableMap.get();
        if (map != null && key != null) {
            return map.get(key);
        }
        return null;
    }

    public Map<String, String> getPropertyMap() {
        return this.threadLocalUnmodifiableMap.get();
    }

    public Set<String> getKeys() {
        Map<String, String> map = this.getPropertyMap();
        if (map != null) {
            return map.keySet();
        }
        return null;
    }

    @Override
    public Map<String, String> getCopyOfContextMap() {
        Map<String, String> hashMap = this.threadLocalUnmodifiableMap.get();
        return this.duplicateMap(hashMap);
    }

    @Override
    public void setContextMap(Map<String, String> contextMap) {
        this.duplicateMap(contextMap);
    }

    @Override
    public void pushByKey(String key, String value) {
        this.threadLocalMapOfDeques.pushByKey(key, value);
    }

    @Override
    public String popByKey(String key) {
        return this.threadLocalMapOfDeques.popByKey(key);
    }

    @Override
    public Deque<String> getCopyOfDequeByKey(String key) {
        return this.threadLocalMapOfDeques.getCopyOfDequeByKey(key);
    }

    @Override
    public void clearDequeByKey(String key) {
        this.threadLocalMapOfDeques.clearDequeByKey(key);
    }
}

