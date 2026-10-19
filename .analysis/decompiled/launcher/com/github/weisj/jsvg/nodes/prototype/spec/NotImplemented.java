/*
 * Decompiled with CFR 0.152.
 */
package com.github.weisj.jsvg.nodes.prototype.spec;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(value={ElementType.METHOD, ElementType.FIELD})
@Retention(value=RetentionPolicy.SOURCE)
public @interface NotImplemented {
    public String value() default "";
}

