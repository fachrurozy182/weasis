/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.core.ui.pref;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.Collator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.util.LocalUtil;
import org.weasis.core.ui.pref.JLocale;
import org.weasis.core.ui.pref.Refreshable;

public class JLocaleFormat
extends JComboBox<JLocale>
implements ItemListener,
Refreshable {
    private static final Logger LOGGER = LoggerFactory.getLogger(JLocaleFormat.class);
    private final Map<Locale, JLocale> localeMap = new HashMap<Locale, JLocale>();
    private Locale pendingLocale = null;

    public JLocaleFormat() {
        DefaultComboBoxModel<JLocale> placeholder = new DefaultComboBoxModel<JLocale>();
        placeholder.addElement(new JLocale(Locale.getDefault(Locale.Category.FORMAT)));
        this.setModel(placeholder);
        this.addItemListener(this);
        new SwingWorker<Object[], Void>(this){
            final /* synthetic */ JLocaleFormat this$0;
            {
                JLocaleFormat jLocaleFormat = this$0;
                Objects.requireNonNull(jLocaleFormat);
                this.this$0 = jLocaleFormat;
            }

            @Override
            protected Object[] doInBackground() {
                return JLocaleFormat.buildSortedData();
            }

            @Override
            protected void done() {
                if (!this.isCancelled()) {
                    try {
                        this.this$0.removeItemListener(this.this$0);
                        this.this$0.applyModel((Object[])this.get());
                        this.this$0.addItemListener(this.this$0);
                        this.this$0.handleLocaleChange(this.this$0.getSelectedItem());
                    }
                    catch (InterruptedException | ExecutionException e) {
                        LOGGER.error("Failed to load format locales", (Throwable)e);
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }.execute();
    }

    private static Object[] buildSortedData() {
        Locale[] locales = Locale.getAvailableLocales();
        Locale defaultLocale = Locale.getDefault();
        Collator collator = Collator.getInstance(defaultLocale);
        HashMap<Locale, String> displayNameCache = new HashMap<Locale, String>(locales.length * 2);
        for (Locale l : locales) {
            displayNameCache.put(l, l.getDisplayName());
        }
        Arrays.sort(locales, (l1, l2) -> collator.compare((String)displayNameCache.get(l1), (String)displayNameCache.get(l2)));
        HashMap<Locale, JLocale> map = new HashMap<Locale, JLocale>(locales.length * 2);
        DefaultComboBoxModel<JLocale> model = new DefaultComboBoxModel<JLocale>();
        for (Locale locale : locales) {
            JLocale val = new JLocale(locale);
            map.put(locale, val);
            model.addElement(val);
        }
        return new Object[]{model, map};
    }

    private void applyModel(Object[] data) {
        this.localeMap.clear();
        this.localeMap.putAll((Map)data[1]);
        this.setModel((DefaultComboBoxModel)data[0]);
        Locale toSelect = this.pendingLocale != null ? this.pendingLocale : Locale.getDefault(Locale.Category.FORMAT);
        this.pendingLocale = null;
        JLocale target = this.localeMap.get(toSelect);
        if (target != null) {
            this.setSelectedItem(target);
        }
    }

    public void selectLocale() {
        JLocale jLocale;
        Locale sLoc = Locale.getDefault(Locale.Category.FORMAT);
        if (this.localeMap.isEmpty()) {
            this.pendingLocale = sLoc;
            return;
        }
        Object item = this.getSelectedItem();
        if (item instanceof JLocale && sLoc.equals((jLocale = (JLocale)item).getLocale())) {
            return;
        }
        JLocale target = this.localeMap.get(sLoc);
        if (target != null) {
            this.setSelectedItem(target);
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == 1) {
            this.handleLocaleChange(e.getItem());
        }
    }

    private void handleLocaleChange(Object item) {
        if (item instanceof JLocale) {
            JLocale jLocale = (JLocale)item;
            this.storeLocale(jLocale.getLocale());
            this.valueHasChanged();
        }
    }

    private void storeLocale(Locale locale) {
        GuiUtils.getUICore().getSystemPreferences().setProperty("locale.format.code", LocalUtil.localeToText(locale));
        Locale.setDefault(Locale.Category.FORMAT, locale);
    }

    public void refresh() {
        this.removeItemListener(this);
        this.applyModel(JLocaleFormat.buildSortedData());
        this.selectLocale();
        this.handleLocaleChange(this.getSelectedItem());
        this.addItemListener(this);
    }
}

