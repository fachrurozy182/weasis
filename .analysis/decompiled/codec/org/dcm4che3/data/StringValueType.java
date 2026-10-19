/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.data;

import java.time.temporal.Temporal;
import java.util.Date;
import java.util.TimeZone;
import org.dcm4che3.data.DatePrecision;
import org.dcm4che3.data.DatePrecisions;
import org.dcm4che3.data.SpecificCharacterSet;
import org.dcm4che3.data.TemporalType;
import org.dcm4che3.data.Value;
import org.dcm4che3.data.ValueType;
import org.dcm4che3.util.ArrayUtils;
import org.dcm4che3.util.StringUtils;

enum StringValueType implements ValueType
{
    ASCII("\\", null),
    STRING("\\", null){

        @Override
        public boolean useSpecificCharacterSet() {
            return true;
        }

        @Override
        protected SpecificCharacterSet cs(SpecificCharacterSet cs) {
            return cs;
        }
    }
    ,
    TEXT("\t\n\f\r", null){

        @Override
        public boolean useSpecificCharacterSet() {
            return true;
        }

        @Override
        protected SpecificCharacterSet cs(SpecificCharacterSet cs) {
            return cs;
        }

        @Override
        protected Object splitAndTrim(String s, SpecificCharacterSet cs) {
            return cs.toText(StringUtils.trimTrailing(s));
        }

        @Override
        protected Object toMultiValue(String s) {
            return s;
        }
    }
    ,
    UR(null, null){

        @Override
        protected Object splitAndTrim(String s, SpecificCharacterSet cs) {
            return StringUtils.trimTrailing(s);
        }

        @Override
        protected Object toMultiValue(String s) {
            return s;
        }
    }
    ,
    DA("\\", TemporalType.DA),
    DT("\\", TemporalType.DT),
    TM("\\", TemporalType.TM),
    PN("^=\\", null){

        @Override
        public boolean useSpecificCharacterSet() {
            return true;
        }

        @Override
        protected SpecificCharacterSet cs(SpecificCharacterSet cs) {
            return cs;
        }
    }
    ,
    DS("\\", null){

        @Override
        public byte[] toBytes(Object val, SpecificCharacterSet cs) {
            if (val instanceof double[]) {
                val = this.toStrings((double[])val);
            }
            return super.toBytes(val, cs);
        }

        @Override
        public String toString(Object val, boolean bigEndian, int valueIndex, String defVal) {
            if (val instanceof double[]) {
                double[] ds = (double[])val;
                return valueIndex < ds.length && !Double.isNaN(ds[valueIndex]) ? StringUtils.formatDS(ds[valueIndex]) : defVal;
            }
            return super.toString(val, bigEndian, valueIndex, defVal);
        }

        @Override
        public Object toStrings(Object val, boolean bigEndian, SpecificCharacterSet cs) {
            return val instanceof double[] ? this.toStrings((double[])val) : super.toStrings(val, bigEndian, cs);
        }

        private Object toStrings(double[] ds) {
            if (ds.length == 1) {
                return StringUtils.formatDS(ds[0]);
            }
            String[] ss = new String[ds.length];
            for (int i = 0; i < ds.length; ++i) {
                ss[i] = !Double.isNaN(ds[i]) ? StringUtils.formatDS(ds[i]) : "";
            }
            return ss;
        }

        @Override
        public float toFloat(Object val, boolean bigEndian, int valueIndex, float defVal) {
            double[] ds = (double[])val;
            return valueIndex < ds.length && !Double.isNaN(ds[valueIndex]) ? (float)ds[valueIndex] : defVal;
        }

        @Override
        public float[] toFloats(Object val, boolean bigEndian) {
            double[] ds = (double[])val;
            float[] fs = new float[ds.length];
            for (int i = 0; i < fs.length; ++i) {
                fs[i] = (float)ds[i];
            }
            return fs;
        }

        @Override
        public double toDouble(Object val, boolean bigEndian, int valueIndex, double defVal) {
            double[] ds = (double[])val;
            return valueIndex < ds.length && !Double.isNaN(ds[valueIndex]) ? ds[valueIndex] : defVal;
        }

        @Override
        public double[] toDoubles(Object val, boolean bigEndian) {
            return (double[])val;
        }

        @Override
        public Object toValue(float[] fs, boolean bigEndian) {
            if (fs == null || fs.length == 0) {
                return Value.NULL;
            }
            if (fs.length == 1) {
                return StringUtils.formatDS(fs[0]);
            }
            String[] ss = new String[fs.length];
            for (int i = 0; i < fs.length; ++i) {
                ss[i] = StringUtils.formatDS(fs[i]);
            }
            return ss;
        }

        @Override
        public Object toValue(double[] ds, boolean bigEndian) {
            if (ds == null || ds.length == 0) {
                return Value.NULL;
            }
            return ds;
        }

        @Override
        public boolean prompt(Object val, boolean bigEndian, SpecificCharacterSet cs, int maxChars, StringBuilder sb) {
            if (val instanceof double[]) {
                val = this.toStrings((double[])val);
            }
            return super.prompt(val, bigEndian, cs, maxChars, sb);
        }
    }
    ,
    IS("\\", null){

        @Override
        public boolean isIntValue() {
            return true;
        }

        @Override
        public byte[] toBytes(Object val, SpecificCharacterSet cs) {
            if (val instanceof long[]) {
                val = this.toStrings((long[])val);
            }
            return super.toBytes(val, cs);
        }

        @Override
        public String toString(Object val, boolean bigEndian, int valueIndex, String defVal) {
            if (val instanceof long[]) {
                long[] ls = (long[])val;
                return valueIndex < ls.length && ls[valueIndex] != Integer.MIN_VALUE ? Long.toString(ls[valueIndex]) : defVal;
            }
            return super.toString(val, bigEndian, valueIndex, defVal);
        }

        @Override
        public Object toStrings(Object val, boolean bigEndian, SpecificCharacterSet cs) {
            return val instanceof long[] ? this.toStrings((long[])val) : super.toStrings(val, bigEndian, cs);
        }

        private Object toStrings(long[] ls) {
            if (ls.length == 1) {
                return Long.toString(ls[0]);
            }
            String[] ss = new String[ls.length];
            for (int i = 0; i < ls.length; ++i) {
                ss[i] = ls[i] != Integer.MIN_VALUE ? Long.toString(ls[i]) : "";
            }
            return ss;
        }

        @Override
        public int toInt(Object val, boolean bigEndian, int valueIndex, int defVal) {
            return (int)this.toLong(val, bigEndian, valueIndex, defVal);
        }

        @Override
        public int[] toInts(Object val, boolean bigEndian) {
            return ArrayUtils.longsToInts((long[])val);
        }

        @Override
        public Object toValue(int[] is, boolean bigEndian) {
            if (is == null || is.length == 0) {
                return Value.NULL;
            }
            return ArrayUtils.intsToLong(is);
        }

        @Override
        public long toLong(Object val, boolean bigEndian, int valueIndex, long defVal) {
            long[] is = (long[])val;
            return valueIndex < is.length && is[valueIndex] != Integer.MIN_VALUE ? is[valueIndex] : defVal;
        }

        @Override
        public long[] toLongs(Object val, boolean bigEndian) {
            return (long[])val;
        }

        @Override
        public Object toValue(long[] ls, boolean bigEndian) {
            if (ls == null || ls.length == 0) {
                return Value.NULL;
            }
            return ls;
        }

        @Override
        public boolean prompt(Object val, boolean bigEndian, SpecificCharacterSet cs, int maxChars, StringBuilder sb) {
            if (val instanceof long[]) {
                val = this.toStrings((long[])val);
            }
            return super.prompt(val, bigEndian, cs, maxChars, sb);
        }
    };

    final String delimiters;
    final TemporalType temporalType;

    private StringValueType(String delimiters, TemporalType temperalType) {
        this.delimiters = delimiters;
        this.temporalType = temperalType;
    }

    @Override
    public boolean isStringValue() {
        return true;
    }

    @Override
    public boolean isIntValue() {
        return false;
    }

    @Override
    public boolean isTemporalType() {
        return this.temporalType != null;
    }

    @Override
    public int numEndianBytes() {
        return 1;
    }

    @Override
    public byte[] toggleEndian(byte[] b, boolean preserve) {
        return b;
    }

    @Override
    public boolean useSpecificCharacterSet() {
        return false;
    }

    protected SpecificCharacterSet cs(SpecificCharacterSet cs) {
        return SpecificCharacterSet.ASCII;
    }

    @Override
    public byte[] toBytes(Object val, SpecificCharacterSet cs) {
        if (val instanceof byte[]) {
            return (byte[])val;
        }
        if (val instanceof String) {
            return this.cs(cs).encode((String)val, this.delimiters);
        }
        if (val instanceof String[]) {
            return this.cs(cs).encode(StringUtils.concat((String[])val, '\\'), this.delimiters);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString(Object val, boolean bigEndian, int valueIndex, String defVal) {
        if (val instanceof String) {
            return (String)(valueIndex == 0 ? val : defVal);
        }
        if (val instanceof String[]) {
            String[] ss = (String[])val;
            return valueIndex < ss.length && ss[valueIndex] != null && !ss[valueIndex].isEmpty() ? ss[valueIndex] : defVal;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Object toStrings(Object val, boolean bigEndian, SpecificCharacterSet cs) {
        if (val instanceof byte[]) {
            return this.splitAndTrim(this.cs(cs).decode((byte[])val, this.delimiters), cs);
        }
        if (val instanceof String || val instanceof String[]) {
            return val;
        }
        throw new UnsupportedOperationException();
    }

    protected Object splitAndTrim(String s, SpecificCharacterSet cs) {
        return StringUtils.splitAndTrim(s, '\\');
    }

    @Override
    public int toInt(Object val, boolean bigEndian, int valueIndex, int defVal) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int[] toInts(Object val, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long toLong(Object val, boolean bigEndian, int valueIndex, long defVal) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long[] toLongs(Object val, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float toFloat(Object val, boolean bigEndian, int valueIndex, float defVal) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float[] toFloats(Object val, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double toDouble(Object val, boolean bigEndian, int valueIndex, double defVal) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double[] toDoubles(Object val, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Temporal toTemporal(Object val, int valueIndex, DatePrecision precision) {
        if (this.temporalType == null) {
            throw new UnsupportedOperationException();
        }
        if (val instanceof String) {
            return valueIndex == 0 ? this.temporalType.parseTemporal((String)val, precision) : null;
        }
        if (val instanceof String[]) {
            String[] ss = (String[])val;
            return valueIndex < ss.length && ss[valueIndex] != null ? this.temporalType.parseTemporal(ss[valueIndex], precision) : null;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Date toDate(Object val, TimeZone tz, int valueIndex, boolean ceil, Date defVal, DatePrecision precision) {
        if (this.temporalType == null) {
            throw new UnsupportedOperationException();
        }
        if (val instanceof String) {
            return valueIndex == 0 ? this.temporalType.parse(tz, (String)val, ceil, precision) : defVal;
        }
        if (val instanceof String[]) {
            String[] ss = (String[])val;
            return valueIndex < ss.length && ss[valueIndex] != null ? this.temporalType.parse(tz, ss[valueIndex], ceil, precision) : defVal;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Date[] toDate(Object val, TimeZone tz, boolean ceil, DatePrecisions precisions) {
        if (this.temporalType == null) {
            throw new UnsupportedOperationException();
        }
        if (val instanceof String) {
            precisions.precisions = new DatePrecision[1];
            Date[] dateArray = new Date[1];
            precisions.precisions[0] = new DatePrecision();
            dateArray[0] = this.temporalType.parse(tz, (String)val, ceil, precisions.precisions[0]);
            return dateArray;
        }
        if (val instanceof String[]) {
            String[] ss = (String[])val;
            Date[] is = new Date[ss.length];
            precisions.precisions = new DatePrecision[ss.length];
            for (int i = 0; i < is.length; ++i) {
                if (ss[i] == null) continue;
                precisions.precisions[i] = new DatePrecision();
                is[i] = this.temporalType.parse(tz, ss[i], ceil, precisions.precisions[i]);
            }
            return is;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Object toValue(byte[] b) {
        return b != null && b.length > 0 ? (Object)b : Value.NULL;
    }

    @Override
    public Object toValue(String s, boolean bigEndian) {
        if (s == null || s.isEmpty()) {
            return Value.NULL;
        }
        return this.toMultiValue(s);
    }

    protected Object toMultiValue(String s) {
        return StringUtils.splitAndTrim(s, '\\');
    }

    @Override
    public Object toValue(String[] ss, boolean bigEndian) {
        if (ss == null || ss.length == 0) {
            return Value.NULL;
        }
        if (ss.length == 1) {
            return this.toValue(ss[0], bigEndian);
        }
        return ss;
    }

    @Override
    public Object toValue(int[] is, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object toValue(long[] ls, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object toValue(float[] fs, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object toValue(double[] ds, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object toValue(Date[] ds, TimeZone tz, DatePrecision precision) {
        if (this.temporalType == null) {
            throw new UnsupportedOperationException();
        }
        if (ds == null || ds.length == 0) {
            return Value.NULL;
        }
        if (ds.length == 1) {
            return this.temporalType.format(tz, ds[0], precision);
        }
        String[] ss = new String[ds.length];
        for (int i = 0; i < ss.length; ++i) {
            ss[i] = this.temporalType.format(tz, ds[i], precision);
        }
        return ss;
    }

    @Override
    public boolean prompt(Object val, boolean bigEndian, SpecificCharacterSet cs, int maxChars, StringBuilder sb) {
        if (val instanceof byte[]) {
            return StringValueType.prompt(this.cs(cs).decode((byte[])val, this.delimiters), maxChars, sb);
        }
        if (val instanceof String) {
            return StringValueType.prompt((String)val, maxChars, sb);
        }
        if (val instanceof String[]) {
            return StringValueType.prompt((String[])val, maxChars, sb);
        }
        return StringValueType.prompt(val.toString(), maxChars, sb);
    }

    static boolean prompt(String s, int maxChars, StringBuilder sb) {
        int maxLength = sb.length() + maxChars;
        sb.append(s.trim());
        if (sb.length() > maxLength) {
            sb.setLength(maxLength + 1);
            return false;
        }
        return true;
    }

    static boolean prompt(String[] ss, int maxChars, StringBuilder sb) {
        int maxLength = sb.length() + maxChars;
        for (String s : ss) {
            if (s != null) {
                sb.append(s);
            }
            if (sb.length() > maxLength) {
                sb.setLength(maxLength + 1);
                return false;
            }
            sb.append('\\');
        }
        sb.setLength(sb.length() - 1);
        return true;
    }

    @Override
    public int vmOf(Object val) {
        if (val instanceof String) {
            return 1;
        }
        if (val instanceof String[]) {
            String[] ss = (String[])val;
            return ss.length;
        }
        throw new UnsupportedOperationException();
    }
}

