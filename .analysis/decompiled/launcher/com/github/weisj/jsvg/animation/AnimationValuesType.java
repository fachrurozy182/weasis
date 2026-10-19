/*
 * Decompiled with CFR 0.152.
 */
package com.github.weisj.jsvg.animation;

public enum AnimationValuesType {
    VALUES,
    FROM_TO,
    FROM_BY,
    BY,
    TO;


    public boolean endIsBy() {
        return this == FROM_BY || this == BY;
    }
}

