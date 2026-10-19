/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.StackUtils;
import java.util.function.BiPredicate;

/*
 * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
 */
class StackUtilsImpl
extends StackUtils {
    StackUtilsImpl() {
    }

    @Override
    boolean wasInvokedFromImpl(BiPredicate<String, String> predicate, int limit) {
        return StackWalker.getInstance().walk(stream -> {
            if (limit > 0) {
                stream = stream.limit(limit + 2);
            }
            return stream.anyMatch(f -> predicate.test(f.getClassName(), f.getMethodName()));
        });
    }
}

