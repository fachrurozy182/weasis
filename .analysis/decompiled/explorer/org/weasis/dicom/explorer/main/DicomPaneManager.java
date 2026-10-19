/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.dicom.codec.DicomSeries
 */
package org.weasis.dicom.explorer.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.swing.SwingUtilities;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.DicomSorter;
import org.weasis.dicom.explorer.main.DicomExplorer;
import org.weasis.dicom.explorer.main.PatientPane;
import org.weasis.dicom.explorer.main.SeriesFilter;
import org.weasis.dicom.explorer.main.SeriesPane;
import org.weasis.dicom.explorer.main.StudyPane;

public class DicomPaneManager {
    private final HashMap<MediaSeriesGroup, List<StudyPane>> patient2study = new HashMap();
    private final HashMap<MediaSeriesGroup, List<SeriesPane>> study2series = new HashMap();
    private final DicomExplorer explorer;

    public DicomPaneManager(DicomExplorer explorer) {
        this.explorer = explorer;
    }

    public SeriesFilter getSeriesFilter() {
        return this.explorer.getSeriesFilter();
    }

    public void removePatientPane(MediaSeriesGroup patient) {
        if (patient != null) {
            List<StudyPane> studies = this.patient2study.remove(patient);
            if (studies != null) {
                for (StudyPane studyPane : studies) {
                    this.study2series.remove(studyPane.getDicomStudy());
                }
            }
            this.explorer.updateRemovedPatient(patient);
        }
    }

    public StudyPane getStudyPane(MediaSeriesGroup study) {
        return this.getStudyList(this.getPatientForStudy(study)).stream().filter(pane -> pane.isStudy(study)).findFirst().orElse(null);
    }

    public StudyPane createStudyPaneInstance(MediaSeriesGroup study, int[] position) {
        StudyPane studyPane = this.getStudyPane(study);
        if (studyPane == null) {
            studyPane = new StudyPane(study);
            MediaSeriesGroup patient = this.getPatientForStudy(study);
            if (patient != null) {
                List studies = this.patient2study.computeIfAbsent(patient, mediaSeriesGroup -> new ArrayList());
                int index = Collections.binarySearch(studies, studyPane, DicomSorter.STUDY_COMPARATOR);
                if (index < 0) {
                    index = -(index + 1);
                }
                if (position != null) {
                    position[0] = index;
                }
                studies.add(index, studyPane);
            }
        } else if (position != null) {
            position[0] = -1;
        }
        return studyPane;
    }

    public List<StudyPane> getStudyList(MediaSeriesGroup patient) {
        List<StudyPane> studies = patient == null ? null : this.patient2study.get(patient);
        return studies == null ? Collections.emptyList() : studies;
    }

    public void removeStudyPane(MediaSeriesGroup study) {
        DicomModel model = this.explorer.getDataExplorerModel();
        MediaSeriesGroup patient = this.getPatientForStudy(study);
        List<StudyPane> studies = this.getStudyList(patient);
        for (int i = studies.size() - 1; i >= 0; --i) {
            StudyPane studyPane = studies.get(i);
            if (!studyPane.isStudy(study)) continue;
            studies.remove(i);
            if (studies.isEmpty()) {
                this.patient2study.remove(patient);
                model.removePatient(patient);
                break;
            }
            this.study2series.remove(study);
            this.explorer.updateRemovedStudy(studyPane, study);
            return;
        }
        this.study2series.remove(study);
    }

    public SeriesPane getSeriesPane(MediaSeriesGroup series) {
        MediaSeriesGroup study = this.getStudyForSeries(series);
        if (study == null) {
            return null;
        }
        return this.getSeriesList(study).stream().filter(pane -> pane.isSeries(series)).findFirst().orElse(null);
    }

    public synchronized SeriesPane createSeriesPaneInstance(DicomSeries series, int[] position) {
        SeriesPane seriesPane = this.getSeriesPane((MediaSeriesGroup)series);
        if (seriesPane == null) {
            DicomModel model = this.explorer.getDataExplorerModel();
            seriesPane = new SeriesPane(series, model);
            MediaSeriesGroup study = this.getStudyForSeries((MediaSeriesGroup)series);
            if (study != null) {
                List seriesList = this.study2series.computeIfAbsent(study, mediaSeriesGroup -> new ArrayList());
                int index = Collections.binarySearch(seriesList, seriesPane, DicomSorter.SERIES_COMPARATOR);
                if (index < 0) {
                    index = -(index + 1);
                }
                if (position != null) {
                    position[0] = index;
                }
                seriesList.add(index, seriesPane);
            }
        } else if (position != null) {
            position[0] = -1;
        }
        return seriesPane;
    }

    public boolean containsSeriesInPatient(PatientPane patientPane, MediaSeriesGroup series) {
        MediaSeriesGroup study = this.getStudyForSeries(series);
        MediaSeriesGroup patient = this.getPatientForStudy(study);
        if (patient == null || study == null || patientPane == null || !patientPane.isPatient(patient)) {
            return false;
        }
        List<SeriesPane> seriesList = this.getSeriesList(study);
        return !seriesList.isEmpty() && seriesList.stream().anyMatch(pane -> pane.isSeries(series));
    }

    public List<SeriesPane> getSeriesList(MediaSeriesGroup study) {
        List<SeriesPane> seriesList = study == null ? null : this.study2series.get(study);
        return seriesList == null ? Collections.emptyList() : seriesList;
    }

    public void removeSeriesPane(MediaSeriesGroup series) {
        MediaSeriesGroup study = this.getStudyForSeries(series);
        if (study == null) {
            return;
        }
        List<SeriesPane> seriesList = this.getSeriesList(study);
        if (!seriesList.isEmpty()) {
            DicomModel model = this.explorer.getDataExplorerModel();
            for (int j = seriesList.size() - 1; j >= 0; --j) {
                SeriesPane se = seriesList.get(j);
                if (!se.isSeries(series)) continue;
                seriesList.remove(j);
                if (seriesList.isEmpty()) {
                    this.study2series.remove(study);
                    model.removeStudy(study);
                    break;
                }
                se.removeAll();
                StudyPane studyPane = this.getStudyPane(study);
                if (studyPane == null || !studyPane.isSeriesVisible(series)) break;
                studyPane.remove(se);
                studyPane.revalidate();
                studyPane.repaint();
                break;
            }
        }
    }

    public void refreshAllPanes() {
        SwingUtilities.invokeLater(() -> this.patient2study.values().stream().flatMap(Collection::stream).forEach(StudyPane::refreshLayout));
    }

    public void refreshStudyPane(MediaSeriesGroup study) {
        StudyPane studyPane = this.getStudyPane(study);
        if (studyPane != null) {
            SwingUtilities.invokeLater(studyPane::refreshLayout);
        }
    }

    public void refreshSeriesPane(MediaSeriesGroup series) {
        SeriesPane seriesPane = this.getSeriesPane(series);
        if (seriesPane != null) {
            SwingUtilities.invokeLater(seriesPane::updateText);
        }
    }

    public MediaSeriesGroup getPatientForStudy(MediaSeriesGroup study) {
        return this.explorer.getDataExplorerModel().getParent(study, DicomModel.patient);
    }

    public MediaSeriesGroup getStudyForSeries(MediaSeriesGroup series) {
        return this.explorer.getDataExplorerModel().getParent(series, DicomModel.study);
    }

    public boolean hasVisibleSeries(MediaSeriesGroup study) {
        return !this.getSeriesList(study).isEmpty();
    }

    public boolean hasVisibleStudies(MediaSeriesGroup patient) {
        return !this.getStudyList(patient).isEmpty();
    }

    public void dispose() {
        this.patient2study.clear();
        this.study2series.clear();
    }
}

