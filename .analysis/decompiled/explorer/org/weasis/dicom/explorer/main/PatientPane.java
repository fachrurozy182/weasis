/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.miginfocom.swing.MigLayout
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 */
package org.weasis.dicom.explorer.main;

import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import net.miginfocom.swing.MigLayout;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.dicom.explorer.main.DicomExplorer;
import org.weasis.dicom.explorer.main.DicomPaneManager;
import org.weasis.dicom.explorer.main.PatientSelectionManager;
import org.weasis.dicom.explorer.main.StudyPane;

public class PatientPane
extends JPanel
implements PatientSelectionManager.PatientSelectionListener {
    private static final String VERTICAL_LAYOUT_CONSTRAINTS = "fillx, flowy, insets 0";
    private static final String HORIZONTAL_LAYOUT_CONSTRAINTS = "fillx, flowx, insets 0";
    private static final String COLUMN_CONSTRAINTS = "[grow,left]";
    private static final Dimension STUDY_PANE_SIZE = new Dimension(50, 50);
    private final DicomExplorer explorer;
    private MediaSeriesGroup currentPatient;

    public PatientPane(DicomExplorer explorer) {
        this.explorer = Objects.requireNonNull(explorer);
        this.initializeComponent();
    }

    private void initializeComponent() {
        this.setAlignmentX(0.0f);
        this.setAlignmentY(0.0f);
        this.setFocusable(false);
    }

    @Override
    public void patientSelected(MediaSeriesGroup patient) {
        this.currentPatient = patient;
        this.showTitle(false);
        this.refreshLayout();
    }

    @Override
    public void patientCleared() {
        this.currentPatient = null;
        this.showTitle(false);
        this.removeAll();
        this.revalidate();
        this.repaint();
    }

    public void showTitle(boolean show) {
        Optional<MediaSeriesGroup> patient = this.getCurrentPatient();
        if (show && patient.isPresent()) {
            TitledBorder title = GuiUtils.getTitledBorder((String)patient.get().toString());
            this.setBorder(BorderFactory.createCompoundBorder(GuiUtils.getEmptyBorder((int)0, (int)3, (int)5, (int)3), title));
        } else {
            this.setBorder(BorderFactory.createEmptyBorder());
        }
    }

    public boolean isStudyVisible(MediaSeriesGroup study) {
        if (study == null) {
            return false;
        }
        return this.getStudyPanes().anyMatch(studyPane -> studyPane.isStudy(study));
    }

    void refreshLayout() {
        this.updateLayout();
        this.removeAll();
        this.getCurrentPatient().map(patient -> this.explorer.getPaneManager().getStudyList((MediaSeriesGroup)patient)).ifPresent(studies -> studies.stream().filter(this::hasVisibleContent).forEach(this::addStudyPane));
        this.revalidate();
        this.repaint();
    }

    public void showAllStudies() {
        this.removeAll();
        Optional<MediaSeriesGroup> patient = this.getCurrentPatient();
        if (patient.isEmpty()) {
            this.revalidate();
            return;
        }
        DicomPaneManager paneManager = this.explorer.getPaneManager();
        for (StudyPane studyPane : paneManager.getStudyList(patient.get())) {
            studyPane.showAllSeries(paneManager);
            if (this.hasVisibleContent(studyPane)) {
                this.addPane(studyPane);
            }
            studyPane.doLayout();
        }
        this.revalidate();
    }

    public void addPane(StudyPane studyPane) {
        if (studyPane != null) {
            this.add(studyPane);
            studyPane.refreshLayout();
        }
    }

    public boolean isPatient(MediaSeriesGroup patient) {
        return Objects.equals(this.currentPatient, patient);
    }

    public Optional<MediaSeriesGroup> getCurrentPatient() {
        return Optional.ofNullable(this.currentPatient);
    }

    private void updateLayout() {
        String layoutConstraints = this.explorer.isVerticalLayout() ? VERTICAL_LAYOUT_CONSTRAINTS : HORIZONTAL_LAYOUT_CONSTRAINTS;
        this.setLayout((LayoutManager)new MigLayout(layoutConstraints, COLUMN_CONSTRAINTS));
    }

    private boolean hasVisibleContent(StudyPane studyPane) {
        return studyPane != null && studyPane.hasSeriesPanes();
    }

    private void addStudyPane(StudyPane studyPane) {
        studyPane.setSize(STUDY_PANE_SIZE);
        this.addPane(studyPane);
        studyPane.doLayout();
    }

    private Stream<StudyPane> getStudyPanes() {
        return Arrays.stream(this.getComponents()).filter(StudyPane.class::isInstance).map(StudyPane.class::cast);
    }
}

