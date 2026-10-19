/*
 * Decompiled with CFR 0.152.
 */
package com.github.weisj.jsvg.attributes;

public enum Overflow {
    Auto(false),
    Visible(false),
    Hidden(true),
    Scroll(true);

    private final boolean establishesClip;

    private Overflow(boolean establishesClip) {
        this.establishesClip = establishesClip;
    }

    public boolean establishesClip() {
        return this.establishesClip;
    }
}

