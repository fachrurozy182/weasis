/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.FlatLaf
 *  com.formdev.flatlaf.util.SystemInfo
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.core.ui.pref;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.util.SystemInfo;
import java.awt.Window;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextPane;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.service.WProperties;

public class ThemeSetting
extends AbstractItemDialogPage {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThemeSetting.class);
    public static final String PAGE_NAME = Messages.getString("appearance");
    private final JLabel jLabelMLook = new JLabel();
    private final JComboBox<LookInfo> comboBoxTheme = new JComboBox();
    private final JButton button = new JButton(Messages.getString("GeneralSetting.show"));
    private final JRadioButton systemScaleRadio = new JRadioButton(Messages.getString("use.the.system.scale.factor"));
    private final JRadioButton userScaleRadio = new JRadioButton(Messages.getString("use.a.custom.scale.factor"));
    private final ButtonGroup buttonGroup = new ButtonGroup();
    private final JSpinner spinner1 = new JSpinner();
    private final JCheckBox checkboxDecoration = new JCheckBox(Messages.getString("menu.integrated.into.title.bar"));
    private final JCheckBox checkboxFileChooser = new JCheckBox(Messages.getString("use.system.file.chooser"));

    public ThemeSetting() {
        super(PAGE_NAME, 102);
        this.setList(this.comboBoxTheme, UIManager.getInstalledLookAndFeels());
        try {
            GuiUtils.setNumberModel(this.spinner1, 100, 20, 400, 5);
            this.jbInit();
            this.initialize(true);
        }
        catch (Exception e) {
            LOGGER.error("Cannot initialize GeneralSetting", (Throwable)e);
        }
    }

    private void jbInit() {
        this.jLabelMLook.setText(Messages.getString("theme") + ":");
        this.add(GuiUtils.getFlowLayoutPanel(2, 10, this.jLabelMLook, this.comboBoxTheme, GuiUtils.boxHorizontalStrut(5), this.button));
        this.add(GuiUtils.boxVerticalStrut(15));
        JPanel panel = GuiUtils.getVerticalBoxLayoutPanel(new JComponent[0]);
        panel.setBorder(GuiUtils.getTitledBorder(Messages.getString("display.scale.factor")));
        panel.add(GuiUtils.getFlowLayoutPanel(2, 0, this.systemScaleRadio));
        panel.add(GuiUtils.getFlowLayoutPanel(2, 0, this.userScaleRadio, this.spinner1));
        this.add(panel);
        this.buttonGroup.add(this.systemScaleRadio);
        this.buttonGroup.add(this.userScaleRadio);
        this.systemScaleRadio.addActionListener(e -> this.spinner1.setEnabled(false));
        this.userScaleRadio.addActionListener(e -> this.spinner1.setEnabled(true));
        this.button.addActionListener(e -> {
            LookInfo item = (LookInfo)((Object)((Object)this.comboBoxTheme.getSelectedItem()));
            String finalLafClassName = Objects.requireNonNull(item).className();
            Runnable runnable = () -> {
                try {
                    UIManager.setLookAndFeel(finalLafClassName);
                    for (Window w : Window.getWindows()) {
                        SwingUtilities.updateComponentTreeUI(w);
                    }
                }
                catch (Exception e1) {
                    LOGGER.error("Can't change look and feel", (Throwable)e1);
                }
            };
            GuiExecutor.execute(runnable);
        });
        if (SystemInfo.isLinux) {
            this.add(GuiUtils.getFlowLayoutPanel(2, 10, this.checkboxDecoration));
        }
        this.add(GuiUtils.getFlowLayoutPanel(2, 10, this.checkboxFileChooser));
        this.add(GuiUtils.boxVerticalStrut(10));
        String alert = "<html><p><font color=\"%s\">%s</font></p></html>\n".formatted(new Object[]{GuiUtils.IconColor.ACTIONS_RED.getHtmlCode(), Messages.getString("GeneralSetting.alertNote")});
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.setPreferredSize(GuiUtils.getDimension(300, 50));
        textPane.setText(alert);
        this.add(textPane);
        this.add(GuiUtils.boxYLastElement(5));
        this.getProperties().setProperty("show.restore", Boolean.TRUE.toString());
        this.getProperties().setProperty("help.item", "theme");
    }

    protected void initialize(boolean firstTime) {
        LookAndFeel currentLAF;
        WProperties preferences = GuiUtils.getUICore().getSystemPreferences();
        String className = preferences.getProperty("weasis.theme");
        if (className == null && (currentLAF = UIManager.getLookAndFeel()) != null) {
            className = currentLAF.getClass().getName();
        }
        LookInfo oldLaf = null;
        if (className != null) {
            for (int i = 0; i < this.comboBoxTheme.getItemCount(); ++i) {
                LookInfo look = this.comboBoxTheme.getItemAt(i);
                if (!className.equals(look.className())) continue;
                oldLaf = look;
                break;
            }
        }
        if (oldLaf == null) {
            this.comboBoxTheme.setSelectedIndex(0);
            oldLaf = (LookInfo)((Object)this.comboBoxTheme.getSelectedItem());
        } else {
            this.comboBoxTheme.setSelectedItem((Object)oldLaf);
        }
        float scale = ThemeSetting.parseScaleFactor(preferences.getProperty("flatlaf.uiScale"));
        if (scale <= 0.0f) {
            scale = 1.0f;
            this.systemScaleRadio.doClick();
        } else {
            this.userScaleRadio.doClick();
        }
        this.spinner1.setValue(Math.round(scale * 100.0f));
        if (SystemInfo.isLinux) {
            this.checkboxDecoration.setSelected(preferences.getBooleanProperty("weasis.linux.windows.decoration", false));
        }
        this.checkboxFileChooser.setSelected(preferences.getBooleanProperty("weasis.use.system.file.chooser", true));
    }

    public void setList(JComboBox<LookInfo> jComboBox, UIManager.LookAndFeelInfo[] look) {
        jComboBox.removeAllItems();
        for (UIManager.LookAndFeelInfo lookAndFeelInfo : Arrays.stream(look).sorted(Comparator.comparing(UIManager.LookAndFeelInfo::getName)).toList()) {
            jComboBox.addItem(new LookInfo(lookAndFeelInfo.getName(), lookAndFeelInfo.getClassName()));
        }
    }

    private static float parseScaleFactor(String s) {
        if (s == null) {
            return -1.0f;
        }
        float units = 1.0f;
        if (s.endsWith("x")) {
            s = s.substring(0, s.length() - 1);
        } else if (s.endsWith("dpi")) {
            units = 96.0f;
            s = s.substring(0, s.length() - 3);
        } else if (s.endsWith("%")) {
            units = 100.0f;
            s = s.substring(0, s.length() - 1);
        }
        try {
            float scale = Float.parseFloat(s);
            return scale > 0.0f ? scale / units : -1.0f;
        }
        catch (NumberFormatException ex) {
            return -1.0f;
        }
    }

    @Override
    public void closeAdditionalWindow() {
        Object object;
        WProperties preferences = GuiUtils.getUICore().getSystemPreferences();
        LookInfo look = (LookInfo)((Object)this.comboBoxTheme.getSelectedItem());
        if (look != null) {
            preferences.setProperty("weasis.theme", look.className());
        }
        GuiUtils.getUICore().saveSystemPreferences();
        LookAndFeel currentLAF = UIManager.getLookAndFeel();
        if (currentLAF != null && look != null && !look.className().equals(currentLAF.getClass().getName())) {
            Runnable runnable = () -> {
                try {
                    UIManager.setLookAndFeel(look.className());
                    FlatLaf.updateUI();
                }
                catch (Exception e) {
                    LOGGER.error("Can't change look and feel", (Throwable)e);
                }
            };
            GuiExecutor.execute(runnable);
        }
        String scale = "-1";
        if (this.userScaleRadio.isSelected() && (object = this.spinner1.getValue()) instanceof Integer) {
            Integer val = (Integer)object;
            scale = String.valueOf((float)val.intValue() / 100.0f);
        }
        preferences.setProperty("flatlaf.uiScale", scale);
        if (SystemInfo.isLinux) {
            preferences.putBooleanProperty("weasis.linux.windows.decoration", this.checkboxDecoration.isSelected());
        }
        boolean useSystemFileChooser = this.checkboxFileChooser.isSelected();
        preferences.putBooleanProperty("weasis.use.system.file.chooser", useSystemFileChooser);
        System.setProperty("flatlaf.useSystemFileChooser", String.valueOf(useSystemFileChooser));
    }

    @Override
    public void resetToDefaultValues() {
        WProperties preferences = GuiUtils.getUICore().getSystemPreferences();
        preferences.setProperty("weasis.theme", "org.weasis.launcher.FlatWeasisTheme");
        preferences.setProperty("flatlaf.uiScale", "-1");
        if (SystemInfo.isLinux) {
            preferences.setProperty("weasis.linux.windows.decoration", Boolean.FALSE.toString());
        }
        preferences.putBooleanProperty("weasis.use.system.file.chooser", true);
        this.initialize(false);
    }

    record LookInfo(String name, String className) {
        public String toString() {
            return this.name;
        }
    }
}

