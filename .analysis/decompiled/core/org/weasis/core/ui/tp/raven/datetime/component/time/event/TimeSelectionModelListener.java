/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.tp.raven.datetime.component.time.event;

import java.util.EventListener;
import org.weasis.core.ui.tp.raven.datetime.component.time.event.TimeSelectionModelEvent;

public interface TimeSelectionModelListener
extends EventListener {
    public void timeSelectionModelChanged(TimeSelectionModelEvent var1);
}

