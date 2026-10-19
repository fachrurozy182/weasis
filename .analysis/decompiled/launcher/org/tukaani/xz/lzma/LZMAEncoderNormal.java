/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.lzma;

import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.lz.LZEncoder;
import org.tukaani.xz.lz.Matches;
import org.tukaani.xz.lzma.LZMAEncoder;
import org.tukaani.xz.lzma.Optimum;
import org.tukaani.xz.lzma.State;
import org.tukaani.xz.rangecoder.RangeEncoder;

final class LZMAEncoderNormal
extends LZMAEncoder {
    private static final int OPTS = 4096;
    private static final int EXTRA_SIZE_BEFORE = 4096;
    private static final int EXTRA_SIZE_AFTER = 4096;
    private final Optimum[] opts = new Optimum[4096];
    private int optCur = 0;
    private int optEnd = 0;
    private Matches matches;
    private final int[] repLens = new int[4];
    private final State nextState = new State();

    static int getMemoryUsage(int dictSize, int extraSizeBefore, int mf) {
        return LZEncoder.getMemoryUsage(dictSize, Math.max(extraSizeBefore, 4096), 4096, 273, mf) + 256;
    }

    LZMAEncoderNormal(RangeEncoder rc, int lc, int lp, int pb, int dictSize, int extraSizeBefore, int niceLen, int mf, int depthLimit, ArrayCache arrayCache) {
        super(rc, LZEncoder.getInstance(dictSize, Math.max(extraSizeBefore, 4096), 4096, niceLen, 273, mf, depthLimit, arrayCache), lc, lp, pb, dictSize, niceLen);
        for (int i = 0; i < 4096; ++i) {
            this.opts[i] = new Optimum();
        }
    }

    @Override
    public void reset() {
        this.optCur = 0;
        this.optEnd = 0;
        super.reset();
    }

    private int convertOpts() {
        this.optEnd = this.optCur;
        int optPrev = this.opts[this.optCur].optPrev;
        do {
            Optimum opt = this.opts[this.optCur];
            if (opt.prev1IsLiteral) {
                this.opts[optPrev].optPrev = this.optCur;
                this.opts[optPrev].backPrev = -1;
                this.optCur = optPrev--;
                if (opt.hasPrev2) {
                    this.opts[optPrev].optPrev = optPrev + 1;
                    this.opts[optPrev].backPrev = opt.backPrev2;
                    this.optCur = optPrev;
                    optPrev = opt.optPrev2;
                }
            }
            int temp = this.opts[optPrev].optPrev;
            this.opts[optPrev].optPrev = this.optCur;
            this.optCur = optPrev;
            optPrev = temp;
        } while (this.optCur > 0);
        this.optCur = this.opts[0].optPrev;
        this.back = this.opts[this.optCur].backPrev;
        return this.optCur;
    }

    @Override
    int getNextSymbol() {
        int shortRepPrice;
        int avail;
        if (this.optCur < this.optEnd) {
            int len = this.opts[this.optCur].optPrev - this.optCur;
            this.optCur = this.opts[this.optCur].optPrev;
            this.back = this.opts[this.optCur].backPrev;
            return len;
        }
        assert (this.optCur == this.optEnd);
        this.optCur = 0;
        this.optEnd = 0;
        this.back = -1;
        if (this.readAhead == -1) {
            this.matches = this.getMatches();
        }
        if ((avail = Math.min(this.lz.getAvail(), 273)) < 2) {
            return 1;
        }
        int repBest = 0;
        for (int rep = 0; rep < 4; ++rep) {
            this.repLens[rep] = this.lz.getMatchLen(this.reps[rep], avail);
            if (this.repLens[rep] < 2) {
                this.repLens[rep] = 0;
                continue;
            }
            if (this.repLens[rep] <= this.repLens[repBest]) continue;
            repBest = rep;
        }
        if (this.repLens[repBest] >= this.niceLen) {
            this.back = repBest;
            this.skip(this.repLens[repBest] - 1);
            return this.repLens[repBest];
        }
        int mainLen = 0;
        int mainDist = 0;
        if (this.matches.count > 0) {
            mainLen = this.matches.len[this.matches.count - 1];
            mainDist = this.matches.dist[this.matches.count - 1];
            if (mainLen >= this.niceLen) {
                this.back = mainDist + 4;
                this.skip(mainLen - 1);
                return mainLen;
            }
        }
        int curByte = this.lz.getByte(0);
        int matchByte = this.lz.getByte(this.reps[0] + 1);
        if (mainLen < 2 && curByte != matchByte && this.repLens[repBest] < 2) {
            return 1;
        }
        int pos = this.lz.getPos();
        int posState = pos & this.posMask;
        int prevByte = this.lz.getByte(1);
        int literalPrice = this.literalEncoder.getPrice(curByte, matchByte, prevByte, pos, this.state);
        this.opts[1].set1(literalPrice, 0, -1);
        int anyMatchPrice = this.getAnyMatchPrice(this.state, posState);
        int anyRepPrice = this.getAnyRepPrice(anyMatchPrice, this.state);
        if (matchByte == curByte && (shortRepPrice = this.getShortRepPrice(anyRepPrice, this.state, posState)) < this.opts[1].price) {
            this.opts[1].set1(shortRepPrice, 0, 0);
        }
        this.optEnd = Math.max(mainLen, this.repLens[repBest]);
        if (this.optEnd < 2) {
            assert (this.optEnd == 0) : this.optEnd;
            this.back = this.opts[1].backPrev;
            return 1;
        }
        this.updatePrices();
        this.opts[0].state.set(this.state);
        System.arraycopy(this.reps, 0, this.opts[0].reps, 0, 4);
        for (int i = this.optEnd; i >= 2; --i) {
            this.opts[i].reset();
        }
        for (int rep = 0; rep < 4; ++rep) {
            int repLen = this.repLens[rep];
            if (repLen < 2) continue;
            int longRepPrice = this.getLongRepPrice(anyRepPrice, rep, this.state, posState);
            do {
                int price;
                if ((price = longRepPrice + this.repLenEncoder.getPrice(repLen, posState)) >= this.opts[repLen].price) continue;
                this.opts[repLen].set1(price, 0, rep);
            } while (--repLen >= 2);
        }
        int len = Math.max(this.repLens[0] + 1, 2);
        if (len <= mainLen) {
            int normalMatchPrice = this.getNormalMatchPrice(anyMatchPrice, this.state);
            int i = 0;
            while (len > this.matches.len[i]) {
                ++i;
            }
            while (true) {
                int dist;
                int price;
                if ((price = this.getMatchAndLenPrice(normalMatchPrice, dist = this.matches.dist[i], len, posState)) < this.opts[len].price) {
                    this.opts[len].set1(price, 0, dist + 4);
                }
                if (len == this.matches.len[i] && ++i == this.matches.count) break;
                ++len;
            }
        }
        avail = Math.min(this.lz.getAvail(), 4095);
        while (++this.optCur < this.optEnd) {
            this.matches = this.getMatches();
            if (this.matches.count > 0 && this.matches.len[this.matches.count - 1] >= this.niceLen) break;
            posState = ++pos & this.posMask;
            this.updateOptStateAndReps();
            anyMatchPrice = this.opts[this.optCur].price + this.getAnyMatchPrice(this.opts[this.optCur].state, posState);
            anyRepPrice = this.getAnyRepPrice(anyMatchPrice, this.opts[this.optCur].state);
            this.calc1BytePrices(pos, posState, --avail, anyRepPrice);
            if (avail < 2) continue;
            int startLen = this.calcLongRepPrices(pos, posState, avail, anyRepPrice);
            if (this.matches.count <= 0) continue;
            this.calcNormalMatchPrices(pos, posState, avail, anyMatchPrice, startLen);
        }
        return this.convertOpts();
    }

    private void updateOptStateAndReps() {
        int optPrev = this.opts[this.optCur].optPrev;
        assert (optPrev < this.optCur);
        if (this.opts[this.optCur].prev1IsLiteral) {
            --optPrev;
            if (this.opts[this.optCur].hasPrev2) {
                this.opts[this.optCur].state.set(this.opts[this.opts[this.optCur].optPrev2].state);
                if (this.opts[this.optCur].backPrev2 < 4) {
                    this.opts[this.optCur].state.updateLongRep();
                } else {
                    this.opts[this.optCur].state.updateMatch();
                }
            } else {
                this.opts[this.optCur].state.set(this.opts[optPrev].state);
            }
            this.opts[this.optCur].state.updateLiteral();
        } else {
            this.opts[this.optCur].state.set(this.opts[optPrev].state);
        }
        if (optPrev == this.optCur - 1) {
            assert (this.opts[this.optCur].backPrev == 0 || this.opts[this.optCur].backPrev == -1);
            if (this.opts[this.optCur].backPrev == 0) {
                this.opts[this.optCur].state.updateShortRep();
            } else {
                this.opts[this.optCur].state.updateLiteral();
            }
            System.arraycopy(this.opts[optPrev].reps, 0, this.opts[this.optCur].reps, 0, 4);
        } else {
            int back;
            if (this.opts[this.optCur].prev1IsLiteral && this.opts[this.optCur].hasPrev2) {
                optPrev = this.opts[this.optCur].optPrev2;
                back = this.opts[this.optCur].backPrev2;
                this.opts[this.optCur].state.updateLongRep();
            } else {
                back = this.opts[this.optCur].backPrev;
                if (back < 4) {
                    this.opts[this.optCur].state.updateLongRep();
                } else {
                    this.opts[this.optCur].state.updateMatch();
                }
            }
            if (back < 4) {
                int rep;
                this.opts[this.optCur].reps[0] = this.opts[optPrev].reps[back];
                for (rep = 1; rep <= back; ++rep) {
                    this.opts[this.optCur].reps[rep] = this.opts[optPrev].reps[rep - 1];
                }
                while (rep < 4) {
                    this.opts[this.optCur].reps[rep] = this.opts[optPrev].reps[rep];
                    ++rep;
                }
            } else {
                this.opts[this.optCur].reps[0] = back - 4;
                System.arraycopy(this.opts[optPrev].reps, 0, this.opts[this.optCur].reps, 1, 3);
            }
        }
    }

    private void calc1BytePrices(int pos, int posState, int avail, int anyRepPrice) {
        int lenLimit;
        int len;
        int shortRepPrice;
        int matchByte;
        boolean nextIsByte = false;
        int curByte = this.lz.getByte(0);
        int literalPrice = this.opts[this.optCur].price + this.literalEncoder.getPrice(curByte, matchByte = this.lz.getByte(this.opts[this.optCur].reps[0] + 1), this.lz.getByte(1), pos, this.opts[this.optCur].state);
        if (literalPrice < this.opts[this.optCur + 1].price) {
            this.opts[this.optCur + 1].set1(literalPrice, this.optCur, -1);
            nextIsByte = true;
        }
        if (matchByte == curByte && (this.opts[this.optCur + 1].optPrev == this.optCur || this.opts[this.optCur + 1].backPrev != 0) && (shortRepPrice = this.getShortRepPrice(anyRepPrice, this.opts[this.optCur].state, posState)) <= this.opts[this.optCur + 1].price) {
            this.opts[this.optCur + 1].set1(shortRepPrice, this.optCur, 0);
            nextIsByte = true;
        }
        if (!nextIsByte && matchByte != curByte && avail > 2 && (len = this.lz.getMatchLen(1, this.opts[this.optCur].reps[0], lenLimit = Math.min(this.niceLen, avail - 1))) >= 2) {
            this.nextState.set(this.opts[this.optCur].state);
            this.nextState.updateLiteral();
            int nextPosState = pos + 1 & this.posMask;
            int price = literalPrice + this.getLongRepAndLenPrice(0, len, this.nextState, nextPosState);
            int i = this.optCur + 1 + len;
            while (this.optEnd < i) {
                this.opts[++this.optEnd].reset();
            }
            if (price < this.opts[i].price) {
                this.opts[i].set2(price, this.optCur, 0);
            }
        }
    }

    private int calcLongRepPrices(int pos, int posState, int avail, int anyRepPrice) {
        int startLen = 2;
        int lenLimit = Math.min(avail, this.niceLen);
        for (int rep = 0; rep < 4; ++rep) {
            int len2;
            int len2Limit;
            int len = this.lz.getMatchLen(this.opts[this.optCur].reps[rep], lenLimit);
            if (len < 2) continue;
            while (this.optEnd < this.optCur + len) {
                this.opts[++this.optEnd].reset();
            }
            int longRepPrice = this.getLongRepPrice(anyRepPrice, rep, this.opts[this.optCur].state, posState);
            for (int i = len; i >= 2; --i) {
                int price = longRepPrice + this.repLenEncoder.getPrice(i, posState);
                if (price >= this.opts[this.optCur + i].price) continue;
                this.opts[this.optCur + i].set1(price, this.optCur, rep);
            }
            if (rep == 0) {
                startLen = len + 1;
            }
            if ((len2Limit = avail - len - 1) < 2) continue;
            if (len2Limit > this.niceLen) {
                len2Limit = this.niceLen;
            }
            if ((len2 = this.lz.getMatchLen(len + 1, this.opts[this.optCur].reps[rep], len2Limit)) < 2) continue;
            int price = longRepPrice + this.repLenEncoder.getPrice(len, posState);
            this.nextState.set(this.opts[this.optCur].state);
            this.nextState.updateLongRep();
            int curByte = this.lz.getByte(len, 0);
            int matchByte = this.lz.getByte(0);
            int prevByte = this.lz.getByte(len, 1);
            price += this.literalEncoder.getPrice(curByte, matchByte, prevByte, pos + len, this.nextState);
            this.nextState.updateLiteral();
            int nextPosState = pos + len + 1 & this.posMask;
            price += this.getLongRepAndLenPrice(0, len2, this.nextState, nextPosState);
            int i = this.optCur + len + 1 + len2;
            while (this.optEnd < i) {
                this.opts[++this.optEnd].reset();
            }
            if (price >= this.opts[i].price) continue;
            this.opts[i].set3(price, this.optCur, rep, len, 0);
        }
        return startLen;
    }

    private void calcNormalMatchPrices(int pos, int posState, int avail, int anyMatchPrice, int startLen) {
        if (this.matches.len[this.matches.count - 1] > avail) {
            this.matches.count = 0;
            while (this.matches.len[this.matches.count] < avail) {
                ++this.matches.count;
            }
            this.matches.len[this.matches.count++] = avail;
        }
        if (this.matches.len[this.matches.count - 1] < startLen) {
            return;
        }
        while (this.optEnd < this.optCur + this.matches.len[this.matches.count - 1]) {
            this.opts[++this.optEnd].reset();
        }
        int normalMatchPrice = this.getNormalMatchPrice(anyMatchPrice, this.opts[this.optCur].state);
        int match = 0;
        while (startLen > this.matches.len[match]) {
            ++match;
        }
        int len = startLen;
        while (true) {
            int dist;
            int matchAndLenPrice;
            if ((matchAndLenPrice = this.getMatchAndLenPrice(normalMatchPrice, dist = this.matches.dist[match], len, posState)) < this.opts[this.optCur + len].price) {
                this.opts[this.optCur + len].set1(matchAndLenPrice, this.optCur, dist + 4);
            }
            if (len == this.matches.len[match]) {
                int len2Limit = avail - len - 1;
                if (len2Limit >= 2) {
                    int len2;
                    if (len2Limit > this.niceLen) {
                        len2Limit = this.niceLen;
                    }
                    if ((len2 = this.lz.getMatchLen(len + 1, dist, len2Limit)) >= 2) {
                        this.nextState.set(this.opts[this.optCur].state);
                        this.nextState.updateMatch();
                        int curByte = this.lz.getByte(len, 0);
                        int matchByte = this.lz.getByte(0);
                        int prevByte = this.lz.getByte(len, 1);
                        int price = matchAndLenPrice + this.literalEncoder.getPrice(curByte, matchByte, prevByte, pos + len, this.nextState);
                        this.nextState.updateLiteral();
                        int nextPosState = pos + len + 1 & this.posMask;
                        price += this.getLongRepAndLenPrice(0, len2, this.nextState, nextPosState);
                        int i = this.optCur + len + 1 + len2;
                        while (this.optEnd < i) {
                            this.opts[++this.optEnd].reset();
                        }
                        if (price < this.opts[i].price) {
                            this.opts[i].set3(price, this.optCur, dist + 4, len, 0);
                        }
                    }
                }
                if (++match == this.matches.count) break;
            }
            ++len;
        }
    }
}

