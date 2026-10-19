/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.gui.Insertable
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.util.ResourceUtil
 *  org.weasis.core.api.util.ResourceUtil$ActionIcon
 *  org.weasis.core.api.util.ResourceUtil$ResourceIconPath
 *  org.weasis.core.ui.util.ColorLayerUI
 *  org.weasis.core.ui.util.DefaultAction
 *  org.weasis.core.ui.util.WtoolBar
 */
package org.weasis.dicom.explorer.exp;

import java.awt.Component;
import java.awt.Window;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.weasis.core.api.gui.Insertable;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.util.ColorLayerUI;
import org.weasis.core.ui.util.DefaultAction;
import org.weasis.core.ui.util.WtoolBar;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.imp.ImportToolBar;
import org.weasis.dicom.explorer.main.DicomExplorer;
import org.weasis.dicom.explorer.pr.DicomExportPR;

public class ExportToolBar
extends WtoolBar {
    public ExportToolBar(int index, DicomExplorer explorer) {
        super(Messages.getString("ExportToolBar.export_dcm"), index);
        this.setAttachedInsertable((Insertable)explorer);
        DicomModel model = explorer.getDataExplorerModel();
        if (GuiUtils.getUICore().getSystemPreferences().getBooleanProperty("weasis.export.dicom", true)) {
            JButton btnExport = new JButton((Icon)ResourceUtil.getToolBarIcon((ResourceUtil.ResourceIconPath)ResourceUtil.ActionIcon.EXPORT_DICOM));
            btnExport.setToolTipText(Messages.getString("ExportToolBar.export_dcm"));
            btnExport.addActionListener(e -> ImportToolBar.showAction((Component)((Object)this), model, null, true));
            this.add(btnExport);
        }
        if (GuiUtils.getUICore().getSystemPreferences().getBooleanProperty("weasis.export.annotations", false)) {
            JButton sendButton = new JButton();
            sendButton.setToolTipText(Messages.getString("AnnotationsToolBar.export_pr"));
            sendButton.setIcon((Icon)ResourceUtil.getToolBarIcon((ResourceUtil.ResourceIconPath)ResourceUtil.ActionIcon.EXPORT_ANNOTATIONS));
            sendButton.addActionListener(e -> {
                Window win = SwingUtilities.getWindowAncestor((Component)((Object)this));
                DicomExportPR dialog = new DicomExportPR(win, model);
                if (dialog.isContainsPR()) {
                    dialog.showFirstPage();
                    dialog.setAlwaysOnTop(true);
                    ColorLayerUI layer = ColorLayerUI.createTransparentLayerUI((Component)((Object)this));
                    ColorLayerUI.showCenterScreen((Window)((Object)dialog), (ColorLayerUI)layer);
                } else {
                    JOptionPane.showMessageDialog(win, Messages.getString("AnnotationsToolBar.error_no_pr"));
                }
            });
            this.add(sendButton);
        }
    }

    public static DefaultAction buildExportAction(Component parent, DicomModel model, String actionName) {
        return new DefaultAction(actionName, (Icon)ResourceUtil.getIcon((ResourceUtil.ResourceIconPath)ResourceUtil.ActionIcon.EXPORT_DICOM), event -> {
            if (GuiUtils.getUICore().getSystemPreferences().getBooleanProperty("weasis.export.dicom", true)) {
                ImportToolBar.showAction(parent, model, Messages.getString("LocalExport.local_dev"), true);
            } else {
                JOptionPane.showMessageDialog((Component)event.getSource(), Messages.getString("DicomExplorer.export_perm"));
            }
        });
    }
}

