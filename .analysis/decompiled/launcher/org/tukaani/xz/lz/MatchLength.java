/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.lz;

/*
 * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
 */
final class MatchLength {
    MatchLength() {
    }

    static int getExtraSize() {
        return 0;
    }

    static int getLen(byte[] buf, int off, int delta, int len, int lenLimit) {
        int i;
        assert (off >= 0);
        assert (delta > 0);
        assert (len >= 0);
        assert (lenLimit >= len);
        lenLimit += off;
        for (i = off + len; i < lenLimit && buf[i] == buf[i - delta]; ++i) {
        }
        return i - off;
    }
}

