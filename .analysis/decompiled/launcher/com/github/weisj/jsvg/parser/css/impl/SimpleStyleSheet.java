/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.parser.css.impl;

import com.github.weisj.jsvg.parser.DomElement;
import com.github.weisj.jsvg.parser.css.StyleProperty;
import com.github.weisj.jsvg.parser.css.StyleSheet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class SimpleStyleSheet
implements StyleSheet {
    @NotNull
    private final @NotNull Map<String, @NotNull List<@NotNull StyleProperty>> classRules = new HashMap<String, List<StyleProperty>>();
    @NotNull
    private final @NotNull Map<String, @NotNull List<@NotNull StyleProperty>> idRules = new HashMap<String, List<StyleProperty>>();
    @NotNull
    private final @NotNull Map<String, @NotNull List<@NotNull StyleProperty>> tagNameRules = new HashMap<String, List<StyleProperty>>();

    @NotNull
    public @NotNull Map<String, @NotNull List<@NotNull StyleProperty>> classRules() {
        return this.classRules;
    }

    @NotNull
    public @NotNull Map<String, @NotNull List<@NotNull StyleProperty>> idRules() {
        return this.idRules;
    }

    @NotNull
    public @NotNull Map<String, @NotNull List<@NotNull StyleProperty>> tagNameRules() {
        return this.tagNameRules;
    }

    void addTagNameRules(@NotNull String tagName, @NotNull @NotNull List<@NotNull StyleProperty> rule) {
        this.tagNameRules.computeIfAbsent(tagName, k -> new ArrayList()).addAll(rule);
    }

    void addClassRules(@NotNull String className, @NotNull @NotNull List<@NotNull StyleProperty> rule) {
        this.classRules.computeIfAbsent(className, k -> new ArrayList()).addAll(rule);
    }

    void addIdRules(@NotNull String id, @NotNull @NotNull List<@NotNull StyleProperty> rule) {
        this.idRules.computeIfAbsent(id, k -> new ArrayList()).addAll(rule);
    }

    @Override
    public void forEachMatchingRule(@NotNull DomElement element, @NotNull StyleSheet.RuleConsumer ruleConsumer) {
        List<@NotNull StyleProperty> rules = this.tagNameRules.get(element.tagName());
        if (rules != null) {
            rules.forEach(ruleConsumer::applyRule);
        }
        if (element.id() != null && (rules = this.idRules.get(element.id())) != null) {
            rules.forEach(ruleConsumer::applyRule);
        }
        for (String className : element.classNames()) {
            rules = this.classRules.get(className);
            if (rules == null) continue;
            rules.forEach(ruleConsumer::applyRule);
        }
    }
}

