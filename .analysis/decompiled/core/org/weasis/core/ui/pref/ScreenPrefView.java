/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.pref;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.Window;
import java.text.NumberFormat;
import java.util.List;
import java.util.Objects;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;
import org.weasis.core.api.gui.util.DecFormatter;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.WinUtil;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.ui.editor.image.dockable.MeasureTool;
import org.weasis.core.ui.pref.Monitor;
import org.weasis.core.ui.pref.ViewSetting;

public class ScreenPrefView
extends AbstractItemDialogPage {
    public ScreenPrefView() {
        super(Messages.getString("ScreenPrefView.monitors"), 108);
        this.getProperties().setProperty("help.item", "zoom/#real-world-size-display");
        JComboBox<String> defMonitorComboBox = new JComboBox<String>();
        List<Monitor> monitors = MeasureTool.viewSetting.getMonitors();
        for (int i = 0; i < monitors.size(); ++i) {
            Monitor monitor = monitors.get(i);
            Rectangle mb = monitor.getBounds();
            StringBuilder buf = new StringBuilder();
            buf.append(i + 1);
            buf.append(". ");
            buf.append(Messages.getString("ScreenPrefView.monitor"));
            buf.append(": ");
            buf.append(monitor.getMonitorID());
            buf.append(".");
            buf.append(mb.width);
            buf.append("x");
            buf.append(mb.height);
            String title = buf.toString();
            defMonitorComboBox.addItem(title);
            JLabel monitorLabel = new JLabel(ScreenPrefView.buildMonitorLabel(title, monitor));
            JButton realZoomButton = new JButton(Messages.getString("ScreenPrefView.sp_calib"));
            realZoomButton.addActionListener(e -> {
                CalibrationDialog dialog = new CalibrationDialog((Window)WinUtil.getParentFrame((Component)e.getSource()), title, Dialog.ModalityType.APPLICATION_MODAL, monitor);
                dialog.setBounds(monitor.getFullscreenBounds());
                dialog.setVisible(true);
                monitorLabel.setText(ScreenPrefView.buildMonitorLabel(title, monitor));
            });
            realZoomButton.setToolTipText(Messages.getString("ScreenPrefView.calib_real"));
            this.add(GuiUtils.getFlowLayoutPanel(monitorLabel, realZoomButton));
        }
        int defIndex = ScreenPrefView.getDefaultMonitor();
        if (defIndex < 0 || defIndex >= defMonitorComboBox.getItemCount()) {
            defIndex = 0;
        }
        defMonitorComboBox.setSelectedIndex(defIndex);
        defMonitorComboBox.addItemListener(e -> {
            if (e.getStateChange() == 1) {
                JComboBox comboBox = (JComboBox)e.getSource();
                GuiUtils.getUICore().getLocalPersistence().putIntProperty("default.monitor", comboBox.getSelectedIndex());
            }
        });
        JLabel presetsLabel = new JLabel(Messages.getString("ScreenPrefView.def_monitor") + ":");
        this.add(GuiUtils.getFlowLayoutPanel(0, 2, 5, presetsLabel, defMonitorComboBox));
        this.add(GuiUtils.boxYLastElement(5));
    }

    public static int getDefaultMonitor() {
        return GuiUtils.getUICore().getLocalPersistence().getIntProperty("default.monitor", 0);
    }

    private static String buildMonitorLabel(String title, Monitor monitor) {
        StringBuilder buf = new StringBuilder(title);
        if (monitor.getRealScaleFactor() > 0.0) {
            Rectangle mb = monitor.getBounds();
            double ratio = Unit.MILLIMETER.getConversionRatio(monitor.getRealScaleFactor());
            buf.append(" (");
            buf.append((int)Math.round((double)mb.width * ratio));
            buf.append("x");
            buf.append((int)Math.round((double)mb.height * ratio));
            buf.append(" ");
            buf.append(Unit.MILLIMETER.getAbbreviation());
            buf.append(")");
        }
        return buf.toString();
    }

    @Override
    public void closeAdditionalWindow() {
    }

    @Override
    public void resetToDefaultValues() {
    }

    static class CalibrationDialog
    extends JDialog {
        private final Monitor monitor;
        private final Cross cross;
        private final JFormattedTextField jTextFieldLineWidth = new JFormattedTextField(NumberFormat.getIntegerInstance());
        private final JComboBox<String> jComboBoxType = new JComboBox<String>(new String[]{Messages.getString("ScreenPrefView.horiz_line"), Messages.getString("ScreenPrefView.vertical_line"), Messages.getString("ScreenPrefView.screen_size")});
        private final JComboBox<Unit> jComboBoxUnit = new JComboBox<Unit>(new Unit[]{Unit.MILLIMETER, Unit.CENTIMETER, Unit.MILLIINCH, Unit.INCH});

        public CalibrationDialog(Window parentWindow, String title, Dialog.ModalityType applicationModal, Monitor monitor) {
            super(parentWindow, title, applicationModal, monitor.getGraphicsConfiguration());
            this.monitor = monitor;
            this.cross = new Cross(monitor);
            this.init();
        }

        protected void init() {
            Container content = this.getContentPane();
            JPanel inputPanel = new JPanel();
            this.jTextFieldLineWidth.setValue(0L);
            GuiUtils.setPreferredWidth(this.jTextFieldLineWidth, 100);
            inputPanel.add(new JLabel(Messages.getString("ScreenPrefView.enter_dist") + ":"));
            inputPanel.add(this.jComboBoxType);
            inputPanel.add(this.jTextFieldLineWidth);
            inputPanel.add(this.jComboBoxUnit);
            inputPanel.add(Box.createHorizontalStrut(15));
            JButton apply = new JButton(Messages.getString("ScreenPrefView.apply"));
            apply.addActionListener(e -> this.computeScaleFactor());
            inputPanel.add(apply);
            content.add((Component)this.cross, "Center");
            content.add((Component)inputPanel, "South");
        }

        private void computeScaleFactor() {
            Object object = this.jTextFieldLineWidth.getValue();
            if (object instanceof Number) {
                Number number = (Number)object;
                double val = number.doubleValue();
                if (val <= 0.0) {
                    this.monitor.setRealScaleFactor(0.0);
                } else {
                    Unit unit = (Unit)((Object)Objects.requireNonNull(this.jComboBoxUnit.getSelectedItem()));
                    int index = this.jComboBoxType.getSelectedIndex();
                    if (index == 0) {
                        int lineLength = this.cross.getHorizontalLength();
                        if (lineLength > 100) {
                            this.monitor.setRealScaleFactor(unit.getFactorToMeters() * val / (double)lineLength);
                        }
                    } else if (index == 1) {
                        int lineLength = this.cross.getVerticalLength();
                        if (lineLength > 100) {
                            this.monitor.setRealScaleFactor(unit.getFactorToMeters() * val / (double)lineLength);
                        }
                    } else if (index == 2) {
                        Rectangle bound = this.monitor.getBounds();
                        double w = bound.getWidth() * bound.getWidth();
                        double h = bound.getHeight() * bound.getHeight();
                        double realHeight = Math.sqrt(val * val * h / (w + h));
                        this.monitor.setRealScaleFactor(unit.getFactorToMeters() * realHeight / bound.getHeight());
                    }
                }
                this.cross.repaint();
                JOptionPane.showMessageDialog(WinUtil.getValidComponent(this), Messages.getString("ScreenPrefView.calib_desc"), Messages.getString("ScreenPrefView.sp_calib"), 2);
                String screen = ViewSetting.buildScreenItem(this.monitor);
                GuiUtils.getUICore().getLocalPersistence().putDoubleProperty(screen, this.monitor.getRealScaleFactor());
            }
        }
    }

    static class Cross
    extends JLabel {
        private final Monitor monitor;
        private int horizontalLength;
        private int verticalLength;

        public Cross(Monitor monitor) {
            this.monitor = monitor;
            this.horizontalLength = 0;
            this.verticalLength = 0;
        }

        @Override
        public void paintComponent(Graphics g) {
            if (g instanceof Graphics2D) {
                Graphics2D graphics2D = (Graphics2D)g;
                this.draw(graphics2D);
            }
        }

        protected void draw(Graphics2D g2d) {
            Stroke oldStroke = g2d.getStroke();
            Paint oldColor = g2d.getPaint();
            g2d.setPaint(Color.BLACK);
            int x = this.getX();
            int y = this.getY();
            int width = this.getWidth();
            int height = this.getHeight();
            g2d.fillRect(x, y, width, height);
            g2d.setPaint(Color.WHITE);
            g2d.setStroke(new BasicStroke(3.0f, 0, 2));
            int offset = 50;
            int x1 = x + offset;
            int y1 = y + height / 2;
            int x2 = x + width - offset;
            int y2 = y1;
            this.horizontalLength = x2 - x1;
            g2d.drawLine(x1, y1, x2, y2);
            int xv1 = x + width / 2;
            int yv1 = y + offset;
            int xv2 = xv1;
            int yv2 = y + height - offset;
            this.verticalLength = yv2 - yv1;
            g2d.drawLine(xv1, yv1, xv2, yv2);
            if (this.monitor.getRealScaleFactor() > 0.0) {
                String hlength = DecFormatter.allNumber(Unit.MILLIMETER.getConversionRatio(this.monitor.getRealScaleFactor()) * (double)this.horizontalLength) + " " + Unit.MILLIMETER.getAbbreviation();
                String vlength = DecFormatter.allNumber(Unit.MILLIMETER.getConversionRatio(this.monitor.getRealScaleFactor()) * (double)this.verticalLength) + " " + Unit.MILLIMETER.getAbbreviation();
                g2d.drawString(hlength, x2 - 70, y2 + 15);
                g2d.drawString(vlength, xv1 + 10, yv2 - 5);
            }
            g2d.setPaint(oldColor);
            g2d.setStroke(oldStroke);
        }

        public int getHorizontalLength() {
            return this.horizontalLength;
        }

        public int getVerticalLength() {
            return this.verticalLength;
        }
    }
}

