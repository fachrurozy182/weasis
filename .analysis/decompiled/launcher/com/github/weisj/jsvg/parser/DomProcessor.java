/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.parser;

import com.github.weisj.jsvg.parser.DomElement;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface DomProcessor {
    public void process(@NotNull DomElement var1);
}

