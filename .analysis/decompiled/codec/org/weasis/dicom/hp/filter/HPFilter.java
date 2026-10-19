/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.MathUtil
 */
package org.weasis.dicom.hp.filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import org.weasis.core.util.MathUtil;
import org.weasis.dicom.macro.Code;

public abstract class HPFilter {
    protected final int numParams;
    protected final String codeString;

    protected HPFilter(String codeString, int numParams) {
        this.codeString = codeString;
        this.numParams = numParams;
    }

    public final int getNumParams() {
        return this.numParams;
    }

    public final boolean isNumeric() {
        return this.numParams != 0;
    }

    public boolean op(String[] values, int valueNumber, String[] params) {
        throw new UnsupportedOperationException();
    }

    public boolean op(Collection<Code> values, Collection<Code> params) {
        throw new UnsupportedOperationException();
    }

    public abstract boolean op(int[] var1, int var2, int[] var3);

    public abstract boolean op(int[] var1, int var2, long[] var3);

    public abstract boolean op(float[] var1, int var2, float[] var3);

    public abstract boolean op(double[] var1, int var2, double[] var3);

    public final String getCodeString() {
        return this.codeString;
    }

    static boolean memberOf(String value, String[] params) {
        return Arrays.asList(params).contains(value);
    }

    static boolean memberOf(int value, int[] params) {
        return Arrays.stream(params).anyMatch(v -> v == value);
    }

    static boolean memberOf(int value, long[] params) {
        return Arrays.stream(params).anyMatch(v -> v == (long)value);
    }

    static boolean memberOf(float value, float[] params) {
        for (float v : params) {
            if (!MathUtil.isEqual((float)v, (float)value)) continue;
            return true;
        }
        return false;
    }

    static boolean memberOf(double value, double[] params) {
        return Arrays.stream(params).anyMatch(v -> MathUtil.isEqual((double)v, (double)value));
    }

    static boolean memberOf(Code value, Collection<Code> params) {
        for (Code param : params) {
            if (!HPFilter.codeEquals(param, value)) continue;
            return true;
        }
        return false;
    }

    static boolean codeEquals(Code item1, Code item2) {
        if (!Objects.equals(item1.getCodeValue(), item2.getCodeValue())) {
            return false;
        }
        if (!Objects.equals(item1.getCodingSchemeDesignator(), item2.getCodingSchemeDesignator())) {
            return false;
        }
        if (!item1.getAttributes().containsValue(524547) || !item2.getAttributes().containsValue(524547)) {
            return true;
        }
        return Objects.equals(item1.getCodingSchemeVersion(), item2.getCodingSchemeVersion());
    }
}

