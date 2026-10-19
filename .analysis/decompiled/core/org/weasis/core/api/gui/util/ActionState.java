/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui.util;

import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.Feature;

public interface ActionState {
    public void enableAction(boolean var1);

    public boolean isActionEnabled();

    public Feature<? extends ActionState> getActionW();

    public boolean registerActionState(Object var1);

    public void unregisterActionState(Object var1);

    public static enum NoneLabel {
        NONE(Messages.getString("ActionState.none")),
        NONE_SERIES(Messages.getString("ActionState.none_all"));

        private final String title;

        private NoneLabel(String title) {
            this.title = title;
        }

        public String getTitle() {
            return this.title;
        }

        public String toString() {
            return this.title;
        }
    }
}

