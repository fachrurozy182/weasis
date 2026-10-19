/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.lzma;

import org.tukaani.xz.lzma.State;

final class Optimum {
    private static final int INFINITY_PRICE = 0x40000000;
    final State state = new State();
    final int[] reps = new int[4];
    int price;
    int optPrev;
    int backPrev;
    boolean prev1IsLiteral;
    boolean hasPrev2;
    int optPrev2;
    int backPrev2;

    Optimum() {
    }

    void reset() {
        this.price = 0x40000000;
    }

    void set1(int newPrice, int optCur, int back) {
        this.price = newPrice;
        this.optPrev = optCur;
        this.backPrev = back;
        this.prev1IsLiteral = false;
    }

    void set2(int newPrice, int optCur, int back) {
        this.price = newPrice;
        this.optPrev = optCur + 1;
        this.backPrev = back;
        this.prev1IsLiteral = true;
        this.hasPrev2 = false;
    }

    void set3(int newPrice, int optCur, int back2, int len2, int back) {
        this.price = newPrice;
        this.optPrev = optCur + len2 + 1;
        this.backPrev = back;
        this.prev1IsLiteral = true;
        this.hasPrev2 = true;
        this.optPrev2 = optCur;
        this.backPrev2 = back2;
    }
}

