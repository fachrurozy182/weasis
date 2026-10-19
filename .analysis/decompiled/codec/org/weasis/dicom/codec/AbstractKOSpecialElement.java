/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.gui.util.Filter
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.util.ResourceUtil$OtherIcon
 *  org.weasis.core.api.util.ResourceUtil$ResourceIconPath
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.codec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.dcm4che3.data.Attributes;
import org.weasis.core.api.gui.util.Filter;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.DicomMediaIO;
import org.weasis.dicom.codec.DicomSpecialElement;
import org.weasis.dicom.codec.HiddenSpecialElement;
import org.weasis.dicom.codec.KOSpecialElement;
import org.weasis.dicom.codec.RejectedKOSpecialElement;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.macro.HierarchicalSOPInstanceReference;
import org.weasis.dicom.macro.KODocumentModule;
import org.weasis.dicom.macro.SOPInstanceReferenceAndMAC;
import org.weasis.dicom.macro.SeriesAndInstanceReference;

public class AbstractKOSpecialElement
extends HiddenSpecialElement {
    protected Map<String, Map<String, SOPInstanceReferenceAndMAC>> sopInstanceReferenceMapBySeriesUID;
    protected Map<String, Map<String, SeriesAndInstanceReference>> seriesAndInstanceReferenceMapByStudyUID;
    protected Map<String, HierarchicalSOPInstanceReference> hierarchicalSOPInstanceReferenceByStudyUID;

    public AbstractKOSpecialElement(DicomMediaIO mediaIO) {
        super(mediaIO);
    }

    @Override
    protected void initLabel() {
        StringBuilder buf = new StringBuilder(this.getLabelPrefix());
        String name = this.getLabelWithoutPrefix();
        if (name != null) {
            buf.append(name);
        }
        this.label = buf.toString();
    }

    @Override
    public ResourceUtil.ResourceIconPath getIconPath() {
        return ResourceUtil.OtherIcon.KEY;
    }

    protected String getLabelWithoutPrefix() {
        Attributes dicom = ((DicomMediaIO)this.mediaIO).getDicomObject();
        Attributes item = dicom.getNestedDataset(4237104);
        if (item != null) {
            return item.getString(4235616);
        }
        return null;
    }

    public Set<String> getReferencedStudyInstanceUIDSet() {
        if (this.hierarchicalSOPInstanceReferenceByStudyUID == null) {
            this.updateHierarchicalSOPInstanceReference();
        }
        return this.hierarchicalSOPInstanceReferenceByStudyUID.keySet();
    }

    public boolean containsStudyInstanceUIDReference(String studyInstanceUIDReference) {
        if (this.hierarchicalSOPInstanceReferenceByStudyUID == null) {
            this.updateHierarchicalSOPInstanceReference();
        }
        return this.hierarchicalSOPInstanceReferenceByStudyUID.containsKey(studyInstanceUIDReference);
    }

    public Set<String> getReferencedSeriesInstanceUIDSet(String studyUID) {
        Map<String, SeriesAndInstanceReference> seriesAndInstanceReferenceBySeriesUID;
        if (this.seriesAndInstanceReferenceMapByStudyUID == null) {
            this.updateHierarchicalSOPInstanceReference();
        }
        return (seriesAndInstanceReferenceBySeriesUID = this.seriesAndInstanceReferenceMapByStudyUID.get(studyUID)) != null ? this.seriesAndInstanceReferenceMapByStudyUID.get(studyUID).keySet() : null;
    }

    public Set<String> getReferencedSeriesInstanceUIDSet() {
        if (this.sopInstanceReferenceMapBySeriesUID == null) {
            this.updateHierarchicalSOPInstanceReference();
        }
        return this.sopInstanceReferenceMapBySeriesUID.keySet();
    }

    public boolean containsSeriesInstanceUIDReference(String seriesInstanceUIDReference) {
        if (this.sopInstanceReferenceMapBySeriesUID == null) {
            this.updateHierarchicalSOPInstanceReference();
        }
        return this.sopInstanceReferenceMapBySeriesUID.containsKey(seriesInstanceUIDReference);
    }

    public Set<String> getReferencedSOPInstanceUIDSet() {
        if (this.sopInstanceReferenceMapBySeriesUID == null) {
            this.updateHierarchicalSOPInstanceReference();
        }
        LinkedHashSet<String> referencedSOPInstanceUIDSet = new LinkedHashSet<String>();
        for (Map<String, SOPInstanceReferenceAndMAC> sopInstanceReference : this.sopInstanceReferenceMapBySeriesUID.values()) {
            referencedSOPInstanceUIDSet.addAll(sopInstanceReference.keySet());
        }
        return referencedSOPInstanceUIDSet;
    }

    public Set<String> getReferencedSOPInstanceUIDSet(String seriesUID) {
        Map<String, SOPInstanceReferenceAndMAC> sopInstanceReferenceBySOPInstanceUID;
        if (seriesUID == null) {
            return this.getReferencedSOPInstanceUIDSet();
        }
        if (this.sopInstanceReferenceMapBySeriesUID == null) {
            this.updateHierarchicalSOPInstanceReference();
        }
        return (sopInstanceReferenceBySOPInstanceUID = this.sopInstanceReferenceMapBySeriesUID.get(seriesUID)) != null ? sopInstanceReferenceBySOPInstanceUID.keySet() : null;
    }

    public Map<String, SOPInstanceReferenceAndMAC> getReferencedSOPInstanceUIDObject(String seriesUID) {
        if (seriesUID == null) {
            return null;
        }
        if (this.sopInstanceReferenceMapBySeriesUID == null) {
            this.updateHierarchicalSOPInstanceReference();
        }
        return this.sopInstanceReferenceMapBySeriesUID.get(seriesUID);
    }

    public int getNumberSelectedImages() {
        return this.sopInstanceReferenceMapBySeriesUID.values().stream().mapToInt(m -> m.values().stream().mapToInt(v -> {
            int[] frames = v.getReferencedFrameNumber();
            if (frames == null || frames.length == 0) {
                return 1;
            }
            return frames.length;
        }).sum()).sum();
    }

    public boolean containsSopInstanceUIDReference(String seriesInstanceUID, String sopInstanceUIDReference, Integer frame) {
        return AbstractKOSpecialElement.isSopuidInReferencedSeriesSequence(this.getReferencedSOPInstanceUIDObject(seriesInstanceUID), sopInstanceUIDReference, frame);
    }

    public boolean isEmpty() {
        if (this.sopInstanceReferenceMapBySeriesUID == null) {
            this.updateHierarchicalSOPInstanceReference();
        }
        return this.sopInstanceReferenceMapBySeriesUID.isEmpty();
    }

    protected void updateHierarchicalSOPInstanceReference() {
        this.init();
        Attributes dcmItems = this.getMediaReader().getDicomObject();
        if (dcmItems != null) {
            Collection<HierarchicalSOPInstanceReference> referencedStudySequence = HierarchicalSOPInstanceReference.toHierarchicalSOPInstanceReferenceMacros(dcmItems.getSequence(4236149));
            boolean sopInstanceExist = false;
            for (HierarchicalSOPInstanceReference studyRef : referencedStudySequence) {
                Collection<SeriesAndInstanceReference> referencedSeriesSequence = studyRef.getReferencedSeries();
                String studyUID = studyRef.getStudyInstanceUID();
                for (SeriesAndInstanceReference seriesRef : referencedSeriesSequence) {
                    Collection<SOPInstanceReferenceAndMAC> referencedSOPInstanceSequence = seriesRef.getReferencedSOPInstances();
                    String seriesUID = seriesRef.getSeriesInstanceUID();
                    for (SOPInstanceReferenceAndMAC sopRef : referencedSOPInstanceSequence) {
                        String sopInstanceUID = sopRef.getReferencedSOPInstanceUID();
                        if (!StringUtil.hasText((CharSequence)sopInstanceUID)) continue;
                        Map sopInstanceReferenceBySOPInstanceUID = this.sopInstanceReferenceMapBySeriesUID.computeIfAbsent(seriesUID, k -> new LinkedHashMap());
                        sopInstanceReferenceBySOPInstanceUID.put(sopInstanceUID, sopRef);
                        sopInstanceExist = true;
                    }
                    if (!sopInstanceExist) continue;
                    Map seriesAndInstanceReferenceBySeriesUID = this.seriesAndInstanceReferenceMapByStudyUID.computeIfAbsent(studyUID, k -> new LinkedHashMap());
                    seriesAndInstanceReferenceBySeriesUID.put(seriesUID, seriesRef);
                }
                if (!sopInstanceExist) continue;
                this.hierarchicalSOPInstanceReferenceByStudyUID.put(studyUID, studyRef);
            }
        }
    }

    private void init() {
        if (this.hierarchicalSOPInstanceReferenceByStudyUID == null) {
            this.hierarchicalSOPInstanceReferenceByStudyUID = new LinkedHashMap<String, HierarchicalSOPInstanceReference>();
        }
        if (this.seriesAndInstanceReferenceMapByStudyUID == null) {
            this.seriesAndInstanceReferenceMapByStudyUID = new LinkedHashMap<String, Map<String, SeriesAndInstanceReference>>();
        }
        if (this.sopInstanceReferenceMapBySeriesUID == null) {
            this.sopInstanceReferenceMapBySeriesUID = new LinkedHashMap<String, Map<String, SOPInstanceReferenceAndMAC>>();
        }
    }

    public boolean addKeyObject(DicomImageElement dicomImage) {
        return this.addKeyObject(new Reference(dicomImage));
    }

    public boolean addKeyObject(Reference ref) {
        Map<String, SOPInstanceReferenceAndMAC> sopInstanceReferenceBySOPInstanceUID;
        if (this.hierarchicalSOPInstanceReferenceByStudyUID == null) {
            this.updateHierarchicalSOPInstanceReference();
        }
        if ((sopInstanceReferenceBySOPInstanceUID = this.sopInstanceReferenceMapBySeriesUID.get(ref.seriesInstanceUID)) == null) {
            sopInstanceReferenceBySOPInstanceUID = new LinkedHashMap<String, SOPInstanceReferenceAndMAC>();
            this.sopInstanceReferenceMapBySeriesUID.put(ref.seriesInstanceUID, sopInstanceReferenceBySOPInstanceUID);
        } else if (sopInstanceReferenceBySOPInstanceUID.containsKey(ref.sopInstanceUID)) {
            int[] seqFrame;
            if (ref.frameList == null || ref.frameList.isEmpty()) {
                return false;
            }
            SOPInstanceReferenceAndMAC val = sopInstanceReferenceBySOPInstanceUID.get(ref.sopInstanceUID);
            int[] nArray = seqFrame = val == null ? null : val.getReferencedFrameNumber();
            if (seqFrame != null && seqFrame.length != 0) {
                val.setReferencedFrameNumber(AbstractKOSpecialElement.mergeFrames(seqFrame, ref.frameList));
                return true;
            }
        }
        SOPInstanceReferenceAndMAC referencedSOP = new SOPInstanceReferenceAndMAC();
        referencedSOP.setReferencedSOPInstanceUID(ref.sopInstanceUID);
        referencedSOP.setReferencedSOPClassUID(ref.sopClassUID);
        referencedSOP.setInstanceNumber(ref.getInstanceNumber());
        referencedSOP.setReferencedFrameNumber(ref.frameList.stream().mapToInt(i -> i).toArray());
        sopInstanceReferenceBySOPInstanceUID.put(ref.sopInstanceUID, referencedSOP);
        Map seriesAndInstanceReferenceBySeriesUID = this.seriesAndInstanceReferenceMapByStudyUID.computeIfAbsent(ref.studyInstanceUID, k -> new LinkedHashMap());
        SeriesAndInstanceReference referencedSeries = (SeriesAndInstanceReference)seriesAndInstanceReferenceBySeriesUID.get(ref.seriesInstanceUID);
        if (referencedSeries == null) {
            referencedSeries = new SeriesAndInstanceReference();
            referencedSeries.setSeriesInstanceUID(ref.seriesInstanceUID);
            seriesAndInstanceReferenceBySeriesUID.put(ref.seriesInstanceUID, referencedSeries);
        }
        referencedSeries.setReferencedSOPInstances(sopInstanceReferenceBySOPInstanceUID.values());
        HierarchicalSOPInstanceReference hierarchicalDicom = this.hierarchicalSOPInstanceReferenceByStudyUID.get(ref.studyInstanceUID);
        if (hierarchicalDicom == null) {
            hierarchicalDicom = new HierarchicalSOPInstanceReference();
            hierarchicalDicom.setStudyInstanceUID(ref.studyInstanceUID);
            this.hierarchicalSOPInstanceReferenceByStudyUID.put(ref.studyInstanceUID, hierarchicalDicom);
        }
        hierarchicalDicom.setReferencedSeries(seriesAndInstanceReferenceBySeriesUID.values());
        Attributes dcmItems = this.getMediaReader().getDicomObject();
        new KODocumentModule(dcmItems).setCurrentRequestedProcedureEvidences(this.hierarchicalSOPInstanceReferenceByStudyUID.values());
        return true;
    }

    public boolean removeKeyObject(DicomImageElement dicomImage) {
        return this.removeKeyObject(new Reference(dicomImage));
    }

    public boolean removeKeyObject(Reference ref) {
        if (this.hierarchicalSOPInstanceReferenceByStudyUID == null) {
            this.updateHierarchicalSOPInstanceReference();
        }
        Map<String, SeriesAndInstanceReference> seriesAndInstanceReferenceBySeriesUID = this.seriesAndInstanceReferenceMapByStudyUID.get(ref.studyInstanceUID);
        Map<String, SOPInstanceReferenceAndMAC> sopInstanceReferenceBySOPInstanceUID = this.sopInstanceReferenceMapBySeriesUID.get(ref.seriesInstanceUID);
        if (sopInstanceReferenceBySOPInstanceUID == null || seriesAndInstanceReferenceBySeriesUID == null || !sopInstanceReferenceBySOPInstanceUID.containsKey(ref.sopInstanceUID)) {
            return false;
        }
        if (ref.frameList != null && !ref.frameList.isEmpty()) {
            int[] seqFrame;
            SOPInstanceReferenceAndMAC val = sopInstanceReferenceBySOPInstanceUID.get(ref.sopInstanceUID);
            int[] nArray = seqFrame = val == null ? null : val.getReferencedFrameNumber();
            if (seqFrame != null && seqFrame.length != 0) {
                int[] frames = AbstractKOSpecialElement.removeFrames(seqFrame, ref.frameList);
                val.setReferencedFrameNumber(frames);
                if (frames.length > 0) {
                    return true;
                }
            }
        }
        sopInstanceReferenceBySOPInstanceUID.remove(ref.sopInstanceUID);
        if (sopInstanceReferenceBySOPInstanceUID.isEmpty()) {
            this.sopInstanceReferenceMapBySeriesUID.remove(ref.seriesInstanceUID);
            seriesAndInstanceReferenceBySeriesUID.remove(ref.seriesInstanceUID);
            if (seriesAndInstanceReferenceBySeriesUID.isEmpty()) {
                this.seriesAndInstanceReferenceMapByStudyUID.remove(ref.studyInstanceUID);
                this.hierarchicalSOPInstanceReferenceByStudyUID.remove(ref.studyInstanceUID);
            } else {
                HierarchicalSOPInstanceReference hierarchicalDicom = this.hierarchicalSOPInstanceReferenceByStudyUID.get(ref.studyInstanceUID);
                hierarchicalDicom.setReferencedSeries(seriesAndInstanceReferenceBySeriesUID.values());
            }
        } else {
            SeriesAndInstanceReference referencedSeries = seriesAndInstanceReferenceBySeriesUID.get(ref.seriesInstanceUID);
            referencedSeries.setReferencedSOPInstances(sopInstanceReferenceBySOPInstanceUID.values());
        }
        Attributes dcmItems = this.getMediaReader().getDicomObject();
        ArrayList<HierarchicalSOPInstanceReference> referencedStudies = null;
        if (!this.hierarchicalSOPInstanceReferenceByStudyUID.isEmpty()) {
            referencedStudies = new ArrayList<HierarchicalSOPInstanceReference>(this.hierarchicalSOPInstanceReferenceByStudyUID.values());
        }
        new KODocumentModule(dcmItems).setCurrentRequestedProcedureEvidences(referencedStudies);
        return true;
    }

    public Filter<DicomImageElement> getSOPInstanceUIDFilter() {
        return new Filter<DicomImageElement>(this){
            final /* synthetic */ AbstractKOSpecialElement this$0;
            {
                AbstractKOSpecialElement abstractKOSpecialElement = this$0;
                Objects.requireNonNull(abstractKOSpecialElement);
                this.this$0 = abstractKOSpecialElement;
            }

            public boolean passes(DicomImageElement dicom) {
                String seriesInstanceUID = TagD.getTagValue((TagReadable)dicom, 0x20000E, String.class);
                if (dicom == null || seriesInstanceUID == null) {
                    return false;
                }
                String sopInstanceUID = TagD.getTagValue((TagReadable)dicom, 524312, String.class);
                Integer frame = TagD.getTagValue((TagReadable)dicom, 2097171, Integer.class);
                return DicomSpecialElement.isSopuidInReferencedSeriesSequence(this.this$0.getReferencedSOPInstanceUIDObject(seriesInstanceUID), sopInstanceUID, frame);
            }
        };
    }

    public String getDocumentTitle() {
        Attributes item;
        Attributes dcmItems = this.getMediaReader().getDicomObject();
        if (dcmItems != null && (item = dcmItems.getNestedDataset(4235331)) != null) {
            return item.getString(524548, null);
        }
        return null;
    }

    private static int[] mergeFrames(int[] arr1, List<Integer> frameList) {
        HashSet<Integer> result = new HashSet<Integer>();
        for (int i2 : arr1) {
            result.add(i2);
        }
        result.addAll(frameList);
        return result.stream().mapToInt(i -> i).toArray();
    }

    private static int[] removeFrames(int[] arr1, List<Integer> frameList) {
        HashSet<Integer> result = new HashSet<Integer>();
        for (int i2 : arr1) {
            result.add(i2);
        }
        Object object = frameList.iterator();
        while (object.hasNext()) {
            Integer integer = (Integer)object.next();
            result.remove(integer);
        }
        return result.stream().mapToInt(i -> i).toArray();
    }

    public static Collection<KOSpecialElement> getKoSpecialElements(Collection<KOSpecialElement> specialElements, String seriesUID) {
        if (specialElements == null) {
            return Collections.emptySet();
        }
        TreeSet<KOSpecialElement> koElementSet = null;
        for (KOSpecialElement koElement : specialElements) {
            Set<String> referencedSeriesInstanceUIDSet = koElement.getReferencedSeriesInstanceUIDSet();
            if (seriesUID != null && !referencedSeriesInstanceUIDSet.contains(seriesUID) && !koElement.getMediaReader().isEditableDicom()) continue;
            if (koElementSet == null) {
                koElementSet = new TreeSet<KOSpecialElement>((Comparator<KOSpecialElement>)ORDER_BY_DATE);
            }
            koElementSet.add(koElement);
        }
        return koElementSet == null ? Collections.emptySet() : koElementSet;
    }

    public static Collection<RejectedKOSpecialElement> getRejectionKoSpecialElements(Collection<RejectedKOSpecialElement> specialElements, String seriesUID) {
        if (specialElements == null) {
            return Collections.emptySet();
        }
        TreeSet<RejectedKOSpecialElement> sortedSet = null;
        for (RejectedKOSpecialElement element : specialElements) {
            Set<String> referencedSeriesInstanceUIDSet = element.getReferencedSeriesInstanceUIDSet();
            if (seriesUID != null && !referencedSeriesInstanceUIDSet.contains(seriesUID) && !element.getMediaReader().isEditableDicom()) continue;
            if (sortedSet == null) {
                sortedSet = new TreeSet<RejectedKOSpecialElement>((Comparator<RejectedKOSpecialElement>)ORDER_BY_DATE);
            }
            sortedSet.add(element);
        }
        return sortedSet == null ? Collections.emptySet() : sortedSet;
    }

    public static RejectedKOSpecialElement getRejectionKoSpecialElement(Collection<RejectedKOSpecialElement> specialElements, String seriesUID, String sopUID, Integer dicomFrameNumber) {
        if (specialElements == null) {
            return null;
        }
        ArrayList koList = null;
        for (RejectedKOSpecialElement koElement : specialElements) {
            if (!AbstractKOSpecialElement.isSopuidInReferencedSeriesSequence(koElement.getReferencedSOPInstanceUIDObject(seriesUID), sopUID, dicomFrameNumber)) continue;
            if (koList == null) {
                koList = new ArrayList();
            }
            koList.add(koElement);
        }
        if (koList != null && !koList.isEmpty()) {
            koList.sort(ORDER_BY_DATE);
            return (RejectedKOSpecialElement)koList.getFirst();
        }
        return null;
    }

    public static class Reference {
        private final String studyInstanceUID;
        private final String seriesInstanceUID;
        private final String sopInstanceUID;
        private final String sopClassUID;
        private final List<Integer> frameList;
        private final Integer instanceNumber;

        public Reference(DicomImageElement dicomImage) {
            this.studyInstanceUID = TagD.getTagValue((TagReadable)dicomImage, 0x20000D, String.class);
            this.seriesInstanceUID = TagD.getTagValue((TagReadable)dicomImage, 0x20000E, String.class);
            this.sopInstanceUID = TagD.getTagValue((TagReadable)dicomImage, 524312, String.class);
            this.sopClassUID = TagD.getTagValue((TagReadable)dicomImage, 524310, String.class);
            this.instanceNumber = TagD.getTagValue((TagReadable)dicomImage, 2097171, Integer.class);
            if (dicomImage.getMediaReader().getMediaElementNumber() > 1) {
                Integer frame = TagD.getTagValue((TagReadable)dicomImage, 2097171, Integer.class);
                this.frameList = new ArrayList<Integer>(1);
                this.frameList.add(frame);
            } else {
                this.frameList = Collections.emptyList();
            }
        }

        public Reference(String studyInstanceUID, String seriesInstanceUID, String sopInstanceUID, String sopClassUID, Integer instanceNumber, int[] frames) {
            this.studyInstanceUID = studyInstanceUID;
            this.seriesInstanceUID = seriesInstanceUID;
            this.sopInstanceUID = sopInstanceUID;
            this.sopClassUID = sopClassUID;
            this.instanceNumber = instanceNumber;
            this.frameList = frames == null ? Collections.emptyList() : Arrays.stream(frames).boxed().collect(Collectors.toList());
        }

        public String getStudyInstanceUID() {
            return this.studyInstanceUID;
        }

        public String getSeriesInstanceUID() {
            return this.seriesInstanceUID;
        }

        public String getSopInstanceUID() {
            return this.sopInstanceUID;
        }

        public String getSopClassUID() {
            return this.sopClassUID;
        }

        public Integer getInstanceNumber() {
            return this.instanceNumber;
        }

        public List<Integer> getFrameList() {
            return this.frameList;
        }
    }
}

