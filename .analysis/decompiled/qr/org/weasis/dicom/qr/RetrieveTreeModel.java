/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.media.data.MediaSeries
 *  org.weasis.core.api.media.data.Series
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.dicom.codec.TagD
 *  org.weasis.dicom.explorer.DicomModel
 *  org.weasis.dicom.explorer.DicomSorter
 *  org.weasis.dicom.explorer.exp.CheckTreeModel
 *  org.weasis.dicom.explorer.exp.CheckTreeModel$ToolTipSeriesNode
 */
package org.weasis.dicom.qr;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.DicomSorter;
import org.weasis.dicom.explorer.exp.CheckTreeModel;
import org.weasis.dicom.qr.Messages;

public class RetrieveTreeModel
extends CheckTreeModel {
    public RetrieveTreeModel() {
        this(null);
    }

    public RetrieveTreeModel(DicomModel dicomModel) {
        super(dicomModel);
    }

    protected void buildSeries(DefaultMutableTreeNode studyNode, Series<?> series) {
        RetrieveTreeModel.insertSeriesNode(studyNode, (DefaultMutableTreeNode)((Object)new QuerySeriesNode(series)));
    }

    protected synchronized DefaultTreeModel buildModel(DicomModel dicomModel) {
        DefaultTreeModel treeModel = super.buildModel(dicomModel);
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)treeModel.getRoot();
        RetrieveTreeModel.forEachStudyNode(root, studyNode -> {
            if (studyNode.getChildCount() == 0) {
                studyNode.add(new SeriesPlaceholderNode());
            }
        });
        return treeModel;
    }

    public static boolean hasPlaceholder(DefaultMutableTreeNode studyNode) {
        SeriesPlaceholderNode placeholder;
        TreeNode treeNode;
        return studyNode.getChildCount() == 1 && (treeNode = studyNode.getChildAt(0)) instanceof SeriesPlaceholderNode && !(placeholder = (SeriesPlaceholderNode)treeNode).isLoading();
    }

    public void setPlaceholderLoading(DefaultMutableTreeNode studyNode, boolean loading) {
        for (int i = 0; i < studyNode.getChildCount(); ++i) {
            TreeNode treeNode = studyNode.getChildAt(i);
            if (!(treeNode instanceof SeriesPlaceholderNode)) continue;
            SeriesPlaceholderNode placeholder = (SeriesPlaceholderNode)treeNode;
            placeholder.setLoading(loading);
            this.model.nodeChanged(placeholder);
        }
    }

    public void setSeriesNodes(DefaultMutableTreeNode studyNode, List<Series<?>> seriesList) {
        for (Series<?> series : seriesList) {
            QuerySeriesNode seriesNode = new QuerySeriesNode(series);
            this.model.insertNodeInto((MutableTreeNode)((Object)seriesNode), studyNode, RetrieveTreeModel.insertionIndex(studyNode, (DefaultMutableTreeNode)((Object)seriesNode)));
        }
        this.removePlaceholder(studyNode);
    }

    public void removePlaceholder(DefaultMutableTreeNode studyNode) {
        for (int i = studyNode.getChildCount() - 1; i >= 0; --i) {
            TreeNode treeNode = studyNode.getChildAt(i);
            if (!(treeNode instanceof SeriesPlaceholderNode)) continue;
            SeriesPlaceholderNode placeholder = (SeriesPlaceholderNode)treeNode;
            this.model.removeNodeFromParent(placeholder);
        }
    }

    private static void insertSeriesNode(DefaultMutableTreeNode studyNode, DefaultMutableTreeNode seriesNode) {
        studyNode.insert(seriesNode, RetrieveTreeModel.insertionIndex(studyNode, seriesNode));
    }

    private static int insertionIndex(DefaultMutableTreeNode studyNode, DefaultMutableTreeNode seriesNode) {
        List series = Collections.list(studyNode.children()).stream().filter(QuerySeriesNode.class::isInstance).toList();
        int index = Collections.binarySearch(series, seriesNode, DicomSorter.SERIES_COMPARATOR);
        return index < 0 ? -(index + 1) : index;
    }

    private static void forEachStudyNode(DefaultMutableTreeNode root, Consumer<DefaultMutableTreeNode> action) {
        Enumeration<TreeNode> patients = root.children();
        while (patients.hasMoreElements()) {
            DefaultMutableTreeNode patient = (DefaultMutableTreeNode)patients.nextElement();
            Enumeration<TreeNode> studies = patient.children();
            while (studies.hasMoreElements()) {
                action.accept((DefaultMutableTreeNode)studies.nextElement());
            }
        }
    }

    public static class QuerySeriesNode
    extends CheckTreeModel.ToolTipSeriesNode {
        public QuerySeriesNode(Series<?> series) {
            super(series, true);
        }

        public String toString() {
            MediaSeries series = (MediaSeries)this.getUserObject();
            StringBuilder buf = new StringBuilder();
            CheckTreeModel.buildSeriesEntry((MediaSeries)series, (StringBuilder)buf);
            Integer instances = (Integer)TagD.getTagValue((TagReadable)series, (int)2101769, Integer.class);
            if (instances != null) {
                buf.append(" -- ").append(instances).append(" instance(s)");
            }
            return buf.toString();
        }
    }

    public static class SeriesPlaceholderNode
    extends DefaultMutableTreeNode {
        private boolean loading;

        public SeriesPlaceholderNode() {
            super(Messages.getString("RetrieveTreeModel.expand_series"), false);
        }

        public boolean isLoading() {
            return this.loading;
        }

        void setLoading(boolean loading) {
            this.loading = loading;
            this.setUserObject(Messages.getString(loading ? "RetrieveTreeModel.loading_series" : "RetrieveTreeModel.expand_series"));
        }
    }
}

