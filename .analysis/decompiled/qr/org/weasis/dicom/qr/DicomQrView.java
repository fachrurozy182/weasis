/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.dcm4che3.data.Attributes
 *  org.dcm4che3.img.util.DicomObjectUtil
 *  org.dcm4che3.net.service.QueryRetrieveLevel
 *  org.osgi.framework.BundleContext
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.explorer.DataExplorerView
 *  org.weasis.core.api.explorer.model.DataExplorerModel
 *  org.weasis.core.api.gui.util.AbstractItemDialogPage
 *  org.weasis.core.api.gui.util.AppProperties
 *  org.weasis.core.api.gui.util.CheckBoxModel
 *  org.weasis.core.api.gui.util.DropDownButton
 *  org.weasis.core.api.gui.util.GroupCheckBoxMenu
 *  org.weasis.core.api.gui.util.GroupPopup
 *  org.weasis.core.api.gui.util.GroupRadioMenu
 *  org.weasis.core.api.gui.util.GuiExecutor
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.gui.util.WinUtil
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.Series
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.media.data.Taggable
 *  org.weasis.core.api.net.URLParameters
 *  org.weasis.core.api.net.auth.AuthMethod
 *  org.weasis.core.api.net.auth.OAuth2ServiceFactory
 *  org.weasis.core.api.service.BundlePreferences
 *  org.weasis.core.api.service.WProperties
 *  org.weasis.core.api.util.ThreadUtil
 *  org.weasis.core.ui.pref.PreferenceDialog
 *  org.weasis.core.ui.tp.raven.datetime.DatePicker
 *  org.weasis.core.ui.tp.raven.datetime.event.DateSelectionListener
 *  org.weasis.core.ui.tp.raven.spinner.SpinnerProgress
 *  org.weasis.core.util.FileUtil
 *  org.weasis.core.util.StreamUtil
 *  org.weasis.core.util.StringUtil
 *  org.weasis.dicom.codec.DicomSeries
 *  org.weasis.dicom.codec.TagD
 *  org.weasis.dicom.codec.display.CharsetEncoding
 *  org.weasis.dicom.codec.display.Modality
 *  org.weasis.dicom.explorer.DicomModel
 *  org.weasis.dicom.explorer.LoadLocalDicom
 *  org.weasis.dicom.explorer.Messages
 *  org.weasis.dicom.explorer.PluginOpeningStrategy
 *  org.weasis.dicom.explorer.imp.ImportDicom
 *  org.weasis.dicom.explorer.main.DicomExplorer
 *  org.weasis.dicom.explorer.pref.node.AbstractDicomNode
 *  org.weasis.dicom.explorer.pref.node.AbstractDicomNode$RetrieveType
 *  org.weasis.dicom.explorer.pref.node.AbstractDicomNode$Type
 *  org.weasis.dicom.explorer.pref.node.AbstractDicomNode$UsageType
 *  org.weasis.dicom.explorer.pref.node.AuthenticationPersistence
 *  org.weasis.dicom.explorer.pref.node.DefaultDicomNode
 *  org.weasis.dicom.explorer.pref.node.DicomWebNode
 *  org.weasis.dicom.explorer.pref.node.DicomWebNode$WebType
 *  org.weasis.dicom.explorer.rs.RsQueryResult
 *  org.weasis.dicom.explorer.wado.DownloadManager
 *  org.weasis.dicom.op.CFind
 *  org.weasis.dicom.param.AdvancedParams
 *  org.weasis.dicom.param.ConnectOptions
 *  org.weasis.dicom.param.DicomNode
 *  org.weasis.dicom.param.DicomParam
 *  org.weasis.dicom.param.DicomProgress
 *  org.weasis.dicom.param.DicomState
 *  org.weasis.dicom.tool.DicomListener
 */
package org.weasis.dicom.qr;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.img.util.DicomObjectUtil;
import org.dcm4che3.net.service.QueryRetrieveLevel;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.DataExplorerView;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.gui.util.CheckBoxModel;
import org.weasis.core.api.gui.util.DropDownButton;
import org.weasis.core.api.gui.util.GroupCheckBoxMenu;
import org.weasis.core.api.gui.util.GroupPopup;
import org.weasis.core.api.gui.util.GroupRadioMenu;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.WinUtil;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.media.data.Taggable;
import org.weasis.core.api.net.URLParameters;
import org.weasis.core.api.net.auth.AuthMethod;
import org.weasis.core.api.net.auth.OAuth2ServiceFactory;
import org.weasis.core.api.service.BundlePreferences;
import org.weasis.core.api.service.WProperties;
import org.weasis.core.api.util.ThreadUtil;
import org.weasis.core.ui.pref.PreferenceDialog;
import org.weasis.core.ui.tp.raven.datetime.DatePicker;
import org.weasis.core.ui.tp.raven.datetime.event.DateSelectionListener;
import org.weasis.core.ui.tp.raven.spinner.SpinnerProgress;
import org.weasis.core.util.FileUtil;
import org.weasis.core.util.StreamUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.display.CharsetEncoding;
import org.weasis.dicom.codec.display.Modality;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.LoadLocalDicom;
import org.weasis.dicom.explorer.PluginOpeningStrategy;
import org.weasis.dicom.explorer.imp.ImportDicom;
import org.weasis.dicom.explorer.main.DicomExplorer;
import org.weasis.dicom.explorer.pref.node.AbstractDicomNode;
import org.weasis.dicom.explorer.pref.node.AuthenticationPersistence;
import org.weasis.dicom.explorer.pref.node.DefaultDicomNode;
import org.weasis.dicom.explorer.pref.node.DicomWebNode;
import org.weasis.dicom.explorer.rs.RsQueryResult;
import org.weasis.dicom.explorer.wado.DownloadManager;
import org.weasis.dicom.op.CFind;
import org.weasis.dicom.param.AdvancedParams;
import org.weasis.dicom.param.ConnectOptions;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.DicomParam;
import org.weasis.dicom.param.DicomProgress;
import org.weasis.dicom.param.DicomState;
import org.weasis.dicom.qr.LoadQrSeries;
import org.weasis.dicom.qr.Messages;
import org.weasis.dicom.qr.RetrieveSelection;
import org.weasis.dicom.qr.RetrieveTask;
import org.weasis.dicom.qr.RetrieveTree;
import org.weasis.dicom.qr.RetrieveTreeModel;
import org.weasis.dicom.qr.RsQuery;
import org.weasis.dicom.qr.SearchParameters;
import org.weasis.dicom.tool.DicomListener;

public class DicomQrView
extends AbstractItemDialogPage
implements ImportDicom {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomQrView.class);
    private static final int STUDY_LEVEL = 3;
    private static final int SERIES_LEVEL = 4;
    private DicomWebNode retrieveNode;
    private static final String LAST_SEL_NODE = "lastSelNode";
    private static final String LAST_CALLING_NODE = "lastCallingNode";
    private static final String LAST_DICOM_CHARSET = "lastRDicomCharset";
    private static final String LAST_RETRIEVE_TYPE = "lastRetrieveType";
    private static final String LAST_RETRIEVE_LIMIT = "lastRetrieveLimit";
    private static final String LAST_SEL_TEMPLATE = "lastSelTemplate";
    private final JComboBox<AbstractDicomNode> comboDestinationNode = new JComboBox();
    private final JComboBox<SearchParameters> templateComboBox = new JComboBox();
    private final JTextField tfSearch = new JTextField(25);
    private final RetrieveTree tree = new RetrieveTree();
    private final JComboBox<TagW> comboTags = new JComboBox<TagW>(TagD.getTagFromIDs((int[])new int[]{0x100010, 0x100020, 524368, 0x200010, 528432, 0x20000D, 524432, 528464, 528480}));
    private final GroupCheckBoxMenu groupMod = new GroupCheckBoxMenu();
    private final DropDownButton modButton = new DropDownButton(this, "search_mod", Messages.getString("DicomQrView.modalities"), GuiUtils.getDownArrowIcon(), (GroupPopup)this.groupMod){
        final /* synthetic */ DicomQrView this$0;
        {
            DicomQrView dicomQrView = this$0;
            Objects.requireNonNull(dicomQrView);
            this.this$0 = dicomQrView;
            super(arg0, arg1, arg2, arg3);
        }

        protected JPopupMenu getPopupMenu() {
            JPopupMenu menu = this.getMenuModel() == null ? new JPopupMenu() : this.getMenuModel().createJPopupMenu();
            menu.setInvoker((Component)((Object)this));
            return menu;
        }
    };
    private Period currentPeriod;
    private final GroupRadioMenu<Period> groupDate = new GroupRadioMenu<Period>(this){
        final /* synthetic */ DicomQrView this$0;
        {
            DicomQrView dicomQrView = this$0;
            Objects.requireNonNull(dicomQrView);
            this.this$0 = dicomQrView;
        }

        public void contentsChanged(ListDataEvent e) {
            super.contentsChanged(e);
            this.this$0.currentPeriod = (Period)((Object)this.getSelectedItem());
            if (this.this$0.currentPeriod != null) {
                this.this$0.startDatePicker.removeDateSelectionListener(this.this$0.dateChangeListener);
                this.this$0.endDatePicker.removeDateSelectionListener(this.this$0.dateChangeListener);
                this.this$0.startDatePicker.setSelectedDate(this.this$0.currentPeriod.getStart());
                this.this$0.endDatePicker.setSelectedDate(this.this$0.currentPeriod.getEnd());
                this.this$0.startDatePicker.addDateSelectionListener(this.this$0.dateChangeListener);
                this.this$0.endDatePicker.addDateSelectionListener(this.this$0.dateChangeListener);
            }
        }
    };
    private final DropDownButton dateButton = new DropDownButton(this, "search_date", Messages.getString("DicomQrView.dates"), GuiUtils.getDownArrowIcon(), (GroupPopup)this.groupDate){
        final /* synthetic */ DicomQrView this$0;
        {
            DicomQrView dicomQrView = this$0;
            Objects.requireNonNull(dicomQrView);
            this.this$0 = dicomQrView;
            super(arg0, arg1, arg2, arg3);
        }

        protected JPopupMenu getPopupMenu() {
            JPopupMenu menu = this.getMenuModel() == null ? new JPopupMenu() : this.getMenuModel().createJPopupMenu();
            menu.setInvoker((Component)((Object)this));
            return menu;
        }
    };
    private final DateSelectionListener dateChangeListener = a -> this.groupDate.setSelected(null);
    private final ActionListener destNodeListener = evt -> this.applySelectedArchive();
    private final ChangeListener queryListener = e -> this.dicomQuery();
    private final DatePicker startDatePicker = this.buildDatePicker();
    private final DatePicker endDatePicker = this.buildDatePicker();
    private final JComboBox<AbstractDicomNode.RetrieveType> comboDicomRetrieveType = new JComboBox<AbstractDicomNode.RetrieveType>(AbstractDicomNode.RetrieveType.values());
    private final JComboBox<AbstractDicomNode> comboCallingNode = new JComboBox();
    private final JComboBox<CharsetEncoding> comboCharset = new JComboBox<CharsetEncoding>(CharsetEncoding.values());
    private final DicomListener dicomListener;
    private final ExecutorService executor = ThreadUtil.newFixedThreadPool((int)3, (String)"DicomQueryRetrieve");
    private final JSpinner limitSpinner = new JSpinner();
    private final JSpinner pageSpinner = new JSpinner();
    private QueryProcess process;
    private AuthMethod authMethod;
    private final SpinnerProgress progressBar = new SpinnerProgress();
    final JLabel lblCalling = new JLabel(Messages.getString("DicomQrView.calling_node") + ":");
    private final JLabel lblCharset = new JLabel(Messages.getString("charset") + ":");
    private final JLabel lblRetrieve = new JLabel(Messages.getString("DicomQrView.retrieve") + ":");

    public DicomQrView() {
        super(Messages.getString("DicomQrView.title"));
        int limit = StringUtil.getInt((String)DicomQrView.getPersistence().getProperty(LAST_RETRIEVE_LIMIT), (int)10);
        GuiUtils.setNumberModel((JSpinner)this.limitSpinner, (int)limit, (int)0, (int)999, (int)5);
        GuiUtils.setNumberModel((JSpinner)this.pageSpinner, (int)1, (int)1, (int)99999, (int)1);
        SearchParameters.loadSearchParameters(this.templateComboBox);
        this.initGUI();
        this.initialize(true);
        DicomListener dcmListener = null;
        try {
            DataExplorerModel dataExplorerModel;
            PluginOpeningStrategy openingStrategy = new PluginOpeningStrategy(DownloadManager.getOpeningViewer());
            openingStrategy.setFullImportSession(false);
            DataExplorerView dicomView = GuiUtils.getUICore().getExplorerPlugin(DicomExplorer.NAME);
            if (dicomView != null && (dataExplorerModel = dicomView.getDataExplorerModel()) instanceof DicomModel) {
                DicomModel model = (DicomModel)dataExplorerModel;
                DicomProgress progress = new DicomProgress();
                progress.addProgressListener(p -> {
                    Path current = p.getProcessedFile();
                    if (current != null && p.getAttributes() == null) {
                        LoadQrSeries.applyIdentityOverrides(current, model, null);
                        LoadLocalDicom task = new LoadLocalDicom(new File[]{current.toFile()}, false, (DataExplorerModel)model, openingStrategy);
                        DicomModel.LOADING_EXECUTOR.execute((Runnable)task);
                    }
                });
                dcmListener = new DicomListener(DicomQrView.getSessionTempFolder(), progress);
            } else {
                dcmListener = new DicomListener(DicomQrView.getSessionTempFolder());
            }
        }
        catch (RuntimeException e2) {
            LOGGER.error("Cannot start DICOM listener", (Throwable)e2);
        }
        this.dicomListener = dcmListener;
    }

    public static WProperties getPersistence() {
        return GuiUtils.getUICore().getPluginPersistence(DicomQrView.class.getPackageName());
    }

    public static Path getSessionTempFolder() {
        return FileUtil.createTempDir((Path)AppProperties.buildAccessibleTempDirectory((String[])new String[]{"tmp", "qr"}));
    }

    public void initGUI() {
        this.tree.setSeriesLoader(this::loadSeriesInTree);
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(GuiUtils.getEmptyBorder((int)5));
        tabbedPane.putClientProperty("JTabbedPane.hasFullBorder", true);
        JPanel sourcePanel = GuiUtils.getVerticalBoxLayoutPanel((JComponent[])new JComponent[]{this.getArchivePanel(), this.getCallingNodePanel(), this.getCharsetPanel(), GuiUtils.boxXLastElement((int)2)});
        sourcePanel.setBorder(GuiUtils.getEmptyBorder((int)5));
        tabbedPane.addTab(Messages.getString("dicom.source"), sourcePanel);
        tabbedPane.addTab(Messages.getString("search.criteria"), this.getSearchPanel());
        this.add(tabbedPane);
        this.add(GuiUtils.boxVerticalStrut((int)10));
        this.add(this.getCtrlSearchPanel());
        this.tree.setBorder(UIManager.getBorder("ScrollPane.border"));
        this.add(this.tree);
    }

    public JPanel getArchivePanel() {
        JLabel lblDest = new JLabel(Messages.getString("DicomQrView.arc") + ":");
        AbstractDicomNode.addTooltipToComboList(this.comboDestinationNode);
        GuiUtils.setPreferredWidth(this.comboDestinationNode, (int)250, (int)150);
        this.comboDicomRetrieveType.setToolTipText(Messages.getString("DicomQrView.msg_sel_type"));
        return GuiUtils.getFlowLayoutPanel((int)2, (int)5, (JComponent[])new JComponent[]{lblDest, this.comboDestinationNode, GuiUtils.boxHorizontalStrut((int)15), this.lblRetrieve, this.comboDicomRetrieveType});
    }

    public JPanel getCallingNodePanel() {
        GuiUtils.setPreferredWidth(this.comboCallingNode, (int)230, (int)150);
        AbstractDicomNode.addTooltipToComboList(this.comboCallingNode);
        JButton btnGeneralOptions = new JButton(Messages.getString("DicomQrView.more_opt"));
        btnGeneralOptions.addActionListener(actionEvent -> {
            this.applyChange();
            PreferenceDialog dialog = new PreferenceDialog(SwingUtilities.getWindowAncestor((Component)((Object)this)));
            dialog.showPage(org.weasis.dicom.explorer.Messages.getString((String)"DicomNodeListView.node_list"));
            GuiUtils.showCenterScreen((Window)dialog);
            this.initNodeList();
        });
        return GuiUtils.getFlowLayoutPanel((int)2, (int)5, (JComponent[])new JComponent[]{this.lblCalling, this.comboCallingNode, GuiUtils.boxHorizontalStrut((int)15), btnGeneralOptions});
    }

    public JPanel getCharsetPanel() {
        GuiUtils.setPreferredWidth(this.comboCharset, (int)350, (int)200);
        return GuiUtils.getFlowLayoutPanel((int)2, (int)5, (JComponent[])new JComponent[]{this.lblCharset, this.comboCharset});
    }

    public JPanel getCtrlSearchPanel() {
        JLabel labelLimit = new JLabel(Messages.getString("limit") + ":");
        this.limitSpinner.setToolTipText(Messages.getString("no.limit"));
        JLabel labelPage = new JLabel(Messages.getString("page") + ":");
        this.pageSpinner.addChangeListener(this.queryListener);
        this.progressBar.setEnabled(false);
        JButton searchBtn = new JButton(Messages.getString("DicomQrView.search"));
        searchBtn.setToolTipText(Messages.getString("DicomQrView.tips_dcm_query"));
        searchBtn.addActionListener(e -> this.dicomQuery());
        return GuiUtils.getFlowLayoutPanel((int)3, (int)2, (int)5, (JComponent[])new JComponent[]{labelLimit, this.limitSpinner, GuiUtils.boxHorizontalStrut((int)15), labelPage, this.pageSpinner, GuiUtils.boxHorizontalStrut((int)15), this.progressBar, GuiUtils.boxHorizontalStrut((int)15), searchBtn});
    }

    public JPanel getSearchPanel() {
        List list = Stream.of(Modality.values()).collect(Collectors.toList());
        list.set(0, Messages.getString("DicomQrView.all_mod"));
        this.groupMod.setModel(list, true, true);
        this.modButton.setToolTipText(Messages.getString("DicomQrView.select_mod"));
        Period[] listDate = new Period[]{Period.ALL, Period.TODAY, Period.YESTERDAY, Period.BEFORE_YESTERDAY, Period.CUR_WEEK, Period.CUR_MONTH, Period.CUR_YEAR, Period.LAST_DAY, Period.LAST_2_DAYS, Period.LAST_3_DAYS, Period.LAST_WEEK, Period.LAST_2_WEEKS, Period.LAST_MONTH, Period.LAST_3_MONTHS, Period.LAST_6_MONTHS, Period.LAST_YEAR};
        DefaultComboBoxModel<Period> dataModel = new DefaultComboBoxModel<Period>(listDate);
        this.groupDate.setModel(dataModel);
        JLabel labelFrom = new JLabel(Messages.getString("DicomQrView.from"));
        JLabel labelTo = new JLabel(Messages.getString("DicomQrView.to"));
        JPanel panel1 = GuiUtils.getFlowLayoutPanel((int)2, (int)5, (JComponent[])new JComponent[]{this.modButton, GuiUtils.boxXLastElement((int)15), this.dateButton, GuiUtils.boxXLastElement((int)10), labelFrom, this.startDatePicker.getEditor(), GuiUtils.boxXLastElement((int)5), labelTo, this.endDatePicker.getEditor()});
        this.comboTags.setMaximumRowCount(15);
        GuiUtils.setPreferredWidth(this.comboTags, (int)230, (int)150);
        String buf = "<html>\n  %s<br>\n  &nbsp&nbsp&nbsp%s<br>\n  &nbsp&nbsp&nbsp%s\n</html>\n".formatted(new Object[]{Messages.getString("DicomQrView.tips_wildcard"), Messages.getString("DicomQrView.tips_star"), Messages.getString("DicomQrView.tips_question")});
        this.tfSearch.setToolTipText(buf);
        this.tfSearch.putClientProperty("JTextField.showClearButton", true);
        JPanel panel2 = GuiUtils.getFlowLayoutPanel((int)2, (int)5, (JComponent[])new JComponent[]{this.comboTags, this.tfSearch});
        JButton clearBtn = new JButton(Messages.getString("DicomQrView.clear"));
        clearBtn.setToolTipText(Messages.getString("DicomQrView.clear_search"));
        clearBtn.addActionListener(e -> this.templateComboBox.setSelectedIndex(0));
        JButton addButton = new JButton(Messages.getString("save"));
        JButton deleteButton = new JButton(Messages.getString("delete"));
        deleteButton.setToolTipText(Messages.getString("delete.the.selected.item"));
        deleteButton.addActionListener(evt -> {
            int index;
            int response = JOptionPane.showConfirmDialog(deleteButton, String.format(org.weasis.dicom.explorer.Messages.getString((String)"AbstractDicomNode.delete_msg"), this.templateComboBox.getSelectedItem()), null, 0, 2);
            if (response == 0 && (index = this.templateComboBox.getSelectedIndex()) > 1) {
                this.templateComboBox.removeItemAt(index);
            }
        });
        addButton.addActionListener(evt -> {
            String title;
            String message = Messages.getString("enter.a.name");
            String description = (String)JOptionPane.showInputDialog(addButton, message, title = Messages.getString("search.template"), 1, null, null, null);
            if (StringUtil.hasText((CharSequence)description)) {
                SearchParameters item = this.buildCurrentSearchParameters(description);
                this.templateComboBox.addItem(item);
                this.templateComboBox.setSelectedItem(item);
            }
        });
        this.templateComboBox.addItemListener(e -> {
            if (e.getStateChange() == 1) {
                this.applyParametersFromSelected();
            }
        });
        JPanel panel3 = GuiUtils.getFlowLayoutPanel((int)2, (int)5, (JComponent[])new JComponent[]{clearBtn, addButton, deleteButton, this.templateComboBox});
        JPanel searchPanel = GuiUtils.getVerticalBoxLayoutPanel((JComponent[])new JComponent[]{panel1, panel2, panel3});
        searchPanel.setBorder(GuiUtils.getEmptyBorder((int)5));
        return searchPanel;
    }

    private void applyParametersFromSelected() {
        Object selectedItem = this.templateComboBox.getSelectedItem();
        this.clearItems();
        if (selectedItem instanceof SearchParameters) {
            SearchParameters parameters = (SearchParameters)selectedItem;
            for (DicomParam dicomParam : parameters.getParameters()) {
                if (dicomParam.getTag() == 524385) {
                    List values;
                    List<Object> list = values = dicomParam.getValues() == null ? Collections.emptyList() : List.of(dicomParam.getValues());
                    if (values.isEmpty()) {
                        this.groupMod.getModelList().forEach(b -> b.setSelected(true));
                        continue;
                    }
                    for (CheckBoxModel box : this.groupMod.getModelList()) {
                        Object object = box.getObject();
                        if (object instanceof Modality) {
                            Modality modality = (Modality)object;
                            box.setSelected(values.contains(modality.name()));
                            continue;
                        }
                        box.setSelected(false);
                    }
                    continue;
                }
                if (dicomParam.getTag() == 524320) {
                    String[] range = dicomParam.getValues()[0].split("-");
                    LocalDate start = null;
                    LocalDate end = null;
                    if (range.length == 1) {
                        if (dicomParam.getValues()[0].startsWith("-")) {
                            end = DicomObjectUtil.getDicomDate((String)range[0]);
                        } else {
                            start = DicomObjectUtil.getDicomDate((String)range[0]);
                        }
                    } else if (range.length == 2) {
                        start = DicomObjectUtil.getDicomDate((String)range[0]);
                        end = DicomObjectUtil.getDicomDate((String)range[1]);
                    }
                    this.startDatePicker.setSelectedDate(start);
                    this.endDatePicker.setSelectedDate(end);
                    continue;
                }
                this.comboTags.setSelectedItem(TagD.get((int)dicomParam.getTag()));
                this.tfSearch.setText(dicomParam.getValues()[0]);
            }
            Period p = parameters.getPeriod();
            if (p != null) {
                this.groupDate.getModel().setSelectedItem((Object)p);
            }
        }
    }

    private DatePicker buildDatePicker() {
        DatePicker picker = new DatePicker();
        picker.setStartWeekOnMonday(true);
        JFormattedTextField editor = new JFormattedTextField();
        picker.setEditor(editor);
        JFormattedTextField textField = picker.getEditor();
        GuiUtils.setPreferredWidth((Component)textField, (int)145);
        picker.addDateSelectionListener(this.dateChangeListener);
        return picker;
    }

    private void clearItems() {
        this.tfSearch.setText(null);
        this.groupMod.selectAll();
        this.startDatePicker.setSelectedDate(null);
        this.endDatePicker.setSelectedDate(null);
        this.currentPeriod = null;
        this.pageSpinner.removeChangeListener(this.queryListener);
        this.pageSpinner.setValue(1);
        this.pageSpinner.addChangeListener(this.queryListener);
    }

    private boolean isEmptyQuery(List<DicomParam> p) {
        if (p == null || p.isEmpty()) {
            return true;
        }
        for (DicomParam param : p) {
            String[] values = param.getValues();
            if (values == null) continue;
            for (String value : values) {
                if (!StringUtil.hasText((CharSequence)value)) continue;
                return false;
            }
        }
        return true;
    }

    private void dicomQuery() {
        this.stopCurrentProcess();
        SearchParameters searchParams = this.buildCurrentSearchParameters("custom");
        List<DicomParam> p = searchParams.getParameters();
        if ((Integer)this.limitSpinner.getValue() < 1 && this.isEmptyQuery(p)) {
            String message = Messages.getString("DicomQrView.msg_empty_query");
            int response = JOptionPane.showOptionDialog(WinUtil.getValidComponent((Component)WinUtil.getParentDialog((Component)((Object)this))), message, this.getTitle(), 0, 2, null, null, null);
            if (response != 0) {
                return;
            }
        }
        AtomicBoolean running = new AtomicBoolean(true);
        AbstractDicomNode selectedItem = (AbstractDicomNode)this.comboDestinationNode.getSelectedItem();
        if (selectedItem instanceof DefaultDicomNode) {
            DefaultDicomNode node = (DefaultDicomNode)selectedItem;
            CharsetEncoding encoding = (CharsetEncoding)this.comboCharset.getSelectedItem();
            if (encoding == null) {
                encoding = CharsetEncoding.ISO_IR_192;
            }
            p.add(new DicomParam(524293, new String[]{encoding.getLabel()}));
            DefaultDicomNode callingNode = (DefaultDicomNode)this.comboCallingNode.getSelectedItem();
            DicomQrView.addReturnTags(p, CFind.PatientName);
            DicomQrView.addReturnTags(p, CFind.PatientID);
            DicomQrView.addReturnTags(p, CFind.PatientSex);
            DicomQrView.addReturnTags(p, CFind.PatientBirthDate);
            DicomQrView.addReturnTags(p, CFind.IssuerOfPatientID);
            DicomQrView.addReturnTags(p, CFind.StudyInstanceUID);
            DicomQrView.addReturnTags(p, CFind.StudyDescription);
            DicomQrView.addReturnTags(p, CFind.StudyDate);
            DicomQrView.addReturnTags(p, CFind.StudyTime);
            DicomQrView.addReturnTags(p, CFind.AccessionNumber);
            DicomQrView.addReturnTags(p, CFind.ReferringPhysicianName);
            DicomQrView.addReturnTags(p, CFind.StudyID);
            DicomQrView.addReturnTags(p, new DicomParam(524385, new String[0]));
            DicomQrView.addReturnTags(p, new DicomParam(2101766, new String[0]));
            DicomQrView.addReturnTags(p, new DicomParam(2101768, new String[0]));
            AdvancedParams params = new AdvancedParams();
            ConnectOptions connectOptions = new ConnectOptions();
            connectOptions.setConnectTimeout(3000);
            connectOptions.setAcceptTimeout(5000);
            params.setConnectOptions(connectOptions);
            Runnable runnable = () -> {
                GuiExecutor.execute(() -> {
                    this.progressBar.setEnabled(true);
                    this.progressBar.setIndeterminate(true);
                });
                DicomState state = CFind.process((AdvancedParams)params, (DicomNode)callingNode.getDicomNodeWithOnlyAET(), (DicomNode)node.getDicomNode(), (int)((Integer)this.limitSpinner.getValue()), (QueryRetrieveLevel)QueryRetrieveLevel.STUDY, (DicomParam[])p.toArray(new DicomParam[0]));
                if (running.get()) {
                    GuiExecutor.execute(() -> {
                        this.progressBar.setEnabled(false);
                        this.progressBar.setIndeterminate(false);
                        this.displayResult(state);
                        if (state.getStatus() != 0) {
                            int limit = (Integer)this.limitSpinner.getValue();
                            String message = state.getStatus() == 65024 && limit > 0 ? Messages.getString("query.canceled.limit").formatted(new Object[]{limit}) : state.getMessage();
                            LOGGER.error("Dicom C-FIND error: {}", (Object)message);
                            JOptionPane.showMessageDialog(WinUtil.getValidComponent((Component)((Object)this)), message, null, 0);
                        }
                    });
                }
            };
            this.process = new QueryProcess(runnable, "DICOM C-FIND", running);
            this.process.start();
        } else if (selectedItem instanceof DicomWebNode) {
            DicomWebNode node = (DicomWebNode)selectedItem;
            AuthMethod auth = AuthenticationPersistence.getAuthMethod((String)node.getAuthMethodUid());
            if (!OAuth2ServiceFactory.NO_AUTH.equals(auth)) {
                String oldCode = auth.getCode();
                this.authMethod = auth;
                if (this.authMethod.getToken() == null) {
                    return;
                }
                if (!Objects.equals(oldCode, this.authMethod.getCode())) {
                    AuthenticationPersistence.saveMethod();
                }
            }
            Properties props = new Properties();
            props.setProperty("dicomweb.url", node.getUrl().toString());
            Integer limit = (Integer)this.limitSpinner.getValue();
            Integer page = (Integer)this.pageSpinner.getValue();
            if (limit > 0) {
                props.setProperty("page.size", String.format("&limit=%d&offset=%d", limit, (page - 1) * limit));
            }
            this.retrieveNode = node;
            RsQuery rsquery = new RsQuery(new DicomModel(), props, p, this.authMethod, node.getHeaders());
            Runnable runnable = () -> {
                try {
                    GuiExecutor.execute(() -> {
                        this.progressBar.setEnabled(true);
                        this.progressBar.setIndeterminate(true);
                    });
                    rsquery.call();
                    if (running.get()) {
                        GuiExecutor.execute(() -> {
                            this.progressBar.setEnabled(false);
                            this.progressBar.setIndeterminate(false);
                            this.tree.setRetrieveTreeModel(new RetrieveTreeModel(rsquery.getDicomModel()));
                            this.tree.revalidate();
                            this.tree.repaint();
                        });
                    }
                }
                catch (Exception e) {
                    LOGGER.error("", (Throwable)e);
                }
            };
            this.process = new QueryProcess(runnable, "QIDO-RS", running);
            this.process.start();
        }
    }

    private static void addReturnTags(List<DicomParam> list, DicomParam p) {
        if (list.stream().noneMatch(d -> d.getTag() == p.getTag())) {
            list.add(p);
        }
    }

    private void displayResult(DicomState state) {
        List items = state.getDicomRSP();
        DicomModel dicomModel = new DicomModel();
        if (items != null) {
            for (int i = 0; i < items.size(); ++i) {
                Attributes item = (Attributes)items.get(i);
                if (item == null) continue;
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("===========================================");
                    LOGGER.trace("CFind Item {}", (Object)(i + 1));
                    LOGGER.trace("===========================================");
                    LOGGER.trace("{}", (Object)item.toString(100, 150));
                }
                RsQuery.populateDicomModel(dicomModel, item);
            }
        }
        this.tree.setRetrieveTreeModel(new RetrieveTreeModel(dicomModel));
        this.tree.revalidate();
        this.tree.repaint();
    }

    protected void initialize(boolean firstTime) {
        int lastTemplate;
        if (firstTime) {
            this.initNodeList();
        }
        if ((lastTemplate = StringUtil.getInt((String)DicomQrView.getPersistence().getProperty(LAST_SEL_TEMPLATE), (int)0)) >= this.templateComboBox.getItemCount()) {
            lastTemplate = 0;
        }
        this.templateComboBox.setSelectedIndex(lastTemplate);
    }

    private void initNodeList() {
        String lastCharset;
        this.comboCallingNode.removeAllItems();
        AbstractDicomNode.loadDicomNodes(this.comboCallingNode, (AbstractDicomNode.Type)AbstractDicomNode.Type.DICOM_CALLING, (AbstractDicomNode.UsageType)AbstractDicomNode.UsageType.RETRIEVE);
        AbstractDicomNode.restoreNodeSelection((WProperties)DicomQrView.getPersistence(), this.comboCallingNode.getModel(), (String)LAST_CALLING_NODE);
        this.comboDestinationNode.removeActionListener(this.destNodeListener);
        this.comboDestinationNode.removeAllItems();
        AbstractDicomNode.loadDicomNodes(this.comboDestinationNode, (AbstractDicomNode.Type)AbstractDicomNode.Type.DICOM, (AbstractDicomNode.UsageType)AbstractDicomNode.UsageType.RETRIEVE);
        AbstractDicomNode.loadDicomNodes(this.comboDestinationNode, (AbstractDicomNode.Type)AbstractDicomNode.Type.WEB, (AbstractDicomNode.UsageType)AbstractDicomNode.UsageType.RETRIEVE, (DicomWebNode.WebType)DicomWebNode.WebType.QIDORS);
        AbstractDicomNode.restoreNodeSelection((WProperties)DicomQrView.getPersistence(), this.comboDestinationNode.getModel(), (String)LAST_SEL_NODE);
        String lastType = DicomQrView.getPersistence().getProperty(LAST_RETRIEVE_TYPE);
        if (lastType != null) {
            try {
                this.comboDicomRetrieveType.setSelectedItem(AbstractDicomNode.RetrieveType.valueOf((String)lastType));
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if ((lastCharset = DicomQrView.getPersistence().getProperty(LAST_DICOM_CHARSET)) != null) {
            try {
                this.comboCharset.setSelectedItem(CharsetEncoding.fromCode((String)lastCharset));
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        this.applySelectedArchive();
        this.comboDestinationNode.addActionListener(this.destNodeListener);
    }

    private void applySelectedArchive() {
        Object selectedItem = this.comboDestinationNode.getSelectedItem();
        boolean dcmOption = selectedItem instanceof DefaultDicomNode;
        this.lblCalling.setEnabled(dcmOption);
        this.lblRetrieve.setEnabled(dcmOption);
        this.comboDicomRetrieveType.setEnabled(dcmOption);
        this.comboCallingNode.setEnabled(dcmOption);
        this.lblCharset.setEnabled(dcmOption);
        this.comboCharset.setEnabled(dcmOption);
        this.pageSpinner.setEnabled(!dcmOption);
        this.pageSpinner.removeChangeListener(this.queryListener);
        this.pageSpinner.setValue(1);
        this.pageSpinner.addChangeListener(this.queryListener);
        this.stopCurrentProcess();
    }

    public void resetSettingsToDefault() {
        this.initialize(false);
    }

    private SearchParameters buildCurrentSearchParameters(String title) {
        SearchParameters p = new SearchParameters(title);
        String sTagValue = this.tfSearch.getText();
        TagW item = (TagW)this.comboTags.getSelectedItem();
        if (item != null) {
            p.getParameters().add(new DicomParam(item.getId(), new String[]{sTagValue}));
        }
        String[] list = null;
        if (this.groupMod.getModelList().stream().anyMatch(c -> !c.isSelected())) {
            list = (String[])this.groupMod.getModelList().stream().filter(c -> c.isSelected() && c.getObject() instanceof Modality).map(c -> ((Modality)c.getObject()).name()).toArray(String[]::new);
        }
        p.getParameters().add(new DicomParam(524385, list));
        LocalDate sDate = this.startDatePicker.getSelectedDate();
        LocalDate eDate = this.endDatePicker.getSelectedDate();
        Object range = "";
        if (sDate != null || eDate != null) {
            range = TagD.formatDicomDate((LocalDate)sDate) + "-" + TagD.formatDicomDate((LocalDate)eDate);
        }
        p.getParameters().add(new DicomParam(524320, new String[]{range}));
        p.setPeriod(this.currentPeriod);
        return p;
    }

    public void applyChange() {
        CharsetEncoding charset;
        AbstractDicomNode.nodeSelectionPersistence((WProperties)DicomQrView.getPersistence(), (AbstractDicomNode)((AbstractDicomNode)this.comboDestinationNode.getSelectedItem()), (String)LAST_SEL_NODE);
        AbstractDicomNode.nodeSelectionPersistence((WProperties)DicomQrView.getPersistence(), (AbstractDicomNode)((AbstractDicomNode)this.comboCallingNode.getSelectedItem()), (String)LAST_CALLING_NODE);
        AbstractDicomNode.RetrieveType type = (AbstractDicomNode.RetrieveType)this.comboDicomRetrieveType.getSelectedItem();
        WProperties persistence = DicomQrView.getPersistence();
        if (type != null) {
            persistence.setProperty(LAST_RETRIEVE_TYPE, type.name());
        }
        if ((charset = (CharsetEncoding)this.comboCharset.getSelectedItem()) != null) {
            persistence.setProperty(LAST_DICOM_CHARSET, charset.getCode());
        }
        persistence.setProperty(LAST_RETRIEVE_LIMIT, String.valueOf(this.limitSpinner.getValue()));
        persistence.setProperty(LAST_SEL_TEMPLATE, String.valueOf(this.templateComboBox.getSelectedIndex()));
        this.saveTemplates(this.templateComboBox);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void saveTemplates(JComboBox<? extends SearchParameters> comboBox) {
        XMLStreamWriter writer = null;
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        BundleContext context = AppProperties.getBundleContext(((Object)((Object)this)).getClass());
        try {
            writer = factory.createXMLStreamWriter(new FileOutputStream(BundlePreferences.getFileInDataFolder((BundleContext)context, (String)"searchParameters.xml").toFile()), "UTF-8");
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeStartElement("searchParametersList");
            for (int i = 2; i < comboBox.getItemCount(); ++i) {
                SearchParameters node = comboBox.getItemAt(i);
                writer.writeStartElement("searchParameters");
                node.saveSearchParameters(writer);
                writer.writeEndElement();
            }
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.flush();
            StreamUtil.safeClose((XMLStreamWriter)writer);
        }
        catch (Exception e) {
            LOGGER.error("Error on writing DICOM node file", (Throwable)e);
        }
        finally {
            StreamUtil.safeClose(writer);
        }
    }

    protected void updateChanges() {
    }

    protected void stopCurrentProcess() {
        QueryProcess t = this.process;
        if (t != null) {
            this.process = null;
            t.running.set(false);
            t.interrupt();
        }
        GuiExecutor.execute(() -> {
            this.progressBar.setEnabled(false);
            this.progressBar.setIndeterminate(false);
            this.tree.setRetrieveTreeModel(new RetrieveTreeModel());
            this.tree.revalidate();
            this.tree.repaint();
        });
    }

    public void closeAdditionalWindow() {
        this.applyChange();
        this.executor.shutdown();
    }

    public void resetToDefaultValues() {
    }

    private RetrieveSelection getSelection(TreePath[] paths) {
        RetrieveSelection selection = new RetrieveSelection();
        for (TreePath treePath : paths) {
            DefaultMutableTreeNode node;
            Object object = treePath.getLastPathComponent();
            if (!(object instanceof DefaultMutableTreeNode) || !((object = (node = (DefaultMutableTreeNode)object).getUserObject()) instanceof MediaSeriesGroup)) continue;
            MediaSeriesGroup group = (MediaSeriesGroup)object;
            if (treePath.getPathCount() == 3) {
                String uid = (String)TagD.getTagValue((TagReadable)group, (int)0x20000D, String.class);
                if (!StringUtil.hasText((CharSequence)uid)) continue;
                selection.addStudy(uid);
                continue;
            }
            if (treePath.getPathCount() != 4) continue;
            DicomQrView.addSeriesSelection(selection, treePath, group);
        }
        return selection;
    }

    private static void addSeriesSelection(RetrieveSelection selection, TreePath seriesPath, MediaSeriesGroup series) {
        DefaultMutableTreeNode studyNode;
        Object object;
        String seriesUid = (String)TagD.getTagValue((TagReadable)series, (int)0x20000E, String.class);
        if (!(StringUtil.hasText((CharSequence)seriesUid) && (object = seriesPath.getParentPath().getLastPathComponent()) instanceof DefaultMutableTreeNode && (object = (studyNode = (DefaultMutableTreeNode)object).getUserObject()) instanceof MediaSeriesGroup)) {
            return;
        }
        MediaSeriesGroup study = (MediaSeriesGroup)object;
        String studyUid = (String)TagD.getTagValue((TagReadable)study, (int)0x20000D, String.class);
        if (StringUtil.hasText((CharSequence)studyUid)) {
            selection.addSeries(studyUid, seriesUid);
        }
    }

    private void loadSeriesInTree(RetrieveTreeModel treeModel, DefaultMutableTreeNode studyNode) {
        MediaSeriesGroup study = (MediaSeriesGroup)studyNode.getUserObject();
        String studyUid = (String)TagD.getTagValue((TagReadable)study, (int)0x20000D, String.class);
        Object selectedItem = this.comboDestinationNode.getSelectedItem();
        if (!StringUtil.hasText((CharSequence)studyUid)) {
            treeModel.removePlaceholder(studyNode);
            return;
        }
        treeModel.setPlaceholderLoading(studyNode, true);
        this.executor.execute(() -> {
            List<Attributes> datasets = this.querySeries(selectedItem, studyUid);
            GuiExecutor.execute(() -> {
                if (this.tree.getRetrieveTreeModel() != treeModel) {
                    return;
                }
                if (datasets == null) {
                    treeModel.setPlaceholderLoading(studyNode, false);
                } else if (datasets.isEmpty()) {
                    treeModel.removePlaceholder(studyNode);
                } else {
                    treeModel.setSeriesNodes(studyNode, DicomQrView.buildQuerySeries(treeModel.getDicomModel(), study, datasets));
                }
            });
        });
    }

    private List<Attributes> querySeries(Object selectedItem, String studyUid) {
        try {
            if (selectedItem instanceof DefaultDicomNode) {
                DefaultDicomNode node = (DefaultDicomNode)selectedItem;
                return this.findSeries(node, studyUid);
            }
            if (selectedItem instanceof DicomWebNode) {
                DicomWebNode node = (DicomWebNode)selectedItem;
                String url = RsQueryResult.seriesQueryUrl((String)node.getUrl().toString(), (String)studyUid, null);
                LOGGER.debug("QIDO-RS request: {}", (Object)url);
                return RsQueryResult.parseJSON((String)url, (AuthMethod)this.authMethod, (URLParameters)RsQueryResult.jsonQueryParameters((Map)node.getHeaders()));
            }
        }
        catch (Exception e) {
            LOGGER.error("Query the series of study {}", (Object)studyUid, (Object)e);
        }
        return null;
    }

    private List<Attributes> findSeries(DefaultDicomNode node, String studyUid) {
        DefaultDicomNode callingNode = (DefaultDicomNode)this.comboCallingNode.getSelectedItem();
        if (callingNode == null) {
            return null;
        }
        AdvancedParams params = new AdvancedParams();
        ConnectOptions connectOptions = new ConnectOptions();
        connectOptions.setConnectTimeout(3000);
        connectOptions.setAcceptTimeout(5000);
        params.setConnectOptions(connectOptions);
        DicomParam[] keys = new DicomParam[]{new DicomParam(0x20000D, new String[]{studyUid}), CFind.SeriesInstanceUID, CFind.Modality, CFind.SeriesNumber, CFind.SeriesDescription, new DicomParam(2101769, new String[0])};
        DicomState state = CFind.process((AdvancedParams)params, (DicomNode)callingNode.getDicomNodeWithOnlyAET(), (DicomNode)node.getDicomNode(), (int)0, (QueryRetrieveLevel)QueryRetrieveLevel.SERIES, (DicomParam[])keys);
        if (state.getStatus() != 0 && state.getDicomRSP() == null) {
            LOGGER.error("Dicom C-FIND series error: {}", (Object)state.getMessage());
        }
        return state.getDicomRSP();
    }

    private static List<Series<?>> buildQuerySeries(DicomModel queryModel, MediaSeriesGroup study, List<Attributes> datasets) {
        ArrayList list = new ArrayList(datasets.size());
        for (Attributes dataset : datasets) {
            String seriesUid = dataset.getString(0x20000E);
            if (!StringUtil.hasText((CharSequence)seriesUid)) continue;
            TagW[] tagWArray = queryModel.getHierarchyNode(study, (Object)seriesUid);
            if (tagWArray instanceof Series) {
                Series existing = (Series)tagWArray;
                list.add(existing);
                continue;
            }
            DicomSeries series = new DicomSeries(seriesUid);
            series.setTag(TagD.get((int)0x20000E), (Object)seriesUid);
            for (TagW tag : TagD.getTagFromIDs((int[])new int[]{524384, 0x200011, 528446, 2101769})) {
                tag.readValue((Object)dataset, (Taggable)series);
            }
            queryModel.addHierarchyNode(study, (MediaSeriesGroup)series);
            list.add((Series<?>)series);
        }
        return list;
    }

    public void importDICOM(DicomModel explorerDcmModel, JProgressBar info) {
        RetrieveSelection selection = this.getSelection(this.tree.getCheckboxTree().getCheckingPaths());
        if (!selection.isEmpty()) {
            DicomWebNode webNode;
            Object selectedItem = this.getComboDestinationNode().getSelectedItem();
            if (selectedItem instanceof DicomWebNode && (webNode = (DicomWebNode)selectedItem).getWebType() == DicomWebNode.WebType.QIDORS) {
                String m1;
                List webNodes = AbstractDicomNode.loadDicomNodes((AbstractDicomNode.Type)AbstractDicomNode.Type.WEB, (AbstractDicomNode.UsageType)AbstractDicomNode.UsageType.RETRIEVE, (DicomWebNode.WebType)DicomWebNode.WebType.WADORS);
                String host = RetrieveTask.getHostname(((DicomWebNode)selectedItem).getUrl().getHost());
                DicomWebNode wnode = RetrieveTask.getWadoUrl(this, host, webNodes, m1 = Messages.getString("DicomQrView.no.url.matches.with.the.qido"));
                if (wnode != null) {
                    this.retrieveNode = wnode;
                }
            }
            this.executor.execute((Runnable)((Object)new RetrieveTask(selection, explorerDcmModel, this)));
        }
    }

    public JComboBox<AbstractDicomNode> getComboDestinationNode() {
        return this.comboDestinationNode;
    }

    public JComboBox<AbstractDicomNode.RetrieveType> getComboDicomRetrieveType() {
        return this.comboDicomRetrieveType;
    }

    public JComboBox<AbstractDicomNode> getComboCallingNode() {
        return this.comboCallingNode;
    }

    public DicomListener getDicomListener() {
        return this.dicomListener;
    }

    public DicomModel getDicomModel() {
        return this.tree.getRetrieveTreeModel().getDicomModel();
    }

    public DicomWebNode getRetrieveNode() {
        return this.retrieveNode;
    }

    public AuthMethod getAuthMethod() {
        return this.authMethod;
    }

    public static enum Period {
        ALL(Messages.getString("DicomQrView.all_dates"), () -> null),
        TODAY(Messages.getString("DicomQrView.today"), Period::today),
        YESTERDAY(Messages.getString("DicomQrView.yesterday"), () -> Period.today().minusDays(1L)),
        BEFORE_YESTERDAY(Messages.getString("DicomQrView.day_before_yest"), () -> Period.today().minusDays(2L)),
        CUR_WEEK(Messages.getString("DicomQrView.this_week"), () -> Period.today().with(WeekFields.of(Locale.getDefault(Locale.Category.FORMAT)).dayOfWeek(), 1L), Period::today),
        CUR_MONTH(Messages.getString("DicomQrView.this_month"), () -> Period.today().with(TemporalAdjusters.firstDayOfMonth()), Period::today),
        CUR_YEAR(Messages.getString("DicomQrView.this_year"), () -> Period.today().with(TemporalAdjusters.firstDayOfYear()), Period::today),
        LAST_DAY(Messages.getString("DicomQrView.last_24h"), () -> Period.today().minusDays(1L), Period::today),
        LAST_2_DAYS(Messages.getString("DicomQrView.last_2_d"), () -> Period.today().minusDays(2L), Period::today),
        LAST_3_DAYS(Messages.getString("DicomQrView.last_3_d"), () -> Period.today().minusDays(3L), Period::today),
        LAST_WEEK(Messages.getString("DicomQrView.last_w"), () -> Period.today().minusWeeks(1L), Period::today),
        LAST_2_WEEKS(Messages.getString("DicomQrView.last_2_w"), () -> Period.today().minusWeeks(2L), Period::today),
        LAST_MONTH(Messages.getString("DicomQrView.last_m"), () -> Period.today().minusMonths(1L), Period::today),
        LAST_3_MONTHS(Messages.getString("DicomQrView.last_3_m"), () -> Period.today().minusMonths(3L), Period::today),
        LAST_6_MONTHS(Messages.getString("DicomQrView.last_6_m"), () -> Period.today().minusMonths(6L), Period::today),
        LAST_YEAR(Messages.getString("DicomQrView.last_year"), () -> Period.today().minusYears(1L), Period::today);

        private final String displayName;
        private final Supplier<LocalDate> startSupplier;
        private final Supplier<LocalDate> endSupplier;

        private Period(String name, Supplier<LocalDate> dateSupplier) {
            this.displayName = name;
            this.startSupplier = dateSupplier;
            this.endSupplier = dateSupplier;
        }

        private Period(String name, Supplier<LocalDate> startSupplier, Supplier<LocalDate> endSupplier) {
            this.displayName = name;
            this.startSupplier = startSupplier;
            this.endSupplier = endSupplier;
        }

        public LocalDate getStart() {
            return this.startSupplier.get();
        }

        public LocalDate getEnd() {
            return this.endSupplier.get();
        }

        private static LocalDate today() {
            return LocalDate.now(ZoneId.systemDefault());
        }

        public String toString() {
            return this.displayName;
        }

        public static Period getPeriod(String name) {
            if (StringUtil.hasText((CharSequence)name)) {
                try {
                    return Period.valueOf(name);
                }
                catch (Exception e) {
                    LOGGER.error("Cannot get Period from {}", (Object)name, (Object)e);
                }
            }
            return null;
        }
    }

    static class QueryProcess
    extends Thread {
        AtomicBoolean running;

        public QueryProcess(Runnable runnable, String name, AtomicBoolean running) {
            super(runnable, name);
            this.running = running;
        }
    }
}

