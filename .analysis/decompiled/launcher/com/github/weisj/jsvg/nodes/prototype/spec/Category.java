/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.prototype.spec;

import com.github.weisj.jsvg.nodes.SVGNode;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import org.jetbrains.annotations.NotNull;

public enum Category {
    Animation(false),
    BasicShape,
    Container,
    Descriptive(false),
    FilterPrimitive,
    TransferFunctionElement,
    Gradient,
    Graphic,
    GraphicsReferencing,
    Shape,
    Structural,
    TextContent,
    TextContentChild,
    None;

    private final boolean effectivelyAllowed;

    private Category() {
        this(true);
    }

    private Category(boolean effectivelyAllowed) {
        this.effectivelyAllowed = effectivelyAllowed;
    }

    public boolean isEffectivelyAllowed() {
        return this.effectivelyAllowed;
    }

    @NotNull
    public static @NotNull Category @NotNull [] categoriesOf(@NotNull SVGNode node) {
        Class<?> nodeType = node.getClass();
        ElementCategories categories = nodeType.getAnnotation(ElementCategories.class);
        if (categories == null) {
            throw new IllegalStateException("Element <" + node.tagName() + "> doesn't specify element category information");
        }
        return categories.value();
    }

    public static boolean hasCategory(@NotNull SVGNode node, @NotNull Category category) {
        Category[] categories;
        for (Category c : categories = Category.categoriesOf(node)) {
            if (c != category) continue;
            return true;
        }
        return false;
    }
}

