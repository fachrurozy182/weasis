/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.lz;

import java.nio.ByteOrder;
import org.tukaani.xz.lz.BasicMatchLengthFinder;
import org.tukaani.xz.lz.MatchLengthFinder;
import org.tukaani.xz.lz.UnalignedLongLEMatchLengthFinder;

/*
 * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
 */
final class MatchLength {
    private static final MatchLengthFinder matchLengthFinder;

    static int getExtraSize() {
        return matchLengthFinder.getExtraSize();
    }

    static int getLen(byte[] buf, int off, int delta, int len, int lenLimit) {
        assert (off >= 0);
        assert (delta > 0);
        assert (len >= 0);
        assert (lenLimit >= len);
        return matchLengthFinder.getLen(buf, off, delta, len, lenLimit);
    }

    private MatchLength() {
    }

    static {
        String prop = System.getProperty("org.tukaani.xz.MatchLengthFinder");
        if (prop == null) {
            String arch = System.getProperty("os.arch");
            prop = arch != null && arch.matches("^(amd64|x86_64|aarch64)$") && ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? "UnalignedLongLE" : "Basic";
        }
        switch (prop) {
            case "Basic": {
                matchLengthFinder = new BasicMatchLengthFinder();
                break;
            }
            case "UnalignedLongLE": {
                matchLengthFinder = new UnalignedLongLEMatchLengthFinder();
                break;
            }
            default: {
                throw new Error("Unsupported value '" + prop + "' in the system property org.tukaani.xz.MatchLengthFinder. Supported values: Basic, UnalignedLongLE");
            }
        }
    }
}

