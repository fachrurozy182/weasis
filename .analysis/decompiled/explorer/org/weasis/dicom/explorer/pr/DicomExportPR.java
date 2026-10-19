/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  eu.essilab.lablib.checkboxtree.TreeCheckingModel
 *  eu.essilab.lablib.checkboxtree.TreeCheckingModel$CheckingMode
 *  org.osgi.framework.BundleContext
 *  org.osgi.framework.ServiceReference
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.explorer.DataExplorerView
 *  org.weasis.core.api.gui.InsertableUtil
 *  org.weasis.core.api.gui.util.AbstractItemDialogPage
 *  org.weasis.core.api.gui.util.AppProperties
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.dicom.codec.DicomSeries
 *  org.weasis.dicom.codec.TagD
 *  org.weasis.dicom.codec.display.Modality
 */
package org.weasis.dicom.explorer.pr;

import eu.essilab.lablib.checkboxtree.TreeCheckingModel;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.DataExplorerView;
import org.weasis.core.api.gui.InsertableUtil;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.TagW;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.display.Modality;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.exp.CheckTreeModel;
import org.weasis.dicom.explorer.exp.DicomExport;
import org.weasis.dicom.explorer.exp.DicomExportFactory;
import org.weasis.dicom.explorer.exp.ExportDicom;
import org.weasis.dicom.explorer.main.DicomExplorer;

public class DicomExportPR
extends DicomExport {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomExportPR.class);
    private boolean containsPR;

    public DicomExportPR(Window parent, DicomModel dicomModel) {
        super(parent, dicomModel);
    }

    @Override
    protected void initializePages() {
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put(this.getDicomModel().getClass().getName(), this.getDicomModel());
        properties.put(this.getTreeModel().getClass().getName(), this.getTreeModel());
        this.initTreeCheckingModel();
        ArrayList<AbstractItemDialogPage> list = new ArrayList<AbstractItemDialogPage>();
        BundleContext context = AppProperties.getBundleContext(((Object)((Object)this)).getClass());
        if (context != null) {
            try {
                for (ServiceReference service : context.getServiceReferences(DicomExportFactory.class, "(component.name=org.weasis.dicom.send.SendDicomFactory)")) {
                    ExportDicom page;
                    DicomExportFactory factory = (DicomExportFactory)context.getService(service);
                    if (factory == null || !((page = factory.createDicomExportPage(properties)) instanceof AbstractItemDialogPage)) continue;
                    AbstractItemDialogPage dialogPage = (AbstractItemDialogPage)page;
                    list.add(dialogPage);
                }
            }
            catch (Exception e) {
                LOGGER.error("Insert DICOM export plugins", (Throwable)e);
            }
        }
        InsertableUtil.sortInsertable(list);
        for (AbstractItemDialogPage page : list) {
            this.pagesRoot.add(new DefaultMutableTreeNode(page));
        }
        this.iniTree();
    }

    @Override
    protected void initTreeCheckingModel() {
        TreeCheckingModel checkingModel = this.getTreeModel().getCheckingModel();
        checkingModel.setCheckingMode(TreeCheckingModel.CheckingMode.PROPAGATE_PRESERVING_UNCHECK);
        ArrayList<DefaultMutableTreeNode> nodesToDelete = new ArrayList<DefaultMutableTreeNode>();
        DataExplorerView dataExplorerView = GuiUtils.getUICore().getExplorerPlugin(DicomExplorer.NAME);
        if (dataExplorerView instanceof DicomExplorer) {
            DicomExplorer explorer = (DicomExplorer)dataExplorerView;
            Set<DicomSeries> openSeriesSet = explorer.getSelectedPatientOpenSeries();
            Object rootNode = this.getTreeModel().getModel().getRoot();
            if (!openSeriesSet.isEmpty() && rootNode instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode)rootNode;
                ArrayList<TreePath> selectedSeriesPathsList = new ArrayList<TreePath>();
                Enumeration<TreeNode> enumTreeNode = mutableTreeNode.breadthFirstEnumeration();
                while (enumTreeNode.hasMoreElements()) {
                    Object userObject;
                    DefaultMutableTreeNode treeNode;
                    TreeNode child = enumTreeNode.nextElement();
                    if (!(child instanceof DefaultMutableTreeNode) || (treeNode = (DefaultMutableTreeNode)child).getLevel() != 3 || !((userObject = treeNode.getUserObject()) instanceof DicomSeries)) continue;
                    DicomSeries series = (DicomSeries)userObject;
                    if (series.getTagValue(TagD.get((int)524384)).equals("PR") && series.getTagValue(TagW.ObjectToSave) != null && this.isSourceSeriesSuitableForPRExport(series.getTagValue(CheckTreeModel.SourceSeriesForPR))) {
                        selectedSeriesPathsList.add(new TreePath(treeNode.getPath()));
                        this.containsPR = true;
                        continue;
                    }
                    nodesToDelete.add(treeNode);
                }
                if (!selectedSeriesPathsList.isEmpty()) {
                    TreePath[] seriesCheckingPaths = selectedSeriesPathsList.toArray(new TreePath[0]);
                    checkingModel.setCheckingPaths(seriesCheckingPaths);
                    this.getTreeModel().setDefaultSelectionPaths(selectedSeriesPathsList);
                }
                if (!nodesToDelete.isEmpty()) {
                    for (DefaultMutableTreeNode node : nodesToDelete) {
                        node.removeAllChildren();
                        TreeNode[] path = node.getPath();
                        for (int i = path.length - 1; i >= 0; --i) {
                            DefaultMutableTreeNode n = (DefaultMutableTreeNode)path[i];
                            if (n.getLevel() == 0 || !n.isLeaf()) continue;
                            n.removeFromParent();
                        }
                    }
                }
            }
        }
    }

    public boolean isContainsPR() {
        return this.containsPR;
    }

    private boolean isSourceSeriesSuitableForPRExport(Object sourceSeries) {
        List<String> modalities = List.of(Modality.XC.name());
        if (sourceSeries instanceof DicomSeries) {
            DicomSeries series = (DicomSeries)sourceSeries;
            return modalities.contains(series.getTagValue(TagD.get((int)524384)).toString());
        }
        return false;
    }
}

