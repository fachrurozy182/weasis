/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.weasis.core.util.StringUtil;

public class Hmac {
    public static final int KEY_BYTE_LENGTH = 16;
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final int EXPECTED_HEX_KEY_LENGTH = 32;
    private static final String HEX_DIGITS = "0123456789abcdef";
    private static final char[] HEX_LOOKUP = "0123456789abcdef".toCharArray();
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final Mac mac;

    public Hmac() {
        this(Hmac.generateRandomKey());
    }

    public Hmac(byte[] hmacKey) {
        this.mac = Hmac.initHMAC(Objects.requireNonNull(hmacKey, "HMAC key cannot be null"));
    }

    private static Mac initHMAC(byte[] keyValue) {
        try {
            SecretKeySpec key = new SecretKeySpec(keyValue, HMAC_SHA256);
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(key);
            return mac;
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("HMAC algorithm not available", e);
        }
        catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid HMAC key", e);
        }
    }

    public static byte[] generateRandomKey() {
        byte[] bytes = new byte[16];
        SECURE_RANDOM.nextBytes(bytes);
        return bytes;
    }

    public static String byteToHex(byte[] byteArray) {
        Objects.requireNonNull(byteArray, "Byte array cannot be null");
        char[] hexChars = new char[byteArray.length * 2];
        for (int i = 0; i < byteArray.length; ++i) {
            int byteValue = byteArray[i] & 0xFF;
            hexChars[i * 2] = HEX_LOOKUP[byteValue >>> 4];
            hexChars[i * 2 + 1] = HEX_LOOKUP[byteValue & 0xF];
        }
        return new String(hexChars);
    }

    public static String showHexKey(String key) {
        Objects.requireNonNull(key, "Key cannot be null");
        if (key.length() != 32) {
            throw new IllegalArgumentException("Key must be exactly 32 characters long");
        }
        return String.format("%s-%s-%s-%s-%s", key.substring(0, 8), key.substring(8, 12), key.substring(12, 16), key.substring(16, 20), key.substring(20));
    }

    public static byte[] hexToByte(String hexString) {
        Objects.requireNonNull(hexString, "Hex string cannot be null");
        Object cleanHex = hexString.startsWith("0x") ? hexString.substring(2) : hexString;
        cleanHex = ((String)cleanHex).replace("-", "");
        if (((String)cleanHex).isEmpty()) {
            return new byte[0];
        }
        if (((String)cleanHex).length() % 2 != 0) {
            cleanHex = "0" + (String)cleanHex;
        }
        byte[] result = new byte[((String)cleanHex).length() / 2];
        for (int i = 0; i < result.length; ++i) {
            int index = i * 2;
            try {
                result[i] = (byte)Integer.parseInt(((String)cleanHex).substring(index, index + 2), 16);
                continue;
            }
            catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid hex character at position " + index + " in: " + hexString, e);
            }
        }
        return result;
    }

    public static boolean validateKey(String hexKey) {
        if (hexKey == null) {
            return false;
        }
        String cleanHexKey = hexKey.replace("-", "");
        if (cleanHexKey.length() != 32) {
            return false;
        }
        try {
            Hmac.hexToByte(cleanHexKey);
            return true;
        }
        catch (IllegalArgumentException e) {
            return false;
        }
    }

    public double scaleHash(String value, int scaledMin, int scaledMax) {
        byte[] hash = new byte[6];
        double max = 2.81474976710656E14;
        double scale = (double)scaledMax - (double)scaledMin;
        System.arraycopy(this.byteHash(value), 0, hash, 0, 6);
        double fraction = new BigInteger(1, hash).doubleValue() / 2.81474976710656E14;
        return (double)((int)(fraction * scale)) + (double)scaledMin;
    }

    public String uidHash(String inputUID) {
        if (!StringUtil.hasText((CharSequence)inputUID)) {
            return null;
        }
        byte[] hash = this.byteHash(inputUID);
        byte[] uuid = new byte[16];
        System.arraycopy(hash, 0, uuid, 0, Math.min(16, hash.length));
        uuid[6] = (byte)(uuid[6] & 0xF | 0x40);
        uuid[8] = (byte)(uuid[8] & 0x3F | 0x80);
        return "2.25." + String.valueOf(new BigInteger(1, uuid));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public byte[] byteHash(String value) {
        Objects.requireNonNull(value, "Value cannot be null");
        Mac mac = this.mac;
        synchronized (mac) {
            return this.mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        }
    }

    public String hexHash(String value) {
        return Hmac.byteToHex(this.byteHash(value));
    }
}

