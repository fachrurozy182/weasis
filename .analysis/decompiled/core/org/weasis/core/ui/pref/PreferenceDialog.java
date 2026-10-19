/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.osgi.framework.BundleContext
 *  org.osgi.framework.ServiceReference
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.ui.pref;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.Insertable;
import org.weasis.core.api.gui.InsertableUtil;
import org.weasis.core.api.gui.PreferencesPageFactory;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;
import org.weasis.core.api.gui.util.AbstractWizardDialog;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.PageItem;
import org.weasis.core.ui.pref.DicomPrefView;
import org.weasis.core.ui.pref.DrawPrefView;
import org.weasis.core.ui.pref.GeneralSetting;
import org.weasis.core.ui.pref.ViewerPrefView;
import org.weasis.core.util.StringUtil;

public class PreferenceDialog
extends AbstractWizardDialog {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreferenceDialog.class);
    public static final String KEY_SHOW_APPLY = "show.apply";
    public static final String KEY_SHOW_RESTORE = "show.restore";
    public static final String KEY_HELP = "help.item";
    protected final JButton jButtonHelp = new JButton();
    protected final JButton restoreButton = new JButton(Messages.getString("restore.values"));
    protected final JButton applyButton = new JButton(Messages.getString("LabelPrefView.apply"));
    protected final JPanel bottomPrefPanel = GuiUtils.getFlowLayoutPanel(4, 10, 7, this.jButtonHelp, this.restoreButton, this.applyButton);

    public PreferenceDialog(Window parentWin) {
        super(parentWin, Messages.getString("OpenPreferencesAction.title"), Dialog.ModalityType.APPLICATION_MODAL, new Dimension(600, 450));
        this.jPanelBottom.add((Component)this.bottomPrefPanel, 0);
        this.jButtonHelp.putClientProperty("JButton.buttonType", "help");
        this.jButtonHelp.setToolTipText(Messages.getString("online.documentation"));
        this.applyButton.addActionListener(e -> {
            if (this.currentPage != null) {
                this.currentPage.closeAdditionalWindow();
            }
        });
        this.restoreButton.addActionListener(e -> {
            if (this.currentPage != null) {
                this.currentPage.resetToDefaultValues();
            }
        });
        this.initializePages();
        this.pack();
        this.showFirstPage();
    }

    @Override
    protected void initializePages() {
        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put("weasis.user.prefs", System.getProperty("weasis.user.prefs", "user"));
        ArrayList<Insertable> list = new ArrayList<Insertable>();
        GeneralSetting generalSetting = new GeneralSetting(this);
        list.add(generalSetting);
        ViewerPrefView viewerSetting = new ViewerPrefView();
        list.add(viewerSetting);
        DicomPrefView dicomPrefView = new DicomPrefView();
        list.add(dicomPrefView);
        DrawPrefView drawPrefView = new DrawPrefView(this);
        list.add(drawPrefView);
        BundleContext context = AppProperties.getBundleContext(this.getClass());
        try {
            for (ServiceReference serviceReference : context.getServiceReferences(PreferencesPageFactory.class, null)) {
                Insertable page;
                String className;
                PreferencesPageFactory factory = (PreferencesPageFactory)context.getService(serviceReference);
                if (factory == null || StringUtil.hasText((CharSequence)(className = GuiUtils.getUICore().getSystemPreferences().getProperty(factory.getClass().getName()))) && !Boolean.parseBoolean(className) || (page = factory.createInstance(properties)) == null) continue;
                int position = ((AbstractItemDialogPage)page).getComponentPosition();
                if (position < 1000) {
                    AbstractItemDialogPage mainPage = position > 500 && position < 600 ? viewerSetting : (position > 600 && position < 700 ? dicomPrefView : (position > 700 && position < 800 ? drawPrefView : generalSetting));
                    JComponent menuPanel = ((AbstractItemDialogPage)mainPage).getMenuPanel();
                    mainPage.addSubPage((PageItem)((Object)page), arg_0 -> this.lambda$initializePages$0((AbstractItemDialogPage)page, arg_0), menuPanel);
                    if (menuPanel == null) continue;
                    menuPanel.revalidate();
                    menuPanel.repaint();
                    continue;
                }
                list.add(page);
            }
        }
        catch (Exception e) {
            LOGGER.error("Get Preference pages from service", (Throwable)e);
        }
        InsertableUtil.sortInsertable(list);
        for (AbstractItemDialogPage abstractItemDialogPage : list) {
            abstractItemDialogPage.sortSubPages();
            this.pagesRoot.add(new DefaultMutableTreeNode(abstractItemDialogPage));
        }
        this.iniTree();
    }

    @Override
    protected void selectPage(AbstractItemDialogPage page) {
        if (page != null) {
            super.selectPage(page);
            this.applyButton.setVisible(Boolean.TRUE.toString().equals(page.getProperty(KEY_SHOW_APPLY)));
            this.restoreButton.setVisible(Boolean.TRUE.toString().equals(page.getProperty(KEY_SHOW_RESTORE)));
            String helpKey = page.getProperty(KEY_HELP);
            for (ActionListener al : this.jButtonHelp.getActionListeners()) {
                this.jButtonHelp.removeActionListener(al);
            }
            this.jButtonHelp.setVisible(StringUtil.hasText((CharSequence)helpKey));
            if (this.jButtonHelp.isVisible()) {
                this.jButtonHelp.addActionListener(GuiUtils.createHelpActionListener(this.jButtonHelp, helpKey));
            }
        }
    }

    @Override
    public void cancel() {
        this.dispose();
    }

    @Override
    public void dispose() {
        this.closeAllPages();
        super.dispose();
    }

    private /* synthetic */ void lambda$initializePages$0(AbstractItemDialogPage page, ActionEvent a) {
        this.showPage(page.getTitle());
    }
}

