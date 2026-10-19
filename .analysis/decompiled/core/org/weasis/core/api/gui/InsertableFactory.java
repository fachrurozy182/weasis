/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui;

import java.util.Hashtable;
import org.weasis.core.api.gui.Insertable;

public interface InsertableFactory {
    public Insertable createInstance(Hashtable<String, Object> var1);

    public void dispose(Insertable var1);

    public boolean isComponentCreatedByThisFactory(Insertable var1);

    public Insertable.Type getType();
}

