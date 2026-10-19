/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.pref;

import java.util.Locale;
import java.util.Objects;

public class JLocale {
    private final Locale locale;

    JLocale(Locale l) {
        this.locale = Objects.requireNonNull(l);
    }

    public String toString() {
        return this.locale.getDisplayName();
    }

    public Locale getLocale() {
        return this.locale;
    }
}

