/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.ui.FlatUIUtils
 *  org.dcm4che3.data.Attributes
 *  org.dcm4che3.data.ElementDictionary
 *  org.dcm4che3.data.Sequence
 *  org.dcm4che3.data.VR$Holder
 *  org.dcm4che3.img.DicomMetaData
 *  org.dcm4che3.util.TagUtils
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.media.data.MediaElement
 *  org.weasis.core.api.media.data.MediaReader
 *  org.weasis.core.api.media.data.MediaSeries
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.Series
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagUtil
 *  org.weasis.core.api.media.data.TagView
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.util.FontItem
 *  org.weasis.core.api.util.ResourceUtil
 *  org.weasis.core.api.util.ResourceUtil$ActionIcon
 *  org.weasis.core.api.util.ResourceUtil$ResourceIconPath
 *  org.weasis.core.ui.editor.SeriesViewer
 *  org.weasis.core.ui.editor.SeriesViewerEvent
 *  org.weasis.core.ui.editor.SeriesViewerEvent$EVENT
 *  org.weasis.core.ui.editor.SeriesViewerListener
 *  org.weasis.core.ui.editor.image.ImageViewerPlugin
 *  org.weasis.core.ui.editor.image.ViewCanvas
 *  org.weasis.core.ui.editor.image.ViewerPlugin
 *  org.weasis.core.ui.model.layer.LayerItem
 *  org.weasis.core.ui.util.TableColumnAdjuster
 *  org.weasis.core.util.StringUtil
 *  org.weasis.dicom.codec.DcmMediaReader
 *  org.weasis.dicom.codec.DicomMediaIO
 *  org.weasis.dicom.codec.DicomSpecialElement
 *  org.weasis.dicom.codec.TagD$Level
 */
package org.weasis.dicom.explorer.tag;

import com.formdev.flatlaf.ui.FlatUIUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.RowSorter;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.dcm4che3.img.DicomMetaData;
import org.dcm4che3.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaReader;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagUtil;
import org.weasis.core.api.media.data.TagView;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.util.FontItem;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.editor.SeriesViewer;
import org.weasis.core.ui.editor.SeriesViewerEvent;
import org.weasis.core.ui.editor.SeriesViewerListener;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.editor.image.ViewerPlugin;
import org.weasis.core.ui.model.layer.LayerItem;
import org.weasis.core.ui.util.TableColumnAdjuster;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.DcmMediaReader;
import org.weasis.dicom.codec.DicomMediaIO;
import org.weasis.dicom.codec.DicomSpecialElement;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.tag.TagSearchDocumentPanel;
import org.weasis.dicom.explorer.tag.TagSearchTablePanel;
import org.weasis.dicom.explorer.wado.DicomManager;

public class DicomFieldsView
extends JTabbedPane
implements SeriesViewerListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomFieldsView.class);
    public static final String[] columns = new String[]{Messages.getString("tag.id"), "VR", Messages.getString("tag.name"), Messages.getString("value")};
    private final JScrollPane allPane = new JScrollPane();
    private final JScrollPane limitedPane = new JScrollPane();
    private final JTextPane jTextPaneLimited = new JTextPane();
    private final SeriesViewer<?> viewer;
    private MediaElement currentMedia;
    private MediaSeries<?> currentSeries;
    private boolean anonymize = false;
    private MediaSeries<?> series;
    private JPanel tableContainer;
    private final DefaultTableModel tableModel = new DefaultTableModel(this, columns, 0){
        final /* synthetic */ DicomFieldsView this$0;
        {
            DicomFieldsView dicomFieldsView = this$0;
            Objects.requireNonNull(dicomFieldsView);
            this.this$0 = dicomFieldsView;
            super(arg0, arg1);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable jtable = new JTable(this, this.tableModel){
        final /* synthetic */ DicomFieldsView this$0;
        {
            DicomFieldsView dicomFieldsView = this$0;
            Objects.requireNonNull(dicomFieldsView);
            this.this$0 = dicomFieldsView;
            super(arg0);
        }

        @Override
        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            Component comp = super.prepareRenderer(renderer, row, column);
            Object value = this.getValueAt(row, column);
            if (value != null && comp instanceof JComponent) {
                JComponent jComponent = (JComponent)comp;
                jComponent.setToolTipText(this.getColumnName(column) + ": " + String.valueOf(value));
            }
            return comp;
        }
    };
    private final TagSearchTablePanel tagSearchTablePanel;
    private final TagSearchDocumentPanel tagSearchDocumentPanel;

    public DicomFieldsView(SeriesViewer<?> viewer) {
        this.viewer = viewer;
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.addTab(Messages.getString("DicomFieldsView.limited"), null, panel, null);
        this.tagSearchDocumentPanel = new TagSearchDocumentPanel(this.jTextPaneLimited);
        panel.add((Component)this.tagSearchDocumentPanel, "North");
        panel.add((Component)this.limitedPane, "Center");
        this.jTextPaneLimited.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.jTextPaneLimited.setContentType("text/html");
        this.jTextPaneLimited.setEditable(false);
        DicomFieldsView.applyFormatting(this.jTextPaneLimited.getStyledDocument());
        DefaultCaret caret = (DefaultCaret)this.jTextPaneLimited.getCaret();
        caret.setUpdatePolicy(1);
        JPanel dump = new JPanel();
        dump.setLayout(new BorderLayout());
        dump.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.addTab(Messages.getString("DicomFieldsView.all"), null, dump, null);
        this.tableContainer = new JPanel(new BorderLayout());
        this.tableContainer.setPreferredSize(GuiUtils.getDimension((int)50, (int)50));
        this.tableContainer.setBorder(GuiUtils.getEmptyBorder((int)3, (int)3, (int)0, (int)3));
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(this, (TableModel)this.tableModel){
            {
                Objects.requireNonNull(this$0);
                super(arg0);
            }

            @Override
            public boolean isSortable(int column) {
                return false;
            }
        };
        this.jtable.setRowSorter((RowSorter<? extends TableModel>)sorter);
        this.tagSearchTablePanel = new TagSearchTablePanel(this.jtable, this.tableContainer);
        dump.add((Component)this.tagSearchTablePanel, "North");
        this.jtable.getTableHeader().setReorderingAllowed(false);
        this.jtable.setShowVerticalLines(true);
        this.jtable.setFont(FontItem.SMALL.getFont());
        this.tableContainer.add((Component)this.jtable.getTableHeader(), "First");
        this.tableContainer.add((Component)this.jtable, "Center");
        this.allPane.setViewportView(this.tableContainer);
        dump.add((Component)this.allPane, "Center");
        this.setPreferredSize(GuiUtils.getDimension((int)400, (int)300));
        this.setMinimumSize(GuiUtils.getDimension((int)150, (int)50));
        this.addChangeListener(changeEvent -> this.changeDicomInfo(this.currentSeries, this.currentMedia));
        this.addComponentListener(new ComponentAdapter(this){
            final /* synthetic */ DicomFieldsView this$0;
            {
                DicomFieldsView dicomFieldsView = this$0;
                Objects.requireNonNull(dicomFieldsView);
                this.this$0 = dicomFieldsView;
            }

            @Override
            public void componentResized(ComponentEvent evt) {
                TableColumnAdjuster.adjustPreferredSizeForViewPort((JTable)this.this$0.jtable, (JPanel)this.this$0.tableContainer);
            }
        });
    }

    public void changingViewContentEvent(SeriesViewerEvent event) {
        SeriesViewerEvent.EVENT type = event.getEventType();
        if (event.getSeriesViewer() == this.viewer && (SeriesViewerEvent.EVENT.SELECT.equals((Object)type) || SeriesViewerEvent.EVENT.LAYOUT.equals((Object)type) || SeriesViewerEvent.EVENT.ANONYM.equals((Object)type))) {
            ViewCanvas sel;
            this.currentMedia = event.getMediaElement();
            this.currentSeries = event.getSeries();
            if (event.getSeriesViewer() instanceof ImageViewerPlugin && (sel = ((ImageViewerPlugin)event.getSeriesViewer()).getSelectedViewCanvas()) != null) {
                this.anonymize = sel.getInfoLayer().getDisplayPreferences(LayerItem.ANONYM_ANNOTATIONS);
            }
            this.changeDicomInfo(this.currentSeries, this.currentMedia);
        }
    }

    private void changeDicomInfo(MediaSeries<?> series, MediaElement media) {
        int index = this.getSelectedIndex();
        if (index == 0) {
            this.displayLimitedDicomInfo(series, media);
        } else {
            this.displayAllDicomInfo(series, media);
        }
        this.series = series;
    }

    private static void applyFormatting(StyledDocument doc) {
        Color color = FlatUIUtils.getUIColor((String)"TextArea.foreground", (Color)Color.DARK_GRAY);
        int fontSize = 11;
        Style defaultStyle = doc.getStyle("default");
        Style bold = doc.addStyle("normal", defaultStyle);
        StyleConstants.setFontSize(bold, fontSize);
        StyleConstants.setForeground(bold, color);
        bold = doc.addStyle("bold", bold);
        StyleConstants.setBold(bold, true);
        bold = doc.addStyle("h3", bold);
        StyleConstants.setFontSize(bold, fontSize + 4);
    }

    private void displayAllDicomInfo(MediaSeries<?> series, MediaElement media) {
        boolean init;
        this.tableContainer.removeAll();
        Point oldPosition = null;
        boolean bl = init = this.jtable.getRowCount() == 0;
        if (!init) {
            if (Objects.equals(series, this.series)) {
                oldPosition = this.allPane.getViewport().getViewPosition();
            }
            this.tableModel.setRowCount(0);
        }
        if (media != null) {
            MediaReader loader = media.getMediaReader();
            if (loader instanceof DicomMediaIO) {
                DicomMediaIO dicomMediaIO = (DicomMediaIO)loader;
                DicomMetaData metaData = dicomMediaIO.getDicomMetaData();
                if (metaData != null) {
                    DicomFieldsView.printAttribute(this.tableModel, metaData.getFileMetaInformation());
                    DicomFieldsView.printAttribute(this.tableModel, metaData.getDicomObject());
                }
            } else if (loader instanceof DcmMediaReader) {
                DcmMediaReader reader = (DcmMediaReader)loader;
                DicomFieldsView.printAttribute(this.tableModel, reader.getDicomObject());
            }
        }
        this.jtable.getColumnModel().setColumnMargin(GuiUtils.getScaleLength((int)7));
        this.tagSearchTablePanel.filter();
        if (oldPosition != null) {
            this.allPane.getViewport().setViewPosition(oldPosition);
        }
    }

    private static void printAttribute(DefaultTableModel model, Attributes dcmObj) {
        if (dcmObj != null) {
            int[] tags;
            for (int tag : tags = dcmObj.tags()) {
                try {
                    DicomFieldsView.printElement(model, dcmObj, tag);
                }
                catch (Exception e) {
                    LOGGER.error("Cannot print tag {}", (Object)TagUtils.toString((int)tag), (Object)e);
                    Object[] values = new Object[4];
                    values[0] = TagUtils.toString((int)tag);
                    values[3] = "CANNOT READ TAG";
                    model.addRow(values);
                }
            }
        }
    }

    private static void printElement(DefaultTableModel model, Attributes dcmObj, int tag) {
        String privateCreator = dcmObj.privateCreatorOf(tag);
        int level = dcmObj.getLevel();
        VR.Holder holder = new VR.Holder();
        Object val = dcmObj.getValue(tag, holder);
        Object[] values = new Object[4];
        values[0] = DicomFieldsView.getPrefixTag(level) + TagUtils.toString((int)tag);
        values[1] = holder.vr;
        String word = ElementDictionary.keywordOf((int)tag, (String)privateCreator);
        if (!StringUtil.hasText((CharSequence)word)) {
            word = "PrivateTag";
        }
        values[2] = word;
        Sequence seq = dcmObj.getSequence(tag);
        if (seq != null) {
            if (!seq.isEmpty()) {
                DicomFieldsView.printSequence(seq, model, values);
            } else {
                values[3] = "";
                model.addRow(values);
            }
        } else {
            values[3] = holder.vr.isInlineBinary() ? "binary data" : DicomFieldsView.printItem(dcmObj.getStrings(privateCreator, tag));
            model.addRow(values);
        }
    }

    private static String getPrefixTag(int level) {
        StringBuilder buf = new StringBuilder();
        if (level > 0) {
            buf.insert(0, "-->");
        }
        for (int i = 1; i < level; ++i) {
            buf.insert(0, "--");
        }
        return buf.toString();
    }

    private static String printItem(String[] values) {
        StringBuilder buf = new StringBuilder();
        if (values != null && values.length > 0) {
            buf.append(values[0]);
            for (int i = 1; i < values.length; ++i) {
                buf.append("\\");
                buf.append(values[i]);
            }
            if (buf.length() > 256) {
                buf.setLength(253);
                buf.append("...");
            }
        }
        return buf.toString();
    }

    private static void printSequence(Sequence seq, DefaultTableModel model, Object[] values) {
        String items = seq.size() <= 1 ? " item" : " items";
        values[3] = DicomFieldsView.printItem(new String[]{seq.size() + items});
        model.addRow(values);
        for (int i = 0; i < seq.size(); ++i) {
            int[] tags;
            Attributes attributes = (Attributes)seq.get(i);
            int level = attributes.getLevel();
            Object[] v = new Object[]{DicomFieldsView.getPrefixTag(level) + " ITEM #" + (i + 1), "", "", ""};
            model.addRow(v);
            for (int tag : tags = attributes.tags()) {
                DicomFieldsView.printElement(model, attributes, tag);
            }
        }
    }

    private void displayLimitedDicomInfo(MediaSeries<?> series, MediaElement media) {
        Object tagValue;
        StyledDocument doc = this.jTextPaneLimited.getStyledDocument();
        try {
            doc.remove(0, doc.getLength());
        }
        catch (BadLocationException e) {
            LOGGER.error("Clear document", (Throwable)e);
        }
        if (series != null && media != null && (tagValue = series.getTagValue(TagW.ExplorerModel)) instanceof DicomModel) {
            DicomModel model = (DicomModel)tagValue;
            MediaReader loader = media.getMediaReader();
            if (loader instanceof DcmMediaReader) {
                List<DicomData> list = DicomManager.getInstance().getLimitedDicomTags();
                for (DicomData dicomData : list) {
                    this.writeItems(dicomData, (TagReadable)this.getGroup(model, series, dicomData), doc);
                }
            }
        }
        this.tagSearchDocumentPanel.filter();
        this.limitedPane.setViewportView(this.jTextPaneLimited);
    }

    private MediaSeriesGroup getGroup(DicomModel model, MediaSeries<?> series, DicomData dicomData) {
        TagD.Level level = dicomData.level;
        if (TagD.Level.PATIENT.equals((Object)level)) {
            return model.getParent((MediaSeriesGroup)series, DicomModel.patient);
        }
        if (TagD.Level.STUDY.equals((Object)level)) {
            return model.getParent((MediaSeriesGroup)series, DicomModel.study);
        }
        if (TagD.Level.SERIES.equals((Object)level)) {
            return model.getParent((MediaSeriesGroup)series, DicomModel.series);
        }
        return null;
    }

    private void writeItems(DicomData dicomData, TagReadable group, StyledDocument doc) {
        int insertTitle = doc.getLength();
        boolean exist = false;
        block4: for (TagView t : dicomData.infos) {
            for (TagW tag : t.getTag()) {
                if (this.anonymize && tag.getAnonymizationType() == 1) continue;
                try {
                    Object val = TagUtil.getTagValue((TagW)tag, (TagReadable[])new TagReadable[]{group, this.currentMedia});
                    if (val == null) continue;
                    exist = true;
                    doc.insertString(doc.getLength(), tag.getDisplayedName(), doc.getStyle("normal"));
                    String format = tag.addGMTOffset(null, this.series);
                    doc.insertString(doc.getLength(), ": " + tag.getFormattedTagValue(val, format) + "\n", doc.getStyle("bold"));
                    continue block4;
                }
                catch (BadLocationException e) {
                    LOGGER.error("Writing text issue", (Throwable)e);
                }
            }
        }
        if (exist) {
            try {
                String formatTitle = insertTitle < 3 ? dicomData.title + "\n" : "\n" + dicomData.title + "\n";
                doc.insertString(insertTitle, formatTitle, doc.getStyle("h3"));
            }
            catch (BadLocationException e) {
                LOGGER.error("Writing text issue", (Throwable)e);
            }
        }
    }

    public static void displayHeader(ImageViewerPlugin<?> container) {
        ViewCanvas selView;
        if (container != null && (selView = container.getSelectedViewCanvas()) != null) {
            DicomFieldsView.showHeaderDialog(container, (MediaSeries<? extends MediaElement>)selView.getSeries(), (MediaElement)selView.getImage());
        }
    }

    public static void displayHeaderForSpecialElement(ViewerPlugin<?> container, Series<?> series) {
        if (container != null && series != null) {
            DicomSpecialElement dcm = DicomModel.getFirstSpecialElement(series, DicomSpecialElement.class);
            DicomFieldsView.showHeaderDialog(container, series, (MediaElement)dcm);
        }
    }

    public static void showHeaderDialog(SeriesViewer<?> container, MediaSeries<? extends MediaElement> series, MediaElement dcm) {
        if (container != null && series != null && dcm != null) {
            Component component;
            JFrame frame = new JFrame(Messages.getString("DicomExplorer.dcmInfo"));
            frame.setSize(GuiUtils.getDimension((int)650, (int)600));
            DicomFieldsView view = new DicomFieldsView(container);
            view.changingViewContentEvent(new SeriesViewerEvent(container, series, dcm, SeriesViewerEvent.EVENT.SELECT));
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.add(view);
            frame.getContentPane().add(panel);
            frame.setIconImage(ResourceUtil.getIcon((ResourceUtil.ResourceIconPath)ResourceUtil.ActionIcon.METADATA).derive(64, 64).getImage());
            if (container instanceof Component) {
                Component component2 = (Component)container;
                component = component2;
            } else {
                component = GuiUtils.getUICore().getMainArea().getComponent();
            }
            Component c = component;
            GuiUtils.showCenterScreen((Window)frame, (Component)c);
        }
    }

    public record DicomData(String title, TagView[] infos, TagD.Level level) {
        public DicomData(String title, TagView[] infos, TagD.Level level) {
            this.title = title;
            this.infos = Objects.requireNonNull(infos);
            this.level = level;
            for (TagView tagView : infos) {
                for (TagW tag : tagView.getTag()) {
                    DicomMediaIO.tagManager.addTag(tag, level);
                }
            }
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || ((Object)((Object)this)).getClass() != o.getClass()) {
                return false;
            }
            DicomData dicomData = (DicomData)((Object)o);
            return Objects.equals(this.title, dicomData.title) && Arrays.equals(this.infos, dicomData.infos) && this.level == dicomData.level;
        }

        public int hashCode() {
            int result = Objects.hash(this.title, this.level);
            result = 31 * result + Arrays.hashCode(this.infos);
            return result;
        }

        public String toString() {
            return this.title;
        }
    }

    static class SearchHighlightPainter
    extends DefaultHighlighter.DefaultHighlightPainter {
        public SearchHighlightPainter(Color color) {
            super(color);
        }
    }
}

