/*
 * Decompiled with CFR 0.152.
 */
package com.github.weisj.jsvg.attributes.stroke;

public enum LineJoin {
    Miter(0),
    Round(1),
    Bevel(2);

    private final int awtCode;

    private LineJoin(int awtCode) {
        this.awtCode = awtCode;
    }

    public int awtCode() {
        return this.awtCode;
    }
}

