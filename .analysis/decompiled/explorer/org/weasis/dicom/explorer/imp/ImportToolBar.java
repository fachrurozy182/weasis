/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.gui.Insertable
 *  org.weasis.core.api.gui.util.AbstractItemDialogPage
 *  org.weasis.core.api.gui.util.AbstractWizardDialog
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.gui.util.WinUtil
 *  org.weasis.core.api.service.WProperties
 *  org.weasis.core.api.util.ResourceUtil
 *  org.weasis.core.api.util.ResourceUtil$ActionIcon
 *  org.weasis.core.api.util.ResourceUtil$ResourceIconPath
 *  org.weasis.core.ui.util.ColorLayerUI
 *  org.weasis.core.ui.util.DefaultAction
 *  org.weasis.core.ui.util.WtoolBar
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.explorer.imp;

import java.awt.Component;
import java.awt.Window;
import java.io.File;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.Insertable;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;
import org.weasis.core.api.gui.util.AbstractWizardDialog;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.WinUtil;
import org.weasis.core.api.service.WProperties;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.util.ColorLayerUI;
import org.weasis.core.ui.util.DefaultAction;
import org.weasis.core.ui.util.WtoolBar;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.HangingProtocols;
import org.weasis.dicom.explorer.LoadDicomDir;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.exp.DicomExport;
import org.weasis.dicom.explorer.imp.DicomDirImport;
import org.weasis.dicom.explorer.imp.DicomImport;
import org.weasis.dicom.explorer.imp.LocalImport;
import org.weasis.dicom.explorer.main.DicomExplorer;
import org.weasis.dicom.explorer.wado.LoadSeries;

public class ImportToolBar
extends WtoolBar {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportToolBar.class);

    public ImportToolBar(int index, DicomExplorer explorer) {
        super(Messages.getString("ImportToolBar.import_dcm"), index);
        JButton btnImport;
        this.setAttachedInsertable((Insertable)explorer);
        DicomModel model = explorer.getDataExplorerModel();
        WProperties preferences = GuiUtils.getUICore().getSystemPreferences();
        if (preferences.getBooleanProperty("weasis.import.dicom", true)) {
            btnImport = new JButton((Icon)ResourceUtil.getToolBarIcon((ResourceUtil.ResourceIconPath)ResourceUtil.ActionIcon.IMPORT_DICOM));
            btnImport.setToolTipText(Messages.getString("ImportToolBar.import_dcm"));
            btnImport.addActionListener(e -> ImportToolBar.showAction((Component)((Object)this), model, null, false));
            this.add(btnImport);
        }
        if (preferences.getBooleanProperty("weasis.import.dicom", true)) {
            btnImport = new JButton((Icon)ResourceUtil.getToolBarIcon((ResourceUtil.ResourceIconPath)ResourceUtil.ActionIcon.IMPORT_CD));
            btnImport.setToolTipText(Messages.getString("ImportToolBar.import_dcm_cd"));
            btnImport.addActionListener(e -> ImportToolBar.openImportDicomCdAction((Component)((Object)this), model, Messages.getString("DicomExplorer.dcmCD")));
            this.add(btnImport);
        }
    }

    public static void openImportDicomCdAction(Component parent, DicomModel model, String actionName) {
        File file = DicomDirImport.getDcmDirFromMedia();
        if (file == null) {
            int response = JOptionPane.showConfirmDialog(WinUtil.getValidComponent((Component)SwingUtilities.getWindowAncestor(parent)), Messages.getString("ImportToolBar.import_cd_question"), actionName, 0, 2);
            if (response == 0) {
                ImportToolBar.showAction(parent, model, Messages.getString("DicomDirImport.dicomdir"), false);
            }
        } else {
            List<LoadSeries> loadSeries = DicomDirImport.loadDicomDir(file, model, true);
            if (loadSeries != null && !loadSeries.isEmpty()) {
                DicomModel.LOADING_EXECUTOR.execute(new LoadDicomDir(loadSeries, model, HangingProtocols.OpeningViewer.ALL_PATIENTS));
            } else {
                AbstractWizardDialog dialog;
                AbstractItemDialogPage page;
                LOGGER.error("Cannot import DICOM from {}", (Object)file);
                int response = JOptionPane.showConfirmDialog(WinUtil.getValidComponent((Component)SwingUtilities.getWindowAncestor(parent)), Messages.getString("DicomExplorer.mes_import_manual"), actionName, 0, 2);
                if (response == 0 && (page = (dialog = ImportToolBar.showAction(parent, model, Messages.getString("LocalImport.local_dev"), false)).getCurrentPage()) instanceof LocalImport) {
                    LocalImport localImport = (LocalImport)page;
                    localImport.setImportPath(file.getParent());
                }
            }
        }
    }

    public static AbstractWizardDialog showAction(Component parent, DicomModel model, String pageName, boolean export) {
        AbstractWizardDialog dialog;
        ColorLayerUI layer = ColorLayerUI.createTransparentLayerUI((Component)parent);
        Window win = SwingUtilities.getWindowAncestor(parent);
        AbstractWizardDialog abstractWizardDialog = dialog = export ? new DicomExport(win, model) : new DicomImport(win, model);
        if (StringUtil.hasText((CharSequence)pageName)) {
            dialog.showPage(pageName);
        }
        ColorLayerUI.showCenterScreen((Window)dialog, (ColorLayerUI)layer);
        return dialog;
    }

    public static DefaultAction buildImportAction(Component parent, DicomModel model, String actionName) {
        return new DefaultAction(actionName, (Icon)ResourceUtil.getIcon((ResourceUtil.ResourceIconPath)ResourceUtil.ActionIcon.IMPORT_DICOM), event -> {
            if (GuiUtils.getUICore().getSystemPreferences().getBooleanProperty("weasis.import.dicom", true)) {
                ImportToolBar.showAction(parent, model, null, false);
            } else {
                JOptionPane.showMessageDialog((Component)event.getSource(), Messages.getString("DicomExplorer.export_perm"));
            }
        });
    }
}

