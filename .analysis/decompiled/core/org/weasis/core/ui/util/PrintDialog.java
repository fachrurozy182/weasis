/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.util;

import java.awt.Dialog;
import java.awt.Window;
import java.util.List;
import java.util.Objects;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.WinUtil;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.util.ExportLayout;
import org.weasis.core.ui.util.ImagePrint;
import org.weasis.core.ui.util.PrintOptions;

public class PrintDialog<I extends ImageElement>
extends JDialog {
    private final JCheckBox annotationsCheckBox = new JCheckBox(Messages.getString("PrintDialog.annotate"));
    private final JComboBox<String> positionComboBox = new JComboBox();
    private final JCheckBox selectedViewCheckbox = new JCheckBox(Messages.getString("PrintDialog.selected_view"));
    private final JComboBox<PrintOptions.DotPerInches> comboBoxDPI = new JComboBox();
    private final ImageViewerEventManager<I> eventManager;

    public PrintDialog(Window parent, String title, ImageViewerEventManager<I> eventManager) {
        super(parent, title, Dialog.ModalityType.APPLICATION_MODAL);
        this.eventManager = eventManager;
        boolean layout = eventManager.getSelectedView2dContainer().getLayoutModel().getCellCount() > 1;
        this.initComponents(layout);
        this.pack();
    }

    private void initComponents(boolean layout) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, 1));
        panel.setBorder(GuiUtils.getEmptyBorder(10, 20, 10, 20));
        JLabel positionLabel = new JLabel(Messages.getString("PrintDialog.pos") + ":");
        this.positionComboBox.setModel(new DefaultComboBoxModel<String>(new String[]{Messages.getString("PrintDialog.center"), Messages.getString("PrintDialog.top")}));
        panel.add(GuiUtils.getFlowLayoutPanel(2, 5, positionLabel, this.positionComboBox));
        JLabel label = new JLabel(Messages.getString("PrintDialog.dpi") + ":");
        this.comboBoxDPI.setModel(new DefaultComboBoxModel<PrintOptions.DotPerInches>(PrintOptions.DotPerInches.values()));
        this.comboBoxDPI.setSelectedItem((Object)PrintOptions.DotPerInches.DPI_150);
        panel.add(GuiUtils.getFlowLayoutPanel(2, 5, label, this.comboBoxDPI));
        this.annotationsCheckBox.setSelected(true);
        panel.add(GuiUtils.getFlowLayoutPanel(2, 5, this.annotationsCheckBox));
        if (layout) {
            panel.add(GuiUtils.getFlowLayoutPanel(2, 5, this.selectedViewCheckbox));
        }
        JButton cancelButton = new JButton(Messages.getString("PrintDialog.cancel"));
        cancelButton.addActionListener(e -> this.dispose());
        JButton printButton = new JButton(Messages.getString("PrintDialog.print"));
        printButton.addActionListener(e -> this.printAction());
        this.getRootPane().setDefaultButton(printButton);
        panel.add(GuiUtils.boxYLastElement(15));
        panel.add(GuiUtils.getFlowLayoutPanel(4, 2, 5, printButton, GuiUtils.boxHorizontalStrut(20), cancelButton));
        this.setContentPane(panel);
        this.pack();
    }

    private void printAction() {
        List<Object> views;
        PrintOptions printOptions = new PrintOptions();
        printOptions.setShowingAnnotations(this.annotationsCheckBox.isSelected());
        printOptions.setDpi((PrintOptions.DotPerInches)((Object)this.comboBoxDPI.getSelectedItem()));
        printOptions.setCenter(Objects.equals(this.positionComboBox.getSelectedItem(), Messages.getString("PrintDialog.center")));
        ImageViewerPlugin<I> container = this.eventManager.getSelectedView2dContainer();
        ViewCanvas<I> selectedView = this.eventManager.getSelectedViewPane();
        boolean singleView = this.selectedViewCheckbox.isSelected();
        List<Object> list = views = container == null ? List.of() : container.getImagePanels();
        if (views.isEmpty() || singleView && selectedView == null) {
            JOptionPane.showMessageDialog(WinUtil.getValidComponent(this), Messages.getString("PrintDialog.no_print"), null, 0);
            this.dispose();
            return;
        }
        this.dispose();
        ExportLayout<I> layout = singleView ? new ExportLayout<I>(selectedView) : new ExportLayout<I>(container);
        ImagePrint print = new ImagePrint(layout, printOptions);
        print.print();
        layout.dispose();
    }
}

