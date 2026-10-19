/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.mf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.mf.ManifestNode;
import org.weasis.dicom.mf.ManifestSerializer;
import org.weasis.dicom.mf.Study;

public class Patient
implements ManifestNode,
Comparable<Patient> {
    private static final String KEY_PATIENT_ID = ManifestNode.keyword(0x100020);
    private static final String KEY_ISSUER_OF_PATIENT_ID = ManifestNode.keyword(0x100021);
    private static final String KEY_PATIENT_NAME = ManifestNode.keyword(0x100010);
    private static final String KEY_PATIENT_BIRTH_DATE = ManifestNode.keyword(0x100030);
    private static final String KEY_PATIENT_BIRTH_TIME = ManifestNode.keyword(1048626);
    private static final String KEY_PATIENT_SEX = ManifestNode.keyword(0x100040);
    private final String patientID;
    private final String issuerOfPatientID;
    private final Map<String, Study> studiesMap;
    private String patientName;
    private String patientBirthDate;
    private String patientBirthTime;
    private String patientSex;

    public Patient(String patientID, String issuerOfPatientID) {
        this.patientID = Objects.requireNonNull(patientID, "Patient ID cannot be null");
        this.issuerOfPatientID = issuerOfPatientID;
        this.patientName = "";
        this.studiesMap = new HashMap<String, Study>();
    }

    public String getPatientID() {
        return this.patientID;
    }

    public String getIssuerOfPatientID() {
        return this.issuerOfPatientID;
    }

    public String getPseudoPatientUID() {
        return this.issuerOfPatientID != null ? this.patientID + this.issuerOfPatientID : this.patientID;
    }

    public String getPatientName() {
        return this.patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = StringUtil.getEmptyStringIfNull((Object)patientName);
    }

    public String getPatientBirthDate() {
        return this.patientBirthDate;
    }

    public void setPatientBirthDate(String patientBirthDate) {
        this.patientBirthDate = patientBirthDate;
    }

    public String getPatientBirthTime() {
        return this.patientBirthTime;
    }

    public void setPatientBirthTime(String patientBirthTime) {
        this.patientBirthTime = patientBirthTime;
    }

    public String getPatientSex() {
        return this.patientSex;
    }

    public void setPatientSex(String patientSex) {
        this.patientSex = PatientSex.normalize(patientSex);
    }

    public void addStudy(Study study) {
        if (study != null) {
            this.studiesMap.put(study.getStudyInstanceUID(), study);
        }
    }

    public Study removeStudy(String studyUID) {
        return this.studiesMap.remove(studyUID);
    }

    public Study getStudy(String studyUID) {
        return this.studiesMap.get(studyUID);
    }

    public Collection<Study> getStudies() {
        return this.studiesMap.values();
    }

    public Set<Map.Entry<String, Study>> getEntrySet() {
        return this.studiesMap.entrySet();
    }

    public boolean isEmpty() {
        return this.studiesMap.values().stream().allMatch(Study::isEmpty);
    }

    @Override
    public void write(ManifestSerializer serializer) throws IOException {
        serializer.beginObject(ManifestNode.Level.PATIENT.getTagName());
        serializer.attribute(KEY_PATIENT_ID, this.patientID);
        serializer.attribute(KEY_ISSUER_OF_PATIENT_ID, this.issuerOfPatientID);
        serializer.attribute(KEY_PATIENT_NAME, this.patientName);
        serializer.attribute(KEY_PATIENT_BIRTH_DATE, this.patientBirthDate);
        serializer.attribute(KEY_PATIENT_BIRTH_TIME, this.patientBirthTime);
        serializer.attribute(KEY_PATIENT_SEX, this.patientSex);
        this.writeStudiesInOrder(serializer);
        serializer.endObject();
    }

    @Override
    public int compareTo(Patient other) {
        return this.patientName.compareTo(other.patientName);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Patient other = (Patient)obj;
        return Objects.equals(this.patientID, other.patientID) && Objects.equals(this.issuerOfPatientID, other.issuerOfPatientID);
    }

    public int hashCode() {
        return Objects.hash(this.patientID, this.issuerOfPatientID);
    }

    private void writeStudiesInOrder(ManifestSerializer serializer) throws IOException {
        if (this.studiesMap.isEmpty()) {
            return;
        }
        ArrayList<Study> sortedStudies = new ArrayList<Study>(this.studiesMap.values());
        Collections.sort(sortedStudies);
        serializer.beginArray(ManifestNode.Level.STUDY.getTagName());
        for (Study study : sortedStudies) {
            study.write(serializer);
        }
        serializer.endArray();
    }

    private static enum PatientSex {
        MALE("M"),
        FEMALE("F"),
        OTHER("O");

        private final String code;

        private PatientSex(String code) {
            this.code = code;
        }

        public String getCode() {
            return this.code;
        }

        public static String normalize(String sex) {
            if (sex == null) {
                return null;
            }
            String upperSex = sex.toUpperCase(Locale.ROOT);
            if (upperSex.startsWith("M")) {
                return MALE.getCode();
            }
            if (upperSex.startsWith("F")) {
                return FEMALE.getCode();
            }
            return OTHER.getCode();
        }
    }
}

