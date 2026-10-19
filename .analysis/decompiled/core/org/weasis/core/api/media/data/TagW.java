/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.api.media.data;

import java.awt.Color;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAccessor;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.Messages;
import org.weasis.core.api.media.data.AttributeSource;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagUtil;
import org.weasis.core.api.media.data.Taggable;
import org.weasis.core.api.media.data.Thumbnailable;
import org.weasis.core.util.StringUtil;

public class TagW {
    private static final Logger LOGGER = LoggerFactory.getLogger(TagW.class);
    private static final AtomicInteger idCounter = new AtomicInteger(Integer.MAX_VALUE);
    protected static final Map<String, TagW> tags = Collections.synchronizedMap(new HashMap());
    public static final String NO_VALUE = "UNKNOWN";
    public static final TagW UnknownTag = new TagW(0, "UnknownTag", "Unknown Tag", TagType.STRING);
    public static final TagW Group = new TagW("Group", Messages.getString("TagW.group"), TagType.STRING);
    public static final TagW PatientPseudoUID = new TagW("PatientPseudoUID", Messages.getString("TagElement.pat_uid"), TagType.STRING);
    public static final TagW SeriesLoading = new TagW("SeriesLoading", Messages.getString("TagElement.laod"), TagType.INTEGER);
    public static final TagW Thumbnail = new TagW("Thumbnail", Messages.getString("TagElement.thumb"), TagType.THUMBNAIL);
    public static final TagW ThumbnailPath = new TagW("ThumbnailPath", TagType.STRING);
    public static final TagW ExplorerModel = new TagW("ExplorerModel", Messages.getString("TagElement.exp_model"), TagType.OBJECT);
    public static final TagW PresentationModel = new TagW("PesentationModel", TagType.OBJECT);
    public static final TagW PresentationModelBirary = new TagW("PesentationModelBinary", TagType.BYTE);
    public static final TagW SplitSeriesNumber = new TagW("SplitSeriesNumber", Messages.getString("TagElement.split_no"), TagType.INTEGER);
    public static final TagW SeriesSelected = new TagW("SeriesSelected", Messages.getString("TagElement.select"), TagType.BOOLEAN);
    public static final TagW SeriesOpen = new TagW("SeriesOpen", Messages.getString("TagElement.open"), TagType.BOOLEAN);
    public static final TagW SeriesFocused = new TagW("SeriesFocused", TagType.BOOLEAN);
    public static final TagW ImageWidth = new TagW("ImageWidth", Messages.getString("TagElement.img_w"), TagType.INTEGER);
    public static final TagW ImageHeight = new TagW("ImageHeight", Messages.getString("TagElement.img_h"), TagType.INTEGER);
    public static final TagW ImageDepth = new TagW("ImageDepth", Messages.getString("TagElement.img_d"), TagType.INTEGER);
    public static final TagW ImageOrientationPlane = new TagW("ImageOrientationPlane", Messages.getString("TagElement.img_or"), TagType.STRING);
    public static final TagW ImageBitsPerPixel = new TagW("ImageBitsPerPixel", Messages.getString("TagElement.img_bpp"), TagType.INTEGER);
    public static final TagW ImageCache = new TagW("ImageCache", TagType.BOOLEAN);
    public static final TagW ShutterFinalShape = new TagW("ShutterFinalShape", TagType.OBJECT);
    public static final TagW ShutterRGBColor = new TagW("ShutterRGBColor", TagType.COLOR);
    public static final TagW ShutterPSValue = new TagW("ShutterPSValue", TagType.INTEGER);
    public static final TagW ImageDescriptor = new TagW("ImageDescriptor", TagType.OBJECT);
    public static final TagW ObjectToSave = new TagW("ObjectToSave", TagType.BOOLEAN);
    public static final TagW stepNDimensions = new TagW("StepNDimension", TagType.INTEGER);
    public static final TagW WadoCompressionRate = new TagW("WadoCompressionRate", TagType.INTEGER);
    public static final TagW WadoTransferSyntaxUID = new TagW("WadoTransferSyntaxUID", TagType.STRING);
    public static final TagW DirectDownloadFile = new TagW("DirectDownloadFile", TagType.STRING);
    public static final TagW DirectDownloadThumbnail = new TagW("DirectDownloadThumbnail", TagType.STRING);
    public static final TagW ReadFromDicomdir = new TagW("ReadFromDicomdir", TagType.BOOLEAN);
    public static final TagW WadoParameters = new TagW("WadoParameters", TagType.OBJECT);
    public static final TagW WadoInstanceReferenceList = new TagW("WadoInstanceReferenceList", TagType.LIST);
    public static final TagW DicomSpecialElementList = new TagW("DicomSpecialElementList", TagType.LIST);
    public static final TagW SlicePosition = new TagW("SlicePosition", TagType.DOUBLE);
    public static final TagW SuvFactor = new TagW("SUVFactor", TagType.DOUBLE);
    public static final TagW RootElement = new TagW("RootElement", TagType.STRING);
    public static final TagW FilePath = new TagW("FilePath", TagType.STRING);
    public static final TagW FileName = new TagW("FileName", TagType.STRING);
    public static final TagW CurrentFolder = new TagW("CurrentFolder", Messages.getString("TagElement.cur_dir"), TagType.STRING);
    public static final TagW Checked = new TagW("Checked", TagType.BOOLEAN);
    public static final TagW SubseriesInstanceUID = new TagW("SubseriesInstanceUID", TagType.STRING);
    public static final TagW PRLUTsExplanation = new TagW("PRLUTsExplanation", TagType.STRING);
    public static final TagW PrDicomObject = new TagW("PrDicomObject", TagType.OBJECT);
    public static final TagW MonoChrome = new TagW("MonoChrome", TagType.BOOLEAN);
    public static TagW AnatomicRegion = new TagW("AnatomicRegion", TagType.OBJECT);
    public static final TagW Timezone = new TagW("Timezone", TagType.OBJECT);
    public static final TagW ExifImageDescription = new TagW("ExifImageDescription", TagType.STRING);
    public static final TagW ExifMake = new TagW("ExifMake", TagType.STRING);
    public static final TagW ExifModel = new TagW("ExifModel", TagType.STRING);
    public static final TagW ExifOrientation = new TagW("ExifOrientation", TagType.STRING);
    public static final TagW ExifXResolution = new TagW("ExifXResolution", TagType.STRING);
    public static final TagW ExifYResolution = new TagW("ExifYResolution", TagType.STRING);
    public static final TagW ExifResolutionUnit = new TagW("ExifResolutionUnit", TagType.STRING);
    public static final TagW ExifSoftware = new TagW("ExifSoftware", TagType.STRING);
    public static final TagW ExifDateTime = new TagW("ExifDateTime", TagType.STRING);
    public static final TagW ExifCopyright = new TagW("ExifCopyright", TagType.STRING);
    protected final int id;
    protected final String keyword;
    protected final String displayedName;
    protected final TagType type;
    protected int anonymizationType;
    protected final int vmMin;
    protected final int vmMax;
    protected final transient Object defaultValue;

    public TagW(int id, String keyword, String displayedName, TagType type, int vmMin, int vmMax, Object defaultValue) {
        this.id = id;
        this.keyword = keyword;
        this.displayedName = displayedName;
        this.type = type == null ? TagType.STRING : type;
        this.anonymizationType = 0;
        this.defaultValue = defaultValue;
        this.vmMax = Math.max(vmMax, 1);
        this.vmMin = Math.max(vmMin, 1);
        if (!this.isTypeCompliant(defaultValue)) {
            throw new IllegalArgumentException("defaultValue is not compliant to the tag type");
        }
    }

    public TagW(int id, String keyword, TagType type, int vmMin, int vmMax) {
        this(id, keyword, null, type, vmMin, vmMax, null);
    }

    public TagW(int id, String keyword, String displayedName, TagType type) {
        this(id, keyword, displayedName, type, 1, 1, null);
    }

    public TagW(int id, String keyword, TagType type) {
        this(id, keyword, null, type, 1, 1, null);
    }

    public TagW(String name, TagType type, int vmMin, int vmMax) {
        this(idCounter.getAndDecrement(), name, null, type, vmMin, vmMax, null);
    }

    public TagW(String keyword, String displayedName, TagType type) {
        this(idCounter.getAndDecrement(), keyword, displayedName, type, 1, 1, null);
    }

    public TagW(String keyword, TagType type) {
        this(idCounter.getAndDecrement(), keyword, null, type);
    }

    public int getId() {
        return this.id;
    }

    public String getKeyword() {
        return this.keyword;
    }

    public String getDisplayedName() {
        if (this.displayedName == null) {
            return StringUtil.splitCamelCaseString((String)this.getKeyword());
        }
        return this.displayedName;
    }

    public TagType getType() {
        return this.type;
    }

    public int getValueMultiplicity() {
        return this.vmMax;
    }

    public boolean isTypeCompliant(Object value) {
        if (value == null) {
            return true;
        }
        if (value.getClass().isArray()) {
            if (this.vmMax == 1) {
                return false;
            }
            Class<?> clazz = value.getClass().getComponentType();
            int vmValue = Array.getLength(value);
            if (this.vmMax != Integer.MAX_VALUE && this.vmMax != vmValue) {
                return false;
            }
            return this.type.getClazz().isAssignableFrom(clazz);
        }
        Object clazz = value;
        return this.type.isInstanceOf(clazz);
    }

    public static int getValueMultiplicity(Object value) {
        if (value == null) {
            return 0;
        }
        if (value.getClass().isArray()) {
            return Array.getLength(value);
        }
        return 1;
    }

    public static Object getValueFromIndex(Object value, int index) {
        if (value == null || !value.getClass().isArray()) {
            return value;
        }
        if (index >= 0 && index < Array.getLength(value)) {
            return Array.get(value, index);
        }
        return null;
    }

    public String toString() {
        return this.getDisplayedName();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        TagW other = (TagW)obj;
        if (this.id != other.id) {
            return false;
        }
        if (this.keyword == null) {
            return other.keyword == null;
        }
        return this.keyword.equals(other.keyword);
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + this.id;
        result = 31 * result + (this.keyword == null ? 0 : this.keyword.hashCode());
        return result;
    }

    public void readValue(Object data, Taggable taggable) {
        taggable.setTagNoNull(this, this.getValue(data));
    }

    public Object getValue(Object data) {
        if (data instanceof AttributeSource) {
            AttributeSource source = (AttributeSource)data;
            return this.getValue(source);
        }
        return null;
    }

    protected Object getValue(AttributeSource source) {
        Object value = this.isStringFamilyType() ? (this.vmMax > 1 ? TagUtil.getStringArrayTagAttribute(source, this.keyword, (String[])this.defaultValue) : TagUtil.getTagAttribute(source, this.keyword, (String)this.defaultValue)) : (TagType.DATE.equals((Object)this.type) || TagType.TIME.equals((Object)this.type) || TagType.DATETIME.equals((Object)this.type) ? (this.vmMax > 1 ? TagUtil.getDatesFromElement(source, this.keyword, this.type, (TemporalAccessor[])this.defaultValue) : TagUtil.getDateFromElement(source, this.keyword, this.type, (TemporalAccessor)this.defaultValue)) : (TagType.INTEGER.equals((Object)this.type) ? (Object)(this.vmMax > 1 ? TagUtil.getIntArrayTagAttribute(source, this.keyword, (int[])this.defaultValue) : (int[])TagUtil.getIntegerTagAttribute(source, this.keyword, (Integer)this.defaultValue)) : (TagType.FLOAT.equals((Object)this.type) ? (Object)(this.vmMax > 1 ? TagUtil.getFloatArrayTagAttribute(source, this.keyword, (float[])this.defaultValue) : (float[])TagUtil.getFloatTagAttribute(source, this.keyword, (Float)this.defaultValue)) : (TagType.DOUBLE.equals((Object)this.type) ? (Object)(this.vmMax > 1 ? TagUtil.getDoubleArrayTagAttribute(source, this.keyword, (double[])this.defaultValue) : (double[])TagUtil.getDoubleTagAttribute(source, this.keyword, (Double)this.defaultValue)) : (this.vmMax > 1 ? TagUtil.getStringArrayTagAttribute(source, this.keyword, (String[])this.defaultValue) : TagUtil.getTagAttribute(source, this.keyword, (String)this.defaultValue))))));
        return value;
    }

    public boolean isStringFamilyType() {
        return TagType.STRING.equals((Object)this.type) || TagType.TEXT.equals((Object)this.type) || TagType.URI.equals((Object)this.type);
    }

    public synchronized int getAnonymizationType() {
        return this.anonymizationType;
    }

    public synchronized void setAnonymizationType(int anonymizationType) {
        this.anonymizationType = anonymizationType;
    }

    public String addGMTOffset(String format, TagReadable readable) {
        TimeZone timeZone;
        if (TagType.DICOM_TIME.equals((Object)this.type) && (timeZone = TagW.getTagValue(readable, Timezone, TimeZone.class)) != null) {
            String offset = timeZone.getDisplayName(true, 0);
            if (StringUtil.hasText((CharSequence)format)) {
                return format + " " + offset;
            }
            return "$V " + offset;
        }
        return format;
    }

    public String getFormattedTagValue(Object value, String format) {
        return TagW.getFormattedText(value, format);
    }

    protected static String getFormattedText(Object value, String format) {
        String str;
        if (value == null) {
            return "";
        }
        if (value instanceof String) {
            String val;
            str = val = (String)value;
        } else if (value instanceof String[]) {
            CharSequence[] stringArray = (String[])value;
            str = String.join((CharSequence)"\\", stringArray);
        } else if (value instanceof TemporalAccessor) {
            TemporalAccessor temporalAccessor = (TemporalAccessor)value;
            str = TagUtil.formatDateTime(temporalAccessor);
        } else if (value instanceof TemporalAccessor[]) {
            TemporalAccessor[] temporalArray = (TemporalAccessor[])value;
            str = Stream.of(temporalArray).map(TagUtil::formatDateTime).collect(Collectors.joining(", "));
        } else if (value instanceof float[]) {
            float[] floats = (float[])value;
            str = IntStream.range(0, floats.length).mapToObj(i -> String.valueOf(floats[i])).collect(Collectors.joining(", "));
        } else if (value instanceof double[]) {
            double[] doubles = (double[])value;
            str = DoubleStream.of(doubles).mapToObj(String::valueOf).collect(Collectors.joining(", "));
        } else if (value instanceof int[]) {
            int[] ints = (int[])value;
            str = IntStream.of(ints).mapToObj(String::valueOf).collect(Collectors.joining(", "));
        } else {
            str = value.toString();
        }
        if (StringUtil.hasText((CharSequence)format) && !"$V".equals(format.trim())) {
            return TagW.formatValue(str, value instanceof Float || value instanceof Double, format);
        }
        return str == null ? "" : str;
    }

    protected static String formatValue(String value, boolean decimal, String format) {
        Object str = value;
        int index = format.indexOf("$V");
        int fmLength = 2;
        if (index != -1) {
            boolean suffix;
            boolean bl = suffix = format.length() > index + fmLength;
            if (suffix && format.charAt(index + fmLength) == ':') {
                String pattern;
                if (format.charAt(index + ++fmLength) == 'f' && decimal) {
                    String pattern2;
                    if ((pattern2 = TagW.getPattern(index + ++fmLength, format)) != null) {
                        fmLength += pattern2.length() + 2;
                        try {
                            str = new DecimalFormat(pattern2).format(Double.parseDouble((String)str));
                        }
                        catch (NumberFormatException e) {
                            LOGGER.warn("Cannot apply pattern to decimal value", (Throwable)e);
                        }
                    }
                } else if (format.charAt(index + fmLength) == 'l' && (pattern = TagW.getPattern(index + ++fmLength, format)) != null) {
                    fmLength += pattern.length() + 2;
                    try {
                        int limit = Integer.parseInt(pattern);
                        int size = ((String)str).length();
                        if (size > limit) {
                            str = ((String)str).substring(0, limit) + "...";
                        }
                    }
                    catch (NumberFormatException e) {
                        LOGGER.warn("Cannot apply pattern to decimal value", (Throwable)e);
                    }
                }
            }
            str = format.substring(0, index) + (String)str;
            if (format.length() > index + fmLength) {
                str = (String)str + format.substring(index + fmLength);
            }
        }
        return str;
    }

    private static String getPattern(int startIndex, String format) {
        int beginIndex = format.indexOf(36, startIndex);
        int endIndex = format.indexOf(36, startIndex + 2);
        if (beginIndex == -1 || endIndex == -1) {
            return null;
        }
        return format.substring(beginIndex + 1, endIndex);
    }

    public static void addTag(TagW tag) {
        if (tag != null) {
            tags.put(tag.getKeyword(), tag);
        }
    }

    public static TagW get(String keyword) {
        return tags.get(keyword);
    }

    public static <T> T getTagValue(TagReadable taggable, TagW tag, Class<T> type) {
        if (taggable != null && tag != null) {
            try {
                return type.cast(taggable.getTagValue(tag));
            }
            catch (ClassCastException e) {
                LOGGER.error("Cannot cast the value of \"{}\" into {}", new Object[]{tag.getKeyword(), type, e});
            }
        }
        return null;
    }

    static {
        TagW.addTag(ImageBitsPerPixel);
        TagW.addTag(ImageCache);
        TagW.addTag(ImageDepth);
        TagW.addTag(ImageHeight);
        TagW.addTag(ImageOrientationPlane);
        TagW.addTag(ImageWidth);
        TagW.addTag(SeriesFocused);
        TagW.addTag(SeriesLoading);
        TagW.addTag(SeriesOpen);
        TagW.addTag(SeriesSelected);
        TagW.addTag(SlicePosition);
        TagW.addTag(SuvFactor);
        TagW.addTag(DirectDownloadFile);
        TagW.addTag(DirectDownloadThumbnail);
        TagW.addTag(RootElement);
        TagW.addTag(FileName);
        TagW.addTag(FilePath);
        TagW.addTag(CurrentFolder);
        TagW.addTag(SubseriesInstanceUID);
        TagW.addTag(PRLUTsExplanation);
        TagW.addTag(PrDicomObject);
        TagW.addTag(MonoChrome);
        TagW.addTag(AnatomicRegion);
    }

    public static enum TagType {
        STRING(String.class),
        TEXT(String.class),
        URI(String.class),
        DATE(LocalDate.class),
        DATETIME(LocalDateTime.class),
        TIME(LocalTime.class),
        BOOLEAN(Boolean.class),
        BYTE(Byte.class),
        INTEGER(Integer.class),
        FLOAT(Float.class),
        DOUBLE(Double.class),
        COLOR(Color.class),
        THUMBNAIL(Thumbnailable.class),
        LIST(List.class),
        OBJECT(Object.class),
        DICOM_DATE(LocalDate.class),
        DICOM_DATETIME(LocalDateTime.class),
        DICOM_TIME(LocalTime.class),
        DICOM_PERIOD(String.class),
        DICOM_PERSON_NAME(String.class),
        DICOM_SEQUENCE(Object.class),
        DICOM_SEX(String.class);

        private final Class<?> clazz;

        private TagType(Class<?> clazz) {
            this.clazz = clazz;
        }

        public Class<?> getClazz() {
            return this.clazz;
        }

        public boolean isInstanceOf(Object value) {
            if (value == null) {
                return true;
            }
            return this.clazz.isAssignableFrom(value.getClass());
        }
    }
}
