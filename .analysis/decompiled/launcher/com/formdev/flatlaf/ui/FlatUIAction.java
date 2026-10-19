/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.ui;

import java.beans.PropertyChangeListener;
import javax.swing.Action;

public abstract class FlatUIAction
implements Action {
    protected final String name;
    protected final Action delegate;

    protected FlatUIAction(String name) {
        this.name = name;
        this.delegate = null;
    }

    protected FlatUIAction(Action delegate) {
        this.name = null;
        this.delegate = delegate;
    }

    @Override
    public Object getValue(String key) {
        if (key == "Name" && this.delegate == null) {
            return this.name;
        }
        return this.delegate != null ? this.delegate.getValue(key) : null;
    }

    @Override
    public boolean isEnabled() {
        return this.delegate != null ? this.delegate.isEnabled() : true;
    }

    @Override
    public void putValue(String key, Object value) {
    }

    @Override
    public void setEnabled(boolean b) {
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }
}

