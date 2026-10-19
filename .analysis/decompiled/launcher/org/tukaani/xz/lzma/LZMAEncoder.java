/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.lzma;

import java.io.IOException;
import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.lz.LZEncoder;
import org.tukaani.xz.lz.Matches;
import org.tukaani.xz.lzma.LZMACoder;
import org.tukaani.xz.lzma.LZMAEncoderFast;
import org.tukaani.xz.lzma.LZMAEncoderNormal;
import org.tukaani.xz.lzma.State;
import org.tukaani.xz.rangecoder.RangeEncoder;

public abstract class LZMAEncoder
extends LZMACoder {
    public static final int MODE_FAST = 1;
    public static final int MODE_NORMAL = 2;
    private static final int LZMA2_UNCOMPRESSED_LIMIT = 0x1FFEEF;
    private static final int LZMA2_COMPRESSED_LIMIT = 65510;
    private static final int DIST_PRICE_UPDATE_INTERVAL = 128;
    private static final int ALIGN_PRICE_UPDATE_INTERVAL = 16;
    private final RangeEncoder rc;
    final LZEncoder lz;
    final LiteralEncoder literalEncoder;
    final LengthEncoder matchLenEncoder;
    final LengthEncoder repLenEncoder;
    final int niceLen;
    private int distPriceCount = 0;
    private int alignPriceCount = 0;
    private final int distSlotPricesSize;
    private final int[][] distSlotPrices;
    private final int[][] fullDistPrices = new int[4][128];
    private final int[] alignPrices = new int[16];
    int back = 0;
    int readAhead = -1;
    private int uncompressedSize = 0;

    public static int getMemoryUsage(int mode, int dictSize, int extraSizeBefore, int mf) {
        int m = 80;
        switch (mode) {
            case 1: {
                m += LZMAEncoderFast.getMemoryUsage(dictSize, extraSizeBefore, mf);
                break;
            }
            case 2: {
                m += LZMAEncoderNormal.getMemoryUsage(dictSize, extraSizeBefore, mf);
                break;
            }
            default: {
                throw new IllegalArgumentException();
            }
        }
        return m;
    }

    public static LZMAEncoder getInstance(RangeEncoder rc, int lc, int lp, int pb, int mode, int dictSize, int extraSizeBefore, int niceLen, int mf, int depthLimit, ArrayCache arrayCache) {
        switch (mode) {
            case 1: {
                return new LZMAEncoderFast(rc, lc, lp, pb, dictSize, extraSizeBefore, niceLen, mf, depthLimit, arrayCache);
            }
            case 2: {
                return new LZMAEncoderNormal(rc, lc, lp, pb, dictSize, extraSizeBefore, niceLen, mf, depthLimit, arrayCache);
            }
        }
        throw new IllegalArgumentException();
    }

    public void putArraysToCache(ArrayCache arrayCache) {
        this.lz.putArraysToCache(arrayCache);
    }

    public static int getDistSlot(int dist) {
        if (dist <= 4 && dist >= 0) {
            return dist;
        }
        int i = 31 - Integer.numberOfLeadingZeros(dist);
        return (i << 1) + (dist >>> i - 1 & 1);
    }

    abstract int getNextSymbol();

    LZMAEncoder(RangeEncoder rc, LZEncoder lz, int lc, int lp, int pb, int dictSize, int niceLen) {
        super(pb);
        this.rc = rc;
        this.lz = lz;
        this.niceLen = niceLen;
        this.literalEncoder = new LiteralEncoder(lc, lp);
        this.matchLenEncoder = new LengthEncoder(pb, niceLen);
        this.repLenEncoder = new LengthEncoder(pb, niceLen);
        this.distSlotPricesSize = LZMAEncoder.getDistSlot(dictSize - 1) + 1;
        this.distSlotPrices = new int[4][this.distSlotPricesSize];
        this.reset();
    }

    public LZEncoder getLZEncoder() {
        return this.lz;
    }

    @Override
    public void reset() {
        super.reset();
        this.literalEncoder.reset();
        this.matchLenEncoder.reset();
        this.repLenEncoder.reset();
        this.distPriceCount = 0;
        this.alignPriceCount = 0;
        this.uncompressedSize += this.readAhead + 1;
        this.readAhead = -1;
    }

    public int getUncompressedSize() {
        return this.uncompressedSize;
    }

    public void resetUncompressedSize() {
        this.uncompressedSize = 0;
    }

    public void encodeForLZMA1() throws IOException {
        if (!this.lz.isStarted() && !this.encodeInit()) {
            return;
        }
        while (this.encodeSymbol()) {
        }
    }

    public void encodeLZMA1EndMarker() throws IOException {
        int posState = this.lz.getPos() - this.readAhead & this.posMask;
        this.rc.encodeBit(this.isMatch[this.state.get()], posState, 1);
        this.rc.encodeBit(this.isRep, this.state.get(), 0);
        this.encodeMatch(-1, 2, posState);
    }

    public boolean encodeForLZMA2() {
        try {
            if (!this.lz.isStarted() && !this.encodeInit()) {
                return false;
            }
            while (this.uncompressedSize <= 0x1FFEEF && this.rc.getPendingSize() <= 65510) {
                if (this.encodeSymbol()) continue;
                return false;
            }
        }
        catch (IOException e) {
            throw new Error();
        }
        return true;
    }

    private boolean encodeInit() throws IOException {
        assert (this.readAhead == -1);
        if (!this.lz.hasEnoughData(0)) {
            return false;
        }
        this.skip(1);
        this.rc.encodeBit(this.isMatch[this.state.get()], 0, 0);
        this.literalEncoder.encodeInit();
        --this.readAhead;
        assert (this.readAhead == -1);
        ++this.uncompressedSize;
        assert (this.uncompressedSize == 1);
        return true;
    }

    private boolean encodeSymbol() throws IOException {
        if (!this.lz.hasEnoughData(this.readAhead + 1)) {
            return false;
        }
        int len = this.getNextSymbol();
        assert (this.readAhead >= 0);
        int posState = this.lz.getPos() - this.readAhead & this.posMask;
        if (this.back == -1) {
            assert (len == 1);
            this.rc.encodeBit(this.isMatch[this.state.get()], posState, 0);
            this.literalEncoder.encode();
        } else {
            this.rc.encodeBit(this.isMatch[this.state.get()], posState, 1);
            if (this.back < 4) {
                assert (this.lz.getMatchLen(-this.readAhead, this.reps[this.back], len) == len);
                this.rc.encodeBit(this.isRep, this.state.get(), 1);
                this.encodeRepMatch(this.back, len, posState);
            } else {
                assert (this.lz.getMatchLen(-this.readAhead, this.back - 4, len) == len);
                this.rc.encodeBit(this.isRep, this.state.get(), 0);
                this.encodeMatch(this.back - 4, len, posState);
            }
        }
        this.readAhead -= len;
        this.uncompressedSize += len;
        return true;
    }

    private void encodeMatch(int dist, int len, int posState) throws IOException {
        this.state.updateMatch();
        this.matchLenEncoder.encode(len, posState);
        int distSlot = LZMAEncoder.getDistSlot(dist);
        this.rc.encodeBitTree(this.distSlots[LZMAEncoder.getDistState(len)], distSlot);
        if (distSlot >= 4) {
            int footerBits = (distSlot >>> 1) - 1;
            int base = (2 | distSlot & 1) << footerBits;
            int distReduced = dist - base;
            if (distSlot < 14) {
                this.rc.encodeReverseBitTree(this.distSpecial[distSlot - 4], distReduced);
            } else {
                this.rc.encodeDirectBits(distReduced >>> 4, footerBits - 4);
                this.rc.encodeReverseBitTree(this.distAlign, distReduced & 0xF);
                --this.alignPriceCount;
            }
        }
        this.reps[3] = this.reps[2];
        this.reps[2] = this.reps[1];
        this.reps[1] = this.reps[0];
        this.reps[0] = dist;
        --this.distPriceCount;
    }

    private void encodeRepMatch(int rep, int len, int posState) throws IOException {
        if (rep == 0) {
            this.rc.encodeBit(this.isRep0, this.state.get(), 0);
            this.rc.encodeBit(this.isRep0Long[this.state.get()], posState, len == 1 ? 0 : 1);
        } else {
            int dist = this.reps[rep];
            this.rc.encodeBit(this.isRep0, this.state.get(), 1);
            if (rep == 1) {
                this.rc.encodeBit(this.isRep1, this.state.get(), 0);
            } else {
                this.rc.encodeBit(this.isRep1, this.state.get(), 1);
                this.rc.encodeBit(this.isRep2, this.state.get(), rep - 2);
                if (rep == 3) {
                    this.reps[3] = this.reps[2];
                }
                this.reps[2] = this.reps[1];
            }
            this.reps[1] = this.reps[0];
            this.reps[0] = dist;
        }
        if (len == 1) {
            this.state.updateShortRep();
        } else {
            this.repLenEncoder.encode(len, posState);
            this.state.updateLongRep();
        }
    }

    Matches getMatches() {
        ++this.readAhead;
        Matches matches = this.lz.getMatches();
        assert (this.lz.verifyMatches(matches));
        return matches;
    }

    void skip(int len) {
        this.readAhead += len;
        this.lz.skip(len);
    }

    int getAnyMatchPrice(State state, int posState) {
        return RangeEncoder.getBitPrice(this.isMatch[state.get()][posState], 1);
    }

    int getNormalMatchPrice(int anyMatchPrice, State state) {
        return anyMatchPrice + RangeEncoder.getBitPrice(this.isRep[state.get()], 0);
    }

    int getAnyRepPrice(int anyMatchPrice, State state) {
        return anyMatchPrice + RangeEncoder.getBitPrice(this.isRep[state.get()], 1);
    }

    int getShortRepPrice(int anyRepPrice, State state, int posState) {
        return anyRepPrice + RangeEncoder.getBitPrice(this.isRep0[state.get()], 0) + RangeEncoder.getBitPrice(this.isRep0Long[state.get()][posState], 0);
    }

    int getLongRepPrice(int anyRepPrice, int rep, State state, int posState) {
        int price = anyRepPrice;
        if (rep == 0) {
            price += RangeEncoder.getBitPrice(this.isRep0[state.get()], 0) + RangeEncoder.getBitPrice(this.isRep0Long[state.get()][posState], 1);
        } else {
            price += RangeEncoder.getBitPrice(this.isRep0[state.get()], 1);
            price = rep == 1 ? (price += RangeEncoder.getBitPrice(this.isRep1[state.get()], 0)) : (price += RangeEncoder.getBitPrice(this.isRep1[state.get()], 1) + RangeEncoder.getBitPrice(this.isRep2[state.get()], rep - 2));
        }
        return price;
    }

    int getLongRepAndLenPrice(int rep, int len, State state, int posState) {
        int anyMatchPrice = this.getAnyMatchPrice(state, posState);
        int anyRepPrice = this.getAnyRepPrice(anyMatchPrice, state);
        int longRepPrice = this.getLongRepPrice(anyRepPrice, rep, state, posState);
        return longRepPrice + this.repLenEncoder.getPrice(len, posState);
    }

    int getMatchAndLenPrice(int normalMatchPrice, int dist, int len, int posState) {
        int price = normalMatchPrice + this.matchLenEncoder.getPrice(len, posState);
        int distState = LZMAEncoder.getDistState(len);
        if (dist < 128) {
            price += this.fullDistPrices[distState][dist];
        } else {
            int distSlot = LZMAEncoder.getDistSlot(dist);
            price += this.distSlotPrices[distState][distSlot] + this.alignPrices[dist & 0xF];
        }
        return price;
    }

    private void updateDistPrices() {
        int distSlot;
        this.distPriceCount = 128;
        for (int distState = 0; distState < 4; ++distState) {
            for (distSlot = 0; distSlot < this.distSlotPricesSize; ++distSlot) {
                this.distSlotPrices[distState][distSlot] = RangeEncoder.getBitTreePrice(this.distSlots[distState], distSlot);
            }
            distSlot = 14;
            while (distSlot < this.distSlotPricesSize) {
                int count = (distSlot >>> 1) - 1 - 4;
                int[] nArray = this.distSlotPrices[distState];
                int n = distSlot++;
                nArray[n] = nArray[n] + RangeEncoder.getDirectBitsPrice(count);
            }
            for (int dist = 0; dist < 4; ++dist) {
                this.fullDistPrices[distState][dist] = this.distSlotPrices[distState][dist];
            }
        }
        int dist = 4;
        for (distSlot = 4; distSlot < 14; ++distSlot) {
            int footerBits = (distSlot >>> 1) - 1;
            int base = (2 | distSlot & 1) << footerBits;
            int limit = this.distSpecial[distSlot - 4].length;
            for (int i = 0; i < limit; ++i) {
                int distReduced = dist - base;
                int price = RangeEncoder.getReverseBitTreePrice(this.distSpecial[distSlot - 4], distReduced);
                for (int distState = 0; distState < 4; ++distState) {
                    this.fullDistPrices[distState][dist] = this.distSlotPrices[distState][distSlot] + price;
                }
                ++dist;
            }
        }
        assert (dist == 128);
    }

    private void updateAlignPrices() {
        this.alignPriceCount = 16;
        for (int i = 0; i < 16; ++i) {
            this.alignPrices[i] = RangeEncoder.getReverseBitTreePrice(this.distAlign, i);
        }
    }

    void updatePrices() {
        if (this.distPriceCount <= 0) {
            this.updateDistPrices();
        }
        if (this.alignPriceCount <= 0) {
            this.updateAlignPrices();
        }
        this.matchLenEncoder.updatePrices();
        this.repLenEncoder.updatePrices();
    }

    class LiteralEncoder
    extends LZMACoder.LiteralCoder {
        private final LiteralSubencoder[] subencoders;

        LiteralEncoder(int lc, int lp) {
            super(lc, lp);
            this.subencoders = new LiteralSubencoder[1 << lc + lp];
            for (int i = 0; i < this.subencoders.length; ++i) {
                this.subencoders[i] = new LiteralSubencoder();
            }
        }

        void reset() {
            for (int i = 0; i < this.subencoders.length; ++i) {
                this.subencoders[i].reset();
            }
        }

        void encodeInit() throws IOException {
            assert (LZMAEncoder.this.readAhead >= 0);
            this.subencoders[0].encode();
        }

        void encode() throws IOException {
            assert (LZMAEncoder.this.readAhead >= 0);
            int i = this.getSubcoderIndex(LZMAEncoder.this.lz.getByte(1 + LZMAEncoder.this.readAhead), LZMAEncoder.this.lz.getPos() - LZMAEncoder.this.readAhead);
            this.subencoders[i].encode();
        }

        int getPrice(int curByte, int matchByte, int prevByte, int pos, State state) {
            int price = RangeEncoder.getBitPrice(LZMAEncoder.this.isMatch[state.get()][pos & LZMAEncoder.this.posMask], 0);
            int i = this.getSubcoderIndex(prevByte, pos);
            return price += state.isLiteral() ? this.subencoders[i].getNormalPrice(curByte) : this.subencoders[i].getMatchedPrice(curByte, matchByte);
        }

        private class LiteralSubencoder
        extends LZMACoder.LiteralCoder.LiteralSubcoder {
            private LiteralSubencoder() {
            }

            void encode() throws IOException {
                int symbol = LZMAEncoder.this.lz.getByte(LZMAEncoder.this.readAhead) | 0x100;
                if (LZMAEncoder.this.state.isLiteral()) {
                    do {
                        int subencoderIndex = symbol >>> 8;
                        int bit = symbol >>> 7 & 1;
                        LZMAEncoder.this.rc.encodeBit(this.probs, subencoderIndex, bit);
                    } while ((symbol <<= 1) < 65536);
                } else {
                    int matchByte = LZMAEncoder.this.lz.getByte(LZMAEncoder.this.reps[0] + 1 + LZMAEncoder.this.readAhead);
                    int offset = 256;
                    do {
                        int matchBit = (matchByte <<= 1) & offset;
                        int subencoderIndex = offset + matchBit + (symbol >>> 8);
                        int bit = symbol >>> 7 & 1;
                        LZMAEncoder.this.rc.encodeBit(this.probs, subencoderIndex, bit);
                        offset &= ~(matchByte ^ (symbol <<= 1));
                    } while (symbol < 65536);
                }
                LZMAEncoder.this.state.updateLiteral();
            }

            int getNormalPrice(int symbol) {
                int price = 0;
                symbol |= 0x100;
                do {
                    int subencoderIndex = symbol >>> 8;
                    int bit = symbol >>> 7 & 1;
                    price += RangeEncoder.getBitPrice(this.probs[subencoderIndex], bit);
                } while ((symbol <<= 1) < 65536);
                return price;
            }

            int getMatchedPrice(int symbol, int matchByte) {
                int price = 0;
                int offset = 256;
                symbol |= 0x100;
                do {
                    int matchBit = (matchByte <<= 1) & offset;
                    int subencoderIndex = offset + matchBit + (symbol >>> 8);
                    int bit = symbol >>> 7 & 1;
                    price += RangeEncoder.getBitPrice(this.probs[subencoderIndex], bit);
                    offset &= ~(matchByte ^ (symbol <<= 1));
                } while (symbol < 65536);
                return price;
            }
        }
    }

    class LengthEncoder
    extends LZMACoder.LengthCoder {
        private static final int PRICE_UPDATE_INTERVAL = 32;
        private final int[] counters;
        private final int[][] prices;

        LengthEncoder(int pb, int niceLen) {
            int posStates = 1 << pb;
            this.counters = new int[posStates];
            int lenSymbols = Math.max(niceLen - 2 + 1, 16);
            this.prices = new int[posStates][lenSymbols];
        }

        @Override
        void reset() {
            super.reset();
            for (int i = 0; i < this.counters.length; ++i) {
                this.counters[i] = 0;
            }
        }

        void encode(int len, int posState) throws IOException {
            if ((len -= 2) < 8) {
                LZMAEncoder.this.rc.encodeBit(this.choice, 0, 0);
                LZMAEncoder.this.rc.encodeBitTree(this.low[posState], len);
            } else {
                LZMAEncoder.this.rc.encodeBit(this.choice, 0, 1);
                if ((len -= 8) < 8) {
                    LZMAEncoder.this.rc.encodeBit(this.choice, 1, 0);
                    LZMAEncoder.this.rc.encodeBitTree(this.mid[posState], len);
                } else {
                    LZMAEncoder.this.rc.encodeBit(this.choice, 1, 1);
                    LZMAEncoder.this.rc.encodeBitTree(this.high, len - 8);
                }
            }
            int n = posState;
            this.counters[n] = this.counters[n] - 1;
        }

        int getPrice(int len, int posState) {
            return this.prices[posState][len - 2];
        }

        void updatePrices() {
            for (int posState = 0; posState < this.counters.length; ++posState) {
                if (this.counters[posState] > 0) continue;
                this.counters[posState] = 32;
                this.updatePrices(posState);
            }
        }

        private void updatePrices(int posState) {
            int i;
            int choice0Price = RangeEncoder.getBitPrice(this.choice[0], 0);
            for (i = 0; i < 8; ++i) {
                this.prices[posState][i] = choice0Price + RangeEncoder.getBitTreePrice(this.low[posState], i);
            }
            choice0Price = RangeEncoder.getBitPrice(this.choice[0], 1);
            int choice1Price = RangeEncoder.getBitPrice(this.choice[1], 0);
            while (i < 16) {
                this.prices[posState][i] = choice0Price + choice1Price + RangeEncoder.getBitTreePrice(this.mid[posState], i - 8);
                ++i;
            }
            choice1Price = RangeEncoder.getBitPrice(this.choice[1], 1);
            while (i < this.prices[posState].length) {
                this.prices[posState][i] = choice0Price + choice1Price + RangeEncoder.getBitTreePrice(this.high, i - 8 - 8);
                ++i;
            }
        }
    }
}

