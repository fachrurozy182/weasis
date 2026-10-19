/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.miginfocom.swing.MigLayout
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.gui.util.WinUtil
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.explorer.pref.node;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.LayoutManager;
import java.awt.Window;
import java.util.Optional;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.WinUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.pref.node.AbstractDicomNode;
import org.weasis.dicom.explorer.pref.node.DefaultDicomNode;
import org.weasis.dicom.explorer.pref.node.DicomPrintNode;
import org.weasis.dicom.explorer.print.DicomPrintOptionPane;

public class DicomNodeDialog
extends JDialog {
    private JTextField aeTitleTf;
    private DicomPrintOptionPane printOptionsPane;
    private JTextField descriptionTf;
    private JTextField hostnameTf;
    private JFormattedTextField portTf;
    private DefaultDicomNode dicomNode;
    private final JComboBox<DefaultDicomNode> nodesComboBox;
    private final AbstractDicomNode.Type typeNode;
    private JComboBox<AbstractDicomNode.UsageType> comboBox;

    public DicomNodeDialog(Window parent, String title, DefaultDicomNode dicomNode, JComboBox<DefaultDicomNode> nodeComboBox, AbstractDicomNode.Type typeNode) {
        super(parent, title, Dialog.ModalityType.APPLICATION_MODAL);
        this.typeNode = dicomNode == null ? (typeNode == null ? AbstractDicomNode.Type.DICOM : typeNode) : dicomNode.getType();
        this.portTf = new JFormattedTextField();
        this.initComponents();
        this.dicomNode = dicomNode;
        this.nodesComboBox = nodeComboBox;
        if (dicomNode != null) {
            this.descriptionTf.setText(dicomNode.getDescription());
            this.aeTitleTf.setText(dicomNode.getAeTitle());
            this.hostnameTf.setText(dicomNode.getHostname());
            this.portTf.setValue(dicomNode.getPort());
            if (dicomNode instanceof DicomPrintNode) {
                DicomPrintNode printNode = (DicomPrintNode)dicomNode;
                this.printOptionsPane.applyOptions(printNode.getPrintOptions());
            } else {
                this.comboBox.setSelectedItem((Object)dicomNode.getUsageType());
            }
        }
        this.pack();
    }

    private void initComponents() {
        this.setDefaultCloseOperation(2);
        JPanel panel = new JPanel();
        panel.setLayout((LayoutManager)new MigLayout("insets 10lp 15lp 10lp 15lp", "[right]rel[grow,fill]"));
        JLabel descriptionLabel = new JLabel(Messages.getString("PrinterDialog.desc") + ":");
        this.descriptionTf = new JTextField();
        this.descriptionTf.setColumns(20);
        panel.add((Component)descriptionLabel, "newline");
        panel.add(this.descriptionTf);
        JLabel aeTitleLabel = new JLabel(Messages.getString("PrinterDialog.aet") + ":");
        this.aeTitleTf = new JTextField();
        this.aeTitleTf.setColumns(15);
        panel.add((Component)aeTitleLabel, "newline");
        panel.add(this.aeTitleTf);
        JLabel hostnameLabel = new JLabel(Messages.getString("PrinterDialog.host") + ":");
        this.hostnameTf = new JTextField();
        this.hostnameTf.setColumns(15);
        panel.add((Component)hostnameLabel, "newline");
        panel.add(this.hostnameTf);
        JLabel portLabel = new JLabel(Messages.getString("PrinterDialog.port") + ":");
        GuiUtils.formatPortTextField((JFormattedTextField)this.portTf);
        GuiUtils.setPreferredWidth((Component)this.portTf, (int)60);
        panel.add((Component)portLabel, "newline");
        panel.add((Component)this.portTf, "grow 0");
        if (this.typeNode == AbstractDicomNode.Type.PRINTER) {
            this.printOptionsPane = new DicomPrintOptionPane();
            panel.add((Component)this.printOptionsPane, "newline, gaptop 10, spanx");
        } else {
            this.comboBox = new JComboBox<AbstractDicomNode.UsageType>(new DefaultComboBoxModel<AbstractDicomNode.UsageType>(AbstractDicomNode.UsageType.values()));
            this.comboBox.setSelectedItem((Object)AbstractDicomNode.UsageType.RETRIEVE);
            if (this.typeNode == AbstractDicomNode.Type.DICOM_CALLING) {
                this.portTf.setValue(11113);
                this.hostnameTf.setText("localhost");
            }
            panel.add((Component)new JLabel(Messages.getString("usage.type") + ":"), "newline");
            panel.add(this.comboBox, "grow 0");
        }
        JButton okButton = new JButton(Messages.getString("PrinterDialog.ok"));
        okButton.addActionListener(e -> this.okButtonActionPerformed());
        JButton cancelButton = new JButton(Messages.getString("PrinterDialog.cancel"));
        cancelButton.addActionListener(e -> this.dispose());
        JButton helpButton = GuiUtils.createHelpButton((String)(this.typeNode == AbstractDicomNode.Type.PRINTER ? "print/#dicom-print" : "dicom-import/#dicom-queryretrieve"));
        panel.add((Component)GuiUtils.getFlowLayoutPanel((int)4, (int)0, (int)5, (JComponent[])new JComponent[]{helpButton, GuiUtils.boxHorizontalStrut((int)20), okButton, GuiUtils.boxHorizontalStrut((int)20), cancelButton}), "newline, spanx, gaptop 10lp");
        this.setContentPane(panel);
    }

    private void okButtonActionPerformed() {
        boolean addNode;
        String desc = this.descriptionTf.getText();
        String aeTitle = this.aeTitleTf.getText();
        String hostname = this.hostnameTf.getText();
        Number port = GuiUtils.getFormattedValue((JFormattedTextField)this.portTf);
        if (!(StringUtil.hasText((CharSequence)desc) && StringUtil.hasText((CharSequence)aeTitle) && StringUtil.hasText((CharSequence)hostname) && port != null)) {
            JOptionPane.showMessageDialog(WinUtil.getValidComponent((Component)this), Messages.getString("PrinterDialog.fill_message"), Messages.getString("PrinterDialog.error"), 0);
            return;
        }
        if (aeTitle.length() > 16) {
            JOptionPane.showMessageDialog(WinUtil.getValidComponent((Component)this), Messages.getString("DicomNodeDialog.long_aet_msg"), Messages.getString("PrinterDialog.error"), 0);
            return;
        }
        AbstractDicomNode.UsageType usageType = Optional.ofNullable(this.comboBox).map(c -> (AbstractDicomNode.UsageType)((Object)((Object)c.getSelectedItem()))).orElse(null);
        boolean bl = addNode = this.dicomNode == null;
        if (addNode) {
            this.dicomNode = AbstractDicomNode.Type.PRINTER == this.typeNode ? new DicomPrintNode(desc, aeTitle, hostname, port.intValue()) : new DefaultDicomNode(desc, aeTitle, hostname, port.intValue(), usageType);
            this.nodesComboBox.addItem(this.dicomNode);
        } else {
            this.dicomNode.setDescription(desc);
            this.dicomNode.setAeTitle(aeTitle);
            this.dicomNode.setHostname(hostname);
            this.dicomNode.setPort(port.intValue());
            this.dicomNode.setUsageType(usageType);
        }
        this.dicomNode.setType(this.typeNode);
        DefaultDicomNode defaultDicomNode = this.dicomNode;
        if (defaultDicomNode instanceof DicomPrintNode) {
            DicomPrintNode printNode = (DicomPrintNode)defaultDicomNode;
            this.printOptionsPane.saveOptions(printNode.getPrintOptions());
        }
        if (addNode) {
            this.nodesComboBox.setSelectedItem(this.dicomNode);
        } else {
            this.nodesComboBox.repaint();
        }
        AbstractDicomNode.saveDicomNodes(this.nodesComboBox, this.typeNode);
        this.dispose();
    }
}

