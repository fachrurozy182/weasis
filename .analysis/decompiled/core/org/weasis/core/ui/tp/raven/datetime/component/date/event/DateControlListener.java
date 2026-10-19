/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.tp.raven.datetime.component.date.event;

import java.util.EventListener;
import org.weasis.core.ui.tp.raven.datetime.component.date.event.DateControlEvent;

public interface DateControlListener
extends EventListener {
    public void dateControlChanged(DateControlEvent var1);
}

