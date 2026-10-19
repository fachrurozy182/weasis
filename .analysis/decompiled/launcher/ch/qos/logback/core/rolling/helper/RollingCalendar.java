/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.rolling.helper;

import ch.qos.logback.core.rolling.helper.PeriodicityType;
import ch.qos.logback.core.spi.ContextAwareBase;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class RollingCalendar
extends GregorianCalendar {
    private static final long serialVersionUID = -5937537740925066161L;
    static final TimeZone GMT_TIMEZONE = TimeZone.getTimeZone("GMT");
    PeriodicityType periodicityType = PeriodicityType.ERRONEOUS;
    String datePattern;

    public RollingCalendar(String datePattern) {
        this.datePattern = datePattern;
        this.periodicityType = this.computePeriodicityType();
    }

    public RollingCalendar(String datePattern, TimeZone tz, Locale locale) {
        super(tz, locale);
        this.datePattern = datePattern;
        this.periodicityType = this.computePeriodicityType();
    }

    public PeriodicityType getPeriodicityType() {
        return this.periodicityType;
    }

    public PeriodicityType computePeriodicityType() {
        GregorianCalendar calendar = new GregorianCalendar(GMT_TIMEZONE, Locale.getDefault());
        Instant epoch = Instant.ofEpochMilli(0L);
        if (this.datePattern != null) {
            for (PeriodicityType i : PeriodicityType.VALID_ORDERED_LIST) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(this.datePattern, Locale.getDefault());
                simpleDateFormat.setTimeZone(GMT_TIMEZONE);
                String r0 = simpleDateFormat.format(new Date(0L));
                Instant next = RollingCalendar.innerGetEndOfThisPeriod(calendar, i, epoch);
                String r1 = simpleDateFormat.format(new Date(next.toEpochMilli()));
                if (r0 == null || r1 == null || r0.equals(r1)) continue;
                return i;
            }
        }
        return PeriodicityType.ERRONEOUS;
    }

    public boolean isCollisionFree() {
        switch (this.periodicityType) {
            case TOP_OF_HOUR: {
                return !this.collision(43200000L);
            }
            case TOP_OF_DAY: {
                if (this.collision(604800000L)) {
                    return false;
                }
                if (this.collision(2678400000L)) {
                    return false;
                }
                return !this.collision(31536000000L);
            }
            case TOP_OF_WEEK: {
                if (this.collision(2937600000L)) {
                    return false;
                }
                return !this.collision(31622400000L);
            }
        }
        return true;
    }

    private boolean collision(long delta) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(this.datePattern);
        simpleDateFormat.setTimeZone(GMT_TIMEZONE);
        Date epoch0 = new Date(0L);
        String r0 = simpleDateFormat.format(epoch0);
        Date epoch12 = new Date(delta);
        String r12 = simpleDateFormat.format(epoch12);
        return r0.equals(r12);
    }

    public void printPeriodicity(ContextAwareBase cab) {
        switch (this.periodicityType) {
            case TOP_OF_MILLISECOND: {
                cab.addInfo("Roll-over every millisecond.");
                break;
            }
            case TOP_OF_SECOND: {
                cab.addInfo("Roll-over every second.");
                break;
            }
            case TOP_OF_MINUTE: {
                cab.addInfo("Roll-over every minute.");
                break;
            }
            case TOP_OF_HOUR: {
                cab.addInfo("Roll-over at the top of every hour.");
                break;
            }
            case HALF_DAY: {
                cab.addInfo("Roll-over at midday and midnight.");
                break;
            }
            case TOP_OF_DAY: {
                cab.addInfo("Roll-over at midnight.");
                break;
            }
            case TOP_OF_WEEK: {
                cab.addInfo("Rollover at the start of week.");
                break;
            }
            case TOP_OF_MONTH: {
                cab.addInfo("Rollover at start of every month.");
                break;
            }
            default: {
                cab.addInfo("Unknown periodicity.");
            }
        }
    }

    public long periodBarriersCrossed(long start, long end) {
        if (start > end) {
            throw new IllegalArgumentException("Start cannot come before end");
        }
        long startFloored = this.getStartOfCurrentPeriodWithGMTOffsetCorrection(start, this.getTimeZone());
        long endFloored = this.getStartOfCurrentPeriodWithGMTOffsetCorrection(end, this.getTimeZone());
        long diff = endFloored - startFloored;
        switch (this.periodicityType) {
            case TOP_OF_MILLISECOND: {
                return diff;
            }
            case TOP_OF_SECOND: {
                return diff / 1000L;
            }
            case TOP_OF_MINUTE: {
                return diff / 60000L;
            }
            case TOP_OF_HOUR: {
                return diff / 3600000L;
            }
            case TOP_OF_DAY: {
                return diff / 86400000L;
            }
            case TOP_OF_WEEK: {
                return diff / 604800000L;
            }
            case TOP_OF_MONTH: {
                return RollingCalendar.diffInMonths(start, end);
            }
        }
        throw new IllegalStateException("Unknown periodicity type.");
    }

    public static int diffInMonths(long startTime, long endTime) {
        if (startTime > endTime) {
            throw new IllegalArgumentException("startTime cannot be larger than endTime");
        }
        Calendar startCal = Calendar.getInstance();
        startCal.setTimeInMillis(startTime);
        Calendar endCal = Calendar.getInstance();
        endCal.setTimeInMillis(endTime);
        int yearDiff = endCal.get(1) - startCal.get(1);
        int monthDiff = endCal.get(2) - startCal.get(2);
        return yearDiff * 12 + monthDiff;
    }

    private static Instant innerGetEndOfThisPeriod(Calendar cal, PeriodicityType periodicityType, Instant instant) {
        return RollingCalendar.innerGetEndOfNextNthPeriod(cal, periodicityType, instant, 1);
    }

    private static Instant innerGetEndOfNextNthPeriod(Calendar cal, PeriodicityType periodicityType, Instant instant, int numPeriods) {
        cal.setTimeInMillis(instant.toEpochMilli());
        switch (periodicityType) {
            case TOP_OF_MILLISECOND: {
                cal.add(14, numPeriods);
                break;
            }
            case TOP_OF_SECOND: {
                cal.set(14, 0);
                cal.add(13, numPeriods);
                break;
            }
            case TOP_OF_MINUTE: {
                cal.set(13, 0);
                cal.set(14, 0);
                cal.add(12, numPeriods);
                break;
            }
            case TOP_OF_HOUR: {
                cal.set(12, 0);
                cal.set(13, 0);
                cal.set(14, 0);
                cal.add(11, numPeriods);
                break;
            }
            case TOP_OF_DAY: {
                cal.set(11, 0);
                cal.set(12, 0);
                cal.set(13, 0);
                cal.set(14, 0);
                cal.add(5, numPeriods);
                break;
            }
            case TOP_OF_WEEK: {
                cal.set(7, cal.getFirstDayOfWeek());
                cal.set(11, 0);
                cal.set(12, 0);
                cal.set(13, 0);
                cal.set(14, 0);
                cal.add(3, numPeriods);
                break;
            }
            case TOP_OF_MONTH: {
                cal.set(5, 1);
                cal.set(11, 0);
                cal.set(12, 0);
                cal.set(13, 0);
                cal.set(14, 0);
                cal.add(2, numPeriods);
                break;
            }
            default: {
                throw new IllegalStateException("Unknown periodicity type.");
            }
        }
        return Instant.ofEpochMilli(cal.getTimeInMillis());
    }

    public Instant getEndOfNextNthPeriod(Instant instant, int periods) {
        return RollingCalendar.innerGetEndOfNextNthPeriod(this, this.periodicityType, instant, periods);
    }

    public Instant getNextTriggeringDate(Instant instant) {
        return this.getEndOfNextNthPeriod(instant, 1);
    }

    public long getStartOfCurrentPeriodWithGMTOffsetCorrection(long now, TimeZone timezone) {
        Calendar aCal = Calendar.getInstance(timezone);
        aCal.setTimeInMillis(now);
        Instant instant = Instant.ofEpochMilli(aCal.getTimeInMillis());
        Instant toppedInstant = this.getEndOfNextNthPeriod(instant, 0);
        Calendar secondCalendar = Calendar.getInstance(timezone);
        secondCalendar.setTimeInMillis(toppedInstant.toEpochMilli());
        long gmtOffset = secondCalendar.get(15) + secondCalendar.get(16);
        return toppedInstant.toEpochMilli() + gmtOffset;
    }
}

