/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.parser.impl;

import com.github.weisj.jsvg.parser.impl.ListSplitter;
import org.jetbrains.annotations.NotNull;

public enum SeparatorMode implements ListSplitter
{
    COMMA_ONLY(',', false),
    SEMICOLON_ONLY(';', false),
    WHITESPACE_ONLY('\u0000', true),
    COMMA_AND_WHITESPACE(',', true);

    private final boolean allowWhitespace;
    private final char separator;

    private SeparatorMode(char separator, boolean allowWhitespace) {
        this.allowWhitespace = allowWhitespace;
        this.separator = separator;
    }

    @Override
    public boolean splitOnWhitespace() {
        return this.allowWhitespace;
    }

    @Override
    public ListSplitter.SplitResult testChar(char c, int subwordIndex, @NotNull String s, int stringIndex) {
        return this.separator != '\u0000' && c == this.separator ? ListSplitter.SplitResult.YES : ListSplitter.SplitResult.NO;
    }
}

