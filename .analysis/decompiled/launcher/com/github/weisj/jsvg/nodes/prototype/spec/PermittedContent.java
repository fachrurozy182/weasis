/*
 * Decompiled with CFR 0.152.
 */
package com.github.weisj.jsvg.nodes.prototype.spec;

import com.github.weisj.jsvg.nodes.SVGNode;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE})
public @interface PermittedContent {
    public Category[] categories() default {};

    public Class<? extends SVGNode>[] anyOf() default {};

    public boolean any() default false;

    public boolean charData() default false;
}

