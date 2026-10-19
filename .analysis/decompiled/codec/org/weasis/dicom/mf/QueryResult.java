/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.mf;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.weasis.dicom.mf.Patient;
import org.weasis.dicom.mf.ViewerMessage;
import org.weasis.dicom.mf.WadoParameters;

public interface QueryResult {
    public WadoParameters getWadoParameters();

    public void removePatientId(List<String> var1, boolean var2);

    public void removeStudyUid(List<String> var1);

    public void removeAccessionNumber(List<String> var1);

    public void removeSeriesUid(List<String> var1);

    public void removeItemsWithoutElements();

    public Map<String, Patient> getPatients();

    public List<ViewerMessage> getViewerMessages();

    public void addViewerMessage(ViewerMessage var1);

    public ViewerMessage getViewerMessage();

    public void setViewerMessage(ViewerMessage var1);

    default public void removePatientId(Collection<String> patientIdList) {
        if (patientIdList instanceof List) {
            List list = (List)patientIdList;
            this.removePatientId(list, false);
        } else {
            this.removePatientId(List.copyOf(patientIdList), false);
        }
    }

    default public void removeStudyUid(Collection<String> studyUidCollection) {
        if (studyUidCollection instanceof List) {
            List list = (List)studyUidCollection;
            this.removeStudyUid(list);
        } else {
            this.removeStudyUid(List.copyOf(studyUidCollection));
        }
    }

    default public void removeSeriesUid(Collection<String> seriesUidCollection) {
        if (seriesUidCollection instanceof List) {
            List list = (List)seriesUidCollection;
            this.removeSeriesUid(list);
        } else {
            this.removeSeriesUid(List.copyOf(seriesUidCollection));
        }
    }

    default public void removeAccessionNumber(Collection<String> accessionNumberCollection) {
        if (accessionNumberCollection instanceof List) {
            List list = (List)accessionNumberCollection;
            this.removeAccessionNumber(list);
        } else {
            this.removeAccessionNumber(List.copyOf(accessionNumberCollection));
        }
    }

    default public boolean hasPatients() {
        Map<String, Patient> patients = this.getPatients();
        return patients != null && !patients.isEmpty();
    }

    default public int getPatientCount() {
        Map<String, Patient> patients = this.getPatients();
        return patients != null ? patients.size() : 0;
    }
}

