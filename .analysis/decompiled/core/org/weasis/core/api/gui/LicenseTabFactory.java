/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui;

import java.util.Hashtable;
import org.weasis.core.api.gui.InsertableFactory;
import org.weasis.core.api.gui.util.AbstractTabLicense;

public interface LicenseTabFactory
extends InsertableFactory {
    @Override
    public AbstractTabLicense createInstance(Hashtable<String, Object> var1);
}

