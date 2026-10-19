/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui.util;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;
import org.weasis.core.api.gui.util.State;

public interface ComboBoxModelAdapter<T>
extends ListDataListener,
State {
    public void setModel(ComboBoxModel<T> var1);
}

