/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.data;

import java.time.temporal.Temporal;
import java.util.Date;
import java.util.TimeZone;
import org.dcm4che3.data.DatePrecision;
import org.dcm4che3.util.DateUtils;

enum TemporalType {
    DA{

        @Override
        public Temporal parseTemporal(String s, DatePrecision precision) {
            precision.lastField = 5;
            return DateUtils.parseLocalDA(s);
        }

        @Override
        public Date parse(TimeZone tz, String s, boolean ceil, DatePrecision precision) {
            precision.lastField = 5;
            return DateUtils.parseDA(null, s, ceil);
        }

        @Override
        public String format(TimeZone tz, Date date, DatePrecision precision) {
            return DateUtils.formatDA(tz, date);
        }
    }
    ,
    DT{

        @Override
        public Temporal parseTemporal(String s, DatePrecision precision) {
            return DateUtils.parseTemporalDT(s, precision);
        }

        @Override
        public Date parse(TimeZone tz, String s, boolean ceil, DatePrecision precision) {
            return DateUtils.parseDT(tz, s, ceil, precision);
        }

        @Override
        public String format(TimeZone tz, Date date, DatePrecision precision) {
            return DateUtils.formatDT(tz, date, precision);
        }
    }
    ,
    TM{

        @Override
        public Temporal parseTemporal(String s, DatePrecision precision) {
            return DateUtils.parseLocalTM(s, precision);
        }

        @Override
        public Date parse(TimeZone tz, String s, boolean ceil, DatePrecision precision) {
            return DateUtils.parseTM(null, s, ceil, precision);
        }

        @Override
        public String format(TimeZone tz, Date date, DatePrecision precision) {
            return DateUtils.formatTM(tz, date, precision);
        }
    };


    public abstract Temporal parseTemporal(String var1, DatePrecision var2);

    public abstract Date parse(TimeZone var1, String var2, boolean var3, DatePrecision var4);

    public abstract String format(TimeZone var1, Date var2, DatePrecision var3);
}

