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
import org.dcm4che3.data.ValueType;

enum SequenceValueType implements ValueType
{
    SQ;


    @Override
    public boolean isStringValue() {
        return false;
    }

    @Override
    public boolean useSpecificCharacterSet() {
        return false;
    }

    @Override
    public boolean isIntValue() {
        return false;
    }

    @Override
    public boolean isTemporalType() {
        return false;
    }

    @Override
    public int numEndianBytes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] toggleEndian(byte[] b, boolean preserve) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] toBytes(Object val, SpecificCharacterSet cs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString(Object val, boolean bigEndian, int valueIndex, String defVal) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object toStrings(Object val, boolean bigEndian, SpecificCharacterSet cs) {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public Object toValue(String s, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object toValue(String[] ss, boolean bigEndian) {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean prompt(Object val, boolean bigEndian, SpecificCharacterSet cs, int maxChars, StringBuilder sb) {
        sb.append(val);
        return true;
    }

    @Override
    public int vmOf(Object val) {
        return 1;
    }
}

