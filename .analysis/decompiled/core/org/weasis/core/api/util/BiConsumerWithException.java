/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.util;

@FunctionalInterface
public interface BiConsumerWithException<T, U, E extends Exception> {
    public void accept(T var1, U var2) throws E;
}

