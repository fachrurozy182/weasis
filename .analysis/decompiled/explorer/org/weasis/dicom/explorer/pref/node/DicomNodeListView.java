/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.gui.util.AbstractItemDialogPage
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.ui.pref.LauncherPrefView
 */
package org.weasis.dicom.explorer.pref.node;

import javax.swing.JButton;
import javax.swing.JComboBox;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.ui.pref.LauncherPrefView;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.pref.node.AbstractDicomNode;

public class DicomNodeListView
extends AbstractItemDialogPage {
    public DicomNodeListView() {
        super(Messages.getString("DicomNodeListView.node_list"), 605);
        this.initGUI();
    }

    private void initGUI() {
        this.buildPanel(AbstractDicomNode.Type.DICOM_CALLING);
        this.buildPanel(AbstractDicomNode.Type.DICOM);
        this.buildPanel(AbstractDicomNode.Type.WEB);
        this.add(GuiUtils.boxYLastElement((int)5));
    }

    private void buildPanel(AbstractDicomNode.Type nodeType) {
        JComboBox<AbstractDicomNode> nodeComboBox = new JComboBox<AbstractDicomNode>();
        AbstractDicomNode.loadDicomNodes(nodeComboBox, nodeType);
        AbstractDicomNode.addTooltipToComboList(nodeComboBox);
        GuiUtils.setPreferredWidth(nodeComboBox, (int)270, (int)150);
        JButton editButton = new JButton(Messages.getString("DicomNodeListView.edit"));
        JButton deleteButton = new JButton(Messages.getString("DicomNodeListView.delete"));
        JButton addNodeButton = new JButton(Messages.getString("DicomNodeListView.add_new"));
        deleteButton.addActionListener(actionEvent -> AbstractDicomNode.deleteNodeActionPerformed(nodeComboBox));
        editButton.addActionListener(actionEvent -> AbstractDicomNode.editNodeActionPerformed(nodeComboBox));
        addNodeButton.addActionListener(e -> AbstractDicomNode.addNodeActionPerformed(nodeComboBox, nodeType));
        this.add(LauncherPrefView.buildItem((String)nodeType.toString(), nodeComboBox, (JButton)editButton, (JButton)deleteButton, (JButton)addNodeButton));
        this.add(GuiUtils.boxVerticalStrut((int)15));
    }

    public void closeAdditionalWindow() {
    }

    public void resetToDefaultValues() {
    }
}

