/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.miginfocom.swing.MigLayout
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.ui.util.PrintOptions$DotPerInches
 */
package org.weasis.dicom.explorer.print;

import java.awt.Component;
import java.awt.LayoutManager;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import net.miginfocom.swing.MigLayout;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.ui.util.PrintOptions;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.print.DicomPrintDialog;
import org.weasis.dicom.explorer.print.DicomPrintOptions;

public class DicomPrintOptionPane
extends JPanel {
    JComboBox<String> borderDensityComboBox;
    JCheckBox colorPrintCheckBox;
    JComboBox<String> filmDestinationComboBox;
    JComboBox<String> filmOrientationComboBox;
    JComboBox<DicomPrintDialog.FilmSize> filmSizeIdComboBox;
    JComboBox<String> imageDisplayFormatComboBox;
    JComboBox<String> magnificationTypeComboBox;
    JComboBox<String> mediumTypeComboBox;
    JSpinner numOfCopiesSpinner;
    JCheckBox printAnnotationsCheckBox;
    JSeparator printOptionsSeparator;
    JComboBox<String> priorityComboBox;
    JComboBox<String> smoothingTypeComboBox;
    JComboBox<String> trimComboBox;
    DefaultComboBoxModel<String> portraitDisplayFormatsModel = new DefaultComboBoxModel<String>(new String[]{"STANDARD\\1,1"});
    JCheckBox checkboxSelectedView;
    JComboBox<PrintOptions.DotPerInches> comboBoxDPI;
    JComboBox<String> comboBoxEmpty;

    public DicomPrintOptionPane() {
        this.initComponents();
    }

    private void initComponents() {
        this.setLayout((LayoutManager)new MigLayout("fillx", "[right]rel[left]15lp[right]rel[left]"));
        this.setBorder(GuiUtils.getTitledBorder((String)Messages.getString("DicomPrintDialog.option_title")));
        this.add((Component)new JLabel(Messages.getString("DicomPrintDialog.med_type") + ":"), "newline");
        this.mediumTypeComboBox = new JComboBox();
        this.mediumTypeComboBox.setModel(new DefaultComboBoxModel<String>(new String[]{"BLUE FILM", "CLEAR FILM", "MAMMO CLEAR FILM", "MAMMO BLUE FILM", "PAPER"}));
        this.add(this.mediumTypeComboBox);
        this.add(new JLabel(Messages.getString("DicomPrintDialog.priority") + ":"));
        this.priorityComboBox = new JComboBox<String>(new DefaultComboBoxModel<String>(new String[]{"LOW", "MED", "HIGH"}));
        this.add(this.priorityComboBox);
        this.add((Component)new JLabel(Messages.getString("DicomPrintDialog.film_dest") + ":"), "newline");
        this.filmDestinationComboBox = new JComboBox<String>(new DefaultComboBoxModel<String>(new String[]{"MAGAZINE", "PROCESSOR"}));
        this.add(this.filmDestinationComboBox);
        this.add(new JLabel(Messages.getString("DicomPrintDialog.copies") + ":"));
        this.numOfCopiesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        this.add(this.numOfCopiesSpinner);
        this.colorPrintCheckBox = new JCheckBox(Messages.getString("DicomPrintDialog.print_color"));
        this.add((Component)this.colorPrintCheckBox, "newline, alignx leading, spanx 2");
        this.printOptionsSeparator = new JSeparator();
        this.add((Component)this.printOptionsSeparator, "newline, spanx");
        this.add((Component)new JLabel(Messages.getString("DicomPrintDialog.film_orientation") + ":"), "newline");
        this.filmOrientationComboBox = new JComboBox<String>(new DefaultComboBoxModel<String>(new String[]{"PORTRAIT", "LANDSCAPE"}));
        this.add(this.filmOrientationComboBox);
        this.add(new JLabel(Messages.getString("DicomPrintDialog.size_id") + ":"));
        this.filmSizeIdComboBox = new JComboBox<DicomPrintDialog.FilmSize>(new DefaultComboBoxModel<DicomPrintDialog.FilmSize>(DicomPrintDialog.FilmSize.values()));
        this.add(this.filmSizeIdComboBox);
        this.add((Component)new JLabel(Messages.getString("DicomPrintDialog.disp_format") + ":"), "newline");
        this.imageDisplayFormatComboBox = new JComboBox<String>(this.portraitDisplayFormatsModel);
        this.imageDisplayFormatComboBox.setMaximumRowCount(10);
        this.add(this.imageDisplayFormatComboBox);
        this.add(new JLabel(Messages.getString("DicomPrintDialog.magn_type") + ":"));
        this.magnificationTypeComboBox = new JComboBox<String>(new DefaultComboBoxModel<String>(new String[]{"REPLICATE", "BILINEAR", "CUBIC"}));
        this.add(this.magnificationTypeComboBox);
        this.add((Component)new JLabel(Messages.getString("DicomPrintDialog.smooth") + ":"), "newline");
        this.smoothingTypeComboBox = new JComboBox<String>(new DefaultComboBoxModel<String>(new String[]{"MEDIUM", "SHARP", "SMOOTH"}));
        this.add(this.smoothingTypeComboBox);
        this.add(new JLabel(Messages.getString("DicomPrintDialog.border") + ":"));
        this.borderDensityComboBox = new JComboBox<String>(new DefaultComboBoxModel<String>(new String[]{"BLACK", "WHITE"}));
        this.add(this.borderDensityComboBox);
        this.add((Component)new JLabel(Messages.getString("DicomPrintDialog.trim") + ":"), "newline");
        this.trimComboBox = new JComboBox<String>(new DefaultComboBoxModel<String>(new String[]{"NO", "YES"}));
        this.add(this.trimComboBox);
        this.add(new JLabel(Messages.getString("DicomPrintDialog.empty_density") + ":"));
        this.comboBoxEmpty = new JComboBox<String>(new DefaultComboBoxModel<String>(new String[]{"BLACK", "WHITE"}));
        this.add(this.comboBoxEmpty);
        this.printAnnotationsCheckBox = new JCheckBox(Messages.getString("PrintDialog.annotate"));
        this.printAnnotationsCheckBox.setSelected(true);
        this.add((Component)this.printAnnotationsCheckBox, "newline, alignx leading, spanx 2");
        this.checkboxSelectedView = new JCheckBox(Messages.getString("PrintDialog.selected_view"));
        this.add((Component)this.checkboxSelectedView, "newline, alignx leading, spanx 2");
        this.add(new JLabel(Messages.getString("DicomPrintDialog.dpi") + ":"));
        this.comboBoxDPI = new JComboBox<PrintOptions.DotPerInches>(new DefaultComboBoxModel<PrintOptions.DotPerInches>(PrintOptions.DotPerInches.values()));
        this.add(this.comboBoxDPI);
    }

    public void applyOptions(DicomPrintOptions options) {
        if (options != null) {
            this.mediumTypeComboBox.setSelectedItem(options.getMediumType());
            this.priorityComboBox.setSelectedItem(options.getPriority());
            this.filmDestinationComboBox.setSelectedItem(options.getFilmDestination());
            this.numOfCopiesSpinner.setValue(options.getNumOfCopies());
            this.colorPrintCheckBox.setSelected(options.isColorPrint());
            this.filmOrientationComboBox.setSelectedItem(options.getFilmOrientation());
            this.filmSizeIdComboBox.setSelectedItem((Object)options.getFilmSizeId());
            this.imageDisplayFormatComboBox.setSelectedItem(options.getImageDisplayFormat());
            this.magnificationTypeComboBox.setSelectedItem(options.getMagnificationType());
            this.smoothingTypeComboBox.setSelectedItem(options.getSmoothingType());
            this.borderDensityComboBox.setSelectedItem(options.getBorderDensity());
            this.trimComboBox.setSelectedItem(options.getTrim());
            this.comboBoxEmpty.setSelectedItem(options.getEmptyDensity());
            this.printAnnotationsCheckBox.setSelected(options.isShowingAnnotations());
            this.checkboxSelectedView.setSelected(options.isPrintOnlySelectedView());
            this.comboBoxDPI.setSelectedItem(options.getDpi());
        }
    }

    public void saveOptions(DicomPrintOptions options) {
        options.setMediumType((String)this.mediumTypeComboBox.getSelectedItem());
        options.setPriority((String)this.priorityComboBox.getSelectedItem());
        options.setFilmDestination((String)this.filmDestinationComboBox.getSelectedItem());
        options.setNumOfCopies((Integer)this.numOfCopiesSpinner.getValue());
        options.setColorPrint(this.colorPrintCheckBox.isSelected());
        options.setFilmOrientation((String)this.filmOrientationComboBox.getSelectedItem());
        options.setFilmSizeId((DicomPrintDialog.FilmSize)((Object)this.filmSizeIdComboBox.getSelectedItem()));
        options.setImageDisplayFormat((String)this.imageDisplayFormatComboBox.getSelectedItem());
        options.setMagnificationType((String)this.magnificationTypeComboBox.getSelectedItem());
        options.setSmoothingType((String)this.smoothingTypeComboBox.getSelectedItem());
        options.setBorderDensity((String)this.borderDensityComboBox.getSelectedItem());
        options.setTrim((String)this.trimComboBox.getSelectedItem());
        options.setEmptyDensity((String)this.comboBoxEmpty.getSelectedItem());
        options.setShowingAnnotations(this.printAnnotationsCheckBox.isSelected());
        options.setPrintOnlySelectedView(this.checkboxSelectedView.isSelected());
        options.setDpi((PrintOptions.DotPerInches)this.comboBoxDPI.getSelectedItem());
    }
}

