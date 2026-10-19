/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.lzma;

final class State {
    static final int STATES = 12;
    private static final int LIT_STATES = 7;
    private static final int LIT_LIT = 0;
    private static final int MATCH_LIT_LIT = 1;
    private static final int REP_LIT_LIT = 2;
    private static final int SHORTREP_LIT_LIT = 3;
    private static final int MATCH_LIT = 4;
    private static final int REP_LIT = 5;
    private static final int SHORTREP_LIT = 6;
    private static final int LIT_MATCH = 7;
    private static final int LIT_LONGREP = 8;
    private static final int LIT_SHORTREP = 9;
    private static final int NONLIT_MATCH = 10;
    private static final int NONLIT_REP = 11;
    private int state;

    State() {
    }

    State(State other) {
        this.state = other.state;
    }

    void reset() {
        this.state = 0;
    }

    int get() {
        return this.state;
    }

    void set(State other) {
        this.state = other.state;
    }

    void updateLiteral() {
        this.state = this.state <= 3 ? 0 : (this.state <= 9 ? (this.state -= 3) : (this.state -= 6));
    }

    void updateMatch() {
        this.state = this.state < 7 ? 7 : 10;
    }

    void updateLongRep() {
        this.state = this.state < 7 ? 8 : 11;
    }

    void updateShortRep() {
        this.state = this.state < 7 ? 9 : 11;
    }

    boolean isLiteral() {
        return this.state < 7;
    }
}

