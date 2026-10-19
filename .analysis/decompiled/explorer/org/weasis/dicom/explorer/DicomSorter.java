/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.runtime.SwitchBootstraps
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.media.data.MediaElement
 *  org.weasis.core.api.media.data.MediaSeries$MEDIA_POSITION
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.Series
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.util.StringUtil
 *  org.weasis.dicom.codec.TagD
 */
package org.weasis.dicom.explorer;

import java.lang.runtime.SwitchBootstraps;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.swing.tree.DefaultMutableTreeNode;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.main.PatientPane;
import org.weasis.dicom.explorer.main.SeriesPane;
import org.weasis.dicom.explorer.main.StudyPane;

public final class DicomSorter {
    private static final String SR_MODALITY = "SR";
    private static final String DOC_MODALITY = "DOC";
    private static final String ENCAPSULATED_SOP_CLASS_PREFIX = "1.2.840.10008.5.1.4.1.1.88";
    private static final String DOCUMENT_SOP_CLASS_PREFIX = "1.2.840.10008.5.1.4.1.1.104";
    private static final String ZERO_PADDING = "%010d";
    private static final String NULL_STACK_PLACEHOLDER = "zzz";
    public static final Comparator<Object> PATIENT_COMPARATOR = Comparator.nullsLast((o1, o2) -> {
        MediaSeriesGroup patient1 = DicomSorter.extractPatient(o1);
        MediaSeriesGroup patient2 = DicomSorter.extractPatient(o2);
        return patient1 == null || patient2 == null ? DicomSorter.compareObjectWithNull(patient1, patient2) : DicomSorter.comparePatients(patient1, patient2);
    });
    public static final Comparator<Object> STUDY_COMPARATOR = Comparator.nullsLast((o1, o2) -> {
        MediaSeriesGroup study1 = DicomSorter.extractStudy(o1);
        MediaSeriesGroup study2 = DicomSorter.extractStudy(o2);
        return study1 == null || study2 == null ? DicomSorter.compareObjectWithNull(study1, study2) : DicomSorter.compareStudies(study1, study2);
    });
    public static final Comparator<Object> SERIES_COMPARATOR = Comparator.nullsLast((o1, o2) -> {
        MediaSeriesGroup series1 = DicomSorter.extractSeries(o1);
        MediaSeriesGroup series2 = DicomSorter.extractSeries(o2);
        return series1 == null || series2 == null ? DicomSorter.compareObjectWithNull(series1, series2) : DicomSorter.compareSeries(series1, series2);
    });

    private DicomSorter() {
    }

    private static MediaSeriesGroup extractPatient(Object obj) {
        MediaSeriesGroup mediaSeriesGroup;
        Object object = obj;
        Objects.requireNonNull(object);
        Object object2 = object;
        int n = 0;
        block5: while (true) {
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{PatientPane.class, DefaultMutableTreeNode.class, MediaSeriesGroup.class}, (Object)object2, (int)n)) {
                case 0: {
                    PatientPane patientPane = (PatientPane)object2;
                    mediaSeriesGroup = patientPane.getCurrentPatient().orElse(null);
                    break block5;
                }
                case 1: {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)object2;
                    Object object3 = node.getUserObject();
                    if (!(object3 instanceof MediaSeriesGroup)) {
                        n = 2;
                        continue block5;
                    }
                    MediaSeriesGroup group = (MediaSeriesGroup)object3;
                    mediaSeriesGroup = group;
                    break block5;
                }
                case 2: {
                    MediaSeriesGroup group;
                    mediaSeriesGroup = group = (MediaSeriesGroup)object2;
                    break block5;
                }
                default: {
                    mediaSeriesGroup = null;
                    break block5;
                }
            }
            break;
        }
        return mediaSeriesGroup;
    }

    private static MediaSeriesGroup extractStudy(Object obj) {
        MediaSeriesGroup mediaSeriesGroup;
        Object object = obj;
        Objects.requireNonNull(object);
        Object object2 = object;
        int n = 0;
        block5: while (true) {
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{StudyPane.class, DefaultMutableTreeNode.class, MediaSeriesGroup.class}, (Object)object2, (int)n)) {
                case 0: {
                    StudyPane studyPane = (StudyPane)object2;
                    mediaSeriesGroup = studyPane.getDicomStudy();
                    break block5;
                }
                case 1: {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)object2;
                    Object object3 = node.getUserObject();
                    if (!(object3 instanceof MediaSeriesGroup)) {
                        n = 2;
                        continue block5;
                    }
                    MediaSeriesGroup group = (MediaSeriesGroup)object3;
                    mediaSeriesGroup = group;
                    break block5;
                }
                case 2: {
                    MediaSeriesGroup group;
                    mediaSeriesGroup = group = (MediaSeriesGroup)object2;
                    break block5;
                }
                default: {
                    mediaSeriesGroup = null;
                    break block5;
                }
            }
            break;
        }
        return mediaSeriesGroup;
    }

    private static MediaSeriesGroup extractSeries(Object obj) {
        MediaSeriesGroup mediaSeriesGroup;
        Object object = obj;
        Objects.requireNonNull(object);
        Object object2 = object;
        int n = 0;
        block5: while (true) {
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{SeriesPane.class, DefaultMutableTreeNode.class, MediaSeriesGroup.class}, (Object)object2, (int)n)) {
                case 0: {
                    SeriesPane seriesPane = (SeriesPane)object2;
                    mediaSeriesGroup = seriesPane.getDicomSeries();
                    break block5;
                }
                case 1: {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)object2;
                    Object object3 = node.getUserObject();
                    if (!(object3 instanceof MediaSeriesGroup)) {
                        n = 2;
                        continue block5;
                    }
                    MediaSeriesGroup group = (MediaSeriesGroup)object3;
                    mediaSeriesGroup = group;
                    break block5;
                }
                case 2: {
                    MediaSeriesGroup group;
                    mediaSeriesGroup = group = (MediaSeriesGroup)object2;
                    break block5;
                }
                default: {
                    mediaSeriesGroup = null;
                    break block5;
                }
            }
            break;
        }
        return mediaSeriesGroup;
    }

    private static int comparePatients(MediaSeriesGroup patient1, MediaSeriesGroup patient2) {
        String name1 = (String)TagD.getTagValue((TagReadable)patient1, (int)0x100010, String.class);
        String name2 = (String)TagD.getTagValue((TagReadable)patient2, (int)0x100010, String.class);
        if (name1 == null || name2 == null) {
            return DicomSorter.compareObjectWithNull(name1, name2);
        }
        return StringUtil.collator.compare(name1, name2);
    }

    private static int compareStudies(MediaSeriesGroup study1, MediaSeriesGroup study2) {
        int dateComparison = DicomSorter.compareStudyDates(study1, study2);
        if (dateComparison != 0) {
            return dateComparison;
        }
        int idComparison = DicomSorter.compareStudyIds(study1, study2);
        return idComparison != 0 ? idComparison : DicomSorter.compareStudyDescriptions(study1, study2);
    }

    private static int compareSeries(MediaSeriesGroup series1, MediaSeriesGroup series2) {
        boolean isEncap2;
        boolean isEncap1 = DicomSorter.isEncapsulatedOrSR(series1);
        if (isEncap1 != (isEncap2 = DicomSorter.isEncapsulatedOrSR(series2))) {
            return isEncap1 ? 1 : -1;
        }
        return Comparator.comparing(DicomSorter::getSeriesNumber).thenComparing(DicomSorter::getSplitSeriesNumber).thenComparing(DicomSorter::getSeriesDateTime).thenComparing(DicomSorter::getSeriesDescription).thenComparing(DicomSorter::compareByMediaElements).compare(series1, series2);
    }

    private static int compareStudyDates(MediaSeriesGroup study1, MediaSeriesGroup study2) {
        LocalDateTime date1 = TagD.dateTime((int)524320, (int)524336, (TagReadable)study1);
        LocalDateTime date2 = TagD.dateTime((int)524320, (int)524336, (TagReadable)study2);
        if (date1 == null || date2 == null) {
            return DicomSorter.compareObjectWithNull(date1, date2);
        }
        return DicomSorter.getStudyDateSorting() == SortingTime.CHRONOLOGICAL ? date1.compareTo(date2) : date2.compareTo(date1);
    }

    private static int compareStudyIds(MediaSeriesGroup study1, MediaSeriesGroup study2) {
        String id1 = (String)TagD.getTagValue((TagReadable)study1, (int)0x200010, String.class);
        String id2 = (String)TagD.getTagValue((TagReadable)study2, (int)0x200010, String.class);
        return id1 == null || id2 == null ? DicomSorter.compareObjectWithNull(id1, id2) : StringUtil.collator.compare(id1, id2);
    }

    private static int compareStudyDescriptions(MediaSeriesGroup study1, MediaSeriesGroup study2) {
        String desc1 = (String)TagD.getTagValue((TagReadable)study1, (int)528432, String.class);
        String desc2 = (String)TagD.getTagValue((TagReadable)study2, (int)528432, String.class);
        return desc1 == null || desc2 == null ? DicomSorter.compareObjectWithNull(desc1, desc2) : StringUtil.collator.compare(desc1, desc2);
    }

    private static Integer getSplitSeriesNumber(MediaSeriesGroup series) {
        return Optional.ofNullable((Integer)TagD.getTagValue((TagReadable)series, (TagW)TagW.SplitSeriesNumber, Integer.class)).orElse(0);
    }

    private static Integer getSeriesNumber(MediaSeriesGroup series) {
        return Optional.ofNullable((Integer)TagD.getTagValue((TagReadable)series, (int)0x200011, Integer.class)).orElse(Integer.MAX_VALUE);
    }

    private static LocalDateTime getSeriesDateTime(MediaSeriesGroup series) {
        return Optional.ofNullable(TagD.dateTime((int)524321, (int)524337, (TagReadable)series)).orElse(LocalDateTime.MAX);
    }

    private static String getSeriesDescription(MediaSeriesGroup series) {
        return Optional.ofNullable((String)TagD.getTagValue((TagReadable)series, (int)528446, String.class)).orElse("");
    }

    private static int compareByMediaElements(MediaSeriesGroup series1, MediaSeriesGroup series2) {
        Series s1;
        block3: {
            block2: {
                if (!(series1 instanceof Series)) break block2;
                s1 = (Series)series1;
                if (series2 instanceof Series) break block3;
            }
            return 0;
        }
        Series s2 = (Series)series2;
        MediaElement media1 = s1.getMedia(MediaSeries.MEDIA_POSITION.FIRST, null, null);
        MediaElement media2 = s2.getMedia(MediaSeries.MEDIA_POSITION.FIRST, null, null);
        return media1 == null || media2 == null ? DicomSorter.compareObjectWithNull(media1, media2) : DicomSorter.compareMediaElements(media1, media2);
    }

    private static int compareObjectWithNull(Object o1, Object o2) {
        return o1 == null ? (o2 == null ? 0 : 1) : -1;
    }

    private static int compareMediaElements(MediaElement media1, MediaElement media2) {
        return Comparator.comparing(DicomSorter::getMediaDateTime).thenComparing(DicomSorter::getSliceLocation).thenComparing(DicomSorter::getStackId).compare(media1, media2);
    }

    private static LocalDateTime getMediaDateTime(MediaElement media) {
        return Stream.of(TagD.dateTime((int)524322, (int)524338, (TagReadable)media), TagD.dateTime((int)524323, (int)524339, (TagReadable)media), TagD.dateTime((int)1576978, (int)1576980, (TagReadable)media)).filter(Objects::nonNull).findFirst().orElse(LocalDateTime.MAX);
    }

    private static Double getSliceLocation(MediaElement media) {
        return Optional.ofNullable((Double)TagD.getTagValue((TagReadable)media, (int)2101313, Double.class)).orElse((Double)Double.MAX_VALUE);
    }

    private static String getStackId(MediaElement media) {
        String stackId = (String)TagD.getTagValue((TagReadable)media, (int)2134102, String.class);
        if (stackId == null) {
            return NULL_STACK_PLACEHOLDER;
        }
        return DicomSorter.parseNumericStackId(stackId).orElse(stackId);
    }

    private static Optional<String> parseNumericStackId(String stackId) {
        try {
            int numericId = Integer.parseInt(stackId);
            return Optional.of(String.format(ZERO_PADDING, numericId));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static boolean isEncapsulatedOrSR(MediaSeriesGroup series) {
        String sopClassUID = (String)TagD.getTagValue((TagReadable)series, (int)524310, String.class);
        if (sopClassUID != null && (sopClassUID.startsWith(ENCAPSULATED_SOP_CLASS_PREFIX) || sopClassUID.startsWith(DOCUMENT_SOP_CLASS_PREFIX))) {
            return true;
        }
        String modality = (String)TagD.getTagValue((TagReadable)series, (int)524384, String.class);
        return SR_MODALITY.equals(modality) || DOC_MODALITY.equals(modality);
    }

    public static SortingTime getStudyDateSorting() {
        int key = GuiUtils.getUICore().getSystemPreferences().getIntProperty("weasis.sorting.study.date", SortingTime.INVERSE_CHRONOLOGICAL.getId());
        return SortingTime.valueOf(key);
    }

    public static enum SortingTime {
        CHRONOLOGICAL(0, Messages.getString("chrono.order")),
        INVERSE_CHRONOLOGICAL(1, Messages.getString("reverse.chrono.order"));

        private final int id;
        private final String title;

        private SortingTime(int id, String title) {
            this.id = id;
            this.title = title;
        }

        public int getId() {
            return this.id;
        }

        public String getTitle() {
            return this.title;
        }

        public String toString() {
            return this.title;
        }

        public static SortingTime valueOf(int id) {
            return Arrays.stream(SortingTime.values()).filter(s -> s.id == id).findFirst().orElse(CHRONOLOGICAL);
        }
    }
}

