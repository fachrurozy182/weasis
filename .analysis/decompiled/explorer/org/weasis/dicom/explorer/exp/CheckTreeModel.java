/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  eu.essilab.lablib.checkboxtree.DefaultTreeCheckingModel
 *  eu.essilab.lablib.checkboxtree.TreeCheckingModel
 *  org.dcm4che3.util.UIDUtils
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.gui.util.GuiUtils$IconColor
 *  org.weasis.core.api.media.data.MediaElement
 *  org.weasis.core.api.media.data.MediaSeries
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.MediaSeriesGroupNode
 *  org.weasis.core.api.media.data.Series
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.media.data.TagW$TagType
 *  org.weasis.core.api.media.data.Thumbnailable
 *  org.weasis.core.ui.model.GraphicModel
 *  org.weasis.core.util.LangUtil
 *  org.weasis.dicom.codec.DcmMediaReader
 *  org.weasis.dicom.codec.DicomSeries
 *  org.weasis.dicom.codec.DicomSpecialElement
 *  org.weasis.dicom.codec.TagD
 */
package org.weasis.dicom.explorer.exp;

import eu.essilab.lablib.checkboxtree.DefaultTreeCheckingModel;
import eu.essilab.lablib.checkboxtree.TreeCheckingModel;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.dcm4che3.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.MediaSeriesGroupNode;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.media.data.Thumbnailable;
import org.weasis.core.ui.model.GraphicModel;
import org.weasis.core.util.LangUtil;
import org.weasis.dicom.codec.DcmMediaReader;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.DicomSpecialElement;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.DicomSorter;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.main.DicomExplorer;

public class CheckTreeModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckTreeModel.class);
    public static final TagW SourceSeriesForPR = new TagW("SourceSeriesForPR", TagW.TagType.OBJECT);
    protected final DefaultMutableTreeNode rootNode;
    protected final DefaultTreeModel model;
    protected final TreeCheckingModel checkingModel;
    protected final List<TreePath> defaultSelectedPaths;
    protected final DicomModel dicomModel;

    public CheckTreeModel(DicomModel dicomModel) {
        this.dicomModel = dicomModel == null ? new DicomModel() : dicomModel;
        this.model = this.buildModel(this.dicomModel);
        this.rootNode = (DefaultMutableTreeNode)this.model.getRoot();
        this.checkingModel = new DefaultTreeCheckingModel((TreeModel)this.model);
        this.defaultSelectedPaths = Collections.synchronizedList(new ArrayList());
    }

    public DicomModel getDicomModel() {
        return this.dicomModel;
    }

    public DefaultMutableTreeNode getRootNode() {
        return this.rootNode;
    }

    public DefaultTreeModel getModel() {
        return this.model;
    }

    public TreeCheckingModel getCheckingModel() {
        return this.checkingModel;
    }

    public TreePath[] getCheckingPaths() {
        return this.checkingModel.getCheckingPaths();
    }

    public void setDefaultSelectionPaths(List<TreePath> selectedPaths) {
        this.defaultSelectedPaths.clear();
        this.defaultSelectedPaths.addAll(selectedPaths);
    }

    public List<TreePath> getDefaultSelectedPaths() {
        return this.defaultSelectedPaths;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void buildSeries(DefaultMutableTreeNode studyNode, Series<?> series) {
        ArrayList<TreeNode> children;
        int index;
        DicomSeries dicomSeries;
        List specialElements;
        ToolTipSeriesNode seriesNode = new ToolTipSeriesNode((TagReadable)series, true);
        if (series instanceof DicomSeries && (specialElements = (dicomSeries = (DicomSeries)series).getAllDicomSpecialElement()) != null) {
            for (DicomSpecialElement specialElement : specialElements) {
                seriesNode.add(new DefaultMutableTreeNode(this, specialElement, false){
                    final /* synthetic */ CheckTreeModel this$0;
                    {
                        CheckTreeModel checkTreeModel = this$0;
                        Objects.requireNonNull(checkTreeModel);
                        this.this$0 = checkTreeModel;
                        super(arg0, arg1);
                    }

                    @Override
                    public String toString() {
                        DicomSpecialElement d = (DicomSpecialElement)this.getUserObject();
                        StringBuilder buf = new StringBuilder();
                        boolean newElement = CheckTreeModel.initToolTipText((TagReadable)d, buf);
                        buf.append(d.getShortLabel());
                        if (newElement) {
                            buf.append("</html>");
                        }
                        return buf.toString();
                    }
                });
            }
        }
        index = (index = Collections.binarySearch(children = Collections.list(studyNode.children()), seriesNode, DicomSorter.SERIES_COMPARATOR)) < 0 ? -(index + 1) : index;
        studyNode.insert(seriesNode, index);
        DefaultMutableTreeNode prVirtualNode = null;
        Series<?> series2 = series;
        synchronized (series2) {
            for (final MediaElement dicom : series.getMedias(null, null)) {
                seriesNode.add(new DefaultMutableTreeNode(this, dicom, false){
                    final /* synthetic */ CheckTreeModel this$0;
                    {
                        CheckTreeModel checkTreeModel = this$0;
                        Objects.requireNonNull(checkTreeModel);
                        this.this$0 = checkTreeModel;
                        super(arg0, arg1);
                    }

                    @Override
                    public String toString() {
                        String sopUID;
                        MediaElement m = (MediaElement)this.getUserObject();
                        Integer val = (Integer)TagD.getTagValue((TagReadable)m, (int)2097171, Integer.class);
                        StringBuilder buf = new StringBuilder();
                        if (val != null) {
                            buf.append("[");
                            buf.append(val);
                            buf.append("] ");
                        }
                        if ((sopUID = (String)TagD.getTagValue((TagReadable)m, (int)524312, String.class)) != null) {
                            buf.append(sopUID);
                        }
                        return buf.toString();
                    }
                });
                GraphicModel grModel = (GraphicModel)dicom.getTagValue(TagW.PresentationModel);
                if (grModel == null || !grModel.hasSerializableGraphics()) continue;
                if (prVirtualNode == null) {
                    String seriesInstanceUID = UIDUtils.createUID();
                    DicomSeries prSeries = new DicomSeries(seriesInstanceUID);
                    prSeries.setTag(TagD.get((int)0x200011), TagD.getTagValue(series, (int)0x200011, Integer.class));
                    prSeries.setTag(TagD.get((int)524384), (Object)"PR");
                    prSeries.setTag(TagD.get((int)0x20000E), (Object)seriesInstanceUID);
                    prSeries.setTag(TagW.ObjectToSave, (Object)Boolean.TRUE);
                    prSeries.setTag(SourceSeriesForPR, series);
                    prSeries.setTag(TagD.get((int)528446), (Object)(Optional.ofNullable((String)TagD.getTagValue(series, (int)528446, String.class)).orElse("") + " [GRAPHICS]"));
                    prVirtualNode = new ToolTipSeriesNode((TagReadable)prSeries, true);
                    studyNode.insert(prVirtualNode, index + 1);
                }
                prVirtualNode.add(new DefaultMutableTreeNode(this, grModel, false){
                    final /* synthetic */ CheckTreeModel this$0;
                    {
                        CheckTreeModel checkTreeModel = this$0;
                        Objects.requireNonNull(checkTreeModel);
                        this.this$0 = checkTreeModel;
                        super(arg0, arg1);
                    }

                    @Override
                    public String toString() {
                        String sopUid = (String)TagD.getTagValue((TagReadable)dicom, (int)524312, String.class);
                        Integer instance = (Integer)TagD.getTagValue((TagReadable)dicom, (int)2097171, Integer.class);
                        StringBuilder buf = new StringBuilder();
                        if (instance != null) {
                            buf.append("PR [");
                            buf.append(instance);
                            buf.append("] ");
                        }
                        if (sopUid != null) {
                            buf.append(sopUid);
                        }
                        return buf.toString();
                    }
                });
            }
        }
    }

    protected synchronized DefaultTreeModel buildModel(DicomModel dicomModel) {
        Collection<MediaSeriesGroup> patients = dicomModel.getChildren(MediaSeriesGroupNode.rootNode);
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(patients.isEmpty() ? Messages.getString("no.patient.found") : DicomExplorer.ALL_PATIENTS);
        for (MediaSeriesGroup pt : patients) {
            ToolTipPatientNode patientNode = new ToolTipPatientNode((TagReadable)pt, true);
            for (MediaSeriesGroup study : dicomModel.getChildren(pt)) {
                ToolTipStudyNode studyNode = new ToolTipStudyNode((TagReadable)study, true);
                for (MediaSeriesGroup item : dicomModel.getChildren(study)) {
                    if (!(item instanceof Series)) continue;
                    this.buildSeries(studyNode, (Series)item);
                }
                ArrayList<TreeNode> children = Collections.list(patientNode.children());
                int index = Collections.binarySearch(children, studyNode, DicomSorter.STUDY_COMPARATOR);
                if (index < 0) {
                    patientNode.insert(studyNode, -(index + 1));
                    continue;
                }
                patientNode.insert(studyNode, index);
            }
            ArrayList<TreeNode> children = Collections.list(rootNode.children());
            int index = Collections.binarySearch(children, patientNode, DicomSorter.PATIENT_COMPARATOR);
            if (index < 0) {
                rootNode.insert(patientNode, -(index + 1));
                continue;
            }
            rootNode.insert(patientNode, index);
        }
        return new DefaultTreeModel(rootNode, false);
    }

    private static boolean initToolTipText(TagReadable tagReadable, StringBuilder buf) {
        boolean newElement = LangUtil.nullToFalse((Boolean)((Boolean)tagReadable.getTagValue(TagW.ObjectToSave)));
        if (newElement) {
            buf.append("<html><font color='");
            buf.append(GuiUtils.IconColor.ACTIONS_YELLOW.getHtmlCode());
            buf.append("'><b>NEW </b></font>");
        }
        return newElement;
    }

    private static String getTags(TagReadable tagReadable) {
        StringBuilder toolTips = new StringBuilder();
        toolTips.append("<html>");
        tagReadable.getTagEntrySetIterator().forEachRemaining(i -> {
            TagW tag = (TagW)i.getKey();
            if (tag == null || tag == TagW.Timezone) {
                return;
            }
            toolTips.append("<b>");
            toolTips.append(tag.getDisplayedName());
            toolTips.append("</b>");
            toolTips.append(": ");
            String f = tag.addGMTOffset(null, tagReadable);
            toolTips.append(tag.getFormattedTagValue(i.getValue(), f));
            toolTips.append("<br>");
        });
        toolTips.append("</html>");
        return toolTips.toString();
    }

    public static String buildToolTipText(TagReadable s) {
        Thumbnailable thumb = (Thumbnailable)s.getTagValue(TagW.Thumbnail);
        if (thumb != null) {
            try {
                File path = thumb.getThumbnailPath();
                if (path != null) {
                    URL url = path.toURI().toURL();
                    StringBuilder buf = new StringBuilder();
                    buf.append("<html>");
                    buf.append("<img src=\"");
                    buf.append(url);
                    buf.append("\"><br>");
                    buf.append(DcmMediaReader.buildDateTimeWithTimeZone((TagReadable)s, (int)524321, (int)524337));
                    buf.append("</html>");
                    return buf.toString();
                }
            }
            catch (Exception e) {
                LOGGER.error("Display tooltip", (Throwable)e);
            }
        }
        return null;
    }

    public static void buildSeriesEntry(MediaSeries<?> s, StringBuilder buf) {
        String desc;
        String modality;
        Integer val = (Integer)TagD.getTagValue(s, (int)0x200011, Integer.class);
        if (val != null) {
            buf.append("[");
            buf.append(val);
            buf.append("] ");
        }
        if ((modality = (String)TagD.getTagValue(s, (int)524384, String.class)) != null) {
            buf.append(modality);
            buf.append(" ");
        }
        if ((desc = (String)TagD.getTagValue(s, (int)528446, String.class)) != null) {
            buf.append(desc);
        }
    }

    public static class ToolTipSeriesNode
    extends DefaultMutableTreeNode {
        public ToolTipSeriesNode(TagReadable userObject, boolean allowsChildren) {
            super(Objects.requireNonNull(userObject), allowsChildren);
        }

        public String getToolTipText() {
            return CheckTreeModel.buildToolTipText((TagReadable)this.getUserObject());
        }

        @Override
        public String toString() {
            MediaSeries s = (MediaSeries)this.getUserObject();
            StringBuilder buf = new StringBuilder();
            boolean newElement = CheckTreeModel.initToolTipText((TagReadable)s, buf);
            CheckTreeModel.buildSeriesEntry(s, buf);
            int child = this.getChildCount();
            if (newElement) {
                child = Math.max(1, child);
            }
            if (child > 0) {
                buf.append(" -- ").append(child).append(" instance(s)");
            }
            if (newElement) {
                buf.append("</html>");
            }
            return buf.toString();
        }
    }

    public static class ToolTipPatientNode
    extends DefaultMutableTreeNode {
        public ToolTipPatientNode(TagReadable userObject, boolean allowsChildren) {
            super(Objects.requireNonNull(userObject), allowsChildren);
        }

        public String getToolTipText() {
            return CheckTreeModel.getTags((TagReadable)this.getUserObject());
        }
    }

    public static class ToolTipStudyNode
    extends DefaultMutableTreeNode {
        public ToolTipStudyNode(TagReadable userObject, boolean allowsChildren) {
            super(Objects.requireNonNull(userObject), allowsChildren);
        }

        public String getToolTipText() {
            return CheckTreeModel.getTags((TagReadable)this.getUserObject());
        }
    }
}

