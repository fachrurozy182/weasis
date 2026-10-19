/*
 * Decompiled with CFR 0.152.
 */
package com.github.weisj.jsvg.nodes.prototype.spec;

import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(value={ElementType.TYPE})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface ElementCategories {
    public Category[] value();
}

