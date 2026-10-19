/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.StringUtil
 */
package org.dcm4che3.img.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.dcm4che3.data.Attributes;
import org.weasis.core.util.StringUtil;

public final class DateTimeUtils {
    private static final DateTimeFormatter DA_PARSER = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4).optionalStart().appendLiteral('.').optionalEnd().appendValue(ChronoField.MONTH_OF_YEAR, 2).optionalStart().appendLiteral('.').optionalEnd().appendValue(ChronoField.DAY_OF_MONTH, 2).toFormatter();
    private static final DateTimeFormatter DA_FORMATTER = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4).appendValue(ChronoField.MONTH_OF_YEAR, 2).appendValue(ChronoField.DAY_OF_MONTH, 2).toFormatter();
    private static final DateTimeFormatter TM_PARSER = new DateTimeFormatterBuilder().appendValue(ChronoField.HOUR_OF_DAY, 2).optionalStart().optionalStart().appendLiteral(':').optionalEnd().appendValue(ChronoField.MINUTE_OF_HOUR, 2).optionalStart().optionalStart().appendLiteral(':').optionalEnd().appendValue(ChronoField.SECOND_OF_MINUTE, 2).optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 0, 6, true).toFormatter();
    private static final DateTimeFormatter TM_FORMATTER = new DateTimeFormatterBuilder().appendValue(ChronoField.HOUR_OF_DAY, 2).appendValue(ChronoField.MINUTE_OF_HOUR, 2).appendValue(ChronoField.SECOND_OF_MINUTE, 2).appendFraction(ChronoField.NANO_OF_SECOND, 6, 6, true).toFormatter();
    private static final DateTimeFormatter DT_PARSER = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4).optionalStart().appendValue(ChronoField.MONTH_OF_YEAR, 2).optionalStart().appendValue(ChronoField.DAY_OF_MONTH, 2).optionalStart().appendValue(ChronoField.HOUR_OF_DAY, 2).optionalStart().appendValue(ChronoField.MINUTE_OF_HOUR, 2).optionalStart().appendValue(ChronoField.SECOND_OF_MINUTE, 2).optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 0, 6, true).optionalEnd().optionalEnd().optionalEnd().optionalEnd().optionalEnd().optionalEnd().optionalStart().appendOffset("+HHMM", "+0000").toFormatter();
    private static final DateTimeFormatter DT_FORMATTER = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4).appendValue(ChronoField.MONTH_OF_YEAR, 2).appendValue(ChronoField.DAY_OF_MONTH, 2).appendValue(ChronoField.HOUR_OF_DAY, 2).appendValue(ChronoField.MINUTE_OF_HOUR, 2).appendValue(ChronoField.SECOND_OF_MINUTE, 2).appendFraction(ChronoField.NANO_OF_SECOND, 6, 6, true).optionalStart().appendOffset("+HHMM", "+0000").toFormatter();
    private static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
    private static final DateTimeFormatter DEFAULT_TIME_FORMATTER = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM);
    private static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);

    private DateTimeUtils() {
    }

    public static LocalDate parseDA(String value) {
        return LocalDate.from(DA_PARSER.parse(value.trim()));
    }

    public static String formatDA(Temporal value) {
        return DA_FORMATTER.format(value);
    }

    public static LocalTime parseTM(String value) {
        return LocalTime.from(TM_PARSER.parse(value.trim()));
    }

    public static LocalTime parseTMMax(String value) {
        return DateTimeUtils.parseTM(value).plusNanos(DateTimeUtils.nanosToAdd(value));
    }

    public static String formatTM(Temporal value) {
        return TM_FORMATTER.format(value);
    }

    public static Temporal parseDT(String value) {
        TemporalAccessor temporal = DT_PARSER.parse(value.trim());
        LocalDate date = DateTimeUtils.extractDate(temporal);
        LocalTime time = DateTimeUtils.extractTime(temporal);
        LocalDateTime dateTime = LocalDateTime.of(date, time);
        return temporal.isSupported(ChronoField.OFFSET_SECONDS) ? ZonedDateTime.of(dateTime, ZoneOffset.ofTotalSeconds(temporal.get(ChronoField.OFFSET_SECONDS))) : dateTime;
    }

    public static Temporal parseDTMax(String value) {
        int length = DateTimeUtils.lengthWithoutZone(value);
        return length > 8 ? DateTimeUtils.parseDT(value).plus(DateTimeUtils.nanosToAdd(length - 8), ChronoUnit.NANOS) : DateTimeUtils.parseDT(value).plus(1L, DateTimeUtils.yearsMonthsDays(length)).minus(1L, ChronoUnit.NANOS);
    }

    public static String formatDT(Temporal value) {
        return DT_FORMATTER.format(value);
    }

    public static LocalDateTime dateTime(LocalDate date, LocalTime time) {
        if (date == null) {
            return null;
        }
        return time == null ? date.atStartOfDay() : LocalDateTime.of(date, time);
    }

    public static long combineTags(int tagDate, int tagTime) {
        return (long)tagDate << 32 | (long)tagTime & 0xFFFFFFFFL;
    }

    public static Date dateTime(Attributes dcm, int tagDate, int tagTime) {
        return dcm == null ? null : dcm.getDate(DateTimeUtils.combineTags(tagDate, tagTime));
    }

    public static Date dateTime(TimeZone tz, Date date, Date time, boolean acceptNullDateOrTime) {
        if (!(acceptNullDateOrTime || date != null && time != null)) {
            return null;
        }
        Calendar calendar = DateTimeUtils.createCalendar(tz, date);
        DateTimeUtils.setDateFields(calendar, date);
        DateTimeUtils.setTimeFields(calendar, time);
        return calendar.getTime();
    }

    public static String truncateTM(String value, int maxLength) {
        if (maxLength < 2) {
            throw new IllegalArgumentException("maxLength " + maxLength + " < 2");
        }
        return DateTimeUtils.truncate(value, value.length(), maxLength, 8);
    }

    public static String truncateDT(String value, int maxLength) {
        if (maxLength < 4) {
            throw new IllegalArgumentException("maxLength " + maxLength + " < 4");
        }
        int index = DateTimeUtils.indexOfZone(value);
        return index < 0 ? DateTimeUtils.truncate(value, value.length(), maxLength, 16) : DateTimeUtils.truncate(value, index, maxLength, 16) + value.substring(index);
    }

    public static String formatDateTime(TemporalAccessor date) {
        return DateTimeUtils.formatDateTime(date, Locale.getDefault());
    }

    public static String formatDateTime(TemporalAccessor date, Locale locale) {
        if (date instanceof LocalDate) {
            return DEFAULT_DATE_FORMATTER.withLocale(locale).format(date);
        }
        if (date instanceof LocalTime) {
            return DEFAULT_TIME_FORMATTER.withLocale(locale).format(date);
        }
        if (date instanceof LocalDateTime || date instanceof ZonedDateTime) {
            return DEFAULT_DATETIME_FORMATTER.withLocale(locale).format(date);
        }
        if (date instanceof Instant) {
            Instant instant = (Instant)date;
            return DEFAULT_DATETIME_FORMATTER.withLocale(locale).format(instant.atZone(ZoneId.systemDefault()));
        }
        return "";
    }

    public static LocalDate toLocalDate(Date date) {
        return date == null ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalTime toLocalTime(Date date) {
        return date == null ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalTime();
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        return date == null ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    public static GregorianCalendar parseXmlDateTime(CharSequence s) throws DatatypeConfigurationException {
        if (!StringUtil.hasText((CharSequence)s)) {
            throw new IllegalArgumentException("Input CharSequence cannot be null or empty");
        }
        String val = s.toString().trim();
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(val).toGregorianCalendar();
    }

    private static LocalDate extractDate(TemporalAccessor temporal) {
        return temporal.isSupported(ChronoField.DAY_OF_MONTH) ? LocalDate.from(temporal) : LocalDate.of(temporal.get(ChronoField.YEAR), DateTimeUtils.getMonth(temporal), 1);
    }

    private static LocalTime extractTime(TemporalAccessor temporal) {
        return temporal.isSupported(ChronoField.HOUR_OF_DAY) ? LocalTime.from(temporal) : LocalTime.MIN;
    }

    private static int getMonth(TemporalAccessor temporal) {
        return temporal.isSupported(ChronoField.MONTH_OF_YEAR) ? temporal.get(ChronoField.MONTH_OF_YEAR) : 1;
    }

    private static Calendar createCalendar(TimeZone tz, Date date) {
        return tz == null || date == null ? Calendar.getInstance() : Calendar.getInstance(tz);
    }

    private static void setDateFields(Calendar calendar, Date date) {
        Calendar datePart = Calendar.getInstance();
        datePart.setTime(date == null ? new Date(0L) : date);
        calendar.set(1, datePart.get(1));
        calendar.set(2, datePart.get(2));
        calendar.set(5, datePart.get(5));
    }

    private static void setTimeFields(Calendar calendar, Date time) {
        Calendar timePart = Calendar.getInstance();
        timePart.setTime(time == null ? new Date(0L) : time);
        calendar.set(11, timePart.get(11));
        calendar.set(12, timePart.get(12));
        calendar.set(13, timePart.get(13));
        calendar.set(14, timePart.get(14));
    }

    private static long nanosToAdd(String tm) {
        int length = tm.length();
        int index = tm.lastIndexOf(58);
        if (index > 0) {
            length -= index > 4 ? 2 : 1;
        }
        return DateTimeUtils.nanosToAdd(length);
    }

    private static long nanosToAdd(int length) {
        return switch (length) {
            case 2 -> 3599999999999L;
            case 4 -> 59999999999L;
            case 6, 7 -> 999999999L;
            case 8 -> 99999999L;
            case 9 -> 9999999L;
            case 10 -> 999999L;
            case 11 -> 99999L;
            case 12 -> 9999L;
            case 13 -> 999L;
            default -> throw new IllegalArgumentException("Unsupported length: " + length);
        };
    }

    private static ChronoUnit yearsMonthsDays(int length) {
        return switch (length) {
            case 4 -> ChronoUnit.YEARS;
            case 6 -> ChronoUnit.MONTHS;
            case 8 -> ChronoUnit.DAYS;
            default -> throw new IllegalArgumentException("Unsupported length: " + length);
        };
    }

    private static int lengthWithoutZone(String value) {
        int index = DateTimeUtils.indexOfZone(value);
        return index < 0 ? value.length() : index;
    }

    private static int indexOfZone(String value) {
        int index = value.length() - 5;
        return index >= 4 && DateTimeUtils.isSign(value.charAt(index)) ? index : -1;
    }

    private static boolean isSign(char ch) {
        return ch == '+' || ch == '-';
    }

    private static String truncate(String value, int length, int maxLength, int fractionPos) {
        return value.substring(0, DateTimeUtils.adjustMaxLength(Math.min(length, maxLength), fractionPos));
    }

    private static int adjustMaxLength(int maxLength, int fractionPos) {
        return maxLength < fractionPos ? maxLength & 0xFFFFFFFE : maxLength;
    }
}

