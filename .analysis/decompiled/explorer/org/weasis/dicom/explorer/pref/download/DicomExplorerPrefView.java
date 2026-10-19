/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.explorer.DataExplorerView
 *  org.weasis.core.api.gui.util.AbstractItemDialogPage
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.media.data.SeriesThumbnail
 *  org.weasis.core.api.service.WProperties
 */
package org.weasis.dicom.explorer.pref.download;

import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import org.weasis.core.api.explorer.DataExplorerView;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.SeriesThumbnail;
import org.weasis.core.api.service.WProperties;
import org.weasis.dicom.explorer.DicomSorter;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.main.DicomExplorer;
import org.weasis.dicom.explorer.main.SeriesFilter;

public class DicomExplorerPrefView
extends AbstractItemDialogPage {
    public static final String DOWNLOAD_IMMEDIATELY = "weasis.download.immediately";
    public static final String STUDY_DATE_SORTING = "weasis.sorting.study.date";
    public static final String FILTER_DEFAULT_MODE = "weasis.dicom.explorer.filter.mode";
    private final JCheckBox downloadImmediatelyCheckbox = new JCheckBox(Messages.getString("SeriesDownloadPrefView.downloadImmediatelyCheckbox"));
    private final JSpinner spinner;
    private final JComboBox<DicomSorter.SortingTime> studyDateSortingComboBox = new JComboBox<DicomSorter.SortingTime>(DicomSorter.SortingTime.values());
    private final JComboBox<SeriesFilter.Mode> filterModeComboBox = new JComboBox<SeriesFilter.Mode>(SeriesFilter.Mode.values());
    private final JCheckBox showUnsupportedSopInfoCheckbox = new JCheckBox(Messages.getString("dicom.unsupported.sop.notify"));

    public DicomExplorerPrefView() {
        super(Messages.getString("DicomExplorer.title"), 607);
        WProperties preferences = GuiUtils.getUICore().getSystemPreferences();
        JPanel panel = GuiUtils.getVerticalBoxLayoutPanel((JComponent[])new JComponent[0]);
        int thumbnailSize = SeriesThumbnail.getThumbnailSizeFromPreferences();
        JLabel thumbSize = new JLabel(Messages.getString("DicomExplorer.thmb_size"));
        SpinnerListModel model = new SpinnerListModel(List.of(Integer.valueOf(48), Integer.valueOf(144), Integer.valueOf(160), Integer.valueOf(176), Integer.valueOf(192), Integer.valueOf(208), Integer.valueOf(224), Integer.valueOf(240), Integer.valueOf(256)));
        this.spinner = new JSpinner(model);
        model.setValue(thumbnailSize);
        panel.add(GuiUtils.getFlowLayoutPanel((JComponent[])new JComponent[]{thumbSize, this.spinner}));
        JLabel labelStudyDate = new JLabel(Messages.getString("study.date.sorting") + ":");
        this.studyDateSortingComboBox.setSelectedItem((Object)DicomSorter.getStudyDateSorting());
        panel.add(GuiUtils.getFlowLayoutPanel((JComponent[])new JComponent[]{labelStudyDate, this.studyDateSortingComboBox}));
        JLabel labelFilterMode = new JLabel(Messages.getString("DicomExplorer.filter_default") + ":");
        this.filterModeComboBox.setSelectedItem((Object)DicomExplorerPrefView.getDefaultFilterMode());
        panel.add(GuiUtils.getFlowLayoutPanel((JComponent[])new JComponent[]{labelFilterMode, this.filterModeComboBox}));
        panel.setBorder(GuiUtils.getTitledBorder((String)Messages.getString("display")));
        this.add(panel);
        this.add(GuiUtils.boxVerticalStrut((int)15));
        JPanel panel2 = GuiUtils.getVerticalBoxLayoutPanel((JComponent[])new JComponent[0]);
        this.downloadImmediatelyCheckbox.setSelected(preferences.getBooleanProperty(DOWNLOAD_IMMEDIATELY, true));
        panel2.add(GuiUtils.getFlowLayoutPanel((int)2, (int)5, (JComponent[])new JComponent[]{this.downloadImmediatelyCheckbox}));
        panel2.setBorder(GuiUtils.getTitledBorder((String)Messages.getString("DicomExplorer.open_win")));
        this.add(panel2);
        this.add(GuiUtils.boxVerticalStrut((int)15));
        JPanel panel3 = GuiUtils.getVerticalBoxLayoutPanel((JComponent[])new JComponent[0]);
        this.showUnsupportedSopInfoCheckbox.setSelected(preferences.getBooleanProperty("weasis.show.unsupported.sop.class.info", true));
        panel3.add(GuiUtils.getFlowLayoutPanel((int)2, (int)5, (JComponent[])new JComponent[]{this.showUnsupportedSopInfoCheckbox}));
        panel3.setBorder(GuiUtils.getTitledBorder((String)Messages.getString("notifications")));
        this.add(panel3);
        this.add(GuiUtils.boxYLastElement((int)5));
        this.getProperties().setProperty("show.apply", Boolean.TRUE.toString());
        this.getProperties().setProperty("show.restore", Boolean.TRUE.toString());
        this.getProperties().setProperty("help.item", "dicom-explorer/#preferences");
    }

    public void resetToDefaultValues() {
        WProperties preferences = GuiUtils.getUICore().getSystemPreferences();
        GuiUtils.getUICore().getSystemPreferences().resetProperty(DOWNLOAD_IMMEDIATELY, Boolean.TRUE.toString());
        this.downloadImmediatelyCheckbox.setSelected(preferences.getBooleanProperty(DOWNLOAD_IMMEDIATELY, true));
        preferences.resetProperty(STUDY_DATE_SORTING, String.valueOf(DicomSorter.SortingTime.INVERSE_CHRONOLOGICAL.getId()));
        this.studyDateSortingComboBox.setSelectedItem((Object)DicomSorter.getStudyDateSorting());
        preferences.resetProperty(FILTER_DEFAULT_MODE, SeriesFilter.Mode.TEXT.name());
        this.filterModeComboBox.setSelectedItem((Object)DicomExplorerPrefView.getDefaultFilterMode());
        this.spinner.setValue(144);
        preferences.resetProperty("weasis.show.unsupported.sop.class.info", Boolean.TRUE.toString());
        this.showUnsupportedSopInfoCheckbox.setSelected(preferences.getBooleanProperty("weasis.show.unsupported.sop.class.info", true));
    }

    public void closeAdditionalWindow() {
        DataExplorerView dicomView;
        SeriesFilter.Mode filterMode;
        WProperties preferences = GuiUtils.getUICore().getSystemPreferences();
        preferences.putBooleanProperty(DOWNLOAD_IMMEDIATELY, this.downloadImmediatelyCheckbox.isSelected());
        DicomSorter.SortingTime sortingTime = (DicomSorter.SortingTime)((Object)this.studyDateSortingComboBox.getSelectedItem());
        if (sortingTime != null) {
            preferences.putIntProperty(STUDY_DATE_SORTING, sortingTime.getId());
        }
        if ((filterMode = (SeriesFilter.Mode)((Object)this.filterModeComboBox.getSelectedItem())) != null) {
            preferences.setProperty(FILTER_DEFAULT_MODE, filterMode.name());
        }
        if ((dicomView = GuiUtils.getUICore().getExplorerPlugin(DicomExplorer.NAME)) instanceof DicomExplorer) {
            DicomExplorer explorer = (DicomExplorer)dicomView;
            int size = (Integer)this.spinner.getValue();
            preferences.putIntProperty("explorer.thumbnail.size", size);
            explorer.updateThumbnailSize(size);
        }
        preferences.putBooleanProperty("weasis.show.unsupported.sop.class.info", this.showUnsupportedSopInfoCheckbox.isSelected());
        GuiUtils.getUICore().saveSystemPreferences();
    }

    public static SeriesFilter.Mode getDefaultFilterMode() {
        String value = GuiUtils.getUICore().getSystemPreferences().getProperty(FILTER_DEFAULT_MODE, SeriesFilter.Mode.TEXT.name());
        try {
            return SeriesFilter.Mode.valueOf(value);
        }
        catch (IllegalArgumentException e) {
            return SeriesFilter.Mode.TEXT;
        }
    }
}

