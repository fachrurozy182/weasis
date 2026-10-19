/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StringUtil
 */
package org.dcm4che3.img.util;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.img.util.DateTimeUtils;
import org.dcm4che3.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.StringUtil;

public final class DicomUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomUtils.class);
    private static final Set<String> VIDEO_TRANSFER_SYNTAXES = Set.of("1.2.840.10008.1.2.4.100", "1.2.840.10008.1.2.4.100.1", "1.2.840.10008.1.2.4.101", "1.2.840.10008.1.2.4.101.1", "1.2.840.10008.1.2.4.102", "1.2.840.10008.1.2.4.102.1", "1.2.840.10008.1.2.4.103", "1.2.840.10008.1.2.4.103.1", "1.2.840.10008.1.2.4.104", "1.2.840.10008.1.2.4.104.1", "1.2.840.10008.1.2.4.105", "1.2.840.10008.1.2.4.105.1", "1.2.840.10008.1.2.4.106", "1.2.840.10008.1.2.4.106.1", "1.2.840.10008.1.2.4.107", "1.2.840.10008.1.2.4.108");
    private static final Set<String> JPEG2000_TRANSFER_SYNTAXES = Set.of("1.2.840.10008.1.2.4.90", "1.2.840.10008.1.2.4.91", "1.2.840.10008.1.2.4.92", "1.2.840.10008.1.2.4.93", "1.2.840.10008.1.2.4.201", "1.2.840.10008.1.2.4.202", "1.2.840.10008.1.2.4.203");
    private static final Set<String> NATIVE_TRANSFER_SYNTAXES = Set.of("1.2.840.10008.1.2", "1.2.840.10008.1.2.1", "1.2.840.10008.1.2.2");
    private static final int[] STUDY_DATE_TAGS = new int[]{524323, 524322, 1576978, 524321, 524320};

    private DicomUtils() {
    }

    public static boolean isValidUID(String uid) {
        if (!StringUtil.hasText((CharSequence)uid) || uid.length() > 64) {
            return false;
        }
        int len = uid.length();
        if (uid.charAt(0) == '.' || uid.charAt(len - 1) == '.') {
            return false;
        }
        boolean prevDot = false;
        for (int i = 0; i < len; ++i) {
            char c = uid.charAt(i);
            if (c == '.') {
                if (prevDot) {
                    return false;
                }
                prevDot = true;
                continue;
            }
            if (c >= '0' && c <= '9') {
                prevDot = false;
                continue;
            }
            return false;
        }
        return true;
    }

    public static boolean isVideo(String uid) {
        return VIDEO_TRANSFER_SYNTAXES.contains(uid);
    }

    public static boolean isJpegXL(String uid) {
        return switch (uid) {
            case "1.2.840.10008.1.2.4.90", "1.2.840.10008.1.2.4.111", "1.2.840.10008.1.2.4.112" -> true;
            default -> false;
        };
    }

    public static boolean isJpeg2000(String uid) {
        return JPEG2000_TRANSFER_SYNTAXES.contains(uid);
    }

    public static boolean isNative(String uid) {
        return NATIVE_TRANSFER_SYNTAXES.contains(uid);
    }

    public static String getFormattedText(Object value, String format) {
        return DicomUtils.getFormattedText(value, format, Locale.getDefault());
    }

    public static String getFormattedText(Object value, String format, Locale locale) {
        if (value == null) {
            return "";
        }
        String stringValue = DicomUtils.convertValueToString(value, locale);
        if (StringUtil.hasText((CharSequence)format) && !"$V".equals(format.trim())) {
            return DicomUtils.formatValue(stringValue, DicomUtils.isDecimalType(value), format);
        }
        return Objects.requireNonNullElse(stringValue, "");
    }

    private static String convertValueToString(Object value, Locale locale) {
        if (value instanceof String) {
            String string = (String)value;
            return string;
        }
        if (value instanceof String[]) {
            CharSequence[] strings = (String[])value;
            return String.join((CharSequence)"\\", strings);
        }
        if (value instanceof TemporalAccessor) {
            TemporalAccessor temporal = (TemporalAccessor)value;
            return DateTimeUtils.formatDateTime(temporal, locale);
        }
        if (value instanceof TemporalAccessor[]) {
            TemporalAccessor[] temporals = (TemporalAccessor[])value;
            return Stream.of(temporals).map(v -> DateTimeUtils.formatDateTime(v, locale)).collect(Collectors.joining(", "));
        }
        if (value instanceof float[]) {
            float[] array = (float[])value;
            return IntStream.range(0, array.length).mapToObj(i -> String.valueOf(array[i])).collect(Collectors.joining(", "));
        }
        if (value instanceof double[]) {
            double[] array = (double[])value;
            return DoubleStream.of(array).mapToObj(String::valueOf).collect(Collectors.joining(", "));
        }
        if (value instanceof int[]) {
            int[] array = (int[])value;
            return IntStream.of(array).mapToObj(String::valueOf).collect(Collectors.joining(", "));
        }
        return value.toString();
    }

    private static boolean isDecimalType(Object value) {
        return value instanceof Double || value instanceof Float;
    }

    static String formatValue(String value, boolean decimal, String format) {
        List<Placeholder> placeholders = PlaceholderParser.parseFormat(format);
        if (placeholders.isEmpty()) {
            return value;
        }
        String result = format;
        for (Placeholder placeholder : placeholders) {
            String processedValue = placeholder.process(value, decimal);
            result = result.replace(placeholder.fullMatch(), processedValue);
        }
        return result;
    }

    public static String getStringFromDicomElement(Attributes dicom, int tag) {
        if (dicom == null || !dicom.containsValue(tag)) {
            return null;
        }
        CharSequence[] strings = dicom.getStrings(tag);
        if (strings == null || strings.length == 0) {
            return null;
        }
        return strings.length == 1 ? strings[0] : String.join((CharSequence)"\\", strings);
    }

    public static String[] getStringArrayFromDicomElement(Attributes dicom, int tag) {
        return DicomUtils.getStringArrayFromDicomElement(dicom, tag, (String)null);
    }

    public static String[] getStringArrayFromDicomElement(Attributes dicom, int tag, String privateCreatorID) {
        if (dicom == null || !dicom.containsValue(tag)) {
            return null;
        }
        return dicom.getStrings(privateCreatorID, tag);
    }

    public static String[] getStringArrayFromDicomElement(Attributes dicom, int tag, String[] defaultValue) {
        return DicomUtils.getStringArrayFromDicomElement(dicom, tag, null, defaultValue);
    }

    public static String[] getStringArrayFromDicomElement(Attributes dicom, int tag, String privateCreatorID, String[] defaultValue) {
        if (dicom == null || !dicom.containsValue(tag)) {
            return defaultValue;
        }
        String[] values = dicom.getStrings(privateCreatorID, tag);
        return values == null || values.length == 0 ? defaultValue : values;
    }

    public static Date getDateFromDicomElement(Attributes dicom, int tag, Date defaultValue) {
        if (dicom == null || !dicom.containsValue(tag)) {
            return defaultValue;
        }
        return dicom.getDate(tag, defaultValue);
    }

    public static Date[] getDatesFromDicomElement(Attributes dicom, int tag, String privateCreatorID, Date[] defaultValue) {
        if (dicom == null || !dicom.containsValue(tag)) {
            return defaultValue;
        }
        Date[] values = dicom.getDates(privateCreatorID, tag);
        return values == null || values.length == 0 ? defaultValue : values;
    }

    public static String getPatientAgeInPeriod(Attributes dicom, int tag, boolean computeOnlyIfNull) {
        return DicomUtils.getPatientAgeInPeriod(dicom, tag, null, null, computeOnlyIfNull);
    }

    public static String getPatientAgeInPeriod(Attributes dicom, int tag, String privateCreatorID, String defaultValue, boolean computeOnlyIfNull) {
        String existingAge;
        if (dicom == null) {
            return defaultValue;
        }
        if (computeOnlyIfNull && StringUtil.hasText((CharSequence)(existingAge = dicom.getString(privateCreatorID, tag, defaultValue)))) {
            return existingAge;
        }
        return DicomUtils.calculateAgeFromDates(dicom);
    }

    private static String calculateAgeFromDates(Attributes dicom) {
        Date studyDate = DicomUtils.findFirstAvailableDate(dicom, STUDY_DATE_TAGS);
        if (studyDate == null) {
            return null;
        }
        Date birthDate = dicom.getDate(0x100030);
        if (birthDate == null) {
            return null;
        }
        return DicomUtils.getPeriod(DateTimeUtils.toLocalDate(birthDate), DateTimeUtils.toLocalDate(studyDate));
    }

    private static Date findFirstAvailableDate(Attributes dicom, int ... tagIds) {
        for (int tagId : tagIds) {
            Date date = dicom.getDate(tagId);
            if (date == null) continue;
            return date;
        }
        return null;
    }

    public static String getPeriod(LocalDate first, LocalDate last) {
        Objects.requireNonNull(first, "First date cannot be null");
        Objects.requireNonNull(last, "Last date cannot be null");
        long years = ChronoUnit.YEARS.between(first, last);
        if (years >= 2L) {
            return "%03dY".formatted(new Object[]{years});
        }
        long months = ChronoUnit.MONTHS.between(first, last);
        if (months >= 2L) {
            return "%03dM".formatted(new Object[]{months});
        }
        return "%03dD".formatted(new Object[]{ChronoUnit.DAYS.between(first, last)});
    }

    public static Float getFloatFromDicomElement(Attributes dicom, int tag, Float defaultValue) {
        return DicomUtils.getFloatFromDicomElement(dicom, tag, null, defaultValue);
    }

    public static Float getFloatFromDicomElement(Attributes dicom, int tag, String privateCreatorID, Float defaultValue) {
        if (dicom == null || !dicom.containsValue(tag)) {
            return defaultValue;
        }
        return DicomUtils.parseNumericValue(() -> Float.valueOf(dicom.getFloat(privateCreatorID, tag, defaultValue == null ? 0.0f : defaultValue.floatValue())), defaultValue, tag, "Float");
    }

    public static Integer getIntegerFromDicomElement(Attributes dicom, int tag, Integer defaultValue) {
        return DicomUtils.getIntegerFromDicomElement(dicom, tag, null, defaultValue);
    }

    public static Integer getIntegerFromDicomElement(Attributes dicom, int tag, String privateCreatorID, Integer defaultValue) {
        if (dicom == null || !dicom.containsValue(tag)) {
            return defaultValue;
        }
        return DicomUtils.parseNumericValue(() -> dicom.getInt(privateCreatorID, tag, defaultValue == null ? 0 : defaultValue), defaultValue, tag, "Integer");
    }

    public static Double getDoubleFromDicomElement(Attributes dicom, int tag, Double defaultValue) {
        return DicomUtils.getDoubleFromDicomElement(dicom, tag, null, defaultValue);
    }

    public static Double getDoubleFromDicomElement(Attributes dicom, int tag, String privateCreatorID, Double defaultValue) {
        if (dicom == null || !dicom.containsValue(tag)) {
            return defaultValue;
        }
        return DicomUtils.parseNumericValue(() -> dicom.getDouble(privateCreatorID, tag, defaultValue == null ? 0.0 : defaultValue), defaultValue, tag, "Double");
    }

    public static int[] getIntArrayFromDicomElement(Attributes dicom, int tag, int[] defaultValue) {
        return DicomUtils.getIntArrayFromDicomElement(dicom, tag, null, defaultValue);
    }

    public static int[] getIntArrayFromDicomElement(Attributes dicom, int tag, String privateCreatorID, int[] defaultValue) {
        if (dicom == null || !dicom.containsValue(tag)) {
            return defaultValue;
        }
        return DicomUtils.parseArrayValue(() -> dicom.getInts(privateCreatorID, tag), defaultValue, tag, "int[]");
    }

    public static float[] getFloatArrayFromDicomElement(Attributes dicom, int tag, float[] defaultValue) {
        return DicomUtils.getFloatArrayFromDicomElement(dicom, tag, null, defaultValue);
    }

    public static float[] getFloatArrayFromDicomElement(Attributes dicom, int tag, String privateCreatorID, float[] defaultValue) {
        if (dicom == null || !dicom.containsValue(tag)) {
            return defaultValue;
        }
        return DicomUtils.parseArrayValue(() -> dicom.getFloats(privateCreatorID, tag), defaultValue, tag, "float[]");
    }

    public static double[] getDoubleArrayFromDicomElement(Attributes dicom, int tag, double[] defaultValue) {
        return DicomUtils.getDoubleArrayFromDicomElement(dicom, tag, null, defaultValue);
    }

    public static double[] getDoubleArrayFromDicomElement(Attributes dicom, int tag, String privateCreatorID, double[] defaultValue) {
        if (dicom == null || !dicom.containsValue(tag)) {
            return defaultValue;
        }
        return DicomUtils.parseArrayValue(() -> dicom.getDoubles(privateCreatorID, tag), defaultValue, tag, "double[]");
    }

    private static <T> T parseNumericValue(NumericValueSupplier<T> supplier, T defaultValue, int tag, String type) {
        try {
            return supplier.get();
        }
        catch (NumberFormatException e) {
            LOGGER.error("Cannot parse {} for tag {}: {}", new Object[]{type, TagUtils.toString(tag), e.getMessage()});
            return defaultValue;
        }
    }

    private static <T> T parseArrayValue(ArrayValueSupplier<T> supplier, T defaultValue, int tag, String type) {
        try {
            T value = supplier.get();
            return value != null && Array.getLength(value) != 0 ? value : defaultValue;
        }
        catch (NumberFormatException e) {
            LOGGER.error("Cannot parse {} for tag {}: {}", new Object[]{type, TagUtils.toString(tag), e.getMessage()});
            return defaultValue;
        }
    }

    private static final class PlaceholderParser {
        private static final String PLACEHOLDER_PATTERN = "\\$V(?::([fl])\\$([^$]*)\\$)?";
        private static final Pattern PATTERN = Pattern.compile("\\$V(?::([fl])\\$([^$]*)\\$)?");

        private PlaceholderParser() {
        }

        static List<Placeholder> parseFormat(String format) {
            ArrayList<Placeholder> placeholders = new ArrayList<Placeholder>();
            Matcher matcher = PATTERN.matcher(format);
            while (matcher.find()) {
                String fullMatch = matcher.group(0);
                String specifier = matcher.group(1);
                String parameter = matcher.group(2);
                PlaceholderType type = PlaceholderParser.determineType(specifier);
                placeholders.add(new Placeholder(type, parameter, fullMatch));
            }
            return placeholders;
        }

        private static PlaceholderType determineType(String specifier) {
            if (specifier == null) {
                return PlaceholderType.SIMPLE;
            }
            return switch (specifier) {
                case "f" -> PlaceholderType.DECIMAL_FORMAT;
                case "l" -> PlaceholderType.LENGTH_LIMIT;
                default -> PlaceholderType.SIMPLE;
            };
        }
    }

    private record Placeholder(PlaceholderType type, String parameter, String fullMatch) {
        String process(String value, boolean isDecimal) {
            return this.type.process(value, this.parameter, isDecimal);
        }
    }

    @FunctionalInterface
    private static interface NumericValueSupplier<T> {
        public T get() throws NumberFormatException;
    }

    @FunctionalInterface
    private static interface ArrayValueSupplier<T> {
        public T get() throws NumberFormatException;
    }

    /*
     * Uses 'sealed' constructs - enablewith --sealed true
     */
    private static enum PlaceholderType {
        SIMPLE{

            @Override
            String process(String value, String parameter, boolean isDecimal) {
                return value;
            }
        }
        ,
        DECIMAL_FORMAT{

            @Override
            String process(String value, String parameter, boolean isDecimal) {
                if (!isDecimal || parameter == null) {
                    return value;
                }
                try {
                    DecimalFormat formatter = new DecimalFormat(parameter, DecimalFormatSymbols.getInstance());
                    return formatter.format(Double.parseDouble(value));
                }
                catch (NumberFormatException e) {
                    LOGGER.warn("Cannot apply decimal pattern '{}' to value '{}'", (Object)parameter, (Object)value);
                    return value;
                }
            }
        }
        ,
        LENGTH_LIMIT{

            @Override
            String process(String value, String parameter, boolean isDecimal) {
                if (parameter == null) {
                    return value;
                }
                try {
                    int limit = Integer.parseInt(parameter);
                    return value.length() > limit ? value.substring(0, limit) + "..." : value;
                }
                catch (NumberFormatException e) {
                    LOGGER.warn("Cannot parse length limit '{}' for value '{}'", (Object)parameter, (Object)value);
                    return value;
                }
            }
        };


        abstract String process(String var1, String var2, boolean var3);
    }
}

