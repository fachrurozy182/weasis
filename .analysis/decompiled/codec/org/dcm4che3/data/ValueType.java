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

interface ValueType {
    public boolean isStringValue();

    public boolean useSpecificCharacterSet();

    public boolean isIntValue();

    public boolean isTemporalType();

    public int numEndianBytes();

    public byte[] toggleEndian(byte[] var1, boolean var2);

    public byte[] toBytes(Object var1, SpecificCharacterSet var2);

    public String toString(Object var1, boolean var2, int var3, String var4);

    public Object toStrings(Object var1, boolean var2, SpecificCharacterSet var3);

    public int toInt(Object var1, boolean var2, int var3, int var4);

    public int[] toInts(Object var1, boolean var2);

    public long toLong(Object var1, boolean var2, int var3, long var4);

    public long[] toLongs(Object var1, boolean var2);

    public float toFloat(Object var1, boolean var2, int var3, float var4);

    public float[] toFloats(Object var1, boolean var2);

    public double toDouble(Object var1, boolean var2, int var3, double var4);

    public double[] toDoubles(Object var1, boolean var2);

    public Temporal toTemporal(Object var1, int var2, DatePrecision var3);

    public Date toDate(Object var1, TimeZone var2, int var3, boolean var4, Date var5, DatePrecision var6);

    public Date[] toDate(Object var1, TimeZone var2, boolean var3, DatePrecisions var4);

    public Object toValue(byte[] var1);

    public Object toValue(String var1, boolean var2);

    public Object toValue(String[] var1, boolean var2);

    public Object toValue(int[] var1, boolean var2);

    public Object toValue(long[] var1, boolean var2);

    public Object toValue(float[] var1, boolean var2);

    public Object toValue(double[] var1, boolean var2);

    public Object toValue(Date[] var1, TimeZone var2, DatePrecision var3);

    public boolean prompt(Object var1, boolean var2, SpecificCharacterSet var3, int var4, StringBuilder var5);

    public int vmOf(Object var1);
}

