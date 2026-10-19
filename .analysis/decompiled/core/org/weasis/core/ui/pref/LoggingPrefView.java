/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  ch.qos.logback.classic.LoggerContext
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.ui.pref;

import ch.qos.logback.classic.LoggerContext;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.service.AuditLog;
import org.weasis.core.api.service.WProperties;
import org.weasis.core.util.StringUtil;

public class LoggingPrefView
extends AbstractItemDialogPage {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingPrefView.class);
    public static final String PAGE_NAME = Messages.getString("logging");
    private final JCheckBox checkboxFileLog = new JCheckBox(Messages.getString("GeneralSetting.rol_log"));
    private final JLabel lblLogLevel = new JLabel(Messages.getString("GeneralSetting.log_level") + ":");
    private final JComboBox<AuditLog.LEVEL> comboBoxLogLevel = new JComboBox<AuditLog.LEVEL>(AuditLog.LEVEL.values());
    private final JLabel labelNumber = new JLabel(Messages.getString("GeneralSetting.log_nb") + ":");
    private final JSpinner spinner = new JSpinner();
    private final JLabel labelSize = new JLabel(Messages.getString("GeneralSetting.log_size") + ":");
    private final JSpinner spinner1 = new JSpinner();
    private final JLabel lblStacktraceLimit = new JLabel(Messages.getString("GeneralSetting.stack_limit") + ":");
    private final JComboBox<String> comboBoxStackLimit = new JComboBox<String>(new String[]{"", "0", "1", "3", "5", "10", "20", "50", "100"});

    public LoggingPrefView() {
        super(PAGE_NAME, 115);
        try {
            GuiUtils.setNumberModel(this.spinner, LoggingPrefView.getIntPreferences("org.apache.sling.commons.log.file.number", 20, null), 1, 99, 1);
            GuiUtils.setNumberModel(this.spinner1, LoggingPrefView.getIntPreferences("org.apache.sling.commons.log.file.size", 10, "MB"), 1, 99, 1);
            this.jbInit();
            this.initialize();
        }
        catch (Exception e) {
            LOGGER.error("Cannot initialize", (Throwable)e);
        }
    }

    private void jbInit() {
        JPanel panel = GuiUtils.getVerticalBoxLayoutPanel(new JComponent[0]);
        panel.add(GuiUtils.getFlowLayoutPanel(2, 5, this.checkboxFileLog));
        panel.add(GuiUtils.getFlowLayoutPanel(this.labelNumber, this.spinner, GuiUtils.boxHorizontalStrut(15), this.labelSize, this.spinner1));
        panel.setBorder(GuiUtils.getTitledBorder(Messages.getString("file")));
        this.add(panel);
        this.add(GuiUtils.getFlowLayoutPanel(2, 10, this.lblLogLevel, this.comboBoxLogLevel, GuiUtils.boxHorizontalStrut(15), this.lblStacktraceLimit, this.comboBoxStackLimit));
        this.add(GuiUtils.boxYLastElement(5));
        this.checkboxFileLog.addActionListener(e -> this.checkRollingLog());
        this.getProperties().setProperty("show.restore", Boolean.TRUE.toString());
        this.getProperties().setProperty("help.item", "logging");
    }

    private void checkRollingLog() {
        boolean rolling = this.checkboxFileLog.isSelected();
        this.spinner.setEnabled(rolling);
        this.spinner1.setEnabled(rolling);
    }

    private static int getIntPreferences(String key, int defaultValue, String removedSuffix) {
        String s;
        if (key != null && (s = GuiUtils.getUICore().getSystemPreferences().getProperty(key)) != null) {
            int index;
            if (removedSuffix != null && (index = s.lastIndexOf(removedSuffix)) > 0) {
                s = s.substring(0, index);
            }
            try {
                return Integer.parseInt(s);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return defaultValue;
    }

    protected void initialize() {
        WProperties prefs = GuiUtils.getUICore().getSystemPreferences();
        this.comboBoxLogLevel.setSelectedItem((Object)AuditLog.LEVEL.getLevel(prefs.getProperty("org.apache.sling.commons.log.level", "INFO")));
        int limit = LoggingPrefView.getIntPreferences("org.apache.sling.commons.log.stack.limit", 3, null);
        if (limit > 0 && limit != 1 && limit != 3 && limit != 5 && limit != 10 && limit != 20 && limit != 50 && limit != 100) {
            this.comboBoxStackLimit.addItem(Integer.toString(limit));
        }
        this.comboBoxStackLimit.setSelectedItem(limit >= 0 ? Integer.toString(limit) : "");
        this.checkboxFileLog.setSelected(prefs.getBooleanProperty("org.apache.sling.commons.log.file.activate", false));
        this.spinner.setValue(LoggingPrefView.getIntPreferences("org.apache.sling.commons.log.file.number", 20, null));
        this.spinner1.setValue(LoggingPrefView.getIntPreferences("org.apache.sling.commons.log.file.size", 10, "MB"));
        this.checkRollingLog();
    }

    @Override
    public void closeAdditionalWindow() {
        WProperties preferences = GuiUtils.getUICore().getSystemPreferences();
        String limit = (String)this.comboBoxStackLimit.getSelectedItem();
        preferences.setProperty("org.apache.sling.commons.log.stack.limit", StringUtil.hasText((CharSequence)limit) ? limit : "-1");
        AuditLog.LEVEL level = (AuditLog.LEVEL)((Object)this.comboBoxLogLevel.getSelectedItem());
        if (level == null) {
            level = AuditLog.LEVEL.INFO;
        }
        preferences.setProperty("org.apache.sling.commons.log.level", level.toString());
        preferences.putBooleanProperty("org.apache.sling.commons.log.file.activate", this.checkboxFileLog.isSelected());
        Object logFile = this.checkboxFileLog.isSelected() ? AuditLog.LOG_FOLDER_PATH + "default.log" : "";
        preferences.setProperty("org.apache.sling.commons.log.file", (String)logFile);
        if (this.checkboxFileLog.isSelected()) {
            String fileNb = this.spinner.getValue().toString();
            String fileSize = this.spinner1.getValue().toString() + "MB";
            preferences.setProperty("org.apache.sling.commons.log.file.number", fileNb);
            preferences.setProperty("org.apache.sling.commons.log.file.size", fileSize);
        }
        LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
        AuditLog.applyConfig(preferences, loggerContext);
    }

    @Override
    public void resetToDefaultValues() {
        WProperties preferences = GuiUtils.getUICore().getSystemPreferences();
        preferences.resetServiceProperty("org.apache.sling.commons.log.stack.limit", "3");
        preferences.resetServiceProperty("org.apache.sling.commons.log.level", "INFO");
        preferences.resetServiceProperty("org.apache.sling.commons.log.file", "");
        preferences.resetServiceProperty("org.apache.sling.commons.log.file.number", "20");
        preferences.resetServiceProperty("org.apache.sling.commons.log.file.size", "10MB");
        this.initialize();
    }
}

