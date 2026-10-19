/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.osgi.framework.BundleContext
 *  org.osgi.framework.ServiceReference
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.gui.InsertableUtil
 *  org.weasis.core.api.gui.util.AbstractItemDialogPage
 *  org.weasis.core.api.gui.util.AbstractWizardDialog
 *  org.weasis.core.api.gui.util.AppProperties
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.gui.util.PageItem
 */
package org.weasis.dicom.explorer.imp;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.InsertableUtil;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;
import org.weasis.core.api.gui.util.AbstractWizardDialog;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.PageItem;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.LocalPersistence;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.imp.DicomDirImport;
import org.weasis.dicom.explorer.imp.DicomImportFactory;
import org.weasis.dicom.explorer.imp.ImportDicom;
import org.weasis.dicom.explorer.imp.LocalImport;

public class DicomImport
extends AbstractWizardDialog {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomImport.class);
    private static final String LAST_PAGE = "last.dicom.import.page";
    private boolean cancelVeto = false;
    private final DicomModel dicomModel;

    public DicomImport(Window parent, DicomModel dicomModel) {
        super(parent, Messages.getString("DicomImport.imp_dicom"), Dialog.ModalityType.APPLICATION_MODAL, new Dimension(650, 500));
        this.dicomModel = dicomModel;
        JButton importAndClose = new JButton(Messages.getString("DicomImport.impAndClose0"));
        importAndClose.addActionListener(e -> {
            this.importSelection();
            this.cancel();
        });
        JButton importButton = new JButton(Messages.getString("DicomImport.imp"));
        importButton.addActionListener(e -> this.importSelection());
        JButton jButtonHelp = GuiUtils.createHelpButton((String)"dicom-import");
        this.jPanelBottom.removeAll();
        this.jPanelBottom.add(GuiUtils.getFlowLayoutPanel((int)4, (int)15, (int)10, (JComponent[])new JComponent[]{jButtonHelp, importButton, importAndClose, this.jButtonClose}));
        this.initializePages();
        this.pack();
        this.showPage(LocalPersistence.getProperties().getProperty(LAST_PAGE));
    }

    protected void initializePages() {
        ArrayList<AbstractItemDialogPage> list = new ArrayList<AbstractItemDialogPage>();
        list.add(new LocalImport());
        list.add(new DicomDirImport());
        BundleContext context = AppProperties.getBundleContext(((Object)((Object)this)).getClass());
        if (context != null) {
            try {
                for (ServiceReference service : context.getServiceReferences(DicomImportFactory.class, null)) {
                    ImportDicom page;
                    DicomImportFactory factory = (DicomImportFactory)context.getService(service);
                    if (factory == null || !((page = factory.createDicomImportPage(null)) instanceof AbstractItemDialogPage)) continue;
                    AbstractItemDialogPage dialogPage = (AbstractItemDialogPage)page;
                    list.add(dialogPage);
                }
            }
            catch (Exception e) {
                LOGGER.error("init import pages", (Throwable)e);
            }
        }
        InsertableUtil.sortInsertable(list);
        for (AbstractItemDialogPage page : list) {
            this.pagesRoot.add(new DefaultMutableTreeNode(page));
        }
        this.iniTree();
    }

    private void importSelection() {
        Component object = null;
        try {
            object = this.jScrollPanePage.getViewport().getComponent(0);
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (object instanceof ImportDicom) {
            ImportDicom selectedPage = (ImportDicom)((Object)object);
            selectedPage.importDICOM(this.dicomModel, null);
        }
    }

    public void setCancelVeto(boolean cancelVeto) {
        this.cancelVeto = cancelVeto;
    }

    public void cancel() {
        if (this.cancelVeto) {
            this.cancelVeto = false;
        } else {
            this.dispose();
        }
    }

    public void dispose() {
        PageItem page = this.getSelectedPage();
        if (page != null) {
            LocalPersistence.getProperties().setProperty(LAST_PAGE, page.getTitle());
        }
        this.closeAllPages();
        super.dispose();
    }
}

