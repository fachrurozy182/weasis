/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.dicom.mf;

import java.io.IOException;
import java.text.Collator;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.dcm4che3.img.util.DateTimeUtils;
import org.dcm4che3.img.util.DicomObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.dicom.mf.ManifestNode;
import org.weasis.dicom.mf.ManifestSerializer;
import org.weasis.dicom.mf.Series;

public class Study
implements ManifestNode,
Comparable<Study> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Study.class);
    private static final String KEY_STUDY_INSTANCE_UID = ManifestNode.keyword(0x20000D);
    private static final String KEY_STUDY_DESCRIPTION = ManifestNode.keyword(528432);
    private static final String KEY_STUDY_DATE = ManifestNode.keyword(524320);
    private static final String KEY_STUDY_TIME = ManifestNode.keyword(524336);
    private static final String KEY_ACCESSION_NUMBER = ManifestNode.keyword(524368);
    private static final String KEY_STUDY_ID = ManifestNode.keyword(0x200010);
    private static final String KEY_REFERRING_PHYSICIAN_NAME = ManifestNode.keyword(524432);
    private final String studyInstanceUID;
    private final Map<String, Series> seriesMap;
    private String studyID;
    private String studyDescription;
    private String studyDate;
    private String studyTime;
    private String accessionNumber;
    private String referringPhysicianName;

    public Study(String studyInstanceUID) {
        this.studyInstanceUID = Objects.requireNonNull(studyInstanceUID, "Study Instance UID cannot be null");
        this.seriesMap = new HashMap<String, Series>();
    }

    public String getStudyInstanceUID() {
        return this.studyInstanceUID;
    }

    public String getStudyID() {
        return this.studyID;
    }

    public void setStudyID(String studyID) {
        this.studyID = studyID;
    }

    public String getStudyDescription() {
        return this.studyDescription;
    }

    public void setStudyDescription(String studyDescription) {
        this.studyDescription = studyDescription;
    }

    public String getStudyDate() {
        return this.studyDate;
    }

    public void setStudyDate(String studyDate) {
        this.studyDate = studyDate;
    }

    public String getStudyTime() {
        return this.studyTime;
    }

    public void setStudyTime(String studyTime) {
        this.studyTime = studyTime;
    }

    public String getAccessionNumber() {
        return this.accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public String getReferringPhysicianName() {
        return this.referringPhysicianName;
    }

    public void setReferringPhysicianName(String referringPhysicianName) {
        this.referringPhysicianName = referringPhysicianName;
    }

    public void addSeries(Series series) {
        if (series != null) {
            this.seriesMap.put(series.getSeriesInstanceUID(), series);
        }
    }

    public Series removeSeries(String seriesUID) {
        return this.seriesMap.remove(seriesUID);
    }

    public Series getSeries(String seriesUID) {
        return this.seriesMap.get(seriesUID);
    }

    public Collection<Series> getSeries() {
        return this.seriesMap.values();
    }

    public Set<Map.Entry<String, Series>> getEntrySet() {
        return this.seriesMap.entrySet();
    }

    public boolean isEmpty() {
        return this.seriesMap.values().stream().allMatch(Series::isEmpty);
    }

    @Override
    public void write(ManifestSerializer serializer) throws IOException {
        serializer.beginObject(ManifestNode.Level.STUDY.getTagName());
        serializer.attribute(KEY_STUDY_INSTANCE_UID, this.studyInstanceUID);
        serializer.attribute(KEY_STUDY_DESCRIPTION, this.studyDescription);
        serializer.attribute(KEY_STUDY_DATE, this.studyDate);
        serializer.attribute(KEY_STUDY_TIME, this.studyTime);
        serializer.attribute(KEY_ACCESSION_NUMBER, this.accessionNumber);
        serializer.attribute(KEY_STUDY_ID, this.studyID);
        serializer.attribute(KEY_REFERRING_PHYSICIAN_NAME, this.referringPhysicianName);
        this.writeSeriesInOrder(serializer);
        serializer.endObject();
    }

    @Override
    public int compareTo(Study other) {
        int datetimeComparison = Study.compareStudyDateTimes(this, other);
        if (datetimeComparison != 0) {
            return datetimeComparison;
        }
        return Study.compareStudyDescriptions(this.studyDescription, other.studyDescription);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Study other = (Study)obj;
        return this.studyInstanceUID.equals(other.studyInstanceUID);
    }

    public int hashCode() {
        return Objects.hash(this.studyInstanceUID);
    }

    private void writeSeriesInOrder(ManifestSerializer serializer) throws IOException {
        if (this.seriesMap.isEmpty()) {
            return;
        }
        ArrayList<Series> sortedSeries = new ArrayList<Series>(this.seriesMap.values());
        Collections.sort(sortedSeries);
        serializer.beginArray(ManifestNode.Level.SERIES.getTagName());
        for (Series series : sortedSeries) {
            series.write(serializer);
        }
        serializer.endArray();
    }

    private static int compareStudyDateTimes(Study study1, Study study2) {
        LocalDateTime datetime1 = Study.parseStudyDateTime(study1.studyDate, study1.studyTime);
        LocalDateTime datetime2 = Study.parseStudyDateTime(study2.studyDate, study2.studyTime);
        if (datetime1 != null && datetime2 != null) {
            return datetime2.compareTo(datetime1);
        }
        if (datetime1 == null && datetime2 == null) {
            return 0;
        }
        return datetime1 == null ? 1 : -1;
    }

    private static int compareStudyDescriptions(String desc1, String desc2) {
        if (desc1 != null && desc2 != null) {
            return Collator.getInstance(Locale.ROOT).compare(desc1, desc2);
        }
        if (desc1 == null && desc2 == null) {
            return 0;
        }
        return desc1 == null ? 1 : -1;
    }

    private static LocalDateTime parseStudyDateTime(String studyDate, String studyTime) {
        try {
            return DateTimeUtils.dateTime(DicomObjectUtil.getDicomDate(studyDate), DicomObjectUtil.getDicomTime(studyTime));
        }
        catch (NumberFormatException | DateTimeException e) {
            LOGGER.debug("Cannot parse study date/time: {} / {}", (Object)studyDate, (Object)studyTime);
            return null;
        }
    }
}

