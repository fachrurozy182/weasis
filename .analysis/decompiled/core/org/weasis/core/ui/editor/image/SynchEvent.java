/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.editor.image;

import java.util.HashMap;
import java.util.Map;
import org.weasis.core.ui.editor.image.ViewCanvas;

public class SynchEvent {
    private final ViewCanvas<?> view;
    private final Map<String, Object> events;
    private final boolean valueIsAdjusting;

    public SynchEvent(ViewCanvas<?> view) {
        this(view, null, null, false);
    }

    public SynchEvent(ViewCanvas<?> view, String command, Object value) {
        this(view, command, value, false);
    }

    public SynchEvent(ViewCanvas<?> view, String command, Object value, boolean valueIsAdjusting) {
        this.view = view;
        this.valueIsAdjusting = valueIsAdjusting;
        this.events = new HashMap<String, Object>();
        if (command != null) {
            this.getEvents().put(command, value);
        }
    }

    public void put(String key, Object value) {
        this.events.put(key, value);
    }

    public ViewCanvas<?> getView() {
        return this.view;
    }

    public Map<String, Object> getEvents() {
        return this.events;
    }

    public boolean isValueIsAdjusting() {
        return this.valueIsAdjusting;
    }
}

