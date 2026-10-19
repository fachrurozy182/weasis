/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.ui.pref;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.util.LocalUtil;
import org.weasis.core.ui.pref.JLocale;
import org.weasis.core.ui.pref.JLocalePercentage;
import org.weasis.core.ui.pref.Refreshable;
import org.weasis.core.util.StringUtil;

public class JLocaleLanguage
extends JComboBox<JLocale>
implements ItemListener,
Refreshable {
    private final ArrayList<JLocalePercentage> languages = new ArrayList();
    private final Map<Locale, JLocalePercentage> localeMap = new HashMap<Locale, JLocalePercentage>();
    private JLocalePercentage englishEntry = null;

    public JLocaleLanguage() {
        this.initLocalesWithPercentages();
        this.sortLocales();
    }

    private void initLocalesWithPercentages() {
        String languageList;
        HashMap<String, Integer> percentages = new HashMap<String, Integer>();
        String percentageData = System.getProperty("weasis.languages.percentage", null);
        if (StringUtil.hasText((CharSequence)percentageData)) {
            String[] items;
            for (String item : items = percentageData.split(",")) {
                String entry = item.trim();
                int index = entry.indexOf(46);
                if (index <= 0) continue;
                String langCode = entry.substring(0, index);
                try {
                    int percentage = Integer.parseInt(entry.substring(index + 1));
                    if (percentage <= 30) continue;
                    percentages.put(langCode, percentage);
                }
                catch (NumberFormatException percentage) {
                    // empty catch block
                }
            }
        }
        if ((languageList = System.getProperty("weasis.languages", null)) != null) {
            String[] items;
            for (String s : items = languageList.split(",")) {
                String item = s.trim();
                int index = item.indexOf(32);
                String langCode = index > 0 ? item.substring(0, index) : item;
                Locale locale = LocalUtil.textToLocale(langCode);
                if (locale == null || percentages.getOrDefault(langCode, 0) <= 30) continue;
                JLocalePercentage entry = new JLocalePercentage(locale, (Integer)percentages.get(langCode));
                this.languages.add(entry);
                this.localeMap.put(locale, entry);
                if (!Locale.ENGLISH.equals(locale)) continue;
                this.englishEntry = entry;
            }
        }
        if (this.languages.isEmpty()) {
            JLocalePercentage defaultEntry = new JLocalePercentage(Locale.ENGLISH, 100);
            this.languages.add(defaultEntry);
            this.localeMap.put(Locale.ENGLISH, defaultEntry);
            this.englishEntry = defaultEntry;
        }
    }

    private void sortLocales() {
        Locale defaultLocale = Locale.getDefault();
        Collator collator = Collator.getInstance(defaultLocale);
        this.languages.sort((l1, l2) -> collator.compare(l1.getLocale().getDisplayName(), l2.getLocale().getDisplayName()));
        DefaultComboBoxModel<JLocalePercentage> model = new DefaultComboBoxModel<JLocalePercentage>();
        JLocalePercentage defaultLocaleEntry = null;
        for (JLocalePercentage localePercentage : this.languages) {
            if (localePercentage.getLocale().equals(defaultLocale)) {
                defaultLocaleEntry = localePercentage;
            }
            model.addElement(localePercentage);
        }
        this.setModel(model);
        if (defaultLocaleEntry != null) {
            this.setSelectedItem(defaultLocaleEntry);
        }
    }

    public void selectLocale(String localeCode) {
        JLocalePercentage target;
        Locale selectedLocale = LocalUtil.textToLocale(localeCode);
        Object item = this.getSelectedItem();
        if (item instanceof JLocalePercentage) {
            JLocalePercentage jLocalePercentage = (JLocalePercentage)item;
            if (selectedLocale != null && selectedLocale.equals(jLocalePercentage.getLocale())) {
                return;
            }
        }
        JLocalePercentage jLocalePercentage = target = selectedLocale != null ? this.localeMap.get(selectedLocale) : null;
        if (target == null) {
            target = this.englishEntry;
        }
        if (target != null) {
            this.setSelectedItem(target);
        }
    }

    @Override
    public void itemStateChanged(ItemEvent iEvt) {
        Object item;
        if (iEvt.getStateChange() == 1 && (item = this.getSelectedItem()) instanceof JLocalePercentage) {
            JLocalePercentage jLocalePercentage = (JLocalePercentage)item;
            this.removeItemListener(this);
            Locale locale = jLocalePercentage.getLocale();
            Locale.setDefault(locale);
            GuiUtils.getUICore().getSystemPreferences().setProperty("locale.lang.code", LocalUtil.localeToText(locale));
            this.sortLocales();
            this.addItemListener(this);
            this.firePropertyChange("locale", null, locale);
            this.valueHasChanged();
        }
    }
}

