/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui.util;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Objects;
import org.weasis.core.api.gui.util.ActionState;
import org.weasis.core.api.gui.util.Feature;
import org.weasis.core.api.gui.util.State;

public class BasicActionState
implements ActionState {
    protected final Feature<? extends ActionState> action;
    protected boolean enabled;
    protected final ArrayList<Object> components;

    public BasicActionState(Feature<? extends ActionState> action) {
        this.action = Objects.requireNonNull(action);
        this.components = new ArrayList();
    }

    @Override
    public void enableAction(boolean enabled) {
        this.enabled = enabled;
        for (Object c : this.components) {
            if (c instanceof Component) {
                Component component = (Component)c;
                component.setEnabled(enabled);
                continue;
            }
            if (!(c instanceof State)) continue;
            State state = (State)c;
            state.setEnabled(enabled);
        }
    }

    @Override
    public boolean isActionEnabled() {
        return this.enabled;
    }

    protected ArrayList<Object> getComponents() {
        return this.components;
    }

    @Override
    public Feature<? extends ActionState> getActionW() {
        return this.action;
    }

    @Override
    public boolean registerActionState(Object c) {
        if (!this.components.contains(c)) {
            this.components.add(c);
            if (c instanceof Component) {
                Component component = (Component)c;
                component.setEnabled(this.enabled);
            } else if (c instanceof State) {
                State state = (State)c;
                state.setEnabled(this.enabled);
            }
            return true;
        }
        return false;
    }

    @Override
    public void unregisterActionState(Object c) {
        this.components.remove(c);
    }
}

