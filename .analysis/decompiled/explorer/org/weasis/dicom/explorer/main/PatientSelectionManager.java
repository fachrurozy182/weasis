/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 */
package org.weasis.dicom.explorer.main;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.dicom.explorer.main.DicomExplorer;

public class PatientSelectionManager {
    private final DicomExplorer explorer;
    private final List<PatientSelectionListener> listeners = new CopyOnWriteArrayList<PatientSelectionListener>();

    public PatientSelectionManager(DicomExplorer explorer) {
        this.explorer = Objects.requireNonNull(explorer);
    }

    public void setCurrentPatient(MediaSeriesGroup patient) {
        if (!this.isCurrentPatient(patient)) {
            this.notifyPatientChanged(patient);
        }
    }

    public void clearCurrentPatient() {
        if (this.getCurrentPatient().isPresent()) {
            this.notifyPatientCleared();
        }
    }

    public Optional<MediaSeriesGroup> getCurrentPatient() {
        return this.explorer.getSelectedPatientPane().getCurrentPatient();
    }

    public boolean isCurrentPatient(MediaSeriesGroup patient) {
        return Objects.equals(this.getCurrentPatient().orElse(null), patient);
    }

    public void addPatientSelectionListener(PatientSelectionListener listener) {
        this.listeners.add(listener);
    }

    public void removePatientSelectionListener(PatientSelectionListener listener) {
        this.listeners.remove(listener);
    }

    private void notifyPatientChanged(MediaSeriesGroup patient) {
        for (PatientSelectionListener listener : this.listeners) {
            listener.patientSelected(patient);
        }
    }

    private void notifyPatientCleared() {
        for (PatientSelectionListener listener : this.listeners) {
            listener.patientCleared();
        }
    }

    public static interface PatientSelectionListener {
        public void patientSelected(MediaSeriesGroup var1);

        public void patientCleared();
    }
}

