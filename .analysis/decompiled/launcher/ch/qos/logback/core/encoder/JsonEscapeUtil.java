/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.encoder;

public class JsonEscapeUtil {
    protected static final char[] HEXADECIMALS_TABLE = "0123456789ABCDEF".toCharArray();
    static final int ESCAPE_CODES_COUNT = 32;
    static final String[] ESCAPE_CODES = new String[32];

    private static String _computeEscapeCodeBelowASCII32(char c) {
        if (c > ' ') {
            throw new IllegalArgumentException("input must be less than 32");
        }
        StringBuilder sb = new StringBuilder(6);
        sb.append("\\u00");
        int highPart = c >> 4;
        sb.append(HEXADECIMALS_TABLE[highPart]);
        int lowPart = c & 0xF;
        sb.append(HEXADECIMALS_TABLE[lowPart]);
        return sb.toString();
    }

    static String getObligatoryEscapeCode(char c) {
        if (c < ' ') {
            return ESCAPE_CODES[c];
        }
        if (c == '\"') {
            return "\\\"";
        }
        if (c == '/') {
            return "\\/";
        }
        if (c == '\\') {
            return "\\\\";
        }
        return null;
    }

    public static String jsonEscapeString(String input) {
        int length = input.length();
        int lenthWithLeeway = (int)((double)length * 1.1);
        StringBuilder sb = new StringBuilder(lenthWithLeeway);
        for (int i = 0; i < length; ++i) {
            char c = input.charAt(i);
            String escaped = JsonEscapeUtil.getObligatoryEscapeCode(c);
            if (escaped == null) {
                sb.append(c);
                continue;
            }
            sb.append(escaped);
        }
        return sb.toString();
    }

    static {
        block7: for (char c = '\u0000'; c < ' '; c = (char)(c + '\u0001')) {
            switch (c) {
                case '\b': {
                    JsonEscapeUtil.ESCAPE_CODES[c] = "\\b";
                    continue block7;
                }
                case '\t': {
                    JsonEscapeUtil.ESCAPE_CODES[c] = "\\t";
                    continue block7;
                }
                case '\n': {
                    JsonEscapeUtil.ESCAPE_CODES[c] = "\\n";
                    continue block7;
                }
                case '\f': {
                    JsonEscapeUtil.ESCAPE_CODES[c] = "\\f";
                    continue block7;
                }
                case '\r': {
                    JsonEscapeUtil.ESCAPE_CODES[c] = "\\r";
                    continue block7;
                }
                default: {
                    JsonEscapeUtil.ESCAPE_CODES[c] = JsonEscapeUtil._computeEscapeCodeBelowASCII32(c);
                }
            }
        }
    }
}

