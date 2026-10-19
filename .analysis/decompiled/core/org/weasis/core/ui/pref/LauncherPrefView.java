/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.pref;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.ui.launcher.Launcher;

public class LauncherPrefView
extends AbstractItemDialogPage {
    public static final String PAGE_NAME = Messages.getString("launcher");

    public LauncherPrefView() {
        super(PAGE_NAME, 120);
        this.initGUI();
    }

    private void initGUI() {
        this.buildPanel(Launcher.Type.DICOM);
        this.buildPanel(Launcher.Type.OTHER);
        this.add(GuiUtils.boxYLastElement(5));
        this.getProperties().setProperty("help.item", "launcher-external");
    }

    private void buildPanel(Launcher.Type type) {
        JComboBox<Launcher> nodeComboBox = new JComboBox<Launcher>();
        Launcher.loadLaunchers(nodeComboBox, type);
        Launcher.addTooltipToComboList(nodeComboBox);
        GuiUtils.setPreferredWidth(nodeComboBox, 270, 150);
        JButton editButton = new JButton(Messages.getString("edit"));
        JButton deleteButton = new JButton(Messages.getString("delete"));
        JButton addNodeButton = new JButton(Messages.getString("add.new"));
        deleteButton.addActionListener(actionEvent -> Launcher.deleteNodeActionPerformed(nodeComboBox, type));
        editButton.addActionListener(actionEvent -> Launcher.editNodeActionPerformed(nodeComboBox, type));
        addNodeButton.addActionListener(actionEvent -> Launcher.addNodeActionPerformed(nodeComboBox, type));
        this.add(LauncherPrefView.buildItem(type.toString(), nodeComboBox, editButton, deleteButton, addNodeButton));
        this.add(GuiUtils.boxVerticalStrut(15));
    }

    public static JPanel buildItem(String title, JComboBox<?> nodeComboBox, JButton editButton, JButton deleteButton, JButton addNodeButton) {
        JPanel panel = GuiUtils.getVerticalBoxLayoutPanel(new JComponent[0]);
        panel.setBorder(GuiUtils.getTitledBorder(title));
        panel.add(GuiUtils.getFlowLayoutPanel(5, 5, nodeComboBox, GuiUtils.boxHorizontalStrut(15), editButton, GuiUtils.boxHorizontalStrut(10), deleteButton));
        panel.add(GuiUtils.getFlowLayoutPanel(5, 5, addNodeButton));
        return panel;
    }

    @Override
    public void closeAdditionalWindow() {
    }

    @Override
    public void resetToDefaultValues() {
    }
}

