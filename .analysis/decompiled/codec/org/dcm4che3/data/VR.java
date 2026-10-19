/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.dcm4che3.data;

import java.time.temporal.Temporal;
import java.util.Date;
import java.util.TimeZone;
import org.dcm4che3.data.BinaryValueType;
import org.dcm4che3.data.DatePrecision;
import org.dcm4che3.data.DatePrecisions;
import org.dcm4che3.data.SequenceValueType;
import org.dcm4che3.data.SpecificCharacterSet;
import org.dcm4che3.data.StringValueType;
import org.dcm4che3.data.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum VR {
    AE(16709, 8, 32, StringValueType.ASCII, false),
    AS(16723, 8, 32, StringValueType.ASCII, false),
    AT(16724, 8, 0, BinaryValueType.TAG, false),
    CS(17235, 8, 32, StringValueType.ASCII, false),
    DA(17473, 8, 32, StringValueType.DA, false),
    DS(17491, 8, 32, StringValueType.DS, false),
    DT(17492, 8, 32, StringValueType.DT, false),
    FD(17988, 8, 0, BinaryValueType.DOUBLE, false),
    FL(17996, 8, 0, BinaryValueType.FLOAT, false),
    IS(18771, 8, 32, StringValueType.IS, false),
    LO(19535, 8, 32, StringValueType.STRING, false),
    LT(19540, 8, 32, StringValueType.TEXT, false),
    OB(20290, 12, 0, BinaryValueType.BYTE, true),
    OD(20292, 12, 0, BinaryValueType.DOUBLE, true),
    OF(20294, 12, 0, BinaryValueType.FLOAT, true),
    OL(20300, 12, 0, BinaryValueType.INT, true),
    OV(20310, 12, 0, BinaryValueType.LONG, true),
    OW(20311, 12, 0, BinaryValueType.SHORT, true),
    PN(20558, 8, 32, StringValueType.PN, false),
    SH(21320, 8, 32, StringValueType.STRING, false),
    SL(21324, 8, 0, BinaryValueType.INT, false),
    SQ(21329, 12, 0, SequenceValueType.SQ, false),
    SS(21331, 8, 0, BinaryValueType.SHORT, false),
    ST(21332, 8, 32, StringValueType.TEXT, false),
    SV(21334, 12, 0, BinaryValueType.LONG, false),
    TM(21581, 8, 32, StringValueType.TM, false),
    UC(21827, 12, 32, StringValueType.STRING, false),
    UI(21833, 8, 0, StringValueType.ASCII, false),
    UL(21836, 8, 0, BinaryValueType.UINT, false),
    UN(21838, 12, 0, BinaryValueType.BYTE, true),
    UR(21842, 12, 32, StringValueType.UR, false),
    US(21843, 8, 0, BinaryValueType.USHORT, false),
    UT(21844, 12, 32, StringValueType.TEXT, false),
    UV(21846, 12, 0, BinaryValueType.ULONG, false);

    private static Logger LOG;
    protected final int code;
    protected final int headerLength;
    protected final int paddingByte;
    protected final ValueType valueType;
    protected final boolean inlineBinary;
    private static final VR[] VALUE_OF;

    private VR(int code, int headerLength, int paddingByte, ValueType valueType, boolean inlineBinary) {
        this.code = code;
        this.headerLength = headerLength;
        this.paddingByte = paddingByte;
        this.valueType = valueType;
        this.inlineBinary = inlineBinary;
    }

    private static int indexOf(int code) {
        return (code & 0x1F00) >> 3 | code & 0x1F;
    }

    public static VR valueOf(int code) {
        return ((code ^ 0x4040) & 0xFFFFE0E0) == 0 ? VALUE_OF[VR.indexOf(code)] : null;
    }

    public int code() {
        return this.code;
    }

    public int headerLength() {
        return this.headerLength;
    }

    public int paddingByte() {
        return this.paddingByte;
    }

    public boolean isTemporalType() {
        return this.valueType.isTemporalType();
    }

    public boolean isStringType() {
        return this.valueType.isStringValue();
    }

    public boolean useSpecificCharacterSet() {
        return this.valueType.useSpecificCharacterSet();
    }

    public boolean isIntType() {
        return this.valueType.isIntValue();
    }

    public boolean isInlineBinary() {
        return this.inlineBinary;
    }

    public int numEndianBytes() {
        return this.valueType.numEndianBytes();
    }

    public byte[] toggleEndian(byte[] b, boolean preserve) {
        return this.valueType.toggleEndian(b, preserve);
    }

    public byte[] toBytes(Object val, SpecificCharacterSet cs) {
        return this.valueType.toBytes(val, cs);
    }

    public Object toStrings(Object val, boolean bigEndian, SpecificCharacterSet cs) {
        return this.valueType.toStrings(val, bigEndian, cs);
    }

    public String toString(Object val, boolean bigEndian, int valueIndex, String defVal) {
        return this.valueType.toString(val, bigEndian, valueIndex, defVal);
    }

    public int toInt(Object val, boolean bigEndian, int valueIndex, int defVal) {
        return this.valueType.toInt(val, bigEndian, valueIndex, defVal);
    }

    public int[] toInts(Object val, boolean bigEndian) {
        return this.valueType.toInts(val, bigEndian);
    }

    public long toLong(Object val, boolean bigEndian, int valueIndex, long defVal) {
        return this.valueType.toLong(val, bigEndian, valueIndex, defVal);
    }

    public long[] toLongs(Object val, boolean bigEndian) {
        return this.valueType.toLongs(val, bigEndian);
    }

    public float toFloat(Object val, boolean bigEndian, int valueIndex, float defVal) {
        return this.valueType.toFloat(val, bigEndian, valueIndex, defVal);
    }

    public float[] toFloats(Object val, boolean bigEndian) {
        return this.valueType.toFloats(val, bigEndian);
    }

    public double toDouble(Object val, boolean bigEndian, int valueIndex, double defVal) {
        return this.valueType.toDouble(val, bigEndian, valueIndex, defVal);
    }

    public double[] toDoubles(Object val, boolean bigEndian) {
        return this.valueType.toDoubles(val, bigEndian);
    }

    public Temporal toTemporal(Object val, int valueIndex, DatePrecision precision) {
        return this.valueType.toTemporal(val, valueIndex, precision);
    }

    public Date toDate(Object val, TimeZone tz, int valueIndex, boolean ceil, Date defVal, DatePrecision precision) {
        return this.valueType.toDate(val, tz, valueIndex, ceil, defVal, precision);
    }

    public Date[] toDates(Object val, TimeZone tz, boolean ceil, DatePrecisions precisions) {
        return this.valueType.toDate(val, tz, ceil, precisions);
    }

    Object toValue(byte[] b) {
        return this.valueType.toValue(b);
    }

    Object toValue(String s, boolean bigEndian) {
        return this.valueType.toValue(s, bigEndian);
    }

    Object toValue(String[] ss, boolean bigEndian) {
        return this.valueType.toValue(ss, bigEndian);
    }

    Object toValue(int[] is, boolean bigEndian) {
        return this.valueType.toValue(is, bigEndian);
    }

    Object toValue(long[] ls, boolean bigEndian) {
        return this.valueType.toValue(ls, bigEndian);
    }

    Object toValue(float[] fs, boolean bigEndian) {
        return this.valueType.toValue(fs, bigEndian);
    }

    Object toValue(double[] ds, boolean bigEndian) {
        return this.valueType.toValue(ds, bigEndian);
    }

    public Object toValue(Date[] ds, TimeZone tz, DatePrecision precision) {
        return this.valueType.toValue(ds, tz, precision);
    }

    public boolean prompt(Object val, boolean bigEndian, SpecificCharacterSet cs, int maxChars, StringBuilder sb) {
        return this.valueType.prompt(val, bigEndian, cs, maxChars, sb);
    }

    public int vmOf(Object val) {
        return this.valueType.vmOf(val);
    }

    static {
        LOG = LoggerFactory.getLogger(VR.class);
        VALUE_OF = new VR[1024];
        VR[] vRArray = VR.values();
        int n = vRArray.length;
        for (int i = 0; i < n; ++i) {
            VR vr;
            VR.VALUE_OF[VR.indexOf((int)vr.code)] = vr = vRArray[i];
        }
    }

    public static class Holder {
        public VR vr;
    }
}

