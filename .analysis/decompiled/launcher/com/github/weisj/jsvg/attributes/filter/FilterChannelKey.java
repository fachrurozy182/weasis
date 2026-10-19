/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.attributes.filter;

import org.jetbrains.annotations.NotNull;

public interface FilterChannelKey {
    @NotNull
    public Object key();

    public static class StringKey
    implements FilterChannelKey {
        @NotNull
        private final String key;

        public StringKey(@NotNull String key) {
            this.key = key;
        }

        @Override
        @NotNull
        public Object key() {
            return this.key;
        }

        public String toString() {
            return "StringKey{key='" + this.key + '\'' + '}';
        }
    }
}

