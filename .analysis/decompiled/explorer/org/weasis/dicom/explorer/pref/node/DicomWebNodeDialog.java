/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.miginfocom.swing.MigLayout
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.gui.util.WinUtil
 *  org.weasis.core.api.net.auth.AuthMethod
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.explorer.pref.node;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.LayoutManager;
import java.awt.Window;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.WinUtil;
import org.weasis.core.api.net.auth.AuthMethod;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.pref.node.AbstractDicomNode;
import org.weasis.dicom.explorer.pref.node.AuthenticationEditor;
import org.weasis.dicom.explorer.pref.node.AuthenticationPersistence;
import org.weasis.dicom.explorer.pref.node.DicomWebNode;
import org.weasis.dicom.explorer.pref.node.HttpHeadersEditor;

public class DicomWebNodeDialog
extends JDialog {
    private JTextField urlTf;
    private JTextField descriptionTf;
    private final DicomWebNode dicomNode;
    private final JComboBox<DicomWebNode> nodesComboBox;
    private JComboBox<DicomWebNode.WebType> comboBox;
    private final JComboBox<AuthMethod> comboBoxAuth = new JComboBox();

    public DicomWebNodeDialog(Window parent, String title, DicomWebNode dicomNode, JComboBox<DicomWebNode> nodeComboBox) {
        super(parent, title, Dialog.ModalityType.APPLICATION_MODAL);
        this.initComponents();
        this.nodesComboBox = nodeComboBox;
        if (dicomNode == null) {
            this.dicomNode = new DicomWebNode("", (DicomWebNode.WebType)((Object)this.comboBox.getSelectedItem()), null, null);
            this.nodesComboBox.addItem(this.dicomNode);
            this.nodesComboBox.setSelectedItem(this.dicomNode);
        } else {
            this.dicomNode = dicomNode;
            this.descriptionTf.setText(dicomNode.getDescription());
            this.urlTf.setText(dicomNode.getUrl().toString());
            this.comboBox.setSelectedItem((Object)dicomNode.getWebType());
            this.comboBoxAuth.setSelectedItem(dicomNode.getAuthMethod());
        }
        this.pack();
    }

    private void initComponents() {
        this.setDefaultCloseOperation(2);
        JPanel panel = new JPanel();
        panel.setLayout((LayoutManager)new MigLayout("insets 10lp 15lp 10lp 15lp", "[right]rel[left][grow,fill]", "[]10[]"));
        JLabel descriptionLabel = new JLabel(Messages.getString("PrinterDialog.desc") + ":");
        this.descriptionTf = new JTextField();
        this.descriptionTf.setColumns(20);
        panel.add((Component)descriptionLabel, "newline");
        panel.add(this.descriptionTf);
        JLabel lblType = new JLabel(Messages.getString("DicomNodeDialog.lblType.text") + ":");
        this.comboBox = new JComboBox<DicomWebNode.WebType>(new DefaultComboBoxModel<DicomWebNode.WebType>(DicomWebNode.WebType.values()));
        panel.add((Component)lblType, "newline");
        panel.add(this.comboBox);
        JLabel urlLabel = new JLabel("URL:");
        this.urlTf = new JTextField(50);
        panel.add((Component)urlLabel, "newline");
        panel.add((Component)this.urlTf, "growx, spanx 3, alignx leading");
        JLabel lblAuth = new JLabel(Messages.getString("authentication") + ":");
        AuthenticationPersistence.loadMethods(this.comboBoxAuth);
        this.comboBoxAuth.setSelectedIndex(0);
        JButton btnAuth = new JButton(Messages.getString("manager"));
        btnAuth.addActionListener(e -> this.manageAuth());
        panel.add((Component)lblAuth, "newline");
        panel.add(this.comboBoxAuth);
        panel.add((Component)btnAuth, "growx 0");
        JLabel headersLabel = new JLabel(Messages.getString("http.optional") + ":");
        JButton btnHttpHeaders = new JButton(Messages.getString("DicomWebNodeDialog.httpHeaders"));
        btnHttpHeaders.addActionListener(e -> this.manageHeader());
        panel.add((Component)headersLabel, "newline");
        panel.add(btnHttpHeaders);
        JButton okButton = new JButton(Messages.getString("PrinterDialog.ok"));
        okButton.addActionListener(e -> this.okButtonActionPerformed());
        JButton cancelButton = new JButton(Messages.getString("PrinterDialog.cancel"));
        cancelButton.addActionListener(e -> this.dispose());
        JButton helpButton = GuiUtils.createHelpButton((String)"dicomweb-config");
        panel.add((Component)GuiUtils.getFlowLayoutPanel((int)4, (int)0, (int)0, (JComponent[])new JComponent[]{helpButton, GuiUtils.boxHorizontalStrut((int)15), okButton, GuiUtils.boxHorizontalStrut((int)15), cancelButton}), "newline, skip 3, gap 15lp 0lp 10lp 10lp, alignx trailing");
        this.setContentPane(panel);
    }

    private void manageAuth() {
        AuthenticationEditor dialog = new AuthenticationEditor((Window)this, this.comboBoxAuth);
        GuiUtils.showCenterScreen((Window)dialog);
    }

    private void manageHeader() {
        HttpHeadersEditor dialog = new HttpHeadersEditor((Window)this, this.dicomNode);
        GuiUtils.showCenterScreen((Window)dialog);
    }

    private void okButtonActionPerformed() {
        URL validUrl;
        String desc = this.descriptionTf.getText();
        String url = this.urlTf.getText();
        DicomWebNode.WebType webType = (DicomWebNode.WebType)((Object)this.comboBox.getSelectedItem());
        if (!StringUtil.hasText((CharSequence)desc) || !StringUtil.hasText((CharSequence)url)) {
            JOptionPane.showMessageDialog(WinUtil.getValidComponent((Component)this), Messages.getString("PrinterDialog.fill_message"), Messages.getString("PrinterDialog.error"), 0);
            return;
        }
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        try {
            validUrl = new URL(url);
        }
        catch (MalformedURLException e) {
            JOptionPane.showMessageDialog(WinUtil.getValidComponent((Component)this), Messages.getString("this.url.is.not.valid"), Messages.getString("PrinterDialog.error"), 0);
            return;
        }
        AbstractDicomNode.UsageType usageType = DicomWebNode.getUsageType(webType);
        this.dicomNode.setDescription(desc);
        this.dicomNode.setWebType(webType);
        this.dicomNode.setUrl(validUrl);
        this.dicomNode.setUsageType(usageType);
        this.dicomNode.setAuthMethodUid(((AuthMethod)this.comboBoxAuth.getSelectedItem()).getUid());
        this.nodesComboBox.repaint();
        AbstractDicomNode.saveDicomNodes(this.nodesComboBox, AbstractDicomNode.Type.WEB);
        this.dispose();
    }
}

