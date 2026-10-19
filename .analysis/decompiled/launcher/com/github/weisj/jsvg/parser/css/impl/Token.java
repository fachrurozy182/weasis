/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.errorprone.annotations.Immutable
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.parser.css.impl;

import com.github.weisj.jsvg.parser.css.impl.TokenType;
import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Immutable
public final class Token {
    @NotNull
    private final TokenType type;
    @Nullable
    private final String data;

    public Token(@NotNull TokenType type) {
        this(type, null);
    }

    public Token(@NotNull TokenType type, @Nullable String data) {
        this.type = type;
        this.data = data;
    }

    @NotNull
    public TokenType type() {
        return this.type;
    }

    @Nullable
    public String data() {
        return this.data;
    }

    public String toString() {
        return "Token{type=" + (Object)((Object)this.type) + ", data='" + this.data + '\'' + '}';
    }
}

