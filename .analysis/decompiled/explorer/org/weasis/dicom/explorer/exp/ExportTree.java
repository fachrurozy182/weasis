/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  eu.essilab.lablib.checkboxtree.CheckboxTree
 *  eu.essilab.lablib.checkboxtree.TreeCheckingModel
 *  org.weasis.core.ui.util.TreeBuilder
 */
package org.weasis.dicom.explorer.exp;

import eu.essilab.lablib.checkboxtree.CheckboxTree;
import eu.essilab.lablib.checkboxtree.TreeCheckingModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import org.weasis.core.ui.util.TreeBuilder;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.exp.CheckTreeModel;

public class ExportTree
extends JPanel {
    public ExportTree(DicomModel dicomModel) {
        this(new CheckTreeModel(dicomModel));
    }

    public ExportTree(CheckTreeModel checkTreeModel) {
        this.setLayout(new BorderLayout());
        this.setCheckboxTreeModel(checkTreeModel);
    }

    public void setCheckboxTreeModel(CheckTreeModel checkTreeModel) {
        Objects.requireNonNull(checkTreeModel);
        CheckboxTree checkboxTree = ExportTree.buildCheckboxTree(checkTreeModel);
        ExportTree.initTree(checkTreeModel, checkboxTree);
        this.add((Component)checkboxTree, "Center");
    }

    public static CheckboxTree buildCheckboxTree(CheckTreeModel checkTreeModel) {
        return new CheckboxTree(checkTreeModel.getModel()){

            public String getToolTipText(MouseEvent evt) {
                if (this.getRowForLocation(evt.getX(), evt.getY()) == -1) {
                    return null;
                }
                TreePath curPath = this.getPathForLocation(evt.getX(), evt.getY());
                if (curPath != null) {
                    Object object = curPath.getLastPathComponent();
                    if (object instanceof CheckTreeModel.ToolTipPatientNode) {
                        CheckTreeModel.ToolTipPatientNode tipPatientNode = (CheckTreeModel.ToolTipPatientNode)object;
                        return tipPatientNode.getToolTipText();
                    }
                    if (object instanceof CheckTreeModel.ToolTipStudyNode) {
                        CheckTreeModel.ToolTipStudyNode tipStudyNode = (CheckTreeModel.ToolTipStudyNode)object;
                        return tipStudyNode.getToolTipText();
                    }
                    if (object instanceof CheckTreeModel.ToolTipSeriesNode) {
                        CheckTreeModel.ToolTipSeriesNode tipSeriesNode = (CheckTreeModel.ToolTipSeriesNode)object;
                        return tipSeriesNode.getToolTipText();
                    }
                }
                return null;
            }
        };
    }

    public static void initTree(CheckTreeModel checkTreeModel, CheckboxTree checkboxTree) {
        checkboxTree.setCellRenderer((TreeCellRenderer)TreeBuilder.buildNoIconCheckboxTreeCellRenderer());
        checkboxTree.setToolTipText("");
        TreeCheckingModel checkingModel = checkTreeModel.getCheckingModel();
        TreePath[] checkingPaths = checkTreeModel.getCheckingPaths();
        checkboxTree.setCheckingModel(checkingModel);
        if (checkingPaths != null && checkingPaths.length > 0) {
            List<TreePath> selectedPaths;
            HashSet<TreePath> studyPathsSet = new HashSet<TreePath>();
            for (TreePath checkingPath : checkingPaths) {
                if (checkingPath.getPathCount() != 4) continue;
                studyPathsSet.add(checkingPath.getParentPath());
            }
            if (!studyPathsSet.isEmpty()) {
                TreePath[] studyCheckingPaths = studyPathsSet.toArray(new TreePath[0]);
                checkboxTree.setCheckingPaths(studyCheckingPaths);
            }
            if (!(selectedPaths = checkTreeModel.getDefaultSelectedPaths()).isEmpty()) {
                checkboxTree.setSelectionPaths(selectedPaths.toArray(new TreePath[0]));
            }
        }
        TreeBuilder.expandTree((JTree)checkboxTree, (DefaultMutableTreeNode)checkTreeModel.getRootNode(), (int)2);
    }
}

