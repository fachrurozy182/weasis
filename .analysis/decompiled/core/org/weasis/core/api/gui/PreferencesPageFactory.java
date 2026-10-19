/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui;

import java.util.Hashtable;
import org.weasis.core.api.gui.Insertable;
import org.weasis.core.api.gui.InsertableFactory;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;

public interface PreferencesPageFactory
extends InsertableFactory {
    @Override
    public AbstractItemDialogPage createInstance(Hashtable<String, Object> var1);

    @Override
    default public void dispose(Insertable component) {
    }

    @Override
    default public Insertable.Type getType() {
        return Insertable.Type.PREFERENCES;
    }
}

