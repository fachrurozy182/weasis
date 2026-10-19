/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.core.ui.pref;

import java.awt.GridLayout;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.PageItem;
import org.weasis.core.api.service.WProperties;
import org.weasis.core.ui.pref.LanguageSetting;
import org.weasis.core.ui.pref.LauncherPrefView;
import org.weasis.core.ui.pref.LoggingPrefView;
import org.weasis.core.ui.pref.PreferenceDialog;
import org.weasis.core.ui.pref.ProxyPrefView;
import org.weasis.core.ui.pref.ScreenPrefView;
import org.weasis.core.ui.pref.ShortcutPrefView;
import org.weasis.core.ui.pref.ThemeSetting;

public class GeneralSetting
extends AbstractItemDialogPage {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralSetting.class);
    public static final String PAGE_NAME = Messages.getString("GeneralSetting.gen");
    private final JCheckBox checkboxConfirmClosing = new JCheckBox(Messages.getString("GeneralSetting.closingConfirmation"));
    private final JPanel menuPanel = new JPanel();

    public GeneralSetting(PreferenceDialog dialog) {
        super(PAGE_NAME, 100);
        try {
            this.menuPanel.setLayout(new GridLayout(0, 2));
            this.add(this.menuPanel);
            this.add(GuiUtils.boxVerticalStrut(15));
            this.jbInit();
            this.initialize();
        }
        catch (Exception e) {
            LOGGER.error("Cannot initialize GeneralSetting", (Throwable)e);
        }
        List<LauncherPrefView> childPages = List.of(new LanguageSetting(), new ThemeSetting(), new ShortcutPrefView(), new ScreenPrefView(), new ProxyPrefView(), new LoggingPrefView(), new LauncherPrefView());
        childPages.forEach(p -> this.addSubPage((PageItem)p, a -> dialog.showPage(p.getTitle()), this.menuPanel));
    }

    private void jbInit() {
        this.add(GuiUtils.getFlowLayoutPanel(0, 10, this.checkboxConfirmClosing));
        this.add(GuiUtils.boxVerticalStrut(15));
        this.add(GuiUtils.boxYLastElement(5));
        this.getProperties().setProperty("show.restore", Boolean.TRUE.toString());
    }

    @Override
    public JPanel getMenuPanel() {
        return this.menuPanel;
    }

    protected void initialize() {
        WProperties preferences = GuiUtils.getUICore().getSystemPreferences();
        this.checkboxConfirmClosing.setSelected(preferences.getBooleanProperty("weasis.confirm.closing", false));
    }

    @Override
    public void closeAdditionalWindow() {
        for (PageItem subpage : this.getSubPages()) {
            subpage.closeAdditionalWindow();
        }
        GuiUtils.getUICore().getSystemPreferences().putBooleanProperty("weasis.confirm.closing", this.checkboxConfirmClosing.isSelected());
    }

    @Override
    public void resetToDefaultValues() {
        GuiUtils.getUICore().getSystemPreferences().resetProperty("weasis.confirm.closing", Boolean.FALSE.toString());
        this.initialize();
    }
}

