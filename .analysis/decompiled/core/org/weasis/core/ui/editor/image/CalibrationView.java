/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.MathUtil
 */
package org.weasis.core.ui.editor.image;

import java.awt.Component;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.DecFormatter;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.model.graphic.imp.line.LineGraphic;
import org.weasis.core.util.MathUtil;

public class CalibrationView
extends JPanel {
    private static final Logger LOGGER = LoggerFactory.getLogger(CalibrationView.class);
    private final ViewCanvas<?> view2d;
    private final LineGraphic line;
    private final JComboBox<Unit> jComboBoxUnit;
    private final JFormattedTextField jTextFieldLineWidth = new JFormattedTextField();
    private final ButtonGroup ratioGroup = new ButtonGroup();
    private final JRadioButton radioButtonSeries = new JRadioButton(Messages.getString("CalibrationView.series"));
    private final JRadioButton radioButtonImage = new JRadioButton(Messages.getString("CalibrationView.current"));

    public CalibrationView(LineGraphic line, ViewCanvas<?> view2d, boolean selectSeries) {
        this.line = line;
        this.view2d = view2d;
        List<Unit> units = Unit.getPhysicalUnits();
        this.jComboBoxUnit = new JComboBox<Unit>(units.toArray(new Unit[0]));
        try {
            this.jbInit();
            this.radioButtonSeries.setSelected(selectSeries);
            if (!selectSeries) {
                this.radioButtonImage.setSelected(true);
            }
            this.initialize();
        }
        catch (Exception e) {
            LOGGER.error(e.getMessage(), (Throwable)e);
        }
    }

    void jbInit() {
        this.setLayout(new BoxLayout(this, 1));
        this.setBorder(GuiUtils.getEmptyBorder(10, 15, 10, 15));
        GuiUtils.setPreferredWidth(this.jTextFieldLineWidth, 170);
        this.jTextFieldLineWidth.setLocale(Locale.getDefault(Locale.Category.FORMAT));
        this.jTextFieldLineWidth.setFormatterFactory(DecFormatter.setPreciseDoubleFormat(5.0E-6, Double.MAX_VALUE));
        this.jTextFieldLineWidth.setValue(1.0);
        GuiUtils.addCheckAction(this.jTextFieldLineWidth);
        JLabel jLabelKnownDist = new JLabel(Messages.getString("CalibrationView.known") + ":");
        JLabel lblApplyTo = new JLabel(Messages.getString("CalibrationView.apply") + ":");
        this.ratioGroup.add(this.radioButtonSeries);
        this.ratioGroup.add(this.radioButtonImage);
        this.add(GuiUtils.getFlowLayoutPanel(jLabelKnownDist, this.jTextFieldLineWidth, this.jComboBoxUnit));
        this.add(GuiUtils.getFlowLayoutPanel(lblApplyTo, this.radioButtonSeries, this.radioButtonImage));
    }

    public boolean isApplyingToSeries() {
        return this.radioButtonSeries.isSelected();
    }

    private void initialize() {
        Object image = this.view2d.getImage();
        if (image != null) {
            Unit unit = ((ImageElement)image).getPixelSpacingUnit();
            if (!Unit.PIXEL.equals((Object)unit)) {
                Point2D ptA = this.line.getStartPoint();
                Point2D ptB = this.line.getEndPoint();
                if (Objects.nonNull(ptA) && Objects.nonNull(ptB)) {
                    this.jTextFieldLineWidth.setValue(ptA.distance(ptB) * ((ImageElement)image).getPixelSize());
                }
            } else {
                JTextArea area = new JTextArea(Messages.getString("CalibrationView.warn"));
                area.setLineWrap(true);
                area.setWrapStyleWord(true);
                area.setEditable(false);
                area.setColumns(0);
                JScrollPane scroll = new JScrollPane(area);
                scroll.setPreferredSize(GuiUtils.getDimension(300, GuiUtils.getBigIconButtonSize((JComponent)area).height * 4));
                this.add((Component)scroll, 0);
                unit = Unit.MILLIMETER;
            }
            this.jComboBoxUnit.setSelectedItem((Object)unit);
        }
    }

    public void removeCalibration() {
        this.applyCalibration(1.0, Unit.PIXEL);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void applyCalibration(double ratio, Unit unit) {
        Object image = this.view2d.getImage();
        if (image != null) {
            MediaSeries seriesList;
            if (this.radioButtonSeries.isSelected() && Objects.nonNull(seriesList = this.view2d.getSeries())) {
                Iterable list = seriesList.getMedias(null, null);
                MediaSeries mediaSeries = seriesList;
                synchronized (mediaSeries) {
                    for (Object media : list) {
                        if (!(media instanceof ImageElement)) continue;
                        ImageElement img = (ImageElement)media;
                        if (media == image) continue;
                        img.setPixelSpacingUnit(unit);
                        img.setPixelSize(ratio);
                        img.setPixelSizeCalibrationDescription("Modified by user");
                        img.setPixelSizeModifiedByUser(true);
                    }
                }
            }
            ((ImageElement)image).setPixelSize(ratio);
            ((ImageElement)image).setPixelSpacingUnit(unit);
            ((ImageElement)image).setPixelSizeCalibrationDescription("Modified by user");
            ((ImageElement)image).setPixelSizeModifiedByUser(true);
            ImageViewerEventManager<?> manager = this.view2d.getEventManager();
            if (manager.getSelectedViewPane() == this.view2d) {
                manager.getAction(ActionW.SPATIAL_UNIT).ifPresent(c -> c.setSelectedItem((Object)unit));
            }
            this.view2d.getGraphicManager().updateLabels(Boolean.TRUE, this.view2d);
        }
    }

    public void applyNewCalibration() {
        Number inputCalibVal;
        Object image = this.view2d.getImage();
        if (image != null && (inputCalibVal = GuiUtils.getFormattedValue(this.jTextFieldLineWidth)) != null) {
            double imgRatio = ((ImageElement)image).getPixelSize();
            Unit unit = (Unit)((Object)this.jComboBoxUnit.getSelectedItem());
            Unit imgUnit = ((ImageElement)image).getPixelSpacingUnit();
            if (!Unit.PIXEL.equals((Object)unit)) {
                double newRatio;
                ((ImageElement)image).setPixelSpacingUnit(unit);
                Double lineLength = null;
                Point2D ptA = this.line.getStartPoint();
                Point2D ptB = this.line.getEndPoint();
                if (Objects.nonNull(ptA) && Objects.nonNull(ptB)) {
                    lineLength = ptA.distance(ptB);
                }
                if (Objects.isNull(lineLength) || lineLength < 1.0) {
                    lineLength = 1.0;
                }
                if (MathUtil.isDifferent((double)(newRatio = inputCalibVal.doubleValue() / lineLength), (double)imgRatio) || !Objects.equals((Object)unit, (Object)imgUnit)) {
                    this.applyCalibration(newRatio, unit);
                }
            }
        }
    }
}

