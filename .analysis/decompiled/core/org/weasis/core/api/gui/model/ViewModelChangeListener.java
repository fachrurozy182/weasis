/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui.model;

import org.weasis.core.api.gui.model.ViewModel;

@FunctionalInterface
public interface ViewModelChangeListener {
    public void handleViewModelChanged(ViewModel var1);
}

