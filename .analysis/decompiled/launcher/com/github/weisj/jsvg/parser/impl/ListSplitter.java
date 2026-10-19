/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.parser.impl;

import org.jetbrains.annotations.NotNull;

public interface ListSplitter {
    public boolean splitOnWhitespace();

    public SplitResult testChar(char var1, int var2, @NotNull String var3, int var4);

    public static enum SplitResult {
        YES,
        YES_INCLUDING_CHAR,
        NO;


        public boolean shouldSplit() {
            return this != NO;
        }

        public boolean shouldIncludeChar() {
            return this == YES_INCLUDING_CHAR;
        }
    }
}

