/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public abstract class Filter<T> {
    public abstract boolean passes(T var1);

    public Iterator<T> filter(Iterator<T> iterator) {
        return new FilterIterator(this, iterator);
    }

    public Iterable<T> filter(Iterable<T> iterable) {
        return () -> this.filter(iterable.iterator());
    }

    public static int size(Iterable<?> iterable) {
        return iterable instanceof Collection ? ((Collection)iterable).size() : Filter.size(iterable.iterator());
    }

    public static int size(Iterator<?> iterator) {
        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            ++count;
        }
        return count;
    }

    public static <T> List<T> makeList(Iterable<T> iter) {
        ArrayList<T> list = new ArrayList<T>();
        for (T item : iter) {
            list.add(item);
        }
        return list;
    }

    private class FilterIterator
    implements Iterator<T> {
        private final Iterator<T> iterator;
        private T next;
        final /* synthetic */ Filter this$0;

        private FilterIterator(Filter filter, Iterator<T> iterator) {
            Filter filter2 = filter;
            Objects.requireNonNull(filter2);
            this.this$0 = filter2;
            this.iterator = iterator;
            this.toNext();
        }

        @Override
        public boolean hasNext() {
            return this.next != null;
        }

        @Override
        public T next() {
            if (this.next == null) {
                throw new NoSuchElementException();
            }
            Object returnValue = this.next;
            this.toNext();
            return returnValue;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private void toNext() {
            this.next = null;
            while (this.iterator.hasNext()) {
                Object item = this.iterator.next();
                if (item == null || !this.this$0.passes(item)) continue;
                this.next = item;
                break;
            }
        }
    }
}

