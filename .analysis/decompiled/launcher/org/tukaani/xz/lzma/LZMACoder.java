/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.lzma;

import org.tukaani.xz.lzma.State;
import org.tukaani.xz.rangecoder.RangeCoder;

abstract class LZMACoder {
    static final int POS_STATES_MAX = 16;
    static final int MATCH_LEN_MIN = 2;
    static final int MATCH_LEN_MAX = 273;
    static final int DIST_STATES = 4;
    static final int DIST_SLOTS = 64;
    static final int DIST_MODEL_START = 4;
    static final int DIST_MODEL_END = 14;
    static final int FULL_DISTANCES = 128;
    static final int ALIGN_BITS = 4;
    static final int ALIGN_SIZE = 16;
    static final int ALIGN_MASK = 15;
    static final int REPS = 4;
    final int posMask;
    final int[] reps = new int[4];
    final State state = new State();
    final short[][] isMatch = new short[12][16];
    final short[] isRep = new short[12];
    final short[] isRep0 = new short[12];
    final short[] isRep1 = new short[12];
    final short[] isRep2 = new short[12];
    final short[][] isRep0Long = new short[12][16];
    final short[][] distSlots = new short[4][64];
    final short[][] distSpecial = new short[][]{new short[2], new short[2], new short[4], new short[4], new short[8], new short[8], new short[16], new short[16], new short[32], new short[32]};
    final short[] distAlign = new short[16];

    static final int getDistState(int len) {
        return len < 6 ? len - 2 : 3;
    }

    LZMACoder(int pb) {
        this.posMask = (1 << pb) - 1;
    }

    void reset() {
        int i;
        this.reps[0] = 0;
        this.reps[1] = 0;
        this.reps[2] = 0;
        this.reps[3] = 0;
        this.state.reset();
        for (i = 0; i < this.isMatch.length; ++i) {
            RangeCoder.initProbs(this.isMatch[i]);
        }
        RangeCoder.initProbs(this.isRep);
        RangeCoder.initProbs(this.isRep0);
        RangeCoder.initProbs(this.isRep1);
        RangeCoder.initProbs(this.isRep2);
        for (i = 0; i < this.isRep0Long.length; ++i) {
            RangeCoder.initProbs(this.isRep0Long[i]);
        }
        for (i = 0; i < this.distSlots.length; ++i) {
            RangeCoder.initProbs(this.distSlots[i]);
        }
        for (i = 0; i < this.distSpecial.length; ++i) {
            RangeCoder.initProbs(this.distSpecial[i]);
        }
        RangeCoder.initProbs(this.distAlign);
    }

    abstract class LengthCoder {
        static final int LOW_SYMBOLS = 8;
        static final int MID_SYMBOLS = 8;
        static final int HIGH_SYMBOLS = 256;
        final short[] choice = new short[2];
        final short[][] low = new short[16][8];
        final short[][] mid = new short[16][8];
        final short[] high = new short[256];

        LengthCoder() {
        }

        void reset() {
            int i;
            RangeCoder.initProbs(this.choice);
            for (i = 0; i < this.low.length; ++i) {
                RangeCoder.initProbs(this.low[i]);
            }
            for (i = 0; i < this.mid.length; ++i) {
                RangeCoder.initProbs(this.mid[i]);
            }
            RangeCoder.initProbs(this.high);
        }
    }

    abstract class LiteralCoder {
        private final int lc;
        private final int literalPosMask;

        LiteralCoder(int lc, int lp) {
            this.lc = lc;
            this.literalPosMask = (1 << lp) - 1;
        }

        final int getSubcoderIndex(int prevByte, int pos) {
            int low = prevByte >> 8 - this.lc;
            int high = (pos & this.literalPosMask) << this.lc;
            return low + high;
        }

        abstract class LiteralSubcoder {
            final short[] probs = new short[768];

            LiteralSubcoder() {
            }

            void reset() {
                RangeCoder.initProbs(this.probs);
            }
        }
    }
}

