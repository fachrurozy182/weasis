/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.miginfocom.swing.MigLayout
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.gui.util.WinUtil
 *  org.weasis.core.api.media.data.ImageElement
 *  org.weasis.core.api.service.WProperties
 *  org.weasis.core.ui.editor.image.ImageViewerEventManager
 *  org.weasis.core.ui.editor.image.ImageViewerPlugin
 *  org.weasis.core.ui.editor.image.ViewCanvas
 *  org.weasis.core.ui.util.ExportLayout
 *  org.weasis.core.ui.util.PrintOptions$DotPerInches
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.explorer.print;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.WinUtil;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.service.WProperties;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.util.ExportLayout;
import org.weasis.core.ui.util.PrintOptions;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.pref.node.AbstractDicomNode;
import org.weasis.dicom.explorer.pref.node.DefaultDicomNode;
import org.weasis.dicom.explorer.pref.node.DicomNodeDialog;
import org.weasis.dicom.explorer.pref.node.DicomPrintNode;
import org.weasis.dicom.explorer.print.DicomPrint;
import org.weasis.dicom.explorer.print.DicomPrintOptionPane;
import org.weasis.dicom.explorer.print.DicomPrintOptions;

public class DicomPrintDialog<I extends ImageElement>
extends JDialog {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomPrintDialog.class);
    private static final String LAST_SEL_NODE = "lastSelNode";
    private static final String LAST_CALLING_NODE = "lastCallingNode";
    private DicomPrintOptionPane optionPane;
    private JComboBox<AbstractDicomNode> printersComboBox;
    private final JComboBox<AbstractDicomNode> comboCallingNode = new JComboBox();
    private final ImageViewerEventManager<I> eventManager;

    public DicomPrintDialog(Window parent, String title, ImageViewerEventManager<I> eventManager) {
        super(parent, title, Dialog.ModalityType.APPLICATION_MODAL);
        this.setDefaultCloseOperation(2);
        this.eventManager = eventManager;
        this.initComponents();
        this.applyOptionsFromSelected();
        this.pack();
    }

    private void initComponents() {
        JPanel panel = new JPanel();
        panel.setLayout((LayoutManager)new MigLayout("insets 10lp 15lp 10lp 15lp", "[grow ,fill][grow 0]"));
        JPanel printersCfg = GuiUtils.getFlowLayoutPanel((int)3, (int)5, (int)5, (JComponent[])new JComponent[0]);
        printersCfg.setBorder(GuiUtils.getTitledBorder((String)Messages.getString("DicomPrintDialog.print_title")));
        JLabel printerLabel = new JLabel(Messages.getString("DicomPrintDialog.printer") + ":");
        printersCfg.add(printerLabel);
        this.printersComboBox = new JComboBox();
        printersCfg.add(this.printersComboBox);
        this.printersComboBox.setModel(new DefaultComboBoxModel());
        AbstractDicomNode.loadDicomNodes(this.printersComboBox, AbstractDicomNode.Type.PRINTER);
        GuiUtils.setPreferredWidth(this.printersComboBox, (int)200, (int)185);
        AbstractDicomNode.addTooltipToComboList(this.printersComboBox);
        AbstractDicomNode.restoreNodeSelection(DicomPrintDialog.getPersistence(), this.printersComboBox.getModel(), LAST_SEL_NODE);
        Component horizontalStrut = Box.createHorizontalStrut(20);
        printersCfg.add(horizontalStrut);
        JButton addPrinterButton = new JButton(Messages.getString("DicomPrintDialog.add"));
        printersCfg.add(addPrinterButton);
        JButton editButton = new JButton(Messages.getString("DicomPrintDialog.edit"));
        printersCfg.add(editButton);
        JButton deleteButton = new JButton(Messages.getString("DicomPrintDialog.delete"));
        printersCfg.add(deleteButton);
        deleteButton.addActionListener(evt -> {
            AbstractDicomNode.deleteNodeActionPerformed(this.printersComboBox);
            this.applyOptionsFromSelected();
        });
        editButton.addActionListener(evt -> {
            AbstractDicomNode.editNodeActionPerformed(this.printersComboBox);
            this.applyOptionsFromSelected();
        });
        addPrinterButton.addActionListener(evt -> {
            AbstractDicomNode.addNodeActionPerformed(this.printersComboBox, AbstractDicomNode.Type.PRINTER);
            this.applyOptionsFromSelected();
        });
        this.printersComboBox.addActionListener(evt -> this.applyOptionsFromSelected());
        panel.add((Component)printersCfg, "newline, spanx");
        this.optionPane = new DicomPrintOptionPane();
        panel.add((Component)this.optionPane, "newline, gaptop 10, spanx");
        JLabel lblCalling = new JLabel(Messages.getString("calling.node") + ":");
        AbstractDicomNode.loadDicomNodes(this.comboCallingNode, AbstractDicomNode.Type.DICOM_CALLING, AbstractDicomNode.UsageType.STORAGE);
        GuiUtils.setPreferredWidth(this.comboCallingNode, (int)185, (int)150);
        AbstractDicomNode.addTooltipToComboList(this.comboCallingNode);
        AbstractDicomNode.restoreNodeSelection(DicomPrintDialog.getPersistence(), this.comboCallingNode.getModel(), LAST_CALLING_NODE);
        panel.add((Component)lblCalling, "newline, growx 0, alignx trailing");
        panel.add(this.comboCallingNode, "growx, alignx trailing, gapright 25");
        JButton printButton = new JButton(Messages.getString("DicomPrintDialog.print"));
        printButton.addActionListener(this::printButtonActionPerformed);
        this.getRootPane().setDefaultButton(printButton);
        JButton cancelButton = new JButton(Messages.getString("DicomPrintDialog.cancel"));
        cancelButton.addActionListener(evt -> this.doClose());
        JButton helpButton = GuiUtils.createHelpButton((String)"print/#dicom-print");
        panel.add((Component)helpButton, "newline, growx 0, alignx leading");
        panel.add((Component)printButton, "growx 0, alignx trailing, split 2");
        panel.add((Component)cancelButton, "gap 15lp 0lp 10lp 10lp");
        this.setContentPane(panel);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void printButtonActionPerformed(ActionEvent evt) {
        List views;
        DicomPrintNode node = (DicomPrintNode)this.printersComboBox.getSelectedItem();
        if (node == null) {
            return;
        }
        DicomPrintOptions options = new DicomPrintOptions();
        this.optionPane.saveOptions(options);
        DicomPrint dicomPrint = new DicomPrint(node, options);
        ImageViewerPlugin container = this.eventManager.getSelectedView2dContainer();
        ViewCanvas selectedView = this.eventManager.getSelectedViewPane();
        boolean singleView = this.optionPane.checkboxSelectedView.isSelected();
        List list = views = container == null ? List.of() : container.getImagePanels();
        if (views.isEmpty() || singleView && selectedView == null) {
            JOptionPane.showMessageDialog(WinUtil.getValidComponent((Component)this), Messages.getString("DicomPrintDialog.no_print"), null, 0);
            this.doClose();
            return;
        }
        this.doClose();
        ExportLayout layout = singleView ? new ExportLayout(selectedView) : new ExportLayout(container);
        try {
            dicomPrint.printImage(dicomPrint.printImage((ExportLayout<? extends ImageElement>)layout), (DefaultDicomNode)this.comboCallingNode.getSelectedItem());
        }
        catch (Exception e) {
            LOGGER.error("DICOM Print Service", (Throwable)e);
            JOptionPane.showMessageDialog(WinUtil.getValidComponent((Component)this), Messages.getString("DicomPrintDialog.error_print"), Messages.getString("DicomPrintDialog.error"), 0);
        }
        finally {
            layout.dispose();
        }
    }

    private void doClose() {
        AbstractDicomNode.nodeSelectionPersistence(DicomPrintDialog.getPersistence(), (AbstractDicomNode)this.printersComboBox.getSelectedItem(), LAST_SEL_NODE);
        AbstractDicomNode.nodeSelectionPersistence(DicomPrintDialog.getPersistence(), (AbstractDicomNode)this.comboCallingNode.getSelectedItem(), LAST_CALLING_NODE);
        this.dispose();
    }

    private void applyOptionsFromSelected() {
        Object selectedItem = this.printersComboBox.getSelectedItem();
        if (selectedItem instanceof DicomPrintNode) {
            DicomPrintNode printNode = (DicomPrintNode)selectedItem;
            this.optionPane.applyOptions(printNode.getPrintOptions());
        }
    }

    public static WProperties getPersistence() {
        return GuiUtils.getUICore().getPluginPersistence(DicomNodeDialog.class.getPackageName());
    }

    public static enum FilmSize {
        IN8X10("8INX10IN", 8.0, 10.0),
        IN8_5X11("8_5INX11IN", 8.5, 11.0),
        IN10X12("10INX12IN", 10.0, 12.0),
        IN10X14("10INX14IN", 10.0, 14.0),
        IN11X14("11INX14IN", 11.0, 14.0),
        IN11X17("11INX17IN", 11.0, 17.0),
        IN14X14("14INX14IN", 14.0, 14.0),
        IN14X17("14INX17IN", 14.0, 17.0),
        CM24X24("24CMX24CM", FilmSize.convertMM2Inch(240), FilmSize.convertMM2Inch(240)),
        CM24X30("24CMX30CM", FilmSize.convertMM2Inch(240), FilmSize.convertMM2Inch(300)),
        A4("A4", FilmSize.convertMM2Inch(210), FilmSize.convertMM2Inch(297)),
        A3("A3", FilmSize.convertMM2Inch(297), FilmSize.convertMM2Inch(420));

        private final String name;
        private final double width;
        private final double height;

        private FilmSize(String name, double width, double height) {
            this.name = name;
            this.width = width;
            this.height = height;
        }

        public String toString() {
            return this.name;
        }

        public int getWidth(PrintOptions.DotPerInches dpi) {
            return FilmSize.getLengthFromInch(this.width, dpi);
        }

        public int getHeight(PrintOptions.DotPerInches dpi) {
            return FilmSize.getLengthFromInch(this.height, dpi);
        }

        public static int getLengthFromInch(double size, PrintOptions.DotPerInches dpi) {
            PrintOptions.DotPerInches dpi2 = dpi == null ? PrintOptions.DotPerInches.DPI_150 : dpi;
            double val = size * (double)dpi2.getDpi();
            return (int)(val + 0.5);
        }

        public static double convertMM2Inch(int size) {
            return (double)size / 25.4;
        }

        public static FilmSize getInstance(String val, FilmSize defaultValue) {
            if (StringUtil.hasText((CharSequence)val)) {
                try {
                    return FilmSize.valueOf(val);
                }
                catch (Exception e) {
                    LOGGER.error("Cannot find FilmSize: {}", (Object)val, (Object)e);
                }
            }
            return defaultValue;
        }
    }
}

