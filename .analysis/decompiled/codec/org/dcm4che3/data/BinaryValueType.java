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
import org.dcm4che3.data.StringValueType;
import org.dcm4che3.data.Value;
import org.dcm4che3.data.ValueType;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.TagUtils;

enum BinaryValueType implements ValueType
{
    BYTE(1, 1){

        @Override
        public byte[] toggleEndian(byte[] b, boolean preserve) {
            return b;
        }

        @Override
        protected int toInt(byte[] b, int off, boolean bigEndian) {
            return b[off];
        }

        @Override
        protected byte[] toBytes(int i, byte[] b, int off, boolean bigEndian) {
            b[off] = (byte)i;
            return b;
        }

        @Override
        protected byte[] toBytes(long l, byte[] b, int off, boolean bigEndian) {
            return this.toBytes((int)l, b, off, bigEndian);
        }
    }
    ,
    SHORT(2, 2){

        @Override
        public boolean isIntValue() {
            return true;
        }

        @Override
        public byte[] toggleEndian(byte[] b, boolean preserve) {
            return ByteUtils.swapShorts(preserve ? (byte[])b.clone() : b, 0, b.length);
        }

        @Override
        protected int toInt(byte[] b, int off, boolean bigEndian) {
            return ByteUtils.bytesToShort(b, off, bigEndian);
        }

        @Override
        protected long toLong(byte[] b, int off, boolean bigEndian) {
            return this.toInt(b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(int i, byte[] b, int off, boolean bigEndian) {
            return ByteUtils.shortToBytes(i, b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(long l, byte[] b, int off, boolean bigEndian) {
            return this.toBytes((int)l, b, off, bigEndian);
        }
    }
    ,
    USHORT(2, 2){

        @Override
        public boolean isIntValue() {
            return true;
        }

        @Override
        public byte[] toggleEndian(byte[] b, boolean preserve) {
            return ByteUtils.swapShorts(preserve ? (byte[])b.clone() : b, 0, b.length);
        }

        @Override
        protected int toInt(byte[] b, int off, boolean bigEndian) {
            return ByteUtils.bytesToUShort(b, off, bigEndian);
        }

        @Override
        protected long toLong(byte[] b, int off, boolean bigEndian) {
            return this.toInt(b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(int i, byte[] b, int off, boolean bigEndian) {
            return ByteUtils.shortToBytes(i, b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(long l, byte[] b, int off, boolean bigEndian) {
            return this.toBytes((int)l, b, off, bigEndian);
        }
    }
    ,
    INT(4, 4){

        @Override
        public boolean isIntValue() {
            return true;
        }

        @Override
        public byte[] toggleEndian(byte[] b, boolean preserve) {
            return ByteUtils.swapInts(preserve ? (byte[])b.clone() : b, 0, b.length);
        }

        @Override
        protected int toInt(byte[] b, int off, boolean bigEndian) {
            return ByteUtils.bytesToInt(b, off, bigEndian);
        }

        @Override
        protected long toLong(byte[] b, int off, boolean bigEndian) {
            return this.toInt(b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(int i, byte[] b, int off, boolean bigEndian) {
            return ByteUtils.intToBytes(i, b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(long l, byte[] b, int off, boolean bigEndian) {
            return this.toBytes((int)l, b, off, bigEndian);
        }
    }
    ,
    UINT(4, 4){

        @Override
        public boolean isIntValue() {
            return true;
        }

        @Override
        public byte[] toggleEndian(byte[] b, boolean preserve) {
            return ByteUtils.swapInts(preserve ? (byte[])b.clone() : b, 0, b.length);
        }

        @Override
        protected int toInt(byte[] b, int off, boolean bigEndian) {
            return ByteUtils.bytesToInt(b, off, bigEndian);
        }

        @Override
        protected long toLong(byte[] b, int off, boolean bigEndian) {
            return this.toInt(b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(int i, byte[] b, int off, boolean bigEndian) {
            return ByteUtils.intToBytes(i, b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(long l, byte[] b, int off, boolean bigEndian) {
            return this.toBytes((int)l, b, off, bigEndian);
        }

        @Override
        protected String toString(byte[] b, int off, boolean bigEndian) {
            return Integer.toUnsignedString(this.toInt(b, off, bigEndian));
        }
    }
    ,
    TAG(4, 2){

        @Override
        public byte[] toggleEndian(byte[] b, boolean preserve) {
            return ByteUtils.swapShorts(preserve ? (byte[])b.clone() : b, 0, b.length);
        }

        @Override
        protected String toString(byte[] b, int off, boolean bigEndian) {
            return TagUtils.toHexString(this.toInt(b, off, bigEndian));
        }

        @Override
        protected int toInt(byte[] b, int off, boolean bigEndian) {
            return ByteUtils.bytesToTag(b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(String s, byte[] b, int off, boolean bigEndian) {
            return this.toBytes(Integer.parseInt(s, 16), b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(int i, byte[] b, int off, boolean bigEndian) {
            return ByteUtils.tagToBytes(i, b, off, bigEndian);
        }
    }
    ,
    LONG(8, 8){

        @Override
        public boolean isIntValue() {
            return true;
        }

        @Override
        public byte[] toggleEndian(byte[] b, boolean preserve) {
            return ByteUtils.swapLongs(preserve ? (byte[])b.clone() : b, 0, b.length);
        }

        @Override
        protected int toInt(byte[] b, int off, boolean bigEndian) {
            return (int)this.toLong(b, off, bigEndian);
        }

        @Override
        protected long toLong(byte[] b, int off, boolean bigEndian) {
            return ByteUtils.bytesToLong(b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(int i, byte[] b, int off, boolean bigEndian) {
            return this.toBytes((long)i, b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(long l, byte[] b, int off, boolean bigEndian) {
            return ByteUtils.longToBytes(l, b, off, bigEndian);
        }
    }
    ,
    ULONG(8, 8){

        @Override
        public boolean isIntValue() {
            return true;
        }

        @Override
        public byte[] toggleEndian(byte[] b, boolean preserve) {
            return ByteUtils.swapLongs(preserve ? (byte[])b.clone() : b, 0, b.length);
        }

        @Override
        protected int toInt(byte[] b, int off, boolean bigEndian) {
            return (int)this.toLong(b, off, bigEndian);
        }

        @Override
        protected long toLong(byte[] b, int off, boolean bigEndian) {
            return ByteUtils.bytesToLong(b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(String s, byte[] b, int off, boolean bigEndian) {
            return this.toBytes(StringUtils.parseUV(s), b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(int i, byte[] b, int off, boolean bigEndian) {
            return this.toBytes((long)i, b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(long l, byte[] b, int off, boolean bigEndian) {
            return ByteUtils.longToBytes(l, b, off, bigEndian);
        }

        @Override
        protected String toString(byte[] b, int off, boolean bigEndian) {
            return Long.toUnsignedString(this.toLong(b, off, bigEndian));
        }
    }
    ,
    FLOAT(4, 4){

        @Override
        public byte[] toggleEndian(byte[] b, boolean preserve) {
            return ByteUtils.swapInts(preserve ? (byte[])b.clone() : b, 0, b.length);
        }

        @Override
        protected String toString(byte[] b, int off, boolean bigEndian) {
            return StringUtils.formatDS(ByteUtils.bytesToFloat(b, off, bigEndian));
        }

        @Override
        protected float toFloat(byte[] b, int off, boolean bigEndian) {
            return ByteUtils.bytesToFloat(b, off, bigEndian);
        }

        @Override
        protected double toDouble(byte[] b, int off, boolean bigEndian) {
            return ByteUtils.bytesToFloat(b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(String s, byte[] b, int off, boolean bigEndian) {
            return this.toBytes(Float.parseFloat(s), b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(float f, byte[] b, int off, boolean bigEndian) {
            return ByteUtils.floatToBytes(f, b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(double d, byte[] b, int off, boolean bigEndian) {
            return ByteUtils.floatToBytes((float)d, b, off, bigEndian);
        }
    }
    ,
    DOUBLE(8, 8){

        @Override
        public byte[] toggleEndian(byte[] b, boolean preserve) {
            return ByteUtils.swapLongs(preserve ? (byte[])b.clone() : b, 0, b.length);
        }

        @Override
        protected String toString(byte[] b, int off, boolean bigEndian) {
            return StringUtils.formatDS(ByteUtils.bytesToDouble(b, off, bigEndian));
        }

        @Override
        protected float toFloat(byte[] b, int off, boolean bigEndian) {
            return (float)ByteUtils.bytesToDouble(b, off, bigEndian);
        }

        @Override
        protected double toDouble(byte[] b, int off, boolean bigEndian) {
            return ByteUtils.bytesToDouble(b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(String s, byte[] b, int off, boolean bigEndian) {
            return this.toBytes(Double.parseDouble(s), b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(float f, byte[] b, int off, boolean bigEndian) {
            return ByteUtils.doubleToBytes(f, b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(double d, byte[] b, int off, boolean bigEndian) {
            return ByteUtils.doubleToBytes(d, b, off, bigEndian);
        }
    };

    final int numBytes;
    final int numEndianBytes;

    private BinaryValueType(int numBytes, int numEndianBytes) {
        this.numBytes = numBytes;
        this.numEndianBytes = numEndianBytes;
    }

    @Override
    public boolean isIntValue() {
        return false;
    }

    @Override
    public boolean isStringValue() {
        return false;
    }

    @Override
    public boolean useSpecificCharacterSet() {
        return false;
    }

    @Override
    public boolean isTemporalType() {
        return false;
    }

    @Override
    public int numEndianBytes() {
        return this.numEndianBytes;
    }

    protected String toString(byte[] b, int off, boolean bigEndian) {
        return Long.toString(this.toLong(b, off, bigEndian));
    }

    protected int toInt(byte[] b, int off, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    protected long toLong(byte[] b, int off, boolean bigEndian) {
        return this.toInt(b, off, bigEndian);
    }

    protected float toFloat(byte[] b, int off, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    protected double toDouble(byte[] b, int off, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    protected byte[] toBytes(String s, byte[] b, int off, boolean bigEndian) {
        return this.toBytes(StringUtils.parseIS(s), b, off, bigEndian);
    }

    protected byte[] toBytes(int i, byte[] b, int off, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    protected byte[] toBytes(long l, byte[] b, int off, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    protected byte[] toBytes(float f, byte[] b, int off, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    protected byte[] toBytes(double d, byte[] b, int off, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] toBytes(Object val, SpecificCharacterSet cs) {
        if (val instanceof byte[]) {
            return (byte[])val;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString(Object val, boolean bigEndian, int valueIndex, String defVal) {
        if (!(val instanceof byte[])) {
            throw new UnsupportedOperationException();
        }
        int off = valueIndex * this.numBytes;
        byte[] b = (byte[])val;
        int len = b.length;
        return off + this.numBytes <= len ? this.toString(b, off, bigEndian) : defVal;
    }

    private void checkLength(int len) {
        if (len % this.numBytes != 0) {
            throw new IllegalArgumentException("length: " + len);
        }
    }

    @Override
    public Object toStrings(Object val, boolean bigEndian, SpecificCharacterSet cs) {
        if (!(val instanceof byte[])) {
            throw new UnsupportedOperationException();
        }
        byte[] b = (byte[])val;
        int len = b.length;
        this.checkLength(len);
        if (len == this.numBytes) {
            return this.toString(b, 0, bigEndian);
        }
        String[] ss = new String[len / this.numBytes];
        int i = 0;
        int off = 0;
        while (i < ss.length) {
            ss[i] = this.toString(b, off, bigEndian);
            ++i;
            off += this.numBytes;
        }
        return ss;
    }

    @Override
    public int toInt(Object val, boolean bigEndian, int valueIndex, int defVal) {
        if (!(val instanceof byte[])) {
            throw new UnsupportedOperationException();
        }
        int off = valueIndex * this.numBytes;
        byte[] b = (byte[])val;
        int len = b.length;
        return off + this.numBytes <= len ? this.toInt(b, off, bigEndian) : defVal;
    }

    @Override
    public int[] toInts(Object val, boolean bigEndian) {
        if (!(val instanceof byte[])) {
            throw new UnsupportedOperationException();
        }
        byte[] b = (byte[])val;
        int len = b.length;
        this.checkLength(len);
        int[] is = new int[len / this.numBytes];
        int i = 0;
        int off = 0;
        while (i < is.length) {
            is[i] = this.toInt(b, off, bigEndian);
            ++i;
            off += this.numBytes;
        }
        return is;
    }

    @Override
    public long toLong(Object val, boolean bigEndian, int valueIndex, long defVal) {
        if (!(val instanceof byte[])) {
            throw new UnsupportedOperationException();
        }
        int off = valueIndex * this.numBytes;
        byte[] b = (byte[])val;
        int len = b.length;
        return off + this.numBytes <= len ? this.toLong(b, off, bigEndian) : defVal;
    }

    @Override
    public long[] toLongs(Object val, boolean bigEndian) {
        if (!(val instanceof byte[])) {
            throw new UnsupportedOperationException();
        }
        byte[] b = (byte[])val;
        int len = b.length;
        this.checkLength(len);
        long[] ls = new long[len / this.numBytes];
        int i = 0;
        int off = 0;
        while (i < ls.length) {
            ls[i] = this.toLong(b, off, bigEndian);
            ++i;
            off += this.numBytes;
        }
        return ls;
    }

    @Override
    public float toFloat(Object val, boolean bigEndian, int valueIndex, float defVal) {
        if (!(val instanceof byte[])) {
            throw new UnsupportedOperationException();
        }
        int off = valueIndex * this.numBytes;
        byte[] b = (byte[])val;
        int len = b.length;
        return off + this.numBytes <= len ? this.toFloat(b, off, bigEndian) : defVal;
    }

    @Override
    public float[] toFloats(Object val, boolean bigEndian) {
        if (!(val instanceof byte[])) {
            throw new UnsupportedOperationException();
        }
        byte[] b = (byte[])val;
        int len = b.length;
        this.checkLength(len);
        float[] fs = new float[len / this.numBytes];
        int i = 0;
        int off = 0;
        while (i < fs.length) {
            fs[i] = this.toFloat(b, off, bigEndian);
            ++i;
            off += this.numBytes;
        }
        return fs;
    }

    @Override
    public double toDouble(Object val, boolean bigEndian, int valueIndex, double defVal) {
        if (!(val instanceof byte[])) {
            throw new UnsupportedOperationException();
        }
        int off = valueIndex * this.numBytes;
        byte[] b = (byte[])val;
        int len = b.length;
        return off + this.numBytes <= len ? this.toDouble(b, off, bigEndian) : defVal;
    }

    @Override
    public double[] toDoubles(Object val, boolean bigEndian) {
        if (!(val instanceof byte[])) {
            throw new UnsupportedOperationException();
        }
        byte[] b = (byte[])val;
        int len = b.length;
        this.checkLength(len);
        double[] ds = new double[len / this.numBytes];
        int i = 0;
        int off = 0;
        while (i < ds.length) {
            ds[i] = this.toDouble(b, off, bigEndian);
            ++i;
            off += this.numBytes;
        }
        return ds;
    }

    @Override
    public Temporal toTemporal(Object val, int valueIndex, DatePrecision precision) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date toDate(Object val, TimeZone tz, int valueIndex, boolean ceil, Date defVal, DatePrecision precision) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date[] toDate(Object val, TimeZone tz, boolean ceil, DatePrecisions precisions) {
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
        return this.toBytes(s, new byte[this.numBytes], 0, bigEndian);
    }

    @Override
    public Object toValue(String[] ss, boolean bigEndian) {
        if (ss == null || ss.length == 0) {
            return Value.NULL;
        }
        if (ss.length == 1) {
            return this.toValue(ss[0], bigEndian);
        }
        byte[] b = new byte[ss.length * this.numBytes];
        int i = 0;
        int off = 0;
        while (i < ss.length) {
            this.toBytes(ss[i], b, off, bigEndian);
            ++i;
            off += this.numBytes;
        }
        return b;
    }

    @Override
    public Object toValue(int[] is, boolean bigEndian) {
        if (is == null || is.length == 0) {
            return Value.NULL;
        }
        byte[] b = new byte[is.length * this.numBytes];
        int i = 0;
        int off = 0;
        while (i < is.length) {
            this.toBytes(is[i], b, off, bigEndian);
            ++i;
            off += this.numBytes;
        }
        return b;
    }

    @Override
    public Object toValue(long[] ls, boolean bigEndian) {
        if (ls == null || ls.length == 0) {
            return Value.NULL;
        }
        byte[] b = new byte[ls.length * this.numBytes];
        int i = 0;
        int off = 0;
        while (i < ls.length) {
            this.toBytes(ls[i], b, off, bigEndian);
            ++i;
            off += this.numBytes;
        }
        return b;
    }

    @Override
    public Object toValue(float[] fs, boolean bigEndian) {
        if (fs == null || fs.length == 0) {
            return Value.NULL;
        }
        byte[] b = new byte[fs.length * this.numBytes];
        int i = 0;
        int off = 0;
        while (i < fs.length) {
            this.toBytes(fs[i], b, off, bigEndian);
            ++i;
            off += this.numBytes;
        }
        return b;
    }

    @Override
    public Object toValue(double[] ds, boolean bigEndian) {
        if (ds == null || ds.length == 0) {
            return Value.NULL;
        }
        byte[] b = new byte[ds.length * this.numBytes];
        int i = 0;
        int off = 0;
        while (i < ds.length) {
            this.toBytes(ds[i], b, off, bigEndian);
            ++i;
            off += this.numBytes;
        }
        return b;
    }

    @Override
    public Object toValue(Date[] ds, TimeZone tz, DatePrecision precision) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean prompt(Object val, boolean bigEndian, SpecificCharacterSet cs, int maxChars, StringBuilder sb) {
        if (val instanceof byte[]) {
            return this.prompt((byte[])val, bigEndian, maxChars, sb);
        }
        return StringValueType.prompt(val.toString(), maxChars, sb);
    }

    private boolean prompt(byte[] b, boolean bigEndian, int maxChars, StringBuilder sb) {
        int maxLength = sb.length() + maxChars;
        int i = b.length / this.numBytes;
        int off = 0;
        while (i-- > 0) {
            sb.append(this.toString(b, off, bigEndian));
            if (sb.length() > maxLength) {
                sb.setLength(maxLength + 1);
                return false;
            }
            if (i > 0) {
                sb.append('\\');
            }
            off += this.numBytes;
        }
        return true;
    }

    @Override
    public int vmOf(Object val) {
        if (val instanceof byte[]) {
            return ((byte[])val).length / this.numBytes;
        }
        throw new UnsupportedOperationException();
    }
}

