/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.img.util;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

@FunctionalInterface
public interface SupplierEx<T, E extends Exception> {
    public T get() throws E;

    public static <T, E extends Exception> SupplierEx<T, E> of(T value) {
        return () -> value;
    }

    default public Supplier<T> unchecked() {
        return () -> {
            try {
                return this.get();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    default public <U> SupplierEx<U, E> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "Mapper function cannot be null");
        return () -> mapper.apply((T)this.get());
    }

    default public SupplierEx<T, E> orElse(SupplierEx<? extends T, ? extends E> fallback) {
        Objects.requireNonNull(fallback, "Fallback supplier cannot be null");
        return () -> {
            T result = this.get();
            return result != null ? result : fallback.get();
        };
    }

    default public SupplierEx<T, E> orElse(T defaultValue) {
        return () -> {
            T result = this.get();
            return result != null ? result : defaultValue;
        };
    }

    default public SupplierEx<T, E> memoized() {
        return new MemoizedSupplierEx(this);
    }

    public static final class MemoizedSupplierEx<T, E extends Exception>
    implements SupplierEx<T, E> {
        private final SupplierEx<T, E> original;
        private volatile boolean computed = false;
        private T cachedValue;
        private E cachedException;

        private MemoizedSupplierEx(SupplierEx<T, E> original) {
            this.original = Objects.requireNonNull(original, "Original supplier cannot be null");
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public T get() throws E {
            if (!this.computed) {
                MemoizedSupplierEx memoizedSupplierEx = this;
                synchronized (memoizedSupplierEx) {
                    if (!this.computed) {
                        try {
                            this.cachedValue = this.original.get();
                        }
                        catch (Exception e) {
                            Exception typedException = e;
                            this.cachedException = typedException;
                        }
                        finally {
                            this.computed = true;
                        }
                    }
                }
            }
            if (this.cachedException != null) {
                throw this.cachedException;
            }
            return this.cachedValue;
        }
    }
}

