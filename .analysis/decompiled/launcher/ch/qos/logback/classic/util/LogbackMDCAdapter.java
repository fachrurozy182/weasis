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

public class LogbackMDCAdapter
implements MDCAdapter {
    final ThreadLocal<Map<String, String>> readWriteThreadLocalMap = new ThreadLocal();
    final ThreadLocal<Map<String, String>> readOnlyThreadLocalMap = new ThreadLocal();
    private final ThreadLocalMapOfStacks threadLocalMapOfDeques = new ThreadLocalMapOfStacks();

    @Override
    public void put(String key, String val) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        Map<String, String> current = this.readWriteThreadLocalMap.get();
        if (current == null) {
            current = new HashMap<String, String>();
            this.readWriteThreadLocalMap.set(current);
        }
        current.put(key, val);
        this.nullifyReadOnlyThreadLocalMap();
    }

    @Override
    public String get(String key) {
        Map<String, String> hashMap = this.readWriteThreadLocalMap.get();
        if (hashMap != null && key != null) {
            return hashMap.get(key);
        }
        return null;
    }

    @Override
    public void remove(String key) {
        if (key == null) {
            return;
        }
        Map<String, String> current = this.readWriteThreadLocalMap.get();
        if (current != null) {
            current.remove(key);
            this.nullifyReadOnlyThreadLocalMap();
        }
    }

    private void nullifyReadOnlyThreadLocalMap() {
        this.readOnlyThreadLocalMap.set(null);
    }

    @Override
    public void clear() {
        this.readWriteThreadLocalMap.set(null);
        this.nullifyReadOnlyThreadLocalMap();
    }

    public Map<String, String> getPropertyMap() {
        Map<String, String> current;
        Map<String, String> readOnlyMap = this.readOnlyThreadLocalMap.get();
        if (readOnlyMap == null && (current = this.readWriteThreadLocalMap.get()) != null) {
            HashMap<String, String> tempMap = new HashMap<String, String>(current);
            readOnlyMap = Collections.unmodifiableMap(tempMap);
            this.readOnlyThreadLocalMap.set(readOnlyMap);
        }
        return readOnlyMap;
    }

    public Map getCopyOfContextMap() {
        Map<String, String> readOnlyMap = this.getPropertyMap();
        if (readOnlyMap == null) {
            return null;
        }
        return new HashMap<String, String>(readOnlyMap);
    }

    public Set<String> getKeys() {
        Map<String, String> readOnlyMap = this.getPropertyMap();
        if (readOnlyMap != null) {
            return readOnlyMap.keySet();
        }
        return null;
    }

    public void setContextMap(Map contextMap) {
        if (contextMap != null) {
            this.readWriteThreadLocalMap.set(new HashMap(contextMap));
        } else {
            this.readWriteThreadLocalMap.set(null);
        }
        this.nullifyReadOnlyThreadLocalMap();
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

