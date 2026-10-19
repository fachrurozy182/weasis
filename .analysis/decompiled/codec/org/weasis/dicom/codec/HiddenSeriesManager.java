/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.media.data.MediaElement
 *  org.weasis.core.api.media.data.MediaSeries
 *  org.weasis.core.api.media.data.MediaSeries$MEDIA_POSITION
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.util.ResourceUtil$ResourceIconPath
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.codec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.HiddenSpecialElement;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.seg.LazyContourLoader;
import org.weasis.dicom.macro.SOPInstanceReference;

public class HiddenSeriesManager {
    private static final HiddenSeriesManager instance = new HiddenSeriesManager();
    public final Map<String, Set<HiddenSpecialElement>> series2Elements = new ConcurrentHashMap<String, Set<HiddenSpecialElement>>();
    public final Map<String, Set<String>> patient2Series = new ConcurrentHashMap<String, Set<String>>();
    public final Map<String, Set<String>> reference2Series = new ConcurrentHashMap<String, Set<String>>();
    public final Map<String, Set<String>> sopRef2Series = new ConcurrentHashMap<String, Set<String>>();

    public static HiddenSeriesManager getInstance() {
        return instance;
    }

    public void extractReferencedSeries(Attributes dicom, String originSeriesUID) {
        this.extractReferencedSeries(dicom, originSeriesUID, null);
    }

    public void extractReferencedSeries(Attributes dicom, String originSeriesUID, Function<String, Map<String, Set<LazyContourLoader>>> addSeries) {
        if (dicom == null || !StringUtil.hasText((CharSequence)originSeriesUID)) {
            return;
        }
        Sequence seriesRef = dicom.getSequence(528661);
        if (seriesRef != null) {
            for (Attributes ref : seriesRef) {
                String seriesUID = ref.getString(0x20000E);
                if (!StringUtil.hasText((CharSequence)seriesUID)) continue;
                this.reference2Series.computeIfAbsent(seriesUID, string -> new CopyOnWriteArraySet()).add(originSeriesUID);
                if (addSeries == null) continue;
                Map<String, Set<LazyContourLoader>> refMap = addSeries.apply(seriesUID);
                Sequence instanceSeq = ref.getSequence(528714);
                if (instanceSeq == null) continue;
                for (Attributes instance : instanceSeq) {
                    String sopInstanceUID = instance.getString(528725);
                    if (!StringUtil.hasText((CharSequence)sopInstanceUID)) continue;
                    refMap.computeIfAbsent(sopInstanceUID, string -> new LinkedHashSet());
                }
            }
        }
    }

    public static void addSourceImage(Attributes derivation, Map<String, List<Integer>> sopUIDToFramesMap) {
        if (derivation == null || sopUIDToFramesMap == null) {
            return;
        }
        Sequence srcSeq = derivation.getSequence(532754);
        if (srcSeq != null) {
            for (Attributes src : srcSeq) {
                SOPInstanceReference sopRef = new SOPInstanceReference(src);
                int[] frames = sopRef.getReferencedFrameNumber();
                String sopInstanceUID = src.getString(528725);
                if (sopInstanceUID == null) continue;
                if (frames == null || frames.length == 0) {
                    sopUIDToFramesMap.put(sopInstanceUID, Collections.emptyList());
                    continue;
                }
                sopUIDToFramesMap.put(sopInstanceUID, HiddenSeriesManager.toIntegerList(frames));
            }
        }
    }

    private static List<Integer> toIntegerList(int[] array) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int value : array) {
            list.add(value);
        }
        return list;
    }

    public static void addReferencedSOPInstanceUID(Attributes seriesRef, String originSeriesUID) {
        if (seriesRef == null || originSeriesUID == null) {
            return;
        }
        String structUID = seriesRef.getString(528725);
        if (StringUtil.hasText((CharSequence)structUID)) {
            HiddenSeriesManager.getInstance().sopRef2Series.computeIfAbsent(structUID, string -> new CopyOnWriteArraySet()).add(originSeriesUID);
        }
    }

    public static <E> boolean hasHiddenElementsFromSeries(Class<E> clazz, String ... seriesUID) {
        if (clazz != null && clazz.isAssignableFrom(clazz)) {
            for (String uid : seriesUID) {
                if (!HiddenSeriesManager.hasHiddenElements(clazz, uid)) continue;
                return true;
            }
        }
        return false;
    }

    private static <E> boolean hasHiddenElements(Class<E> clazz, String seriesUID) {
        Set<HiddenSpecialElement> hiddenElements;
        if (StringUtil.hasText((CharSequence)seriesUID) && (hiddenElements = HiddenSeriesManager.getInstance().series2Elements.get(seriesUID)) != null) {
            for (HiddenSpecialElement media : hiddenElements) {
                if (!clazz.isInstance(media)) continue;
                return true;
            }
        }
        return false;
    }

    private static <E> void addHiddenElements(List<E> list, Class<E> clazz, String seriesUID, int childNumber) {
        Set<HiddenSpecialElement> hiddenElements;
        if (StringUtil.hasText((CharSequence)seriesUID) && (hiddenElements = HiddenSeriesManager.getInstance().series2Elements.get(seriesUID)) != null) {
            for (HiddenSpecialElement media : hiddenElements) {
                String sopUID;
                Set<String> refs;
                if (!clazz.isInstance(media) || list.contains(media)) continue;
                list.add(media);
                if (childNumber <= 0 || (refs = HiddenSeriesManager.getInstance().sopRef2Series.get(sopUID = TagD.getTagValue((TagReadable)media, 524312, String.class))) == null) continue;
                for (String ref : refs) {
                    HiddenSeriesManager.addHiddenElements(list, clazz, ref, childNumber - 1);
                }
            }
        }
    }

    public static <E> List<E> getHiddenElementsFromSeries(Class<E> clazz, String ... seriesUID) {
        if (clazz != null && clazz.isAssignableFrom(clazz)) {
            ArrayList list = new ArrayList();
            for (String uid : seriesUID) {
                HiddenSeriesManager.addHiddenElements(list, clazz, uid, 2);
            }
            return list;
        }
        return Collections.emptyList();
    }

    public static <E> boolean hasHiddenSpecialElements(Class<E> clazz, MediaSeriesGroup patient) {
        String patientPseudoUID;
        Set<String> patients;
        if (patient != null && clazz != null && clazz.isAssignableFrom(clazz) && (patients = HiddenSeriesManager.getInstance().patient2Series.get(patientPseudoUID = (String)patient.getTagValue(TagW.PatientPseudoUID))) != null) {
            for (String seriesUID : patients) {
                if (!HiddenSeriesManager.hasHiddenElements(clazz, seriesUID)) continue;
                return true;
            }
        }
        return false;
    }

    public static <E> List<E> getHiddenElementsFromPatient(Class<E> clazz, MediaSeriesGroup patient) {
        if (patient != null) {
            String patientPseudoUID = (String)patient.getTagValue(TagW.PatientPseudoUID);
            return HiddenSeriesManager.getHiddenElementsFromPatient(clazz, patientPseudoUID);
        }
        return Collections.emptyList();
    }

    public static <E> List<E> getHiddenElementsFromPatient(Class<E> clazz, String patientPseudoUID) {
        Set<String> patients;
        if (StringUtil.hasText((CharSequence)patientPseudoUID) && clazz != null && clazz.isAssignableFrom(clazz) && (patients = HiddenSeriesManager.getInstance().patient2Series.get(patientPseudoUID)) != null) {
            ArrayList list = new ArrayList();
            for (String seriesUID : patients) {
                HiddenSeriesManager.addHiddenElements(list, clazz, seriesUID, 2);
            }
            return list;
        }
        return Collections.emptyList();
    }

    private static String resolvePatientPseudoUID(MediaSeries<?> series) {
        if (series == null) {
            return null;
        }
        Object first = series.getMedia(MediaSeries.MEDIA_POSITION.FIRST, null, null);
        if (first instanceof MediaElement) {
            MediaElement element = (MediaElement)first;
            return (String)element.getTagValue(TagW.PatientPseudoUID);
        }
        return null;
    }

    public static <E> boolean hasHiddenElementsByFrameOfReference(Class<E> clazz, MediaSeries<?> series) {
        if (clazz == null || series == null) {
            return false;
        }
        String frameOfRef = TagD.getTagValue(series, 0x200052, String.class);
        if (!StringUtil.hasText((CharSequence)frameOfRef)) {
            return false;
        }
        String patientPseudoUID = HiddenSeriesManager.resolvePatientPseudoUID(series);
        if (!StringUtil.hasText((CharSequence)patientPseudoUID)) {
            return false;
        }
        Set<String> patientSeries = HiddenSeriesManager.getInstance().patient2Series.get(patientPseudoUID);
        if (patientSeries == null) {
            return false;
        }
        String selfSeriesUID = TagD.getTagValue(series, 0x20000E, String.class);
        for (String segSeriesUID : patientSeries) {
            Set<HiddenSpecialElement> hiddenElements;
            if (segSeriesUID.equals(selfSeriesUID) || (hiddenElements = HiddenSeriesManager.getInstance().series2Elements.get(segSeriesUID)) == null) continue;
            for (HiddenSpecialElement e : hiddenElements) {
                if (!clazz.isInstance(e) || !frameOfRef.equals(TagD.getTagValue((TagReadable)e.getMediaReader().getMediaSeries(), 0x200052, String.class))) continue;
                return true;
            }
        }
        return false;
    }

    public static <E> List<E> getHiddenElementsByFrameOfReference(Class<E> clazz, MediaSeries<?> series) {
        if (clazz == null || series == null) {
            return Collections.emptyList();
        }
        String frameOfRef = TagD.getTagValue(series, 0x200052, String.class);
        if (!StringUtil.hasText((CharSequence)frameOfRef)) {
            return Collections.emptyList();
        }
        String patientPseudoUID = HiddenSeriesManager.resolvePatientPseudoUID(series);
        if (!StringUtil.hasText((CharSequence)patientPseudoUID)) {
            return Collections.emptyList();
        }
        Set<String> patientSeries = HiddenSeriesManager.getInstance().patient2Series.get(patientPseudoUID);
        if (patientSeries == null) {
            return Collections.emptyList();
        }
        String selfSeriesUID = TagD.getTagValue(series, 0x20000E, String.class);
        ArrayList<HiddenSpecialElement> result = new ArrayList<HiddenSpecialElement>();
        for (String segSeriesUID : patientSeries) {
            Set<HiddenSpecialElement> hiddenElements;
            if (segSeriesUID.equals(selfSeriesUID) || (hiddenElements = HiddenSeriesManager.getInstance().series2Elements.get(segSeriesUID)) == null) continue;
            for (HiddenSpecialElement e : hiddenElements) {
                if (!clazz.isInstance(e) || !frameOfRef.equals(TagD.getTagValue((TagReadable)e.getMediaReader().getMediaSeries(), 0x200052, String.class)) || result.contains(e)) continue;
                result.add(e);
            }
        }
        return result;
    }

    public static Set<ResourceUtil.ResourceIconPath> getRelatedIcons(String seriesUID) {
        Set<String> list;
        if (StringUtil.hasText((CharSequence)seriesUID) && (list = HiddenSeriesManager.getInstance().reference2Series.get(seriesUID)) != null && !list.isEmpty()) {
            LinkedHashSet<ResourceUtil.ResourceIconPath> icons = new LinkedHashSet<ResourceUtil.ResourceIconPath>();
            for (String uid : list) {
                Set<HiddenSpecialElement> hiddenElements = HiddenSeriesManager.getInstance().series2Elements.get(uid);
                if (hiddenElements == null) continue;
                for (HiddenSpecialElement media : hiddenElements) {
                    ResourceUtil.ResourceIconPath path = media.getIconPath();
                    if (path == null) continue;
                    icons.add(path);
                }
            }
            return icons;
        }
        return Collections.emptySet();
    }
}

