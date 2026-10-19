/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.dcm4che3.data.DatePrecision;

public class DateUtils {
    public static final Date[] EMPTY_DATES = new Date[0];
    private static TimeZone cachedTimeZone;

    private static Calendar cal(TimeZone tz) {
        GregorianCalendar cal = tz != null ? new GregorianCalendar(tz) : new GregorianCalendar();
        cal.clear();
        return cal;
    }

    private static Calendar cal(TimeZone tz, Date date) {
        GregorianCalendar cal = tz != null ? new GregorianCalendar(tz) : new GregorianCalendar();
        cal.setTime(date);
        return cal;
    }

    private static void ceil(Calendar cal, int field) {
        cal.add(field, 1);
        cal.add(14, -1);
    }

    public static String formatDA(TimeZone tz, Date date) {
        return DateUtils.formatDA(tz, date, new StringBuilder(8)).toString();
    }

    public static StringBuilder formatDA(TimeZone tz, Date date, StringBuilder toAppendTo) {
        return DateUtils.formatDT(DateUtils.cal(tz, date), toAppendTo, 5);
    }

    public static String formatTM(TimeZone tz, Date date) {
        return DateUtils.formatTM(tz, date, new DatePrecision());
    }

    public static String formatTM(TimeZone tz, Date date, DatePrecision precision) {
        return DateUtils.formatTM(DateUtils.cal(tz, date), new StringBuilder(10), precision.lastField).toString();
    }

    private static StringBuilder formatTM(Calendar cal, StringBuilder toAppendTo, int lastField) {
        DateUtils.appendXX(cal.get(11), toAppendTo);
        if (lastField > 11) {
            DateUtils.appendXX(cal.get(12), toAppendTo);
            if (lastField > 12) {
                DateUtils.appendXX(cal.get(13), toAppendTo);
                if (lastField > 13) {
                    toAppendTo.append('.');
                    DateUtils.appendXXX(cal.get(14), toAppendTo);
                }
            }
        }
        return toAppendTo;
    }

    public static String formatDT(TimeZone tz, Date date) {
        return DateUtils.formatDT(tz, date, new DatePrecision());
    }

    public static String formatDT(TimeZone tz, Date date, DatePrecision precision) {
        return DateUtils.formatDT(tz, date, new StringBuilder(23), precision).toString();
    }

    public static StringBuilder formatDT(TimeZone tz, Date date, StringBuilder toAppendTo, DatePrecision precision) {
        Calendar cal = DateUtils.cal(tz, date);
        DateUtils.formatDT(cal, toAppendTo, precision.lastField);
        if (precision.includeTimezone) {
            int offset = cal.get(15) + cal.get(16);
            DateUtils.appendZZZZZ(offset, toAppendTo);
        }
        return toAppendTo;
    }

    private static StringBuilder appendZZZZZ(int offset, StringBuilder sb) {
        if (offset < 0) {
            offset = -offset;
            sb.append('-');
        } else {
            sb.append('+');
        }
        int min = offset / 60000;
        DateUtils.appendXX(min / 60, sb);
        DateUtils.appendXX(min % 60, sb);
        return sb;
    }

    public static String formatTimezoneOffsetFromUTC(TimeZone tz) {
        return DateUtils.appendZZZZZ(tz.getRawOffset(), new StringBuilder(5)).toString();
    }

    public static String formatTimezoneOffsetFromUTC(TimeZone tz, Date date) {
        return DateUtils.appendZZZZZ(tz.getOffset(date == null ? System.currentTimeMillis() : date.getTime()), new StringBuilder(5)).toString();
    }

    private static StringBuilder formatDT(Calendar cal, StringBuilder toAppendTo, int lastField) {
        DateUtils.appendXXXX(cal.get(1), toAppendTo);
        if (lastField > 1) {
            DateUtils.appendXX(cal.get(2) + 1, toAppendTo);
            if (lastField > 2) {
                DateUtils.appendXX(cal.get(5), toAppendTo);
                if (lastField > 5) {
                    DateUtils.formatTM(cal, toAppendTo, lastField);
                }
            }
        }
        return toAppendTo;
    }

    private static void appendXXXX(int i, StringBuilder toAppendTo) {
        if (i < 1000) {
            toAppendTo.append('0');
        }
        DateUtils.appendXXX(i, toAppendTo);
    }

    private static void appendXXX(int i, StringBuilder toAppendTo) {
        if (i < 100) {
            toAppendTo.append('0');
        }
        DateUtils.appendXX(i, toAppendTo);
    }

    private static void appendXX(int i, StringBuilder toAppendTo) {
        if (i < 10) {
            toAppendTo.append('0');
        }
        toAppendTo.append(i);
    }

    public static Date parseDA(TimeZone tz, String s) {
        return DateUtils.parseDA(tz, s, false);
    }

    public static LocalDate parseLocalDA(String s) {
        int length = s.length();
        if (length != 8 && (length != 10 || Character.isDigit(s.charAt(4)) || s.charAt(7) != s.charAt(4))) {
            throw new IllegalArgumentException(s);
        }
        int pos = 0;
        int year = DateUtils.parseDigit(s, pos++) * 1000 + DateUtils.parseDigit(s, pos++) * 100 + DateUtils.parseDigit(s, pos++) * 10 + DateUtils.parseDigit(s, pos++);
        if (length == 10) {
            // empty if block
        }
        int n = ++pos;
        int n2 = ++pos;
        ++pos;
        int month = DateUtils.parseDigit(s, n) * 10 + DateUtils.parseDigit(s, n2);
        if (length == 10) {
            // empty if block
        }
        int n3 = ++pos;
        int n4 = ++pos;
        ++pos;
        int dayOfMonth = DateUtils.parseDigit(s, n3) * 10 + DateUtils.parseDigit(s, n4);
        return LocalDate.of(year, month, dayOfMonth);
    }

    public static Date parseDA(TimeZone tz, String s, boolean ceil) {
        Calendar cal = DateUtils.cal(tz);
        int length = s.length();
        if (length != 8 && (length != 10 || Character.isDigit(s.charAt(4)) || s.charAt(7) != s.charAt(4))) {
            throw new IllegalArgumentException(s);
        }
        int pos = 0;
        cal.set(1, DateUtils.parseDigit(s, pos++) * 1000 + DateUtils.parseDigit(s, pos++) * 100 + DateUtils.parseDigit(s, pos++) * 10 + DateUtils.parseDigit(s, pos++));
        if (length == 10) {
            // empty if block
        }
        int n = ++pos;
        int n2 = ++pos;
        ++pos;
        cal.set(2, DateUtils.parseDigit(s, n) * 10 + DateUtils.parseDigit(s, n2) - 1);
        if (length == 10) {
            // empty if block
        }
        int n3 = ++pos;
        int n4 = ++pos;
        ++pos;
        cal.set(5, DateUtils.parseDigit(s, n3) * 10 + DateUtils.parseDigit(s, n4));
        if (ceil) {
            DateUtils.ceil(cal, 5);
        }
        return cal.getTime();
    }

    public static LocalTime parseLocalTM(String s, DatePrecision precision) {
        int pos = 0;
        int length = s.length();
        if (pos + 2 > length) {
            throw new IllegalArgumentException(s);
        }
        precision.lastField = 11;
        int hour = DateUtils.parseDigit(s, pos++) * 10 + DateUtils.parseDigit(s, pos++);
        int minute = 0;
        int second = 0;
        int nanos = 0;
        if (pos < length) {
            if (!Character.isDigit(s.charAt(pos))) {
                ++pos;
            }
            if (pos + 2 > length) {
                throw new IllegalArgumentException(s);
            }
            precision.lastField = 12;
            minute = DateUtils.parseDigit(s, pos++) * 10 + DateUtils.parseDigit(s, pos++);
            if (pos < length) {
                if (!Character.isDigit(s.charAt(pos))) {
                    ++pos;
                }
                if (pos + 2 > length) {
                    throw new IllegalArgumentException(s);
                }
                precision.lastField = 13;
                second = DateUtils.parseDigit(s, pos++) * 10 + DateUtils.parseDigit(s, pos++);
                int n = length - pos;
                if (n > 0) {
                    if (s.charAt(pos++) != '.') {
                        throw new IllegalArgumentException(s);
                    }
                    int fraction = 100000000;
                    for (int i = 1; i < n; ++i) {
                        int d = DateUtils.parseDigit(s, pos++);
                        nanos += d * fraction;
                        fraction /= 10;
                    }
                    precision.lastField = 14;
                }
            }
        }
        return LocalTime.of(hour, minute, second, nanos);
    }

    public static Date parseTM(TimeZone tz, String s, DatePrecision precision) {
        return DateUtils.parseTM(tz, s, false, precision);
    }

    public static Date parseTM(TimeZone tz, String s, boolean ceil, DatePrecision precision) {
        return DateUtils.parseTM(DateUtils.cal(tz), DateUtils.truncateTimeZone(s), ceil, precision);
    }

    private static String truncateTimeZone(String s) {
        char sign;
        int length = s.length();
        if (length > 4 && ((sign = s.charAt(length - 5)) == '+' || sign == '-')) {
            return s.substring(0, length - 5);
        }
        return s;
    }

    private static Date parseTM(Calendar cal, String s, boolean ceil, DatePrecision precision) {
        int pos = 0;
        int length = s.length();
        if (pos + 2 > length) {
            throw new IllegalArgumentException(s);
        }
        precision.lastField = 11;
        cal.set(11, DateUtils.parseDigit(s, pos++) * 10 + DateUtils.parseDigit(s, pos++));
        if (pos < length) {
            if (!Character.isDigit(s.charAt(pos))) {
                ++pos;
            }
            if (pos + 2 > length) {
                throw new IllegalArgumentException(s);
            }
            precision.lastField = 12;
            cal.set(12, DateUtils.parseDigit(s, pos++) * 10 + DateUtils.parseDigit(s, pos++));
            if (pos < length) {
                if (!Character.isDigit(s.charAt(pos))) {
                    ++pos;
                }
                if (pos + 2 > length) {
                    throw new IllegalArgumentException(s);
                }
                precision.lastField = 13;
                cal.set(13, DateUtils.parseDigit(s, pos++) * 10 + DateUtils.parseDigit(s, pos++));
                int n = length - pos;
                if (n > 0) {
                    int i;
                    if (s.charAt(pos++) != '.') {
                        throw new IllegalArgumentException(s);
                    }
                    int millis = 0;
                    for (i = 1; i < n; ++i) {
                        int d = DateUtils.parseDigit(s, pos++);
                        if (i < 4) {
                            millis += d;
                        } else if (i == 4 & d > 4) {
                            ++millis;
                        }
                        if (i >= 3) continue;
                        millis *= 10;
                    }
                    for (i = n; i < 3; ++i) {
                        millis *= 10;
                    }
                    precision.lastField = 14;
                    cal.set(14, millis);
                    return cal.getTime();
                }
            }
        }
        if (ceil) {
            DateUtils.ceil(cal, precision.lastField);
        }
        return cal.getTime();
    }

    private static int parseDigit(String s, int index) {
        int d = s.charAt(index) - 48;
        if (d < 0 || d > 9) {
            throw new IllegalArgumentException(s);
        }
        return d;
    }

    public static Date parseDT(TimeZone tz, String s, DatePrecision precision) {
        return DateUtils.parseDT(tz, s, false, precision);
    }

    public static TimeZone timeZone(String s) {
        TimeZone tz;
        if (s.length() != 5 || (tz = DateUtils.safeTimeZone(s)) == null) {
            throw new IllegalArgumentException("Illegal Timezone Offset: " + s);
        }
        return tz;
    }

    private static TimeZone safeTimeZone(String s) {
        String tzid = DateUtils.tzid(s);
        if (tzid == null) {
            return null;
        }
        TimeZone tz = cachedTimeZone;
        if (tz == null || !tz.getID().equals(tzid)) {
            cachedTimeZone = tz = TimeZone.getTimeZone(tzid);
        }
        return tz;
    }

    private static ZoneOffset safeZoneOffset(String s) {
        char sign;
        int length = s.length();
        if (length < 5) {
            return null;
        }
        int pos = length - 5;
        if ((sign = s.charAt(pos++)) != '+' && sign != '-') {
            return null;
        }
        try {
            int hour = DateUtils.parseDigit(s, pos++) * 10 + DateUtils.parseDigit(s, pos++);
            int minute = DateUtils.parseDigit(s, pos++) * 10 + DateUtils.parseDigit(s, pos++);
            if (sign == '-') {
                hour *= -1;
            }
            return ZoneOffset.ofHoursMinutes(hour, minute);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String tzid(String s) {
        int length = s.length();
        if (length > 4) {
            char[] tzid = new char[]{'G', 'M', 'T', '\u0000', '\u0000', '\u0000', ':', '\u0000', '\u0000'};
            s.getChars(length - 5, length - 2, tzid, 3);
            s.getChars(length - 2, length, tzid, 7);
            if ((tzid[3] == '+' || tzid[3] == '-') && Character.isDigit(tzid[4]) && Character.isDigit(tzid[5]) && Character.isDigit(tzid[7]) && Character.isDigit(tzid[8])) {
                return new String(tzid);
            }
        }
        return null;
    }

    public static Date parseDT(TimeZone tz, String s, boolean ceil, DatePrecision precision) {
        int length = s.length();
        TimeZone tz1 = DateUtils.safeTimeZone(s);
        precision.includeTimezone = tz1 != null;
        if (precision.includeTimezone) {
            length -= 5;
            tz = tz1;
        }
        Calendar cal = DateUtils.cal(tz);
        int pos = 0;
        if (pos + 4 > length) {
            throw new IllegalArgumentException(s);
        }
        precision.lastField = 1;
        cal.set(1, DateUtils.parseDigit(s, pos++) * 1000 + DateUtils.parseDigit(s, pos++) * 100 + DateUtils.parseDigit(s, pos++) * 10 + DateUtils.parseDigit(s, pos++));
        if (pos < length) {
            if (!Character.isDigit(s.charAt(pos))) {
                ++pos;
            }
            if (pos + 2 > length) {
                throw new IllegalArgumentException(s);
            }
            precision.lastField = 2;
            cal.set(2, DateUtils.parseDigit(s, pos++) * 10 + DateUtils.parseDigit(s, pos++) - 1);
            if (pos < length) {
                if (!Character.isDigit(s.charAt(pos))) {
                    ++pos;
                }
                if (pos + 2 > length) {
                    throw new IllegalArgumentException(s);
                }
                precision.lastField = 5;
                cal.set(5, DateUtils.parseDigit(s, pos++) * 10 + DateUtils.parseDigit(s, pos++));
                if (pos < length) {
                    return DateUtils.parseTM(cal, s.substring(pos, length), ceil, precision);
                }
            }
        }
        if (ceil) {
            DateUtils.ceil(cal, precision.lastField);
        }
        return cal.getTime();
    }

    public static Temporal parseTemporalDT(String s, DatePrecision precision) {
        int pos;
        int length = s.length();
        ZoneOffset offset = DateUtils.safeZoneOffset(s);
        if (offset != null) {
            precision.includeTimezone = true;
            length -= 5;
        }
        if ((pos = 0) + 4 > length) {
            throw new IllegalArgumentException(s);
        }
        precision.lastField = 1;
        int year = DateUtils.parseDigit(s, pos++) * 1000 + DateUtils.parseDigit(s, pos++) * 100 + DateUtils.parseDigit(s, pos++) * 10 + DateUtils.parseDigit(s, pos++);
        int month = 0;
        int day = 0;
        LocalTime time = null;
        if (pos < length) {
            if (!Character.isDigit(s.charAt(pos))) {
                ++pos;
            }
            if (pos + 2 > length) {
                throw new IllegalArgumentException(s);
            }
            precision.lastField = 2;
            month = DateUtils.parseDigit(s, pos++) * 10 + DateUtils.parseDigit(s, pos++);
            if (pos < length) {
                if (!Character.isDigit(s.charAt(pos))) {
                    ++pos;
                }
                if (pos + 2 > length) {
                    throw new IllegalArgumentException(s);
                }
                precision.lastField = 5;
                day = DateUtils.parseDigit(s, pos++) * 10 + DateUtils.parseDigit(s, pos++);
                if (pos < length) {
                    time = DateUtils.parseLocalTM(s.substring(pos, length), precision);
                }
            }
        }
        if (time == null) {
            time = LocalTime.of(0, 0);
        }
        LocalDateTime dateTime = LocalDate.of(year, month, day).atTime(time);
        if (offset != null) {
            return dateTime.atOffset(offset);
        }
        return dateTime;
    }
}

