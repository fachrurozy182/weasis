/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

public class DecFormatter {
    private static final NumberFormat df1 = NumberFormat.getNumberInstance();
    private static final NumberFormat df2 = NumberFormat.getNumberInstance();
    private static final NumberFormat df4 = NumberFormat.getNumberInstance();
    private static final NumberFormat percent2 = NumberFormat.getPercentInstance();
    private static final DecimalFormat decimalAndNumber = new DecimalFormat("#,##0.#");
    private static final DecimalFormat dfSci = new DecimalFormat("0.####E0");

    private DecFormatter() {
    }

    public static String allNumber(Number val) {
        return decimalAndNumber.format(val);
    }

    public static String oneDecimal(Number val) {
        return df1.format(val);
    }

    public static String twoDecimal(Number val) {
        return df2.format(val);
    }

    public static String fourDecimal(Number val) {
        return df4.format(val);
    }

    public static String percentTwoDecimal(Number val) {
        return percent2.format(val);
    }

    public static String scientificFormat(Number val) {
        return dfSci.format(val);
    }

    public static DefaultFormatterFactory setPreciseDoubleFormat(double min, double max) {
        NumberFormatter displayFormatter = new NumberFormatter(new DecimalFormat("#,##0.##"));
        displayFormatter.setValueClass(Double.class);
        NumberFormatter editFormatter = new NumberFormatter(new DecimalFormat("#,##0.0#############"));
        editFormatter.setValueClass(Double.class);
        editFormatter.setMinimum(Double.valueOf(min));
        editFormatter.setMaximum(Double.valueOf(max));
        editFormatter.setAllowsInvalid(true);
        return new DefaultFormatterFactory(displayFormatter, displayFormatter, editFormatter);
    }

    static {
        df1.setMaximumFractionDigits(1);
        df2.setMaximumFractionDigits(2);
        df4.setMaximumFractionDigits(4);
        percent2.setMaximumFractionDigits(2);
    }
}

