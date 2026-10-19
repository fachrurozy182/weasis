/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.core.api.media.data;

import java.lang.reflect.Array;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.media.data.AttributeSource;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagW;

public final class TagUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(TagUtil.class);

    private TagUtil() {
    }

    public static Date toLocalDate(TemporalAccessor temporal) {
        if (temporal != null) {
            try {
                TemporalAccessor t = temporal;
                if (temporal instanceof LocalDate) {
                    LocalDate date = (LocalDate)temporal;
                    t = date.atStartOfDay(ZoneId.systemDefault());
                } else if (temporal instanceof LocalTime) {
                    LocalTime time = (LocalTime)temporal;
                    t = time.atDate(LocalDate.ofEpochDay(0L)).atZone(ZoneId.systemDefault());
                } else if (temporal instanceof LocalDateTime) {
                    LocalDateTime dateTime = (LocalDateTime)temporal;
                    t = dateTime.atZone(ZoneId.systemDefault());
                }
                return Date.from(Instant.from(t));
            }
            catch (Exception e) {
                LOGGER.error("Date conversion", (Throwable)e);
            }
        }
        return null;
    }

    public static Date[] toLocalDates(Object array) {
        if (array != null && array.getClass().isArray()) {
            Date[] dates = new Date[Array.getLength(array)];
            for (int i = 0; i < dates.length; ++i) {
                dates[i] = TagUtil.toLocalDate((TemporalAccessor)Array.get(array, i));
            }
            return dates;
        }
        return null;
    }

    public static LocalDate toLocalDate(Date date) {
        if (date != null) {
            try {
                LocalDateTime datetime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                return datetime.toLocalDate();
            }
            catch (Exception e) {
                LOGGER.error("Date conversion", (Throwable)e);
            }
        }
        return null;
    }

    public static LocalTime toLocalTime(Date date) {
        if (date != null) {
            try {
                LocalDateTime datetime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                return datetime.toLocalTime();
            }
            catch (Exception e) {
                LOGGER.error("Time conversion", (Throwable)e);
            }
        }
        return null;
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        if (date != null) {
            try {
                return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            }
            catch (Exception e) {
                LOGGER.error("DateTime conversion", (Throwable)e);
            }
        }
        return null;
    }

    public static ZonedDateTime getZonedDateTime(LocalDateTime dateTime, TimeZone timeZone) {
        if (dateTime != null) {
            ZoneId zoneId = timeZone == null ? ZoneId.systemDefault() : timeZone.toZoneId();
            return dateTime.atZone(zoneId);
        }
        return null;
    }

    public static String formatDateTime(TemporalAccessor date) {
        if (date instanceof LocalDate) {
            return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(date);
        }
        if (date instanceof LocalTime) {
            return DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).format(date);
        }
        if (date instanceof LocalDateTime) {
            return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(date);
        }
        if (date instanceof ZonedDateTime) {
            ZonedDateTime zonedDateTime = (ZonedDateTime)date;
            return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()));
        }
        if (date instanceof Instant) {
            Instant instant = (Instant)date;
            return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(instant.atZone(ZoneId.systemDefault()));
        }
        return "";
    }

    public static TagW[] getTagFromKeywords(String ... tagKey) {
        ArrayList<TagW> list = new ArrayList<TagW>();
        if (tagKey != null) {
            for (String key : tagKey) {
                TagW t = TagW.get(key);
                if (t == null) continue;
                list.add(t);
            }
        }
        return list.toArray(new TagW[0]);
    }

    public static String getTagAttribute(AttributeSource source, String attribute, String defaultValue) {
        String val;
        if (attribute != null && (val = source.getAttribute(attribute)) != null) {
            return val;
        }
        return defaultValue;
    }

    public static String[] getStringArrayTagAttribute(AttributeSource source, String attribute, String[] defaultValue) {
        return TagUtil.getStringArrayTagAttribute(source, attribute, defaultValue, "\\");
    }

    public static String[] getStringArrayTagAttribute(AttributeSource source, String attribute, String[] defaultValue, String separator) {
        String val;
        if (attribute != null && (val = source.getAttribute(attribute)) != null) {
            return val.split(Pattern.quote(separator));
        }
        return defaultValue;
    }

    public static Boolean getBooleanTagAttribute(AttributeSource source, String attribute, Boolean defaultValue) {
        if (attribute != null) {
            String val = source.getAttribute(attribute);
            try {
                if (val != null) {
                    return Boolean.valueOf(val);
                }
            }
            catch (NumberFormatException e) {
                LOGGER.error("Cannot parse boolean {} of {}", (Object)val, (Object)attribute);
            }
        }
        return defaultValue;
    }

    public static Integer getIntegerTagAttribute(AttributeSource source, String attribute, Integer defaultValue) {
        if (attribute != null) {
            String val = source.getAttribute(attribute);
            try {
                if (val != null) {
                    return Integer.valueOf(val);
                }
            }
            catch (NumberFormatException e) {
                LOGGER.error("Cannot parse integer {} of {}", (Object)val, (Object)attribute);
            }
        }
        return defaultValue;
    }

    public static int[] getIntArrayTagAttribute(AttributeSource source, String attribute, int[] defaultValue) {
        return TagUtil.getIntArrayTagAttribute(source, attribute, defaultValue, "\\");
    }

    public static int[] getIntArrayTagAttribute(AttributeSource source, String attribute, int[] defaultValue, String separator) {
        String val;
        if (attribute != null && (val = source.getAttribute(attribute)) != null) {
            String[] strs = val.split(Pattern.quote(separator));
            int[] vals = new int[strs.length];
            for (int i = 0; i < strs.length; ++i) {
                vals[i] = Integer.parseInt(strs[i], 10);
            }
            return vals;
        }
        return defaultValue;
    }

    public static Double getDoubleTagAttribute(AttributeSource source, String attribute, Double defaultValue) {
        if (attribute != null) {
            String val = source.getAttribute(attribute);
            try {
                if (val != null) {
                    return Double.valueOf(val);
                }
            }
            catch (NumberFormatException e) {
                LOGGER.error("Cannot parse double {} of {}", (Object)val, (Object)attribute);
            }
        }
        return defaultValue;
    }

    public static double[] getDoubleArrayTagAttribute(AttributeSource source, String attribute, double[] defaultValue) {
        return TagUtil.getDoubleArrayTagAttribute(source, attribute, defaultValue, "\\");
    }

    public static double[] getDoubleArrayTagAttribute(AttributeSource source, String attribute, double[] defaultValue, String separator) {
        String val;
        if (attribute != null && (val = source.getAttribute(attribute)) != null) {
            String[] strs = val.split(Pattern.quote(separator));
            double[] vals = new double[strs.length];
            for (int i = 0; i < strs.length; ++i) {
                vals[i] = Double.parseDouble(strs[0]);
            }
            return vals;
        }
        return defaultValue;
    }

    public static Float getFloatTagAttribute(AttributeSource source, String attribute, Float defaultValue) {
        if (attribute != null) {
            String val = source.getAttribute(attribute);
            try {
                if (val != null) {
                    return Float.valueOf(val);
                }
            }
            catch (NumberFormatException e) {
                LOGGER.error("Cannot parse float {} of {}", (Object)val, (Object)attribute);
            }
        }
        return defaultValue;
    }

    public static float[] getFloatArrayTagAttribute(AttributeSource source, String attribute, float[] defaultValue) {
        return TagUtil.getFloatArrayTagAttribute(source, attribute, defaultValue, "\\");
    }

    public static float[] getFloatArrayTagAttribute(AttributeSource source, String attribute, float[] defaultValue, String separator) {
        String val;
        if (attribute != null && (val = source.getAttribute(attribute)) != null) {
            String[] strs = val.split(Pattern.quote(separator));
            float[] vals = new float[strs.length];
            for (int i = 0; i < strs.length; ++i) {
                vals[i] = Float.parseFloat(strs[0]);
            }
            return vals;
        }
        return defaultValue;
    }

    public static TemporalAccessor getDateFromElement(AttributeSource source, String attribute, TagW.TagType type, TemporalAccessor defaultValue) {
        String val;
        if (attribute != null && (val = source.getAttribute(attribute)) != null) {
            try {
                if (TagW.TagType.TIME.equals((Object)type)) {
                    return LocalTime.parse(val);
                }
                if (TagW.TagType.DATETIME.equals((Object)type)) {
                    return LocalDateTime.parse(val);
                }
                return LocalDate.parse(val);
            }
            catch (Exception e) {
                LOGGER.error("Parse date", (Throwable)e);
            }
        }
        return defaultValue;
    }

    public static TemporalAccessor[] getDatesFromElement(AttributeSource source, String attribute, TagW.TagType type, TemporalAccessor[] defaultValue) {
        return TagUtil.getDatesFromElement(source, attribute, type, defaultValue, "\\");
    }

    public static TemporalAccessor[] getDatesFromElement(AttributeSource source, String attribute, TagW.TagType type, TemporalAccessor[] defaultValue, String separator) {
        String val;
        if (attribute != null && (val = source.getAttribute(attribute)) != null) {
            String[] strs = val.split(Pattern.quote(separator));
            TemporalAccessor[] vals = new TemporalAccessor[strs.length];
            for (int i = 0; i < strs.length; ++i) {
                try {
                    if (TagW.TagType.TIME.equals((Object)type)) {
                        vals[i] = LocalTime.parse(strs[i]);
                        continue;
                    }
                    if (TagW.TagType.DATETIME.equals((Object)type)) {
                        vals[i] = LocalDateTime.parse(strs[i]);
                        continue;
                    }
                    vals[i] = LocalDate.parse(strs[i]);
                    continue;
                }
                catch (Exception e) {
                    LOGGER.error("Parse date", (Throwable)e);
                }
            }
            return vals;
        }
        return defaultValue;
    }

    public static boolean isEquals(Object value1, Object value2) {
        if (value1 == null && value2 == null) {
            return true;
        }
        if (value1 == null || value2 == null) {
            return false;
        }
        if (value1.getClass().isArray()) {
            int vm2;
            if (!value2.getClass().isArray()) {
                return false;
            }
            int vm1 = Array.getLength(value1);
            if (vm1 != (vm2 = Array.getLength(value2))) {
                return false;
            }
            for (int i = 0; i < vm1; ++i) {
                Object o1 = Array.get(value1, i);
                Object o2 = Array.get(value2, i);
                if (o1 == null && o2 == null || o1 == null || o1.equals(o2)) continue;
                return false;
            }
            return true;
        }
        return value1.equals(value2);
    }

    public static Object getTagValue(TagW tag, TagReadable ... taggable) {
        for (TagReadable t : taggable) {
            Object val;
            if (t == null || (val = t.getTagValue(tag)) == null) continue;
            return val;
        }
        return null;
    }

    public static boolean isEquals(Object value1, Object value2, boolean ignoreCase) {
        if (value1 == null && value2 == null) {
            return true;
        }
        if (value1 == null || value2 == null) {
            return false;
        }
        if (value1.getClass().isArray()) {
            int vm2;
            if (!value2.getClass().isArray()) {
                return false;
            }
            int vm1 = Array.getLength(value1);
            if (vm1 != (vm2 = Array.getLength(value2))) {
                return false;
            }
            for (int i = 0; i < vm1; ++i) {
                Object o1 = Array.get(value1, i);
                Object o2 = Array.get(value2, i);
                if (o1 == null && o2 == null) continue;
                if (ignoreCase && o1 instanceof String) {
                    String st1 = (String)o1;
                    if (o2 instanceof String) {
                        String st2 = (String)o2;
                        if (st1.equalsIgnoreCase(st2)) continue;
                        return false;
                    }
                }
                if (o1 == null || o1.equals(o2)) continue;
                return false;
            }
            return true;
        }
        if (ignoreCase && value1 instanceof String) {
            String st1 = (String)value1;
            if (value2 instanceof String) {
                String st2 = (String)value2;
                return st1.equalsIgnoreCase(st2);
            }
        }
        return value1.equals(value2);
    }

    public static boolean isContaining(Object value, String s, boolean ignoreCase) {
        if (s == null) {
            return true;
        }
        if (value == null) {
            return false;
        }
        if (value.getClass().isArray()) {
            int vm1 = Array.getLength(value);
            for (int i = 0; i < vm1; ++i) {
                Object o1 = Array.get(value, i);
                if (o1 == null) continue;
                if (ignoreCase && o1.toString().toLowerCase().contains(s.toLowerCase())) {
                    return true;
                }
                if (!o1.toString().contains(s)) continue;
                return true;
            }
            return false;
        }
        if (ignoreCase) {
            return value.toString().toLowerCase().contains(s.toLowerCase());
        }
        return value.toString().contains(s);
    }
}

