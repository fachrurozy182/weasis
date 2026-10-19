/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MD5Util {
    static String MD5_ALGORITHM_KEY = "MD5";
    final MessageDigest md5 = MessageDigest.getInstance(MD5_ALGORITHM_KEY);

    public boolean equalsHash(byte[] b0, byte[] b1) {
        return Arrays.equals(b0, b1);
    }

    public byte[] md5Hash(String input) {
        byte[] messageDigest = this.md5.digest(input.getBytes());
        this.md5.reset();
        return messageDigest;
    }

    public String asHexString(byte[] messageDigest) {
        BigInteger number = new BigInteger(1, messageDigest);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }
        return hexString.toString();
    }
}

