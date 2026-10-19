/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  bibliothek.gui.dock.DockElementRepresentative
 *  bibliothek.gui.dock.common.CLocation
 *  bibliothek.gui.dock.common.mode.ExtendedMode
 *  bibliothek.gui.dock.control.focus.DefaultFocusRequest
 *  bibliothek.gui.dock.control.focus.FocusRequest
 *  com.formdev.flatlaf.icons.FlatSearchIcon
 *  com.formdev.flatlaf.ui.FlatUIUtils
 *  com.formdev.flatlaf.util.UIScale
 *  java.lang.MatchException
 *  net.miginfocom.swing.MigLayout
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.explorer.DataExplorerView
 *  org.weasis.core.api.explorer.ObservableEvent
 *  org.weasis.core.api.explorer.ObservableEvent$BasicAction
 *  org.weasis.core.api.explorer.model.DataExplorerModel
 *  org.weasis.core.api.gui.Insertable$Type
 *  org.weasis.core.api.gui.util.ActionW
 *  org.weasis.core.api.gui.util.GuiExecutor
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.media.data.MediaElement
 *  org.weasis.core.api.media.data.MediaSeries
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.MediaSeriesGroupNode
 *  org.weasis.core.api.media.data.SeriesThumbnail
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.util.FontItem
 *  org.weasis.core.api.util.ResourceUtil
 *  org.weasis.core.api.util.ResourceUtil$OtherIcon
 *  org.weasis.core.api.util.ResourceUtil$ResourceIconPath
 *  org.weasis.core.ui.docking.PluginTool
 *  org.weasis.core.ui.docking.PluginTool$POSITION
 *  org.weasis.core.ui.editor.SeriesViewerEvent
 *  org.weasis.core.ui.editor.SeriesViewerEvent$EVENT
 *  org.weasis.core.ui.editor.SeriesViewerListener
 *  org.weasis.core.ui.editor.image.ImageViewerPlugin
 *  org.weasis.core.ui.editor.image.SequenceHandler
 *  org.weasis.core.ui.editor.image.ViewCanvas
 *  org.weasis.core.ui.util.ArrayListComboBoxModel
 *  org.weasis.core.ui.util.DefaultAction
 *  org.weasis.core.ui.util.SearchableComboBox
 *  org.weasis.core.ui.util.TitleMenuItem
 *  org.weasis.core.util.StringUtil
 *  org.weasis.dicom.codec.DicomImageElement
 *  org.weasis.dicom.codec.DicomSeries
 *  org.weasis.dicom.codec.DicomSpecialElement
 *  org.weasis.dicom.codec.HiddenSeriesManager
 *  org.weasis.dicom.codec.KOSpecialElement
 *  org.weasis.dicom.codec.TagD
 *  org.weasis.dicom.codec.TagD$Level
 */
package org.weasis.dicom.explorer.main;

import bibliothek.gui.dock.DockElementRepresentative;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.mode.ExtendedMode;
import bibliothek.gui.dock.control.focus.DefaultFocusRequest;
import bibliothek.gui.dock.control.focus.FocusRequest;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.DataExplorerView;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.gui.Insertable;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.MediaSeriesGroupNode;
import org.weasis.core.api.media.data.SeriesThumbnail;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.util.FontItem;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.docking.PluginTool;
import org.weasis.core.ui.editor.SeriesViewerEvent;
import org.weasis.core.ui.editor.SeriesViewerListener;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.SequenceHandler;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.util.ArrayListComboBoxModel;
import org.weasis.core.ui.util.DefaultAction;
import org.weasis.core.ui.util.SearchableComboBox;
import org.weasis.core.ui.util.TitleMenuItem;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.DicomSpecialElement;
import org.weasis.dicom.codec.HiddenSeriesManager;
import org.weasis.dicom.codec.KOSpecialElement;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.DicomSeriesHandler;
import org.weasis.dicom.explorer.DicomSorter;
import org.weasis.dicom.explorer.HangingProtocols;
import org.weasis.dicom.explorer.LoadLocalDicom;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.exp.ExplorerTask;
import org.weasis.dicom.explorer.exp.ExportToolBar;
import org.weasis.dicom.explorer.imp.ImportToolBar;
import org.weasis.dicom.explorer.main.DicomPaneManager;
import org.weasis.dicom.explorer.main.DicomTaskManager;
import org.weasis.dicom.explorer.main.LoadingPanel;
import org.weasis.dicom.explorer.main.PatientPane;
import org.weasis.dicom.explorer.main.PatientSelectionManager;
import org.weasis.dicom.explorer.main.SeriesFilter;
import org.weasis.dicom.explorer.main.SeriesPane;
import org.weasis.dicom.explorer.main.SeriesSelectionModel;
import org.weasis.dicom.explorer.main.SplitSeriesManager;
import org.weasis.dicom.explorer.main.StudyPane;
import org.weasis.dicom.explorer.main.ThumbnailMouseAndKeyAdapter;
import org.weasis.dicom.explorer.pref.download.DicomExplorerPrefView;

public class DicomExplorer
extends PluginTool
implements DataExplorerView,
SeriesViewerListener,
PropertyChangeListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomExplorer.class);
    public static final String NAME = Messages.getString("DicomExplorer.title");
    public static final String BUTTON_NAME = Messages.getString("DicomExplorer.btn_title");
    public static final String DESCRIPTION = Messages.getString("DicomExplorer.desc");
    public static final String ALL_PATIENTS = Messages.getString("DicomExplorer.sel_all_pat");
    public static final String ALL_STUDIES = Messages.getString("DicomExplorer.sel_all_st");
    private final DicomModel model;
    private final DicomPaneManager paneManager;
    private final SplitSeriesManager splitSeriesManager;
    private final DicomTaskManager taskManager;
    private final PatientPane selectedPatient;
    private final JScrollPane thumbnailView;
    private final LoadingPanel loadingPanel;
    private final SeriesSelectionModel selectionList;
    private final JButton koOpen;
    private JPanel panelMain;
    private final ArrayListComboBoxModel<Object> modelPatient;
    private final SearchableComboBox<Object> patientCombobox;
    private final ItemListener patientItemListener;
    private final Map<MediaSeriesGroup, SeriesFilter> filtersByPatient = new HashMap<MediaSeriesGroup, SeriesFilter>();
    private SeriesFilter seriesFilter = new SeriesFilter();
    private final SearchableComboBox<Object> seriesFilterField = new SearchableComboBox();
    private final JButton modeButton = new JButton();
    private final JPopupMenu modePopup = new JPopupMenu();
    private final Map<SeriesFilter.Mode, JRadioButtonMenuItem> modeItems = new EnumMap<SeriesFilter.Mode, JRadioButtonMenuItem>(SeriesFilter.Mode.class);
    private final JLabel filterStatusLabel = new JLabel();
    private final Timer filterDebounce = new Timer(250, actionEvent -> this.runFilter());
    private final Set<String> availableModalities = new TreeSet<String>();
    private JPanel filterBar;
    private JTextField seriesFilterEditor;
    private boolean adjustingFilterUi;
    private final PatientSelectionManager patientSelectionManager;
    private final boolean verticalLayout = true;

    public DicomExplorer() {
        this(null);
    }

    public DicomExplorer(DicomModel model) {
        super(NAME, PluginTool.POSITION.WEST, ExtendedMode.NORMALIZED, Insertable.Type.EXPLORER, 20);
        this.model = model != null ? model : new DicomModel();
        this.paneManager = new DicomPaneManager(this);
        this.splitSeriesManager = new SplitSeriesManager(this);
        this.taskManager = new DicomTaskManager(this);
        this.selectedPatient = new PatientPane(this);
        this.thumbnailView = new JScrollPane();
        this.loadingPanel = new LoadingPanel();
        this.selectionList = new SeriesSelectionModel(this);
        this.modelPatient = new ArrayListComboBoxModel(DicomSorter.PATIENT_COMPARATOR);
        this.patientCombobox = new SearchableComboBox(this.modelPatient);
        this.koOpen = new JButton(Messages.getString("DicomExplorer.open_ko"), (Icon)ResourceUtil.getIcon((ResourceUtil.ResourceIconPath)ResourceUtil.OtherIcon.KEY_IMAGE));
        this.patientItemListener = this.createPatientItemListener();
        this.patientSelectionManager = new PatientSelectionManager(this);
        this.patientSelectionManager.addPatientSelectionListener(this.selectedPatient);
        this.dockable.setMaximizable(true);
        this.setupInitialSize();
        this.setupComboBoxes();
        this.setupFilterBar();
        this.setupThumbnailView();
        this.model.addPropertyChangeListener(this);
    }

    public String getDescription() {
        return DESCRIPTION;
    }

    public String getUIName() {
        return NAME;
    }

    public String toString() {
        return NAME;
    }

    protected void changeToolWindowAnchor(CLocation clocation) {
        this.selectedPatient.refreshLayout();
    }

    private void rebuildLayout() {
        this.removeAll();
        this.buildLayout();
        this.selectedPatient.refreshLayout();
    }

    private void buildLayout() {
        this.setLayout((LayoutManager)new MigLayout("fillx, ins 0", "[grow,fill]", "[]rel[grow,fill]unrel[]"));
        this.add(this.getMainPanel(), "");
        this.add(this.thumbnailView, "newline, top");
        this.add(this.loadingPanel, "newline,");
    }

    public PatientSelectionManager getPatientSelectionManager() {
        return this.patientSelectionManager;
    }

    private void updatePatientComboBoxSelection(MediaSeriesGroup patient) {
        if (patient == null || this.modelPatient.getSelectedItem() == patient || this.modelPatient.getIndexOf((Object)patient) < 0) {
            return;
        }
        this.patientCombobox.removeItemListener(this.patientItemListener);
        try {
            this.patientCombobox.setSelectedItem((Object)patient);
            this.showSelectedPatientFromStart();
        }
        finally {
            this.patientCombobox.addItemListener(this.patientItemListener);
        }
    }

    public void ensurePatientComboSelection() {
        this.updatePatientComboBoxSelection((MediaSeriesGroup)this.getSelectedPatient());
    }

    public boolean isVerticalLayout() {
        return true;
    }

    protected JPanel getMainPanel() {
        if (this.panelMain == null) {
            MigLayout layout = new MigLayout("fillx, ins 5 0 5 0", "[right]rel[grow,fill]", "[]10lp[]");
            this.panelMain = new JPanel((LayoutManager)layout);
            JPanel patientBar = new JPanel((LayoutManager)new MigLayout("ins 1, fillx", "[grow,fill]", ""));
            patientBar.add((Component)this.patientCombobox, "growx, width 30lp:30lp:250lp");
            this.panelMain.add((Component)patientBar, "newline, spanx, growx");
            this.filterBar = new JPanel((LayoutManager)new MigLayout("ins 1, fillx", "[grow,fill]", ""));
            this.filterBar.add((Component)this.seriesFilterField, "growx, width 30lp:30lp:250lp");
            this.panelMain.add((Component)this.filterBar, "newline, spanx, growx");
            this.panelMain.add((Component)this.filterStatusLabel, "newline, spanx, growx, hidemode 3");
            this.updateFilterIndicator();
            this.koOpen.setToolTipText(this.koOpen.getText());
            this.panelMain.add((Component)this.koOpen, "newline, spanx, alignx left, width 30lp:pref:pref, hidemode 2");
            this.koOpen.addActionListener(e -> {
                Object patt0$temp;
                MediaSeriesGroupNode patient = this.getSelectedPatient();
                if (patient != null && (patt0$temp = e.getSource()) instanceof JButton) {
                    JButton button = (JButton)patt0$temp;
                    List list = HiddenSeriesManager.getHiddenElementsFromPatient(KOSpecialElement.class, (MediaSeriesGroup)patient);
                    if (!list.isEmpty()) {
                        if (list.size() == 1) {
                            this.model.openRelatedSeries((KOSpecialElement)list.getFirst(), (MediaSeriesGroup)patient);
                        } else {
                            list.sort(DicomSpecialElement.ORDER_BY_DATE);
                            JPopupMenu popupMenu = new JPopupMenu();
                            popupMenu.add((Component)new TitleMenuItem(ActionW.KO_SELECTION.getTitle()));
                            popupMenu.addSeparator();
                            ButtonGroup group = new ButtonGroup();
                            for (KOSpecialElement koSpecialElement : list) {
                                JMenuItem item = new JMenuItem(koSpecialElement.getShortLabel());
                                item.addActionListener(arg_0 -> this.lambda$getMainPanel$1(koSpecialElement, (MediaSeriesGroup)patient, arg_0));
                                popupMenu.add(item);
                                group.add(item);
                            }
                            popupMenu.show(button, 0, button.getHeight());
                        }
                    }
                }
            });
            this.koOpen.setVisible(false);
        }
        return this.panelMain;
    }

    private void setupInitialSize() {
        this.setDockableWidth(this.computeThumbnailPanelWidth(SeriesThumbnail.getThumbnailSizeFromPreferences()));
    }

    private int computeThumbnailPanelWidth(int thumbnailSize) {
        int scrollBar = this.thumbnailView.getVerticalScrollBar().getPreferredSize().width;
        int margin = GuiUtils.getScaleLength((int)2);
        int column = this.measuredSingleColumnWidth();
        if (column <= 0) {
            column = GuiUtils.getScaleLength((int)Math.max(thumbnailSize, 144)) + StudyPane.getHorizontalChrome(this.selectedPatient);
        }
        return Math.round((float)(column + scrollBar + margin) / UIScale.getUserScaleFactor());
    }

    private int measuredSingleColumnWidth() {
        int max = 0;
        for (StudyPane studyPane : this.paneManager.getStudyList((MediaSeriesGroup)this.getSelectedPatient())) {
            max = Math.max(max, studyPane.getSingleColumnWidth());
        }
        return max;
    }

    private void adjustDockableWidth() {
        int width = this.computeThumbnailPanelWidth(SeriesThumbnail.getThumbnailSizeFromPreferences());
        if (width != this.getDockableWidth()) {
            this.updateDockableWidth(width);
        }
    }

    private void setupComboBoxes() {
        this.patientCombobox.setMaximumRowCount(15);
        this.patientCombobox.setFont(FontItem.SMALL_SEMIBOLD.getFont());
        this.patientCombobox.putClientProperty((Object)"FlatLaf.style", (Object)"padding: 3,2,3,0");
        JTextField patientEditor = (JTextField)this.patientCombobox.getEditor().getEditorComponent();
        this.patientCombobox.putClientProperty((Object)"JTextField.placeholderText", (Object)Messages.getString("DicomExplorer.search_patient"));
        JLabel patientIcon = new JLabel((Icon)ResourceUtil.getIcon((ResourceUtil.ResourceIconPath)ResourceUtil.OtherIcon.PATIENT, (int)16, (int)16));
        patientEditor.putClientProperty("JTextField.leadingComponent", patientIcon);
        this.patientCombobox.addItemListener(this.patientItemListener);
    }

    public SeriesFilter getSeriesFilter() {
        return this.seriesFilter;
    }

    private void setupFilterBar() {
        this.filterDebounce.setRepeats(false);
        this.seriesFilterField.setMaximumRowCount(15);
        this.seriesFilterField.setFont(FontItem.SMALL_SEMIBOLD.getFont());
        this.seriesFilterField.putClientProperty((Object)"FlatLaf.style", (Object)"padding: 3,2,3,0");
        this.seriesFilterEditor = (JTextField)this.seriesFilterField.getEditor().getEditorComponent();
        this.seriesFilterField.setSearchCallback(string -> this.onFilterInput());
        this.seriesFilterField.addActionListener(actionEvent -> this.onFilterSelection());
        this.seriesFilterField.addPopupMenuListener(new PopupMenuListener(this){
            final /* synthetic */ DicomExplorer this$0;
            {
                DicomExplorer dicomExplorer = this$0;
                Objects.requireNonNull(dicomExplorer);
                this.this$0 = dicomExplorer;
            }

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                this.this$0.refreshSuggestions();
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
        this.modeButton.setFocusable(false);
        this.modeButton.putClientProperty("JButton.buttonType", "toolBarButton");
        this.modeButton.putClientProperty("FlatLaf.style", "arc: 6; toolbar.hoverBackground: $Button.toolbar.selectedBackground; toolbar.pressedBackground: $Button.toolbar.pressedBackground");
        this.modeButton.setMargin(new Insets(0, 0, 0, 0));
        this.modeButton.addActionListener(actionEvent -> this.modePopup.show(this.modeButton, 0, this.modeButton.getHeight()));
        this.seriesFilterEditor.putClientProperty("JTextField.leadingComponent", this.modeButton);
        this.buildModePopup();
        this.filterStatusLabel.setFont(FontItem.SMALL.getFont());
        this.filterStatusLabel.setVisible(false);
        this.applyMode(DicomExplorerPrefView.getDefaultFilterMode());
    }

    private static SeriesFilter newSeriesFilter() {
        SeriesFilter filter = new SeriesFilter();
        filter.setMode(DicomExplorerPrefView.getDefaultFilterMode());
        return filter;
    }

    private void buildModePopup() {
        ButtonGroup group = new ButtonGroup();
        for (SeriesFilter.Mode mode : SeriesFilter.Mode.values()) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(mode.getTitle(), DicomExplorer.modeIcon(mode));
            item.setSelected(mode == this.seriesFilter.getMode());
            item.addActionListener(actionEvent -> this.applyMode(mode));
            group.add(item);
            this.modePopup.add(item);
            this.modeItems.put(mode, item);
        }
    }

    private void applyMode(SeriesFilter.Mode mode) {
        this.seriesFilter.setMode(mode);
        this.syncFilterToState((MediaSeriesGroup)this.getSelectedPatient());
        this.runFilter();
    }

    private void loadFilterForPatient(MediaSeriesGroup patient) {
        this.seriesFilter = patient == null ? DicomExplorer.newSeriesFilter() : this.filtersByPatient.computeIfAbsent(patient, mediaSeriesGroup -> DicomExplorer.newSeriesFilter());
        this.syncFilterToState(patient);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    private void syncFilterToState(MediaSeriesGroup patient) {
        mode = this.seriesFilter.getMode();
        this.modeButton.setIcon(DicomExplorer.modeIcon(mode));
        this.modeButton.setToolTipText(MessageFormat.format(Messages.getString("DicomExplorer.filter_mode"), new Object[]{mode.getTitle()}));
        modeItem = this.modeItems.get((Object)mode);
        if (modeItem != null) {
            modeItem.setSelected(true);
        }
        this.availableModalities.clear();
        this.availableModalities.addAll(this.collectSeriesValues(patient, 524384));
        this.adjustingFilterUi = true;
        try {
            this.seriesFilterField.putClientProperty((Object)"JTextField.placeholderText", (Object)DicomExplorer.modePlaceholder(mode));
            switch (3.$SwitchMap$org$weasis$dicom$explorer$main$SeriesFilter$Mode[mode.ordinal()]) {
                case 1: {
                    suggestions = this.collectSeriesValues(patient, 528446);
                    suggestions.addAll(this.collectStudyValues(patient, 528432));
                    this.setFieldItems(suggestions.toArray());
                    this.seriesFilterEditor.setText(this.seriesFilter.getText());
                    ** break;
lbl21:
                    // 1 sources

                    break;
                }
                case 2: {
                    this.setFieldItems(this.buildModalitySuggestions(patient).toArray());
                    this.seriesFilterEditor.setText(String.join((CharSequence)", ", this.seriesFilter.getModalities()));
                    ** break;
lbl26:
                    // 1 sources

                    break;
                }
                case 3: {
                    study = this.seriesFilter.getStudy();
                    if (study != null && this.paneManager.getStudyPane(study) == null) {
                        this.seriesFilter.setStudy(null);
                        study = null;
                    }
                    this.setFieldItems(this.buildStudyItems(patient));
                    this.seriesFilterField.setSelectedItem(study != null ? study : DicomExplorer.ALL_STUDIES);
                    this.seriesFilterEditor.setText("");
                    break;
                }
                ** default:
lbl37:
                // 1 sources

                break;
            }
        }
        finally {
            this.adjustingFilterUi = false;
        }
        this.showFilterValueFromStart();
        this.updateFilterIndicator();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private void refreshSuggestions() {
        if (this.adjustingFilterUi) return;
        if (this.seriesFilterField.isFiltering()) {
            return;
        }
        MediaSeriesGroupNode patient = this.getSelectedPatient();
        this.availableModalities.clear();
        this.availableModalities.addAll(this.collectSeriesValues((MediaSeriesGroup)patient, 524384));
        this.adjustingFilterUi = true;
        try {
            String current = this.seriesFilterEditor.getText();
            switch (this.seriesFilter.getMode()) {
                case TEXT: {
                    Set<String> suggestions = this.collectSeriesValues((MediaSeriesGroup)patient, 528446);
                    suggestions.addAll(this.collectStudyValues((MediaSeriesGroup)patient, 528432));
                    this.setFieldItems(suggestions.toArray());
                    this.seriesFilterEditor.setText(current);
                    return;
                }
                case MODALITY: {
                    this.setFieldItems(this.buildModalitySuggestions((MediaSeriesGroup)patient).toArray());
                    this.seriesFilterEditor.setText(current);
                    return;
                }
                case DATE: {
                    MediaSeriesGroup study = this.seriesFilter.getStudy();
                    this.setFieldItems(this.buildStudyItems((MediaSeriesGroup)patient));
                    this.seriesFilterField.setSelectedItem(study != null ? study : ALL_STUDIES);
                    return;
                }
            }
            return;
        }
        finally {
            this.adjustingFilterUi = false;
        }
    }

    private void onFilterInput() {
        if (this.adjustingFilterUi) {
            return;
        }
        String value = this.seriesFilterEditor.getText();
        switch (this.seriesFilter.getMode()) {
            case TEXT: {
                this.seriesFilter.setText(value);
                break;
            }
            case MODALITY: {
                this.seriesFilter.setModalities(this.parseModalities(value));
                break;
            }
        }
        this.filterDebounce.restart();
    }

    private void onFilterSelection() {
        if (this.adjustingFilterUi) {
            return;
        }
        if (this.seriesFilter.getMode() == SeriesFilter.Mode.DATE) {
            MediaSeriesGroup study;
            Object selected = this.seriesFilterField.getSelectedItem();
            this.seriesFilter.setStudy(selected instanceof MediaSeriesGroup ? (study = (MediaSeriesGroup)selected) : null);
            this.filterDebounce.restart();
        } else {
            this.onFilterInput();
        }
        if (!this.seriesFilterField.isFiltering()) {
            this.showFilterValueFromStart();
        }
    }

    private void showFilterValueFromStart() {
        if (this.seriesFilterEditor != null) {
            SwingUtilities.invokeLater(() -> this.seriesFilterEditor.setCaretPosition(0));
        }
    }

    private void runFilter() {
        this.applyFilterLayout();
    }

    private void applyFilterLayout() {
        this.selectionList.clear();
        if (this.getSelectedPatient() != null) {
            this.selectedPatient.showAllStudies();
        } else {
            this.selectedPatient.removeAll();
        }
        this.selectedPatient.repaint();
        this.updateFilterIndicator();
        SwingUtilities.invokeLater(this::adjustDockableWidth);
    }

    private void setFieldItems(Object[] items) {
        this.seriesFilterField.setModel(new DefaultComboBoxModel<Object>(items));
        this.seriesFilterField.setSelectedItem(null);
    }

    private Object[] buildStudyItems(MediaSeriesGroup patient) {
        ArrayList<String> items = new ArrayList<String>();
        items.add(ALL_STUDIES);
        if (patient != null) {
            for (StudyPane studyPane : this.paneManager.getStudyList(patient)) {
                items.add((String)studyPane.getDicomStudy());
            }
        }
        return items.toArray();
    }

    private List<String> buildModalitySuggestions(MediaSeriesGroup patient) {
        ArrayList<String> suggestions = new ArrayList<String>(this.availableModalities);
        if (patient != null) {
            TreeSet<String> combinations = new TreeSet<String>();
            for (StudyPane studyPane : this.paneManager.getStudyList(patient)) {
                TreeSet<String> perStudy = new TreeSet<String>();
                for (SeriesPane seriesPane : this.paneManager.getSeriesList(studyPane.getDicomStudy())) {
                    String modality = (String)TagD.getTagValue((TagReadable)seriesPane.getDicomSeries(), (int)524384, String.class);
                    if (!StringUtil.hasText((CharSequence)modality)) continue;
                    perStudy.add(modality);
                }
                if (perStudy.size() <= 1) continue;
                combinations.add(String.join((CharSequence)", ", perStudy));
            }
            suggestions.addAll(combinations);
        }
        return suggestions;
    }

    private Set<String> parseModalities(String value) {
        LinkedHashSet<String> result = new LinkedHashSet<String>();
        if (StringUtil.hasText((CharSequence)value)) {
            for (String token : value.split("[,\\s]+")) {
                if (!StringUtil.hasText((CharSequence)token)) continue;
                result.add(token.trim().toUpperCase());
            }
        }
        return result;
    }

    private Set<String> collectSeriesValues(MediaSeriesGroup patient, int tagId) {
        TreeSet<String> values = new TreeSet<String>();
        if (patient != null) {
            for (StudyPane studyPane : this.paneManager.getStudyList(patient)) {
                for (SeriesPane seriesPane : this.paneManager.getSeriesList(studyPane.getDicomStudy())) {
                    String value = (String)TagD.getTagValue((TagReadable)seriesPane.getDicomSeries(), (int)tagId, String.class);
                    if (!StringUtil.hasText((CharSequence)value)) continue;
                    values.add(value);
                }
            }
        }
        return values;
    }

    private Set<String> collectStudyValues(MediaSeriesGroup patient, int tagId) {
        TreeSet<String> values = new TreeSet<String>();
        if (patient != null) {
            for (StudyPane studyPane : this.paneManager.getStudyList(patient)) {
                String value = (String)TagD.getTagValue((TagReadable)studyPane.getDicomStudy(), (int)tagId, String.class);
                if (!StringUtil.hasText((CharSequence)value)) continue;
                values.add(value);
            }
        }
        return values;
    }

    private void updateFilterIndicator() {
        boolean active = this.seriesFilter.isActive();
        if (this.filterBar == null) {
            return;
        }
        if (active) {
            int[] counts = this.countSeries();
            Color accent = FlatUIUtils.getUIColor((String)"Component.accentColor", (Color)Color.BLUE);
            this.filterBar.setBorder(BorderFactory.createLineBorder(accent));
            this.filterStatusLabel.setForeground(accent);
            this.filterStatusLabel.setText(MessageFormat.format(Messages.getString("DicomExplorer.filter_status"), counts[0], counts[1]));
            this.filterStatusLabel.setVisible(true);
        } else {
            this.filterBar.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            this.filterStatusLabel.setText("");
            this.filterStatusLabel.setVisible(false);
        }
        this.filterBar.revalidate();
        this.filterBar.repaint();
    }

    private int[] countSeries() {
        MediaSeriesGroupNode patient = this.getSelectedPatient();
        int matching = 0;
        int total = 0;
        if (patient != null) {
            for (StudyPane studyPane : this.paneManager.getStudyList((MediaSeriesGroup)patient)) {
                MediaSeriesGroup study = studyPane.getDicomStudy();
                for (SeriesPane seriesPane : this.paneManager.getSeriesList(study)) {
                    ++total;
                    if (!this.seriesFilter.test((MediaSeriesGroup)seriesPane.getDicomSeries(), study)) continue;
                    ++matching;
                }
            }
        }
        return new int[]{matching, total};
    }

    private static String modePlaceholder(SeriesFilter.Mode mode) {
        return switch (mode) {
            default -> throw new MatchException(null, null);
            case SeriesFilter.Mode.TEXT -> Messages.getString("DicomExplorer.filter_ph_text");
            case SeriesFilter.Mode.DATE -> Messages.getString("DicomExplorer.filter_ph_date");
            case SeriesFilter.Mode.MODALITY -> Messages.getString("DicomExplorer.filter_ph_modality");
        };
    }

    private static Icon modeIcon(SeriesFilter.Mode mode) {
        return switch (mode) {
            default -> throw new MatchException(null, null);
            case SeriesFilter.Mode.TEXT -> new FlatSearchIcon();
            case SeriesFilter.Mode.DATE -> ResourceUtil.getIcon((ResourceUtil.ResourceIconPath)ResourceUtil.OtherIcon.CALENDAR, (int)16, (int)16);
            case SeriesFilter.Mode.MODALITY -> ResourceUtil.getIcon((ResourceUtil.ResourceIconPath)ResourceUtil.OtherIcon.MODALITY, (int)16, (int)16);
        };
    }

    private void setupThumbnailView() {
        this.thumbnailView.setBorder(BorderFactory.createEmptyBorder());
        this.thumbnailView.getVerticalScrollBar().setUnitIncrement(16);
        this.thumbnailView.setViewportView(this.selectedPatient);
        this.thumbnailView.setVerticalScrollBarPolicy(20);
        this.thumbnailView.setHorizontalScrollBarPolicy(31);
        this.thumbnailView.getViewport().addComponentListener(new ComponentAdapter(this){
            final /* synthetic */ DicomExplorer this$0;
            {
                DicomExplorer dicomExplorer = this$0;
                Objects.requireNonNull(dicomExplorer);
                this.this$0 = dicomExplorer;
            }

            @Override
            public void componentResized(ComponentEvent e) {
                this.this$0.paneManager.getStudyList((MediaSeriesGroup)this.this$0.getSelectedPatient()).forEach(StudyPane::refreshLayoutAsync);
            }
        });
        this.rebuildLayout();
        this.setTransferHandler((TransferHandler)((Object)new SeriesHandler()));
    }

    private ItemListener createPatientItemListener() {
        return e -> {
            Object patt0$temp;
            if (e.getStateChange() == 1 && !this.patientCombobox.isFiltering() && (patt0$temp = e.getItem()) instanceof MediaSeriesGroup) {
                MediaSeriesGroup patient = (MediaSeriesGroup)patt0$temp;
                this.selectPatient(patient);
                this.showSelectedPatientFromStart();
            }
        };
    }

    private void showSelectedPatientFromStart() {
        Component component = this.patientCombobox.getEditor().getEditorComponent();
        if (component instanceof JTextField) {
            JTextField field = (JTextField)component;
            SwingUtilities.invokeLater(() -> field.setCaretPosition(0));
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt instanceof ObservableEvent) {
            ObservableEvent event = (ObservableEvent)evt;
            this.handleObservableEvent(event);
        }
    }

    private void handleObservableEvent(ObservableEvent event) {
        ObservableEvent.BasicAction action = event.getActionCommand();
        Object newVal = event.getNewValue();
        switch (action) {
            case SELECT: {
                this.handleSelectEvent(newVal);
                break;
            }
            case ADD: {
                this.handleAddEvent(newVal);
                break;
            }
            case REMOVE: {
                this.handleRemoveEvent(newVal);
                break;
            }
            case UPDATE: {
                this.handleUpdateEvent(event);
                break;
            }
            case REPLACE: {
                this.handleReplaceEvent(event);
                break;
            }
            case LOADING_START: {
                this.showLoadingPanel(event);
                break;
            }
            case LOADING_STOP: 
            case LOADING_CANCEL: {
                this.hideLoadingPanel(event);
                break;
            }
            case LOADING_GLOBAL_MSG: {
                this.loadingPanelMessage(event);
                break;
            }
            default: {
                LOGGER.trace("Unhandled observable event: {}", (Object)action);
            }
        }
    }

    private void handleSelectEvent(Object newVal) {
        MediaSeriesGroup patient = null;
        if (newVal instanceof DicomSeries) {
            DicomSeries dcm = (DicomSeries)newVal;
            patient = this.model.getParent((MediaSeriesGroup)dcm, DicomModel.patient);
        } else if (newVal instanceof MediaSeriesGroup) {
            MediaSeriesGroup group = (MediaSeriesGroup)newVal;
            TagW tagID = group.getTagID();
            if (TagD.getUID((TagD.Level)TagD.Level.PATIENT).equals((Object)tagID)) {
                patient = group;
            }
        }
        if (patient != null && !this.isSelectedPatient(patient)) {
            if (this.modelPatient.getIndexOf((Object)patient) < 0) {
                this.modelPatient.addElement((Object)patient);
            }
            this.modelPatient.setSelectedItem((Object)patient);
            GuiUtils.getUICore().getDockingControl().getController().setFocusedDockable((FocusRequest)new DefaultFocusRequest((DockElementRepresentative)this.dockable.intern(), (Component)((Object)this), false, true, false));
        }
    }

    private void handleAddEvent(Object newVal) {
        if (newVal instanceof DicomSeries) {
            DicomSeries series = (DicomSeries)newVal;
            this.addDicomSeries(series);
        }
    }

    private void handleRemoveEvent(Object newVal) {
        if (newVal instanceof MediaSeriesGroup) {
            MediaSeriesGroup group = (MediaSeriesGroup)newVal;
            if (group.getTagID().equals((Object)DicomModel.patient.tagElement())) {
                this.paneManager.removePatientPane(group);
            } else if (group.getTagID().equals((Object)DicomModel.study.tagElement())) {
                this.paneManager.removeStudyPane(group);
            } else if (group.getTagID().equals((Object)DicomModel.series.tagElement())) {
                this.paneManager.removeSeriesPane(group);
            }
        }
    }

    private void handleUpdateEvent(ObservableEvent event) {
        Object newVal = event.getNewValue();
        if (newVal instanceof DicomSeries) {
            DicomSeries series = (DicomSeries)newVal;
            this.updateSplitSeries(series);
        }
    }

    private void handleReplaceEvent(ObservableEvent event) {
        Object oldVal = event.getOldValue();
        Object newVal = event.getNewValue();
        if (oldVal instanceof DicomSeries) {
            DicomSeries oldSeries = (DicomSeries)oldVal;
            if (newVal instanceof DicomSeries) {
                DicomSeries newSeries = (DicomSeries)newVal;
                this.handleSeriesReplacement(oldSeries, newSeries);
            }
        }
    }

    public void selectPatient(MediaSeriesGroup patient) {
        if (!this.patientSelectionManager.isCurrentPatient(patient)) {
            this.patientSelectionManager.setCurrentPatient(patient);
            this.updatePatientComboBoxSelection(patient);
            this.onPatientChanged(patient);
            this.selectedPatient.repaint();
        }
    }

    private void onPatientChanged(MediaSeriesGroup patient) {
        this.loadFilterForPatient(patient);
        this.applyFilterLayout();
        this.koOpen.setVisible(HiddenSeriesManager.hasHiddenSpecialElements(KOSpecialElement.class, (MediaSeriesGroup)patient));
        this.model.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.SELECT, (Object)this.model, null, (Object)patient));
    }

    public MediaSeriesGroupNode getSelectedPatient() {
        return this.selectedPatient.getCurrentPatient().orElse(null);
    }

    private boolean isSelectedPatient(MediaSeriesGroup patient) {
        return this.patientSelectionManager.isCurrentPatient(patient);
    }

    private void addDicomSeries(DicomSeries series) {
        if (DicomModel.isHiddenModality(series)) {
            return;
        }
        LOGGER.info("Add series: {}", (Object)series);
        MediaSeriesGroup study = this.paneManager.getStudyForSeries((MediaSeriesGroup)series);
        MediaSeriesGroup patient = this.paneManager.getPatientForStudy(study);
        if (patient == null) {
            LOGGER.warn("No patient found for series: {}", (Object)series);
            return;
        }
        if (this.modelPatient.getIndexOf((Object)patient) < 0) {
            this.modelPatient.addElement((Object)patient);
            if (this.modelPatient.getSize() == 1) {
                this.modelPatient.setSelectedItem((Object)patient);
            }
        }
        int[] positionStudy = new int[1];
        StudyPane studyPane = this.paneManager.createStudyPaneInstance(study, positionStudy);
        int[] positionSeries = new int[1];
        this.paneManager.createSeriesPaneInstance(series, positionSeries);
        if (this.isSelectedPatient(patient) && positionSeries[0] != -1) {
            if (positionStudy[0] != -1) {
                this.applyFilterLayout();
            } else {
                this.addSeriesToStudyView(study, studyPane, series);
            }
        }
    }

    private void addSeriesToStudyView(MediaSeriesGroup study, StudyPane studyPane, DicomSeries series) {
        if (!this.seriesFilter.test((MediaSeriesGroup)series, study)) {
            return;
        }
        SeriesPane seriesPane = this.paneManager.getSeriesPane((MediaSeriesGroup)series);
        if (seriesPane == null) {
            return;
        }
        boolean wasVisible = this.selectedPatient.isStudyVisible(study);
        studyPane.addPane(seriesPane, this.visibleSeriesIndex(study, series), SeriesThumbnail.getThumbnailSizeFromPreferences());
        if (!wasVisible) {
            this.selectedPatient.addPane(studyPane);
        }
        studyPane.refreshLayout();
        this.selectedPatient.revalidate();
        this.selectedPatient.repaint();
    }

    private int visibleSeriesIndex(MediaSeriesGroup study, DicomSeries series) {
        SeriesPane pane;
        DicomSeries current;
        int index = 0;
        Iterator<SeriesPane> iterator = this.paneManager.getSeriesList(study).iterator();
        while (iterator.hasNext() && !(current = (pane = iterator.next()).getDicomSeries()).equals((Object)series)) {
            if (!this.seriesFilter.test((MediaSeriesGroup)current, study)) continue;
            ++index;
        }
        return index;
    }

    public void updateRemovedPatient(MediaSeriesGroup patient) {
        SwingUtilities.invokeLater(() -> {
            this.modelPatient.removeElement((Object)patient);
            this.filtersByPatient.remove(patient);
            if (this.modelPatient.getSize() == 0) {
                this.koOpen.setVisible(false);
                this.resetFilter();
                this.selectionList.clear();
                this.patientSelectionManager.clearCurrentPatient();
            }
            if (this.selectedPatient.isPatient(patient)) {
                this.selectedPatient.refreshLayout();
            }
            this.thumbnailView.revalidate();
            this.thumbnailView.repaint();
        });
    }

    private void resetFilter() {
        this.filtersByPatient.clear();
        this.seriesFilter = DicomExplorer.newSeriesFilter();
        this.syncFilterToState(null);
    }

    public void updateRemovedStudy(StudyPane studyPane, MediaSeriesGroup study) {
        SwingUtilities.invokeLater(() -> {
            if (this.selectedPatient.isStudyVisible(study)) {
                this.selectedPatient.remove(studyPane);
                this.selectedPatient.revalidate();
                this.selectedPatient.repaint();
            }
        });
    }

    private void handleSeriesReplacement(DicomSeries oldSeries, DicomSeries newSeries) {
        this.splitSeriesManager.markSeriesAsReplacement(oldSeries);
        if (newSeries instanceof DicomSeries) {
            DicomSeries dicomSeries = newSeries;
            this.updateSplitSeries(dicomSeries);
        }
    }

    public void updateSplitSeries(DicomSeries dcmSeries) {
        if (dcmSeries != null) {
            this.splitSeriesManager.updateSplitSeries(dcmSeries);
        }
    }

    public List<DicomSeries> getSplitSeries(DicomSeries series) {
        return this.splitSeriesManager.getSplitSeries(series);
    }

    public PatientPane getSelectedPatientPane() {
        return this.selectedPatient;
    }

    public void updateThumbnailSize(int thumbnailSize) {
        this.updateDockableWidth(this.computeThumbnailPanelWidth(thumbnailSize));
        MediaSeriesGroupNode patient = this.getSelectedPatient();
        for (StudyPane studyPane : this.paneManager.getStudyList((MediaSeriesGroup)patient)) {
            studyPane.updateThumbnailSize(thumbnailSize);
            studyPane.doLayout();
        }
        this.selectedPatient.revalidate();
        this.selectedPatient.repaint();
    }

    public MediaSeries<? extends MediaElement> movePatient(ViewCanvas<DicomImageElement> view, ListPosition position) {
        MediaSeriesGroup seriesGroup;
        if (view == null) {
            return null;
        }
        MediaSeries series = view.getSeries();
        if (series == null) {
            seriesGroup = this.getSeries(null, null, ListPosition.FIRST);
        } else {
            MediaSeriesGroup studyGroup = this.model.getParent((MediaSeriesGroup)series, DicomModel.study);
            MediaSeriesGroup patientGroup = this.model.getParent(studyGroup, DicomModel.patient);
            seriesGroup = this.getSeriesGroupFromPatient(patientGroup, position);
        }
        if (seriesGroup instanceof DicomSeries) {
            DicomSeries dicomSeries = (DicomSeries)seriesGroup;
            ThumbnailMouseAndKeyAdapter.openSeriesInDefaultPlugin(dicomSeries, this.model);
        }
        return null;
    }

    public MediaSeries<? extends MediaElement> moveStudy(ViewCanvas<DicomImageElement> view, ListPosition position) {
        if (view == null) {
            return null;
        }
        MediaSeries series = view.getSeries();
        MediaSeriesGroup seriesGroup = series == null ? this.getSeries(null, null, ListPosition.FIRST) : this.getSeriesGroupFromStudy(this.model.getParent((MediaSeriesGroup)series, DicomModel.study), position);
        return this.displaySeries(view, seriesGroup);
    }

    public MediaSeries<? extends MediaElement> moveSeries(ViewCanvas<DicomImageElement> view, ListPosition position) {
        if (view == null) {
            return null;
        }
        MediaSeriesGroup seriesGroup = this.getSeriesGroup((MediaSeriesGroup)view.getSeries(), position);
        return this.displaySeries(view, seriesGroup);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Set<DicomSeries> getSelectedPatientOpenSeries() {
        MediaSeriesGroupNode patient = this.getSelectedPatient();
        if (patient != null) {
            LinkedHashSet<DicomSeries> openSeriesSet = new LinkedHashSet<DicomSeries>();
            DicomModel dicomModel = this.model;
            synchronized (dicomModel) {
                for (MediaSeriesGroup study : this.model.getChildren((MediaSeriesGroup)patient)) {
                    for (MediaSeriesGroup seq : this.model.getChildren(study)) {
                        if (!(seq instanceof DicomSeries)) continue;
                        DicomSeries series = (DicomSeries)seq;
                        if (!Boolean.TRUE.equals(seq.getTagValue(TagW.SeriesOpen))) continue;
                        openSeriesSet.add(series);
                    }
                }
            }
            return openSeriesSet;
        }
        return Set.of();
    }

    public MediaSeriesGroup getSeriesGroupFromPatient(MediaSeriesGroup patientGroup, ListPosition position) {
        if (patientGroup == null) {
            return this.getSeries(null, null, ListPosition.FIRST);
        }
        MediaSeriesGroup patient = this.getPatientFromList(patientGroup, position);
        return this.getSeries(this.getFirstStudy(patient), null, ListPosition.FIRST);
    }

    public MediaSeriesGroup getSeriesGroupFromStudy(MediaSeriesGroup studyGroup, ListPosition position) {
        if (studyGroup == null) {
            return this.getSeries(null, null, ListPosition.FIRST);
        }
        MediaSeriesGroup study = this.getStudyFromList(studyGroup, position);
        return this.getSeries(study, null, ListPosition.FIRST);
    }

    public MediaSeriesGroup getSeriesGroup(MediaSeriesGroup series, ListPosition position) {
        if (series == null) {
            return this.getSeries(null, null, position);
        }
        MediaSeriesGroup studyGroup = this.model.getParent(series, DicomModel.study);
        return this.getSeries(studyGroup, series, position);
    }

    private MediaSeriesGroup getSeries(MediaSeriesGroup studyGroup, MediaSeriesGroup series, ListPosition position) {
        if (studyGroup == null || series == null) {
            List<StudyPane> studyList = this.paneManager.getStudyList(this.paneManager.getPatientForStudy(studyGroup));
            for (StudyPane studyPane : studyList) {
                List<SeriesPane> seriesList = this.paneManager.getSeriesList(studyPane.getDicomStudy());
                if (seriesList.isEmpty()) continue;
                if (position == ListPosition.LAST) {
                    return ((SeriesPane)seriesList.getLast()).getDicomSeries();
                }
                return ((SeriesPane)seriesList.getFirst()).getDicomSeries();
            }
        } else {
            return this.getSeriesFromList(studyGroup, series, position);
        }
        return null;
    }

    private MediaSeriesGroup getSeriesFromList(MediaSeriesGroup studyGroup, MediaSeriesGroup series, ListPosition position) {
        List<SeriesPane> seriesList = this.paneManager.getSeriesList(studyGroup);
        if (!seriesList.isEmpty()) {
            if (series == null) {
                if (position == ListPosition.LAST) {
                    return ((SeriesPane)seriesList.getLast()).getDicomSeries();
                }
                return ((SeriesPane)seriesList.getFirst()).getDicomSeries();
            }
            int index = 0;
            if (position == ListPosition.LAST) {
                index = seriesList.size() - 1;
            } else if (position == ListPosition.PREVIOUS) {
                index = this.getSeriesIndex(seriesList, series) - 1;
            } else if (position == ListPosition.NEXT) {
                index = this.getSeriesIndex(seriesList, series) + 1;
            }
            if (index >= 0 && index < seriesList.size()) {
                return seriesList.get(index).getDicomSeries();
            }
        }
        return null;
    }

    private MediaSeriesGroup getPatientFromList(MediaSeriesGroup patientGroup, ListPosition position) {
        if (patientGroup == null) {
            return this.getSelectedPatient();
        }
        if (this.modelPatient.getSize() > 0) {
            Object object;
            int index = 0;
            if (position == ListPosition.LAST) {
                index = this.modelPatient.getSize() - 1;
            } else if (position == ListPosition.PREVIOUS) {
                index = this.getPatientIndex(patientGroup) - 1;
            } else if (position == ListPosition.NEXT) {
                index = this.getPatientIndex(patientGroup) + 1;
            }
            if (index >= 0 && index < this.modelPatient.getSize() && (object = this.modelPatient.getElementAt(index)) instanceof MediaSeriesGroupNode) {
                MediaSeriesGroupNode patient = (MediaSeriesGroupNode)object;
                return patient;
            }
        }
        return null;
    }

    private MediaSeriesGroup getStudyFromList(MediaSeriesGroup studyGroup, ListPosition position) {
        MediaSeriesGroup patientGroup = this.paneManager.getPatientForStudy(studyGroup);
        if (patientGroup == null || studyGroup == null) {
            return this.getFirstStudy(patientGroup);
        }
        List<StudyPane> studyPanes = this.paneManager.getStudyList(patientGroup);
        if (studyPanes != null && !studyPanes.isEmpty()) {
            int index = 0;
            if (position == ListPosition.LAST) {
                index = studyPanes.size() - 1;
            } else if (position == ListPosition.PREVIOUS) {
                index = this.getStudyIndex(studyPanes, studyGroup) - 1;
            } else if (position == ListPosition.NEXT) {
                index = this.getStudyIndex(studyPanes, studyGroup) + 1;
            }
            if (index >= 0 && index < studyPanes.size()) {
                return studyPanes.get(index).getDicomStudy();
            }
        }
        return null;
    }

    private MediaSeriesGroup getFirstStudy(MediaSeriesGroup patient) {
        List<StudyPane> studyList = this.paneManager.getStudyList(patient);
        if (!studyList.isEmpty()) {
            return ((StudyPane)studyList.getFirst()).getDicomStudy();
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int getPatientIndex(MediaSeriesGroup patientGroup) {
        if (patientGroup != null) {
            ArrayListComboBoxModel<Object> arrayListComboBoxModel = this.modelPatient;
            synchronized (arrayListComboBoxModel) {
                for (int i = 0; i < this.modelPatient.getSize(); ++i) {
                    if (!patientGroup.equals(this.modelPatient.getElementAt(i))) continue;
                    return i;
                }
            }
        }
        return 0;
    }

    private int getStudyIndex(List<StudyPane> studyList, MediaSeriesGroup studyGroup) {
        if (studyList != null && !studyList.isEmpty()) {
            for (int i = 0; i < studyList.size(); ++i) {
                StudyPane st = studyList.get(i);
                if (!st.isStudy(studyGroup)) continue;
                return i;
            }
        }
        return 0;
    }

    private int getSeriesIndex(List<SeriesPane> seriesList, MediaSeriesGroup seriesGroup) {
        if (seriesList != null && !seriesList.isEmpty()) {
            for (int i = 0; i < seriesList.size(); ++i) {
                SeriesPane se = seriesList.get(i);
                if (!se.isSeries(seriesGroup)) continue;
                return i;
            }
        }
        return 0;
    }

    private MediaSeries<DicomImageElement> displaySeries(ViewCanvas<DicomImageElement> view, MediaSeriesGroup seriesGroup) {
        if (view != null && seriesGroup instanceof DicomSeries) {
            DicomSeries dicomSeries = (DicomSeries)seriesGroup;
            view.setSeries(null);
            view.setSeries((MediaSeries)dicomSeries, null);
            return dicomSeries;
        }
        return null;
    }

    private void loadingPanelMessage(ObservableEvent event) {
        Object object = event.getNewValue();
        if (object instanceof String) {
            String message = (String)object;
            this.loadingPanel.setGlobalMessage(message);
        }
    }

    private void showLoadingPanel(ObservableEvent event) {
        Object object = event.getNewValue();
        if (object instanceof ExplorerTask) {
            ExplorerTask task = (ExplorerTask)object;
            GuiExecutor.invokeAndWait(() -> {
                this.loadingPanel.addTask(task);
                this.revalidate();
                this.repaint();
            });
        }
    }

    private void hideLoadingPanel(ObservableEvent event) {
        MediaSeriesGroupNode patient;
        Object object = event.getNewValue();
        if (object instanceof ExplorerTask) {
            ExplorerTask task = (ExplorerTask)object;
            GuiExecutor.invokeAndWait(() -> {
                if (this.loadingPanel.removeTask(task)) {
                    this.revalidate();
                    this.repaint();
                }
            });
        }
        if ((patient = this.getSelectedPatient()) != null) {
            this.koOpen.setVisible(HiddenSeriesManager.hasHiddenSpecialElements(KOSpecialElement.class, (MediaSeriesGroup)patient));
        }
    }

    private MediaSeriesGroupNode getPatient(Object item) {
        if (item instanceof MediaSeriesGroupNode) {
            MediaSeriesGroupNode patient = (MediaSeriesGroupNode)item;
            return patient;
        }
        return null;
    }

    public DicomModel getDataExplorerModel() {
        return this.model;
    }

    public List<Action> getOpenImportDialogAction() {
        ArrayList<Action> actions = new ArrayList<Action>(2);
        actions.add((Action)ImportToolBar.buildImportAction((Component)((Object)this), this.model, BUTTON_NAME));
        DefaultAction importCDAction = new DefaultAction(Messages.getString("DicomExplorer.dcmCD"), (Icon)ResourceUtil.getIcon((ResourceUtil.ResourceIconPath)ResourceUtil.OtherIcon.CDROM), event -> ImportToolBar.openImportDicomCdAction((Component)((Object)this), this.model, Messages.getString("DicomExplorer.dcmCD")));
        actions.add((Action)importCDAction);
        return actions;
    }

    public List<Action> getOpenExportDialogAction() {
        return Collections.singletonList(ExportToolBar.buildExportAction((Component)((Object)this), this.model, BUTTON_NAME));
    }

    public void importFiles(File[] files, boolean recursive) {
        if (files != null) {
            DicomModel.LOADING_EXECUTOR.execute(new LoadLocalDicom(files, recursive, (DataExplorerModel)this.model, HangingProtocols.OpeningViewer.ALL_PATIENTS));
        }
    }

    public boolean canImportFiles() {
        return true;
    }

    public SeriesSelectionModel getSelectionList() {
        return this.selectionList;
    }

    public DicomTaskManager getTaskManager() {
        return this.taskManager;
    }

    public DicomPaneManager getPaneManager() {
        return this.paneManager;
    }

    public SplitSeriesManager getSplitSeriesManager() {
        return this.splitSeriesManager;
    }

    public void changingViewContentEvent(SeriesViewerEvent event) {
        SeriesPane p;
        MediaSeries s;
        ViewCanvas pane;
        SeriesViewerEvent.EVENT type = event.getEventType();
        if (SeriesViewerEvent.EVENT.SELECT_VIEW.equals((Object)type) && event.getSeriesViewer() instanceof ImageViewerPlugin && (pane = ((ImageViewerPlugin)event.getSeriesViewer()).getSelectedViewCanvas()) != null && (s = pane.getSeries()) != null && !this.getSelectionList().isOpeningSeries() && this.paneManager.containsSeriesInPatient(this.selectedPatient, (MediaSeriesGroup)s) && (p = this.paneManager.getSeriesPane((MediaSeriesGroup)s)) != null) {
            JViewport vp = this.thumbnailView.getViewport();
            Rectangle bound = vp.getViewRect();
            Point ptmin = SwingUtilities.convertPoint(p, new Point(0, 0), this.selectedPatient);
            Point ptmax = SwingUtilities.convertPoint(p, new Point(0, p.getHeight()), this.selectedPatient);
            if (!bound.contains(ptmin.x, ptmin.y) || !bound.contains(ptmax.x, ptmax.y)) {
                Point pt = vp.getViewPosition();
                pt.y = ptmin.y + (ptmax.y - ptmin.y) / 2;
                pt.y -= vp.getHeight() / 2;
                int maxHeight = (int)(vp.getViewSize().getHeight() - vp.getExtentSize().getHeight());
                if (pt.y < 0) {
                    pt.y = 0;
                } else if (pt.y > maxHeight) {
                    pt.y = maxHeight;
                }
                vp.setViewPosition(pt);
                this.getSelectionList().clear();
            }
        }
    }

    public void dispose() {
        if (this.model != null) {
            this.model.removePropertyChangeListener(this);
        }
        if (this.patientSelectionManager != null) {
            this.patientSelectionManager.removePatientSelectionListener(this.selectedPatient);
        }
        if (this.taskManager != null) {
            this.taskManager.shutdown();
        }
        if (this.paneManager != null) {
            this.paneManager.dispose();
        }
        if (this.splitSeriesManager != null) {
            this.splitSeriesManager.dispose();
        }
        super.closeDockable();
        LOGGER.info("DicomExplorer disposed");
    }

    private /* synthetic */ void lambda$getMainPanel$1(KOSpecialElement koSpecialElement, MediaSeriesGroup patient, ActionEvent actionEvent) {
        this.model.openRelatedSeries(koSpecialElement, patient);
    }

    private static class SeriesHandler
    extends SequenceHandler {
        public SeriesHandler() {
            super(false, true);
        }

        protected boolean dropFiles(List<Path> files) {
            return DicomSeriesHandler.dropDicomFiles(files);
        }
    }

    public static enum ListPosition {
        FIRST,
        PREVIOUS,
        NEXT,
        LAST;

    }
}

