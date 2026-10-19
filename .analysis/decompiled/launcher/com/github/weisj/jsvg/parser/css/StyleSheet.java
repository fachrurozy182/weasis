/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.parser.css;

import com.github.weisj.jsvg.parser.DomElement;
import com.github.weisj.jsvg.parser.css.StyleProperty;
import org.jetbrains.annotations.NotNull;

public interface StyleSheet {
    public void forEachMatchingRule(@NotNull DomElement var1, @NotNull RuleConsumer var2);

    public static interface RuleConsumer {
        public void applyRule(@NotNull StyleProperty var1);
    }
}

