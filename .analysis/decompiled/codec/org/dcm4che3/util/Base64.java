/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.util;

import java.io.IOException;
import java.io.OutputStream;

public class Base64 {
    private static final char[] BASE64 = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};
    private static final byte[] INV_BASE64 = new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51};

    public static void encode(byte[] src, int srcPos, int srcLen, char[] dest, int destPos) {
        byte b2;
        byte b1;
        if (srcPos < 0 || srcLen < 0 || srcLen > src.length - srcPos) {
            throw new IndexOutOfBoundsException();
        }
        int destLen = srcLen * 4 / 3 + 3 & 0xFFFFFFFC;
        if (destPos < 0 || destLen > dest.length - destPos) {
            throw new IndexOutOfBoundsException();
        }
        int n = srcLen / 3;
        int r = srcLen - 3 * n;
        while (n-- > 0) {
            int n2 = destPos++;
            b1 = src[srcPos++];
            dest[n2] = BASE64[b1 >>> 2 & 0x3F];
            int n3 = destPos++;
            b2 = src[srcPos++];
            dest[n3] = BASE64[(b1 & 3) << 4 | b2 >>> 4 & 0xF];
            int n4 = destPos++;
            byte b3 = src[srcPos++];
            dest[n4] = BASE64[(b2 & 0xF) << 2 | b3 >>> 6 & 3];
            dest[destPos++] = BASE64[b3 & 0x3F];
        }
        if (r > 0) {
            if (r == 1) {
                int n5 = destPos++;
                b1 = src[srcPos];
                dest[n5] = BASE64[b1 >>> 2 & 0x3F];
                dest[destPos++] = BASE64[(b1 & 3) << 4];
                dest[destPos++] = 61;
                dest[destPos++] = 61;
            } else {
                int n6 = destPos++;
                b1 = src[srcPos++];
                dest[n6] = BASE64[b1 >>> 2 & 0x3F];
                int n7 = destPos++;
                b2 = src[srcPos];
                dest[n7] = BASE64[(b1 & 3) << 4 | b2 >>> 4 & 0xF];
                dest[destPos++] = BASE64[(b2 & 0xF) << 2];
                dest[destPos++] = 61;
            }
        }
    }

    public static void decode(char[] ch, int off, int len, OutputStream out) throws IOException {
        while ((len -= 2) >= 0) {
            int n = off++;
            byte b2 = INV_BASE64[ch[off++]];
            out.write((byte)(INV_BASE64[ch[n]] << 2 | b2 >>> 4));
            if (len-- == 0 || ch[off] == '=') break;
            byte b3 = INV_BASE64[ch[off++]];
            out.write((byte)(b2 << 4 | b3 >>> 2));
            if (len-- == 0 || ch[off] == '=') break;
            out.write((byte)(b3 << 6 | INV_BASE64[ch[off++]]));
        }
    }
}

