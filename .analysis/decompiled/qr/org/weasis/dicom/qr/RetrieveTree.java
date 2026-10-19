/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  eu.essilab.lablib.checkboxtree.CheckboxTree
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.dicom.explorer.DicomModel
 *  org.weasis.dicom.explorer.exp.CheckTreeModel
 *  org.weasis.dicom.explorer.exp.ExportTree
 */
package org.weasis.dicom.qr;

import eu.essilab.lablib.checkboxtree.CheckboxTree;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Objects;
import javax.swing.JPanel;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.exp.CheckTreeModel;
import org.weasis.dicom.explorer.exp.ExportTree;
import org.weasis.dicom.qr.RetrieveTreeModel;

public class RetrieveTree
extends JPanel {
    private RetrieveTreeModel retrieveTreeModel;
    private SeriesLoader seriesLoader;

    public RetrieveTree() {
        this(new RetrieveTreeModel());
    }

    public RetrieveTree(DicomModel dicomModel) {
        this(new RetrieveTreeModel(dicomModel));
    }

    public RetrieveTree(RetrieveTreeModel retrieveTreeModel) {
        this.setLayout(new BorderLayout());
        this.setRetrieveTreeModel(retrieveTreeModel);
    }

    public RetrieveTreeModel getRetrieveTreeModel() {
        return this.retrieveTreeModel;
    }

    public void setSeriesLoader(SeriesLoader seriesLoader) {
        this.seriesLoader = seriesLoader;
    }

    public void setRetrieveTreeModel(RetrieveTreeModel retrieveTreeModel) {
        this.retrieveTreeModel = Objects.requireNonNull(retrieveTreeModel);
        CheckboxTree checkboxTree = ExportTree.buildCheckboxTree((CheckTreeModel)retrieveTreeModel);
        ExportTree.initTree((CheckTreeModel)retrieveTreeModel, (CheckboxTree)checkboxTree);
        checkboxTree.addTreeExpansionListener((TreeExpansionListener)new SeriesExpansionListener(this));
        this.removeAll();
        this.add((Component)checkboxTree, "Center");
    }

    public CheckboxTree getCheckboxTree() {
        for (int i = 0; i < this.getComponentCount(); ++i) {
            Component c = this.getComponent(i);
            if (!(c instanceof CheckboxTree)) continue;
            CheckboxTree tree = (CheckboxTree)c;
            return tree;
        }
        throw new IllegalStateException("CheckboxTree cannot be null");
    }

    @FunctionalInterface
    public static interface SeriesLoader {
        public void loadSeries(RetrieveTreeModel var1, DefaultMutableTreeNode var2);
    }

    private class SeriesExpansionListener
    implements TreeExpansionListener {
        final /* synthetic */ RetrieveTree this$0;

        private SeriesExpansionListener(RetrieveTree retrieveTree) {
            RetrieveTree retrieveTree2 = retrieveTree;
            Objects.requireNonNull(retrieveTree2);
            this.this$0 = retrieveTree2;
        }

        @Override
        public void treeExpanded(TreeExpansionEvent event) {
            DefaultMutableTreeNode studyNode;
            if (this.this$0.seriesLoader == null || event.getPath().getPathCount() != 3) {
                return;
            }
            Object object = event.getPath().getLastPathComponent();
            if (object instanceof DefaultMutableTreeNode && (studyNode = (DefaultMutableTreeNode)object).getUserObject() instanceof MediaSeriesGroup && RetrieveTreeModel.hasPlaceholder(studyNode)) {
                this.this$0.seriesLoader.loadSeries(this.this$0.retrieveTreeModel, studyNode);
            }
        }

        @Override
        public void treeCollapsed(TreeExpansionEvent event) {
        }
    }
}

