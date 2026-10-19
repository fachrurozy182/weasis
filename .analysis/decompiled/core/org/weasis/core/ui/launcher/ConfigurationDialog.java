/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.miginfocom.swing.MigLayout
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.ui.launcher;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.LayoutManager;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.ui.launcher.Launcher;
import org.weasis.core.util.StringUtil;

public class ConfigurationDialog
extends JDialog {
    private final JTextField uriField = new JTextField(25);
    private final JTextField binaryPathField = new JTextField(25);
    private final JTextField workingDirectoryField = new JTextField(25);
    private final JTextArea parametersArea = new JTextArea(3, 25);
    private final JTextArea environmentVariablesArea = new JTextArea(3, 25);
    private final JComboBox<Launcher.Compatibility> compatibilityField = new JComboBox<Launcher.Compatibility>(Launcher.Compatibility.values());
    private final JRadioButton uriRadioButton = new JRadioButton(Messages.getString("uri"));
    private final JRadioButton appRadioButton = new JRadioButton(Messages.getString("application"));
    private final JPanel cardPanel;
    private final Launcher launcher;

    public ConfigurationDialog(Window parent, Launcher launcher, Launcher.Type type) {
        super(parent, Messages.getString("launcher.type"), Dialog.ModalityType.APPLICATION_MODAL);
        this.setLayout((LayoutManager)new MigLayout("insets 10lp 15lp 10lp 15lp", "[][grow]", "[][]"));
        ButtonGroup group = new ButtonGroup();
        group.add(this.uriRadioButton);
        group.add(this.appRadioButton);
        this.uriRadioButton.setSelected(true);
        this.launcher = launcher;
        this.initialize(launcher);
        this.add((Component)this.uriRadioButton, "cell 0 0");
        this.add((Component)this.appRadioButton, "cell 1 0");
        this.cardPanel = new JPanel(new CardLayout());
        this.cardPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString("configuration")));
        JPanel uriPanel = new JPanel((LayoutManager)new MigLayout("insets 10", "[][grow]", "[]"));
        uriPanel.add((Component)new JLabel(Messages.getString("uri") + ":"), "cell 0 0");
        uriPanel.add((Component)this.uriField, "cell 1 0, growx");
        JPanel appPanel = new JPanel((LayoutManager)new MigLayout("insets 10", "[][grow]", "[][][][][]"));
        appPanel.add((Component)new JLabel(Messages.getString("binary.path") + ":"), "cell 0 0");
        appPanel.add((Component)this.binaryPathField, "cell 1 0, growx");
        appPanel.add((Component)new JLabel(Messages.getString("working.dir") + ":"), "cell 0 1");
        appPanel.add((Component)this.workingDirectoryField, "cell 1 1, growx");
        appPanel.add((Component)new JLabel(Messages.getString("parameters") + ":"), "cell 0 2");
        appPanel.add((Component)new JScrollPane(this.parametersArea), "cell 1 2, growx");
        appPanel.add((Component)new JLabel(Messages.getString("env.variables") + ":"), "cell 0 3");
        appPanel.add((Component)new JScrollPane(this.environmentVariablesArea), "cell 1 3, growx");
        appPanel.add((Component)new JLabel(Messages.getString("compatibility") + ":"), "cell 0 4");
        appPanel.add(this.compatibilityField, "cell 1 4, growx");
        this.cardPanel.add((Component)uriPanel, this.uriRadioButton.getText());
        this.cardPanel.add((Component)appPanel, this.appRadioButton.getText());
        this.add((Component)this.cardPanel, "cell 0 1 2 1, grow");
        this.uriRadioButton.addActionListener(actionEvent -> this.showCard());
        this.appRadioButton.addActionListener(actionEvent -> this.showCard());
        JButton saveButton = new JButton(Messages.getString("save"));
        saveButton.addActionListener(actionEvent -> this.saveConfiguration());
        JButton jButtonHelp = GuiUtils.createHelpButton("launcher-external");
        this.add((Component)jButtonHelp, "cell 1 3, split 3, align right, gapright 15, gaptop 15");
        this.add((Component)saveButton, "align right, gaptop 15");
        JButton cancelButton = new JButton(Messages.getString("cancel"));
        cancelButton.addActionListener(actionEvent -> this.dispose());
        this.add(cancelButton);
        this.showCard();
        this.pack();
        this.setLocationRelativeTo(parent);
    }

    private void initialize(Launcher launcher) {
        if (launcher != null) {
            Launcher.Configuration config = launcher.getConfiguration();
            if (config instanceof Launcher.URIConfiguration) {
                Launcher.URIConfiguration uriConfig = (Launcher.URIConfiguration)config;
                this.uriRadioButton.setSelected(true);
                this.uriField.setText(uriConfig.getUri());
            } else if (config instanceof Launcher.ApplicationConfiguration) {
                Launcher.ApplicationConfiguration appConfig = (Launcher.ApplicationConfiguration)config;
                this.appRadioButton.setSelected(true);
                this.binaryPathField.setText(appConfig.getBinaryPath());
                this.workingDirectoryField.setText(appConfig.getWorkingDirectory());
                this.parametersArea.setText(String.join((CharSequence)"\n", appConfig.getParameters()));
                this.environmentVariablesArea.setText(appConfig.getEnvironmentVariables().entrySet().stream().map(e -> (String)e.getKey() + "=" + (String)e.getValue()).reduce((a, b) -> a + "\n" + b).orElse(""));
                this.compatibilityField.setSelectedItem((Object)appConfig.getCompatibility());
            }
        }
    }

    private void showCard() {
        CardLayout cl = (CardLayout)this.cardPanel.getLayout();
        if (this.appRadioButton.isSelected()) {
            cl.show(this.cardPanel, this.appRadioButton.getText());
        } else {
            cl.show(this.cardPanel, this.uriRadioButton.getText());
        }
    }

    private void saveConfiguration() {
        Launcher.Configuration config = this.launcher.getConfiguration();
        config = this.appRadioButton.isSelected() ? this.setApplicationConfiguration(config) : this.setUriConfiguration(config);
        this.launcher.setConfiguration(config);
        if (!config.isValid()) {
            config.showInvalidField(this);
            return;
        }
        this.dispose();
    }

    private Launcher.ApplicationConfiguration setApplicationConfiguration(Launcher.Configuration config) {
        Launcher.ApplicationConfiguration appConfig = config instanceof Launcher.ApplicationConfiguration ? (Launcher.ApplicationConfiguration)config : new Launcher.ApplicationConfiguration();
        appConfig.setBinaryPath(this.binaryPathField.getText());
        appConfig.setWorkingDirectory(this.workingDirectoryField.getText());
        appConfig.setParameters(new ArrayList<String>(Arrays.stream(this.parametersArea.getText().split("\\n")).filter(StringUtil::hasText).toList()));
        appConfig.setEnvironmentVariables(this.parseEnvironmentVariables(this.environmentVariablesArea.getText()));
        appConfig.setCompatibility((Launcher.Compatibility)((Object)this.compatibilityField.getSelectedItem()));
        return appConfig;
    }

    private Map<String, String> parseEnvironmentVariables(String text) {
        HashMap<String, String> env = new HashMap<String, String>();
        for (String line : text.split("\\n")) {
            String[] parts = line.split("=");
            if (parts.length != 2) continue;
            env.put(parts[0], parts[1]);
        }
        return env;
    }

    private Launcher.URIConfiguration setUriConfiguration(Launcher.Configuration config) {
        Launcher.URIConfiguration uriConfig = config instanceof Launcher.URIConfiguration ? (Launcher.URIConfiguration)config : new Launcher.URIConfiguration();
        uriConfig.setUri(this.uriField.getText());
        return uriConfig;
    }
}

