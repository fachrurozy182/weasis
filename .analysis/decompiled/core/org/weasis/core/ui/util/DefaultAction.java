/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.util;

import java.awt.event.ActionEvent;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.AbstractAction;
import javax.swing.Icon;

public class DefaultAction
extends AbstractAction {
    private final Consumer<ActionEvent> action;

    public DefaultAction(String name, Consumer<ActionEvent> action) {
        super(name);
        this.action = Objects.requireNonNull(action);
    }

    public DefaultAction(String name, Icon icon, Consumer<ActionEvent> action) {
        super(name, icon);
        this.action = Objects.requireNonNull(action);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.action.accept(e);
    }
}

