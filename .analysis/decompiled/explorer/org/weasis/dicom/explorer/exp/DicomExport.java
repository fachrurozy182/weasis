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
 *  org.weasis.core.api.gui.util.AbstractWizardDialog
 *  org.weasis.core.api.gui.util.AppProperties
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.gui.util.PageItem
 *  org.weasis.dicom.codec.DicomSeries
 */
package org.weasis.dicom.explorer.exp;

import eu.essilab.lablib.checkboxtree.TreeCheckingModel;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JComponent;
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
import org.weasis.core.api.gui.util.AbstractWizardDialog;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.PageItem;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.LocalPersistence;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.exp.CheckTreeModel;
import org.weasis.dicom.explorer.exp.DicomExportFactory;
import org.weasis.dicom.explorer.exp.ExportDicom;
import org.weasis.dicom.explorer.exp.LocalExport;
import org.weasis.dicom.explorer.main.DicomExplorer;

public class DicomExport
extends AbstractWizardDialog {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomExport.class);
    private static final String LAST_PAGE = "last.dicom.export.page";
    private final DicomModel dicomModel;
    private final CheckTreeModel treeModel;

    public DicomExport(Window parent, DicomModel dicomModel) {
        super(parent, Messages.getString("DicomExport.exp_dicom"), Dialog.ModalityType.APPLICATION_MODAL, new Dimension(650, 500));
        this.dicomModel = dicomModel;
        this.treeModel = new CheckTreeModel(dicomModel);
        JButton exportAndClose = new JButton(Messages.getString("DicomExport.exp_close"));
        exportAndClose.addActionListener(actionEvent -> {
            this.exportSelection();
            this.cancel();
        });
        JButton exportButton = new JButton();
        exportButton.addActionListener(actionEvent -> this.exportSelection());
        exportButton.setText(Messages.getString("DicomExport.exp"));
        JButton jButtonHelp = GuiUtils.createHelpButton((String)"dicom-export/#dicom-export");
        this.jPanelBottom.removeAll();
        this.jPanelBottom.add(GuiUtils.getFlowLayoutPanel((int)4, (int)15, (int)10, (JComponent[])new JComponent[]{jButtonHelp, exportButton, exportAndClose, this.jButtonClose}));
        this.initializePages();
        this.pack();
        this.showPage(LocalPersistence.getProperties().getProperty(LAST_PAGE));
    }

    protected void initializePages() {
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put(this.dicomModel.getClass().getName(), this.dicomModel);
        properties.put(this.treeModel.getClass().getName(), this.treeModel);
        this.initTreeCheckingModel();
        ArrayList<AbstractItemDialogPage> list = new ArrayList<AbstractItemDialogPage>();
        list.add(new LocalExport(this.dicomModel, this.treeModel));
        BundleContext context = AppProperties.getBundleContext(((Object)((Object)this)).getClass());
        if (context == null) {
            LOGGER.error("Bundle context is null, cannot load DICOM export plugins");
            return;
        }
        try {
            for (ServiceReference service : context.getServiceReferences(DicomExportFactory.class, null)) {
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
        InsertableUtil.sortInsertable(list);
        for (AbstractItemDialogPage page : list) {
            this.pagesRoot.add(new DefaultMutableTreeNode(page));
        }
        this.iniTree();
    }

    protected void initTreeCheckingModel() {
        TreeCheckingModel checkingModel = this.treeModel.getCheckingModel();
        checkingModel.setCheckingMode(TreeCheckingModel.CheckingMode.PROPAGATE_PRESERVING_UNCHECK);
        DataExplorerView dataExplorerView = GuiUtils.getUICore().getExplorerPlugin(DicomExplorer.NAME);
        if (dataExplorerView instanceof DicomExplorer) {
            DicomExplorer explorer = (DicomExplorer)dataExplorerView;
            Set<DicomSeries> openSeriesSet = explorer.getSelectedPatientOpenSeries();
            Object rootNode = this.treeModel.getModel().getRoot();
            if (!openSeriesSet.isEmpty() && rootNode instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode)rootNode;
                ArrayList<TreePath> selectedSeriesPathsList = new ArrayList<TreePath>();
                Enumeration<TreeNode> enumTreeNode = mutableTreeNode.breadthFirstEnumeration();
                while (enumTreeNode.hasMoreElements()) {
                    Object userObject;
                    DefaultMutableTreeNode treeNode;
                    TreeNode child = enumTreeNode.nextElement();
                    if (!(child instanceof DefaultMutableTreeNode) || (treeNode = (DefaultMutableTreeNode)child).getLevel() != 3 || !((userObject = treeNode.getUserObject()) instanceof DicomSeries) || !openSeriesSet.contains(userObject)) continue;
                    selectedSeriesPathsList.add(new TreePath(treeNode.getPath()));
                }
                if (!selectedSeriesPathsList.isEmpty()) {
                    TreePath[] seriesCheckingPaths = selectedSeriesPathsList.toArray(new TreePath[0]);
                    checkingModel.setCheckingPaths(seriesCheckingPaths);
                    this.treeModel.setDefaultSelectionPaths(selectedSeriesPathsList);
                }
            }
        }
    }

    private void exportSelection() {
        Component object = null;
        try {
            object = this.jScrollPanePage.getViewport().getComponent(0);
        }
        catch (Exception e) {
            LOGGER.debug("Failed to extract DICOM export", (Throwable)e);
        }
        if (object instanceof ExportDicom) {
            ExportDicom selectedPage = (ExportDicom)((Object)object);
            try {
                selectedPage.exportDICOM(this.treeModel, null);
            }
            catch (IOException e) {
                LOGGER.error("DICOM export failed", (Throwable)e);
            }
        }
    }

    public void cancel() {
        this.dispose();
    }

    public void dispose() {
        PageItem page = this.getSelectedPage();
        if (page != null) {
            LocalPersistence.getProperties().setProperty(LAST_PAGE, page.getTitle());
        }
        this.closeAllPages();
        super.dispose();
    }

    public DicomModel getDicomModel() {
        return this.dicomModel;
    }

    public CheckTreeModel getTreeModel() {
        return this.treeModel;
    }
}

