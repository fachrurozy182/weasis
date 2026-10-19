/*
 * Decompiled with CFR 0.152.
 */
package com.github.weisj.jsvg.attributes.stroke;

public enum LineCap {
    Butt(0),
    Square(2),
    Round(1);

    private final int awtCode;

    private LineCap(int awtCode) {
        this.awtCode = awtCode;
    }

    public int awtCode() {
        return this.awtCode;
    }
}

