/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.miginfocom.swing.MigLayout
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.ui.launcher;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.LayoutManager;
import java.awt.Window;
import java.util.List;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.WinUtil;
import org.weasis.core.ui.launcher.ConfigurationDialog;
import org.weasis.core.ui.launcher.Launcher;
import org.weasis.core.util.StringUtil;

public class LauncherDialog
extends JDialog {
    private final JTextField nameField = new JTextField(25);
    private final JTextField iconNameField = new JTextField(25);
    private final JCheckBox enableCheckBox = new JCheckBox(Messages.getString("enable"), true);
    private final JCheckBox buttonCheckBox = new JCheckBox(Messages.getString("button"));
    private final JComboBox<Launcher> nodesComboBox;
    private final Launcher launcher;
    private final Launcher.Type type;

    public LauncherDialog(Window parent, Launcher.Type type, Launcher launcher, JComboBox<Launcher> nodeComboBox) {
        super(parent, type.toString(), Dialog.ModalityType.APPLICATION_MODAL);
        this.setLayout((LayoutManager)new MigLayout("insets 10lp 15lp 10lp 15lp, wrap 2", "[][grow]", "[][][][]"));
        this.type = Objects.requireNonNull(type);
        this.nodesComboBox = nodeComboBox;
        if (launcher == null) {
            this.launcher = new Launcher();
            this.launcher.setConfiguration(new Launcher.URIConfiguration());
        } else {
            this.launcher = launcher;
            this.nameField.setText(launcher.getName());
            this.iconNameField.setText(launcher.getIconPath());
            this.enableCheckBox.setSelected(launcher.isEnable());
            this.buttonCheckBox.setSelected(launcher.isButton());
        }
        this.add((Component)new JLabel(Messages.getString("name") + ":"), "align right");
        this.add((Component)this.nameField, "growx");
        this.add((Component)new JLabel(Messages.getString("icon.path") + ":"), "align right");
        this.add((Component)this.iconNameField, "growx");
        this.add((Component)this.enableCheckBox, "span, split 3");
        this.add((Component)this.buttonCheckBox, "growx");
        JButton configurationButton = new JButton(Messages.getString("configure"));
        configurationButton.addActionListener(actionEvent -> new ConfigurationDialog((Window)this, this.launcher, type).setVisible(true));
        this.add((Component)configurationButton, "align right");
        JButton saveButton = new JButton(Messages.getString("save"));
        saveButton.addActionListener(actionEvent -> this.saveLauncher());
        this.add((Component)saveButton, "cell 1 4, split 2, align right, gapright 15, gaptop 15");
        JButton cancelButton = new JButton(Messages.getString("cancel"));
        cancelButton.addActionListener(actionEvent -> this.dispose());
        this.add(cancelButton);
        this.pack();
        this.setLocationRelativeTo(parent);
    }

    private void saveLauncher() {
        String name = this.nameField.getText();
        if (!StringUtil.hasText((CharSequence)name)) {
            String fieldName = Messages.getString("name");
            LauncherDialog.ShowRequiredValue(this, fieldName);
            return;
        }
        if (!this.launcher.getConfiguration().isValid()) {
            this.launcher.getConfiguration().showInvalidField(this);
            return;
        }
        this.launcher.setName(name);
        this.launcher.setIconPath(this.iconNameField.getText());
        this.launcher.setEnable(this.enableCheckBox.isSelected());
        this.launcher.setButton(this.buttonCheckBox.isSelected());
        List<Launcher> list = Launcher.getLaunchers(this.type);
        if (!list.contains(this.launcher)) {
            list.add(this.launcher);
            this.nodesComboBox.addItem(this.launcher);
            this.nodesComboBox.setSelectedItem(this.launcher);
        }
        this.nodesComboBox.repaint();
        Launcher.saveLaunchers(this.type);
        this.dispose();
    }

    static void ShowRequiredValue(Window parent, String fieldName) {
        JOptionPane.showMessageDialog(WinUtil.getValidComponent(parent), String.format(Messages.getString("is.required"), fieldName), Messages.getString("error"), 0);
    }
}

