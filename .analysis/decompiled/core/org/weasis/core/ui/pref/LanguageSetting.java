/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.core.ui.pref;

import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Objects;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.service.WProperties;
import org.weasis.core.ui.pref.JLocaleFormat;
import org.weasis.core.ui.pref.JLocaleLanguage;
import org.weasis.core.ui.pref.JLocalePercentage;

public class LanguageSetting
extends AbstractItemDialogPage {
    private static final Logger LOGGER = LoggerFactory.getLogger(LanguageSetting.class);
    public static final String PAGE_NAME = Messages.getString("GeneralSetting.language");
    private final JTextPane textPane = new JTextPane();
    private final JLabel labelLocale = new JLabel(Messages.getString("GeneralSetting.language") + ":");
    private final JLabel labelLocale2 = new JLabel(Messages.getString("GeneralSetting.language.data") + ":");
    private final JLocaleFormat comboBoxFormat = new JLocaleFormat(this){
        final /* synthetic */ LanguageSetting this$0;
        {
            LanguageSetting languageSetting = this$0;
            Objects.requireNonNull(languageSetting);
            this.this$0 = languageSetting;
        }

        @Override
        public void valueHasChanged() {
            this.this$0.textPane.setText(this.this$0.getText());
        }
    };
    private final JLocaleLanguage comboBoxLang = new JLocaleLanguage(this){
        final /* synthetic */ LanguageSetting this$0;
        {
            LanguageSetting languageSetting = this$0;
            Objects.requireNonNull(languageSetting);
            this.this$0 = languageSetting;
        }

        @Override
        public void valueHasChanged() {
            this.this$0.comboBoxFormat.refresh();
        }
    };

    public LanguageSetting() {
        super(PAGE_NAME, 101);
        try {
            this.jbInit();
            this.initialize();
            this.comboBoxLang.addItemListener(this.comboBoxLang);
        }
        catch (Exception e) {
            LOGGER.error("Cannot initialize GeneralSetting", (Throwable)e);
        }
    }

    private void jbInit() {
        GuiUtils.setPreferredWidth(this.comboBoxLang, 300, 150);
        GuiUtils.setPreferredWidth(this.comboBoxFormat, 300, 150);
        this.add(GuiUtils.getFlowLayoutPanel(3, 2, 10, this.labelLocale, this.comboBoxLang));
        this.add(GuiUtils.getFlowLayoutPanel(3, 2, 5, this.labelLocale2, this.comboBoxFormat));
        this.textPane.setPreferredSize(GuiUtils.getDimension(300, 150));
        this.textPane.setContentType("text/html");
        this.textPane.setEditable(false);
        this.textPane.addHyperlinkListener(GuiUtils.buildHyperlinkListener());
        this.add(this.textPane);
        this.getProperties().setProperty("show.restore", Boolean.TRUE.toString());
        this.getProperties().setProperty("help.item", "locale");
    }

    private String getText() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        int translationPercentage = 100;
        Object object = this.comboBoxLang.getSelectedItem();
        if (object instanceof JLocalePercentage) {
            JLocalePercentage jLocalePercentage = (JLocalePercentage)object;
            translationPercentage = jLocalePercentage.getPercentage();
        }
        String baseText = "<html>\n  <h3>%s</h3>\n  <font size=\"-1\">\n  %s (%s): %s<BR>\n  %2$s (%s): %s<BR>\n  %2$s (%s): %s<BR>\n  %2$s (%s): %s<BR>\n  %s: %s\n  </font>\n  <p><font color=\"%s\">%s</font></p>\n</html>\n".formatted(new Object[]{Messages.getString("GeneralSetting.regionalTitle"), Messages.getString("GeneralSetting.date"), Messages.getString("GeneralSetting.short"), DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(now), Messages.getString("GeneralSetting.medium"), DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(now), Messages.getString("GeneralSetting.long"), DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).format(now), Messages.getString("GeneralSetting.full"), DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL).format(now), Messages.getString("GeneralSetting.nb"), NumberFormat.getNumberInstance().format(2543456.3465), GuiUtils.IconColor.ACTIONS_RED.getHtmlCode(), Messages.getString("GeneralSetting.alertNote")});
        if (translationPercentage < 100) {
            String transifexUrl = "https://app.transifex.com/weasis/weasis/";
            String translationNote = "<p><font color=\"%s\">%s: <a href=\"%s\">%s</a></font></p>\n".formatted(new Object[]{GuiUtils.IconColor.ACTIONS_BLUE.getHtmlCode(), Messages.getString("GeneralSetting.translation.msg").formatted(new Object[]{translationPercentage}), transifexUrl, transifexUrl});
            baseText = baseText.replace("</html>", translationNote + "</html>");
        }
        return baseText;
    }

    protected void initialize() {
        WProperties preferences = GuiUtils.getUICore().getSystemPreferences();
        this.comboBoxLang.selectLocale(preferences.getProperty("locale.lang.code"));
        this.comboBoxFormat.selectLocale();
        this.textPane.setText(this.getText());
    }

    @Override
    public void closeAdditionalWindow() {
        GuiUtils.getUICore().saveSystemPreferences();
    }

    @Override
    public void resetToDefaultValues() {
        WProperties preferences = GuiUtils.getUICore().getSystemPreferences();
        preferences.resetProperty("locale.lang.code", "en");
        Locale.setDefault(Locale.ENGLISH);
        preferences.resetProperty("locale.format.code", null);
        this.initialize();
    }
}

