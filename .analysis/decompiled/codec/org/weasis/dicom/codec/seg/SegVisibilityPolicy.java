/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.service.WProperties
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.codec.seg;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.dcm4che3.data.Attributes;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.service.WProperties;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.DicomMediaIO;
import org.weasis.dicom.codec.HiddenSeriesManager;
import org.weasis.dicom.codec.HiddenSpecialElement;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.seg.SegSpecialElement;

public final class SegVisibilityPolicy {
    public static final String HIDE_ALL = "weasis.dicom.seg.hide.all";
    public static final String HIDE_KEYWORDS = "weasis.dicom.seg.hide.keywords";
    public static final String DEFAULT_HIDE_KEYWORDS = "table removal, table segmentation, tabletop, couch, patient support, bed removal";
    public static final String HIDE_COUNT = "weasis.dicom.seg.hide.count";
    public static final int DEFAULT_HIDE_COUNT = 3;
    private static final Map<String, Map<String, AnnouncedSegSeries>> ANNOUNCED = new ConcurrentHashMap<String, Map<String, AnnouncedSegSeries>>();
    private static final AtomicLong GENERATION = new AtomicLong();

    private SegVisibilityPolicy() {
    }

    public static long generation() {
        return GENERATION.get();
    }

    public static void invalidate() {
        GENERATION.incrementAndGet();
    }

    public static void announce(String patientPseudoUID, String seriesUID, String seriesDescription, int instances) {
        if (!StringUtil.hasText((CharSequence)patientPseudoUID) || !StringUtil.hasText((CharSequence)seriesUID) || instances <= 0) {
            return;
        }
        ANNOUNCED.computeIfAbsent(patientPseudoUID, string -> new ConcurrentHashMap()).put(seriesUID, new AnnouncedSegSeries(seriesDescription, instances));
        SegVisibilityPolicy.invalidate();
    }

    public static void forgetSeries(String patientPseudoUID, String seriesUID) {
        Map<String, AnnouncedSegSeries> announced;
        Map<String, AnnouncedSegSeries> map = announced = patientPseudoUID == null ? null : ANNOUNCED.get(patientPseudoUID);
        if (announced != null && seriesUID != null && announced.remove(seriesUID) != null) {
            SegVisibilityPolicy.invalidate();
        }
    }

    public static void forgetPatient(String patientPseudoUID) {
        if (patientPseudoUID != null && ANNOUNCED.remove(patientPseudoUID) != null) {
            SegVisibilityPolicy.invalidate();
        }
    }

    public static void renamePatient(String oldPatientPseudoUID, String newPatientPseudoUID) {
        Map<String, AnnouncedSegSeries> announced;
        Map<String, AnnouncedSegSeries> map = announced = oldPatientPseudoUID == null ? null : ANNOUNCED.remove(oldPatientPseudoUID);
        if (announced == null || announced.isEmpty() || !StringUtil.hasText((CharSequence)newPatientPseudoUID)) {
            return;
        }
        ANNOUNCED.computeIfAbsent(newPatientPseudoUID, string -> new ConcurrentHashMap()).putAll(announced);
        SegVisibilityPolicy.invalidate();
    }

    public static boolean isHiddenByDefault(SegSpecialElement seg) {
        if (seg == null) {
            return false;
        }
        WProperties prefs = GuiUtils.getUICore().getSystemPreferences();
        if (prefs.getBooleanProperty(HIDE_ALL, false)) {
            return true;
        }
        List<String> keywords = SegVisibilityPolicy.hideKeywords(prefs.getProperty(HIDE_KEYWORDS, DEFAULT_HIDE_KEYWORDS));
        return SegVisibilityPolicy.matchesHideKeywords(seg, keywords) || SegVisibilityPolicy.isCrowded(seg, prefs, keywords);
    }

    private static boolean matchesHideKeywords(SegSpecialElement seg, List<String> keywords) {
        if (keywords.isEmpty()) {
            return false;
        }
        Attributes dicom = SegVisibilityPolicy.dicomObject(seg);
        if (dicom != null && (SegVisibilityPolicy.matchesAny(dicom.getString(528446), keywords) || SegVisibilityPolicy.matchesAny(dicom.getString(7340161), keywords) || SegVisibilityPolicy.matchesAny(dicom.getString(0x700080), keywords))) {
            return true;
        }
        return !seg.getSegAttributes().isEmpty() && seg.getSegAttributes().values().stream().allMatch(r -> SegVisibilityPolicy.matchesAny(r.getLabel(), keywords) || SegVisibilityPolicy.matchesAny(r.getDescription(), keywords) || SegVisibilityPolicy.matchesAny(r.getAlgorithmName(), keywords));
    }

    private static boolean isCrowded(SegSpecialElement seg, WProperties prefs, List<String> keywords) {
        Collection<String> seriesUIDs;
        int threshold = prefs.getIntProperty(HIDE_COUNT, 3);
        if (threshold <= 0) {
            return false;
        }
        HiddenSeriesManager manager = HiddenSeriesManager.getInstance();
        String patientUID = (String)seg.getTagValue(TagW.PatientPseudoUID);
        Map<String, AnnouncedSegSeries> announced = SegVisibilityPolicy.announcedOf(patientUID);
        return SegVisibilityPolicy.countCrowding(manager.series2Elements, announced, seriesUIDs = SegVisibilityPolicy.crowdingSeries(manager, seg, patientUID, announced), keywords) >= (long)threshold;
    }

    static Map<String, AnnouncedSegSeries> announcedOf(String patientPseudoUID) {
        Map<String, AnnouncedSegSeries> announced = patientPseudoUID == null ? null : ANNOUNCED.get(patientPseudoUID);
        return announced == null ? Map.of() : announced;
    }

    private static Collection<String> crowdingSeries(HiddenSeriesManager manager, SegSpecialElement seg, String patientUID, Map<String, AnnouncedSegSeries> announced) {
        Set<String> loaded;
        Set<String> set = loaded = patientUID == null ? null : manager.patient2Series.get(patientUID);
        if (loaded == null) {
            String seriesUID = TagD.getTagValue((TagReadable)seg, 0x20000E, String.class);
            Set<Object> set2 = loaded = StringUtil.hasText((CharSequence)seriesUID) ? Set.of(seriesUID) : Set.of();
        }
        if (announced.isEmpty()) {
            return loaded;
        }
        HashSet<String> union = new HashSet<String>(loaded);
        union.addAll(announced.keySet());
        return union;
    }

    static long countCrowding(Map<String, Set<HiddenSpecialElement>> series2Elements, Map<String, AnnouncedSegSeries> announced, Collection<String> seriesUIDs, List<String> keywords) {
        long total = 0L;
        for (String seriesUID : seriesUIDs) {
            Set<HiddenSpecialElement> elements = series2Elements.get(seriesUID);
            AnnouncedSegSeries series = announced.get(seriesUID);
            long promised = series == null || SegVisibilityPolicy.matchesAny(series.description(), keywords) ? 0L : (long)series.instances();
            total += Math.max(elements == null ? 0L : SegVisibilityPolicy.countCrowding(elements, keywords), promised);
        }
        return total;
    }

    static long countCrowding(Collection<? extends HiddenSpecialElement> elements, List<String> keywords) {
        return elements.stream().filter(SegSpecialElement.class::isInstance).map(SegSpecialElement.class::cast).filter(seg -> !SegVisibilityPolicy.matchesHideKeywords(seg, keywords)).count();
    }

    private static Attributes dicomObject(SegSpecialElement seg) {
        DicomMediaIO reader = seg.getMediaReader();
        return reader == null ? null : reader.getDicomObject();
    }

    static List<String> hideKeywords(String pref) {
        if (!StringUtil.hasText((CharSequence)pref)) {
            return List.of();
        }
        return Arrays.stream(pref.split(",")).map(SegVisibilityPolicy::normalize).filter(StringUtil::hasText).toList();
    }

    static boolean matchesAny(String value, List<String> keywords) {
        if (!StringUtil.hasText((CharSequence)value)) {
            return false;
        }
        String normalized = SegVisibilityPolicy.normalize(value);
        return keywords.stream().anyMatch(normalized::contains);
    }

    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{Nd}]", "");
    }

    record AnnouncedSegSeries(String description, int instances) {
    }
}

