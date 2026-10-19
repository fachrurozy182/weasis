/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.tp.raven.datetime.component.date.event;

import java.util.EventListener;
import org.weasis.core.ui.tp.raven.datetime.component.date.event.DateSelectionModelEvent;

public interface DateSelectionModelListener
extends EventListener {
    public void dateSelectionModelChanged(DateSelectionModelEvent var1);
}

