/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.explorer;

import java.beans.PropertyChangeEvent;

public class ObservableEvent
extends PropertyChangeEvent {
    private final BasicAction actionCommand;

    public ObservableEvent(BasicAction actionCommand, Object source, Object oldValue, Object newValue) {
        super(source, null, oldValue, newValue);
        if (actionCommand == null) {
            throw new IllegalArgumentException("null source");
        }
        this.actionCommand = actionCommand;
    }

    @Override
    public String getPropertyName() {
        return this.actionCommand.toString();
    }

    public BasicAction getActionCommand() {
        return this.actionCommand;
    }

    @Override
    public Object getSource() {
        return this.source;
    }

    public static enum BasicAction {
        SELECT,
        ADD,
        REMOVE,
        UPDATE,
        UPDATE_PARENT,
        NULL_SELECTION,
        UPDATE_TOOLS,
        UPDATE_TOOLBARS,
        REGISTER,
        UNREGISTER,
        REPLACE,
        LOADING_START,
        LOADING_CANCEL,
        LOADING_STOP,
        LOADING_GLOBAL_MSG;

    }
}

