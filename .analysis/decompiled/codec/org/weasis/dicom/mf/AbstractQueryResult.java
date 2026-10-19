/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.LangUtil
 */
package org.weasis.dicom.mf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.weasis.core.util.LangUtil;
import org.weasis.dicom.mf.Patient;
import org.weasis.dicom.mf.QueryResult;
import org.weasis.dicom.mf.Series;
import org.weasis.dicom.mf.Study;
import org.weasis.dicom.mf.ViewerMessage;

public abstract class AbstractQueryResult
implements QueryResult {
    protected final Map<String, Patient> patientMap;
    protected final List<ViewerMessage> viewerMessages = new ArrayList<ViewerMessage>();

    protected AbstractQueryResult() {
        this(null);
    }

    protected AbstractQueryResult(Collection<Patient> patients) {
        this.patientMap = new ConcurrentHashMap<String, Patient>();
        Collection safePatients = LangUtil.emptyIfNull(patients);
        safePatients.forEach(this::addPatient);
    }

    @Override
    public void removePatientId(List<String> patientIdList, boolean containsIssuer) {
        if (patientIdList == null || patientIdList.isEmpty()) {
            return;
        }
        if (containsIssuer) {
            patientIdList.forEach(this.patientMap::remove);
        } else {
            this.removePatientsByIdWithoutIssuer(patientIdList);
        }
    }

    @Override
    public void removeStudyUid(List<String> studyUidList) {
        if (this.patientUidListIsEmpty(studyUidList)) {
            return;
        }
        this.patientMap.values().forEach(patient -> studyUidList.forEach(patient::removeStudy));
        this.removeItemsWithoutElements();
    }

    @Override
    public void removeAccessionNumber(List<String> accessionNumberList) {
        if (this.patientUidListIsEmpty(accessionNumberList)) {
            return;
        }
        Set<String> accessionSet = Set.copyOf(accessionNumberList);
        this.patientMap.values().forEach(patient -> patient.getEntrySet().removeIf(entry -> !accessionSet.contains(((Study)entry.getValue()).getAccessionNumber())));
        this.removeItemsWithoutElements();
    }

    @Override
    public void removeSeriesUid(List<String> seriesUidList) {
        if (this.patientUidListIsEmpty(seriesUidList)) {
            return;
        }
        this.patientMap.values().forEach(patient -> patient.getStudies().forEach(study -> seriesUidList.forEach(study::removeSeries)));
        this.removeItemsWithoutElements();
    }

    @Override
    public void removeItemsWithoutElements() {
        this.patientMap.values().forEach(this::removeEmptyStudiesAndSeries);
        this.patientMap.entrySet().removeIf(entry -> ((Patient)entry.getValue()).isEmpty());
    }

    public void addPatient(Patient patient) {
        if (patient != null) {
            this.patientMap.put(patient.getPseudoPatientUID(), patient);
        }
    }

    public Patient removePatient(String patientID, String issuerOfPatientID) {
        String key = this.buildPatientKey(patientID, issuerOfPatientID);
        return key != null ? this.patientMap.remove(key) : null;
    }

    public Patient getPatient(String patientID, String issuerOfPatientID) {
        String key = this.buildPatientKey(patientID, issuerOfPatientID);
        return key != null ? this.patientMap.get(key) : null;
    }

    @Override
    public Map<String, Patient> getPatients() {
        return Collections.unmodifiableMap(this.patientMap);
    }

    @Override
    public List<ViewerMessage> getViewerMessages() {
        return Collections.unmodifiableList(this.viewerMessages);
    }

    @Override
    public void addViewerMessage(ViewerMessage viewerMessage) {
        if (viewerMessage != null) {
            this.viewerMessages.add(viewerMessage);
        }
    }

    @Override
    public ViewerMessage getViewerMessage() {
        return this.viewerMessages.isEmpty() ? null : this.viewerMessages.get(0);
    }

    @Override
    public void setViewerMessage(ViewerMessage viewerMessage) {
        this.viewerMessages.clear();
        this.addViewerMessage(viewerMessage);
    }

    private void removePatientsByIdWithoutIssuer(List<String> patientIdList) {
        List pseudoUidsToRemove = patientIdList.stream().map(this::findPseudoUidForPatientId).filter(Objects::nonNull).toList();
        pseudoUidsToRemove.forEach(this.patientMap::remove);
    }

    private String findPseudoUidForPatientId(String patientID) {
        return this.patientMap.values().stream().filter(patient -> patientID.equals(patient.getPatientID())).map(Patient::getPseudoPatientUID).findFirst().orElse(null);
    }

    private void removeEmptyStudiesAndSeries(Patient patient) {
        patient.getEntrySet().removeIf(studyEntry -> {
            Study study = (Study)studyEntry.getValue();
            study.getEntrySet().removeIf(seriesEntry -> ((Series)seriesEntry.getValue()).isEmpty());
            return study.isEmpty();
        });
    }

    private String buildPatientKey(String patientID, String issuerOfPatientID) {
        if (patientID == null) {
            return null;
        }
        return issuerOfPatientID != null ? patientID + issuerOfPatientID : patientID;
    }

    private boolean patientUidListIsEmpty(List<String> uidList) {
        return uidList == null || uidList.isEmpty();
    }
}

