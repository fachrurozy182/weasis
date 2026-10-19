/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.FieldPosition;
import java.text.Format;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.time.Duration;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.UnaryOperator;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.TagUtils;
import org.dcm4che3.util.UIDUtils;

public class AttributesFormat
extends Format {
    private static final long serialVersionUID = 1901510733531643054L;
    private static final char[] CHARS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v'};
    private static final int LONG_BYTES = 8;
    private final String pattern;
    private final int[][] tagPaths;
    private final int[] index;
    private final int[] offsets;
    private final Object[] dateTimeOffsets;
    private final UnaryOperator<String>[] slices;
    private final Type[] types;
    private final MessageFormat format;

    public AttributesFormat(String pattern) {
        ArrayList<String> tokens = this.tokenize(pattern);
        int n = tokens.size() / 2;
        this.pattern = pattern;
        this.tagPaths = new int[n][];
        this.index = new int[n];
        this.types = new Type[n];
        this.offsets = new int[n];
        this.dateTimeOffsets = new Object[n];
        this.slices = new UnaryOperator[n];
        this.format = this.buildMessageFormat(tokens);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private ArrayList<String> tokenize(String s) {
        ArrayList<String> result = new ArrayList<String>();
        StringTokenizer stk = new StringTokenizer(s, "{}", true);
        int prevDelim = 125;
        int level = 0;
        StringBuilder sb = new StringBuilder();
        while (stk.hasMoreTokens()) {
            String tk = stk.nextToken();
            char delim = tk.charAt(0);
            if (delim == '{') {
                if (level++ == 0) {
                    if (prevDelim == 125) {
                        result.add("");
                    }
                } else {
                    sb.append(delim);
                }
            } else if (delim == '}') {
                if (--level == 0) {
                    result.add(sb.toString());
                    sb.setLength(0);
                } else {
                    if (level <= 0) throw new IllegalArgumentException(s);
                    sb.append(delim);
                }
            } else if (level == 0) {
                result.add(tk);
            } else {
                sb.append(tk);
            }
            prevDelim = delim;
        }
        return result;
    }

    private MessageFormat buildMessageFormat(ArrayList<String> tokens) {
        StringBuilder formatBuilder = new StringBuilder(this.pattern.length());
        int j = 0;
        for (int i = 0; i < this.tagPaths.length; ++i) {
            formatBuilder.append(tokens.get(j++)).append('{').append(i);
            String tagStr = tokens.get(j++);
            int typeStart = tagStr.indexOf(44) + 1;
            boolean rnd = tagStr.startsWith("rnd");
            if (!rnd && !tagStr.startsWith("now")) {
                int tagStrLen = typeStart != 0 ? typeStart - 1 : tagStr.length();
                int indexStart = tagStr.charAt(tagStrLen - 1) == ']' ? tagStr.lastIndexOf(91, tagStrLen - 3) + 1 : 0;
                try {
                    this.tagPaths[i] = TagUtils.parseTagPath(tagStr.substring(0, indexStart != 0 ? indexStart - 1 : tagStrLen));
                    if (indexStart != 0) {
                        this.index[i] = Integer.parseInt(tagStr.substring(indexStart, tagStrLen - 1));
                    }
                }
                catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(this.pattern);
                }
            }
            if (typeStart != 0) {
                int typeEnd = tagStr.indexOf(44, typeStart);
                if (typeEnd < 0) {
                    typeEnd = tagStr.length();
                }
                try {
                    if (tagStr.startsWith("date", typeStart)) {
                        this.types[i] = Type.date;
                        if (typeStart + 4 < typeEnd) {
                            this.dateTimeOffsets[i] = Period.parse(tagStr.substring(typeStart + 4, typeEnd));
                        }
                    } else if (tagStr.startsWith("time", typeStart)) {
                        this.types[i] = Type.time;
                        if (typeStart + 4 < typeEnd) {
                            this.dateTimeOffsets[i] = Duration.parse(tagStr.substring(typeStart + 4, typeEnd));
                        }
                    } else {
                        this.types[i] = Type.valueOf(tagStr.substring(typeStart, typeEnd));
                    }
                }
                catch (IllegalArgumentException | DateTimeParseException e) {
                    throw new IllegalArgumentException(this.pattern);
                }
                switch (this.types[i].ordinal()) {
                    case 3: 
                    case 5: 
                    case 6: 
                    case 7: {
                        formatBuilder.append(',').append((Object)this.types[i]).append(tagStr.substring(typeEnd));
                        break;
                    }
                    case 4: {
                        try {
                            this.offsets[i] = Integer.parseInt(tagStr.substring(typeEnd + 1));
                        }
                        catch (IllegalArgumentException e) {
                            throw new IllegalArgumentException(this.pattern);
                        }
                    }
                    case 2: {
                        try {
                            this.slices[i] = new Slice(tagStr.substring(typeEnd + 1));
                            break;
                        }
                        catch (RuntimeException e) {
                            throw new IllegalArgumentException(this.pattern);
                        }
                    }
                }
            } else {
                this.types[i] = Type.none;
            }
            if (rnd) {
                switch (this.types[i].ordinal()) {
                    case 0: {
                        this.types[i] = Type.rnd;
                    }
                    case 12: 
                    case 13: {
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException(this.pattern);
                    }
                }
            }
            formatBuilder.append('}');
        }
        if (j < tokens.size()) {
            formatBuilder.append(tokens.get(j));
        }
        try {
            return new MessageFormat(formatBuilder.toString());
        }
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(this.pattern);
        }
    }

    public static AttributesFormat valueOf(String s) {
        return s != null ? new AttributesFormat(s) : null;
    }

    @Override
    public StringBuffer format(Object obj, StringBuffer result, FieldPosition pos) {
        return this.format.format(this.toArgs((Attributes)obj), result, pos);
    }

    private Object[] toArgs(Attributes attrs) {
        Object[] args = new Object[this.tagPaths.length];
        block0: for (int i = 0; i < args.length; ++i) {
            Attributes item = attrs;
            int tag = 0;
            int[] tagPath = this.tagPaths[i];
            if (tagPath != null) {
                int last = tagPath.length - 1;
                tag = tagPath[last];
                for (int j = 0; j < last; ++j) {
                    if ((item = item.getNestedDataset(tagPath[j])) == null) continue block0;
                }
            }
            args[i] = this.types[i].toArg(item, tag, this.index[i], this.offsets[i], this.dateTimeOffsets[i], this.slices[i]);
        }
        return args;
    }

    @Override
    public Object parseObject(String source, ParsePosition pos) {
        throw new UnsupportedOperationException();
    }

    public String toString() {
        return this.pattern;
    }

    private static enum Type {
        none{

            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset, Object dateTimeOffset, UnaryOperator<String> splice) {
                return attrs.getString(tag, index);
            }
        }
        ,
        upper{

            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset, Object dateTimeOffset, UnaryOperator<String> splice) {
                String s = attrs.getString(tag, index);
                return s != null ? s.toUpperCase() : null;
            }
        }
        ,
        slice{

            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset, Object dateTimeOffset, UnaryOperator<String> slice) {
                String s = attrs.getString(tag, index);
                return s != null ? slice.apply(s) : null;
            }
        }
        ,
        number{

            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset, Object dateTimeOffset, UnaryOperator<String> splice) {
                return attrs.getDouble(tag, index, 0.0);
            }
        }
        ,
        offset{

            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset, Object dateTimeOffset, UnaryOperator<String> splice) {
                return Integer.toString(attrs.getInt(tag, index, 0) + offset);
            }
        }
        ,
        date{

            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset, Object dateTimeOffset, UnaryOperator<String> splice) {
                Date date;
                Date date2 = date = tag != 0 ? attrs.getDate(tag, index) : new Date();
                if (date == null || !(dateTimeOffset instanceof Period)) {
                    return date;
                }
                Period dateOffset = (Period)dateTimeOffset;
                Calendar cal = Calendar.getInstance(attrs.getTimeZone());
                cal.setTime(date);
                cal.add(1, dateOffset.getYears());
                cal.add(2, dateOffset.getMonths());
                cal.add(5, dateOffset.getDays());
                return cal.getTime();
            }
        }
        ,
        time{

            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset, Object dateTimeOffset, UnaryOperator<String> splice) {
                Date date;
                Date date2 = date = tag != 0 ? attrs.getDate(tag, index) : new Date();
                if (date == null || !(dateTimeOffset instanceof Duration)) {
                    return date;
                }
                Duration timeOffset = (Duration)dateTimeOffset;
                Calendar cal = Calendar.getInstance(attrs.getTimeZone());
                cal.setTime(date);
                cal.add(13, (int)timeOffset.getSeconds());
                return cal.getTime();
            }
        }
        ,
        choice{

            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset, Object dateTimeOffset, UnaryOperator<String> splice) {
                return attrs.getDouble(tag, index, 0.0);
            }
        }
        ,
        hash{

            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset, Object dateTimeOffset, UnaryOperator<String> splice) {
                String s = attrs.getString(tag, index);
                return s != null ? TagUtils.toHexString(s.hashCode()) : null;
            }
        }
        ,
        md5{

            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset, Object dateTimeOffset, UnaryOperator<String> splice) {
                String s = attrs.getString(tag, index);
                return s != null ? this.getMD5String(s) : null;
            }
        }
        ,
        urlencoded{

            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset, Object dateTimeOffset, UnaryOperator<String> splice) {
                String s = attrs.getString(tag, index);
                try {
                    return s != null ? URLEncoder.encode(s, "UTF-8") : null;
                }
                catch (UnsupportedEncodingException e) {
                    throw new AssertionError((Object)e);
                }
            }
        }
        ,
        rnd{

            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset, Object dateTimeOffset, UnaryOperator<String> splice) {
                return TagUtils.toHexString(ThreadLocalRandom.current().nextInt());
            }
        }
        ,
        uuid{

            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset, Object dateTimeOffset, UnaryOperator<String> splice) {
                return UUID.randomUUID();
            }
        }
        ,
        uid{

            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset, Object dateTimeOffset, UnaryOperator<String> splice) {
                return UIDUtils.createUID();
            }
        };


        abstract Object toArg(Attributes var1, int var2, int var3, int var4, Object var5, UnaryOperator<String> var6);

        String getMD5String(String s) {
            try {
                MessageDigest digest = MessageDigest.getInstance("MD5");
                digest.update(s == null ? new byte[]{} : s.getBytes(StandardCharsets.UTF_8));
                return Type.toString32(digest.digest());
            }
            catch (NoSuchAlgorithmException e) {
                return s;
            }
        }

        static String toString32(byte[] ba) {
            int i;
            long l1 = Type.toLong(ba, 0);
            long l2 = Type.toLong(ba, 8);
            char[] ca = new char[26];
            for (i = 0; i < 12; ++i) {
                ca[i] = CHARS[(int)l1 & 0x1F];
                l1 >>>= 5;
            }
            ca[12] = CHARS[(int)(l1 |= (l2 & 1L) << 4) & 0x1F];
            l2 >>>= 1;
            for (i = 13; i < 26; ++i) {
                ca[i] = CHARS[(int)l2 & 0x1F];
                l2 >>>= 5;
            }
            return new String(ca);
        }

        static long toLong(byte[] ba, int offset) {
            long l = 0L;
            int len = offset + 8;
            for (int i = offset; i < len; ++i) {
                l |= (long)(ba[i] & 0xFF);
                l <<= 8;
            }
            return l;
        }
    }

    private class Slice
    implements UnaryOperator<String> {
        final int beginIndex;
        final int endIndex;

        public Slice(String s) {
            String[] ss = StringUtils.split(s, ',');
            if (ss.length == 1) {
                this.beginIndex = Integer.parseInt(ss[0]);
                this.endIndex = 0;
            } else if (ss.length == 2) {
                this.endIndex = Integer.parseInt(ss[1]);
                this.beginIndex = this.endIndex != 0 ? Integer.parseInt(ss[0]) : 0;
            } else {
                throw new IllegalArgumentException(s);
            }
        }

        @Override
        public String apply(String s) {
            try {
                int l = s.length();
                return this.endIndex == 0 ? s.substring(this.beginIndex < 0 ? Math.max(0, l + this.beginIndex) : this.beginIndex) : s.substring(this.beginIndex < 0 ? Math.max(0, l + this.beginIndex) : this.beginIndex, this.endIndex < 0 ? l + this.endIndex : Math.min(l, this.endIndex));
            }
            catch (RuntimeException e) {
                return null;
            }
        }
    }
}

