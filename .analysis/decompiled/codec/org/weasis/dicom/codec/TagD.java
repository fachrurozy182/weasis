/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.Messages
 *  org.weasis.core.api.media.data.AttributeSource
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagUtil
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.media.data.TagW$TagType
 *  org.weasis.core.util.LangUtil
 *  org.weasis.core.util.StreamUtil
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.codec;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.VR;
import org.dcm4che3.img.util.DateTimeUtils;
import org.dcm4che3.img.util.DicomUtils;
import org.dcm4che3.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.Messages;
import org.weasis.core.api.media.data.AttributeSource;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagUtil;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.util.LangUtil;
import org.weasis.core.util.StreamUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.TagSeq;
import org.weasis.dicom.codec.utils.DicomMediaUtils;

public class TagD
extends TagW {
    private static final Logger LOGGER = LoggerFactory.getLogger(TagD.class);
    private static final Map<VR, Integer> vrToMaxChars = new HashMap<VR, Integer>();
    protected final VR vr;
    protected final String privateCreatorID;
    protected final boolean retired;

    public TagD(int tagID) {
        this(tagID, null, null, ElementDictionary.vrOf(tagID, null), 1, 1, null);
    }

    public TagD(int tagID, int vmMax) {
        this(tagID, null, null, ElementDictionary.vrOf(tagID, null), vmMax, vmMax, null);
    }

    public TagD(int tagID, int vmMin, int vmMax) {
        this(tagID, null, null, ElementDictionary.vrOf(tagID, null), vmMin, vmMax, null);
    }

    public TagD(int tagID, int vmMin, int vmMax, Object defaultValue) {
        this(tagID, null, null, ElementDictionary.vrOf(tagID, null), vmMin, vmMax, defaultValue);
    }

    public TagD(int tagID, String privateCreatorID) {
        this(tagID, null, privateCreatorID, ElementDictionary.vrOf(tagID, privateCreatorID), 1, 1, null);
    }

    public TagD(int tagID, String privateCreatorID, int vmMin, int vmMax, Object defaultValue) {
        this(tagID, null, privateCreatorID, ElementDictionary.vrOf(tagID, privateCreatorID), vmMin, vmMax, defaultValue);
    }

    public TagD(int tagID, String displayedName, String privateCreatorID, int vmMin, int vmMax, Object defaultValue) {
        this(tagID, displayedName, privateCreatorID, ElementDictionary.vrOf(tagID, privateCreatorID), vmMin, vmMax, defaultValue);
    }

    public TagD(int tagID, String keyword, String displayedName, String privateCreatorID, VR vr, int vmMin, int vmMax, Object defaultValue, boolean retired) {
        super(tagID, keyword, displayedName, TagD.getTypeFromTag(tagID, vr), vmMin, vmMax, defaultValue);
        this.vr = vr;
        this.privateCreatorID = privateCreatorID;
        this.retired = retired;
    }

    private TagD(int tagID, String displayedName, String privateCreatorID, VR vr, int vmMin, int vmMax, Object defaultValue) {
        super(tagID, TagD.getKeywordFromTag(tagID, privateCreatorID), displayedName, TagD.getTypeFromTag(tagID, vr), vmMin, vmMax, defaultValue);
        this.vr = vr;
        this.privateCreatorID = privateCreatorID;
        this.retired = false;
    }

    public VR getValueRepresentation() {
        return this.vr;
    }

    public int getDicomValueMultiplicity(Object value) {
        if (value == null) {
            return 0;
        }
        try {
            return this.vr.vmOf(value);
        }
        catch (Exception e) {
            LOGGER.error("Cannot evaluate multiplicity from DICOM VR", (Throwable)e);
            return TagD.getValueMultiplicity((Object)value);
        }
    }

    public int getMaximumChars() {
        return vrToMaxChars.getOrDefault((Object)this.vr, 64);
    }

    public int hashCode() {
        int prime = 31;
        int result = super.hashCode();
        result = 31 * result + (this.privateCreatorID == null ? 0 : this.privateCreatorID.hashCode());
        result = 31 * result + (this.vr == null ? 0 : this.vr.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (((Object)((Object)this)).getClass() != obj.getClass()) {
            return false;
        }
        TagD other = (TagD)((Object)obj);
        if (this.privateCreatorID == null ? other.privateCreatorID != null : !this.privateCreatorID.equals(other.privateCreatorID)) {
            return false;
        }
        return this.vr == other.vr;
    }

    public Object getValue(Object data) {
        Object value = null;
        if (data instanceof Attributes) {
            Attributes attributes = (Attributes)data;
            value = this.readValue(attributes);
        } else if (data instanceof AttributeSource) {
            AttributeSource source = (AttributeSource)data;
            value = this.readValue(source);
        } else if (data instanceof String) {
            String s = (String)data;
            value = this.readValue(s);
        }
        return value;
    }

    private Object readValue(Attributes dataset) {
        Object value = this.isStringFamilyType() ? (this.vmMax > 1 ? DicomUtils.getStringArrayFromDicomElement(dataset, this.id, this.privateCreatorID, (String[])this.defaultValue) : dataset.getString(this.privateCreatorID, this.id, (String)this.defaultValue)) : (TagW.TagType.DICOM_DATE.equals((Object)this.type) || TagW.TagType.DICOM_TIME.equals((Object)this.type) || TagW.TagType.DICOM_DATETIME.equals((Object)this.type) ? (this.vmMax > 1 ? DicomMediaUtils.getDatesFromDicomElement(this.type, dataset, this.id, this.privateCreatorID, (TemporalAccessor[])this.defaultValue) : DicomMediaUtils.getDateFromDicomElement(this.type, dataset, this.id, this.privateCreatorID, (TemporalAccessor)this.defaultValue)) : (TagW.TagType.INTEGER.equals((Object)this.type) ? (Object)(this.vmMax > 1 ? DicomUtils.getIntArrayFromDicomElement(dataset, this.id, this.privateCreatorID, (int[])this.defaultValue) : (int[])DicomUtils.getIntegerFromDicomElement(dataset, this.id, this.privateCreatorID, (Integer)this.defaultValue)) : (TagW.TagType.FLOAT.equals((Object)this.type) ? (Object)(this.vmMax > 1 ? DicomUtils.getFloatArrayFromDicomElement(dataset, this.id, this.privateCreatorID, (float[])this.defaultValue) : (float[])DicomUtils.getFloatFromDicomElement(dataset, this.id, this.privateCreatorID, (Float)this.defaultValue)) : (TagW.TagType.DOUBLE.equals((Object)this.type) ? (Object)(this.vmMax > 1 ? DicomUtils.getDoubleArrayFromDicomElement(dataset, this.id, this.privateCreatorID, (double[])this.defaultValue) : (double[])DicomUtils.getDoubleFromDicomElement(dataset, this.id, this.privateCreatorID, (Double)this.defaultValue)) : (TagW.TagType.DICOM_SEQUENCE.equals((Object)this.type) ? dataset.getSequence(this.privateCreatorID, this.id) : (Object)dataset.getSafeBytes(this.privateCreatorID, this.id))))));
        return value;
    }

    private Object readValue(AttributeSource source) {
        Object value = this.isStringFamilyType() ? (this.vmMax > 1 ? TagUtil.getStringArrayTagAttribute((AttributeSource)source, (String)this.keyword, (String[])((String[])this.defaultValue)) : TagUtil.getTagAttribute((AttributeSource)source, (String)this.keyword, (String)((String)this.defaultValue))) : (TagW.TagType.DICOM_DATE.equals((Object)this.type) || TagW.TagType.DICOM_TIME.equals((Object)this.type) || TagW.TagType.DICOM_DATETIME.equals((Object)this.type) ? (this.vmMax > 1 ? TagD.getDatesFromElement(source, this.keyword, this.type, (TemporalAccessor[])this.defaultValue) : TagD.getDateFromElement(source, this.keyword, this.type, (TemporalAccessor)this.defaultValue)) : (TagW.TagType.INTEGER.equals((Object)this.type) ? (Object)(this.vmMax > 1 ? TagUtil.getIntArrayTagAttribute((AttributeSource)source, (String)this.keyword, (int[])((int[])this.defaultValue)) : (int[])TagUtil.getIntegerTagAttribute((AttributeSource)source, (String)this.keyword, (Integer)((Integer)this.defaultValue))) : (TagW.TagType.FLOAT.equals((Object)this.type) ? (Object)(this.vmMax > 1 ? TagUtil.getFloatArrayTagAttribute((AttributeSource)source, (String)this.keyword, (float[])((float[])this.defaultValue)) : (float[])TagUtil.getFloatTagAttribute((AttributeSource)source, (String)this.keyword, (Float)((Float)this.defaultValue))) : (TagW.TagType.DOUBLE.equals((Object)this.type) ? (Object)(this.vmMax > 1 ? TagUtil.getDoubleArrayTagAttribute((AttributeSource)source, (String)this.keyword, (double[])((double[])this.defaultValue)) : (double[])TagUtil.getDoubleTagAttribute((AttributeSource)source, (String)this.keyword, (Double)((Double)this.defaultValue))) : (TagW.TagType.DICOM_SEQUENCE.equals((Object)this.type) ? TagUtil.getTagAttribute((AttributeSource)source, (String)this.keyword, (String)((String)this.defaultValue)) : (this.vmMax > 1 ? TagUtil.getStringArrayTagAttribute((AttributeSource)source, (String)this.keyword, (String[])((String[])this.defaultValue)) : TagUtil.getTagAttribute((AttributeSource)source, (String)this.keyword, (String)((String)this.defaultValue))))))));
        return value;
    }

    private Object readValue(String data) {
        Object value;
        if (!StringUtil.hasText((CharSequence)data)) {
            return null;
        }
        if (this.isStringFamilyType()) {
            value = this.vmMax > 1 ? TagD.toStrings((String)data) : data;
        } else if (TagW.TagType.DICOM_DATE.equals((Object)this.type)) {
            if (this.vmMax > 1) {
                String[] ss = TagD.toStrings((String)data);
                LocalDate[] is = new LocalDate[ss.length];
                for (int i = 0; i < is.length; ++i) {
                    is[i] = TagD.getDicomDate((String)data);
                }
                value = is;
            } else {
                value = TagD.getDicomDate((String)data);
            }
        } else if (TagW.TagType.DICOM_TIME.equals((Object)this.type)) {
            if (this.vmMax > 1) {
                String[] ss = TagD.toStrings((String)data);
                LocalTime[] is = new LocalTime[ss.length];
                for (int i = 0; i < is.length; ++i) {
                    is[i] = TagD.getDicomTime((String)data);
                }
                value = is;
            } else {
                value = TagD.getDicomTime((String)data);
            }
        } else if (TagW.TagType.DICOM_DATETIME.equals((Object)this.type)) {
            if (this.vmMax > 1) {
                String[] ss = TagD.toStrings((String)data);
                Temporal[] is = new Temporal[ss.length];
                for (int i = 0; i < is.length; ++i) {
                    is[i] = TagD.getDicomDateTime((String)data);
                }
                value = is;
            } else {
                value = TagD.getDicomDateTime((String)data);
            }
        } else if (TagW.TagType.INTEGER.equals((Object)this.type)) {
            if (this.vmMax > 1) {
                String[] ss = TagD.toStrings((String)data);
                int[] ds = new int[ss.length];
                for (int i = 0; i < ds.length; ++i) {
                    String s = ss[i];
                    ds[i] = s != null && !s.isEmpty() ? (int)StringUtils.parseIS(s) : 0;
                }
                value = ds;
            } else {
                value = (int)StringUtils.parseIS((String)data);
            }
        } else if (TagW.TagType.FLOAT.equals((Object)this.type)) {
            if (this.vmMax > 1) {
                String[] ss = TagD.toStrings((String)data);
                float[] ds = new float[ss.length];
                for (int i = 0; i < ds.length; ++i) {
                    String s = ss[i];
                    ds[i] = s != null && !s.isEmpty() ? (float)StringUtils.parseDS(s) : Float.NaN;
                }
                value = ds;
            } else {
                value = Float.valueOf((float)StringUtils.parseDS((String)data));
            }
        } else if (TagW.TagType.DOUBLE.equals((Object)this.type)) {
            if (this.vmMax > 1) {
                String[] ss = TagD.toStrings((String)data);
                double[] ds = new double[ss.length];
                for (int i = 0; i < ds.length; ++i) {
                    String s = ss[i];
                    ds[i] = s != null && !s.isEmpty() ? StringUtils.parseDS(s) : Double.NaN;
                }
                value = ds;
            } else {
                value = StringUtils.parseDS((String)data);
            }
        } else {
            value = TagW.TagType.DICOM_SEQUENCE.equals((Object)this.type) ? data : (Object)data.getBytes(StandardCharsets.UTF_8);
        }
        return value;
    }

    private static String[] toStrings(String val) {
        return StringUtils.split(val, '\\');
    }

    public boolean isStringFamilyType() {
        return TagW.TagType.STRING.equals((Object)this.type) || TagW.TagType.TEXT.equals((Object)this.type) || TagW.TagType.URI.equals((Object)this.type) || TagW.TagType.DICOM_PERSON_NAME.equals((Object)this.type) || TagW.TagType.DICOM_PERIOD.equals((Object)this.type) || TagW.TagType.DICOM_SEX.equals((Object)this.type);
    }

    public String getFormattedTagValue(Object value, String format) {
        if (value == null) {
            return "";
        }
        if (TagW.TagType.DICOM_PERSON_NAME.equals((Object)this.type)) {
            if (value instanceof String[]) {
                String[] strings = (String[])value;
                return Arrays.stream(strings).map(TagD::getDicomPersonName).collect(Collectors.joining(", "));
            }
            return TagD.getDicomPersonName(value.toString());
        }
        if (TagW.TagType.DICOM_PERIOD.equals((Object)this.type)) {
            return TagD.getDicomPeriod(value.toString());
        }
        if (TagW.TagType.DICOM_SEX.equals((Object)this.type)) {
            return TagD.getDicomPatientSex(value.toString());
        }
        return super.getFormattedTagValue(value, format);
    }

    public static TagW get(String keyword) {
        return (TagW)tags.get(keyword);
    }

    public static String getKeywordFromTag(int tagID, String privateCreatorID) {
        return ElementDictionary.getElementDictionary(privateCreatorID).keywordOf(tagID);
    }

    private static TagW.TagType getTypeFromTag(int tagID, VR vr) {
        if (vr != null) {
            if (vr.isIntType()) {
                return TagW.TagType.INTEGER;
            }
            if (vr.isTemporalType()) {
                if (VR.DA.equals((Object)vr)) {
                    return TagW.TagType.DICOM_DATE;
                }
                if (VR.TM.equals((Object)vr)) {
                    return TagW.TagType.DICOM_TIME;
                }
                return TagW.TagType.DICOM_DATETIME;
            }
            if (vr.isStringType()) {
                if (VR.DS.equals((Object)vr)) {
                    return TagW.TagType.DOUBLE;
                }
                if (VR.PN.equals((Object)vr)) {
                    return TagW.TagType.DICOM_PERSON_NAME;
                }
                if (VR.UR.equals((Object)vr)) {
                    return TagW.TagType.URI;
                }
                if (VR.AS.equals((Object)vr)) {
                    return TagW.TagType.DICOM_PERIOD;
                }
                if (0x100040 == tagID) {
                    return TagW.TagType.DICOM_SEX;
                }
                if (VR.LT.equals((Object)vr) || VR.ST.equals((Object)vr) || VR.UT.equals((Object)vr)) {
                    return TagW.TagType.TEXT;
                }
                return TagW.TagType.STRING;
            }
            if (VR.SQ.equals((Object)vr)) {
                return TagW.TagType.DICOM_SEQUENCE;
            }
            if (VR.FD.equals((Object)vr)) {
                return TagW.TagType.DOUBLE;
            }
            if (VR.FL.equals((Object)vr)) {
                return TagW.TagType.FLOAT;
            }
            if (VR.SL.equals((Object)vr) || VR.SS.equals((Object)vr) || VR.UL.equals((Object)vr) || VR.US.equals((Object)vr)) {
                return TagW.TagType.INTEGER;
            }
            return TagW.TagType.BYTE;
        }
        return TagW.TagType.STRING;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Map<Integer, TagD> readTags() {
        HashMap<Integer, TagD> map;
        block7: {
            map = new HashMap<Integer, TagD>();
            XMLStreamReader xmler = null;
            InputStream stream = null;
            try {
                XMLInputFactory factory = XMLInputFactory.newInstance();
                factory.setProperty("javax.xml.stream.isSupportingExternalEntities", Boolean.FALSE);
                factory.setProperty("javax.xml.stream.supportDTD", Boolean.FALSE);
                stream = TagD.class.getResourceAsStream("/dataelements.xml");
                xmler = factory.createXMLStreamReader(stream);
                while (xmler.hasNext()) {
                    String key;
                    int eventType = xmler.next();
                    if (eventType != 1 || !"dataelements".equals(key = xmler.getName().getLocalPart())) continue;
                    while (xmler.hasNext()) {
                        eventType = xmler.next();
                        if (eventType != 1 || !"el".equals(key = xmler.getName().getLocalPart())) continue;
                        TagD.readElement(xmler, map);
                    }
                }
                StreamUtil.safeClose((XMLStreamReader)xmler);
            }
            catch (Exception e) {
                LOGGER.error("Cannot read dataelements.xml! ", (Throwable)e);
                break block7;
            }
            finally {
                StreamUtil.safeClose(xmler);
                StreamUtil.safeClose(stream);
            }
            StreamUtil.safeClose((AutoCloseable)stream);
        }
        return map;
    }

    private static void readElement(XMLStreamReader xmler, Map<Integer, TagD> map) throws XMLStreamException {
        String tag = xmler.getAttributeValue(null, "tag");
        String keyword = xmler.getAttributeValue(null, "keyword");
        String vr = xmler.getAttributeValue(null, "vr");
        String vm = xmler.getAttributeValue(null, "vm");
        String retired = xmler.getAttributeValue(null, "retired");
        boolean state = true;
        block6: while (xmler.hasNext() && state) {
            int eventType = xmler.next();
            switch (eventType) {
                case 4: {
                    if (tag != null && keyword != null && vr != null && vm != null) {
                        String disp = xmler.getText();
                        if (!StringUtil.hasText((CharSequence)disp)) continue block6;
                        try {
                            VR vrVal;
                            int vmMin;
                            int vmMax;
                            if (tag.startsWith("F")) {
                                return;
                            }
                            int tagID = Integer.parseInt(tag.replace('x', '0'), 16);
                            String[] vms = vm.split("-", 2);
                            if (vms.length == 1) {
                                vmMin = vmMax = TagD.getVM(vms[0]);
                            } else if (vms.length == 2) {
                                vmMin = TagD.getVM(vms[0]);
                                vmMax = TagD.getVM(vms[1]);
                            } else {
                                vmMax = 1;
                                vmMin = 1;
                            }
                            String defaultValue = null;
                            if (tagID == 0x100020 || tagID == 0x100010 || tagID == 0x20000D || tagID == 0x20000E || tagID == 524384) {
                                defaultValue = "UNKNOWN";
                            }
                            TagD t = VR.SQ.equals((Object)(vrVal = TagD.getVR(vr))) ? new TagSeq(tagID, keyword, disp, null, vrVal, vmMin, vmMax, defaultValue, LangUtil.emptyToFalse((String)retired)) : new TagD(tagID, keyword, disp, null, vrVal, vmMin, vmMax, defaultValue, LangUtil.emptyToFalse((String)retired));
                            TagW.addTag((TagW)t);
                        }
                        catch (Exception e) {
                            LOGGER.error("Cannot read {}", (Object)disp, (Object)e);
                        }
                        continue block6;
                    }
                    if (tag != null && tag.startsWith("FFFEE0")) continue block6;
                    LOGGER.error("Missing attribute: {} {} {} {}", new Object[]{tag, keyword, vr, vm});
                    continue block6;
                }
                case 2: {
                    if (!"el".equals(xmler.getName().getLocalPart())) continue block6;
                    state = false;
                    continue block6;
                }
            }
        }
    }

    private static int getVM(String val) {
        if ("n".equals(val) || val.toLowerCase().contains("n")) {
            return Integer.MAX_VALUE;
        }
        return Integer.parseInt(val);
    }

    private static VR getVR(String vr) {
        try {
            return VR.valueOf(vr);
        }
        catch (Exception e) {
            return VR.OB;
        }
    }

    public static TagW get(int tagID) {
        return TagD.get(tagID, null);
    }

    public static TagW get(int tagID, String privateCreatorID) {
        TagW val = TagD.getNullable(tagID, privateCreatorID);
        if (val == null) {
            return UnknownTag;
        }
        return val;
    }

    public static TagW getNullable(int tagID) {
        return TagD.getNullable(tagID, null);
    }

    public static TagW getNullable(int tagID, String privateCreatorID) {
        String key = TagD.getKeywordFromTag(tagID, privateCreatorID);
        return key == null ? null : (TagW)tags.get(key);
    }

    public static Object getTagValue(TagReadable taggable, int tagID) {
        String key;
        if (taggable != null && (key = TagD.getKeywordFromTag(tagID, null)) != null) {
            return taggable.getTagValue((TagW)tags.get(key));
        }
        return null;
    }

    public static <T> T getTagValue(TagReadable taggable, int tagID, Class<T> type) {
        String key;
        if (taggable != null && (key = TagD.getKeywordFromTag(tagID, null)) != null) {
            try {
                return type.cast(taggable.getTagValue((TagW)tags.get(key)));
            }
            catch (ClassCastException e) {
                LOGGER.error("Cannot cast the value of \"{}\" into {}", new Object[]{key, type, e});
            }
        }
        return null;
    }

    public static TagW[] getTagFromIDs(int ... tagID) {
        ArrayList<TagW> list = new ArrayList<TagW>();
        if (tagID != null) {
            for (int id : tagID) {
                TagW t = TagD.getNullable(id, null);
                if (t == null) continue;
                list.add(t);
            }
        }
        return list.toArray(new TagW[0]);
    }

    public static TagW getUID(Level level) {
        if (level != null) {
            switch (level.ordinal()) {
                case 0: {
                    return TagW.PatientPseudoUID;
                }
                case 1: {
                    return TagD.get(0x20000D);
                }
                case 2: {
                    return TagW.SubseriesInstanceUID;
                }
                case 3: 
                case 4: {
                    return TagD.get(524312);
                }
            }
        }
        return TagW.UnknownTag;
    }

    public static LocalDate getDicomDate(String date) {
        if (StringUtil.hasText((CharSequence)date)) {
            try {
                return DateTimeUtils.parseDA(date);
            }
            catch (Exception e) {
                LOGGER.error("Parse DICOM date", (Throwable)e);
            }
        }
        return null;
    }

    public static LocalTime getDicomTime(String time) {
        if (StringUtil.hasText((CharSequence)time)) {
            try {
                return DateTimeUtils.parseTM(time);
            }
            catch (Exception e1) {
                LOGGER.error("Parse DICOM time", (Throwable)e1);
            }
        }
        return null;
    }

    public static Temporal getDicomDateTime(String value) {
        if (StringUtil.hasText((CharSequence)value)) {
            try {
                return DateTimeUtils.parseDT(value);
            }
            catch (Exception e) {
                LOGGER.error("Parse DICOM dateTime", (Throwable)e);
            }
        }
        return null;
    }

    public static LocalDateTime dateTime(long dateTimeID, Attributes attributes) {
        if (attributes == null) {
            return null;
        }
        Date date = attributes.getDate(dateTimeID);
        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    public static LocalDateTime dateTime(int dateID, int timeID, TagReadable taggable) {
        LocalDate date = TagD.getTagValue(taggable, dateID, LocalDate.class);
        LocalTime time = TagD.getTagValue(taggable, timeID, LocalTime.class);
        return DateTimeUtils.dateTime(date, time);
    }

    public static String formatDicomDate(LocalDate date) {
        if (date != null) {
            try {
                return DateTimeUtils.formatDA(date);
            }
            catch (Exception e) {
                LOGGER.error("Format date", (Throwable)e);
            }
        }
        return "";
    }

    public static String formatDicomTime(LocalTime time) {
        if (time != null) {
            try {
                return DateTimeUtils.formatTM(time);
            }
            catch (Exception e) {
                LOGGER.error("Format time", (Throwable)e);
            }
        }
        return "";
    }

    public static TemporalAccessor getDateFromElement(AttributeSource source, String attribute, TagW.TagType type, TemporalAccessor defaultValue) {
        String val;
        if (attribute != null && (val = source.getAttribute(attribute)) != null) {
            if (TagW.TagType.DICOM_TIME.equals((Object)type)) {
                return TagD.getDicomTime(val);
            }
            if (TagW.TagType.DICOM_DATETIME.equals((Object)type)) {
                return TagD.getDicomDateTime(val);
            }
            return TagD.getDicomDate(val);
        }
        return defaultValue;
    }

    public static TemporalAccessor[] getDatesFromElement(AttributeSource source, String attribute, TagW.TagType type, TemporalAccessor[] defaultValue) {
        return TagD.getDatesFromElement(source, attribute, type, defaultValue, "\\");
    }

    public static TemporalAccessor[] getDatesFromElement(AttributeSource source, String attribute, TagW.TagType type, TemporalAccessor[] defaultValue, String separator) {
        String val;
        if (attribute != null && (val = source.getAttribute(attribute)) != null) {
            String[] strs = val.split(Pattern.quote(separator));
            TemporalAccessor[] vals = new TemporalAccessor[strs.length];
            for (int i = 0; i < strs.length; ++i) {
                vals[i] = TagW.TagType.TIME.equals((Object)type) ? TagD.getDicomTime(strs[i]) : (TagW.TagType.DATETIME.equals((Object)type) ? TagD.getDicomDateTime(strs[i]) : TagD.getDicomDate(strs[i]));
            }
            return vals;
        }
        return defaultValue;
    }

    public static String getDicomPeriod(String value) {
        String unit;
        if (!StringUtil.hasText((CharSequence)value)) {
            return "";
        }
        if (value.length() < 2) {
            return "";
        }
        switch (value.charAt(value.length() - 1)) {
            case 'Y': {
                unit = ChronoUnit.YEARS.toString();
                break;
            }
            case 'M': {
                unit = ChronoUnit.MONTHS.toString();
                break;
            }
            case 'W': {
                unit = ChronoUnit.WEEKS.toString();
                break;
            }
            case 'D': {
                unit = ChronoUnit.DAYS.toString();
                break;
            }
            default: {
                return "";
            }
        }
        return value.substring(0, value.length() - 1).replaceFirst("^0+(?!$)", "") + " " + unit;
    }

    public static String getDicomPersonName(String name) {
        if (!StringUtil.hasText((CharSequence)name)) {
            return "";
        }
        StringBuilder buf = new StringBuilder();
        String[] names = name.split("=");
        for (int k = 0; k < names.length; ++k) {
            if (k > 0) {
                buf.append("=");
            }
            String[] vals = names[k].split("\\^");
            for (int i = 0; i < vals.length; ++i) {
                if (!StringUtil.hasText((CharSequence)vals[i])) continue;
                if (i == 1 || i >= 3) {
                    buf.append(", ");
                } else {
                    buf.append(" ");
                }
                buf.append(vals[i]);
            }
        }
        return buf.toString().trim();
    }

    public static String getDicomPatientSex(String val) {
        if (!StringUtil.hasText((CharSequence)val)) {
            return "";
        }
        return Sex.getSex(val).toString();
    }

    static {
        vrToMaxChars.put(VR.AE, 16);
        vrToMaxChars.put(VR.AS, 4);
        vrToMaxChars.put(VR.CS, 16);
        vrToMaxChars.put(VR.DA, 8);
        vrToMaxChars.put(VR.DS, 16);
        vrToMaxChars.put(VR.DT, 26);
        vrToMaxChars.put(VR.IS, 12);
        vrToMaxChars.put(VR.LO, 64);
        vrToMaxChars.put(VR.LT, 10240);
        vrToMaxChars.put(VR.PN, 129);
        vrToMaxChars.put(VR.SH, 16);
        vrToMaxChars.put(VR.ST, 1024);
        vrToMaxChars.put(VR.UI, 64);
        TagD.readTags();
    }

    public static enum Level {
        PATIENT("Patient"),
        STUDY("Study"),
        SERIES("Series"),
        INSTANCE("Instance"),
        FRAME("Frame");

        private final String tag;

        private Level(String tag) {
            this.tag = tag;
        }

        public String getTagName() {
            return this.tag;
        }

        public String toString() {
            return this.tag;
        }
    }

    public static enum Sex {
        SEX_MALE("M", Messages.getString((String)"TagW.Male")),
        SEX_FEMALE("F", Messages.getString((String)"TagW.female")),
        SEX_OTHER("O", Messages.getString((String)"TagW.other"));

        private final String value;
        private final String displayValue;

        private Sex(String value, String displayValue) {
            this.value = value;
            this.displayValue = displayValue;
        }

        public String getValue() {
            return this.value;
        }

        public String toString() {
            return this.displayValue;
        }

        public static Sex getSex(String sex) {
            Sex s = SEX_OTHER;
            if (StringUtil.hasText((CharSequence)sex)) {
                return sex.toUpperCase().startsWith("F") ? SEX_FEMALE : (sex.toUpperCase().startsWith("M") ? SEX_MALE : s);
            }
            return s;
        }
    }
}
