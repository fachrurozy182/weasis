/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.media.data;

public class SeriesEvent {
    private final Action actionCommand;
    private final Object source;
    private final Object param;

    public SeriesEvent(Action actionCommand, Object source, Object param) {
        this.actionCommand = actionCommand;
        this.source = source;
        this.param = param;
    }

    public Action getActionCommand() {
        return this.actionCommand;
    }

    public Object getSource() {
        return this.source;
    }

    public Object getParam() {
        return this.param;
    }

    public static enum Action {
        UPDATE,
        ADD_IMAGE,
        REMOVE_IMAGE,
        UPDATE_IMAGE,
        PRELOADING;

    }
}

