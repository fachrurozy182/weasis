/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.attributes;

import com.github.weisj.jsvg.parser.impl.AttributeNode;
import com.github.weisj.jsvg.parser.impl.AttributeParser;
import com.github.weisj.jsvg.parser.impl.SeparatorMode;
import com.github.weisj.jsvg.util.AttributeUtil;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PaintOrder {
    public static final PaintOrder NORMAL = new PaintOrder(Phase.FILL, Phase.STROKE, Phase.MARKERS);
    @NotNull
    private final Phase[] phases;

    public PaintOrder(Phase ... phases) {
        this.phases = phases;
    }

    @NotNull
    public Phase[] phases() {
        return this.phases;
    }

    @Nullable
    public static PaintOrder parse(@NotNull AttributeNode attributeNode) {
        @Nullable String value = attributeNode.getValue("paint-order");
        @NotNull AttributeParser parser = attributeNode.parser();
        if (value == null) {
            return null;
        }
        if ("inherit".equals(value)) {
            return null;
        }
        if ("none".equals(value)) {
            return NORMAL;
        }
        if ("normal".equals(value)) {
            return NORMAL;
        }
        String[] rawPhases = parser.parseStringList(value, SeparatorMode.COMMA_AND_WHITESPACE);
        Phase[] phases = new Phase[3];
        int length = Math.min(phases.length, rawPhases.length);
        int phasesIndex = 0;
        for (int rawPhasesIndex = 0; phasesIndex < length && rawPhasesIndex < length; ++rawPhasesIndex) {
            Phase phase = parser.parseEnum(rawPhases[rawPhasesIndex], Phase.class);
            if (phase == null || AttributeUtil.arrayContains(phases, phase)) continue;
            phases[phasesIndex] = phase;
            ++phasesIndex;
        }
        while (phasesIndex < 3) {
            phases[phasesIndex] = PaintOrder.findNextInNormalOrder(phases, phasesIndex);
            ++phasesIndex;
        }
        return new PaintOrder(phases);
    }

    @NotNull
    private static Phase findNextInNormalOrder(@NotNull Phase[] phases, int maxIndex) {
        for (Phase phase : NORMAL.phases()) {
            boolean found = false;
            for (int i = 0; i < maxIndex; ++i) {
                if (phases[i] != phase) continue;
                found = true;
                break;
            }
            if (found) continue;
            return phase;
        }
        throw new IllegalStateException();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        PaintOrder that = (PaintOrder)o;
        return Arrays.equals((Object[])this.phases, (Object[])that.phases);
    }

    public int hashCode() {
        return Arrays.hashCode((Object[])this.phases);
    }

    public String toString() {
        return "PaintOrder" + Arrays.toString((Object[])this.phases);
    }

    public static enum Phase {
        FILL,
        STROKE,
        MARKERS;

    }
}

