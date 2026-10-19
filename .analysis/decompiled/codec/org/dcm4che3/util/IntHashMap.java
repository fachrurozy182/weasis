/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

public class IntHashMap<V>
implements Cloneable,
Serializable {
    private static final int DEFAULT_CAPACITY = 32;
    private static final int MINIMUM_CAPACITY = 4;
    private static final int MAXIMUM_CAPACITY = 0x40000000;
    private static final byte FREE = 0;
    private static final byte FULL = 1;
    private static final byte REMOVED = -1;
    private transient int[] keys;
    private transient Object[] values;
    private transient byte[] states;
    private transient int free;
    private transient int size;
    private static final long serialVersionUID = 9153226350279204066L;

    public IntHashMap() {
        this.init(32);
    }

    public IntHashMap(int expectedMaxSize) {
        if (expectedMaxSize < 0) {
            throw new IllegalArgumentException("expectedMaxSize is negative: " + expectedMaxSize);
        }
        this.init(this.capacity(expectedMaxSize));
    }

    private int capacity(int expectedMaxSize) {
        int capacity;
        int minCapacity = expectedMaxSize << 1;
        if (minCapacity > 0x40000000) {
            return 0x40000000;
        }
        for (capacity = 4; capacity < minCapacity; capacity <<= 1) {
        }
        return capacity;
    }

    private void init(int initCapacity) {
        this.keys = new int[initCapacity];
        this.values = new Object[initCapacity];
        this.states = new byte[initCapacity];
        this.free = initCapacity >>> 1;
    }

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    public V get(int key) {
        byte[] states = this.states;
        int[] keys = this.keys;
        int mask = keys.length - 1;
        int i = key & mask;
        while (states[i] != 0) {
            if (keys[i] == key) {
                return (V)this.values[i];
            }
            i = i + 1 & mask;
        }
        return null;
    }

    public boolean containsKey(int key) {
        byte[] states = this.states;
        int[] keys = this.keys;
        int mask = keys.length - 1;
        int i = key & mask;
        while (states[i] != 0) {
            if (keys[i] == key) {
                return states[i] > 0;
            }
            i = i + 1 & mask;
        }
        return false;
    }

    public V put(int key, V value) {
        byte[] states = this.states;
        int[] keys = this.keys;
        int mask = keys.length - 1;
        int i = key & mask;
        while (states[i] > 0) {
            if (keys[i] == key) {
                Object oldValue = this.values[i];
                this.values[i] = value;
                return (V)oldValue;
            }
            i = i + 1 & mask;
        }
        byte oldState = states[i];
        states[i] = 1;
        keys[i] = key;
        this.values[i] = value;
        ++this.size;
        if (oldState == 0 && --this.free < 0) {
            this.resize(Math.max(this.capacity(this.size), keys.length));
        }
        return null;
    }

    public void trimToSize() {
        this.resize(this.capacity(this.size));
    }

    public void rehash() {
        this.resize(this.keys.length);
    }

    private void resize(int newLength) {
        if (newLength > 0x40000000) {
            throw new IllegalStateException("Capacity exhausted.");
        }
        int[] oldKeys = this.keys;
        Object[] oldValues = this.values;
        byte[] oldStates = this.states;
        int[] newKeys = new int[newLength];
        Object[] newValues = new Object[newLength];
        byte[] newStates = new byte[newLength];
        int mask = newLength - 1;
        for (int j = 0; j < oldKeys.length; ++j) {
            if (oldStates[j] <= 0) continue;
            int key = oldKeys[j];
            int i = key & mask;
            while (newStates[i] != 0) {
                i = i + 1 & mask;
            }
            newStates[i] = 1;
            newKeys[i] = key;
            newValues[i] = oldValues[j];
            oldValues[j] = null;
        }
        this.keys = newKeys;
        this.values = newValues;
        this.states = newStates;
        this.free = (newLength >>> 1) - this.size;
    }

    public V remove(int key) {
        byte[] states = this.states;
        int[] keys = this.keys;
        int mask = keys.length - 1;
        int i = key & mask;
        while (states[i] != 0) {
            if (keys[i] == key) {
                if (states[i] < 0) {
                    return null;
                }
                states[i] = -1;
                Object oldValue = this.values[i];
                this.values[i] = null;
                --this.size;
                return (V)oldValue;
            }
            i = i + 1 & mask;
        }
        return null;
    }

    public void clear() {
        Arrays.fill(this.values, null);
        Arrays.fill(this.states, (byte)0);
        this.size = 0;
        this.free = this.keys.length >>> 1;
    }

    public Object clone() {
        try {
            IntHashMap m = (IntHashMap)super.clone();
            m.states = (byte[])this.states.clone();
            m.keys = (int[])this.keys.clone();
            m.values = (Object[])this.values.clone();
            return m;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    public boolean accept(Visitor<V> visitor) {
        for (int i = 0; i < this.states.length; ++i) {
            if (this.states[i] <= 0 || visitor.visit(this.keys[i], this.values[i])) continue;
            return false;
        }
        return true;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        byte[] states = this.states;
        int[] keys = this.keys;
        Object[] values = this.values;
        s.writeInt(this.size);
        for (int i = 0; i < states.length; ++i) {
            if (states[i] <= 0) continue;
            s.writeInt(keys[i]);
            s.writeObject(values[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        int count = s.readInt();
        this.init(this.capacity(count));
        this.size = count;
        this.free -= count;
        byte[] states = this.states;
        int[] keys = this.keys;
        Object[] values = this.values;
        int mask = keys.length - 1;
        while (count-- > 0) {
            int key = s.readInt();
            int i = key & mask;
            while (states[i] != 0) {
                i = i + 1 & mask;
            }
            states[i] = 1;
            keys[i] = key;
            values[i] = s.readObject();
        }
    }

    public static interface Visitor<V> {
        public boolean visit(int var1, V var2);
    }
}

