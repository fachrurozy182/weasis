/*
 * Decompiled with CFR 0.152.
 */
package com.github.weisj.jsvg.attributes.filter;

public enum ColorChannel {
    R(2),
    G(1),
    B(0),
    A(3);

    private final int index;

    private ColorChannel(int index) {
        this.index = index;
    }

    public int value(int pixelData) {
        return pixelData >> this.index * 8 & 0xFF;
    }
}

