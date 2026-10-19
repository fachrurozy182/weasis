/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.media.data.ImageElement
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagUtil
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.media.data.TagW$TagType
 *  org.weasis.core.api.media.data.Taggable
 *  org.weasis.core.util.MathUtil
 *  org.weasis.core.util.StreamUtil
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.codec.utils;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDate;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.dcm4che3.img.DicomMetaData;
import org.dcm4che3.img.lut.ModalityLutModule;
import org.dcm4che3.img.lut.VoiLutModule;
import org.dcm4che3.img.util.DateTimeUtils;
import org.dcm4che3.img.util.DicomObjectUtil;
import org.dcm4che3.img.util.DicomUtils;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.TagUtils;
import org.dcm4che3.util.UIDUtils;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagUtil;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.media.data.Taggable;
import org.weasis.core.util.MathUtil;
import org.weasis.core.util.StreamUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.DicomMediaIO;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.TagSeq;
import org.weasis.dicom.codec.geometry.ImageOrientation;
import org.weasis.dicom.codec.geometry.PatientOrientation;
import org.weasis.dicom.codec.geometry.VectorUtils;
import org.weasis.dicom.codec.utils.PatientComparator;

public class DicomMediaUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomMediaUtils.class);
    private static final int[] modalityLutAttributes = new int[]{2625618, 2625619};
    private static final int[] VOILUTWindowLevelAttributes = new int[]{2625616, 2625617};
    private static final int[] LUTAttributes = new int[]{2633730, 2633734};

    private DicomMediaUtils() {
    }

    public static synchronized void enableAnonymizationProfile(boolean activate) {
        int[] list = new int[]{0x100010, 0x100020, 0x100040, 0x100030, 1048626, 0x101010, 0x104000, 0x101030, 524368, 0x200010, 528448, 524416, 524432, 528432, 528446, 528400, 0x204000};
        int type = activate ? 1 : 0;
        for (int id : list) {
            TagW t = TagD.getNullable(id);
            if (t == null) continue;
            t.setAnonymizationType(type);
        }
        TagW.PatientPseudoUID.setAnonymizationType(type);
    }

    public static boolean containsRequiredAttributes(Attributes dcmItems, int ... requiredTags) {
        if (dcmItems == null || requiredTags == null || requiredTags.length == 0) {
            return false;
        }
        int countValues = 0;
        for (int tag : requiredTags) {
            if (!dcmItems.containsValue(tag)) continue;
            ++countValues;
        }
        return countValues == requiredTags.length;
    }

    public static boolean containsRequiredModalityLUTAttributes(Attributes dcmItems) {
        return DicomMediaUtils.containsRequiredAttributes(dcmItems, modalityLutAttributes);
    }

    public static boolean containsRequiredModalityLUTDataAttributes(Attributes dcmItems) {
        return DicomMediaUtils.containsRequiredAttributes(dcmItems, 2633732) && DicomMediaUtils.containsLUTAttributes(dcmItems);
    }

    public static boolean containsRequiredVOILUTWindowLevelAttributes(Attributes dcmItems) {
        return DicomMediaUtils.containsRequiredAttributes(dcmItems, VOILUTWindowLevelAttributes);
    }

    public static boolean containsLUTAttributes(Attributes dcmItems) {
        return DicomMediaUtils.containsRequiredAttributes(dcmItems, LUTAttributes);
    }

    public static boolean hasOverlay(Attributes attrs) {
        if (attrs != null) {
            for (int i = 0; i < 16; ++i) {
                int gg0000 = i << 17;
                if ((0xFFFF & 1 << i) == 0 || !attrs.containsValue(0x60000010 | gg0000)) continue;
                return true;
            }
        }
        return false;
    }

    public static Integer getIntPixelValue(Attributes ds, int tag, boolean signed, int stored) {
        VR vr = ds.getVR(tag);
        if (vr == null) {
            return null;
        }
        int result = 0;
        if (vr == VR.OB || vr == VR.OW) {
            try {
                result = ByteUtils.bytesToUShortLE(ds.getBytes(tag), 0);
            }
            catch (IOException e) {
                LOGGER.error("Cannot read {} ", (Object)TagUtils.toString(tag), (Object)e);
            }
            if (signed && (result & 1 << stored - 1) != 0) {
                int andmask = (1 << stored) - 1;
                int ormask = ~andmask;
                result |= ormask;
            }
        } else if (!signed && vr != VR.US || signed && vr != VR.SS) {
            vr = signed ? VR.SS : VR.US;
            result = ds.getInt(null, tag, vr, 0);
        } else {
            result = ds.getInt(tag, 0);
        }
        int minInValue = signed ? -(1 << stored - 1) : 0;
        int maxInValue = signed ? (1 << stored - 1) - 1 : (1 << stored) - 1;
        return result < minInValue ? minInValue : Math.min(result, maxInValue);
    }

    public static void setTag(Map<TagW, Object> tags, TagW tag, Object value) {
        if (tag != null) {
            if (value instanceof Sequence) {
                Sequence seq = (Sequence)value;
                Attributes[] list = new Attributes[seq.size()];
                for (int i = 0; i < list.length; ++i) {
                    Attributes attributes = (Attributes)seq.get(i);
                    list[i] = attributes.getParent() == null ? attributes : new Attributes(attributes);
                }
                tags.put(tag, list);
            } else {
                tags.put(tag, value);
            }
        }
    }

    public static void setTagNoNull(Map<TagW, Object> tags, TagW tag, Object value) {
        if (value != null) {
            DicomMediaUtils.setTag(tags, tag, value);
        }
    }

    public static void writeMetaData(MediaSeriesGroup group, Attributes header) {
        if (group == null || header == null) {
            return;
        }
        if (TagD.getUID(TagD.Level.PATIENT).equals((Object)group.getTagID())) {
            String pid2;
            Object pid = group.getTagValue(TagW.PatientPseudoUID);
            if (pid != null && !Objects.equals(pid, pid2 = new PatientComparator(header).buildPatientPseudoUID())) {
                LOGGER.warn("Inconsistent Patient ID + Issuer of Patient ID between DICOM objets: {} and {}", pid, (Object)pid2);
            }
            DicomMediaIO.tagManager.readTags(TagD.Level.PATIENT, header, (Taggable)group);
        } else if (TagD.getUID(TagD.Level.STUDY).equals((Object)group.getTagID())) {
            TagW tagID = TagD.getUID(TagD.Level.STUDY);
            Object studyUID = group.getTagValue(tagID);
            if (studyUID != null && !Objects.equals(studyUID, header.getString(tagID.getId()))) {
                LOGGER.warn("Inconsistent Study Instance UID between DICOM objets: {} and {}", group.getTagValue(TagD.getUID(TagD.Level.STUDY)), (Object)header.getString(0x20000D));
            }
            DicomMediaIO.tagManager.readTags(TagD.Level.STUDY, header, (Taggable)group);
        } else if (TagD.getUID(TagD.Level.SERIES).equals((Object)group.getTagID())) {
            TagW tagID = TagD.get(0x20000E);
            Object seriesUID = group.getTagValue(tagID);
            if (seriesUID != null && !Objects.equals(seriesUID, header.getString(tagID.getId()))) {
                LOGGER.warn("Inconsistent Series Instance UID between DICOM objets: {} and {}", seriesUID, (Object)header.getString(tagID.getId()));
            }
            DicomMediaIO.tagManager.readTags(TagD.Level.SERIES, header, (Taggable)group);
            group.setTagNoNull(TagD.get(0x101010), (Object)DicomUtils.getPatientAgeInPeriod(header, 0x101010, true));
        }
    }

    public static Vector3d computeImageNormal(TagReadable taggable) {
        Vector3d vr = ImageOrientation.getRowImagePosition(taggable);
        Vector3d vc = ImageOrientation.getColumnImagePosition(taggable);
        if (vr == null || vc == null) {
            return null;
        }
        Vector3d normal = VectorUtils.computeNormalOfSurface(vr, vc);
        return VectorUtils.orientNormalToDominantPositiveAxis(normal);
    }

    public static Double computeSlicePosition(Taggable taggable) {
        if (taggable != null) {
            Vector3d normal;
            Vector3d pPos = PatientOrientation.getPatientPosition((TagReadable)taggable);
            if (pPos != null && (normal = DicomMediaUtils.computeImageNormal((TagReadable)taggable)) != null) {
                double dot = normal.dot((Vector3dc)pPos);
                taggable.setTag(TagW.SlicePosition, (Object)dot);
                return dot;
            }
            Double sliceLoc = TagD.getTagValue((TagReadable)taggable, 2101313, Double.class);
            if (sliceLoc != null) {
                taggable.setTag(TagW.SlicePosition, (Object)sliceLoc);
                return sliceLoc;
            }
        }
        return null;
    }

    public static double getSlicePositionValue(TagReadable taggable) {
        if (taggable == null) {
            return 0.0;
        }
        Double loc = (Double)taggable.getTagValue(TagW.SlicePosition);
        return loc == null ? 0.0 : loc;
    }

    public static void setShutter(Taggable taggable, Attributes dcmObject) {
        taggable.setTagNoNull(TagW.ShutterFinalShape, (Object)DicomObjectUtil.getShutterShape(dcmObject));
        taggable.setTagNoNull(TagW.ShutterRGBColor, (Object)DicomObjectUtil.getShutterColor(dcmObject));
    }

    public static void writeFunctionalGroupsSequence(Taggable taggable, DicomMetaData md, Attributes dcm, int frameIndex) {
        if (dcm != null && taggable != null) {
            Attributes vLutItems;
            TagSeq.MacroSeqData data = new TagSeq.MacroSeqData(dcm, TagD.getTagFromIDs(2621488, 1572944));
            TagD.get(2658576).readValue((Object)data, taggable);
            data = new TagSeq.MacroSeqData(dcm, TagD.getTagFromIDs(2134358, 2134102, 2134103, 2134312));
            TagD.get(2134289).readValue((Object)data, taggable);
            Integer stackPosNb = (Integer)taggable.getTagValue(TagD.get(2134103));
            if (stackPosNb != null) {
                Integer offset = (Integer)taggable.getTagValue(TagD.get(2134568));
                int nb = offset == null ? stackPosNb : offset + stackPosNb;
                taggable.setTag(TagD.get(2097171), (Object)nb);
            }
            data = new TagSeq.MacroSeqData(dcm, TagD.getTagFromIDs(0x200032));
            TagD.get(2134291).readValue((Object)data, taggable);
            data = new TagSeq.MacroSeqData(dcm, TagD.getTagFromIDs(2097207));
            TagD.get(2134294).readValue((Object)data, taggable);
            taggable.setTagNoNull(TagW.ImageOrientationPlane, (Object)ImageOrientation.getPlan((TagReadable)taggable));
            data = new TagSeq.MacroSeqData(dcm, TagD.getTagFromIDs(2134130));
            TagD.get(2134129).readValue((Object)data, taggable);
            Attributes mLutItems = dcm.getNestedDataset(2658629);
            if (mLutItems != null) {
                ModalityLutModule mlut = new ModalityLutModule(mLutItems);
                md.getImageDescriptor().setModalityLutForFrame(Math.max(frameIndex, 0), mlut);
            }
            if ((vLutItems = dcm.getNestedDataset(2658610)) != null) {
                VoiLutModule vlut = new VoiLutModule(vLutItems);
                md.getImageDescriptor().setVoiLutForFrame(Math.max(frameIndex, 0), vlut);
            }
            data = new TagSeq.MacroSeqData(dcm, TagD.getTagFromIDs(0x200020));
            TagD.get(2135120).readValue((Object)data, taggable);
            Attributes macroFrameDisplayShutter = dcm.getNestedDataset(1610866);
            if (macroFrameDisplayShutter != null) {
                DicomMediaUtils.setShutter(taggable, macroFrameDisplayShutter);
            }
            data = new TagSeq.MacroSeqData(dcm, TagD.getTagFromIDs(561159));
            TagD.get(1610278).readValue((Object)data, taggable);
            TagD.get(1610537).readValue((Object)data, taggable);
            TagD.get(1610279).readValue((Object)data, taggable);
            TagD.get(1611601).readValue((Object)data, taggable);
            TagD.get(1611012).readValue((Object)data, taggable);
            TagD.get(0x520025).readValue((Object)data, taggable);
        }
    }

    public static boolean writeFrameGeometry(Taggable taggable, DicomMetaData md, int index) {
        return DicomMediaUtils.writePerFrameFunctionalGroupsSequence(taggable, md, index) || DicomMediaUtils.writeNmTomoGeometry(taggable, md, index);
    }

    public static boolean writePerFrameFunctionalGroupsSequence(Taggable taggable, DicomMetaData md, int index) {
        Attributes a;
        Attributes header = md.getDicomObject();
        if (header != null && taggable != null && (a = header.getNestedDataset(1375769136, index)) != null) {
            DicomMediaUtils.writeFunctionalGroupsSequence(taggable, md, a, index);
            return true;
        }
        return false;
    }

    public static boolean writeNmTomoGeometry(Taggable taggable, DicomMetaData md, int frameIndex) {
        Attributes header;
        Attributes attributes = header = md == null ? null : md.getDicomObject();
        if (header == null || taggable == null || !"NM".equals(header.getString(524384))) {
            return false;
        }
        Attributes detector = header.getNestedDataset(5505058);
        if (detector == null) {
            return false;
        }
        double[] iop = detector.getDoubles(2097207);
        double[] ipp = detector.getDoubles(0x200032);
        double spacing = header.getDouble(0x180088, 0.0);
        if (iop == null || iop.length != 6 || ipp == null || ipp.length != 3 || spacing == 0.0) {
            return false;
        }
        Vector3d normal = new Vector3d();
        new Vector3d(iop[0], iop[1], iop[2]).cross((Vector3dc)new Vector3d(iop[3], iop[4], iop[5]), normal);
        if (normal.lengthSquared() == 0.0) {
            return false;
        }
        normal.normalize();
        int sliceOffset = frameIndex;
        int[] sliceVector = header.getInts(5505152);
        if (sliceVector != null && frameIndex >= 0 && frameIndex < sliceVector.length) {
            sliceOffset = sliceVector[frameIndex] - 1;
        }
        double step = spacing * (double)sliceOffset;
        double[] framePos = new double[]{ipp[0] + normal.x * step, ipp[1] + normal.y * step, ipp[2] + normal.z * step};
        taggable.setTag(TagD.get(2097207), (Object)iop);
        taggable.setTag(TagD.get(0x200032), (Object)framePos);
        return true;
    }

    public static void computeSUVFactor(Attributes dicomObject, Taggable taggable, int index) {
        String modality = TagD.getTagValue((TagReadable)taggable, 524384, String.class);
        if (!"PT".equals(modality)) {
            return;
        }
        double suvFactor = DicomMediaUtils.suvFactor(dicomObject, index);
        if (MathUtil.isDifferentFromZero((double)suvFactor)) {
            taggable.setTag(TagW.SuvFactor, (Object)suvFactor);
        }
    }

    private static double noSuvFactor(int tag) {
        LOGGER.debug("No SUV factor: {} is missing or not supported", (Object)ElementDictionary.keywordOf(tag, null));
        return 0.0;
    }

    private static double suvFactor(Attributes dicomObject, int index) {
        String correctedImage = DicomUtils.getStringFromDicomElement(dicomObject, 2621521);
        if (correctedImage == null || !correctedImage.contains("ATTN") || !correctedImage.contains("DECY")) {
            return DicomMediaUtils.noSuvFactor(2621521);
        }
        String units = dicomObject.getString(5509121);
        return switch (units == null ? "" : units) {
            case "BQML" -> DicomMediaUtils.bqmlSuvFactor(dicomObject, index);
            case "CNTS" -> DicomMediaUtils.philipsSuvFactor(dicomObject);
            case "GML" -> 1.0;
            default -> DicomMediaUtils.noSuvFactor(5509121);
        };
    }

    private static double bqmlSuvFactor(Attributes dicomObject, int index) {
        Float weight = DicomUtils.getFloatFromDicomElement(dicomObject, 0x101030, Float.valueOf(0.0f));
        if (!MathUtil.isDifferentFromZero((float)weight.floatValue())) {
            return DicomMediaUtils.noSuvFactor(0x101030);
        }
        Attributes dcm = dicomObject.getNestedDataset(5505046, index);
        if (dcm == null) {
            return DicomMediaUtils.noSuvFactor(5505046);
        }
        Float totalDose = DicomUtils.getFloatFromDicomElement(dcm, 1577076, null);
        if (totalDose == null) {
            return DicomMediaUtils.noSuvFactor(1577076);
        }
        Float halfLife = DicomUtils.getFloatFromDicomElement(dcm, 1577077, null);
        if (halfLife == null || halfLife.floatValue() <= 0.0f) {
            return DicomMediaUtils.noSuvFactor(1577077);
        }
        if (!"START".equals(dicomObject.getString(5509378))) {
            return DicomMediaUtils.noSuvFactor(5509378);
        }
        double time = DicomMediaUtils.uptakeTime(dicomObject, dcm);
        if (time <= 0.0) {
            return DicomMediaUtils.noSuvFactor(1577080);
        }
        double correctedDose = (double)totalDose.floatValue() * Math.pow(2.0, -time / (1000.0 * (double)halfLife.floatValue()));
        if (correctedDose <= 0.0) {
            return DicomMediaUtils.noSuvFactor(1577076);
        }
        return (double)weight.floatValue() * 1000.0 / correctedDose;
    }

    private static double philipsSuvFactor(Attributes dicomObject) {
        String privateTagCreator = dicomObject.getString(1884487696);
        double privateSUVFactor = dicomObject.getDouble(1884491776, 0.0);
        if ("Philips PET Private Group".equals(privateTagCreator) && MathUtil.isDifferentFromZero((double)privateSUVFactor)) {
            return privateSUVFactor;
        }
        return DicomMediaUtils.noSuvFactor(5509121);
    }

    private static double uptakeTime(Attributes dicomObject, Attributes radiopharmaceutical) {
        Date acquisitionDateTime = dicomObject.getDate(2251945843097650L);
        Date scanDate = dicomObject.getDate(2251941548130353L);
        if (acquisitionDateTime == null || scanDate == null) {
            return 0.0;
        }
        long scanDateTime = scanDate.getTime();
        Date injectDateTime = DicomUtils.getDateFromDicomElement(radiopharmaceutical, 1577080, null);
        if (injectDateTime == null) {
            Date injectTime = DicomUtils.getDateFromDicomElement(radiopharmaceutical, 1577074, null);
            if (injectTime == null) {
                return 0.0;
            }
            if (scanDateTime > acquisitionDateTime.getTime()) {
                String privateCreator = dicomObject.getString(589840);
                Date privateScanDateTime = DicomUtils.getDateFromDicomElement(radiopharmaceutical, 593933, null);
                if (!"GEMS_PETD_01".equals(privateCreator) || privateScanDateTime == null) {
                    return 0.0;
                }
                scanDate = privateScanDateTime;
            }
            injectDateTime = DateTimeUtils.dateTime(dicomObject.getTimeZone(), scanDate, injectTime, false);
        }
        return (double)scanDateTime - (double)injectDateTime.getTime();
    }

    public static double[] getFrameTime(Attributes attributes) {
        double[] frameTimes = null;
        String frameIncrementPointer = attributes.getString(2621449, null);
        if (StringUtil.hasText((CharSequence)frameIncrementPointer)) {
            frameTimes = DicomUtils.getDoubleArrayFromDicomElement(attributes, TagUtils.intFromHexString(frameIncrementPointer), null);
        }
        if (frameTimes == null) {
            Integer frameRate = DicomUtils.getIntegerFromDicomElement(attributes, 532804, null);
            if (frameRate != null) {
                frameTimes = new double[]{1000.0f / (float)frameRate.intValue()};
            }
        } else {
            frameTimes = DicomMediaUtils.validateFrameTime(frameTimes);
        }
        return frameTimes;
    }

    private static double[] validateFrameTime(double[] frameTimes) {
        if (frameTimes != null && frameTimes.length > 1) {
            boolean same = true;
            double first = frameTimes[0];
            for (int i = 1; i < frameTimes.length; ++i) {
                if (!(Math.abs(first - frameTimes[i]) > 1.0E-9) || !MathUtil.isDifferentFromZero((double)frameTimes[i])) continue;
                same = false;
                break;
            }
            if (same) {
                return new double[]{first};
            }
        }
        return frameTimes;
    }

    public static Attributes createDicomKeyObject(Attributes dicomSourceAttribute, String keyObjectDescription, String seriesInstanceUID) {
        Map<String, KeyObjectSelectionCode> codeByValue = DicomMediaUtils.getKeyObjectSelectionMappingResources();
        HashMap<String, Set> resourcesByContextID = new HashMap<String, Set>();
        for (KeyObjectSelectionCode code : codeByValue.values()) {
            Set resourceSet = resourcesByContextID.computeIfAbsent(code.contextGroupID, k -> new TreeSet());
            resourceSet.add(code);
        }
        Attributes documentTitle = codeByValue.get("113000").toCodeItem();
        String seriesNumber = "999";
        String instanceNumber = "1";
        int[] patientStudyAttributes = new int[]{524293, 524320, 524336, 524368, 524369, 524432, 0x100010, 0x100020, 0x100021, 0x100030, 0x100040, 0x20000D, 0x200010};
        Arrays.sort(patientStudyAttributes);
        Attributes dKOS = new Attributes(dicomSourceAttribute, patientStudyAttributes);
        dKOS.setString(524310, VR.UI, "1.2.840.10008.5.1.4.1.1.88.59");
        dKOS.setString(524312, VR.UI, UIDUtils.createUID());
        dKOS.setDate(2251950138064947L, new Date());
        dKOS.setString(524384, VR.CS, "KO");
        dKOS.setNull(528657, VR.SQ);
        dKOS.setString(0x20000E, VR.UI, StringUtil.hasText((CharSequence)seriesInstanceUID) ? seriesInstanceUID : UIDUtils.createUID());
        dKOS.setString(0x200011, VR.IS, "999");
        dKOS.setString(2097171, VR.IS, "1");
        dKOS.setString(0x40A040, VR.CS, "CONTAINER");
        dKOS.setString(4235344, VR.CS, "SEPARATE");
        dKOS.newSequence(4235331, 1).add(documentTitle);
        dKOS.newSequence(4236149, 1);
        Attributes templateIdentifier = new Attributes(2);
        templateIdentifier.setString(524549, VR.CS, "DCMR");
        templateIdentifier.setString(4250368, VR.CS, "2010");
        dKOS.newSequence(4236548, 1).add(templateIdentifier);
        Sequence contentSeq = dKOS.newSequence(4237104, 1);
        if (StringUtil.hasText((CharSequence)keyObjectDescription)) {
            Attributes keyObjectDescriptionSequence = new Attributes(4);
            keyObjectDescriptionSequence.setString(4235280, VR.CS, "CONTAINS");
            keyObjectDescriptionSequence.setString(0x40A040, VR.CS, "TEXT");
            keyObjectDescriptionSequence.newSequence(4235331, 1).add(codeByValue.get("113012").toCodeItem());
            keyObjectDescriptionSequence.setString(4235616, VR.UT, keyObjectDescription);
            contentSeq.add(keyObjectDescriptionSequence);
            dKOS.setString(528446, VR.LO, keyObjectDescription);
        }
        return dKOS;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static Map<String, KeyObjectSelectionCode> getKeyObjectSelectionMappingResources() {
        HashMap<String, KeyObjectSelectionCode> codeByValue;
        block7: {
            codeByValue = new HashMap<String, KeyObjectSelectionCode>();
            XMLStreamReader xmler = null;
            InputStream stream = null;
            try {
                XMLInputFactory factory = XMLInputFactory.newInstance();
                factory.setProperty("javax.xml.stream.isSupportingExternalEntities", Boolean.FALSE);
                factory.setProperty("javax.xml.stream.supportDTD", Boolean.FALSE);
                stream = DicomMediaUtils.class.getResourceAsStream("/config/KeyObjectSelectionCodes.xml");
                xmler = factory.createXMLStreamReader(stream);
                while (xmler.hasNext()) {
                    String key;
                    if (xmler.next() != 1 || !"resources".equals(key = xmler.getName().getLocalPart())) continue;
                    while (xmler.hasNext()) {
                        if (xmler.next() != 1) continue;
                        DicomMediaUtils.readCodeResource(xmler, codeByValue);
                    }
                }
                StreamUtil.safeClose((XMLStreamReader)xmler);
            }
            catch (XMLStreamException e) {
                LOGGER.error("Reading KO Codes", (Throwable)e);
                codeByValue = null;
                break block7;
            }
            finally {
                StreamUtil.safeClose(xmler);
                StreamUtil.safeClose(stream);
            }
            StreamUtil.safeClose((AutoCloseable)stream);
        }
        return codeByValue;
    }

    private static void readCodeResource(XMLStreamReader xmler, Map<String, KeyObjectSelectionCode> codeByValue) throws XMLStreamException {
        String key = xmler.getName().getLocalPart();
        if ("resource".equals(key)) {
            String resourceName = xmler.getAttributeValue(null, "name");
            String contextGroupID = xmler.getAttributeValue(null, "contextId");
            while (xmler.hasNext()) {
                int eventType = xmler.next();
                if (eventType != 1 || !"code".equals(key = xmler.getName().getLocalPart())) continue;
                String codingSchemeDesignator = xmler.getAttributeValue(null, "scheme");
                String codeValue = xmler.getAttributeValue(null, "value");
                String codeMeaning = xmler.getAttributeValue(null, "meaning");
                String conceptNameCodeModifier = xmler.getAttributeValue(null, "conceptMod");
                String contextGroupIdModifier = xmler.getAttributeValue(null, "contexId");
                codeByValue.put(codeValue, new KeyObjectSelectionCode(resourceName, contextGroupID, codingSchemeDesignator, codeValue, codeMeaning, conceptNameCodeModifier, contextGroupIdModifier));
            }
        }
    }

    public static TemporalAccessor getDateFromDicomElement(TagW.TagType type, Attributes dicom, int tag, String privateCreatorID, TemporalAccessor defaultValue) {
        if (dicom == null || !dicom.containsValue(tag)) {
            return defaultValue;
        }
        Date date = dicom.getDate(privateCreatorID, tag);
        if (date == null) {
            return defaultValue;
        }
        if (TagW.TagType.DICOM_DATE == type) {
            return TagUtil.toLocalDate((Date)date);
        }
        if (TagW.TagType.DICOM_TIME == type) {
            return TagUtil.toLocalTime((Date)date);
        }
        return TagUtil.toLocalDateTime((Date)date);
    }

    public static TemporalAccessor[] getDatesFromDicomElement(TagW.TagType type, Attributes dicom, int tag, String privateCreatorID, TemporalAccessor[] defaultValue) {
        int i;
        Comparable<ChronoLocalDate>[] vals;
        if (dicom == null || !dicom.containsValue(tag)) {
            return defaultValue;
        }
        Date[] dates = dicom.getDates(privateCreatorID, tag);
        if (dates == null || dates.length == 0) {
            return defaultValue;
        }
        if (TagW.TagType.DICOM_DATE == type) {
            vals = new LocalDate[dates.length];
            for (i = 0; i < vals.length; ++i) {
                vals[i] = TagUtil.toLocalDate((Date)dates[i]);
            }
        } else if (TagW.TagType.DICOM_TIME == type) {
            vals = new LocalTime[dates.length];
            for (i = 0; i < vals.length; ++i) {
                vals[i] = TagUtil.toLocalTime((Date)dates[i]);
            }
        }
        vals = new LocalDateTime[dates.length];
        for (i = 0; i < vals.length; ++i) {
            vals[i] = TagUtil.toLocalDateTime((Date)dates[i]);
        }
        return vals;
    }

    public static TemporalAccessor getDateFromDicomElement(XMLStreamReader xmler, String attribute, TagW.TagType type, TemporalAccessor defaultValue) {
        String val;
        if (attribute != null && (val = xmler.getAttributeValue(null, attribute)) != null) {
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

    public static TemporalAccessor[] getDatesFromDicomElement(XMLStreamReader xmler, String attribute, TagW.TagType type, TemporalAccessor[] defaultValue) {
        return DicomMediaUtils.getDatesFromDicomElement(xmler, attribute, type, defaultValue, "\\");
    }

    public static TemporalAccessor[] getDatesFromDicomElement(XMLStreamReader xmler, String attribute, TagW.TagType type, TemporalAccessor[] defaultValue, String separator) {
        String val;
        if (attribute != null && (val = xmler.getAttributeValue(null, attribute)) != null) {
            String[] strs = val.split(Pattern.quote(separator));
            TemporalAccessor[] vals = new TemporalAccessor[strs.length];
            for (int i = 0; i < strs.length; ++i) {
                vals[i] = TagW.TagType.DICOM_TIME.equals((Object)type) ? TagD.getDicomTime(strs[i]) : (TagW.TagType.DICOM_DATETIME.equals((Object)type) ? TagD.getDicomDateTime(strs[i]) : TagD.getDicomDate(strs[i]));
            }
            return vals;
        }
        return defaultValue;
    }

    public static void fillAttributes(Map<TagW, Object> tags, Attributes dataset) {
        if (tags != null && dataset != null) {
            ElementDictionary dic = ElementDictionary.getStandardElementDictionary();
            for (Map.Entry<TagW, Object> entry : tags.entrySet()) {
                DicomMediaUtils.fillAttributes(dataset, entry.getKey(), entry.getValue(), dic);
            }
        }
    }

    public static void fillAttributes(Iterator<Map.Entry<TagW, Object>> iter, Attributes dataset) {
        if (iter != null && dataset != null) {
            ElementDictionary dic = ElementDictionary.getStandardElementDictionary();
            while (iter.hasNext()) {
                Map.Entry<TagW, Object> entry = iter.next();
                DicomMediaUtils.fillAttributes(dataset, entry.getKey(), entry.getValue(), dic);
            }
        }
    }

    public static void fillAttributes(Attributes dataset, TagW tag, Object val, ElementDictionary dic) {
        block5: {
            int id;
            TagW.TagType type;
            block13: {
                block14: {
                    block11: {
                        block12: {
                            block9: {
                                block10: {
                                    block7: {
                                        block8: {
                                            block6: {
                                                if (dataset == null || tag == null) break block5;
                                                type = tag.getType();
                                                id = tag.getId();
                                                String key = dic.keywordOf(id);
                                                if (val == null || !StringUtil.hasLength((CharSequence)key)) {
                                                    return;
                                                }
                                                if (!tag.isStringFamilyType()) break block6;
                                                if (val instanceof String[]) {
                                                    String[] stringArray = (String[])val;
                                                    dataset.setString(id, dic.vrOf(id), stringArray);
                                                } else {
                                                    dataset.setString(id, dic.vrOf(id), val.toString());
                                                }
                                                break block5;
                                            }
                                            if (!TagW.TagType.DICOM_DATE.equals((Object)type) && !TagW.TagType.DICOM_TIME.equals((Object)type) && !TagW.TagType.DICOM_DATETIME.equals((Object)type)) break block7;
                                            if (!(val instanceof TemporalAccessor)) break block8;
                                            TemporalAccessor temporalAccessor = (TemporalAccessor)val;
                                            dataset.setDate(id, dic.vrOf(id), TagUtil.toLocalDate((TemporalAccessor)temporalAccessor));
                                            break block5;
                                        }
                                        if (!val.getClass().isArray()) break block5;
                                        dataset.setDate(id, dic.vrOf(id), TagUtil.toLocalDates((Object)val));
                                        break block5;
                                    }
                                    if (!TagW.TagType.INTEGER.equals((Object)type)) break block9;
                                    if (!(val instanceof Integer)) break block10;
                                    Integer integer = (Integer)val;
                                    dataset.setInt(id, dic.vrOf(id), integer);
                                    break block5;
                                }
                                if (!(val instanceof int[])) break block5;
                                int[] intArray = (int[])val;
                                dataset.setInt(id, dic.vrOf(id), intArray);
                                break block5;
                            }
                            if (!TagW.TagType.FLOAT.equals((Object)type)) break block11;
                            if (!(val instanceof Float)) break block12;
                            Float floatVal = (Float)val;
                            dataset.setFloat(id, dic.vrOf(id), floatVal.floatValue());
                            break block5;
                        }
                        if (!(val instanceof float[])) break block5;
                        float[] floatArray = (float[])val;
                        dataset.setFloat(id, dic.vrOf(id), floatArray);
                        break block5;
                    }
                    if (!TagW.TagType.DOUBLE.equals((Object)type)) break block13;
                    if (!(val instanceof Double)) break block14;
                    Double doubleVal = (Double)val;
                    dataset.setDouble(id, dic.vrOf(id), doubleVal);
                    break block5;
                }
                if (!(val instanceof double[])) break block5;
                double[] doubleArray = (double[])val;
                dataset.setDouble(id, dic.vrOf(id), doubleArray);
                break block5;
            }
            if (TagW.TagType.DICOM_SEQUENCE.equals((Object)type) && val instanceof Attributes[]) {
                Attributes[] sIn = (Attributes[])val;
                Sequence sOut = dataset.newSequence(id, sIn.length);
                for (Attributes attributes : sIn) {
                    sOut.add(new Attributes(attributes));
                }
            }
        }
    }

    public static double getThickness(ImageElement firstDcm, ImageElement lastDcm) {
        double p1Val = DicomMediaUtils.getSlicePositionValue((TagReadable)firstDcm);
        double p2Val = DicomMediaUtils.getSlicePositionValue((TagReadable)lastDcm);
        if (p1Val != 0.0 || p2Val != 0.0) {
            double diff = Math.abs(p2Val - p1Val);
            Double t1 = TagD.getTagValue((TagReadable)firstDcm, 1572944, Double.class);
            if (t1 != null) {
                diff += t1 / 2.0;
            }
            if ((t1 = TagD.getTagValue((TagReadable)lastDcm, 1572944, Double.class)) != null) {
                diff += t1 / 2.0;
            }
            return diff;
        }
        return 0.0;
    }

    public static class KeyObjectSelectionCode
    implements Comparable<KeyObjectSelectionCode> {
        final String resourceName;
        final String contextGroupID;
        final String codingSchemeDesignator;
        final String codeValue;
        final String codeMeaning;
        final String conceptNameCodeModifier;
        final String contextGroupIdModifier;

        public KeyObjectSelectionCode(String resourceName, String contextGroupID, String codingSchemeDesignator, String codeValue, String codeMeaning, String conceptNameCodeModifier, String contextGroupIdModifier) {
            this.resourceName = resourceName;
            this.contextGroupID = contextGroupID;
            this.codingSchemeDesignator = codingSchemeDesignator;
            this.codeValue = codeValue;
            this.codeMeaning = codeMeaning;
            this.conceptNameCodeModifier = conceptNameCodeModifier;
            this.contextGroupIdModifier = contextGroupIdModifier;
        }

        final Boolean hasConceptModifier() {
            return this.conceptNameCodeModifier != null;
        }

        @Override
        public int compareTo(KeyObjectSelectionCode o) {
            return this.codeValue.compareToIgnoreCase(o.codeValue);
        }

        public Attributes toCodeItem() {
            Attributes attrs = new Attributes(3);
            attrs.setString(524544, VR.SH, this.codeValue);
            attrs.setString(524546, VR.SH, this.codingSchemeDesignator);
            attrs.setString(524548, VR.LO, this.codeMeaning);
            return attrs;
        }
    }
}

