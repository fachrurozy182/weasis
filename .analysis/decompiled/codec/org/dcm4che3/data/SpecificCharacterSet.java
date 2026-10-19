/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.dcm4che3.data;

import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.StringTokenizer;
import org.dcm4che3.util.SafeBuffer;
import org.dcm4che3.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpecificCharacterSet {
    private static final Logger LOG = LoggerFactory.getLogger(SpecificCharacterSet.class);
    public static final SpecificCharacterSet ASCII;
    private static SpecificCharacterSet DEFAULT;
    private static final ThreadLocal<SoftReference<Encoder>> cachedEncoder1;
    private static final ThreadLocal<SoftReference<Encoder>> cachedEncoder2;
    protected final Codec[] codecs;
    protected final String[] dicomCodes;

    public static SpecificCharacterSet getDefaultCharacterSet() {
        return DEFAULT;
    }

    public static void setDefaultCharacterSet(String code) {
        SpecificCharacterSet cs;
        SpecificCharacterSet specificCharacterSet = cs = code != null ? SpecificCharacterSet.valueOf(code) : ASCII;
        if (!cs.containsASCII()) {
            throw new IllegalArgumentException("Default Character Set must contain ASCII - " + code);
        }
        DEFAULT = cs;
    }

    public static void setCharsetNameMapping(String code, String charsetName) {
        Codec.forCode(code, false).setCharsetName(SpecificCharacterSet.checkCharsetName(charsetName));
    }

    public static void resetCharsetNameMappings() {
        Codec.resetCharsetNames();
    }

    public static String checkSpecificCharacterSet(String code) {
        Codec.forCode(code, false);
        return code;
    }

    public static String checkCharsetName(String charsetName) {
        if (!Charset.isSupported(charsetName)) {
            throw new UnsupportedCharsetException(charsetName);
        }
        return charsetName;
    }

    public static SpecificCharacterSet valueOf(String ... codes) {
        if (codes == null || codes.length == 0) {
            return DEFAULT;
        }
        boolean iso2022 = codes.length > 1;
        Codec defCodec = SpecificCharacterSet.DEFAULT.codecs[0];
        if (iso2022) {
            codes = SpecificCharacterSet.checkISO2022(codes);
            if (defCodec == Codec.UTF_8) {
                defCodec = Codec.ISO_646;
            }
        }
        Codec[] infos = new Codec[codes.length];
        for (int i = 0; i < codes.length; ++i) {
            infos[i] = Codec.forCode(codes[i], true, defCodec);
        }
        return iso2022 ? new ISO2022(infos, codes) : new SpecificCharacterSet(infos, codes);
    }

    public static boolean trimISO2022(String[] codes) {
        if (codes != null && codes.length == 1 && codes[0].startsWith("ISO 2022")) {
            switch (codes[0]) {
                case "ISO 2022 IR 6": {
                    codes[0] = "";
                    return true;
                }
                case "ISO 2022 IR 100": {
                    codes[0] = "ISO_IR 100";
                    return true;
                }
                case "ISO 2022 IR 101": {
                    codes[0] = "ISO_IR 101";
                    return true;
                }
                case "ISO 2022 IR 109": {
                    codes[0] = "ISO_IR 109";
                    return true;
                }
                case "ISO 2022 IR 110": {
                    codes[0] = "ISO_IR 110";
                    return true;
                }
                case "ISO 2022 IR 144": {
                    codes[0] = "ISO_IR 144";
                    return true;
                }
                case "ISO 2022 IR 127": {
                    codes[0] = "ISO_IR 127";
                    return true;
                }
                case "ISO 2022 IR 126": {
                    codes[0] = "ISO_IR 126";
                    return true;
                }
                case "ISO 2022 IR 138": {
                    codes[0] = "ISO_IR 138";
                    return true;
                }
                case "ISO 2022 IR 148": {
                    codes[0] = "ISO_IR 148";
                    return true;
                }
                case "ISO 2022 IR 203": {
                    codes[0] = "ISO_IR 203";
                    return true;
                }
                case "ISO 2022 IR 13": {
                    codes[0] = "ISO_IR 13";
                    return true;
                }
                case "ISO 2022 IR 166": {
                    codes[0] = "ISO_IR 166";
                    return true;
                }
            }
        }
        return false;
    }

    private static String[] checkISO2022(String[] codes) {
        String[] results = codes;
        block17: for (int i = 0; i < codes.length; ++i) {
            String code = codes[i];
            if (code == null || code.isEmpty() || code.startsWith("ISO 2022")) continue;
            switch (code) {
                case "ISO_IR 100": 
                case "ISO_IR 101": 
                case "ISO_IR 109": 
                case "ISO_IR 110": 
                case "ISO_IR 144": 
                case "ISO_IR 127": 
                case "ISO_IR 126": 
                case "ISO_IR 138": 
                case "ISO_IR 148": 
                case "ISO_IR 203": 
                case "ISO_IR 13": 
                case "ISO_IR 166": {
                    if (results == codes) {
                        results = (String[])codes.clone();
                    }
                    results[i] = "ISO 2022 " + code.substring(4);
                    continue block17;
                }
                default: {
                    LOG.info("Invalid Specific Character Set: [{}] - treat as [{}]", (Object)StringUtils.concat(codes, '\\'), (Object)StringUtils.maskNull(codes[0], ""));
                    return new String[]{codes[0]};
                }
            }
        }
        if (codes != results) {
            LOG.info("Invalid Specific Character Set: [{}] - treat as [{}]", (Object)StringUtils.concat(codes, '\\'), (Object)StringUtils.concat(results, '\\'));
        }
        return SpecificCharacterSet.ensureFirstContainsASCII(results);
    }

    private static String[] ensureFirstContainsASCII(String[] codes) {
        for (int i = 0; i < codes.length; ++i) {
            if (!Codec.forCode(codes[i]).containsASCII()) continue;
            if (i == 0) {
                return codes;
            }
            String[] clone = (String[])codes.clone();
            clone[0] = codes[i];
            clone[i] = codes[0];
            LOG.info("Invalid Specific Character Set: [{}] - treat as [{}]", (Object)StringUtils.concat(codes, '\\'), (Object)StringUtils.concat(clone, '\\'));
            return clone;
        }
        String[] withASCII = new String[1 + codes.length];
        withASCII[0] = "";
        System.arraycopy(codes, 0, withASCII, 1, codes.length);
        LOG.info("Invalid Specific Character Set: [{}] - treat as [{}]", (Object)StringUtils.concat(codes, '\\'), (Object)StringUtils.concat(withASCII, '\\'));
        return withASCII;
    }

    public String[] toCodes() {
        return this.dicomCodes;
    }

    private static Encoder encoder(ThreadLocal<SoftReference<Encoder>> tl, Codec codec) {
        Encoder enc;
        SoftReference<Encoder> sr = tl.get();
        if (sr == null || (enc = sr.get()) == null || enc.codec != codec) {
            enc = new Encoder(codec);
            tl.set(new SoftReference<Encoder>(enc));
        }
        return enc;
    }

    protected SpecificCharacterSet(Codec[] codecs, String ... codes) {
        this.codecs = codecs;
        this.dicomCodes = codes;
    }

    public byte[] encode(String val, String delimiters) {
        return this.codecs[0].encode(val);
    }

    public String decode(byte[] val, String delimiters) {
        return this.codecs[0].decode(val, 0, val.length);
    }

    public boolean isUTF8() {
        return this.codecs[0].equals((Object)Codec.UTF_8);
    }

    public boolean isASCII() {
        return this.codecs[0].equals((Object)Codec.ISO_646);
    }

    public boolean containsASCII() {
        return this.codecs[0].containsASCII();
    }

    public boolean contains(SpecificCharacterSet other) {
        return Arrays.equals((Object[])this.codecs, (Object[])other.codecs) || (other.isASCII() || other == ASCII) && this.containsASCII();
    }

    public String toText(String s) {
        return this.codecs[0].toText(s);
    }

    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this.getClass() != other.getClass()) {
            return false;
        }
        SpecificCharacterSet othercs = (SpecificCharacterSet)other;
        return Arrays.equals((Object[])this.codecs, (Object[])othercs.codecs);
    }

    public int hashCode() {
        return Arrays.hashCode((Object[])this.codecs);
    }

    static {
        DEFAULT = ASCII = new SpecificCharacterSet(new Codec[]{Codec.ISO_646}, new String[0]);
        cachedEncoder1 = new ThreadLocal();
        cachedEncoder2 = new ThreadLocal();
    }

    private static enum Codec {
        ISO_646(true, 10306, 0, 1),
        ISO_8859_1(true, 10306, 11585, 1),
        ISO_8859_2(true, 10306, 11586, 1),
        ISO_8859_3(true, 10306, 11587, 1),
        ISO_8859_4(true, 10306, 11588, 1),
        ISO_8859_5(true, 10306, 11596, 1),
        ISO_8859_6(true, 10306, 11591, 1),
        ISO_8859_7(true, 10306, 11590, 1),
        ISO_8859_8(true, 10306, 11592, 1),
        ISO_8859_9(true, 10306, 11597, 1),
        ISO_8859_15(true, 10306, 11618, 1),
        JIS_X_201(true, 10314, 10569, 1){

            @Override
            public String toText(String s) {
                return s.replace('\\', '\u00a5');
            }
        }
        ,
        TIS_620(true, 10306, 11604, 1),
        JIS_X_208(false, 9282, 0, 1),
        JIS_X_212(false, 0x242844, 0, 2),
        KS_X_1001(false, 0, 2369859, -1),
        GB2312(false, 0, 2369857, -1),
        UTF_8(true, 0, 0, -1),
        GB18030(false, 0, 0, -1);

        private static final String[] charsetNames;
        private final boolean containsASCII;
        private final int escSeq0;
        private final int escSeq1;
        private final int bytesPerChar;

        private Codec(boolean containsASCII, int escSeq0, int escSeq1, int bytesPerChar) {
            this.containsASCII = containsASCII;
            this.escSeq0 = escSeq0;
            this.escSeq1 = escSeq1;
            this.bytesPerChar = bytesPerChar;
        }

        private static void resetCharsetNames() {
            Codec.resetCharsetNames(charsetNames);
        }

        private static String[] resetCharsetNames(String[] charsetNames) {
            charsetNames[0] = "US-ASCII";
            charsetNames[1] = "ISO-8859-1";
            charsetNames[2] = "ISO-8859-2";
            charsetNames[3] = "ISO-8859-3";
            charsetNames[4] = "ISO-8859-4";
            charsetNames[5] = "ISO-8859-5";
            charsetNames[6] = "ISO-8859-6";
            charsetNames[7] = "ISO-8859-7";
            charsetNames[8] = "ISO-8859-8";
            charsetNames[9] = "ISO-8859-9";
            charsetNames[10] = "ISO-8859-15";
            charsetNames[11] = "JIS_X0201";
            charsetNames[12] = "TIS-620";
            charsetNames[13] = "x-JIS0208";
            charsetNames[14] = "JIS_X0212-1990";
            charsetNames[15] = "EUC-KR";
            charsetNames[16] = "GB2312";
            charsetNames[17] = "UTF-8";
            charsetNames[18] = "GB18030";
            return charsetNames;
        }

        public static Codec forCode(String code) {
            return Codec.forCode(code, true);
        }

        private static Codec forCode(String code, boolean lenient) {
            return Codec.forCode(code, lenient, DEFAULT.codecs[0]);
        }

        private static Codec forCode(String code, boolean lenient, Codec defCodec) {
            switch (code != null ? code : "") {
                case "": 
                case "ISO 2022 IR 6": {
                    return defCodec;
                }
                case "ISO_IR 100": 
                case "ISO 2022 IR 100": {
                    return ISO_8859_1;
                }
                case "ISO_IR 101": 
                case "ISO 2022 IR 101": {
                    return ISO_8859_2;
                }
                case "ISO_IR 109": 
                case "ISO 2022 IR 109": {
                    return ISO_8859_3;
                }
                case "ISO_IR 110": 
                case "ISO 2022 IR 110": {
                    return ISO_8859_4;
                }
                case "ISO_IR 144": 
                case "ISO 2022 IR 144": {
                    return ISO_8859_5;
                }
                case "ISO_IR 127": 
                case "ISO 2022 IR 127": {
                    return ISO_8859_6;
                }
                case "ISO_IR 126": 
                case "ISO 2022 IR 126": {
                    return ISO_8859_7;
                }
                case "ISO_IR 138": 
                case "ISO 2022 IR 138": {
                    return ISO_8859_8;
                }
                case "ISO_IR 148": 
                case "ISO 2022 IR 148": {
                    return ISO_8859_9;
                }
                case "ISO_IR 203": 
                case "ISO 2022 IR 203": {
                    return ISO_8859_15;
                }
                case "ISO_IR 13": 
                case "ISO 2022 IR 13": {
                    return JIS_X_201;
                }
                case "ISO_IR 166": 
                case "ISO 2022 IR 166": {
                    return TIS_620;
                }
                case "ISO 2022 IR 87": {
                    return JIS_X_208;
                }
                case "ISO 2022 IR 159": {
                    return JIS_X_212;
                }
                case "ISO 2022 IR 149": {
                    return KS_X_1001;
                }
                case "ISO 2022 IR 58": {
                    return GB2312;
                }
                case "ISO_IR 192": {
                    return UTF_8;
                }
                case "GB18030": 
                case "GBK": {
                    return GB18030;
                }
            }
            if (!lenient) {
                throw new IllegalArgumentException("No such Specific Character Set Code: " + code);
            }
            return defCodec;
        }

        public byte[] encode(String val) {
            try {
                return val.getBytes(this.charsetName());
            }
            catch (UnsupportedEncodingException e) {
                throw new AssertionError((Object)e);
            }
        }

        private String charsetName() {
            return charsetNames[this.ordinal()];
        }

        private void setCharsetName(String charsetName) {
            Codec.charsetNames[this.ordinal()] = charsetName;
        }

        public String decode(byte[] b, int off, int len) {
            try {
                return new String(b, off, len, this.charsetName());
            }
            catch (UnsupportedEncodingException e) {
                throw new AssertionError((Object)e);
            }
        }

        public boolean containsASCII() {
            return this.containsASCII;
        }

        public int getEscSeq0() {
            return this.escSeq0;
        }

        public int getEscSeq1() {
            return this.escSeq1;
        }

        public int getBytesPerChar() {
            return this.bytesPerChar;
        }

        public String toText(String s) {
            return s;
        }

        static {
            charsetNames = Codec.resetCharsetNames(new String[19]);
        }
    }

    private static final class Encoder {
        final Codec codec;
        final CharsetEncoder encoder;

        public Encoder(Codec codec) {
            this.codec = codec;
            this.encoder = Charset.forName(codec.charsetName()).newEncoder();
        }

        public boolean encode(CharBuffer cb, ByteBuffer bb, int escSeq, G0G1 useRange, CodingErrorAction errorAction) {
            this.encoder.onMalformedInput(errorAction).onUnmappableCharacter(errorAction).reset();
            int cbmark = cb.position();
            int bbmark = bb.position();
            try {
                Encoder.escSeq(bb, escSeq);
                int graphicCharStart = bb.position();
                CoderResult cr = this.encoder.encode(cb, bb, true);
                if (!cr.isUnderflow()) {
                    cr.throwException();
                }
                if (!(cr = this.encoder.flush(bb)).isUnderflow()) {
                    cr.throwException();
                }
                if (useRange == G0G1.G0) {
                    int end = bb.position();
                    for (int i = graphicCharStart; i < end; ++i) {
                        if (0 <= bb.get(i)) continue;
                        throw new CharacterCodingException();
                    }
                } else if (useRange == G0G1.G1) {
                    int end = bb.position();
                    for (int i = graphicCharStart; i < end; ++i) {
                        if (0 > bb.get(i)) continue;
                        throw new CharacterCodingException();
                    }
                }
            }
            catch (CharacterCodingException x) {
                SafeBuffer.position(cb, cbmark);
                SafeBuffer.position(bb, bbmark);
                return false;
            }
            return true;
        }

        private static void escSeq(ByteBuffer bb, int seq) {
            if (seq == 0) {
                return;
            }
            bb.put((byte)27);
            int b1 = seq >> 16;
            if (b1 != 0) {
                bb.put((byte)b1);
            }
            bb.put((byte)(seq >> 8));
            bb.put((byte)seq);
        }

        public byte[] replacement() {
            return this.encoder.replacement();
        }
    }

    private static final class ISO2022
    extends SpecificCharacterSet {
        private ISO2022(Codec[] charsetInfos, String ... codes) {
            super(charsetInfos, codes);
        }

        @Override
        public byte[] encode(String val, String delimiters) {
            byte[] buf;
            ByteBuffer bb;
            int strlen = val.length();
            CharBuffer cb = CharBuffer.wrap(val.toCharArray());
            Encoder enc1 = SpecificCharacterSet.encoder(cachedEncoder1, this.codecs[0]);
            if (!enc1.encode(cb, bb = ByteBuffer.wrap(buf = new byte[strlen]), 0, G0G1.Both, CodingErrorAction.REPORT)) {
                Encoder[] encs = new Encoder[this.codecs.length];
                encs[0] = enc1;
                encs[1] = SpecificCharacterSet.encoder(cachedEncoder2, this.codecs[1]);
                StringTokenizer comps = new StringTokenizer(val, delimiters, true);
                buf = new byte[6 * strlen];
                bb = ByteBuffer.wrap(buf);
                int[] cur = new int[]{0, 0};
                while (comps.hasMoreTokens()) {
                    String comp = comps.nextToken();
                    if (comp.length() == 1 && delimiters.indexOf(comp.charAt(0)) >= 0) {
                        this.activateInitialCharacterSet(bb, cur);
                        bb.put((byte)comp.charAt(0));
                        continue;
                    }
                    cb = CharBuffer.wrap(comp.toCharArray());
                    this.encodeComponent(encs, cb, bb, cur);
                }
                this.activateInitialCharacterSet(bb, cur);
            }
            return Arrays.copyOf(buf, bb.position());
        }

        private void encodeComponent(Encoder[] encs, CharBuffer cb, ByteBuffer bb, int[] cur) {
            if (this.codecs[cur[1]].getEscSeq1() != 0 && encs[cur[1]].encode(cb, bb, 0, G0G1.G1, CodingErrorAction.REPORT)) {
                return;
            }
            if ((this.codecs[cur[1]].getEscSeq1() == 0 || this.codecs[cur[1]].getEscSeq0() != this.codecs[cur[0]].getEscSeq0()) && encs[cur[0]].encode(cb, bb, 0, G0G1.G0, CodingErrorAction.REPORT)) {
                return;
            }
            int next = encs.length;
            while (--next >= 0) {
                if (encs[next] == null) {
                    encs[next] = new Encoder(this.codecs[next]);
                }
                if (this.codecs[next].getEscSeq1() != 0 && encs[next].encode(cb, bb, this.codecs[next].getEscSeq1(), G0G1.G1, CodingErrorAction.REPORT)) {
                    cur[1] = next;
                    break;
                }
                if (this.codecs[next].getEscSeq0() == 0 || !encs[next].encode(cb, bb, this.codecs[next].getEscSeq0(), G0G1.G0, CodingErrorAction.REPORT)) continue;
                cur[0] = next;
                break;
            }
            if (next < 0) {
                if (cb.length() > 1) {
                    for (int i = 0; i < cb.length(); ++i) {
                        this.encodeComponent(encs, cb.subSequence(i, i + 1), bb, cur);
                    }
                } else {
                    bb.put(encs[cur[0]].replacement());
                }
            }
        }

        private void activateInitialCharacterSet(ByteBuffer bb, int[] cur) {
            if (cur[0] != 0) {
                Encoder.escSeq(bb, this.codecs[0].getEscSeq0());
                cur[0] = 0;
            }
            if (cur[1] != 0) {
                Encoder.escSeq(bb, this.codecs[0].getEscSeq1());
                cur[1] = 0;
            }
        }

        @Override
        public String decode(byte[] b, String delimiters) {
            Codec[] codec = new Codec[]{this.codecs[0], this.codecs[0]};
            int g = 0;
            int off = 0;
            int cur = 0;
            StringBuilder sb = new StringBuilder(b.length);
            while (cur < b.length) {
                int bytesPerChar;
                if (b[cur] == 27 && cur + 2 < b.length) {
                    if (off < cur) {
                        sb.append(codec[g].decode(b, off, cur - off));
                    }
                    int esc0 = cur++;
                    int esc1 = cur++;
                    int esc2 = cur++;
                    block0 : switch (((b[esc1] & 0xFF) << 8) + (b[esc2] & 0xFF)) {
                        case 9256: {
                            if (cur < b.length && b[cur++] == 68) {
                                codec[0] = Codec.JIS_X_212;
                                break;
                            }
                            sb.append(codec[0].decode(b, esc0, cur - esc0));
                            break;
                        }
                        case 9257: {
                            switch (cur < b.length ? b[cur++] : -1) {
                                case 65: {
                                    this.switchCodec(codec, 1, Codec.GB2312);
                                    break block0;
                                }
                                case 67: {
                                    this.switchCodec(codec, 1, Codec.KS_X_1001);
                                    break block0;
                                }
                            }
                            sb.append(codec[0].decode(b, esc0, cur - esc0));
                            break;
                        }
                        case 9282: {
                            codec[0] = Codec.JIS_X_208;
                            break;
                        }
                        case 10306: {
                            this.switchCodec(codec, 0, Codec.ISO_646);
                            break;
                        }
                        case 10314: {
                            codec[0] = Codec.JIS_X_201;
                            if (codec[1].getEscSeq1() != 0) break;
                            codec[1] = codec[0];
                            break;
                        }
                        case 10569: {
                            codec[1] = Codec.JIS_X_201;
                            break;
                        }
                        case 11585: {
                            this.switchCodec(codec, 1, Codec.ISO_8859_1);
                            break;
                        }
                        case 11586: {
                            this.switchCodec(codec, 1, Codec.ISO_8859_2);
                            break;
                        }
                        case 11587: {
                            this.switchCodec(codec, 1, Codec.ISO_8859_3);
                            break;
                        }
                        case 11588: {
                            this.switchCodec(codec, 1, Codec.ISO_8859_4);
                            break;
                        }
                        case 11590: {
                            this.switchCodec(codec, 1, Codec.ISO_8859_7);
                            break;
                        }
                        case 11591: {
                            this.switchCodec(codec, 1, Codec.ISO_8859_6);
                            break;
                        }
                        case 11592: {
                            this.switchCodec(codec, 1, Codec.ISO_8859_8);
                            break;
                        }
                        case 11596: {
                            this.switchCodec(codec, 1, Codec.ISO_8859_5);
                            break;
                        }
                        case 11597: {
                            this.switchCodec(codec, 1, Codec.ISO_8859_9);
                            break;
                        }
                        case 11618: {
                            this.switchCodec(codec, 1, Codec.ISO_8859_15);
                            break;
                        }
                        case 11604: {
                            this.switchCodec(codec, 1, Codec.TIS_620);
                            break;
                        }
                        default: {
                            sb.append(codec[0].decode(b, esc0, cur - esc0));
                        }
                    }
                    off = cur;
                    continue;
                }
                if (codec[0] != codec[1] && g == (b[cur] < 0 ? 0 : 1)) {
                    if (off < cur) {
                        sb.append(codec[g].decode(b, off, cur - off));
                    }
                    off = cur;
                    g = 1 - g;
                }
                if (g == 0 && codec[g].containsASCII & delimiters.indexOf(b[cur]) >= 0) {
                    codec[0] = codec[1] = this.codecs[0];
                }
                cur += (bytesPerChar = codec[g].getBytesPerChar()) > 0 ? bytesPerChar : (b[cur] < 0 ? 2 : 1);
            }
            if (off < cur) {
                sb.append(codec[g].decode(b, off, Math.min(cur, b.length) - off));
            }
            return sb.toString();
        }

        private void switchCodec(Codec[] codecs, int i, Codec codec) {
            codecs[i] = codec;
            if (codecs[0].getEscSeq0() == codecs[1].getEscSeq0()) {
                codecs[0] = codecs[1];
            }
        }
    }

    private static enum G0G1 {
        G0,
        G1,
        Both;

    }
}

