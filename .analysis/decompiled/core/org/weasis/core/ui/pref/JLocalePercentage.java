/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.pref;

import java.util.Locale;
import org.weasis.core.ui.pref.JLocale;

public class JLocalePercentage
extends JLocale {
    private final int percentage;

    public JLocalePercentage(Locale locale, int percentage) {
        super(locale);
        this.percentage = percentage;
    }

    public int getPercentage() {
        return this.percentage;
    }

    @Override
    public String toString() {
        return super.toString() + " (" + this.percentage + "%)";
    }
}

