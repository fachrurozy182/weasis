/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.imageio.codec.jpeg;

public class JPEG {
    public static final int TEM = 1;
    private static final int JPEG2000_STANDALONE = 48;
    public static final int FF_SOC = 65359;
    public static final int SOC = 79;
    public static final int SIZ = 81;
    public static final int COD = 82;
    public static final int TLM = 85;
    public static final int SOT = 144;
    public static final int SOD = 147;
    public static final int SOF0 = 192;
    public static final int SOF1 = 193;
    public static final int SOF2 = 194;
    public static final int SOF3 = 195;
    public static final int DHT = 196;
    public static final int SOF5 = 197;
    public static final int SOF6 = 198;
    public static final int SOF7 = 199;
    public static final int JPG = 200;
    public static final int SOF9 = 201;
    public static final int SOF10 = 202;
    public static final int SOF11 = 203;
    public static final int DAC = 204;
    public static final int SOF13 = 205;
    public static final int SOF14 = 206;
    public static final int SOF15 = 207;
    public static final int RST0 = 208;
    public static final int RST1 = 209;
    public static final int RST2 = 210;
    public static final int RST3 = 211;
    public static final int RST4 = 212;
    public static final int RST5 = 213;
    public static final int RST6 = 214;
    public static final int RST7 = 215;
    public static final int RESTART_RANGE = 8;
    public static final int FF_SOI = 65496;
    public static final int SOI = 216;
    public static final int EOI = 217;
    public static final int SOS = 218;
    public static final int DQT = 219;
    public static final int DNL = 220;
    public static final int DRI = 221;
    public static final int DHP = 222;
    public static final int EXP = 223;
    public static final int APP0 = 224;
    public static final int APP1 = 225;
    public static final int APP2 = 226;
    public static final int APP3 = 227;
    public static final int APP4 = 228;
    public static final int APP5 = 229;
    public static final int APP6 = 230;
    public static final int APP7 = 231;
    public static final int APP8 = 232;
    public static final int APP9 = 233;
    public static final int APP10 = 234;
    public static final int APP11 = 235;
    public static final int APP12 = 236;
    public static final int APP13 = 237;
    public static final int APP14 = 238;
    public static final int APP15 = 239;
    public static final int SOF55 = 247;
    public static final int LSE = 248;
    public static final int COM = 254;

    private JPEG() {
    }

    public static boolean isStandalone(int marker) {
        return switch (marker) {
            case 1, 208, 209, 210, 211, 212, 213, 214, 215, 216, 217 -> true;
            default -> (marker & 0xF0) == 48;
        };
    }

    public static boolean isSOF(int marker) {
        return switch (marker) {
            case 192, 193, 194, 195, 197, 198, 199, 201, 202, 203, 205, 206, 207, 247 -> true;
            default -> false;
        };
    }

    public static boolean isAPP(int marker) {
        return (marker & 0xF0) == 224;
    }
}

